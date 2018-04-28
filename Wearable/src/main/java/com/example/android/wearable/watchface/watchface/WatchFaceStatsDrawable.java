/*
 * Copyright (C) 2018 Terence Tan
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

package com.example.android.wearable.watchface.watchface;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.SystemClock;
import android.support.annotation.NonNull;

@Deprecated
final class WatchFaceStatsDrawable extends WatchFaceDrawable {
    @Deprecated
    int invalid = 0;
    long now[] = new long[5];

    @Override
    public void draw(@NonNull Canvas canvas) {
        super.draw(canvas);

        Paint textPaint = mStateObject.ambient
                ? mPalette.getAmbientPaint() : mPalette.getFillHighlightPaint();
        String pos = invalid
                + String.format(" Alt: %.2f° / ", mLocationCalculator.getSunAltitude());

        canvas.drawText(pos
                        + String.format("%.2f", (double) (now[0] + now[1] + now[2] + now[3] + now[4]) / 1000000d)
                        + (canvas.isHardwareAccelerated() ? " (hw)" : " (sw)"),
                12f * pc, 55f * pc, textPaint);
        canvas.drawText(String.format("%.2f / %.2f / %.2f / %.2f / %.2f",
                (double) (now[0]) / 1000000d,
                (double) (now[1]) / 1000000d,
                (double) (now[2]) / 1000000d,
                (double) (now[3]) / 1000000d,
                (double) (now[4]) / 1000000d), 12f * pc, 45f * pc, textPaint);
    }
}
