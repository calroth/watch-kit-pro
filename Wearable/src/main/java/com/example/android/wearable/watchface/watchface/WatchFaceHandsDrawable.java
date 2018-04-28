/*
 * Copyright (C) 2018 Terence Tan
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

package com.example.android.wearable.watchface.watchface;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.android.wearable.watchface.model.WatchFacePreset;

import java.util.Calendar;

final class WatchFaceHandsDrawable extends WatchFaceDrawable {
    private static final float HUB_RADIUS_PERCENT = 3f; // 3% // 1.5f; // 1.5%
    private static final float DIAMOND_HAND_ASPECT_RATIO = 8f;
    private static final float STRAIGHT_HAND_WIDTH_PERCENT = 2f; // 2% // 0.3f; // 0.3%
    private static final float HOUR_MINUTE_HAND_MIDPOINT = 0.333f;
    private static final float ROUND_RECT_RADIUS_PERCENT = 1f;

    @Override
    public void draw(@NonNull Canvas canvas) {
        super.draw(canvas);
//    }
//    void drawHands(Canvas canvas, WatchFacePreset preset) {
        // Add a hub.
//        Path hub = new Path();
//        hub.addCircle(mCenterX, mCenterY, HUB_RADIUS_PERCENT * pc, Path.Direction.CCW);

        WatchFacePreset preset = mStateObject.preset;

        /*
         * These calculations reflect the rotation in degrees per unit of time, e.g.,
         * 360 / 60 = 6 and 360 / 12 = 30.
         */
        final float seconds =
                (mCalendar.get(Calendar.SECOND) + mCalendar.get(Calendar.MILLISECOND) / 1000f);
        final float secondsRotation = seconds * 6f;

        final float minuteHandOffset = secondsRotation / 60f;
        final float minutesRotation = mCalendar.get(Calendar.MINUTE) * 6f + minuteHandOffset;

        final float hourHandOffset = minutesRotation / 12f;
        final float hoursRotation = (mCalendar.get(Calendar.HOUR) * 30) + hourHandOffset;

        Paint hourHandPaint = mPalette.getPaintFromPreset(preset.getHourHandPalette());
        Path hourHandShape = getHourHandShape(preset, hoursRotation);
//        Path hourHandShape = getHandShape(
//                preset.getHourHandShape(), preset.getHourHandLength(),
//                preset.getHourHandThickness(), preset.getHourHandStalk(),
//                false, false, hoursRotation, null);
//        if (ambient) {
//            // Punch the hub out of the hour hand in ambient mode.
//            hourHandShape.op(hub, Path.Op.DIFFERENCE);
//        }
        drawPath(canvas, hourHandShape, hourHandPaint);

        Paint minuteHandPaint = mPalette.getPaintFromPreset(preset.getMinuteHandPalette());
        Path minuteHandShape = getMinuteHandShape(preset, minutesRotation);
        // Add the hub to the minute hand in ambient mode.
//        Path minuteHub = ambient ? hub : null;
//        Path minuteHandShape = getHandShape(
//                preset.getMinuteHandShape(), preset.getMinuteHandLength(),
//                preset.getMinuteHandThickness(), preset.getMinuteHandStalk(),
//                true, false, minutesRotation, minuteHub);
        drawPath(canvas, minuteHandShape, minuteHandPaint);

        /*
         * Ensure the "seconds" hand is drawn only when we are in interactive mode.
         * Otherwise, we only update the watch face once a minute.
         */
        if (!mStateObject.ambient) {
            Paint secondHandPaint = mPalette.getPaintFromPreset(preset.getSecondHandPalette());
            // Add the hub to the second hand in interactive mode.
            Path secondHandShape = getSecondHandShape(preset, secondsRotation);
//            Path secondHandShape = getHandShape(
//                    preset.getSecondHandShape(), preset.getSecondHandLength(),
//                    preset.getSecondHandThickness(), WatchFacePreset.HandStalk.NEGATIVE,
//                    false, true, secondsRotation, hub);
            drawPath(canvas, secondHandShape, secondHandPaint);
        }
//        drawCircle(canvas, mCenterX, mCenterY, HUB_RADIUS_PERCENT * pc, mHandPaint);
//        Log.d("AnalogWatchFace", "End ticks 2");
    }

    private Path mHub;
    private Path getHub() {
        if (mHub == null) {
            mHub = new Path();
            mHub.addCircle(mCenterX, mCenterY, HUB_RADIUS_PERCENT * pc, Path.Direction.CCW);
        }
        return mHub;
    }

    private WatchFacePreset mHourPreset = null;
    private Path mHourHandActivePath = new Path();
    private Path mHourHandAmbientPath = new Path();
    private Path mHourHandPath = new Path();

    private Path getHourHandShape(WatchFacePreset preset, float degreesRotation) {
        boolean cacheHit = false;

        if (mHourPreset != null) {
            cacheHit = preset.getHourHandShape() == mHourPreset.getHourHandShape() &
                    preset.getHourHandLength() == mHourPreset.getHourHandLength() &&
                    preset.getHourHandThickness() == mHourPreset.getHourHandThickness() &&
                    preset.getHourHandStalk() == mHourPreset.getHourHandStalk();
        }

        if (!cacheHit) {
//            Log.d("AnalogWatchFace", "getHourHandShape: cache " + (cacheHit ? "hit" : "miss"));

            // Cache miss. Regenerate the hand.
            mHourHandActivePath.reset();
            getHandShape(mHourHandActivePath,
                    preset.getHourHandShape(), preset.getHourHandLength(),
                    preset.getHourHandThickness(), preset.getHourHandStalk(),
                    false, false);

            mHourHandAmbientPath.reset();
            mHourHandAmbientPath.addPath(mHourHandActivePath);
            // Punch the hub out of the hour hand in ambient mode.
            mHourHandAmbientPath.op(getHub(), Path.Op.DIFFERENCE);
//            Log.d("AnalogWatchFace", "getHourHandShape: cache 2 " + (cacheHit ? "hit" : "miss"));

            // Set the hour preset to current. Next time this will ensure a cache hit.
            mHourPreset = preset.clone();
//            Log.d("AnalogWatchFace", "getHourHandShape: cache 3 " + (cacheHit ? "hit" : "miss"));
        }

        // Rotate the hand to its specified position.
        Matrix m = new Matrix();
        m.postRotate(degreesRotation, mCenterX, mCenterY);

        // Reset mHourHandPath and rotate the relevant hand into it.
        mHourHandPath.reset();
        if (mStateObject.ambient)
            mHourHandAmbientPath.transform(m, mHourHandPath);
        else
            mHourHandActivePath.transform(m, mHourHandPath);

        return mHourHandPath;
    }

    private WatchFacePreset mMinutePreset = null;
    private Path mMinuteHandActivePath = new Path();
    private Path mMinuteHandAmbientPath = new Path();
    private Path mMinuteHandPath = new Path();

    private Path getMinuteHandShape(WatchFacePreset preset, float degreesRotation) {
        boolean cacheHit = false;

        if (mMinutePreset != null) {
            cacheHit = preset.getMinuteHandShape() == mMinutePreset.getMinuteHandShape() &
                    preset.getMinuteHandLength() == mMinutePreset.getMinuteHandLength() &&
                    preset.getMinuteHandThickness() == mMinutePreset.getMinuteHandThickness() &&
                    preset.getMinuteHandStalk() == mMinutePreset.getMinuteHandStalk();
        }

        if (!cacheHit) {
            Log.d("AnalogWatchFace", "getMinuteHandShape: cache " + (cacheHit ? "hit" : "miss"));

            // Cache miss. Regenerate the hand.
            mMinuteHandActivePath.reset();
            getHandShape(mMinuteHandActivePath,
                    preset.getMinuteHandShape(), preset.getMinuteHandLength(),
                    preset.getMinuteHandThickness(), preset.getMinuteHandStalk(),
                    true, false);

            mMinuteHandAmbientPath.reset();
            mMinuteHandAmbientPath.addPath(mMinuteHandActivePath);
            // Add the hub to the Minute hand in ambient mode.
            mMinuteHandAmbientPath.op(getHub(), Path.Op.UNION);
            Log.d("AnalogWatchFace", "getMinuteHandShape: cache 2 " + (cacheHit ? "hit" : "miss"));

            // Set the Minute preset to current. Next time this will ensure a cache hit.
            mMinutePreset = preset.clone();
            Log.d("AnalogWatchFace", "getMinuteHandShape: cache 3 " + (cacheHit ? "hit" : "miss"));
        }

        // Rotate the hand to its specified position.
        Matrix m = new Matrix();
        m.postRotate(degreesRotation, mCenterX, mCenterY);

        // Reset mMinuteHandPath and rotate the relevant hand into it.
        mMinuteHandPath.reset();
        if (mStateObject.ambient)
            mMinuteHandAmbientPath.transform(m, mMinuteHandPath);
        else
            mMinuteHandActivePath.transform(m, mMinuteHandPath);

        return mMinuteHandPath;
    }

    private WatchFacePreset mSecondPreset = null;
    private Path mSecondHandActivePath = new Path();
    private Path mSecondHandPath = new Path();

    private Path getSecondHandShape(WatchFacePreset preset, float degreesRotation) {
        boolean cacheHit = false;

        if (mSecondPreset != null) {
            cacheHit = preset.getSecondHandShape() == mSecondPreset.getSecondHandShape() &
                    preset.getSecondHandLength() == mSecondPreset.getSecondHandLength() &&
                    preset.getSecondHandThickness() == mSecondPreset.getSecondHandThickness();
        }

        if (!cacheHit) {
            Log.d("AnalogWatchFace", "getSecondHandShape: cache " + (cacheHit ? "hit" : "miss"));

            // Cache miss. Regenerate the hand.
            mSecondHandActivePath.reset();
            getHandShape(mSecondHandActivePath,
                    preset.getSecondHandShape(), preset.getSecondHandLength(),
                    preset.getSecondHandThickness(), WatchFacePreset.HandStalk.NEGATIVE,
                    false, true);

            // Add the hub to the Second hand in active mode.
            mSecondHandActivePath.op(getHub(), Path.Op.UNION);
            Log.d("AnalogWatchFace", "getSecondHandShape: cache 2 " + (cacheHit ? "hit" : "miss"));

            // Set the Second preset to current. Next time this will ensure a cache hit.
            mSecondPreset = preset.clone();
            Log.d("AnalogWatchFace", "getSecondHandShape: cache 3 " + (cacheHit ? "hit" : "miss"));
        }

        // Rotate the hand to its specified position.
        Matrix m = new Matrix();
        m.postRotate(degreesRotation, mCenterX, mCenterY);

        // Reset mSecondHandPath and rotate the relevant hand into it.
        mSecondHandPath.reset();
        mSecondHandActivePath.transform(m, mSecondHandPath);

        return mSecondHandPath;
    }

    private void getHandShape(
            Path p,
            WatchFacePreset.HandShape handShape, WatchFacePreset.HandLength handLength,
            WatchFacePreset.HandThickness handThickness, WatchFacePreset.HandStalk handStalk,
            boolean isMinuteHand, boolean isSecondHand) {
//        Path p = new Path();

        float length, thickness;
        float bottom;

        switch (handLength) {
            case SHORT:
                length = 0.6f;
                break;
            case MEDIUM:
                length = 0.8f;
                break;
            case LONG:
                length = 1.0f;
                break;
            case X_LONG:
                length = 1.2f;
                break;
            default:
                // Shouldn't happen!
                // Make same as LONG
                length = 1.0f;
        }

        if (isMinuteHand) {
            length = length * 40f * pc; // 40%
        } else if (isSecondHand) {
            length = length * 45f * pc; // 45%
        } else {
            length = length * 30f * pc; // 30%
        }

        switch (handThickness) {
            case THIN:
                thickness = 0.5f;
                break;
            case REGULAR:
                thickness = 1.0f;
                break;
            case THICK:
                thickness = 1.5f;
                break;
            case X_THICK:
                thickness = 2.0f;
                break;
            default:
                // Shouldn't happen!
                // Make same as REGULAR
                thickness = 1.0f;
        }

        if (isSecondHand) {
            // Second hands are automatically thinner.
            thickness /= 3;
        }

//        Path stalk = null;
        float roundRectRadius = ROUND_RECT_RADIUS_PERCENT * pc;
        float stalkBottom = -HUB_RADIUS_PERCENT * pc * 2;
        // We add a bit extra to the stalk top so it overlaps with the hand,
        // in order that the union works OK without gaps.
        float stalkTopBitExtra = HUB_RADIUS_PERCENT * pc * 0.5f;

        switch (handStalk) {
            case NEGATIVE:
                bottom = -HUB_RADIUS_PERCENT * pc * 2;
                break;
            case NONE:
                bottom = 0;
                break;
            case SHORT:
                // Current: it's a factor of the size of the hub
                //bottom = HUB_RADIUS_PERCENT * pc * 5;
                // Alternate: it's a factor of the length of the stalk
                bottom = (length - HUB_RADIUS_PERCENT * pc) * 0.25f + HUB_RADIUS_PERCENT * pc;
                // Alternate: it's a factor of the size of the watch face
                //bottom = mCenterX * 0.25f
//                stalk = new Path();
                // Draw a stalk. This is just a straight hand at 1/2 the thickness.
                float straightWidth1 = STRAIGHT_HAND_WIDTH_PERCENT * pc * thickness / 4f;
//                p.moveTo(mCenterX + straightWidth1, mCenterY - stalkBottom1);
//                p.lineTo(mCenterX + straightWidth1, mCenterY - bottom);
//                p.lineTo(mCenterX - straightWidth1, mCenterY - bottom);
//                p.lineTo(mCenterX - straightWidth1, mCenterY - stalkBottom1);
//                p.lineTo(mCenterX + straightWidth1, mCenterY - stalkBottom1);
                p.addRoundRect(mCenterX - straightWidth1, mCenterY - bottom - stalkTopBitExtra, mCenterX + straightWidth1,
                        mCenterY - stalkBottom, roundRectRadius, roundRectRadius, Path.Direction.CW);
                break;
            case MEDIUM:
                // Current: it's a factor of the size of the hub
                //bottom = HUB_RADIUS_PERCENT * pc * 9;
                // Alternate: it's a factor of the length of the stalk
                bottom = (length - HUB_RADIUS_PERCENT * pc) * 0.5f + HUB_RADIUS_PERCENT * pc;
                // Alternate: it's a factor of the size of the watch face
                //bottom = mCenterX * 0.5f
//                stalk = new Path();
                // Draw a stalk. This is just a straight hand at 1/2 the thickness.
                float straightWidth2 = STRAIGHT_HAND_WIDTH_PERCENT * pc * thickness / 4f;
//                p.moveTo(mCenterX + straightWidth2, mCenterY - stalkBottom2);
//                p.lineTo(mCenterX + straightWidth2, mCenterY - bottom);
//                p.lineTo(mCenterX - straightWidth2, mCenterY - bottom);
//                p.lineTo(mCenterX - straightWidth2, mCenterY - stalkBottom2);
//                p.lineTo(mCenterX + straightWidth2, mCenterY - stalkBottom2);
                p.addRoundRect(mCenterX - straightWidth2, mCenterY - bottom - stalkTopBitExtra, mCenterX + straightWidth2,
                        mCenterY - stalkBottom, roundRectRadius, roundRectRadius, Path.Direction.CW);
                break;
            default:
                // Shouldn't happen!
                // Make same as NONE
                bottom = 0;
                break;
        }

        switch (handShape) {
            case STRAIGHT:
                float straightWidth = STRAIGHT_HAND_WIDTH_PERCENT * pc * thickness / 2f;
                p.addRoundRect(mCenterX - straightWidth, mCenterY - length, mCenterX + straightWidth,
                        mCenterY - bottom, roundRectRadius, roundRectRadius, Path.Direction.CW);
//                p.moveTo(mCenterX + straightWidth, mCenterY - bottom);
//                p.lineTo(mCenterX + straightWidth, mCenterY - length);
//                p.lineTo(mCenterX - straightWidth, mCenterY - length);
//                p.lineTo(mCenterX - straightWidth, mCenterY - bottom);
//                p.lineTo(mCenterX + straightWidth, mCenterY - bottom);
                break;
            case DIAMOND:
                float diamondWidth = DIAMOND_HAND_ASPECT_RATIO * thickness;
                // Add extra extension to the diamond top and bottom
                // because the diamond shape tapers to a point
                float diamondTop = length + (HUB_RADIUS_PERCENT * pc * 0.5f);
                float diamondBottom = bottom - (HUB_RADIUS_PERCENT * pc * 0.5f);
                float diamondMidpoint = (diamondTop - diamondBottom) * HOUR_MINUTE_HAND_MIDPOINT +
                        diamondBottom;
                p.moveTo(mCenterX, mCenterY - diamondBottom); // Extend past the hub
                p.lineTo(mCenterX - diamondWidth, mCenterY - diamondMidpoint);
                p.lineTo(mCenterX, mCenterY - diamondTop);
                p.lineTo(mCenterX + diamondWidth, mCenterY - diamondMidpoint);
                //p.lineTo(mCenterX, mCenterY - diamondBottom); // Extend past the hub
                p.close();
                //p.lineTo(mCenterX, mCenterY - diamondTop);
                break;
            case ROUNDED:
                break;
            case UNKNOWN1:
                break;
        }

        float cutoutWidth = 0.8f * pc; // 0.8 percent

        // Cutout
        switch (handShape) {
            case STRAIGHT:
                break;
            case DIAMOND:
                float diamondWidth = DIAMOND_HAND_ASPECT_RATIO * thickness;
                // Add extra extension to the diamond top and bottom
                // because the diamond shape tapers to a point
                float diamondTop = length + (HUB_RADIUS_PERCENT * pc * 0.5f);
                float diamondBottom = bottom - (HUB_RADIUS_PERCENT * pc * 0.5f);
                float diamondMidpoint = (diamondTop - diamondBottom) * HOUR_MINUTE_HAND_MIDPOINT +
                        diamondBottom;

                float x = diamondWidth;
                float y = diamondTop - diamondMidpoint;
                float z = (float)Math.sqrt(x * x + y * y);

                float w = cutoutWidth;

                float y2 = w * z / x;
                float x2 = w * z / y;

                // 1 / z = sin α / x
                // x / z = sin α
                // y / z = sin β

                // sin α = w / y2
                // sin β = w / x2

                // Ratio x : w = Ratio z : y1
                // Ratio w : x = Ratio y1 : z
                // w / x = y1 / z
                // w * z / x = y1

                y = diamondMidpoint - diamondBottom;
                z = (float)Math.sqrt(x * x + y * y);
                float y3 = w * z / x;

                Path cutout = new Path();
                cutout.moveTo(mCenterX, mCenterY - diamondTop + y2); // Tip
                cutout.lineTo(mCenterX + diamondWidth - x2, mCenterY - diamondMidpoint); // Right
                cutout.lineTo(mCenterX, mCenterY - diamondBottom - y3); // Bottom
                cutout.lineTo(mCenterX - diamondWidth + x2, mCenterY - diamondMidpoint); // Left
                cutout.close();

                p.op(cutout, Path.Op.DIFFERENCE);

                break;
            case ROUNDED:
                break;
            case UNKNOWN1:
                break;
        }

        // Stalk cutout
        RectF r;
        switch (handStalk) {
            case NEGATIVE:
                bottom = -HUB_RADIUS_PERCENT * pc * 2;
                break;
            case NONE:
                bottom = 0;
                break;
            case SHORT:
                // Current: it's a factor of the size of the hub
                //bottom = HUB_RADIUS_PERCENT * pc * 5;
                // Alternate: it's a factor of the length of the stalk
                bottom = (length - HUB_RADIUS_PERCENT * pc) * 0.25f + HUB_RADIUS_PERCENT * pc;
                // Alternate: it's a factor of the size of the watch face
                //bottom = mCenterX * 0.25f
//                stalk = new Path();
                // Draw a stalk. This is just a straight hand at 1/2 the thickness.
                float straightWidth1 = STRAIGHT_HAND_WIDTH_PERCENT * pc * thickness / 4f;

                r = new RectF(mCenterX - straightWidth1,
                        mCenterY - bottom - stalkTopBitExtra,
                        mCenterX + straightWidth1,
                        mCenterY - stalkBottom);
                r.inset(cutoutWidth, cutoutWidth);

                // Only if our cutout isn't wider than the stalk itself...
                if (r.left < r.right) {
                    Path cutout = new Path();
                    float radius = roundRectRadius - cutoutWidth;
                    radius = radius > 0f ? radius : 0f;
                    cutout.addRoundRect(r, radius, radius, Path.Direction.CW);
                    p.op(cutout, Path.Op.DIFFERENCE);
                }
                break;
            case MEDIUM:
                // Current: it's a factor of the size of the hub
                //bottom = HUB_RADIUS_PERCENT * pc * 9;
                // Alternate: it's a factor of the length of the stalk
                bottom = (length - HUB_RADIUS_PERCENT * pc) * 0.5f + HUB_RADIUS_PERCENT * pc;
                // Alternate: it's a factor of the size of the watch face
                //bottom = mCenterX * 0.5f
//                stalk = new Path();
                // Draw a stalk. This is just a straight hand at 1/2 the thickness.
                float straightWidth2 = STRAIGHT_HAND_WIDTH_PERCENT * pc * thickness / 4f;

                r = new RectF(mCenterX - straightWidth2,
                        mCenterY - bottom - stalkTopBitExtra,
                        mCenterX + straightWidth2,
                        mCenterY - stalkBottom);
                r.inset(cutoutWidth, cutoutWidth);

                // Only if our cutout isn't wider than the stalk itself...
                if (r.left < r.right) {
                    Path cutout = new Path();
                    float radius = roundRectRadius - cutoutWidth;
                    radius = radius > 0f ? radius : 0f;
                    cutout.addRoundRect(r, radius, radius, Path.Direction.CW);
                    p.op(cutout, Path.Op.DIFFERENCE);
                }
                break;
            default:
                // Shouldn't happen!
                // Make same as NONE
                bottom = 0;
                break;
        }

        // Add the stalk!
//        if (stalk != null) {
//            p.op(stalk, Path.Op.UNION);
//        }

//        return p;
    }
}
