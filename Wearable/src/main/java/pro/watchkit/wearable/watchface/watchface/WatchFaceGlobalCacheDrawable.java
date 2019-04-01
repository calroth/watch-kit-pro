package pro.watchkit.wearable.watchface.watchface;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;

import java.util.Objects;

import androidx.annotation.NonNull;
import pro.watchkit.wearable.watchface.model.WatchFaceState;

class WatchFaceGlobalCacheDrawable extends LayerDrawable {
    private WatchFaceState mWatchFaceState;
    private Drawable[] mWatchPartDrawables;
    private int mPreviousSerial = -1;
    private Bitmap mCacheBitmap;
    private Canvas mCacheCanvas;

    WatchFaceGlobalCacheDrawable(@NonNull Drawable[] watchPartDrawables) {
        super(watchPartDrawables);

        mWatchPartDrawables = watchPartDrawables;
    }

    void setWatchFaceState(@NonNull WatchFaceState watchFaceState) {
        mWatchFaceState = watchFaceState;

        for (Drawable d : mWatchPartDrawables) {
            if (d instanceof WatchPartDrawable) {
                ((WatchPartDrawable) d).setWatchFaceState(mWatchFaceState);
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

        mCacheBitmap = Bitmap.createBitmap(
                bounds.width(), bounds.height(), Bitmap.Config.ARGB_8888);
        mCacheCanvas = new Canvas(mCacheBitmap);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        // Invalidate our bits as required.
        // Invalidate if complications, unread notifications or total notifications have changed.
        // Or the entire preset. Or if we've flipped between active and ambient.
        // Or anything else of interest in the WatchFaceState.
        int currentSerial = Objects.hashCode(mWatchFaceState);
        if (mPreviousSerial != currentSerial) {
            // Cache invalid. Draw into our cache canvas.
            mCacheCanvas.drawColor(Color.TRANSPARENT); // Clear it first.
            super.draw(mCacheCanvas);
            mPreviousSerial = currentSerial;
        }

        // Then copy our cache to the results!
        canvas.drawBitmap(mCacheBitmap, 0, 0, null);
    }
}
