package pro.watchkit.wearable.watchface.watchface;

import android.graphics.Path;

import androidx.annotation.NonNull;

import pro.watchkit.wearable.watchface.model.BytePackable.HandCutout;
import pro.watchkit.wearable.watchface.model.BytePackable.HandLength;
import pro.watchkit.wearable.watchface.model.BytePackable.HandShape;
import pro.watchkit.wearable.watchface.model.BytePackable.HandStalk;
import pro.watchkit.wearable.watchface.model.BytePackable.HandThickness;
import pro.watchkit.wearable.watchface.model.BytePackable.Style;

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

    @Override
    HandCutout getHandCutout() {
        return mWatchFaceState.getMinuteHandCutout();
    }

    @Override
    Style getHandStyle() {
        return mWatchFaceState.getMinuteHandStyle();
    }

    @Override
    Style getHandCutoutStyle() {
        return mWatchFaceState.getMinuteHandCutoutStyle();
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
