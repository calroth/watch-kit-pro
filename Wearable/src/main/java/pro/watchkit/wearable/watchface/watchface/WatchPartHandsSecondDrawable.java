package pro.watchkit.wearable.watchface.watchface;

import android.graphics.Canvas;
import android.graphics.Path;

import androidx.annotation.NonNull;

import pro.watchkit.wearable.watchface.model.BytePackable;
import pro.watchkit.wearable.watchface.model.BytePackable.HandCutout;
import pro.watchkit.wearable.watchface.model.BytePackable.HandLength;
import pro.watchkit.wearable.watchface.model.BytePackable.HandShape;
import pro.watchkit.wearable.watchface.model.BytePackable.HandStalk;
import pro.watchkit.wearable.watchface.model.BytePackable.HandThickness;
import pro.watchkit.wearable.watchface.model.BytePackable.Style;

final class WatchPartHandsSecondDrawable extends WatchPartHandsDrawable {
    @Override
    String getStatsName() {
        return "Second";
    }

    @NonNull
    @Override
    HandShape getHandShape() {
        return mWatchFaceState.getSecondHandShape();
    }

    @NonNull
    @Override
    HandLength getHandLength() {
        return mWatchFaceState.getSecondHandLength();
    }

    @NonNull
    @Override
    HandThickness getHandThickness() {
        return mWatchFaceState.getSecondHandThickness();
    }

    @NonNull
    @Override
    HandStalk getHandStalk() {
        return BytePackable.HandStalk.NONE; // Don't have this for the seconds hand.
    }

    @NonNull
    @Override
    HandCutout getHandCutout() {
        return BytePackable.HandCutout.NONE; // Don't have this for the seconds hand either.
    }

    @NonNull
    @Override
    Style getStyle() {
        return mWatchFaceState.getSecondHandStyle();
    }

    @Override
    void punchHub(Path active, Path ambient) {
        // Punch the hub out of the seconds hand in ambient and active modes.
        ambient.op(getHub(), Path.Op.DIFFERENCE);
        active.op(getHub(), Path.Op.DIFFERENCE);
    }

    @Override
    float getDegreesRotation() {
        final float seconds = mWatchFaceState.getSecondsDecimal();
        final float secondsRotation = seconds * 6f;

//        final float minuteHandOffset = secondsRotation / 60f;
//        final float minutesRotation = mWatchFaceState.getMinutes() * 6f + minuteHandOffset;
//
//        final float hourHandOffset = minutesRotation / 12f;
//        final float hoursRotation = mWatchFaceState.getHours() * 30f + hourHandOffset;

        return secondsRotation;
    }

    @Override
    public void draw2(@NonNull Canvas canvas) {
        // Special case: Only draw seconds hand if not ambient.
        if (!mWatchFaceState.isAmbient()) {
            super.draw2(canvas);
        }
    }

    @Override
    boolean isSecondHand() {
        return true;
    }
}
