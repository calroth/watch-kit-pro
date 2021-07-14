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

import android.graphics.Paint;

import androidx.annotation.NonNull;

import pro.watchkit.wearable.watchface.model.BytePackable.Material;
import pro.watchkit.wearable.watchface.model.BytePackable.PipShape;
import pro.watchkit.wearable.watchface.model.BytePackable.PipSize;

final class WatchPartPipsMinuteDrawable extends WatchPartPipsDrawable {
    @NonNull
    @Override
    String getStatsName() {
        return "Minute";
    }

    @Override
    protected boolean isVisible(int pipIndex) {
        if (pipIndex % 15 == 0)
            return false;
        else if (pipIndex % 5 == 0)
            return false;
        else
            return mWatchFaceState.isMinutePipsVisible();
    }

    @Override
    protected float getMod() {
        return (float) Math.sqrt(0.5d);
    }

    @Override
    protected PipShape getPipShape() {
        return mWatchFaceState.getMinutePipShape();
    }

    @Override
    protected PipSize getPipSize() {
        return mWatchFaceState.getMinutePipSize();
    }

    @Override
    protected Material getPipStyle() {
        return mWatchFaceState.getMinutePipMaterial();
    }

    @Override
    protected Paint getAmbientPaint() {
        return mWatchFaceState.getPaintBox().getAmbientPaintFaded();
    }
}
