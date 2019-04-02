package pro.watchkit.wearable.watchface.watchface;

import android.graphics.Path;

import pro.watchkit.wearable.watchface.model.WatchFacePreset;
import pro.watchkit.wearable.watchface.model.WatchFacePreset.HandLength;
import pro.watchkit.wearable.watchface.model.WatchFacePreset.HandShape;
import pro.watchkit.wearable.watchface.model.WatchFacePreset.HandStalk;
import pro.watchkit.wearable.watchface.model.WatchFacePreset.HandThickness;

final class WatchPartHandsHourDrawable extends WatchPartHandsDrawable {
    @Override
    String getStatsName() {
        return "Hour";
    }

    @Override
    HandShape getHandShape() {
        return mWatchFaceState.getWatchFacePreset().getHourHandShape();
    }

    @Override
    HandLength getHandLength() {
        return mWatchFaceState.getWatchFacePreset().getHourHandLength();
    }

    @Override
    HandThickness getHandThickness() {
        return mWatchFaceState.getWatchFacePreset().getHourHandThickness();
    }

    @Override
    HandStalk getHandStalk() {
        return mWatchFaceState.getWatchFacePreset().getHourHandStalk();
    }

    @Override
    WatchFacePreset.Style getStyle() {
        return mWatchFaceState.getWatchFacePreset().getHourHandStyle();
    }

    @Override
    void punchHub() {
        // Punch the hub out of the hour hand in ambient mode.
        mHandAmbientPath.op(getHub(), Path.Op.DIFFERENCE);
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
