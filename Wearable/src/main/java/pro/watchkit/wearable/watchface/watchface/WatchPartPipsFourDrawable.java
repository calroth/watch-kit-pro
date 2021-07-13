/*
 * Copyright (C) 2019-2021 Terence Tan
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

import androidx.annotation.NonNull;

import pro.watchkit.wearable.watchface.model.BytePackable.Material;
import pro.watchkit.wearable.watchface.model.BytePackable.PipShape;
import pro.watchkit.wearable.watchface.model.BytePackable.PipSize;

final class WatchPartPipsFourDrawable extends WatchPartPipsDrawable {
    @NonNull
    @Override
    String getStatsName() {
        return "Four";
    }

    @Override
    protected boolean isVisible(int pipIndex) {
        if (pipIndex % 15 == 0)
            return mWatchFaceState.isFourPipsVisible();
        else
            return false;
    }

    @Override
    protected float getMod() {
        return (float) Math.sqrt(2d);
    }

    @Override
    protected PipShape getPipShape() {
        return mWatchFaceState.getFourPipShape();
    }

    @Override
    protected PipSize getPipSize() {
        return mWatchFaceState.getFourPipSize();
    }

    @Override
    protected Material getPipStyle() {
        return mWatchFaceState.getFourPipMaterial();
    }
}
