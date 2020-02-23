/*
 * Copyright (C) 2019 Terence Tan
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
 * This file incorporates work covered by the following copyright and
 * permission notice:
 *
 *     Copyright (C) 2017 The Android Open Source Project
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */

package pro.watchkit.wearable.watchface.watchface;

import android.graphics.Canvas;
import android.graphics.Path;

import androidx.annotation.NonNull;

import pro.watchkit.wearable.watchface.model.BytePackable.Material;

class WatchPartTickBackgroundDrawable extends WatchPartDrawable {
    @NonNull
    private final Path p = new Path();
    @NonNull
    private final Path p2 = new Path();

    @NonNull
    @Override
    String getStatsName() {
        return "Tbg";
    }

    @Override
    public void draw2(@NonNull Canvas canvas) {
        if (mWatchFaceState.isDeveloperMode() && mWatchFaceState.isHideTicks()) {
            // If we set developer mode "hide ticks", then just return!
            return;
        }

        // Draw the tick background ring if we asked for one!
        if (mWatchFaceState.getTickBackgroundMaterial() == Material.BASE_ACCENT) {
            // The overall watch background is Style.BASE_ACCENT.
            // If the tick ring is too, skip it. Otherwise draw something!
            return;
        }

        // Oh yes. Only draw in active mode -- don't need to do this in ambient.
        if (mWatchFaceState.isAmbient()) {
            return;
        }

        p.reset();
        p2.reset();

        float mCenter = Math.min(mCenterX, mCenterY);

        // Draw a big rectangle that's larger than bounds.
        p.addRect(-mCenterX, -mCenterY, mCenterX * 4f, mCenterY * 4f, getDirection());

        // Get the inner circle. Inset it 1f percent for a bit of padding.
        p2.addCircle(mCenterX, mCenterY,
                mCenter - (mWatchFaceState.getTickBandStart(pc) +
                        mWatchFaceState.getTickBandHeight(pc) + 1f) * pc, getDirection());

        // Punch the circle from the big rectangle and draw it.
        p.op(p2, Path.Op.DIFFERENCE);
        drawPath(canvas, p, mWatchFaceState.getPaintBox().getPaintFromPreset(
                mWatchFaceState.getTickBackgroundMaterial()));
    }
}
