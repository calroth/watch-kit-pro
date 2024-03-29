/*
 * Copyright (C) 2022 Terence Tan
 *
 *  This file is free software: you may copy, redistribute and/or modify it
 *  under the terms of the GNU General Public License as published by the
 *  Free Software Foundation, either version 3 of the License, or (at your
 *  option) any later version.
 *
 *  This file is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package pro.watchkit.wearable.watchface.watchface;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff.Mode;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import pro.watchkit.wearable.watchface.model.PaintBox;
import pro.watchkit.wearable.watchface.model.WatchFaceState;

/**
 * A WatchFaceGlobalDrawable that quickly renders a placeholder to begin with,
 * then does all the slow drawing on a background thread. After each layer, it
 * pings a callback to draw the result. So it incrementally updates until everything
 * is drawn.
 */
public class WatchFaceGlobalDeferredDrawable extends Drawable {
    private final WatchFaceState mWatchFaceState;
    private int mPreviousSerial = -1;
    private Bitmap mCacheBitmap;
    private Bitmap mHardwareCacheBitmap;
    private final Object mHardwareCacheBitmapLock = new Object();
    private Canvas mCacheCanvas;
    @NonNull
    private final View mParentView;
    private WatchPartClipDrawable mClipDrawable;
    private WatchPartComplicationsDrawable mComplicationsDrawable;
    @NonNull
    private final Drawable[] mWatchPartDrawables;

    public WatchFaceGlobalDeferredDrawable(
            @NonNull Context context, int flags, @NonNull View parentView) {
        mWatchPartDrawables = WatchFaceGlobalDrawable.buildDrawables(null, flags);

        mWatchFaceState = new WatchFaceState(context);
        mParentView = parentView;
        Path mExclusionPath = new Path();
        Path mInnerGlowPath = new Path();

        for (Drawable d : mWatchPartDrawables) {
            if (d instanceof WatchPartDrawable) {
                ((WatchPartDrawable) d).setWatchFaceState(
                        mWatchFaceState, mExclusionPath, mInnerGlowPath);
                if (d instanceof WatchPartClipDrawable) {
                    mClipDrawable = (WatchPartClipDrawable) d;
                } else if (d instanceof WatchPartComplicationsDrawable) {
                    mComplicationsDrawable = (WatchPartComplicationsDrawable) d;
                }
            }
        }
    }

    @Override
    public void setAlpha(int alpha) {
        // Unused
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        // Unused
    }

    @Override
    public int getOpacity() {
        // No op.
        return PixelFormat.UNKNOWN;
    }

    @NonNull
    public WatchFaceState getWatchFaceState() {
        return mWatchFaceState;
    }

    @Override
    protected void onBoundsChange(@NonNull Rect bounds) {
        super.onBoundsChange(bounds);

        if (bounds.width() == 0 || bounds.height() == 0) {
            return;
        }

        mCacheBitmap = Bitmap.createBitmap(
                bounds.width(), bounds.height(), Bitmap.Config.ARGB_8888);
        mCacheCanvas = new Canvas(mCacheBitmap);

        mPreviousSerial = -1;

        // Pre-cache bounds change...
        mWatchFaceState.getComplicationsForDrawing(bounds);

        // Propagate our bounds change to our drawables.
        for (Drawable d : mWatchPartDrawables) {
            d.setBounds(bounds);
        }
    }

    private void regenerateCacheBitmaps() {
        // Invalidate our bits as required.
        // Invalidate if complications, unread notifications or total notifications have changed.
        // Or the entire preset. Or if we've flipped between active and ambient.
        // Or anything else of interest in the WatchFaceState.
        int currentSerial = Objects.hash(mWatchFaceState);
        if (mPreviousSerial != currentSerial) {
            mPreviousSerial = currentSerial;
            // Keep track of what our ambient currently is, because we're about to draw them both.
            boolean currentAmbient = mWatchFaceState.isAmbient();

            // Cache invalid. Draw into our cache canvas.
            mCacheCanvas.drawColor(Color.TRANSPARENT, Mode.CLEAR); // Clear it first.

            // Pre-cache our active canvas.
            mWatchFaceState.setAmbient(false);

            // Quick hack: make sure the PaintBox is up-to-date with all our settings.
            mWatchFaceState.getPaintBox(); // This calls "regeneratePaints".

            Bitmap.Config config = Bitmap.Config.ARGB_8888;
            if (Build.VERSION.SDK_INT >= 26 && mWatchFaceState.isHardwareAccelerationEnabled()) {
                // Hardware power!
                config = Bitmap.Config.HARDWARE;
            }

            // Draw each of our layers.
            // After each draw, post an invalidate so that it's copied to screen.
            // So we incrementally draw each layer until it's all done.
            for (Drawable d : mWatchPartDrawables) {
                if (Thread.currentThread().isInterrupted()) {
                    continue; // Oh, what do you mean I have to check on this for myself?
                }
                if (d == mComplicationsDrawable) {
                    continue; // Don't even attempt to draw the complications (yet).
                }
                d.draw(mCacheCanvas);
                if (d == mClipDrawable) {
                    continue; // I mean, don't blit the WatchPartClipDrawable to screen...
                }
                final Bitmap newHardwareCacheBitmap = mCacheBitmap.copy(config, false);
                newHardwareCacheBitmap.prepareToDraw();
                synchronized (mHardwareCacheBitmapLock) {
                    // Quickly swap out the "old" mHardwareCacheBitmap with the new one.
                    // Recycle the old one. So we don't have old bitmaps piling up.
                    // Temporarily commenting out whilst I can decide whether this is
                    // causing bugs...
//                        if (mHardwareCacheBitmap != null) {
//                            mHardwareCacheBitmap.recycle();
//                        }
                    mHardwareCacheBitmap = newHardwareCacheBitmap;
                    // We synchronise to ensure "mHardwareCacheBitmap" doesn't get recycled
                    // whilst we're still drawing it.
                }
                // Force a redraw. "postInvalidate" is what you call from a non-UI thread.
                mParentView.postInvalidate();
            }

            // And back to how we were.
            mWatchFaceState.setAmbient(currentAmbient);
        }
    }

    /**
     * Our thread pool for background tasks.
     */
    private static final ExecutorService mExecutorService = Executors.newCachedThreadPool();

    /**
     * Our background task.
     */
    private Future<?> mBackgroundTask = null;

    /**
     * Draw into the given canvas. (Updates our cache bitmaps first, if necessary.)
     *
     * @param canvas Canvas to draw into
     */
    @Override
    public void draw(@NonNull Canvas canvas) {
        int currentSerial = Objects.hash(mWatchFaceState);
        if (mPreviousSerial != currentSerial) {
            // Something's changed (or we're drawing for the first time).
            drawPlaceholder(canvas);

            // Schedule a background thread redraw -- if not already happening?
            if (mBackgroundTask == null || mBackgroundTask.isDone()) {
                mBackgroundTask = mExecutorService.submit(this::regenerateCacheBitmaps);
            }
        } else if (mHardwareCacheBitmap != null) {
            // We've drawn something to the incremental bitmap. Display it.
            synchronized (mHardwareCacheBitmapLock) {
                // We synchronise to ensure "mHardwareCacheBitmap" doesn't get recycled
                // whilst we're still drawing it.
                if (!mHardwareCacheBitmap.isRecycled()) {
                    canvas.drawBitmap(mHardwareCacheBitmap, 0, 0, null);
                }
            }
            // Now draw the complications, if we have them.
            if (mComplicationsDrawable != null) {
                mComplicationsDrawable.draw(canvas);
            }
        } else {
            // We haven't yet drawn anything to the incremental bitmap.
            // That's OK, sometimes it takes a while for PaintBox to init.
            // Just draw the placeholder.
            drawPlaceholder(canvas);
        }
    }

    public void cancelBackgroundTasks() {
        if (mBackgroundTask != null) {
            mBackgroundTask.cancel(true);
        }
    }

    /**
     * A reusable Paint we use for drawing placeholders.
     */
    private final Paint mPlaceholderPaint = new Paint();

    /**
     * Draw a fast and cheap placeholder whilst we draw our main watch face in the background.
     *
     * @param canvas Canvas to draw into
     */
    private void drawPlaceholder(@NonNull Canvas canvas) {
        float mCenterX = canvas.getWidth() / 2f;
        float mCenterY = canvas.getHeight() / 2f;

        // We draw something that looks similar to the base-accent material...
        int colorA = mWatchFaceState.getColor(PaintBox.ColorType.BASE);
        int colorB = mWatchFaceState.getColor(PaintBox.ColorType.ACCENT);

        // Draw a quick placeholder gradient with no texture.
        switch (mWatchFaceState.getBaseAccentMaterialGradient()) {
            case FLAT:
                mPlaceholderPaint.reset();
                mPlaceholderPaint.setColor(colorA);
                mPlaceholderPaint.setShader(null);
                break;
            case RIPPLE:
                // For triangle, that's expensive, just draw flat something half A and half B.
                mPlaceholderPaint.reset();
                mPlaceholderPaint.setColor(
                        PaintBox.getIntermediateColorFast(colorA, colorB, 0.5d));
                mPlaceholderPaint.setShader(null);
                break;
            case SWEEP:
                int[] grad0 = new int[]{
                        PaintBox.getIntermediateColorFast(colorA, colorB, 1.0d), // Original
                        PaintBox.getIntermediateColorFast(colorA, colorB, 0.8d),
                        PaintBox.getIntermediateColorFast(colorA, colorB, 0.6d),
                        PaintBox.getIntermediateColorFast(colorA, colorB, 0.4d),
                        PaintBox.getIntermediateColorFast(colorA, colorB, 0.2d),
                        PaintBox.getIntermediateColorFast(colorA, colorB, 0.0d), // Original
                        PaintBox.getIntermediateColorFast(colorA, colorB, 0.2d),
                        PaintBox.getIntermediateColorFast(colorA, colorB, 0.4d),
                        PaintBox.getIntermediateColorFast(colorA, colorB, 0.6d),
                        PaintBox.getIntermediateColorFast(colorA, colorB, 0.8d),
                        PaintBox.getIntermediateColorFast(colorA, colorB, 1.0d), // Original
                        PaintBox.getIntermediateColorFast(colorA, colorB, 0.8d),
                        PaintBox.getIntermediateColorFast(colorA, colorB, 0.6d),
                        PaintBox.getIntermediateColorFast(colorA, colorB, 0.4d),
                        PaintBox.getIntermediateColorFast(colorA, colorB, 0.2d),
                        PaintBox.getIntermediateColorFast(colorA, colorB, 0.0d), // Original
                        PaintBox.getIntermediateColorFast(colorA, colorB, 0.2d),
                        PaintBox.getIntermediateColorFast(colorA, colorB, 0.4d),
                        PaintBox.getIntermediateColorFast(colorA, colorB, 0.6d),
                        PaintBox.getIntermediateColorFast(colorA, colorB, 0.8d),
                        PaintBox.getIntermediateColorFast(colorA, colorB, 1.0d), // Original
                };
                mPlaceholderPaint.reset();
                mPlaceholderPaint.setStyle(Paint.Style.FILL);
                mPlaceholderPaint.setShader(new SweepGradient(mCenterX, mCenterY, grad0, null));
                break;
            case RADIAL:
            default:
                int[] grad1 = new int[]{
                        colorB, // Original
                        colorB,
                        colorB,
                        colorB,
                        colorB,
                        colorB, // Original
                        colorB,
                        colorB,
                        PaintBox.getIntermediateColorFast(colorA, colorB, 0.025d), // Taper it in
                        PaintBox.getIntermediateColorFast(colorA, colorB, 0.05d),
                        PaintBox.getIntermediateColorFast(colorA, colorB, 0.1d), // Not original
                        PaintBox.getIntermediateColorFast(colorA, colorB, 0.2d),
                        PaintBox.getIntermediateColorFast(colorA, colorB, 0.4d),
                        PaintBox.getIntermediateColorFast(colorA, colorB, 0.6d),
                        PaintBox.getIntermediateColorFast(colorA, colorB, 0.8d),
                        PaintBox.getIntermediateColorFast(colorA, colorB, 0.9d), // Not original
                        PaintBox.getIntermediateColorFast(colorA, colorB, 0.95d),
                        PaintBox.getIntermediateColorFast(colorA, colorB, 0.975d), // Taper it out
                        colorA,
                        colorA,
                        colorA // Original
                };
                mPlaceholderPaint.reset();
                mPlaceholderPaint.setStyle(Paint.Style.FILL);
                mPlaceholderPaint.setShader(new RadialGradient(
                        mCenterX, mCenterY, mCenterY, grad1, null, Shader.TileMode.CLAMP));
                break;
        }

        // Clip our screen if we've got a clip drawable.
        if (mClipDrawable != null) {
            mClipDrawable.draw2(canvas);
        }

        // And draw!
        canvas.drawPaint(mPlaceholderPaint);
    }
}
