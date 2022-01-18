/*
 * Copyright (C) 2019-2022 Terence Tan
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

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.support.wearable.watchface.decomposition.ImageComponent;
import android.support.wearable.watchface.decomposition.WatchFaceDecomposition;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import pro.watchkit.wearable.watchface.model.WatchFaceState;

class WatchFaceGlobalCacheDrawable extends LayerDrawable
        implements WatchFaceGlobalDrawable.WatchFaceDecompositionComponent {
    private WatchFaceState mWatchFaceState;
    @NonNull
    private final Drawable[] mWatchPartDrawables;
    private int mPreviousSerial = -1;
    private Bitmap mActiveCacheBitmap;
    private Bitmap mActiveHardwareCacheBitmap;
    private Canvas mActiveCacheCanvas;
    @NonNull
    private final Path mActiveExclusionPath = new Path();
    @NonNull
    private final Path mActiveInnerGlowPath = new Path();
    private Bitmap mAmbientCacheBitmap;
    private Bitmap mAmbientHardwareCacheBitmap;
    private Canvas mAmbientCacheCanvas;
    @NonNull
    private final Path mAmbientExclusionPath = new Path();
    @NonNull
    private final Path mAmbientInnerGlowPath = new Path();
    private Path mExclusionPath;
    private Path mInnerGlowPath;
    @NonNull
    private final Path mCacheExclusionPath = new Path();
    @NonNull
    private final Path mCacheInnerGlowPath = new Path();

    /**
     * If we're rendering a decomposition, it's rendered here.
     */
    @Nullable
    private Bitmap mDecompositionIntermediateBitmap;

    /**
     * If we're rendering a decomposition, it's rendered into
     * "mDecompositionIntermediateBitmap", then mapped to 16 colors here.
     */
    @Nullable
    private Bitmap mDecompositionDestBitmap;

    /**
     * If we're rendering a decomposition, we draw here, which
     * renders into "mDecompositionDestBitmap".
     */
    @Nullable
    private Canvas mDecompositionIntermediateCanvas;

    /**
     * A private copy of our current ambient tint. Useful for caching.
     */
    @ColorInt
    private int mCurrentAmbientTint;

    /**
     * Is our decomposition dirty and ready to be redrawn? We mark this as true every time
     * we update our caches. But if we don't need to redraw our decomposition (or, more to the
     * point, if we don't need to re-send it to the offload processor) then we avoid it.
     */
    private boolean mIsAmbientCacheBitmapDirty = false;

    WatchFaceGlobalCacheDrawable(int flags) {
        this(WatchFaceGlobalDrawable.buildDrawables(null, flags));
    }

    private WatchFaceGlobalCacheDrawable(@NonNull Drawable[] watchPartDrawables) {
        super(watchPartDrawables);

        mWatchPartDrawables = watchPartDrawables;
    }

    void setWatchFaceState(@NonNull WatchFaceState watchFaceState, @NonNull Path exclusionPath,
                           @NonNull Path innerGlowPath) {
        mWatchFaceState = watchFaceState;
        mExclusionPath = exclusionPath;
        mInnerGlowPath = innerGlowPath;

        for (Drawable d : mWatchPartDrawables) {
            if (d instanceof WatchPartDrawable) {
                ((WatchPartDrawable) d).setWatchFaceState(
                        mWatchFaceState, mCacheExclusionPath, mCacheInnerGlowPath);
            }
        }
    }

    // Stats start
    @NonNull
    Drawable[] getWatchPartDrawables() {
        return mWatchPartDrawables;
    }
    // Stats end

    @Override
    protected void onBoundsChange(@NonNull Rect bounds) {
        super.onBoundsChange(bounds);

        if (bounds.width() == 0 || bounds.height() == 0) {
            return;
        }

        mActiveCacheBitmap = Bitmap.createBitmap(
                bounds.width(), bounds.height(), Bitmap.Config.ARGB_8888);
        mActiveCacheCanvas = new Canvas(mActiveCacheBitmap);
        mAmbientCacheBitmap = Bitmap.createBitmap(
                bounds.width(), bounds.height(), Bitmap.Config.ARGB_8888);
        mAmbientCacheCanvas = new Canvas(mAmbientCacheBitmap);

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
            mAmbientCacheCanvas.drawColor(Color.TRANSPARENT, Mode.CLEAR); // Clear it first.
            mActiveCacheCanvas.drawColor(Color.TRANSPARENT, Mode.CLEAR); // Clear it first.

            // Pre-cache our ambient canvas.
            mCacheExclusionPath.reset();
            mWatchFaceState.setAmbient(true);
            super.draw(mAmbientCacheCanvas);
            mAmbientExclusionPath.set(mCacheExclusionPath);
            mAmbientInnerGlowPath.set(mCacheInnerGlowPath);

            // Pre-cache our active canvas.
            mCacheExclusionPath.reset();
            mWatchFaceState.setAmbient(false);
            super.draw(mActiveCacheCanvas);
            mActiveExclusionPath.set(mCacheExclusionPath);
            mActiveInnerGlowPath.set(mCacheInnerGlowPath);

            // And back to how we were.
            mWatchFaceState.setAmbient(currentAmbient);
            mPreviousSerial = currentSerial;

            // Flag "mAmbientCacheBitmap" as updated and requiring copying to our ImageComponent.
            mIsAmbientCacheBitmapDirty = true;

            Bitmap.Config config = Bitmap.Config.ARGB_8888;
            if (Build.VERSION.SDK_INT >= 26 && mWatchFaceState.isHardwareAccelerationEnabled()) {
                // Hardware power!
                config = Bitmap.Config.HARDWARE;
            }
            mAmbientHardwareCacheBitmap = mAmbientCacheBitmap.copy(config, false);
            mAmbientHardwareCacheBitmap.prepareToDraw();
            mActiveHardwareCacheBitmap = mActiveCacheBitmap.copy(config, false);
            mActiveHardwareCacheBitmap.prepareToDraw();
        }

        // Then copy our cache to the results!
        mExclusionPath.set(
                mWatchFaceState.isAmbient() ? mAmbientExclusionPath : mActiveExclusionPath);
        mInnerGlowPath.set(
                mWatchFaceState.isAmbient() ? mAmbientInnerGlowPath : mActiveInnerGlowPath);
    }

    /**
     * Draw into the given canvas. (Updates our cache bitmaps first, if necessary.)
     *
     * @param canvas Canvas to draw into
     */
    @Override
    public void draw(@NonNull Canvas canvas) {
        regenerateCacheBitmaps();

        Bitmap mHardwareCacheBitmap = mWatchFaceState.isAmbient() ?
                mAmbientHardwareCacheBitmap : mActiveHardwareCacheBitmap;
        Bitmap mCacheBitmap = mWatchFaceState.isAmbient() ?
                mAmbientCacheBitmap : mActiveCacheBitmap;
        canvas.drawBitmap(mHardwareCacheBitmap != null ? mHardwareCacheBitmap : mCacheBitmap,
                0, 0, null);
    }

    /**
     * Build the watch face decomposition into "builder". In this case, it'll be our cache
     * drawables and (also) our non-time-dependent complications.
     *
     * @param builder WatchFaceDecomposition builder to build into.
     * @param idA     AtomicInteger for the component ID, which we will increment
     * @return The time at which this decomposition expires, at which point, call this again
     */
    @Override
    public long buildWatchFaceDecompositionComponents(
            @NonNull WatchFaceDecomposition.Builder builder, @NonNull AtomicInteger idA) {
        // Our generated decomposition image goes into mDecompositionDestBitmap.
        // Regenerate it if there are updates available? Skip if unnecessary.
        if (hasDecompositionUpdateAvailable(mWatchFaceState.getTimeInMillis())) {
            // Copy "mAmbientCacheBitmap" into an intermediate Bitmap we can draw all over.
            if (mDecompositionIntermediateBitmap == null ||
                    mDecompositionDestBitmap == null ||
                    mDecompositionIntermediateCanvas == null ||
                    mDecompositionDestBitmap.getWidth() != mAmbientCacheBitmap.getWidth() ||
                    mDecompositionDestBitmap.getHeight() != mAmbientCacheBitmap.getHeight()) {
                if (mDecompositionIntermediateBitmap != null) {
                    mDecompositionIntermediateBitmap.recycle(); // If there's an old bitmap, recycle it.
                }
                if (mDecompositionDestBitmap != null) {
                    mDecompositionDestBitmap.recycle(); // Ditto.
                }
                mDecompositionDestBitmap = Bitmap.createBitmap(
                        mAmbientCacheBitmap.getWidth(), mAmbientCacheBitmap.getHeight(),
                        Bitmap.Config.ARGB_8888);
                mDecompositionIntermediateBitmap = Bitmap.createBitmap(
                        mAmbientCacheBitmap.getWidth(), mAmbientCacheBitmap.getHeight(),
                        Bitmap.Config.ARGB_8888);
                mDecompositionIntermediateCanvas = new Canvas(mDecompositionIntermediateBitmap);
            }
            assert mDecompositionIntermediateBitmap != null;
            assert mDecompositionDestBitmap != null;
            assert mDecompositionIntermediateCanvas != null;

            // Copy the existing ambient bitmap into "mDecompositionDestBitmap2".
            mDecompositionIntermediateCanvas.drawBitmap(mAmbientCacheBitmap, 0, 0, null);

            // Hack all our existing (non-time-dependent) complications in.
            boolean wasAmbient = mWatchFaceState.isAmbient();
            mWatchFaceState.setAmbient(true);
            // Just draw each (non-time-dependent) complication straight over the top.
            mWatchFaceState.getComplicationsForDrawing(getBounds())
                    .stream().filter(c -> c.isForeground && !c.isTimeDependent())
                    .forEach(c -> c.drawAmbientCache(mDecompositionIntermediateCanvas));
            mWatchFaceState.setAmbient(wasAmbient);

            // Fast map from "mDecompositionIntermediateBitmap" to "mDecompositionDestBitmap".
            // Using RenderScript!
            mWatchFaceState.getPaintBox().mapBitmapWith8LevelsFromBlack(
                    mWatchFaceState.getAmbientTint(), mDecompositionIntermediateBitmap,
                    mDecompositionDestBitmap);

            // Save a copy of the ambient tint we used to draw this,
            // so next time we won't (necessarily) run this path.
            mCurrentAmbientTint = mWatchFaceState.getAmbientTint();

            // Note that we've drawn the updated "mAmbientCacheBitmap",
            // so next time we won't (necessarily) run this path.
            mIsAmbientCacheBitmapDirty = false;
        }

        int baseId = idA.getAndIncrement();

        // OK, build and add our ImageComponent to the decomposition.
        builder.addImageComponents(new ImageComponent.Builder()
                .setComponentId(baseId)
                .setZOrder(baseId)
                .setImage(Icon.createWithBitmap(mDecompositionDestBitmap))
                .setBounds(new RectF(0f, 0f, 1f, 1f)) // Entire screen
                .build());

        // This doesn't need updating on a schedule.
        return Long.MAX_VALUE;
    }

    /**
     * Does this WatchFaceDecomposition component have an update available? We ask because if
     * there are no updates available, we want to avoid sending updates to the offload
     * processor.
     *
     * @param currentTimeMillis The time when we're asking if there's an update available
     * @return Whether the update is available?
     */
    @Override
    public boolean hasDecompositionUpdateAvailable(long currentTimeMillis) {
        // Are there any updated non-time-dependent complications?
        boolean hasUpdatedComplicationData = mWatchFaceState.getComplications().stream()
                .filter(c -> c.isForeground && !c.isTimeDependent())
                .map(c -> c.checkUpdatedComplicationData(currentTimeMillis))
                .reduce(false, (a, b) -> a || b);

        // Regenerate our cache bitmaps; if there's nothing to do, this returns quickly.
        regenerateCacheBitmaps();

        // Regenerate the decomposition if our (non-time-dependent) complications have changed.
        // Regenerate the decomposition if the ambient cache bitmap (ticks & digits) has changed.
        // Regenerate the decomposition if our ambient tint color has changed (this happens
        // regularly during dusk and dawn).
        // Regenerate the decomposition if we've never drawn it before!
        return hasUpdatedComplicationData || mIsAmbientCacheBitmapDirty ||
                mCurrentAmbientTint != mWatchFaceState.getAmbientTint() ||
                mDecompositionDestBitmap == null;
    }
}
