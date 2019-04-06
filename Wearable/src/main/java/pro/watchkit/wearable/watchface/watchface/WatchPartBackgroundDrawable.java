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
import pro.watchkit.wearable.watchface.model.PaintBox;

final class WatchPartBackgroundDrawable extends WatchPartDrawable {
    @Override
    String getStatsName() {
        return "Bg";
    }

    @Override
    public void draw2(@NonNull Canvas canvas) {
        if (mWatchFaceState.isAmbient()) {
            mWatchFaceState.getPaintBox().getAmbientPaint().setColor(
                    mWatchFaceState.getLocationCalculator().getDuskDawnColor(PaintBox.AMBIENT_WHITE));

            // Test: if ambient, draw our ambient burn-in exclusion rings
            // We can't draw here because Wear OS shifts our watchface +/- 6px in each direction
            // and it gets cut off, so just don't try drawing there.

            Path ambientExclusionPath = new Path();
            final int exclusion = 6;

            Path p5 = new Path();
            p5.addCircle(mCenterX + exclusion, mCenterY + exclusion, mCenterX, getDirection());
            Path p6 = new Path();
            p6.addCircle(mCenterX + exclusion, mCenterY - exclusion, mCenterX, getDirection());
            Path p7 = new Path();
            p7.addCircle(mCenterX - exclusion, mCenterY + exclusion, mCenterX, getDirection());
            Path p8 = new Path();
            p8.addCircle(mCenterX - exclusion, mCenterY - exclusion, mCenterX, getDirection());

            p5.op(p6, Path.Op.INTERSECT);
            p5.op(p7, Path.Op.INTERSECT);
            p5.op(p8, Path.Op.INTERSECT);

            ambientExclusionPath.addPath(p5);
            addExclusionPath(ambientExclusionPath, Path.Op.UNION);

            canvas.drawColor(Color.BLACK);
        } else {
            Path activeExclusionPath = new Path();
            activeExclusionPath.addCircle(mCenterX, mCenterY, mCenterX, getDirection());
            addExclusionPath(activeExclusionPath, Path.Op.UNION);

            Paint p = mWatchFaceState.getPaintBox().getPaintFromPreset(
                    mWatchFaceState.getWatchFacePreset().getBackgroundStyle());
            canvas.drawPaint(p);
        }
    }
}
