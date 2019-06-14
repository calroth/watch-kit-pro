package pro.watchkit.wearable.watchface.watchface;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;

import androidx.annotation.NonNull;

import java.util.Objects;

import pro.watchkit.wearable.watchface.model.WatchFaceState;

class WatchFaceGlobalCacheDrawable extends LayerDrawable {
    private WatchFaceState mWatchFaceState;
    private Drawable[] mWatchPartDrawables;
    private int mPreviousSerial = -1;
    private Bitmap mActiveCacheBitmap;
    private Bitmap mActiveHardwareCacheBitmap;
    private Canvas mActiveCacheCanvas;
    private Bitmap mAmbientCacheBitmap;
    private Bitmap mAmbientHardwareCacheBitmap;
    private Canvas mAmbientCacheCanvas;
    private Path mExclusionPath;
    @NonNull
    private Path mCacheExclusionPath = new Path();

    WatchFaceGlobalCacheDrawable(int flags) {
        this(WatchFaceGlobalDrawable.buildDrawables(null, flags));
    }

    private WatchFaceGlobalCacheDrawable(@NonNull Drawable[] watchPartDrawables) {
        super(watchPartDrawables);

        mWatchPartDrawables = watchPartDrawables;
    }

    void setWatchFaceState(@NonNull WatchFaceState watchFaceState, @NonNull Path path) {
        mWatchFaceState = watchFaceState;
        mExclusionPath = path;

        for (Drawable d : mWatchPartDrawables) {
            if (d instanceof WatchPartDrawable) {
                ((WatchPartDrawable) d).setWatchFaceState(mWatchFaceState, mCacheExclusionPath);
            }
        }
    }

    // Stats start
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

    @Override
    public void draw(@NonNull Canvas canvas) {
        // Invalidate our bits as required.
        // Invalidate if complications, unread notifications or total notifications have changed.
        // Or the entire preset. Or if we've flipped between active and ambient.
        // Or anything else of interest in the WatchFaceState.
        int currentSerial = Objects.hash(mWatchFaceState);
        if (mPreviousSerial != currentSerial) {
            // Keep track of what our ambient currently is, because we're about to draw them both.
            boolean currentAmbient = mWatchFaceState.isAmbient();

            // Cache invalid. Draw into our cache canvas.
            mAmbientCacheCanvas.drawColor(Color.TRANSPARENT); // Clear it first.
            mActiveCacheCanvas.drawColor(Color.TRANSPARENT); // Clear it first.

            // Pre-cache our ambient canvas.
            mCacheExclusionPath.reset();
            mWatchFaceState.setAmbient(true);
            super.draw(mAmbientCacheCanvas);

            // Pre-cache our active canvas.
            mCacheExclusionPath.reset();
            mWatchFaceState.setAmbient(false);
            super.draw(mActiveCacheCanvas);

            // And back to how we were.
            mWatchFaceState.setAmbient(currentAmbient);
            mPreviousSerial = currentSerial;

            // Hardware power!
            if (Build.VERSION.SDK_INT >= 26) {
                mAmbientHardwareCacheBitmap =
                        mAmbientCacheBitmap.copy(Bitmap.Config.HARDWARE, false);
                mActiveHardwareCacheBitmap =
                        mActiveCacheBitmap.copy(Bitmap.Config.HARDWARE, false);
            }
        }

        // Then copy our cache to the results!
        mExclusionPath.set(mCacheExclusionPath);
        Bitmap mHardwareCacheBitmap = mWatchFaceState.isAmbient() ?
                mAmbientHardwareCacheBitmap : mActiveHardwareCacheBitmap;
        Bitmap mCacheBitmap = mWatchFaceState.isAmbient() ?
                mAmbientCacheBitmap : mActiveCacheBitmap;
        canvas.drawBitmap(mHardwareCacheBitmap != null ? mHardwareCacheBitmap : mCacheBitmap,
                0, 0, null);
    }
}
