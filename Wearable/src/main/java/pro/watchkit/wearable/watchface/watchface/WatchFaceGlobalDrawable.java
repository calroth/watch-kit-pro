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
import android.support.wearable.watchface.decomposition.WatchFaceDecomposition;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import pro.watchkit.wearable.watchface.model.WatchFaceState;

/**
 * A very basic Drawable that you feed a WatchFacePreset and a PaintBox and it
 * draws a watch face!
 */
public class WatchFaceGlobalDrawable extends LayerDrawable {
    private WatchFaceState mWatchFaceState;
    @NonNull
    private final Drawable[] mWatchPartDrawables;
    @NonNull
    private final Path mExclusionPath = new Path();
    @NonNull
    private final Path mInnerGlowPath = new Path();

    public static final int PART_CLIP = 1;
    public static final int PART_BACKGROUND = 2;
    static final int PART_NOTIFICATIONS = 4;
    public static final int PART_RINGS_ACTIVE = 8;
    public static final int PART_RINGS_ALL = 16;
    public static final int PART_COMPLICATIONS = 32;
    static final int PART_STATS = 64;
    private static final int PART_PIPS_BACKGROUND = 128;
    private static final int PART_DIGITS = 256;
    private static final int PART_PIPS_SIXTY = 512;
    private static final int PART_PIPS_TWELVE = 1024;
    private static final int PART_PIPS_FOUR = 2048;
    public static final int PART_PIPS = PART_PIPS_BACKGROUND | PART_PIPS_SIXTY |
            PART_PIPS_TWELVE | PART_PIPS_FOUR | PART_DIGITS;
    private static final int PART_HANDS_HOUR = 4096;
    private static final int PART_HANDS_MINUTE = 8192;
    private static final int PART_HANDS_SECOND = 16384;
    public static final int PART_HANDS = PART_HANDS_HOUR | PART_HANDS_MINUTE | PART_HANDS_SECOND;
    public static final int PART_SWATCH = 32768;

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

    @NonNull
    private final Paint mTint = new Paint();

    private void setWatchFaceState(@NonNull Context context) {
        mWatchFaceState = new WatchFaceState(context);

        for (Drawable d : mWatchPartDrawables) {
            if (d instanceof WatchPartDrawable) {
                ((WatchPartDrawable) d).setWatchFaceState(
                        mWatchFaceState, mExclusionPath, mInnerGlowPath);
            } else if (d instanceof WatchFaceGlobalCacheDrawable) {
                ((WatchFaceGlobalCacheDrawable) d).setWatchFaceState(
                        mWatchFaceState, mExclusionPath, mInnerGlowPath);
            }
        }
    }

    @NonNull
    public WatchFaceState getWatchFaceState() {
        return mWatchFaceState;
    }

    @NonNull
    private final Xfermode mTintXfermode = new PorterDuffXfermode(PorterDuff.Mode.MULTIPLY);

    @NonNull
    static Drawable[] buildDrawables(@Nullable Drawable cache, int flags) {
        List<Drawable> d = new ArrayList<>();

        if (cache != null) {
            d.add(cache);
        }

        if ((flags & PART_CLIP) > 0) {
            d.add(new WatchPartClipDrawable());
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
        if ((flags & PART_PIPS_BACKGROUND) > 0) {
            d.add(new WatchPartPipBackgroundDrawable());
        }
        if ((flags & PART_DIGITS) > 0) {
            d.add(new WatchPartDigitsDrawable());
        }
        if ((flags & PART_PIPS_SIXTY) > 0) {
            d.add(new WatchPartPipsMinuteDrawable());
        }
        if ((flags & PART_PIPS_TWELVE) > 0) {
            d.add(new WatchPartPipsHourDrawable());
        }
        if ((flags & PART_PIPS_FOUR) > 0) {
            d.add(new WatchPartPipsQuarterDrawable());
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
        if ((flags & PART_SWATCH) > 0) {
            d.add(new WatchPartSwatchDrawable());
        }

        return d.toArray(new Drawable[0]);
    }

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

        // If we're ambient
        if (mWatchFaceState.isAmbient()) {
            // By default we draw ambient in white, then tint it to the user's selected color.
            mTint.setColor(mWatchFaceState.getAmbientTint());
            mTint.setXfermode(mTintXfermode);
            // Draw the tint all over the canvas.
            canvas.drawPaint(mTint);
        }

        // Stats start
        WatchPartStatsDrawable.total = SystemClock.elapsedRealtimeNanos() - start;
        // Stats end
    }

    /**
     * Does this watch face have a decomposition update available? Has the decomposition
     * changed since last time? (We want to avoid having to send unnecessary updates to
     * the offload processor.)
     *
     * @return Whether the decomposition has changed since the last call
     */
    public boolean hasDecompositionUpdateAvailable() {
        long currentTimeMillis = mWatchFaceState.getTimeInMillis();
        // Call "hasUpdateAvailable" on each component.
        // If any are true, return true, else return false.
        return Arrays.stream(mWatchPartDrawables)
                .filter(d -> d instanceof WatchFaceDecompositionComponent)
                .map(d -> ((WatchFaceDecompositionComponent) d)
                        .hasDecompositionUpdateAvailable(currentTimeMillis))
                .reduce(false, (a, b) -> a || b);
    }

    /**
     * Build the watch face decomposition into "builder". This watch face decomposition is
     * composed of multiple components (hence it's called a "decomposition"); this method
     * will build them all in order. (In fact in the same order we draw with the regular
     * draw path: background first, then rings, pips, complications, hands; i.e. the
     * order defined in "buildDrawables".)
     *
     * @param builder WatchFaceDecomposition builder to build into.
     * @return The time at which this decomposition expires, at which point (or before),
     * call this again
     */
    public long buildDecomposition(WatchFaceDecomposition.Builder builder) {
        AtomicInteger idA = new AtomicInteger(0);
        // Call "buildWatchFaceDecompositionComponents" on each component.
        // Return the earliest time of all returned times, or Long.MAX_VALUE.
        return Arrays.stream(mWatchPartDrawables)
                .filter(d -> d instanceof WatchFaceDecompositionComponent)
                .mapToLong(d -> ((WatchFaceDecompositionComponent) d)
                        .buildWatchFaceDecompositionComponents(builder, idA))
                .min().orElse(Long.MAX_VALUE);
    }

    /**
     * This is a component in a watch face decomposition. Components include background graphics,
     * hands, complications etc. We have a list of components which we call in order (back to
     * front); for each component, we pass a WatchFaceDecomposition.Builder and each component
     * builds itself using that Builder. (Only those WatchPartDrawables which implement this
     * interface will be called as part of the decomposition building.)
     */
    interface WatchFaceDecompositionComponent {
        /**
         * Build this watch face decomposition component into "builder".
         *
         * @param builder WatchFaceDecomposition builder to build into.
         * @param idA     AtomicInteger for the component ID, which we will increment
         * @return The time at which this component expires, at which point (or before),
         * call this again
         */
        long buildWatchFaceDecompositionComponents(
                @NonNull WatchFaceDecomposition.Builder builder, @NonNull AtomicInteger idA);

        /**
         * Does this WatchFaceDecomposition component have an update available? We ask because if
         * there are no updates available, we want to avoid sending updates to the offload
         * processor.
         *
         * @param currentTimeMillis The time when we're asking if there's an update available
         * @return Whether the update is available?
         */
        boolean hasDecompositionUpdateAvailable(long currentTimeMillis);
    }
}
