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

package pro.watchkit.wearable.watchface.model;

import java.util.Objects;

final class WatchFacePreset extends BytePackable {
    boolean mMinuteHandOverride, mSecondHandOverride;
    HandShape mHourHandShape, mMinuteHandShape, mSecondHandShape;
    HandLength mHourHandLength, mMinuteHandLength, mSecondHandLength;
    HandThickness mHourHandThickness, mMinuteHandThickness, mSecondHandThickness;
    HandStalk mHourHandStalk, mMinuteHandStalk;
    Material mHourHandMaterial, mMinuteHandMaterial, mSecondHandMaterial;
    HandCutoutCombination mHourHandCutoutCombination, mMinuteHandCutoutCombination;
    TicksDisplay mTicksDisplay;
    boolean mTwelveTickOverride, mSixtyTickOverride;
    TickShape mFourTickShape, mTwelveTickShape, mSixtyTickShape;
    TickSize mFourTickSize, mTwelveTickSize, mSixtyTickSize;
    Material mFourTickMaterial, mTwelveTickMaterial, mSixtyTickMaterial, mDigitMaterial, mTickBackgroundMaterial;
    TickMargin mTickMargin;
    DigitDisplay mDigitDisplay;
    DigitSize mDigitSize;
    DigitRotation mDigitRotation;
    DigitFormat mDigitFormat;
    int mFillSixBitColor, mAccentSixBitColor, mHighlightSixBitColor, mBaseSixBitColor;
    MaterialGradient mFillHighlightMaterialGradient, mAccentFillMaterialGradient,
            mAccentHighlightMaterialGradient, mBaseAccentMaterialGradient;
    MaterialTexture mFillHighlightMaterialTexture, mAccentFillMaterialTexture,
            mAccentHighlightMaterialTexture, mBaseAccentMaterialTexture;

    @Override
    public int hashCode() {
        return Objects.hash(
                super.hashCode(),
                mMinuteHandOverride, mSecondHandOverride,
                mHourHandShape, mMinuteHandShape, mSecondHandShape,
                mHourHandLength, mMinuteHandLength, mSecondHandLength,
                mHourHandThickness, mMinuteHandThickness, mSecondHandThickness,
                mHourHandStalk, mMinuteHandStalk,
                mHourHandMaterial, mMinuteHandMaterial, mSecondHandMaterial,
                mHourHandCutoutCombination, mMinuteHandCutoutCombination,
                mTicksDisplay,
                mTwelveTickOverride, mSixtyTickOverride,
                mFourTickShape, mTwelveTickShape, mSixtyTickShape,
                mFourTickSize, mTwelveTickSize, mSixtyTickSize,
                mTickMargin,
                mFourTickMaterial, mTwelveTickMaterial, mSixtyTickMaterial, mDigitMaterial, mTickBackgroundMaterial,
                mDigitDisplay,
                mDigitSize,
                mDigitRotation,
                mDigitFormat,
                mFillSixBitColor, mAccentSixBitColor, mHighlightSixBitColor, mBaseSixBitColor,
                mFillHighlightMaterialGradient, mAccentFillMaterialGradient,
                mAccentHighlightMaterialGradient, mBaseAccentMaterialGradient,
                mFillHighlightMaterialTexture, mAccentFillMaterialTexture,
                mAccentHighlightMaterialTexture, mBaseAccentMaterialTexture);
    }

    @Override
    void pack() {
        // Version 0
        mBytePacker.rewind();

        // Pack version 0
        mBytePacker.put(3, 0);

        mHourHandShape.pack(mBytePacker);
        mHourHandLength.pack(mBytePacker);
        mHourHandThickness.pack(mBytePacker);
        mHourHandStalk.pack(mBytePacker);
        mHourHandCutoutCombination.pack(mBytePacker);
        mHourHandMaterial.pack(mBytePacker);

        mDigitMaterial.pack(mBytePacker);
        mDigitDisplay.pack(mBytePacker);
        mDigitSize.pack(mBytePacker);
        mDigitRotation.pack(mBytePacker);
        mDigitFormat.pack(mBytePacker);

        mBytePacker.put(mMinuteHandOverride);
        mMinuteHandShape.pack(mBytePacker);
        mMinuteHandLength.pack(mBytePacker);
        mMinuteHandThickness.pack(mBytePacker);
        mMinuteHandStalk.pack(mBytePacker);
        mMinuteHandCutoutCombination.pack(mBytePacker);
        mMinuteHandMaterial.pack(mBytePacker);

        mBytePacker.put(mSecondHandOverride);
        // mSecondHandShape.pack(mBytePacker); // Don't worry about this.
        mSecondHandLength.pack(mBytePacker);
        mSecondHandThickness.pack(mBytePacker);
        mSecondHandMaterial.pack(mBytePacker);

        mTicksDisplay.pack(mBytePacker);
        mTickMargin.pack(mBytePacker);
        mTickBackgroundMaterial.pack(mBytePacker);

        mFourTickShape.pack(mBytePacker);
        mFourTickSize.pack(mBytePacker);
//        mFourTickThickness.pack(mBytePacker);
        mFourTickMaterial.pack(mBytePacker);

        mBytePacker.put(mTwelveTickOverride);
        mTwelveTickShape.pack(mBytePacker);
        mTwelveTickSize.pack(mBytePacker);
//        mTwelveTickThickness.pack(mBytePacker);
        mTwelveTickMaterial.pack(mBytePacker);

        mBytePacker.put(mSixtyTickOverride);
        mSixtyTickShape.pack(mBytePacker);
        mSixtyTickSize.pack(mBytePacker);
//        mSixtyTickThickness.pack(mBytePacker);
        mSixtyTickMaterial.pack(mBytePacker);

        mFillHighlightMaterialGradient.pack(mBytePacker);
        mFillHighlightMaterialTexture.pack(mBytePacker);
        mAccentFillMaterialGradient.pack(mBytePacker);
        mAccentFillMaterialTexture.pack(mBytePacker);
        mAccentHighlightMaterialGradient.pack(mBytePacker);
        mAccentHighlightMaterialTexture.pack(mBytePacker);
        mBaseAccentMaterialGradient.pack(mBytePacker);
        mBaseAccentMaterialTexture.pack(mBytePacker);

        mBytePacker.putSixBitColor(mFillSixBitColor);
        mBytePacker.putSixBitColor(mHighlightSixBitColor);
        mBytePacker.putSixBitColor(mAccentSixBitColor);
        mBytePacker.putSixBitColor(mBaseSixBitColor);

        mBytePacker.finish();
    }

    @Override
    void unpack() {
        mBytePacker.rewind();

        int version = mBytePacker.get(3);
        switch (version) {
            case 0: {
                mHourHandShape = HandShape.unpack(mBytePacker);
                mHourHandLength = HandLength.unpack(mBytePacker);
                mHourHandThickness = HandThickness.unpack(mBytePacker);
                mHourHandStalk = HandStalk.unpack(mBytePacker);
                mHourHandCutoutCombination = HandCutoutCombination.unpack(mBytePacker);
                mHourHandMaterial = Material.unpack(mBytePacker);

                mDigitMaterial = Material.unpack(mBytePacker);
                mDigitDisplay = DigitDisplay.unpack(mBytePacker);
                mDigitSize = DigitSize.unpack(mBytePacker);
                mDigitRotation = DigitRotation.unpack(mBytePacker);
                mDigitFormat = DigitFormat.unpack(mBytePacker);

                mMinuteHandOverride = mBytePacker.getBoolean();
                mMinuteHandShape = HandShape.unpack(mBytePacker);
                mMinuteHandLength = HandLength.unpack(mBytePacker);
                mMinuteHandThickness = HandThickness.unpack(mBytePacker);
                mMinuteHandStalk = HandStalk.unpack(mBytePacker);
                mMinuteHandCutoutCombination = HandCutoutCombination.unpack(mBytePacker);
                mMinuteHandMaterial = Material.unpack(mBytePacker);

                mSecondHandOverride = mBytePacker.getBoolean();
                mSecondHandShape = HandShape.STRAIGHT; // Hard-coded!
                mSecondHandLength = HandLength.unpack(mBytePacker);
                mSecondHandThickness = HandThickness.unpack(mBytePacker);
                mSecondHandMaterial = Material.unpack(mBytePacker);

                mTicksDisplay = TicksDisplay.unpack(mBytePacker);
                mTickMargin = TickMargin.unpack(mBytePacker);
                mTickBackgroundMaterial = Material.unpack(mBytePacker);

                mFourTickShape = TickShape.unpack(mBytePacker);
                mFourTickSize = TickSize.unpack(mBytePacker);
                mFourTickMaterial = Material.unpack(mBytePacker);

                mTwelveTickOverride = mBytePacker.getBoolean();
                mTwelveTickShape = TickShape.unpack(mBytePacker);
                mTwelveTickSize = TickSize.unpack(mBytePacker);
                mTwelveTickMaterial = Material.unpack(mBytePacker);

                mSixtyTickOverride = mBytePacker.getBoolean();
                mSixtyTickShape = TickShape.unpack(mBytePacker);
                mSixtyTickSize = TickSize.unpack(mBytePacker);
                mSixtyTickMaterial = Material.unpack(mBytePacker);

                mFillHighlightMaterialGradient = MaterialGradient.unpack(mBytePacker);
                mFillHighlightMaterialTexture = MaterialTexture.unpack(mBytePacker);
                mAccentFillMaterialGradient = MaterialGradient.unpack(mBytePacker);
                mAccentFillMaterialTexture = MaterialTexture.unpack(mBytePacker);
                mAccentHighlightMaterialGradient = MaterialGradient.unpack(mBytePacker);
                mAccentHighlightMaterialTexture = MaterialTexture.unpack(mBytePacker);
                mBaseAccentMaterialGradient = MaterialGradient.unpack(mBytePacker);
                mBaseAccentMaterialTexture = MaterialTexture.unpack(mBytePacker);

                mFillSixBitColor = mBytePacker.getSixBitColor();
                mHighlightSixBitColor = mBytePacker.getSixBitColor();
                mAccentSixBitColor = mBytePacker.getSixBitColor();
                mBaseSixBitColor = mBytePacker.getSixBitColor();
                break;
            }
            case 1: {
                mHourHandShape = HandShape.unpack(mBytePacker);
                mHourHandLength = HandLength.unpack(mBytePacker);
                mHourHandThickness = HandThickness.unpack(mBytePacker);
                mHourHandStalk = HandStalk.unpack(mBytePacker);
                /*mHourHandCutout =*/
                HandStalk.unpack(mBytePacker);
                mHourHandMaterial = Material.unpack(mBytePacker);
                mHourHandCutoutCombination = HandCutoutCombination.NONE;

                mDigitMaterial = Material.unpack(mBytePacker);
                mDigitDisplay = DigitDisplay.unpack(mBytePacker);
                mDigitRotation = DigitRotation.unpack(mBytePacker);
                mDigitFormat = DigitFormat.unpack2(mBytePacker);

                mMinuteHandOverride = mBytePacker.getBoolean();
                mMinuteHandShape = HandShape.unpack(mBytePacker);
                mMinuteHandLength = HandLength.unpack(mBytePacker);
                mMinuteHandThickness = HandThickness.unpack(mBytePacker);
                mMinuteHandStalk = HandStalk.unpack(mBytePacker);
                /*mMinuteHandCutout =*/
                HandStalk.unpack(mBytePacker);
                mMinuteHandMaterial = Material.unpack(mBytePacker);
                mMinuteHandCutoutCombination = HandCutoutCombination.NONE;

                mSecondHandOverride = mBytePacker.getBoolean();
                mSecondHandShape = HandShape.STRAIGHT; // Hard-coded!
                mSecondHandLength = HandLength.unpack(mBytePacker);
                mSecondHandThickness = HandThickness.unpack(mBytePacker);
                mSecondHandMaterial = Material.unpack(mBytePacker);

                mTicksDisplay = TicksDisplay.unpack(mBytePacker);

                mFourTickShape = TickShape.unpack2(mBytePacker);
                mFourTickSize = TickSize.unpack2(mBytePacker);
                mBytePacker.get(2); // mFourTickThickness
                mBytePacker.get(2); // mFourTickRadiusPosition
                mFourTickMaterial = Material.unpack(mBytePacker);

                mTwelveTickOverride = mBytePacker.getBoolean();
                mTwelveTickShape = TickShape.unpack2(mBytePacker);
                mTwelveTickSize = TickSize.unpack2(mBytePacker);
                mBytePacker.get(2); // mTwelveTickThickness
                mBytePacker.get(2); // mTwelveTickRadiusPosition
                mTwelveTickMaterial = Material.unpack(mBytePacker);

                mSixtyTickOverride = mBytePacker.getBoolean();
                mSixtyTickShape = TickShape.unpack2(mBytePacker);
                mSixtyTickSize = TickSize.unpack2(mBytePacker);
                mBytePacker.get(2); // mSixtyTickThickness
                mBytePacker.get(2); // mSixtyTickRadiusPosition;
                mSixtyTickMaterial = Material.unpack(mBytePacker);

                mFillHighlightMaterialGradient = MaterialGradient.unpack(mBytePacker);
                mFillHighlightMaterialTexture = MaterialTexture.unpack(mBytePacker);
                mAccentFillMaterialGradient = MaterialGradient.unpack(mBytePacker);
                mAccentFillMaterialTexture = MaterialTexture.unpack(mBytePacker);
                mAccentHighlightMaterialGradient = MaterialGradient.unpack(mBytePacker);
                mAccentHighlightMaterialTexture = MaterialTexture.unpack(mBytePacker);
                mBaseAccentMaterialGradient = MaterialGradient.unpack(mBytePacker);
                mBaseAccentMaterialTexture = MaterialTexture.unpack(mBytePacker);

                mFillSixBitColor = mBytePacker.getSixBitColor();
                mHighlightSixBitColor = mBytePacker.getSixBitColor();
                mAccentSixBitColor = mBytePacker.getSixBitColor();
                mBaseSixBitColor = mBytePacker.getSixBitColor();

                mTickMargin = TickMargin.unpack(mBytePacker);
                mTickBackgroundMaterial = Material.unpack(mBytePacker);
                mDigitSize = DigitSize.unpack(mBytePacker);
                break;
            }
            case 2: {
                mHourHandShape = HandShape.unpack(mBytePacker);
                mHourHandLength = HandLength.unpack(mBytePacker);
                mHourHandThickness = HandThickness.unpack(mBytePacker);
                mHourHandStalk = HandStalk.unpack(mBytePacker);
                /*mHourHandCutout =*/
                HandStalk.unpack(mBytePacker);
                mHourHandMaterial = Material.unpack(mBytePacker);
                mHourHandCutoutCombination = HandCutoutCombination.NONE;

                mDigitMaterial = Material.unpack(mBytePacker);
                mDigitDisplay = DigitDisplay.unpack(mBytePacker);
                mDigitSize = DigitSize.unpack(mBytePacker);
                mDigitRotation = DigitRotation.unpack(mBytePacker);
                mDigitFormat = DigitFormat.unpack(mBytePacker);

                mMinuteHandOverride = mBytePacker.getBoolean();
                mMinuteHandShape = HandShape.unpack(mBytePacker);
                mMinuteHandLength = HandLength.unpack(mBytePacker);
                mMinuteHandThickness = HandThickness.unpack(mBytePacker);
                mMinuteHandStalk = HandStalk.unpack(mBytePacker);
                /*mMinuteHandCutout =*/
                HandStalk.unpack(mBytePacker);
                mMinuteHandMaterial = Material.unpack(mBytePacker);
                mMinuteHandCutoutCombination = HandCutoutCombination.NONE;

                mSecondHandOverride = mBytePacker.getBoolean();
                mSecondHandShape = HandShape.STRAIGHT; // Hard-coded!
                mSecondHandLength = HandLength.unpack(mBytePacker);
                mSecondHandThickness = HandThickness.unpack(mBytePacker);
                mSecondHandMaterial = Material.unpack(mBytePacker);

                mTicksDisplay = TicksDisplay.unpack(mBytePacker);
                mTickMargin = TickMargin.unpack(mBytePacker);
                mTickBackgroundMaterial = Material.unpack(mBytePacker);

                mFourTickShape = TickShape.unpack2(mBytePacker);
                mFourTickSize = TickSize.unpack2(mBytePacker);
                mBytePacker.get(2); // mFourTickThickness
                mFourTickMaterial = Material.unpack(mBytePacker);

                mTwelveTickOverride = mBytePacker.getBoolean();
                mTwelveTickShape = TickShape.unpack2(mBytePacker);
                mTwelveTickSize = TickSize.unpack2(mBytePacker);
                mBytePacker.get(2); // mTwelveTickThickness
                mTwelveTickMaterial = Material.unpack(mBytePacker);

                mSixtyTickOverride = mBytePacker.getBoolean();
                mSixtyTickShape = TickShape.unpack2(mBytePacker);
                mSixtyTickSize = TickSize.unpack2(mBytePacker);
                mBytePacker.get(2); // mSixtyTickThickness
                mSixtyTickMaterial = Material.unpack(mBytePacker);

                mFillHighlightMaterialGradient = MaterialGradient.unpack(mBytePacker);
                mFillHighlightMaterialTexture = MaterialTexture.unpack(mBytePacker);
                mAccentFillMaterialGradient = MaterialGradient.unpack(mBytePacker);
                mAccentFillMaterialTexture = MaterialTexture.unpack(mBytePacker);
                mAccentHighlightMaterialGradient = MaterialGradient.unpack(mBytePacker);
                mAccentHighlightMaterialTexture = MaterialTexture.unpack(mBytePacker);
                mBaseAccentMaterialGradient = MaterialGradient.unpack(mBytePacker);
                mBaseAccentMaterialTexture = MaterialTexture.unpack(mBytePacker);

                mFillSixBitColor = mBytePacker.getSixBitColor();
                mHighlightSixBitColor = mBytePacker.getSixBitColor();
                mAccentSixBitColor = mBytePacker.getSixBitColor();
                mBaseSixBitColor = mBytePacker.getSixBitColor();
                break;
            }
            case 3: {
                mHourHandShape = HandShape.unpack(mBytePacker);
                mHourHandLength = HandLength.unpack(mBytePacker);
                mHourHandThickness = HandThickness.unpack(mBytePacker);
                mHourHandStalk = HandStalk.unpack(mBytePacker);
                /*mHourHandCutout =*/
                HandStalk.unpack(mBytePacker);
                mHourHandMaterial = Material.unpack(mBytePacker);
                mHourHandCutoutCombination = HandCutoutCombination.NONE;

                mDigitMaterial = Material.unpack(mBytePacker);
                mDigitDisplay = DigitDisplay.unpack(mBytePacker);
                mDigitSize = DigitSize.unpack(mBytePacker);
                mDigitRotation = DigitRotation.unpack(mBytePacker);
                mDigitFormat = DigitFormat.unpack(mBytePacker);

                mMinuteHandOverride = mBytePacker.getBoolean();
                mMinuteHandShape = HandShape.unpack(mBytePacker);
                mMinuteHandLength = HandLength.unpack(mBytePacker);
                mMinuteHandThickness = HandThickness.unpack(mBytePacker);
                mMinuteHandStalk = HandStalk.unpack(mBytePacker);
                /*mMinuteHandCutout =*/
                HandStalk.unpack(mBytePacker);
                mMinuteHandMaterial = Material.unpack(mBytePacker);
                mMinuteHandCutoutCombination = HandCutoutCombination.NONE;

                mSecondHandOverride = mBytePacker.getBoolean();
                mSecondHandShape = HandShape.STRAIGHT; // Hard-coded!
                mSecondHandLength = HandLength.unpack(mBytePacker);
                mSecondHandThickness = HandThickness.unpack(mBytePacker);
                mSecondHandMaterial = Material.unpack(mBytePacker);

                mTicksDisplay = TicksDisplay.unpack(mBytePacker);
                mTickMargin = TickMargin.unpack(mBytePacker);
                mTickBackgroundMaterial = Material.unpack(mBytePacker);

                mFourTickShape = TickShape.unpack(mBytePacker);
                mFourTickSize = TickSize.unpack(mBytePacker);
                mFourTickMaterial = Material.unpack(mBytePacker);

                mTwelveTickOverride = mBytePacker.getBoolean();
                mTwelveTickShape = TickShape.unpack(mBytePacker);
                mTwelveTickSize = TickSize.unpack(mBytePacker);
                mTwelveTickMaterial = Material.unpack(mBytePacker);

                mSixtyTickOverride = mBytePacker.getBoolean();
                mSixtyTickShape = TickShape.unpack(mBytePacker);
                mSixtyTickSize = TickSize.unpack(mBytePacker);
                mSixtyTickMaterial = Material.unpack(mBytePacker);

                mFillHighlightMaterialGradient = MaterialGradient.unpack(mBytePacker);
                mFillHighlightMaterialTexture = MaterialTexture.unpack(mBytePacker);
                mAccentFillMaterialGradient = MaterialGradient.unpack(mBytePacker);
                mAccentFillMaterialTexture = MaterialTexture.unpack(mBytePacker);
                mAccentHighlightMaterialGradient = MaterialGradient.unpack(mBytePacker);
                mAccentHighlightMaterialTexture = MaterialTexture.unpack(mBytePacker);
                mBaseAccentMaterialGradient = MaterialGradient.unpack(mBytePacker);
                mBaseAccentMaterialTexture = MaterialTexture.unpack(mBytePacker);

                mFillSixBitColor = mBytePacker.getSixBitColor();
                mHighlightSixBitColor = mBytePacker.getSixBitColor();
                mAccentSixBitColor = mBytePacker.getSixBitColor();
                mBaseSixBitColor = mBytePacker.getSixBitColor();

                /*mHourHandCutoutMaterial =*/
                Material.unpack(mBytePacker);
                /*mMinuteHandCutoutMaterial =*/
                Material.unpack(mBytePacker);
                break;
            }
        }
    }
}
