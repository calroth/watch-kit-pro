package pro.watchkit.wearable.watchface.watchface;

import android.graphics.Canvas;
import android.graphics.Path;

import androidx.annotation.NonNull;
import pro.watchkit.wearable.watchface.model.WatchFacePreset.HandLength;
import pro.watchkit.wearable.watchface.model.WatchFacePreset.HandShape;
import pro.watchkit.wearable.watchface.model.WatchFacePreset.HandStalk;
import pro.watchkit.wearable.watchface.model.WatchFacePreset.HandThickness;
import pro.watchkit.wearable.watchface.model.WatchFacePreset.Style;

final class WatchPartHandsSecondDrawable extends WatchPartHandsDrawable {
    @Override
    String getStatsName() {
        return "Second";
    }

    @Override
    HandShape getHandShape() {
        return mWatchFaceState.getWatchFacePreset().getSecondHandShape();
    }

    @Override
    HandLength getHandLength() {
        return mWatchFaceState.getWatchFacePreset().getSecondHandLength();
    }

    @Override
    HandThickness getHandThickness() {
        return mWatchFaceState.getWatchFacePreset().getSecondHandThickness();
    }

    @Override
    HandStalk getHandStalk() {
        return HandStalk.NONE; // Don't have this for the seconds hand.
    }

    @Override
    Style getStyle() {
        return mWatchFaceState.getWatchFacePreset().getSecondHandStyle();
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
