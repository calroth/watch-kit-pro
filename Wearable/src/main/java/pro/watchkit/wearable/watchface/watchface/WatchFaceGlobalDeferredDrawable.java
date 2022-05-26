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
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff.Mode;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.view.View;

import androidx.annotation.NonNull;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import pro.watchkit.wearable.watchface.model.PaintBox;
import pro.watchkit.wearable.watchface.model.WatchFaceState;

/**
 * A WatchFaceGlobalDrawable that quickly renders a placeholder to begin with,
 * then does all the slow drawing on a background thread. When that's done, it
 * pings a callback to draw the result.
 */
public class WatchFaceGlobalDeferredDrawable extends LayerDrawable {
    private final WatchFaceState mWatchFaceState;
    private int mPreviousSerial = -1;
    private Bitmap mCacheBitmap;
    private Bitmap mHardwareCacheBitmap;
    private Canvas mCacheCanvas;
    @NonNull
    private final View mParentView;
    private WatchPartClipDrawable mClipDrawable;

    public WatchFaceGlobalDeferredDrawable(
            @NonNull Context context, int flags, @NonNull View parentView) {
        this(context, WatchFaceGlobalDrawable.buildDrawables(null, flags), parentView);
    }

    private WatchFaceGlobalDeferredDrawable(
            @NonNull Context context, @NonNull Drawable[] watchPartDrawables,
            @NonNull View parentView) {
        super(watchPartDrawables);

        mWatchFaceState = new WatchFaceState(context);
        mParentView = parentView;
        Path mExclusionPath = new Path();
        Path mInnerGlowPath = new Path();

        for (Drawable d : watchPartDrawables) {
            if (d instanceof WatchPartDrawable) {
                ((WatchPartDrawable) d).setWatchFaceState(
                        mWatchFaceState, mExclusionPath, mInnerGlowPath);
                if (d instanceof WatchPartClipDrawable) {
                    mClipDrawable = (WatchPartClipDrawable) d;
                }
            }
        }
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
    }

    private void regenerateCacheBitmaps() {
        // Invalidate our bits as required.
        // Invalidate if complications, unread notifications or total notifications have changed.
        // Or the entire preset. Or if we've flipped between active and ambient.
        // Or anything else of interest in the WatchFaceState.
        int currentSerial = Objects.hash(mWatchFaceState);
        if (mPreviousSerial != currentSerial) {
            // Keep track of what our ambient currently is, because we're about to draw them both.
            boolean currentAmbient = mWatchFaceState.isAmbient();

            // Cache invalid. Draw into our cache canvas.
            mCacheCanvas.drawColor(Color.TRANSPARENT, Mode.CLEAR); // Clear it first.

            // Pre-cache our active canvas.
            mWatchFaceState.setAmbient(false);
            super.draw(mCacheCanvas);

            // And back to how we were.
            mWatchFaceState.setAmbient(currentAmbient);
            mPreviousSerial = currentSerial;

            Bitmap.Config config = Bitmap.Config.ARGB_8888;
            if (Build.VERSION.SDK_INT >= 26 && mWatchFaceState.isHardwareAccelerationEnabled()) {
                // Hardware power!
                config = Bitmap.Config.HARDWARE;
            }
            mHardwareCacheBitmap = mCacheBitmap.copy(config, false);
            mHardwareCacheBitmap.prepareToDraw();
        }

        // Force a redraw. "postInvalidate" is what you call from a non-UI thread.
        mParentView.postInvalidate();
    }

    /**
     * Our thread pool for background tasks.
     */
    private static final ExecutorService mExecutorService = Executors.newCachedThreadPool();

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

            // Schedule a background thread redraw.
            mExecutorService.execute(this::regenerateCacheBitmaps);
        } else {
            // Nothing's changed or we've finished our background draw.
            // Blit it to the screen.
            canvas.drawBitmap(mHardwareCacheBitmap != null ? mHardwareCacheBitmap : mCacheBitmap,
                    0, 0, null);
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
            case TRIANGLE:
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
