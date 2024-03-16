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
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Xfermode;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.SystemClock;

import androidx.annotation.NonNull;

import pro.watchkit.wearable.watchface.model.WatchFaceStateBase;

/**
 * A very basic Drawable that you feed a WatchFacePreset and a PaintBox and it
 * draws a watch face!
 */
public abstract class WatchFaceGlobalDrawable extends LayerDrawable {
    WatchFaceStateBase mWatchFaceState;
    @NonNull
    final Drawable[] mWatchPartDrawables;
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
    static final int PART_PIPS_BACKGROUND = 128;
    static final int PART_DIGITS = 256;
    static final int PART_PIPS_SIXTY = 512;
    static final int PART_PIPS_TWELVE = 1024;
    static final int PART_PIPS_FOUR = 2048;
    public static final int PART_PIPS = PART_PIPS_BACKGROUND | PART_PIPS_SIXTY |
            PART_PIPS_TWELVE | PART_PIPS_FOUR | PART_DIGITS;
    static final int PART_HANDS_HOUR = 4096;
    static final int PART_HANDS_MINUTE = 8192;
    static final int PART_HANDS_SECOND = 16384;
    public static final int PART_HANDS = PART_HANDS_HOUR | PART_HANDS_MINUTE | PART_HANDS_SECOND;
    public static final int PART_SWATCH = 32768;

    WatchFaceGlobalDrawable(@NonNull Context context, @NonNull Drawable[] watchPartDrawables) {
        this(watchPartDrawables);

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

    @NonNull
    private final Paint mTint = new Paint();

    @NonNull
    abstract WatchFaceStateBase createNewWatchFaceState(@NonNull Context context);

    private void setWatchFaceState(@NonNull Context context) {
        mWatchFaceState = createNewWatchFaceState(context);

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
    public WatchFaceStateBase getWatchFaceState() {
        return mWatchFaceState;
    }

    @NonNull
    private final Xfermode mTintXfermode = new PorterDuffXfermode(PorterDuff.Mode.MULTIPLY);

    static Drawable createDrawable(int part) {
        return switch (part) {
            case PART_CLIP -> new WatchPartClipDrawable();
            case PART_BACKGROUND -> new WatchPartBackgroundDrawable();
            case PART_NOTIFICATIONS -> new WatchPartNotificationsDrawable();
            case PART_PIPS_BACKGROUND -> new WatchPartPipBackgroundDrawable();
            case PART_DIGITS -> new WatchPartDigitsDrawable();
            case PART_PIPS_SIXTY -> new WatchPartPipsMinuteDrawable();
            case PART_PIPS_TWELVE -> new WatchPartPipsHourDrawable();
            case PART_PIPS_FOUR -> new WatchPartPipsQuarterDrawable();
            case PART_HANDS_HOUR -> new WatchPartHandsDrawable(WatchPartHandsDrawable.HandType.HOUR);
            case PART_HANDS_MINUTE -> new WatchPartHandsDrawable(WatchPartHandsDrawable.HandType.MINUTE);
            case PART_HANDS_SECOND -> new WatchPartHandsDrawable(WatchPartHandsDrawable.HandType.SECOND);
            case PART_STATS -> new WatchPartStatsDrawable();
            case PART_SWATCH -> new WatchPartSwatchDrawable();
            default -> null;
        };
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
}
