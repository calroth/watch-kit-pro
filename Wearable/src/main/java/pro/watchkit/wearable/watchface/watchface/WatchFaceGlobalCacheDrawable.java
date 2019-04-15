package pro.watchkit.wearable.watchface.watchface;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;

import java.util.Objects;

import androidx.annotation.NonNull;
import pro.watchkit.wearable.watchface.model.WatchFaceState;

class WatchFaceGlobalCacheDrawable extends LayerDrawable {
    private WatchFaceState mWatchFaceState;
    private Drawable[] mWatchPartDrawables;
    private int mActivePreviousSerial = -1;
    private Bitmap mActiveCacheBitmap;
    private Bitmap mActiveHardwareCacheBitmap;
    private Canvas mActiveCacheCanvas;
    private int mAmbientPreviousSerial = -1;
    private Bitmap mAmbientCacheBitmap;
    private Bitmap mAmbientHardwareCacheBitmap;
    private Canvas mAmbientCacheCanvas;
    private Path mExclusionPath;
    private Path mCacheExclusionPath = new Path();

    WatchFaceGlobalCacheDrawable(@NonNull Drawable[] watchPartDrawables) {
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
    protected void onBoundsChange(Rect bounds) {
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

        mActivePreviousSerial = -1;
        mAmbientPreviousSerial = -1;
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        Canvas mCacheCanvas = mWatchFaceState.isAmbient() ?
                mAmbientCacheCanvas : mActiveCacheCanvas;
        Bitmap mCacheBitmap = mWatchFaceState.isAmbient() ?
                mAmbientCacheBitmap : mActiveCacheBitmap;
        int previousSerial = mWatchFaceState.isAmbient() ?
                mAmbientPreviousSerial : mActivePreviousSerial;
        // Invalidate our bits as required.
        // Invalidate if complications, unread notifications or total notifications have changed.
        // Or the entire preset. Or if we've flipped between active and ambient.
        // Or anything else of interest in the WatchFaceState.
        int currentSerial = Objects.hash(mWatchFaceState);
        if (previousSerial != currentSerial) {
            // Cache invalid. Draw into our cache canvas.
            mCacheCanvas.drawColor(Color.TRANSPARENT); // Clear it first.
            mCacheExclusionPath.reset();
            super.draw(mCacheCanvas);
            if (mWatchFaceState.isAmbient()) {
                mAmbientPreviousSerial = currentSerial;
            } else {
                mActivePreviousSerial = currentSerial;
            }
            // Hardware power!
            if (Build.VERSION.SDK_INT >= 26) {
                if (mWatchFaceState.isAmbient()) {
                    mAmbientHardwareCacheBitmap = mCacheBitmap.copy(Bitmap.Config.HARDWARE, false);
                } else {
                    mActiveHardwareCacheBitmap = mCacheBitmap.copy(Bitmap.Config.HARDWARE, false);
                }
            }
        }

        // Then copy our cache to the results!
        mExclusionPath.set(mCacheExclusionPath);
        Bitmap mHardwareCacheBitmap = mWatchFaceState.isAmbient() ?
                mAmbientHardwareCacheBitmap : mActiveHardwareCacheBitmap;
        canvas.drawBitmap(mHardwareCacheBitmap != null ? mHardwareCacheBitmap : mCacheBitmap, 0, 0, null);
    }
}
