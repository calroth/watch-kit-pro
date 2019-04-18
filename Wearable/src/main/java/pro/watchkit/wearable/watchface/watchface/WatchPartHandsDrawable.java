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
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

import androidx.annotation.NonNull;
import pro.watchkit.wearable.watchface.model.WatchFacePreset.HandLength;
import pro.watchkit.wearable.watchface.model.WatchFacePreset.HandShape;
import pro.watchkit.wearable.watchface.model.WatchFacePreset.HandStalk;
import pro.watchkit.wearable.watchface.model.WatchFacePreset.HandThickness;
import pro.watchkit.wearable.watchface.model.WatchFacePreset.Style;

abstract class WatchPartHandsDrawable extends WatchPartDrawable {
    private static final float HUB_RADIUS_PERCENT = 3f; // 3% // 1.5f; // 1.5%
    private static final float HOUR_MINUTE_HAND_MIDPOINT = 0.333f;
    private static final float ROUND_RECT_RADIUS_PERCENT = 1.5f;

    private Map<HandShape, Map<HandThickness, Float>> mHandThicknessDimensions
            = new EnumMap<>(HandShape.class);

    private Map<HandShape, Map<HandLength, Float>> mHandLengthDimensions
            = new EnumMap<>(HandShape.class);

    private Path mHub = new Path();

    private Path mHandActivePath = new Path();
    private Path mHandAmbientPath = new Path();
    private int mPreviousSerial = -1;
    private Path mStalk = new Path();
    private Path mCutout = new Path();

    WatchPartHandsDrawable() {
        super();

        final float STRAIGHT_HAND_WIDTH_PERCENT = 2f;// 2% // 0.3f; // 0.3%
        final float DIAMOND_HAND_ASPECT_RATIO = 8f;

        float globalScale = 1.0f;

        // f0, f1, f2, f3 are a geometric series!
        float f0 = globalScale * (float) (1d / Math.sqrt(2d));
        float f1 = globalScale * 1f;
        float f2 = globalScale * (float) Math.sqrt(2d);
        float f3 = globalScale * 2f;

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

        mHandThicknessDimensions.get(HandShape.DIAMOND).put(HandThickness.THIN, f0 * DIAMOND_HAND_ASPECT_RATIO);
        mHandThicknessDimensions.get(HandShape.DIAMOND).put(HandThickness.REGULAR, f1 * DIAMOND_HAND_ASPECT_RATIO);
        mHandThicknessDimensions.get(HandShape.DIAMOND).put(HandThickness.THICK, f2 * DIAMOND_HAND_ASPECT_RATIO);
        mHandThicknessDimensions.get(HandShape.DIAMOND).put(HandThickness.X_THICK, f3 * DIAMOND_HAND_ASPECT_RATIO);

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

        mHandLengthDimensions.get(HandShape.STRAIGHT).put(HandLength.SHORT, 2f + f0);
        mHandLengthDimensions.get(HandShape.STRAIGHT).put(HandLength.MEDIUM, 2f + f1);
        mHandLengthDimensions.get(HandShape.STRAIGHT).put(HandLength.LONG, 2f + f2);
        mHandLengthDimensions.get(HandShape.STRAIGHT).put(HandLength.X_LONG, 2f + f3);

        mHandLengthDimensions.get(HandShape.ROUNDED).put(HandLength.SHORT, 2f + f0);
        mHandLengthDimensions.get(HandShape.ROUNDED).put(HandLength.MEDIUM, 2f + f1);
        mHandLengthDimensions.get(HandShape.ROUNDED).put(HandLength.LONG, 2f + f2);
        mHandLengthDimensions.get(HandShape.ROUNDED).put(HandLength.X_LONG, 2f + f3);

        mHandLengthDimensions.get(HandShape.DIAMOND).put(HandLength.SHORT, 2f + f0);
        mHandLengthDimensions.get(HandShape.DIAMOND).put(HandLength.MEDIUM, 2f + f1);
        mHandLengthDimensions.get(HandShape.DIAMOND).put(HandLength.LONG, 2f + f2);
        mHandLengthDimensions.get(HandShape.DIAMOND).put(HandLength.X_LONG, 2f + f3);

        mHandLengthDimensions.get(HandShape.UNKNOWN1).put(HandLength.SHORT, 2f + f0);
        mHandLengthDimensions.get(HandShape.UNKNOWN1).put(HandLength.MEDIUM, 2f + f1);
        mHandLengthDimensions.get(HandShape.UNKNOWN1).put(HandLength.LONG, 2f + f2);
        mHandLengthDimensions.get(HandShape.UNKNOWN1).put(HandLength.X_LONG, 2f + f3);
    }

    private float mLastDegrees = -360f;

    @Override
    public void draw2(@NonNull Canvas canvas) {
        // Reset the exclusion path. We ignore it for this layer up!
        resetExclusionPath();

        Paint paint = mWatchFaceState.getPaintBox().getPaintFromPreset(getStyle());
        Path path = getHandPath();
        float degrees = getDegreesRotation();
        if ((degrees - mLastDegrees) % 360f > 6f) {
            // Generate a new bezel if the current one is more than 6 degrees (1 minute) out.
            generateBezels(path, degrees);
            mLastDegrees = degrees;
        }
        fastDrawPath(canvas, path, paint, degrees);
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        mHub.reset();
        mHub.addCircle(mCenterX, mCenterY, HUB_RADIUS_PERCENT * pc, getDirection());

        mPreviousSerial = -1;
    }

    Path getHub() {
        return mHub;
    }

    abstract HandShape getHandShape();

    abstract HandLength getHandLength();

    abstract HandThickness getHandThickness();

    abstract HandStalk getHandStalk();

    abstract Style getStyle();

    abstract float getDegreesRotation();

    abstract void punchHub(Path active, Path ambient);

    boolean isMinuteHand() {
        return false;
    }

    boolean isSecondHand() {
        return false;
    }

    private Path getHandPath() {
        // Regenerate "mHandActivePath" and "mHandAmbientPath" if we need to.
        int currentSerial = Objects.hash(mWatchFaceState);

        if (mPreviousSerial != currentSerial) {
            mPreviousSerial = currentSerial;
            // Cache miss. Regenerate the hand.
            mHandActivePath.reset();
            getHandShapePath();
            mHandAmbientPath.reset();
            mHandAmbientPath.addPath(mHandActivePath);
            punchHub(mHandActivePath, mHandAmbientPath);
            // Regenerate our bezels too.
            mLastDegrees = -360f;
        }

        if (mWatchFaceState.isAmbient())
            return mHandAmbientPath;
        else
            return mHandActivePath;
    }

    private void getHandShapePath() {
        Path p = mHandActivePath;
        HandShape handShape = getHandShape();
        HandLength handLength = getHandLength();
        HandThickness handThickness = getHandThickness();
        HandStalk handStalk = getHandStalk();
        boolean isMinuteHand = isMinuteHand();
        boolean isSecondHand = isSecondHand();

        float length = mHandLengthDimensions.get(handShape).get(handLength);
        float thickness = mHandThicknessDimensions.get(handShape).get(handThickness);
        float bottom;

        if (isMinuteHand || isSecondHand) {
            length = length * 12.5f * pc; // 12.5%
            // Min multiplier is 2.7 (for hand length short) so that'd be 33.75%
            // Max multiplier is 4 (for hand length x-long) so that'd be 50%.
        } else {
            length = length * 12.5f * pc * 0.61803398875f; // 12.5% - golden ratio
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

        // A bit more for tweaking.
        stalkTopBitExtra += 10.0f * pc;
        mStalk.reset();

        switch (handStalk) {
            case NEGATIVE: {
                bottom = -HUB_RADIUS_PERCENT * pc * 2f;
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
                mStalk.addRoundRect(mCenterX - stalkThickness, mCenterY - bottom - stalkTopBitExtra, mCenterX + stalkThickness,
                        mCenterY - stalkBottom, roundRectRadius, roundRectRadius, getDirection());
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
                mStalk.addRoundRect(mCenterX - stalkThickness, mCenterY - bottom - stalkTopBitExtra, mCenterX + stalkThickness,
                        mCenterY - stalkBottom, roundRectRadius, roundRectRadius, getDirection());
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
                p.addRect(mCenterX - straightWidth, mCenterY - length, mCenterX + straightWidth,
                        mCenterY - bottom, getDirection());
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
                if (getDirection() == Path.Direction.CW) {
                    p.lineTo(mCenterX - diamondWidth, mCenterY - diamondMidpoint); // Left
                    p.lineTo(mCenterX, mCenterY - diamondTop); // Top
                    p.lineTo(mCenterX + diamondWidth, mCenterY - diamondMidpoint); // Right
                } else {
                    p.lineTo(mCenterX + diamondWidth, mCenterY - diamondMidpoint); // Right
                    p.lineTo(mCenterX, mCenterY - diamondTop); // Top
                    p.lineTo(mCenterX - diamondWidth, mCenterY - diamondMidpoint); // Left
                }
                //p.lineTo(mCenterX, mCenterY - diamondBottom); // Extend past the hub
                p.close();
                //p.lineTo(mCenterX, mCenterY - diamondTop);
                break;
            }
            case ROUNDED: {
                float straightWidth = thickness * pc;
                p.addRoundRect(mCenterX - straightWidth, mCenterY - length, mCenterX + straightWidth,
                        mCenterY - bottom, roundRectRadius, roundRectRadius, getDirection());
                break;
            }
            case UNKNOWN1: {
                float straightWidth = thickness * pc;
                p.addRoundRect(mCenterX - straightWidth, mCenterY - length, mCenterX + straightWidth,
                        mCenterY - bottom, roundRectRadius * 2f, roundRectRadius * 2f, getDirection());
                break;
            }
        }

        p.op(mStalk, Path.Op.UNION);

        float cutoutWidth = 1.2f * pc; // 1.2 percent

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

                float w = cutoutWidth * 1.5f;

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

                mCutout.reset();
                mCutout.moveTo(mCenterX, mCenterY - diamondTop + y2); // Tip
                if (getDirection() == Path.Direction.CW) {
                    mCutout.lineTo(mCenterX + diamondWidth - x2, mCenterY - diamondMidpoint); // Right
                    mCutout.lineTo(mCenterX, mCenterY - diamondBottom - y3); // Bottom
                    mCutout.lineTo(mCenterX - diamondWidth + x2, mCenterY - diamondMidpoint); // Left
                } else {
                    mCutout.lineTo(mCenterX - diamondWidth + x2, mCenterY - diamondMidpoint); // Left
                    mCutout.lineTo(mCenterX, mCenterY - diamondBottom - y3); // Bottom
                    mCutout.lineTo(mCenterX + diamondWidth - x2, mCenterY - diamondMidpoint); // Right
                }
                mCutout.close();

                p.op(mCutout, Path.Op.DIFFERENCE);

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
                    mCutout.reset();
                    float radius = roundRectRadius - cutoutWidth;
                    radius = radius > 0f ? radius : 0f;
                    mCutout.addRoundRect(r, radius, radius, getDirection());
                    p.op(mCutout, Path.Op.DIFFERENCE);
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
                    mCutout.reset();
                    float radius = roundRectRadius - cutoutWidth;
                    radius = radius > 0f ? radius : 0f;
                    mCutout.addRoundRect(r, radius, radius, getDirection());
                    p.op(mCutout, Path.Op.DIFFERENCE);
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
