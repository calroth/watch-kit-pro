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

import androidx.annotation.NonNull;

final class WatchPartBackgroundDrawable extends WatchPartDrawable {
    @NonNull
    @Override
    String getStatsName() {
        return "Bg";
    }

    @Override
    public void draw2(@NonNull Canvas canvas) {
        // As the bottom-most layer of the draw stack, reset our exclusion paths now.
        resetExclusionPath();

        if (mWatchFaceState.isAmbient()) {
            canvas.drawColor(Color.BLACK);
        } else if (mWatchFaceState.isTransparentBackground()) {
            canvas.drawColor(Color.TRANSPARENT); // Probably doesn't do anything?
        } else {
            canvas.drawPaint(mWatchFaceState.getPaintBox().getPaintFromPreset(
                    mWatchFaceState.getBackgroundMaterial()));
        }
    }
}
