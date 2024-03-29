/*
 * Copyright (C) 2018-2022 Terence Tan
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
import android.graphics.Path;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;

import java.util.Formatter;

import pro.watchkit.wearable.watchface.model.BytePackable.Material;

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
    static final String INVALID_ALARM = "Alarm";
    static final String INVALID_WTF = "WTF?";
    static long total;
    static int invalid = 0;
    Drawable[] mWatchPartDrawables, mWatchPartDrawables2;
    @NonNull
    static String mInvalidTrigger = "";
    @NonNull
    private final StringBuffer mStringBuilder = new StringBuffer();
    @NonNull
    private final Formatter mFormatter = new Formatter(mStringBuilder);
    private Path mTextPath;

    @NonNull
    @Override
    String getStatsName() {
        return "Stats";
    }

    @Override
    public void draw2(@NonNull Canvas canvas) {
        // Only draw stats in developer mode if selected. If not, return early.
        if (!mWatchFaceState.isDeveloperMode() || !mWatchFaceState.isStats()) return;

        drawInnerGlowPath(canvas, mWatchFaceState.getPaintBox().getShadowPaint());

        Paint textPaint = mWatchFaceState.isAmbient() ?
                mWatchFaceState.getPaintBox().getAmbientPaint() :
                mWatchFaceState.getPaintBox().getPaintFromPreset(Material.FILL_HIGHLIGHT);

        float originalTextSize = textPaint.getTextSize();
        textPaint.setTextSize(3f * pc);

        float x = 12f * pc;
        float y = 35f * pc;
        mStringBuilder.setLength(0);

        // Show detailed stats if selected. But not in ambient mode.
        if (!mWatchFaceState.isAmbient() && mWatchFaceState.isStatsDetail()) {
//            canvas.drawText(mInvalidTrigger, x, y, textPaint);
            y += 3f * pc;

            if (mWatchPartDrawables2 != null) {
                for (Drawable d : mWatchPartDrawables2) {
                    if (d instanceof WatchPartDrawable) {
                        y = drawStats(((WatchPartDrawable) d), canvas, textPaint, x, y);
                    }
                }
            }

            if (mWatchPartDrawables != null) {
                for (Drawable d : mWatchPartDrawables) {
                    if (d instanceof WatchPartDrawable) {
                        y = drawStats(((WatchPartDrawable) d), canvas, textPaint, x, y);
                    }
                }
            }
        }

//        mStringBuilder.setLength(0);
        mStringBuilder.append(invalid).append(" Alt: ");
        mFormatter.format("%.2f", mWatchFaceState.getLocationCalculator().getSunAltitude());
        mStringBuilder.append("° / ");
        mFormatter.format("%.2f", (double) (total) / 1000000d);
        mStringBuilder.append(canvas.isHardwareAccelerated() ? " (hw)" : " (sw)");
//        canvas.drawText(mStringBuilder.toString(), x, y, textPaint);

//        canvas.drawText(invalid
//                + String.format(Locale.getDefault(),
//                " Alt: %.2f° / ", mWatchFaceState.getLocationCalculator().getSunAltitude())
////                + Objects.hashCode(mWatchFaceState)
//                + String.format(Locale.getDefault(),
//                "%.2f", (double) (total) / 1000000d)
//                + (canvas.isHardwareAccelerated() ? " (hw)" : " (sw)"), x, y, textPaint);

        if (mTextPath == null) {
            mTextPath = new Path();
            mTextPath.addArc(4f * pc, 4f * pc, 96f * pc, 96f * pc, 5f + 45f, 350f);
        }

        canvas.drawTextOnPath(mStringBuilder.toString(), mTextPath, 0, 0, textPaint);

        textPaint.setTextSize(originalTextSize);
    }

    private float drawStats(
            @NonNull WatchPartDrawable d, @NonNull Canvas canvas, Paint textPaint, float x, float y) {

//        mStringBuilder.setLength(0);
        mStringBuilder.append(d.getStatsName()).append(": ");
        mFormatter.format("%.2f", (double) (d.mLastStatsTime) / 1000000d);
        mStringBuilder.append(" - ");
//        canvas.drawText(mStringBuilder.toString(), x, y, textPaint);

//        canvas.drawText(String.format(Locale.getDefault(),
//                "%s: %.2f", d.getStatsName(),
//                    (double) (d.mLastStatsTime) / 1000000d), x, y, textPaint);
        y += 3f * pc;
        return y;
    }
}
