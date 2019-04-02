package pro.watchkit.wearable.watchface.watchface;

import android.graphics.Path;

import pro.watchkit.wearable.watchface.model.WatchFacePreset.HandLength;
import pro.watchkit.wearable.watchface.model.WatchFacePreset.HandShape;
import pro.watchkit.wearable.watchface.model.WatchFacePreset.HandStalk;
import pro.watchkit.wearable.watchface.model.WatchFacePreset.HandThickness;
import pro.watchkit.wearable.watchface.model.WatchFacePreset.Style;

final class WatchPartHandsMinuteDrawable extends WatchPartHandsDrawable {
    @Override
    String getStatsName() {
        return "Minute";
    }

    @Override
    HandShape getHandShape() {
        return mWatchFaceState.getWatchFacePreset().getMinuteHandShape();
    }

    @Override
    HandLength getHandLength() {
        return mWatchFaceState.getWatchFacePreset().getMinuteHandLength();
    }

    @Override
    HandThickness getHandThickness() {
        return mWatchFaceState.getWatchFacePreset().getMinuteHandThickness();
    }

    @Override
    HandStalk getHandStalk() {
        return mWatchFaceState.getWatchFacePreset().getMinuteHandStalk();
    }

    @Override
    Style getStyle() {
        return mWatchFaceState.getWatchFacePreset().getMinuteHandStyle();
    }

    @Override
    void punchHub() {
        // Add the hub to the Minute hand in ambient mode.
        mHandAmbientPath.op(getHub(), Path.Op.UNION);
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
