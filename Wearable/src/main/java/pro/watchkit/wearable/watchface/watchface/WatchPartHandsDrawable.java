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
    private Path mHandCutout = new Path();
    private Path mStalkCutout = new Path();

    WatchPartHandsDrawable() {
        super();

        float globalScale = 1.0f;

        // f0, f1, f2, f3 are a geometric series!
        float f0 = globalScale * (float) (1d / Math.sqrt(2d));
        float f1 = globalScale * 1f;
        float f2 = globalScale * (float) Math.sqrt(2d);
        float f3 = globalScale * 2f;

        // Diamonds are drawn slightly thicker to account for the fact they taper at the ends.
        final float DIAMOND_HAND_ASPECT_RATIO = f2;

        mHandThicknessDimensions.put(HandShape.STRAIGHT,
                new EnumMap<HandThickness, Float>(HandThickness.class));
        mHandThicknessDimensions.put(HandShape.ROUNDED,
                new EnumMap<HandThickness, Float>(HandThickness.class));
        mHandThicknessDimensions.put(HandShape.DIAMOND,
                new EnumMap<HandThickness, Float>(HandThickness.class));
        mHandThicknessDimensions.put(HandShape.UNKNOWN1,
                new EnumMap<HandThickness, Float>(HandThickness.class));

        mHandThicknessDimensions.get(HandShape.STRAIGHT).put(HandThickness.THIN, f0);
        mHandThicknessDimensions.get(HandShape.STRAIGHT).put(HandThickness.REGULAR, f1);
        mHandThicknessDimensions.get(HandShape.STRAIGHT).put(HandThickness.THICK, f2);
        mHandThicknessDimensions.get(HandShape.STRAIGHT).put(HandThickness.X_THICK, f3);

        mHandThicknessDimensions.get(HandShape.ROUNDED).put(HandThickness.THIN, f0);
        mHandThicknessDimensions.get(HandShape.ROUNDED).put(HandThickness.REGULAR, f1);
        mHandThicknessDimensions.get(HandShape.ROUNDED).put(HandThickness.THICK, f2);
        mHandThicknessDimensions.get(HandShape.ROUNDED).put(HandThickness.X_THICK, f3);

        mHandThicknessDimensions.get(HandShape.DIAMOND).put(HandThickness.THIN, f0 * DIAMOND_HAND_ASPECT_RATIO);
        mHandThicknessDimensions.get(HandShape.DIAMOND).put(HandThickness.REGULAR, f1 * DIAMOND_HAND_ASPECT_RATIO);
        mHandThicknessDimensions.get(HandShape.DIAMOND).put(HandThickness.THICK, f2 * DIAMOND_HAND_ASPECT_RATIO);
        mHandThicknessDimensions.get(HandShape.DIAMOND).put(HandThickness.X_THICK, f3 * DIAMOND_HAND_ASPECT_RATIO);

        mHandThicknessDimensions.get(HandShape.UNKNOWN1).put(HandThickness.THIN, f0);
        mHandThicknessDimensions.get(HandShape.UNKNOWN1).put(HandThickness.REGULAR, f1);
        mHandThicknessDimensions.get(HandShape.UNKNOWN1).put(HandThickness.THICK, f2);
        mHandThicknessDimensions.get(HandShape.UNKNOWN1).put(HandThickness.X_THICK, f3);

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
            getHandShapePath(mHandActivePath);
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

    private void getHandShapePath(Path p) {
        HandShape handShape = getHandShape();
        HandLength handLength = getHandLength();
        HandThickness handThickness = getHandThickness();
        HandStalk handStalk = getHandStalk();
        boolean isMinuteHand = isMinuteHand();
        boolean isSecondHand = isSecondHand();

        float thickness = mHandThicknessDimensions.get(handShape).get(handThickness);
        float top, bottom;

        {
            float length = mHandLengthDimensions.get(handShape).get(handLength);
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

            top = mCenterY - length;
        }

        float roundRectRadius = ROUND_RECT_RADIUS_PERCENT * pc;
        // We add a bit extra to the stalk top so it overlaps with the hand,
        // in order that the union works OK without gaps.
        // For the stalk thickness, use the width of the Straight hand shape, but only half.
        float stalkThickness =
                mHandThicknessDimensions.get(HandShape.STRAIGHT).get(handThickness) * pc * 0.5f;

        mStalk.reset();
        mHandCutout.reset();
        mStalkCutout.reset();

        switch (handStalk) {
            case NEGATIVE: {
                bottom = mCenterY + HUB_RADIUS_PERCENT * pc * 2f;
                break;
            }
            case NONE: {
                bottom = mCenterY;
                break;
            }
            case SHORT: {
                // Stalk length is 25% of hand length. This dimension "bottom" refers to the hand.
                bottom = (top + mCenterY + mCenterY + mCenterY) / 4f;

                // Draw the stalk from the hub to 50% of the hand length.
                // There'll be some overlap, that's OK!
                float stalkTop = (top + mCenterY) / 2f;
                float stalkBottom = mCenterY + HUB_RADIUS_PERCENT * pc * 2;

                // Draw a stalk.
                drawRoundRect(mStalk,
                        mCenterX - stalkThickness, stalkTop,
                        mCenterX + stalkThickness, stalkBottom, roundRectRadius, 0f);

                // Draw a cutout.
                drawRoundRect(mStalkCutout,
                        mCenterX - stalkThickness, stalkTop,
                        mCenterX + stalkThickness, stalkBottom, roundRectRadius, 0.5f);
                break;
            }
            case MEDIUM: {
                // Stalk length is 25% of hand length. This dimension "bottom" refers to the hand.
                bottom = (top + mCenterY) / 2f;

                // Draw the stalk from the hub to 75% of the hand length.
                // There'll be some overlap, that's OK!
                float stalkTop = (top + top + top + mCenterY) / 4f;
                float stalkBottom = mCenterY + HUB_RADIUS_PERCENT * pc * 2;

                // Draw a stalk.
                drawRoundRect(mStalk,
                        mCenterX - stalkThickness, stalkTop,
                        mCenterX + stalkThickness, stalkBottom, roundRectRadius, 0f);

                // Draw a cutout.
                drawRoundRect(mStalkCutout,
                        mCenterX - stalkThickness, stalkTop,
                        mCenterX + stalkThickness, stalkBottom, roundRectRadius, 0.5f);
                break;
            }
            default: {
                // Shouldn't happen!
                // Make same as NONE
                bottom = mCenterY;
                break;
            }
        }

        float left = mCenterX - (thickness * pc);
        float right = mCenterX + (thickness * pc);

        switch (handShape) {
            case STRAIGHT: {
                // Draw a rectangle.
                drawRect(p, left, top, right, bottom, 0f);

                // Draw a cutout.
                drawRect(mHandCutout, left, top, right, bottom, 0.5f);
                break;
            }
            case DIAMOND: {
                // Draw a diamond.
                drawDiamond(p, left, top, right, bottom, 0f);

                // Draw a cutout.
                drawDiamond(mHandCutout, left, top, right, bottom, 0.5f);
                break;
            }
            case ROUNDED: {
                // Draw a round rect.
                drawRoundRect(p, left, top, right, bottom, roundRectRadius, 0f);

                // Draw a cutout.
                drawRoundRect(mHandCutout, left, top, right, bottom, roundRectRadius, 0.5f);
                break;
            }
            case UNKNOWN1: {
                // Dunno! Draw a round rect with double corner radius.
                drawRoundRect(p, left, top, right, bottom,
                        roundRectRadius * 2f, 0f);

                // Draw a cutout.
                drawRoundRect(mHandCutout, left, top, right, bottom,
                        roundRectRadius * 2f, 0.5f);
                break;
            }
        }

        // Remove the stalk cutout.
        // TODO: depending on if we're doing the hand cutout, stalk cutout or both, change this.
        mStalk.op(mStalkCutout, Path.Op.DIFFERENCE);
        // Add the stalk.
        p.op(mStalk, Path.Op.UNION);
        // Remove the cutout.
        p.op(mHandCutout, Path.Op.DIFFERENCE);
    }

    private void drawRect(
            Path path, float left, float top, float right, float bottom, float scale) {
        float x0 = (right - left) * 0.5f * scale;
        float y0 = (bottom - top) * 0.5f * scale;

        path.addRect(left + x0, top + y0, right - x0, bottom - y0,
                getDirection());
    }

    private void drawRoundRect(
            Path path, float left, float top, float right, float bottom, float cornerRadius,
            float scale) {
        float x0 = (right - left) * 0.5f * scale;
        float y0 = (bottom - top) * 0.5f * scale;
        float v = cornerRadius * (1f - scale);

        path.addRoundRect(left + x0, top + y0, right - x0, bottom - y0,
                v, v, getDirection());
    }

    private void drawDiamond(
            Path path, float left, float top, float right, float bottom, float scale) {
        // Add extra extension to the diamond top and bottom
        // because the diamond shape tapers to a point
        float diamondTop = top - (HUB_RADIUS_PERCENT * pc * 0.5f);
        float diamondBottom = bottom + (HUB_RADIUS_PERCENT * pc * 0.5f);
        float diamondMidpoint = (diamondTop * HOUR_MINUTE_HAND_MIDPOINT) +
                (diamondBottom * (1 - HOUR_MINUTE_HAND_MIDPOINT));

        // Scale factor. Ignored if scale == 0f
        float x0 = (right - left) * 0.5f * scale;
        float y1 = (diamondMidpoint - diamondTop) * scale;
        float y2 = (diamondBottom - diamondMidpoint) * scale;

        path.moveTo(mCenterX, diamondBottom - y2); // Extend past the hub
        if (getDirection() == Path.Direction.CW) {
            path.lineTo(left + x0, diamondMidpoint); // Left
            path.lineTo(mCenterX, diamondTop + y1); // Top
            path.lineTo(right - x0, diamondMidpoint); // Right
        } else {
            path.lineTo(right - x0, diamondMidpoint); // Right
            path.lineTo(mCenterX, diamondTop + y1); // Top
            path.lineTo(left + x0, diamondMidpoint); // Left
        }
        path.close();
    }
}
