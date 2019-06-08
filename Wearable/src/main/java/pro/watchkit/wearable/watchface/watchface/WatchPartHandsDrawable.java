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

import androidx.annotation.NonNull;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

import pro.watchkit.wearable.watchface.model.BytePackable;
import pro.watchkit.wearable.watchface.model.BytePackable.HandCutout;
import pro.watchkit.wearable.watchface.model.BytePackable.HandLength;
import pro.watchkit.wearable.watchface.model.BytePackable.HandShape;
import pro.watchkit.wearable.watchface.model.BytePackable.HandStalk;
import pro.watchkit.wearable.watchface.model.BytePackable.HandThickness;
import pro.watchkit.wearable.watchface.model.BytePackable.Style;

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
    private Path mStalkCutout = new Path();
    private Path mHandFullCutout = new Path();
    private Path mHandTopCutout = new Path();
    private Path mHandBottomCutout = new Path();

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

        mHandThicknessDimensions.put(BytePackable.HandShape.STRAIGHT,
                new EnumMap<HandThickness, Float>(HandThickness.class));
        mHandThicknessDimensions.put(BytePackable.HandShape.ROUNDED,
                new EnumMap<HandThickness, Float>(HandThickness.class));
        mHandThicknessDimensions.put(BytePackable.HandShape.DIAMOND,
                new EnumMap<HandThickness, Float>(HandThickness.class));
        mHandThicknessDimensions.put(BytePackable.HandShape.UNKNOWN1,
                new EnumMap<HandThickness, Float>(HandThickness.class));

        mHandThicknessDimensions.get(BytePackable.HandShape.STRAIGHT).put(BytePackable.HandThickness.THIN, f0);
        mHandThicknessDimensions.get(BytePackable.HandShape.STRAIGHT).put(BytePackable.HandThickness.REGULAR, f1);
        mHandThicknessDimensions.get(BytePackable.HandShape.STRAIGHT).put(BytePackable.HandThickness.THICK, f2);
        mHandThicknessDimensions.get(BytePackable.HandShape.STRAIGHT).put(BytePackable.HandThickness.X_THICK, f3);

        mHandThicknessDimensions.get(BytePackable.HandShape.ROUNDED).put(BytePackable.HandThickness.THIN, f0);
        mHandThicknessDimensions.get(BytePackable.HandShape.ROUNDED).put(BytePackable.HandThickness.REGULAR, f1);
        mHandThicknessDimensions.get(BytePackable.HandShape.ROUNDED).put(BytePackable.HandThickness.THICK, f2);
        mHandThicknessDimensions.get(BytePackable.HandShape.ROUNDED).put(BytePackable.HandThickness.X_THICK, f3);

        mHandThicknessDimensions.get(BytePackable.HandShape.DIAMOND).put(BytePackable.HandThickness.THIN, f0 * DIAMOND_HAND_ASPECT_RATIO);
        mHandThicknessDimensions.get(BytePackable.HandShape.DIAMOND).put(BytePackable.HandThickness.REGULAR, f1 * DIAMOND_HAND_ASPECT_RATIO);
        mHandThicknessDimensions.get(BytePackable.HandShape.DIAMOND).put(BytePackable.HandThickness.THICK, f2 * DIAMOND_HAND_ASPECT_RATIO);
        mHandThicknessDimensions.get(BytePackable.HandShape.DIAMOND).put(BytePackable.HandThickness.X_THICK, f3 * DIAMOND_HAND_ASPECT_RATIO);

        mHandThicknessDimensions.get(BytePackable.HandShape.UNKNOWN1).put(BytePackable.HandThickness.THIN, f0);
        mHandThicknessDimensions.get(BytePackable.HandShape.UNKNOWN1).put(BytePackable.HandThickness.REGULAR, f1);
        mHandThicknessDimensions.get(BytePackable.HandShape.UNKNOWN1).put(BytePackable.HandThickness.THICK, f2);
        mHandThicknessDimensions.get(BytePackable.HandShape.UNKNOWN1).put(BytePackable.HandThickness.X_THICK, f3);

        mHandLengthDimensions.put(BytePackable.HandShape.STRAIGHT,
                new EnumMap<HandLength, Float>(HandLength.class));
        mHandLengthDimensions.put(BytePackable.HandShape.ROUNDED,
                new EnumMap<HandLength, Float>(HandLength.class));
        mHandLengthDimensions.put(BytePackable.HandShape.DIAMOND,
                new EnumMap<HandLength, Float>(HandLength.class));
        mHandLengthDimensions.put(BytePackable.HandShape.UNKNOWN1,
                new EnumMap<HandLength, Float>(HandLength.class));

        mHandLengthDimensions.get(BytePackable.HandShape.STRAIGHT).put(BytePackable.HandLength.SHORT, 2f + f0);
        mHandLengthDimensions.get(BytePackable.HandShape.STRAIGHT).put(BytePackable.HandLength.MEDIUM, 2f + f1);
        mHandLengthDimensions.get(BytePackable.HandShape.STRAIGHT).put(BytePackable.HandLength.LONG, 2f + f2);
        mHandLengthDimensions.get(BytePackable.HandShape.STRAIGHT).put(BytePackable.HandLength.X_LONG, 2f + f3);

        mHandLengthDimensions.get(BytePackable.HandShape.ROUNDED).put(BytePackable.HandLength.SHORT, 2f + f0);
        mHandLengthDimensions.get(BytePackable.HandShape.ROUNDED).put(BytePackable.HandLength.MEDIUM, 2f + f1);
        mHandLengthDimensions.get(BytePackable.HandShape.ROUNDED).put(BytePackable.HandLength.LONG, 2f + f2);
        mHandLengthDimensions.get(BytePackable.HandShape.ROUNDED).put(BytePackable.HandLength.X_LONG, 2f + f3);

        mHandLengthDimensions.get(BytePackable.HandShape.DIAMOND).put(BytePackable.HandLength.SHORT, 2f + f0);
        mHandLengthDimensions.get(BytePackable.HandShape.DIAMOND).put(BytePackable.HandLength.MEDIUM, 2f + f1);
        mHandLengthDimensions.get(BytePackable.HandShape.DIAMOND).put(BytePackable.HandLength.LONG, 2f + f2);
        mHandLengthDimensions.get(BytePackable.HandShape.DIAMOND).put(BytePackable.HandLength.X_LONG, 2f + f3);

        mHandLengthDimensions.get(BytePackable.HandShape.UNKNOWN1).put(BytePackable.HandLength.SHORT, 2f + f0);
        mHandLengthDimensions.get(BytePackable.HandShape.UNKNOWN1).put(BytePackable.HandLength.MEDIUM, 2f + f1);
        mHandLengthDimensions.get(BytePackable.HandShape.UNKNOWN1).put(BytePackable.HandLength.LONG, 2f + f2);
        mHandLengthDimensions.get(BytePackable.HandShape.UNKNOWN1).put(BytePackable.HandLength.X_LONG, 2f + f3);
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

    abstract HandCutout getHandCutout();

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
        HandCutout handCutout = getHandCutout();
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
                mHandThicknessDimensions.get(BytePackable.HandShape.STRAIGHT).get(handThickness) * pc * 0.5f;

        mStalk.reset();
        mStalkCutout.reset();
        mHandFullCutout.reset();
        mHandTopCutout.reset();
        mHandBottomCutout.reset();

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
                drawRoundRectInset(mStalkCutout,
                        mCenterX - stalkThickness, stalkTop,
                        mCenterX + stalkThickness, stalkBottom, roundRectRadius, 1.0f, 1.0f);
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
                drawRoundRectInset(mStalkCutout,
                        mCenterX - stalkThickness, stalkTop,
                        mCenterX + stalkThickness, stalkBottom, roundRectRadius, 1.0f, 1.0f);
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
                drawRectInset(mHandFullCutout, left, top, right, bottom, 1.0f, 1.0f);
                drawRectInset(mHandTopCutout, left, top, right, bottom, 1.0f, 0.5f);
                drawRectInset(mHandBottomCutout, left, top, right, bottom, 0.5f, 1.0f);
                break;
            }
            case DIAMOND: {
                // Draw a diamond.
                drawDiamond(p, left, top, right, bottom, 0f);

                // Golden ratio scale = 1 / sqrt(golden ratio)
                float scale = 1f - (float) (Math.sqrt(1d / 1.61803398875d / 1.61803398875d));

                // Draw a cutout.
                drawDiamond(mHandFullCutout, left, top, right, bottom, scale);
                drawDiamond(mHandTopCutout, left, top, right, bottom, scale, true, false);
                drawDiamond(mHandBottomCutout, left, top, right, bottom, scale, false, true);
                break;
            }
            case ROUNDED: {
                // Draw a round rect.
                drawRoundRect(p, left, top, right, bottom, roundRectRadius, 0f);

                // Draw a cutout.
                drawRoundRectInset(mHandFullCutout, left, top, right, bottom, roundRectRadius, 1.0f, 1.0f);
                drawRoundRectInset(mHandTopCutout, left, top, right, bottom, roundRectRadius, 1.0f, 0.5f);
                drawRoundRectInset(mHandBottomCutout, left, top, right, bottom, roundRectRadius, 0.5f, 1.0f);
                break;
            }
            case UNKNOWN1: {
                // Dunno! Draw a round rect with double corner radius.
                drawRoundRect(p, left, top, right, bottom,
                        roundRectRadius * 2f, 0f);

                // Draw a cutout.
                drawRoundRectInset(mHandFullCutout, left, top, right, bottom,
                        roundRectRadius * 2f, 1.0f, 1.0f);
                drawRoundRectInset(mHandTopCutout, left, top, right, bottom,
                        roundRectRadius * 2f, 1.0f, 0.5f);
                drawRoundRectInset(mHandBottomCutout, left, top, right, bottom,
                        roundRectRadius * 2f, 0.5f, 1.0f);
                break;
            }
        }

        if (handStalk == BytePackable.HandStalk.SHORT || handStalk == BytePackable.HandStalk.MEDIUM) {
            switch (handCutout) {
                case NONE: {
                    p.op(mStalk, Path.Op.UNION); // Add the stalk to the hand.
                    break;
                }
                case HAND: {
                    p.op(mStalk, Path.Op.UNION); // Add the stalk to the hand.
                    p.op(mHandFullCutout, Path.Op.DIFFERENCE); // Remove the hand cutout.
                    break;
                }
                case STALK: {
                    mStalk.op(mStalkCutout, Path.Op.DIFFERENCE); // Remove the stalk cutout.
                    p.op(mStalk, Path.Op.UNION); // Add the stalk to the hand.
                    break;
                }
                case HAND_STALK: {
                    p.op(mStalk, Path.Op.UNION); // Add the stalk to the hand.
                    p.op(mStalkCutout, Path.Op.DIFFERENCE); // Remove the stalk cutout.
                    p.op(mHandFullCutout, Path.Op.DIFFERENCE); // Remove the hand cutout.
                    break;
                }
            }
        } else if (handStalk == BytePackable.HandStalk.NONE || handStalk == BytePackable.HandStalk.NEGATIVE) {
            switch (handCutout) {
                case NONE: {
                    break;
                }
                case HAND: {
                    p.op(mHandTopCutout, Path.Op.DIFFERENCE); // Remove the top hand cutout.
                    break;
                }
                case STALK: {
                    p.op(mHandBottomCutout, Path.Op.DIFFERENCE); // Remove the bottom hand cutout.
                    break;
                }
                case HAND_STALK: {
                    p.op(mHandFullCutout, Path.Op.DIFFERENCE); // Remove the full hand cutout.
                    break;
                }
            }
        }
    }

    /**
     * Better implementation of drawRect. Draws a rectangle in the specified bounds (or extending
     * past them). Notionally the area of this rectangle is k of the area of the bounds. Plus it
     * has a property that the inset is equal on all sides.
     * <p>
     * Where "k" is the golden ratio 2nd term ≈ 0.38196601125...
     * <p>
     * For example, pass in bounds of 6x4 (24 area) and it will calculate a rectangle of 4x2 (12
     * area) with an inset of 1 on all sides.
     * <p>
     * If offsetTop or offsetBottom is 1.0, then it uses the calculations as specified. If it's
     * greater than 1.0, the top or bottom is moved outwards. If less than 1.0, inwards.
     * @param path Path to draw into
     * @param left Left boundary
     * @param top Top boundary
     * @param right Right boundary
     * @param bottom Bottom boundary
     * @param offsetTop Factor to move the top border, 1.0f is no change
     * @param offsetBottom Factor to move the bottom border, 1.0f is no change.
     */
    private void drawRectInset(
            Path path, float left, float top, float right, float bottom,
            float offsetTop, float offsetBottom) {
        // Inset calculation:
        //   k = golden ratio 2nd term
        //     = (3 − √5) / 2
        //     ≈ 0.38196601125...
        //  xy = ((x - n)(y - n)) / k
        //   n = (x + y − √( x² + y² + (4 − 2√5)xy)) / 2
        // And then...
        //   offset = n / 2
        float x = (right - left);
        float y = (bottom - top);
        float n = (x + y - (float) Math.sqrt(x * x + y * y + (4f - 2f * Math.sqrt(5d)) * x * y)) * 0.5f;
        float offset = n / 2f;

        float newTop = bottom - (y * offsetTop);
        float newBottom = top + (y * offsetBottom);

        path.addRect(left + offset, newTop + offset,
                right - offset, newBottom - offset, getDirection());
    }

    /**
     * Better implementation of drawRoundRect. Draws a round rectangle in the specified bounds (or
     * extending past them). Notionally the area of this round rectangle is k of the area of the
     * bounds. Plus it has a property that the inset is equal on all sides.
     * <p>
     * Where "k" is the golden ratio 2nd term ≈ 0.38196601125...
     * <p>
     * For example, pass in bounds of 6x4 (24 area) and it will calculate a rectangle of 4x2 (12
     * area) with an inset of 1 on all sides.
     * <p>
     * If offsetTop or offsetBottom is 1.0, then it uses the calculations as specified. If it's
     * greater than 1.0, the top or bottom is moved outwards. If less than 1.0, inwards.
     *
     * @param path         Path to draw into
     * @param left         Left boundary
     * @param top          Top boundary
     * @param right        Right boundary
     * @param bottom       Bottom boundary
     * @param cornerRadius Corner radius of round rectangle
     * @param offsetTop    Factor to move the top border, 1.0f is no change
     * @param offsetBottom Factor to move the bottom border, 1.0f is no change.
     */
    private void drawRoundRectInset(
            Path path, float left, float top, float right, float bottom, float cornerRadius,
            float offsetTop, float offsetBottom) {
        // Inset calculation:
        //   k = golden ratio 2nd term
        //     = (3 − √5) / 2
        //     ≈ 0.38196601125...
        //  xy = ((x - n)(y - n)) / k
        //   n = (x + y − √( x² + y² + (4 − 2√5)xy)) / 2
        // And then...
        //   offset = n / 2
        float x = (right - left);
        float y = (bottom - top);
        float n = (x + y - (float) Math.sqrt(x * x + y * y + (4f - 2f * Math.sqrt(5d)) * x * y)) * 0.5f;
        float offset = n / 2f;

        float newTop = bottom - (y * offsetTop);
        float newBottom = top + (y * offsetBottom);

        float v = cornerRadius - n;
        v = v < 0 ? 0 : v; // Cap minimum at 0.

        path.addRoundRect(left + offset, newTop + offset,
                right - offset, newBottom - offset, v, v, getDirection());
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
            Path path, float left, float top, float right, float bottom, float scale,
            boolean drawTopHalf, boolean drawBottomHalf) {
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

        if (getDirection() == Path.Direction.CW) {
            path.moveTo(left + x0, diamondMidpoint); // Left
            if (drawTopHalf) {
                path.lineTo(mCenterX, diamondTop + y1); // Top
            }
            path.lineTo(right - x0, diamondMidpoint); // Right
            if (drawBottomHalf) {
                path.lineTo(mCenterX, diamondBottom - y2); // Bottom: extend past the hub
            }
        } else {
            path.moveTo(right - x0, diamondMidpoint); // Right
            if (drawTopHalf) {
                path.lineTo(mCenterX, diamondTop + y1); // Top
            }
            path.lineTo(left + x0, diamondMidpoint); // Left
            if (drawBottomHalf) {
                path.lineTo(mCenterX, diamondBottom - y2); // Bottom: extend past the hub
            }
        }
        path.close();
    }

    private void drawDiamond(
            Path path, float left, float top, float right, float bottom, float scale) {
        drawDiamond(path, left, top, right, bottom, scale, true, true);
    }
}
