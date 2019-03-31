/*
 * Copyright (C) 2018-2019 Terence Tan
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

import android.graphics.Canvas;
import android.graphics.Paint;

import java.util.Objects;

import androidx.annotation.NonNull;

@Deprecated
final class WatchPartStatsDrawable extends WatchPartDrawable {
    static final String INVALID_COMPLICATION = "Complication";
    static final String INVALID_TIME_TICK = "Time Tick";
    static final String INVALID_TIMER_HANDLER = "Timer Handler";
    static final String INVALID_TIMEZONE = "Time Zone Change";
    static final String INVALID_LOCATION = "Location Change";
    static final String INVALID_AMBIENT = "Ambient Change";
    static final String INVALID_INTERRUPTION = "Interruption Filter";
    static final String INVALID_SURFACE = "Surface Change";
    static final String INVALID_NOTIFICATION = "Notification Change";
    static final String INVALID_WTF = "WTF?";
    @Deprecated
    static int invalid = 0;
    @Deprecated
    WatchPartDrawable[] mWatchPartDrawables;
    static String mInvalidTrigger = "";

    @Override
    String getStatsName() {
        return "Stats";
    }

    @Override
    public void draw2(@NonNull Canvas canvas) {
        Paint textPaint = mWatchFaceState.isAmbient()
                ? mWatchFaceState.getPaintBox().getAmbientPaint() : mWatchFaceState.getPaintBox().getFillHighlightPaint();

        float x = 12f * pc;
        float y = 45f * pc;

        canvas.drawText(mInvalidTrigger, x, y, textPaint);
        y += 3f * pc;

        for (WatchPartDrawable d : mWatchPartDrawables) {
            String extra = "";
            if (d.canBeCached()) {
                extra += "(c) ";
            }
            if (d.mSkipDrawingIntoCache) {
                extra += "(s) ";
            }
            if (d.mIsCachePoint) {
                extra += "(p) ";
            }
            extra = extra.trim();
            canvas.drawText(String.format("%s %s: %.2f", d.getStatsName(), extra,
                    (double) (d.mLastStatsTime) / 1000000d), x, y, textPaint);
            y += 3f * pc;
        }

        canvas.drawText(invalid
                + String.format(" Alt: %.2f° / ", mWatchFaceState.getLocationCalculator().getSunAltitude())
                + Objects.hashCode(mWatchFaceState)
                //+ String.format("%.2f", (double) (now[0] + now[1] + now[2] + now[3] + now[4]) / 1000000d)
                + (canvas.isHardwareAccelerated() ? " (hw)" : " (sw)"), x, y, textPaint);
    }
}