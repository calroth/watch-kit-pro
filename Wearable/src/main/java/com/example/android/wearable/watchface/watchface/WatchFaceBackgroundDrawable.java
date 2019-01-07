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
import android.graphics.Color;
import android.graphics.Paint;
import androidx.annotation.NonNull;

import com.example.android.wearable.watchface.model.Palette;

final class WatchFaceBackgroundDrawable extends WatchFaceDrawable {
    @Override
    public void draw(@NonNull Canvas canvas) {
        super.draw(canvas);

        if (mStateObject.ambient) {
            mPalette.getAmbientPaint().setColor(
                    mLocationCalculator.getDuskDawnColor(Palette.AMBIENT_WHITE));
        }

        if (mStateObject.ambient /*&& (mLowBitAmbient || mBurnInProtection)*/) {
            canvas.drawColor(Color.BLACK);
        } else {
            mPalette.getBaseAccentPaint().setStyle(Paint.Style.FILL);
            canvas.drawPaint(mPalette.getBackgroundPaint());
        }
    }
}
