/*
 * Copyright (C) 2018-2021 Terence Tan
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
import android.graphics.Rect;

import androidx.annotation.NonNull;

import java.util.Objects;

import pro.watchkit.wearable.watchface.model.BytePackable;
import pro.watchkit.wearable.watchface.model.BytePackable.HandCutoutShape;
import pro.watchkit.wearable.watchface.model.BytePackable.HandLength;
import pro.watchkit.wearable.watchface.model.BytePackable.HandShape;
import pro.watchkit.wearable.watchface.model.BytePackable.HandStalk;
import pro.watchkit.wearable.watchface.model.BytePackable.HandThickness;
import pro.watchkit.wearable.watchface.model.BytePackable.Material;
import pro.watchkit.wearable.watchface.model.WatchFaceState;

abstract class WatchPartHandsDrawable extends WatchPartDrawable {
    private static final float HUB_RADIUS_PERCENT = 3f; // 3% // 1.5f; // 1.5%
    private static final float HOUR_MINUTE_HAND_MIDPOINT = 0.333f;
    private static final float ROUND_RECT_RADIUS_PERCENT = 1.5f;

    @NonNull
    private final Path mHub = new Path();

    @NonNull
    private final Path mHandActivePath = new Path();
    @NonNull
    private final Path mHandTwoToneCutoutPath = new Path();
    @NonNull
    private final Path mHandAmbientPath = new Path();
    private int mPreviousSerial = -1;
    @NonNull
    private final Path mStalk = new Path();
    @NonNull
    private final Path mStalkCutout = new Path();
    @NonNull
    private final Path mHandFullCutout = new Path();
    @NonNull
    private final Path mHandTipCutout = new Path();
    @NonNull
    private final Path mHandTopCutout = new Path();
    @NonNull
    private final Path mHandBottomCutout = new Path();
    @NonNull
    private final Matrix m2 = new Matrix();

    @Override
    public void draw2(@NonNull Canvas canvas) {
        if (mWatchFaceState.isDeveloperMode() && mWatchFaceState.isHideHands()) {
            // If we set developer mode "hide hands", then just return!
            return;
        }

        // Reset the exclusion path. We ignore it for this layer up!
        resetExclusionPath();

        Paint paint = mWatchFaceState.getPaintBox().getPaintFromPreset(getHandMaterial());
        Path path = getHandPath();
        drawPath(canvas, path, paint);

        // Quick-and-dirty draw of the two-tone cutout, if we're not ambient.
        if (!mWatchFaceState.isAmbient() && isTwoToneCutout()) {
            m2.reset();
            m2.postRotate(getDegreesRotation(), mCenterX, mCenterY);
            // Here we treat "mHandBottomCutout" as a cheap throwaway path.
            mHandTwoToneCutoutPath.transform(m2, mHandBottomCutout);
            canvas.drawPath(mHandBottomCutout,
                    mWatchFaceState.getPaintBox().getPaintFromPreset(getHandCutoutMaterial()));
        }
    }

    @Override
    boolean enablePathShadows() {
        return mWatchFaceState.isDrawShadows();
    }

    /**
     * Does this hand have a cutout (two-tone or punched out)?
     * Only if if's not the hand material (in which case, there's no cutout).
     *
     * @return Whether we have a cutout
     */
    private boolean isCutout() {
        return !getHandCutoutMaterial().equals(getHandMaterial());
    }

    /**
     * Does this hand have a two-tone cutout?
     * Only if if's not the hand material (in which case, there's no cutout)
     * and if it's not the background material (in which case, don't need to go two-tone).
     *
     * @return Whether we have a two-tone cutout
     */
    private boolean isTwoToneCutout() {
        return isCutout() &&
                !getHandCutoutMaterial().equals(mWatchFaceState.getBackgroundMaterial());
    }

    @Override
    protected void onBoundsChange(@NonNull Rect bounds) {
        super.onBoundsChange(bounds);
        mHub.reset();
        mHub.addCircle(mCenterX, mCenterY, HUB_RADIUS_PERCENT * pc, getDirection());

        mPreviousSerial = -1;
    }

    @NonNull
    Path getHub() {
        return mHub;
    }

    abstract HandShape getHandShape();

    abstract HandLength getHandLength();

    abstract HandThickness getHandThickness();

    abstract HandStalk getHandStalk();

    @NonNull
    abstract HandCutoutShape getHandCutout();

    abstract Material getHandMaterial();

    @NonNull
    abstract Material getHandCutoutMaterial();

    abstract void punchHub(Path active, Path ambient);

    boolean isMinuteHand() {
        return false;
    }

    boolean isSecondHand() {
        return false;
    }

    @NonNull
    private Path getHandPath() {
        // Regenerate "mHandActivePath" and "mHandAmbientPath" if we need to.
        int currentSerial = Objects.hash(mWatchFaceState);

        if (mPreviousSerial != currentSerial) {
            mPreviousSerial = currentSerial;
            // Cache miss. Regenerate the hand.
            mHandActivePath.reset();
            regenerateActivePath();
            mHandAmbientPath.reset();
            mHandAmbientPath.addPath(mHandActivePath);
            punchHub(mHandActivePath, mHandAmbientPath);
            // Regenerate our bezels too.
            regenerateBezels();
        }

        if (mWatchFaceState.isAmbient())
            return mHandAmbientPath;
        else
            return mHandActivePath;
    }

    private void regenerateActivePath() {
        HandShape handShape = getHandShape();
        HandLength handLength = getHandLength();
        HandThickness handThickness = getHandThickness();
        HandStalk handStalk = getHandStalk();
        HandCutoutShape handCutoutShape = getHandCutout();

        float thickness = WatchFaceState.getHandThickness(handShape, handThickness);
        float length = WatchFaceState.getHandLength(handLength);
        float top, bottom;
        if (isMinuteHand() || isSecondHand()) {
            length = length * 12.5f * pc; // 12.5%
            // Min multiplier is 2.7 (for hand length short) so that'd be 33.75%
            // Max multiplier is 4 (for hand length x-long) so that'd be 50%.
        } else {
            length = length * 12.5f * pc * 0.61803398875f; // 12.5% - golden ratio
        }

        if (isSecondHand()) {
            // Second hands are automatically thinner.
            thickness /= 3;
        }

        top = mCenterY - length;

        final float roundRectRadius = ROUND_RECT_RADIUS_PERCENT * pc;
        // We add a bit extra to the stalk top so it overlaps with the hand,
        // in order that the union works OK without gaps.
        // For the stalk thickness, use the width of the Straight hand shape, but only half.
        final float stalkThickness = pc * 0.5f *
                WatchFaceState.getHandThickness(BytePackable.HandShape.STRAIGHT, handThickness);

        mStalk.reset();
        mStalkCutout.reset();
        mHandFullCutout.reset();
        mHandTipCutout.reset();
        mHandTopCutout.reset();
        mHandBottomCutout.reset();
        mHandTwoToneCutoutPath.reset();

        // Golden ratio scale = -2nd golden ratio term ~= 0.38196601125
        final float cutoutScale = (float) (1d / 1.61803398875d / 1.61803398875d);

        switch (handStalk) {
            case NEGATIVE: {
                bottom = mCenterY + HUB_RADIUS_PERCENT * pc * 2f;
                break;
            }
            default:
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
                        mCenterX + stalkThickness, stalkBottom, roundRectRadius, 1f);

                // Draw a cutout.
                drawRoundRect(mStalkCutout,
                        mCenterX - stalkThickness, stalkTop,
                        mCenterX + stalkThickness, stalkBottom, roundRectRadius, cutoutScale);
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
                        mCenterX + stalkThickness, stalkBottom, roundRectRadius, 1f);

                // Draw a cutout.
                drawRoundRect(mStalkCutout,
                        mCenterX - stalkThickness, stalkTop,
                        mCenterX + stalkThickness, stalkBottom, roundRectRadius, cutoutScale);
                break;
            }
        }

        float left = mCenterX - (thickness * pc);
        float right = mCenterX + (thickness * pc);

        switch (handShape) {
            case STRAIGHT: {
                // Draw a rectangle.
                drawRect(mHandActivePath, left, top, right, bottom, 1f);

                // Draw a cutout.
                drawRect(mHandFullCutout, left, top, right, bottom, cutoutScale);
                drawRect(mHandTipCutout, left, top, right, bottom, cutoutScale, 0f, 0.75f);
                drawRect(mHandTopCutout, left, top, right, bottom, cutoutScale, 0f, 0.5f);
                drawRect(mHandBottomCutout, left, top, right, bottom, cutoutScale, 0.5f, 0f);
                break;
            }
            case ROUNDED: {
                // Draw a round rect with double corner radius.
                drawRoundRect(mHandActivePath, left, top, right, bottom,
                        roundRectRadius * 2f, 1f);

                // Draw a cutout.
                drawRoundRect(mHandFullCutout, left, top, right, bottom,
                        roundRectRadius * 2f, cutoutScale);
                drawRoundRect(mHandTipCutout, left, top, right, bottom,
                        roundRectRadius * 2f, cutoutScale, 0f, 0.75f);
                drawRoundRect(mHandTopCutout, left, top, right, bottom,
                        roundRectRadius * 2f, cutoutScale, 0f, 0.5f);
                drawRoundRect(mHandBottomCutout, left, top, right, bottom,
                        roundRectRadius * 2f, cutoutScale, 0.5f, 0f);
                break;
            }
            case DIAMOND: {
                // Add extra extension to the diamond top and bottom
                // because the diamond shape tapers to a point
                final float diamondTop = top - (HUB_RADIUS_PERCENT * pc * 0.5f);
                final float diamondBottom = bottom + (HUB_RADIUS_PERCENT * pc * 0.5f);

                // Draw a diamond.
                drawDiamond(mHandActivePath, left, diamondTop, right, diamondBottom, 1f,
                        HOUR_MINUTE_HAND_MIDPOINT);

                // Draw a cutout.
                drawDiamond(mHandFullCutout, left, diamondTop, right, diamondBottom, cutoutScale,
                        HOUR_MINUTE_HAND_MIDPOINT);
                drawDiamond(mHandTipCutout, left, diamondTop, right, diamondBottom, cutoutScale,
                        HOUR_MINUTE_HAND_MIDPOINT, 0f, 0.75f);
                drawDiamond(mHandTopCutout, left, diamondTop, right, diamondBottom, cutoutScale,
                        HOUR_MINUTE_HAND_MIDPOINT, 0f, 0.5f);
                drawDiamond(mHandBottomCutout, left, diamondTop, right, diamondBottom, cutoutScale,
                        HOUR_MINUTE_HAND_MIDPOINT, 0.5f, 0f);
                break;
            }
            case TRIANGLE: {
                // Add extra extension to the triangle top and bottom
                // because the triangle shape tapers to a point
                final float triangleTop = top - (HUB_RADIUS_PERCENT * pc * 0.5f);
                final float triangleBottom = bottom + (HUB_RADIUS_PERCENT * pc * 0.5f);

                // Draw a triangle.
                drawTriangle(mHandActivePath, left, triangleTop, right, triangleBottom, 1f);

                // Draw a cutout.
                drawTriangle(mHandFullCutout, left, triangleTop, right, triangleBottom, cutoutScale);
                drawTriangle(mHandTipCutout, left, triangleTop, right, triangleBottom, cutoutScale,
                        0f, 0.75f);
                drawTriangle(mHandTopCutout, left, triangleTop, right, triangleBottom, cutoutScale,
                        0f, 0.5f);
                drawTriangle(mHandBottomCutout, left, triangleTop, right, triangleBottom, cutoutScale,
                        0.5f, 0f);
                break;
            }
        }

        if (handStalk == HandStalk.SHORT || handStalk == HandStalk.MEDIUM) {
            if (!isCutout()) {
                mHandActivePath.op(mStalk, Path.Op.UNION); // Add the stalk to the hand.
            } else {
                switch (handCutoutShape) {
                    case TIP: {
                        mHandActivePath.op(mStalk, Path.Op.UNION); // Add the stalk to the hand.
                        if (isTwoToneCutout()) {
                            mHandTwoToneCutoutPath.op(mHandTipCutout, Path.Op.UNION); // Add to two-tone
                        } else {
                            mHandActivePath.op(mHandTipCutout, Path.Op.DIFFERENCE); // Remove the hand cutout.
                        }
                        break;
                    }
                    case TIP_STALK: {
                        if (isTwoToneCutout()) {
                            mHandTwoToneCutoutPath.op(mStalkCutout, Path.Op.UNION); // Add to two-tone
                            mHandTwoToneCutoutPath.op(mHandActivePath, Path.Op.DIFFERENCE); // Remove hand from two-tone
                        } else {
                            mStalk.op(mStalkCutout, Path.Op.DIFFERENCE); // Remove the stalk cutout.
                        }
                        mHandActivePath.op(mStalk, Path.Op.UNION); // Add the stalk to the hand.
                        if (isTwoToneCutout()) {
                            mHandTwoToneCutoutPath.op(mHandTipCutout, Path.Op.UNION); // Add to two-tone
                        } else {
                            mHandActivePath.op(mHandTipCutout, Path.Op.DIFFERENCE); // Remove the hand cutout.
                        }
                        break;
                    }
                    case HAND: {
                        mHandActivePath.op(mStalk, Path.Op.UNION); // Add the stalk to the hand.
                        if (isTwoToneCutout()) {
                            mHandTwoToneCutoutPath.op(mHandFullCutout, Path.Op.UNION); // Add to two-tone
                        } else {
                            mHandActivePath.op(mHandFullCutout, Path.Op.DIFFERENCE); // Remove the hand cutout.
                        }
                        break;
                    }
                    case STALK: {
                        if (isTwoToneCutout()) {
                            mHandTwoToneCutoutPath.op(mStalkCutout, Path.Op.UNION); // Add to two-tone
                            mHandTwoToneCutoutPath.op(mHandActivePath, Path.Op.DIFFERENCE); // Remove hand from two-tone
                        } else {
                            mStalk.op(mStalkCutout, Path.Op.DIFFERENCE); // Remove the stalk cutout.
                        }
                        mHandActivePath.op(mStalk, Path.Op.UNION); // Add the stalk to the hand.
                        break;
                    }
                    case HAND_STALK: {
                        mHandActivePath.op(mStalk, Path.Op.UNION); // Add the stalk to the hand.
                        if (isTwoToneCutout()) {
                            mHandTwoToneCutoutPath.op(mStalkCutout, Path.Op.UNION); // Add to two-tone
                            mHandTwoToneCutoutPath.op(mHandFullCutout, Path.Op.UNION); // Add to two-tone
                        } else {
                            mHandActivePath.op(mStalkCutout, Path.Op.DIFFERENCE); // Remove the stalk cutout.
                            mHandActivePath.op(mHandFullCutout, Path.Op.DIFFERENCE); // Remove the hand cutout.
                        }
                        break;
                    }
                }
            }
        } else if ((handStalk == HandStalk.NONE || handStalk == HandStalk.NEGATIVE) && isCutout()) {
            switch (handCutoutShape) {
                case TIP: {
                    if (isTwoToneCutout()) {
                        mHandTwoToneCutoutPath.op(mHandTipCutout, Path.Op.UNION); // Add to two-tone
                    } else {
                        mHandActivePath.op(mHandTipCutout, Path.Op.DIFFERENCE); // Remove the top hand cutout.
                    }
                    break;
                }
                case TIP_STALK: {
                    if (isTwoToneCutout()) {
                        mHandTwoToneCutoutPath.op(mHandTipCutout, Path.Op.UNION); // Add to two-tone
                        mHandTwoToneCutoutPath.op(mHandBottomCutout, Path.Op.UNION); // Add to two-tone
                    } else {
                        mHandActivePath.op(mHandTipCutout, Path.Op.DIFFERENCE); // Remove the top hand cutout.
                        mHandActivePath.op(mHandBottomCutout, Path.Op.DIFFERENCE); // Remove the bottom hand cutout.
                    }
                    break;
                }
                case HAND: {
                    if (isTwoToneCutout()) {
                        mHandTwoToneCutoutPath.op(mHandTopCutout, Path.Op.UNION); // Add to two-tone
                    } else {
                        mHandActivePath.op(mHandTopCutout, Path.Op.DIFFERENCE); // Remove the top hand cutout.
                    }
                    break;
                }
                case STALK: {
                    if (isTwoToneCutout()) {
                        mHandTwoToneCutoutPath.op(mHandBottomCutout, Path.Op.UNION); // Add to two-tone
                    } else {
                        mHandActivePath.op(mHandBottomCutout, Path.Op.DIFFERENCE); // Remove the bottom hand cutout.
                    }
                    break;
                }
                case HAND_STALK: {
                    if (isTwoToneCutout()) {
                        mHandTwoToneCutoutPath.op(mHandFullCutout, Path.Op.UNION); // Add to two-tone
                    } else {
                        mHandActivePath.op(mHandFullCutout, Path.Op.DIFFERENCE); // Remove the full hand cutout.
                    }
                    break;
                }
            }
        }
    }
}
