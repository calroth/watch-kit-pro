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
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

import androidx.annotation.NonNull;

final class WatchPartBackgroundDrawable extends WatchPartDrawable {
    @Override
    String getStatsName() {
        return "Bg";
    }

    /**
     * Whether we draw our background to the entire canvas or just to an area within.
     */
    private final boolean mFullCanvas;
    @NonNull
    private final Path mShape = new Path();

    WatchPartBackgroundDrawable(boolean fullCanvas) {
        super();
        mFullCanvas = fullCanvas;
    }

    @Override
    public void draw2(@NonNull Canvas canvas) {
        // As the bottom-most layer of the draw stack, reset our exclusion paths now.
        resetExclusionPath();

        if (mWatchFaceState.isAmbient()) {
            canvas.drawColor(Color.BLACK);
        } else {
            Paint p = mWatchFaceState.getPaintBox().getPaintFromPreset(
                    mWatchFaceState.getBackgroundStyle());
            if (mFullCanvas) {
                canvas.drawPaint(p);
            } else {
                // Draw to a prescribed area.
                // TODO: set square or circle according to device
                // At the moment it's neither: it's a dodecagon...
                float[] c = new float[]{966f, -259f,
                        707f, -707f,
                        259f, -966f,
                        -259f, -966f,
                        -707f, -707f,
                        -966f, -259f,
                        -966f, 259f,
                        -707f, 707f,
                        -259f, 966f,
                        259f, 966f,
                        707f, 707f,
                        966f, 259f};
                float f = Math.min(mCenterX, mCenterY) / 966f;
                mShape.reset();
                mShape.moveTo(mCenterX + f * c[0], mCenterY + f * c[1]);
                mShape.lineTo(mCenterX + f * c[2], mCenterY + f * c[3]);
                mShape.lineTo(mCenterX + f * c[4], mCenterY + f * c[5]);
                mShape.lineTo(mCenterX + f * c[6], mCenterY + f * c[7]);
                mShape.lineTo(mCenterX + f * c[8], mCenterY + f * c[9]);
                mShape.lineTo(mCenterX + f * c[10], mCenterY + f * c[11]);
                mShape.lineTo(mCenterX + f * c[12], mCenterY + f * c[13]);
                mShape.lineTo(mCenterX + f * c[14], mCenterY + f * c[15]);
                mShape.lineTo(mCenterX + f * c[16], mCenterY + f * c[17]);
                mShape.lineTo(mCenterX + f * c[18], mCenterY + f * c[19]);
                mShape.lineTo(mCenterX + f * c[20], mCenterY + f * c[21]);
                mShape.lineTo(mCenterX + f * c[22], mCenterY + f * c[23]);
                canvas.drawPath(mShape, p);
            }
        }
    }
}
