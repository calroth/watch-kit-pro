/*
 * Copyright (C) 2021 Terence Tan
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
import android.graphics.Path;

import androidx.annotation.NonNull;

final class WatchPartClipDrawable extends WatchPartDrawable {
    @NonNull
    private final Path mScreenShapePath = new Path();

    @NonNull
    @Override
    String getStatsName() {
        return "Clp";
    }

    @Override
    public void draw2(@NonNull Canvas canvas) {
        // Set the screen shape path.
        mScreenShapePath.reset();
        final float r = Math.min(mCenterX, mCenterY);
        if (mWatchFaceState.isScreenRound()) {
            // Round screen, make it a circle.
            mScreenShapePath.addCircle(mCenterX, mCenterY, r, getDirection());
            // Deal with cutouts here?
        } else {
            // Square screen, make it a rectangle.
            mScreenShapePath.addRect(mCenterX - r, mCenterY - r,
                    mCenterX + r, mCenterY + r, getDirection());
            // Deal with rectangular screens here?
        }

        // Save the (non-existent) clip state of the canvas before we start clipping.
        canvas.save();
        // OK, set the canvas clip to the shape of the screen.
        canvas.clipPath(mScreenShapePath);
    }
}
