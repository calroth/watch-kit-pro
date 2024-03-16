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

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.wearable.watchface.decomposition.WatchFaceDecomposition;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import pro.watchkit.wearable.watchface.model.WatchFaceState;

public class LegacyWatchFaceGlobalDrawable extends WatchFaceGlobalDrawable {

    public LegacyWatchFaceGlobalDrawable(@NonNull Context context, int flags) {
        this(context, null, flags);
    }

    LegacyWatchFaceGlobalDrawable(@NonNull Context context, WatchFaceGlobalCacheDrawable cache, int flags) {
        super(context, buildDrawables(cache, flags));
    }

    @NonNull
    @Override
    WatchFaceState createNewWatchFaceState(@NonNull Context context) {
        return new WatchFaceState(context);
    }

    @NonNull
    @Override
    public WatchFaceState getWatchFaceState() {
        return (WatchFaceState)mWatchFaceState;
    }

    static Drawable createDrawable(int part) {
        return switch (part) {
            case PART_RINGS_ACTIVE -> new WatchPartRingsDrawable();
            case PART_RINGS_ALL -> new WatchPartRingsDrawable(true);
            case PART_COMPLICATIONS -> new WatchPartComplicationsDrawable();
            case PART_HANDS_HOUR -> new LegacyWatchPartHandsDrawable(WatchPartHandsDrawable.HandType.HOUR);
            case PART_HANDS_MINUTE -> new LegacyWatchPartHandsDrawable(WatchPartHandsDrawable.HandType.MINUTE);
            case PART_HANDS_SECOND -> new LegacyWatchPartHandsDrawable(WatchPartHandsDrawable.HandType.SECOND);
            default -> WatchFaceGlobalDrawable.createDrawable(part);
        };
    }

    @NonNull
    static Drawable[] buildDrawables(@Nullable Drawable cache, int flags) {
        List<Drawable> d = new ArrayList<>();

        if (cache != null) {
            d.add(cache);
        }

        if ((flags & PART_CLIP) > 0) {
            d.add(createDrawable(PART_CLIP));
        }
        if ((flags & PART_BACKGROUND) > 0) {
            d.add(createDrawable(PART_BACKGROUND));
        }
        if ((flags & PART_NOTIFICATIONS) > 0) {
            d.add(createDrawable(PART_NOTIFICATIONS));
        }
        if ((flags & PART_RINGS_ACTIVE) > 0) {
            d.add(createDrawable(PART_RINGS_ACTIVE));
        } else if ((flags & PART_RINGS_ALL) > 0) {
            d.add(createDrawable(PART_RINGS_ALL));
        }
        if ((flags & PART_PIPS_BACKGROUND) > 0) {
            d.add(createDrawable(PART_PIPS_BACKGROUND));
        }
        if ((flags & PART_DIGITS) > 0) {
            d.add(createDrawable(PART_DIGITS));
        }
        if ((flags & PART_PIPS_SIXTY) > 0) {
            d.add(createDrawable(PART_PIPS_SIXTY));
        }
        if ((flags & PART_PIPS_TWELVE) > 0) {
            d.add(createDrawable(PART_PIPS_TWELVE));
        }
        if ((flags & PART_PIPS_FOUR) > 0) {
            d.add(createDrawable(PART_PIPS_FOUR));
        }
        if ((flags & PART_COMPLICATIONS) > 0) {
            d.add(createDrawable(PART_COMPLICATIONS));
        }
        if ((flags & PART_HANDS_HOUR) > 0) {
            d.add(createDrawable(PART_HANDS_HOUR));
        }
        if ((flags & PART_HANDS_MINUTE) > 0) {
            d.add(createDrawable(PART_HANDS_MINUTE));
        }
        if ((flags & PART_HANDS_SECOND) > 0) {
            d.add(createDrawable(PART_HANDS_SECOND));
        }
        if ((flags & PART_STATS) > 0) {
            d.add(createDrawable(PART_STATS));
        }
        if ((flags & PART_SWATCH) > 0) {
            d.add(createDrawable(PART_SWATCH));
        }

        return d.stream().filter(Objects::nonNull).toArray(Drawable[]::new);
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
