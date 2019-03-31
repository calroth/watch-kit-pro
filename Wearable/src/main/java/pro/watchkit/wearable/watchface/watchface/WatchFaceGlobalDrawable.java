/*
 * Copyright (C) 2019 Terence Tan
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
import android.graphics.Rect;
import android.graphics.drawable.LayerDrawable;

import java.util.Objects;

import androidx.annotation.NonNull;
import pro.watchkit.wearable.watchface.model.WatchFaceState;

/**
 * A very basic Drawable that you feed a WatchFacePreset and a PaintBox and it
 * draws a watch face!
 */
public class WatchFaceGlobalDrawable extends LayerDrawable {
    private WatchFaceState mWatchFaceState;
    private WatchPartDrawable[] mWatchPartDrawables;

    private WatchFaceGlobalDrawable(@NonNull WatchPartDrawable[] watchPartDrawables) {
        super(watchPartDrawables);
        mWatchPartDrawables = watchPartDrawables;

        // Stats start
        for (WatchPartDrawable d : mWatchPartDrawables) {
            if (d instanceof WatchPartStatsDrawable) {
                ((WatchPartStatsDrawable) d).mWatchPartDrawables = watchPartDrawables;
            }
        }
        // Stats end
    }

    WatchFaceGlobalDrawable(
            @NonNull Context context, @NonNull WatchPartDrawable[] watchPartDrawables) {
        this(watchPartDrawables);

        mWatchFaceState = new WatchFaceState(context);

        for (WatchPartDrawable d : mWatchPartDrawables) {
            d.setWatchFaceState(mWatchFaceState);
        }
    }

    public WatchFaceGlobalDrawable(@NonNull Context context) {
        this(new WatchPartDrawable[]{
                new WatchPartBackgroundDrawable(),
                new WatchPartTicksFourDrawable(),
                new WatchPartTicksTwelveDrawable(),
                new WatchPartTicksSixtyDrawable(),
                new WatchPartHandsDrawable()});

        mWatchFaceState = new WatchFaceState(context);

        for (WatchPartDrawable d : mWatchPartDrawables) {
            d.setWatchFaceState(mWatchFaceState);
        }
    }

    @NonNull
    public WatchFaceState getWatchFaceState() {
        return mWatchFaceState;
    }

    private int mPreviousSerial = -1;

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);

        if (bounds.width() == 0 || bounds.height() == 0) {
            return;
        }

        Bitmap mCacheBitmap = Bitmap.createBitmap(
                bounds.width(), bounds.height(), Bitmap.Config.ARGB_8888);
        Canvas mCacheCanvas = new Canvas(mCacheBitmap);

        for (WatchPartDrawable d : mWatchPartDrawables) {
            // Skip drawing into cache if our serial matches our previous serial (i.e. nothing
            // has changed). Don't skip if it's changed!
            d.setCacheCanvas(mCacheCanvas, mCacheBitmap);
            // While we're here, reset this.
            d.setCachePoint(false);
        }

        for (int i = 0; i < mWatchPartDrawables.length - 1; i++) {
            WatchPartDrawable d0 = mWatchPartDrawables[i];
            WatchPartDrawable d1 = mWatchPartDrawables[i + 1];
            if (d0.canBeCached() && !d1.canBeCached()) {
                // d0 is the last drawable that can be cached.
                // Set it as our cache point.
                d0.setCachePoint(true);
                break;
            }
        }
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        // Set the current date and time.
        mWatchFaceState.setDefaultTimeZone();
        mWatchFaceState.setCurrentTimeToNow();

        // Reset the direction so we get consistency per draw (hopefully).
        WatchPartDrawable.resetDirection();

        // Invalidate our bits as required.
        // Invalidate if complications, unread notifications or total notifications have changed.
        // Or the entire preset. Or if we've flipped between active and ambient.
        // Or anything else of interest in the WatchFaceState.
        int currentSerial = Objects.hashCode(mWatchFaceState);
        for (WatchPartDrawable d : mWatchPartDrawables) {
            // Skip drawing into cache if our serial matches our previous serial (i.e. nothing
            // has changed). Don't skip if it's changed!
            d.skipDrawingIntoCache(mPreviousSerial == currentSerial);
        }
        mPreviousSerial = currentSerial;

        super.draw(canvas);
    }
}