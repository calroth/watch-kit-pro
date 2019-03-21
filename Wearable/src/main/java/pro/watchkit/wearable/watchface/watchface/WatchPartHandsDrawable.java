/*
 * Copyright (C) 2018-2019 Terence Tan
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

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

import java.util.EnumMap;
import java.util.Map;

import androidx.annotation.NonNull;
import pro.watchkit.wearable.watchface.model.WatchFacePreset;
import pro.watchkit.wearable.watchface.model.WatchFacePreset.HandLength;
import pro.watchkit.wearable.watchface.model.WatchFacePreset.HandShape;
import pro.watchkit.wearable.watchface.model.WatchFacePreset.HandStalk;
import pro.watchkit.wearable.watchface.model.WatchFacePreset.HandThickness;

final class WatchPartHandsDrawable extends WatchPartDrawable {
    private static final float HUB_RADIUS_PERCENT = 3f; // 3% // 1.5f; // 1.5%
    private static final float HOUR_MINUTE_HAND_MIDPOINT = 0.333f;
    private static final float ROUND_RECT_RADIUS_PERCENT = 1f;
    private Path mHub;
    private WatchFacePreset mHourPreset = null;
    private Path mHourHandActivePath = new Path();
    private Path mHourHandAmbientPath = new Path();
    private Path mHourHandPath = new Path();
    private WatchFacePreset mMinutePreset = null;
    private Path mMinuteHandActivePath = new Path();
    private Path mMinuteHandAmbientPath = new Path();
    private Path mMinuteHandPath = new Path();
    private WatchFacePreset mSecondPreset = null;
    private Path mSecondHandActivePath = new Path();
    private Path mSecondHandPath = new Path();

    private Map<HandShape, Map<HandThickness, Float>> mHandThicknessDimensions
            = new EnumMap<>(HandShape.class);

    private Map<HandShape, Map<HandLength, Float>> mHandLengthDimensions
            = new EnumMap<>(HandShape.class);

    WatchPartHandsDrawable() {
        super();

        final float STRAIGHT_HAND_WIDTH_PERCENT = 2f;// 2% // 0.3f; // 0.3%
        final float DIAMOND_HAND_ASPECT_RATIO = 8f;

        mHandThicknessDimensions.put(HandShape.STRAIGHT,
                new EnumMap<HandThickness, Float>(HandThickness.class));
        mHandThicknessDimensions.put(HandShape.ROUNDED,
                new EnumMap<HandThickness, Float>(HandThickness.class));
        mHandThicknessDimensions.put(HandShape.DIAMOND,
                new EnumMap<HandThickness, Float>(HandThickness.class));
        mHandThicknessDimensions.put(HandShape.UNKNOWN1,
                new EnumMap<HandThickness, Float>(HandThickness.class));

        mHandThicknessDimensions.get(HandShape.STRAIGHT).put(HandThickness.THIN, 0.5f * STRAIGHT_HAND_WIDTH_PERCENT * 0.5f);
        mHandThicknessDimensions.get(HandShape.STRAIGHT).put(HandThickness.REGULAR, 1.0f * STRAIGHT_HAND_WIDTH_PERCENT * 0.5f);
        mHandThicknessDimensions.get(HandShape.STRAIGHT).put(HandThickness.THICK, 1.5f * STRAIGHT_HAND_WIDTH_PERCENT * 0.5f);
        mHandThicknessDimensions.get(HandShape.STRAIGHT).put(HandThickness.X_THICK, 2.0f * STRAIGHT_HAND_WIDTH_PERCENT * 0.5f);

        mHandThicknessDimensions.get(HandShape.ROUNDED).put(HandThickness.THIN, 0.5f);
        mHandThicknessDimensions.get(HandShape.ROUNDED).put(HandThickness.REGULAR, 1.0f);
        mHandThicknessDimensions.get(HandShape.ROUNDED).put(HandThickness.THICK, 1.5f);
        mHandThicknessDimensions.get(HandShape.ROUNDED).put(HandThickness.X_THICK, 2.0f);

        mHandThicknessDimensions.get(HandShape.DIAMOND).put(HandThickness.THIN, 0.5f * DIAMOND_HAND_ASPECT_RATIO);
        mHandThicknessDimensions.get(HandShape.DIAMOND).put(HandThickness.REGULAR, 1.0f * DIAMOND_HAND_ASPECT_RATIO);
        mHandThicknessDimensions.get(HandShape.DIAMOND).put(HandThickness.THICK, 1.5f * DIAMOND_HAND_ASPECT_RATIO);
        mHandThicknessDimensions.get(HandShape.DIAMOND).put(HandThickness.X_THICK, 2.0f * DIAMOND_HAND_ASPECT_RATIO);

        mHandThicknessDimensions.get(HandShape.UNKNOWN1).put(HandThickness.THIN, 0.5f);
        mHandThicknessDimensions.get(HandShape.UNKNOWN1).put(HandThickness.REGULAR, 1.0f);
        mHandThicknessDimensions.get(HandShape.UNKNOWN1).put(HandThickness.THICK, 1.5f);
        mHandThicknessDimensions.get(HandShape.UNKNOWN1).put(HandThickness.X_THICK, 2.0f);

        mHandLengthDimensions.put(HandShape.STRAIGHT,
                new EnumMap<HandLength, Float>(HandLength.class));
        mHandLengthDimensions.put(HandShape.ROUNDED,
                new EnumMap<HandLength, Float>(HandLength.class));
        mHandLengthDimensions.put(HandShape.DIAMOND,
                new EnumMap<HandLength, Float>(HandLength.class));
        mHandLengthDimensions.put(HandShape.UNKNOWN1,
                new EnumMap<HandLength, Float>(HandLength.class));

        mHandLengthDimensions.get(HandShape.STRAIGHT).put(HandLength.SHORT, 0.6f);
        mHandLengthDimensions.get(HandShape.STRAIGHT).put(HandLength.MEDIUM, 0.8f);
        mHandLengthDimensions.get(HandShape.STRAIGHT).put(HandLength.LONG, 1.0f);
        mHandLengthDimensions.get(HandShape.STRAIGHT).put(HandLength.X_LONG, 1.2f);

        mHandLengthDimensions.get(HandShape.ROUNDED).put(HandLength.SHORT, 0.6f);
        mHandLengthDimensions.get(HandShape.ROUNDED).put(HandLength.MEDIUM, 0.8f);
        mHandLengthDimensions.get(HandShape.ROUNDED).put(HandLength.LONG, 1.0f);
        mHandLengthDimensions.get(HandShape.ROUNDED).put(HandLength.X_LONG, 1.2f);

        mHandLengthDimensions.get(HandShape.DIAMOND).put(HandLength.SHORT, 0.6f);
        mHandLengthDimensions.get(HandShape.DIAMOND).put(HandLength.MEDIUM, 0.8f);
        mHandLengthDimensions.get(HandShape.DIAMOND).put(HandLength.LONG, 1.0f);
        mHandLengthDimensions.get(HandShape.DIAMOND).put(HandLength.X_LONG, 1.2f);

        mHandLengthDimensions.get(HandShape.UNKNOWN1).put(HandLength.SHORT, 0.6f);
        mHandLengthDimensions.get(HandShape.UNKNOWN1).put(HandLength.MEDIUM, 0.8f);
        mHandLengthDimensions.get(HandShape.UNKNOWN1).put(HandLength.LONG, 1.0f);
        mHandLengthDimensions.get(HandShape.UNKNOWN1).put(HandLength.X_LONG, 1.2f);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        super.draw(canvas);
//    }
//    void drawHands(Canvas canvas, WatchFacePreset preset) {
        // Add a hub.
//        Path hub = new Path();
//        hub.addCircle(mCenterX, mCenterY, HUB_RADIUS_PERCENT * pc, Path.Direction.CCW);

        WatchFacePreset preset = mWatchFaceState.getWatchFacePreset();

        /*
         * These calculations reflect the rotation in degrees per unit of time, e.g.,
         * 360 / 60 = 6 and 360 / 12 = 30.
         */
        final float seconds = mWatchFaceState.getSecondsDecimal();
        final float secondsRotation = seconds * 6f;

        final float minuteHandOffset = secondsRotation / 60f;
        final float minutesRotation = mWatchFaceState.getMinutes() * 6f + minuteHandOffset;

        final float hourHandOffset = minutesRotation / 12f;
        final float hoursRotation = mWatchFaceState.getHours() * 30f + hourHandOffset;

        Paint hourHandPaint = mWatchFaceState.getPaintBox().getPaintFromPreset(preset.getHourHandStyle());
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

        Paint minuteHandPaint = mWatchFaceState.getPaintBox().getPaintFromPreset(preset.getMinuteHandStyle());
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
        if (!mWatchFaceState.isAmbient()) {
            Paint secondHandPaint = mWatchFaceState.getPaintBox().getPaintFromPreset(preset.getSecondHandStyle());
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

    private Path getHub() {
        if (mHub == null) {
            mHub = new Path();
            mHub.addCircle(mCenterX, mCenterY, HUB_RADIUS_PERCENT * pc, Path.Direction.CCW);
        }
        return mHub;
    }

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
        if (mWatchFaceState.isAmbient())
            mHourHandAmbientPath.transform(m, mHourHandPath);
        else
            mHourHandActivePath.transform(m, mHourHandPath);

        return mHourHandPath;
    }

    private Path getMinuteHandShape(WatchFacePreset preset, float degreesRotation) {
        boolean cacheHit = false;

        if (mMinutePreset != null) {
            cacheHit = preset.getMinuteHandShape() == mMinutePreset.getMinuteHandShape() &
                    preset.getMinuteHandLength() == mMinutePreset.getMinuteHandLength() &&
                    preset.getMinuteHandThickness() == mMinutePreset.getMinuteHandThickness() &&
                    preset.getMinuteHandStalk() == mMinutePreset.getMinuteHandStalk();
        }

        if (!cacheHit) {
//            Log.d("AnalogWatchFace", "getMinuteHandShape: cache " + (cacheHit ? "hit" : "miss"));

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
//            Log.d("AnalogWatchFace", "getMinuteHandShape: cache 2 " + (cacheHit ? "hit" : "miss"));

            // Set the Minute preset to current. Next time this will ensure a cache hit.
            mMinutePreset = preset.clone();
//            Log.d("AnalogWatchFace", "getMinuteHandShape: cache 3 " + (cacheHit ? "hit" : "miss"));
        }

        // Rotate the hand to its specified position.
        Matrix m = new Matrix();
        m.postRotate(degreesRotation, mCenterX, mCenterY);

        // Reset mMinuteHandPath and rotate the relevant hand into it.
        mMinuteHandPath.reset();
        if (mWatchFaceState.isAmbient())
            mMinuteHandAmbientPath.transform(m, mMinuteHandPath);
        else
            mMinuteHandActivePath.transform(m, mMinuteHandPath);

        return mMinuteHandPath;
    }

    private Path getSecondHandShape(WatchFacePreset preset, float degreesRotation) {
        boolean cacheHit = false;

        if (mSecondPreset != null) {
            cacheHit = preset.getSecondHandShape() == mSecondPreset.getSecondHandShape() &
                    preset.getSecondHandLength() == mSecondPreset.getSecondHandLength() &&
                    preset.getSecondHandThickness() == mSecondPreset.getSecondHandThickness();
        }

        if (!cacheHit) {
//            Log.d("AnalogWatchFace", "getSecondHandShape: cache " + (cacheHit ? "hit" : "miss"));

            // Cache miss. Regenerate the hand.
            mSecondHandActivePath.reset();
            getHandShape(mSecondHandActivePath,
                    preset.getSecondHandShape(), preset.getSecondHandLength(),
                    preset.getSecondHandThickness(), WatchFacePreset.HandStalk.NEGATIVE,
                    false, true);

            // Add the hub to the Second hand in active mode.
            mSecondHandActivePath.op(getHub(), Path.Op.UNION);
//            Log.d("AnalogWatchFace", "getSecondHandShape: cache 2 " + (cacheHit ? "hit" : "miss"));

            // Set the Second preset to current. Next time this will ensure a cache hit.
            mSecondPreset = preset.clone();
//            Log.d("AnalogWatchFace", "getSecondHandShape: cache 3 " + (cacheHit ? "hit" : "miss"));
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
            HandShape handShape, HandLength handLength,
            HandThickness handThickness, HandStalk handStalk,
            boolean isMinuteHand, boolean isSecondHand) {
//        Path p = new Path();

        float length = mHandLengthDimensions.get(handShape).get(handLength);
        float thickness = mHandThicknessDimensions.get(handShape).get(handThickness);
        float bottom;

        if (isMinuteHand) {
            length = length * 40f * pc; // 40%
        } else if (isSecondHand) {
            length = length * 45f * pc; // 45%
        } else {
            length = length * 30f * pc; // 30%
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
        // For the stalk thickness, use the width of the Straight hand shape, but only half.
        float stalkThickness =
                mHandThicknessDimensions.get(HandShape.STRAIGHT).get(handThickness) * pc * 0.5f;
        float stalkTopBitExtra = HUB_RADIUS_PERCENT * pc * 0.5f;

        switch (handStalk) {
            case NEGATIVE: {
                bottom = -HUB_RADIUS_PERCENT * pc * 2;
                break;
            }
            case NONE: {
                bottom = 0;
                break;
            }
            case SHORT: {
                // Current: it's a factor of the size of the hub
                //bottom = HUB_RADIUS_PERCENT * pc * 5;
                // Alternate: it's a factor of the length of the stalk
                bottom = (length - HUB_RADIUS_PERCENT * pc) * 0.25f + HUB_RADIUS_PERCENT * pc;
                // Alternate: it's a factor of the size of the watch face
                //bottom = mCenterX * 0.25f
//                stalk = new Path();
                // Draw a stalk. This is just a straight hand at 1/2 the thickness.
//                p.moveTo(mCenterX + straightWidth1, mCenterY - stalkBottom1);
//                p.lineTo(mCenterX + straightWidth1, mCenterY - bottom);
//                p.lineTo(mCenterX - straightWidth1, mCenterY - bottom);
//                p.lineTo(mCenterX - straightWidth1, mCenterY - stalkBottom1);
//                p.lineTo(mCenterX + straightWidth1, mCenterY - stalkBottom1);
                p.addRoundRect(mCenterX - stalkThickness, mCenterY - bottom - stalkTopBitExtra, mCenterX + stalkThickness,
                        mCenterY - stalkBottom, roundRectRadius, roundRectRadius, Path.Direction.CW);
                break;
            }
            case MEDIUM: {
                // Current: it's a factor of the size of the hub
                //bottom = HUB_RADIUS_PERCENT * pc * 9;
                // Alternate: it's a factor of the length of the stalk
                bottom = (length - HUB_RADIUS_PERCENT * pc) * 0.5f + HUB_RADIUS_PERCENT * pc;
                // Alternate: it's a factor of the size of the watch face
                //bottom = mCenterX * 0.5f
//                stalk = new Path();
                // Draw a stalk. This is just a straight hand at 1/2 the thickness.
//                p.moveTo(mCenterX + straightWidth2, mCenterY - stalkBottom2);
//                p.lineTo(mCenterX + straightWidth2, mCenterY - bottom);
//                p.lineTo(mCenterX - straightWidth2, mCenterY - bottom);
//                p.lineTo(mCenterX - straightWidth2, mCenterY - stalkBottom2);
//                p.lineTo(mCenterX + straightWidth2, mCenterY - stalkBottom2);
                p.addRoundRect(mCenterX - stalkThickness, mCenterY - bottom - stalkTopBitExtra, mCenterX + stalkThickness,
                        mCenterY - stalkBottom, roundRectRadius, roundRectRadius, Path.Direction.CW);
                break;
            }
            default: {
                // Shouldn't happen!
                // Make same as NONE
                bottom = 0;
                break;
            }
        }

        switch (handShape) {
            case STRAIGHT: {
                float straightWidth = thickness * pc;
                p.addRoundRect(mCenterX - straightWidth, mCenterY - length, mCenterX + straightWidth,
                        mCenterY - bottom, roundRectRadius, roundRectRadius, Path.Direction.CW);
//                p.moveTo(mCenterX + straightWidth, mCenterY - bottom);
//                p.lineTo(mCenterX + straightWidth, mCenterY - length);
//                p.lineTo(mCenterX - straightWidth, mCenterY - length);
//                p.lineTo(mCenterX - straightWidth, mCenterY - bottom);
//                p.lineTo(mCenterX + straightWidth, mCenterY - bottom);
                break;
            }
            case DIAMOND: {
                float diamondWidth = thickness;
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
            }
            case ROUNDED: {
                break;
            }
            case UNKNOWN1: {
                break;
            }
        }

        float cutoutWidth = 0.8f * pc; // 0.8 percent

        // Cutout
        switch (handShape) {
            case STRAIGHT: {
                break;
            }
            case DIAMOND: {
                float diamondWidth = thickness;
                // Add extra extension to the diamond top and bottom
                // because the diamond shape tapers to a point
                float diamondTop = length + (HUB_RADIUS_PERCENT * pc * 0.5f);
                float diamondBottom = bottom - (HUB_RADIUS_PERCENT * pc * 0.5f);
                float diamondMidpoint = (diamondTop - diamondBottom) * HOUR_MINUTE_HAND_MIDPOINT +
                        diamondBottom;

                float x = diamondWidth;
                float y = diamondTop - diamondMidpoint;
                float z = (float) Math.sqrt(x * x + y * y);

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
                z = (float) Math.sqrt(x * x + y * y);
                float y3 = w * z / x;

                Path cutout = new Path();
                cutout.moveTo(mCenterX, mCenterY - diamondTop + y2); // Tip
                cutout.lineTo(mCenterX + diamondWidth - x2, mCenterY - diamondMidpoint); // Right
                cutout.lineTo(mCenterX, mCenterY - diamondBottom - y3); // Bottom
                cutout.lineTo(mCenterX - diamondWidth + x2, mCenterY - diamondMidpoint); // Left
                cutout.close();

                p.op(cutout, Path.Op.DIFFERENCE);

                break;
            }
            case ROUNDED: {
                break;
            }
            case UNKNOWN1: {
                break;
            }
        }

        // Stalk cutout
        RectF r;
        switch (handStalk) {
            case NEGATIVE: {
                bottom = -HUB_RADIUS_PERCENT * pc * 2;
                break;
            }
            case NONE: {
                bottom = 0;
                break;
            }
            case SHORT: {
                // Current: it's a factor of the size of the hub
                //bottom = HUB_RADIUS_PERCENT * pc * 5;
                // Alternate: it's a factor of the length of the stalk
                bottom = (length - HUB_RADIUS_PERCENT * pc) * 0.25f + HUB_RADIUS_PERCENT * pc;
                // Alternate: it's a factor of the size of the watch face
                //bottom = mCenterX * 0.25f
//                stalk = new Path();
                // Draw a stalk. This is just a straight hand at 1/2 the thickness.

                r = new RectF(mCenterX - stalkThickness,
                        mCenterY - bottom - stalkTopBitExtra,
                        mCenterX + stalkThickness,
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
            }
            case MEDIUM: {
                // Current: it's a factor of the size of the hub
                //bottom = HUB_RADIUS_PERCENT * pc * 9;
                // Alternate: it's a factor of the length of the stalk
                bottom = (length - HUB_RADIUS_PERCENT * pc) * 0.5f + HUB_RADIUS_PERCENT * pc;
                // Alternate: it's a factor of the size of the watch face
                //bottom = mCenterX * 0.5f
//                stalk = new Path();
                // Draw a stalk. This is just a straight hand at 1/2 the thickness.

                r = new RectF(mCenterX - stalkThickness,
                        mCenterY - bottom - stalkTopBitExtra,
                        mCenterX + stalkThickness,
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
            }
            default: {
                // Shouldn't happen!
                // Make same as NONE
                bottom = 0;
                break;
            }
        }

        // Add the stalk!
//        if (stalk != null) {
//            p.op(stalk, Path.Op.UNION);
//        }

//        return p;
    }
}
