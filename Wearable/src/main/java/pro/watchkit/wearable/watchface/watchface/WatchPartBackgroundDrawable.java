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

import pro.watchkit.wearable.watchface.util.SharedPref;

final class WatchPartBackgroundDrawable extends WatchPartDrawable {
    @NonNull
    @Override
    String getStatsName() {
        return "Bg";
    }

    /**
     * Whether we draw our background to the entire canvas or just to an area within.
     */
    private final boolean mFullCanvas;

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
        } else if (mWatchFaceState.isTransparentBackground()) {
            canvas.drawColor(Color.TRANSPARENT); // Probably doesn't do anything?
        } else {
            final Paint p = mWatchFaceState.getPaintBox().getPaintFromPreset(
                    mWatchFaceState.getBackgroundStyle());
            final float r = Math.min(mCenterX, mCenterY);
            if (mFullCanvas) {
                canvas.drawPaint(p);
            } else if (SharedPref.isRoundScreen()) {
                // Draw a round background.
                canvas.drawCircle(mCenterX, mCenterY, r, p);
            } else {
                // Draw a square background.
                canvas.drawRect(mCenterX - r, mCenterY - r,
                        mCenterX + r, mCenterY + r, p);
            }
        }
    }
}
