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

final class WatchPartHandsHourDrawable extends WatchPartHandsDrawable {
    @NonNull
    @Override
    String getStatsName() {
        return "Hour";
    }

    @Override
    HandShape getHandShape() {
        return mWatchFaceState.getHourHandShape();
    }

    @Override
    HandLength getHandLength() {
        return mWatchFaceState.getHourHandLength();
    }

    @Override
    HandThickness getHandThickness() {
        return mWatchFaceState.getHourHandThickness();
    }

    @Override
    HandStalk getHandStalk() {
        return mWatchFaceState.getHourHandStalk();
    }

    @NonNull
    @Override
    HandCutoutShape getHandCutout() {
        return mWatchFaceState.getHourHandCutoutShape();
    }

    @Override
    Material getHandMaterial() {
        return mWatchFaceState.getHourHandMaterial();
    }

    @NonNull
    @Override
    Material getHandCutoutMaterial() {
        return mWatchFaceState.getHourHandCutoutMaterialAsMaterial();
    }

    @Override
    void punchHub(@NonNull Path active, @NonNull Path ambient) {
        // Punch the hub out of the hour hand in ambient and active modes.
        ambient.op(getHub(), Path.Op.DIFFERENCE);
        active.op(getHub(), Path.Op.DIFFERENCE);
    }

    @Override
    float getDegreesRotation() {
        final float seconds = mWatchFaceState.getSecondsDecimal();
        final float secondsRotation = seconds * 6f;

        final float minuteHandOffset = secondsRotation / 60f;
        final float minutesRotation = mWatchFaceState.getMinutes() * 6f + minuteHandOffset;

        final float hourHandOffset = minutesRotation / 12f;
        final float hoursRotation = mWatchFaceState.getHours() * 30f + hourHandOffset;

        return hoursRotation;
    }
}
