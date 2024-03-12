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
    PipsDisplay mPipsDisplay;
    boolean mHourPipOverride, mMinutePipOverride;
    PipShape mQuarterPipShape, mHourPipShape, mMinutePipShape;
    PipSize mQuarterPipSize, mHourPipSize, mMinutePipSize;
    Material mQuarterPipMaterial, mHourPipMaterial, mMinutePipMaterial, mDigitMaterial, mPipBackgroundMaterial;
    PipMargin mPipMargin;
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
                mPipsDisplay,
                mHourPipOverride, mMinutePipOverride,
                mQuarterPipShape, mHourPipShape, mMinutePipShape,
                mQuarterPipSize, mHourPipSize, mMinutePipSize,
                mPipMargin,
                mQuarterPipMaterial, mHourPipMaterial, mMinutePipMaterial, mDigitMaterial, mPipBackgroundMaterial,
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

        mPipsDisplay.pack(mBytePacker);
        mPipMargin.pack(mBytePacker);
        mPipBackgroundMaterial.pack(mBytePacker);

        mQuarterPipShape.pack(mBytePacker);
        mQuarterPipSize.pack(mBytePacker);
//        mQuarterPipThickness.pack(mBytePacker);
        mQuarterPipMaterial.pack(mBytePacker);

        mBytePacker.put(mHourPipOverride);
        mHourPipShape.pack(mBytePacker);
        mHourPipSize.pack(mBytePacker);
//        mHourPipThickness.pack(mBytePacker);
        mHourPipMaterial.pack(mBytePacker);

        mBytePacker.put(mMinutePipOverride);
        mMinutePipShape.pack(mBytePacker);
        mMinutePipSize.pack(mBytePacker);
//        mMinutePipThickness.pack(mBytePacker);
        mMinutePipMaterial.pack(mBytePacker);

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

                mPipsDisplay = PipsDisplay.unpack(mBytePacker);
                mPipMargin = PipMargin.unpack(mBytePacker);
                mPipBackgroundMaterial = Material.unpack(mBytePacker);

                mQuarterPipShape = PipShape.unpack(mBytePacker);
                mQuarterPipSize = PipSize.unpack(mBytePacker);
                mQuarterPipMaterial = Material.unpack(mBytePacker);

                mHourPipOverride = mBytePacker.getBoolean();
                mHourPipShape = PipShape.unpack(mBytePacker);
                mHourPipSize = PipSize.unpack(mBytePacker);
                mHourPipMaterial = Material.unpack(mBytePacker);

                mMinutePipOverride = mBytePacker.getBoolean();
                mMinutePipShape = PipShape.unpack(mBytePacker);
                mMinutePipSize = PipSize.unpack(mBytePacker);
                mMinutePipMaterial = Material.unpack(mBytePacker);

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

                mPipsDisplay = PipsDisplay.unpack(mBytePacker);

                mQuarterPipShape = PipShape.unpack2(mBytePacker);
                mQuarterPipSize = PipSize.unpack2(mBytePacker);
                mBytePacker.get(2); // mQuarterPipThickness
                mBytePacker.get(2); // mQuarterPipRadiusPosition
                mQuarterPipMaterial = Material.unpack(mBytePacker);

                mHourPipOverride = mBytePacker.getBoolean();
                mHourPipShape = PipShape.unpack2(mBytePacker);
                mHourPipSize = PipSize.unpack2(mBytePacker);
                mBytePacker.get(2); // mHourPipThickness
                mBytePacker.get(2); // mHourPipRadiusPosition
                mHourPipMaterial = Material.unpack(mBytePacker);

                mMinutePipOverride = mBytePacker.getBoolean();
                mMinutePipShape = PipShape.unpack2(mBytePacker);
                mMinutePipSize = PipSize.unpack2(mBytePacker);
                mBytePacker.get(2); // mMinutePipThickness
                mBytePacker.get(2); // mMinutePipRadiusPosition;
                mMinutePipMaterial = Material.unpack(mBytePacker);

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

                mPipMargin = PipMargin.unpack(mBytePacker);
                mPipBackgroundMaterial = Material.unpack(mBytePacker);
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

                mPipsDisplay = PipsDisplay.unpack(mBytePacker);
                mPipMargin = PipMargin.unpack(mBytePacker);
                mPipBackgroundMaterial = Material.unpack(mBytePacker);

                mQuarterPipShape = PipShape.unpack2(mBytePacker);
                mQuarterPipSize = PipSize.unpack2(mBytePacker);
                mBytePacker.get(2); // mQuarterPipThickness
                mQuarterPipMaterial = Material.unpack(mBytePacker);

                mHourPipOverride = mBytePacker.getBoolean();
                mHourPipShape = PipShape.unpack2(mBytePacker);
                mHourPipSize = PipSize.unpack2(mBytePacker);
                mBytePacker.get(2); // mHourPipThickness
                mHourPipMaterial = Material.unpack(mBytePacker);

                mMinutePipOverride = mBytePacker.getBoolean();
                mMinutePipShape = PipShape.unpack2(mBytePacker);
                mMinutePipSize = PipSize.unpack2(mBytePacker);
                mBytePacker.get(2); // mMinutePipThickness
                mMinutePipMaterial = Material.unpack(mBytePacker);

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

                mPipsDisplay = PipsDisplay.unpack(mBytePacker);
                mPipMargin = PipMargin.unpack(mBytePacker);
                mPipBackgroundMaterial = Material.unpack(mBytePacker);

                mQuarterPipShape = PipShape.unpack(mBytePacker);
                mQuarterPipSize = PipSize.unpack(mBytePacker);
                mQuarterPipMaterial = Material.unpack(mBytePacker);

                mHourPipOverride = mBytePacker.getBoolean();
                mHourPipShape = PipShape.unpack(mBytePacker);
                mHourPipSize = PipSize.unpack(mBytePacker);
                mHourPipMaterial = Material.unpack(mBytePacker);

                mMinutePipOverride = mBytePacker.getBoolean();
                mMinutePipShape = PipShape.unpack(mBytePacker);
                mMinutePipSize = PipSize.unpack(mBytePacker);
                mMinutePipMaterial = Material.unpack(mBytePacker);

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
