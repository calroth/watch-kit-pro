/*
 * Copyright (C) 2019-2020 Terence Tan
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

import pro.watchkit.wearable.watchface.model.BytePackable.Style;
import pro.watchkit.wearable.watchface.model.BytePackable.TickShape;
import pro.watchkit.wearable.watchface.model.BytePackable.TickSize;

final class WatchPartTicksFourDrawable extends WatchPartTicksDrawable {
    @NonNull
    @Override
    String getStatsName() {
        return "Four";
    }

    @Override
    protected boolean isVisible(int tickIndex) {
        if (tickIndex % 15 == 0)
            return mWatchFaceState.isFourTicksVisible();
        else
            return false;
    }

    @Override
    protected float getMod() {
        return (float) Math.sqrt(2d);
    }

    @Override
    protected TickShape getTickShape() {
        return mWatchFaceState.getFourTickShape();
    }

    @Override
    protected TickSize getTickSize() {
        return mWatchFaceState.getFourTickSize();
    }

    @Override
    protected Style getTickStyle() {
        return mWatchFaceState.getFourTickStyle();
    }
}
