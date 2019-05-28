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

import java.util.ArrayList;
import java.util.List;

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

    public static final int PART_BACKGROUND = 1;
    static final int PART_NOTIFICATIONS = 2;
    static final int PART_RINGS_ACTIVE = 4;
    public static final int PART_RINGS_ALL = 8;
    public static final int PART_COMPLICATIONS = 16;
    static final int PART_STATS = 32;
    private static final int PART_TICKS_FOUR = 64;
    private static final int PART_TICKS_TWELVE = 128;
    private static final int PART_TICKS_SIXTY = 256;
    public static final int PART_TICKS = PART_TICKS_FOUR | PART_TICKS_TWELVE | PART_TICKS_SIXTY;
    private static final int PART_HANDS_HOUR = 1024;
    private static final int PART_HANDS_MINUTE = 2048;
    private static final int PART_HANDS_SECOND = 4096;
    public static final int PART_HANDS = PART_HANDS_HOUR | PART_HANDS_MINUTE | PART_HANDS_SECOND;

    public WatchFaceGlobalDrawable(@NonNull Context context, int flags) {
        this(buildDrawables(null, flags));

        setWatchFaceState(context);
    }

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

    WatchFaceGlobalDrawable(@NonNull Context context, WatchFaceGlobalCacheDrawable cache, int flags) {
        this(buildDrawables(cache, flags));

        setWatchFaceState(context);
    }

    static Drawable[] buildDrawables(Drawable cache, int flags) {
        List<Drawable> d = new ArrayList<>();

        if (cache != null) {
            d.add(cache);
        }

        if ((flags & PART_BACKGROUND) > 0) {
            d.add(new WatchPartBackgroundDrawable());
        }
        if ((flags & PART_NOTIFICATIONS) > 0) {
            d.add(new WatchPartNotificationsDrawable());
        }
        if ((flags & PART_RINGS_ACTIVE) > 0) {
            d.add(new WatchPartRingsDrawable());
        } else if ((flags & PART_RINGS_ALL) > 0) {
            d.add(new WatchPartRingsDrawable(true));
        }
        if ((flags & PART_TICKS_FOUR) > 0) {
            d.add(new WatchPartTicksFourDrawable());
        }
        if ((flags & PART_TICKS_TWELVE) > 0) {
            d.add(new WatchPartTicksTwelveDrawable());
        }
        if ((flags & PART_TICKS_SIXTY) > 0) {
            d.add(new WatchPartTicksSixtyDrawable());
        }
        if ((flags & PART_COMPLICATIONS) > 0) {
            d.add(new WatchPartComplicationsDrawable());
        }
        if ((flags & PART_HANDS_HOUR) > 0) {
            d.add(new WatchPartHandsHourDrawable());
        }
        if ((flags & PART_HANDS_MINUTE) > 0) {
            d.add(new WatchPartHandsMinuteDrawable());
        }
        if ((flags & PART_HANDS_SECOND) > 0) {
            d.add(new WatchPartHandsSecondDrawable());
        }
        if ((flags & PART_STATS) > 0) {
            d.add(new WatchPartStatsDrawable());
        }

        return d.toArray(new Drawable[0]);
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
