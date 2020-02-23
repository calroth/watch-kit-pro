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

import android.graphics.Path;

import androidx.annotation.NonNull;

import pro.watchkit.wearable.watchface.model.BytePackable.HandCutoutShape;
import pro.watchkit.wearable.watchface.model.BytePackable.HandLength;
import pro.watchkit.wearable.watchface.model.BytePackable.HandShape;
import pro.watchkit.wearable.watchface.model.BytePackable.HandStalk;
import pro.watchkit.wearable.watchface.model.BytePackable.HandThickness;
import pro.watchkit.wearable.watchface.model.BytePackable.Material;

final class WatchPartHandsMinuteDrawable extends WatchPartHandsDrawable {
    @NonNull
    @Override
    String getStatsName() {
        return "Minute";
    }

    @Override
    HandShape getHandShape() {
        return mWatchFaceState.getMinuteHandShape();
    }

    @Override
    HandLength getHandLength() {
        return mWatchFaceState.getMinuteHandLength();
    }

    @Override
    HandThickness getHandThickness() {
        return mWatchFaceState.getMinuteHandThickness();
    }

    @Override
    HandStalk getHandStalk() {
        return mWatchFaceState.getMinuteHandStalk();
    }

    @NonNull
    @Override
    HandCutoutShape getHandCutout() {
        return mWatchFaceState.getMinuteHandCutoutShape();
    }

    @Override
    Material getHandMaterial() {
        return mWatchFaceState.getMinuteHandMaterial();
    }

    @NonNull
    @Override
    Material getHandCutoutMaterial() {
        return mWatchFaceState.getMinuteHandCutoutMaterialAsMaterial();
    }

    @Override
    void punchHub(@NonNull Path active, @NonNull Path ambient) {
        // Add the hub to the Minute hand in ambient and active modes.
        ambient.op(getHub(), Path.Op.UNION);
        active.op(getHub(), Path.Op.UNION);
    }

    @Override
    float getDegreesRotation() {
        final float seconds = mWatchFaceState.getSecondsDecimal();
        final float secondsRotation = seconds * 6f;

        final float minuteHandOffset = secondsRotation / 60f;
        final float minutesRotation = mWatchFaceState.getMinutes() * 6f + minuteHandOffset;
//
//        final float hourHandOffset = minutesRotation / 12f;
//        final float hoursRotation = mWatchFaceState.getHours() * 30f + hourHandOffset;

        return minutesRotation;
    }

    @Override
    boolean isMinuteHand() {
        return true;
    }
}
