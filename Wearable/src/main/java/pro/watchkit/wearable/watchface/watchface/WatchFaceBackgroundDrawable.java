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

import androidx.annotation.NonNull;
import pro.watchkit.wearable.watchface.model.PaintBox;

final class WatchFaceBackgroundDrawable extends WatchFaceDrawable {
    @Override
    public void draw(@NonNull Canvas canvas) {
        super.draw(canvas);

        if (mStateObject.ambient) {
            mStateObject.paintBox.getAmbientPaint().setColor(
                    mLocationCalculator.getDuskDawnColor(PaintBox.AMBIENT_WHITE));
        }

        if (mStateObject.ambient /*&& (mLowBitAmbient || mBurnInProtection)*/) {
            canvas.drawColor(Color.BLACK);
        } else {
            Paint p = mStateObject.paintBox.getPaintFromPreset(mStateObject.preset.getBackgroundStyle());
            canvas.drawPaint(p);
//            mPaintBox.getBaseAccentPaint().setStyle(Paint.Style.FILL);
//            canvas.drawPaint(mPaintBox.getBackgroundPaint());
        }
    }
}
