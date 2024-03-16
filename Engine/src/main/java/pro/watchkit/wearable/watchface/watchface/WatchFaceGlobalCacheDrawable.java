/*
 * Copyright (C) 2019-2024 Terence Tan
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
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;

import androidx.annotation.NonNull;

import java.util.Objects;

import pro.watchkit.wearable.watchface.model.WatchFaceStateBase;

abstract class WatchFaceGlobalCacheDrawable extends LayerDrawable {
    WatchFaceStateBase mWatchFaceState;
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
    Bitmap mAmbientCacheBitmap;
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
     * Is our decomposition dirty and ready to be redrawn? We mark this as true every time
     * we update our caches. But if we don't need to redraw our decomposition (or, more to the
     * point, if we don't need to re-send it to the offload processor) then we avoid it.
     */
    boolean mIsAmbientCacheBitmapDirty = false;

    WatchFaceGlobalCacheDrawable(@NonNull Drawable[] watchPartDrawables) {
        super(watchPartDrawables);

        mWatchPartDrawables = watchPartDrawables;
    }

    void setWatchFaceState(@NonNull WatchFaceStateBase watchFaceState, @NonNull Path exclusionPath,
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

    void regenerateCacheBitmaps() {
        // Invalidate our bits as required.
        // Invalidate if complications, unread notifications or total notifications have changed.
        // Or the entire preset. Or if we've flipped between active and ambient.
        // Or anything else of interest in the WatchFaceState.
        int currentSerial = Objects.hash(mWatchFaceState);
        if (mPreviousSerial != currentSerial) {
            // Recycle our previous hardware bitmaps, to keep our memory usage down.
            if (mActiveHardwareCacheBitmap != null) {
                mActiveHardwareCacheBitmap.recycle();
                mActiveHardwareCacheBitmap = null;
            }
            if (mAmbientHardwareCacheBitmap != null) {
                mAmbientHardwareCacheBitmap.recycle();
                mAmbientHardwareCacheBitmap = null;
            }

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
            android.util.Log.d("Watch1", mActiveCacheCanvas.toString());
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
}
