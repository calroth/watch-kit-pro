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
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Xfermode;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.SystemClock;

import androidx.annotation.NonNull;
import pro.watchkit.wearable.watchface.model.PaintBox;
import pro.watchkit.wearable.watchface.model.WatchFaceState;

/**
 * A very basic Drawable that you feed a WatchFacePreset and a PaintBox and it
 * draws a watch face!
 */
public class WatchFaceGlobalDrawable extends LayerDrawable {
    private WatchFaceState mWatchFaceState;
    private Drawable[] mWatchPartDrawables;
    private Path mExclusionPath = new Path();

    private WatchFaceGlobalDrawable(@NonNull Drawable[] watchPartDrawables) {
        super(watchPartDrawables);
        mWatchPartDrawables = watchPartDrawables;

        // Stats start
        for (Drawable d : mWatchPartDrawables) {
            if (d instanceof WatchPartStatsDrawable) {
                ((WatchPartStatsDrawable) d).mWatchPartDrawables = watchPartDrawables;

                for (Drawable d2 : mWatchPartDrawables) {
                    if (d2 instanceof WatchFaceGlobalCacheDrawable) {
                        ((WatchPartStatsDrawable) d).mWatchPartDrawables2 =
                                ((WatchFaceGlobalCacheDrawable) d2).getWatchPartDrawables();
                    }
                }
            }
        }
        // Stats end
    }

    WatchFaceGlobalDrawable(
            @NonNull Context context, @NonNull Drawable[] watchPartDrawables) {
        this(watchPartDrawables);

        setWatchFaceState(context);
    }

    public WatchFaceGlobalDrawable(@NonNull Context context) {
        this(new WatchPartDrawable[]{
                new WatchPartBackgroundDrawable(),
                new WatchPartTicksFourDrawable(),
                new WatchPartTicksTwelveDrawable(),
                new WatchPartTicksSixtyDrawable(),
                new WatchPartHandsHourDrawable(),
                new WatchPartHandsMinuteDrawable(),
                new WatchPartHandsSecondDrawable()});

        setWatchFaceState(context);
    }

    private void setWatchFaceState(@NonNull Context context) {
        mWatchFaceState = new WatchFaceState(context);

        for (Drawable d : mWatchPartDrawables) {
            if (d instanceof WatchPartDrawable) {
                ((WatchPartDrawable) d).setWatchFaceState(mWatchFaceState, mExclusionPath);
            } else if (d instanceof WatchFaceGlobalCacheDrawable) {
                ((WatchFaceGlobalCacheDrawable) d).setWatchFaceState(mWatchFaceState, mExclusionPath);
            }
        }
    }

    @NonNull
    public WatchFaceState getWatchFaceState() {
        return mWatchFaceState;
    }

    private Paint mTint = new Paint();
    private Xfermode mTintXfermode;

    @Override
    public void draw(@NonNull Canvas canvas) {
        // Stats start
        long start = SystemClock.elapsedRealtimeNanos();
        // Stats end

        mExclusionPath.reset();

        // Set the current date and time.
        mWatchFaceState.setDefaultTimeZone();
        mWatchFaceState.setCurrentTimeToNow();

        // Reset the direction so we get consistency per draw (hopefully).
        WatchPartDrawable.resetDirection();

        super.draw(canvas);

        // If we're ambient and it's night...
        if (mWatchFaceState.isAmbient() &&
                mWatchFaceState.getLocationCalculator().getDuskDawnMultiplier() > 0d) {
            // Set a night vision tint!
            mTint.setColor(mWatchFaceState.getLocationCalculator().getDuskDawnColor(
                    PaintBox.AMBIENT_WHITE));
            if (mTintXfermode == null) {
                // Set the transfer mode on first use.
                mTintXfermode = new PorterDuffXfermode(PorterDuff.Mode.MULTIPLY);
                mTint.setXfermode(mTintXfermode);
            }

            canvas.drawPaint(mTint);
        }

        // Stats start
        WatchPartStatsDrawable.total = SystemClock.elapsedRealtimeNanos() - start;
        // Stats end
    }
}
