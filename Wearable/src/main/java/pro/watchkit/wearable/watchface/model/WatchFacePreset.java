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
    HandCutout mHourHandCutout, mMinuteHandCutout;
    Style mHourHandStyle, mMinuteHandStyle, mSecondHandStyle;
    Style mHourHandCutoutStyle, mMinuteHandCutoutStyle;
    TicksDisplay mTicksDisplay;
    boolean mTwelveTickOverride, mSixtyTickOverride;
    TickShape mFourTickShape, mTwelveTickShape, mSixtyTickShape;
    TickSize mFourTickSize, mTwelveTickSize, mSixtyTickSize;
    Style mFourTickStyle, mTwelveTickStyle, mSixtyTickStyle, mDigitStyle, mTickBackgroundStyle;
    TickMargin mTickMargin;
    DigitDisplay mDigitDisplay;
    DigitSize mDigitSize;
    DigitRotation mDigitRotation;
    DigitFormat mDigitFormat;
    int mFillSixBitColor, mAccentSixBitColor, mHighlightSixBitColor, mBaseSixBitColor;
    StyleGradient mFillHighlightStyleGradient, mAccentFillStyleGradient,
            mAccentHighlightStyleGradient, mBaseAccentStyleGradient;
    StyleTexture mFillHighlightStyleTexture, mAccentFillStyleTexture,
            mAccentHighlightStyleTexture, mBaseAccentStyleTexture;

    @Override
    public int hashCode() {
        return Objects.hash(
                super.hashCode(),
                mMinuteHandOverride, mSecondHandOverride,
                mHourHandShape, mMinuteHandShape, mSecondHandShape,
                mHourHandLength, mMinuteHandLength, mSecondHandLength,
                mHourHandThickness, mMinuteHandThickness, mSecondHandThickness,
                mHourHandStalk, mMinuteHandStalk,
                mHourHandCutout, mMinuteHandCutout,
                mHourHandStyle, mMinuteHandStyle, mSecondHandStyle,
                mTicksDisplay,
                mTwelveTickOverride, mSixtyTickOverride,
                mFourTickShape, mTwelveTickShape, mSixtyTickShape,
                mFourTickSize, mTwelveTickSize, mSixtyTickSize,
                mTickMargin,
                mFourTickStyle, mTwelveTickStyle, mSixtyTickStyle, mDigitStyle, mTickBackgroundStyle,
                mDigitDisplay,
                mDigitSize,
                mDigitRotation,
                mDigitFormat,
                mFillSixBitColor, mAccentSixBitColor, mHighlightSixBitColor, mBaseSixBitColor,
                mFillHighlightStyleGradient, mAccentFillStyleGradient,
                mAccentHighlightStyleGradient, mBaseAccentStyleGradient,
                mFillHighlightStyleTexture, mAccentFillStyleTexture,
                mAccentHighlightStyleTexture, mBaseAccentStyleTexture);
    }

    @Override
    void pack() {
        // Version 3
        mBytePacker.rewind();

        // Pack version 3
        mBytePacker.put(3, 3);

        mHourHandShape.pack(mBytePacker);
        mHourHandLength.pack(mBytePacker);
        mHourHandThickness.pack(mBytePacker);
        mHourHandStalk.pack(mBytePacker);
        mHourHandCutout.pack(mBytePacker);
        mHourHandStyle.pack(mBytePacker);

        mDigitStyle.pack(mBytePacker);
        mDigitDisplay.pack(mBytePacker);
        mDigitSize.pack(mBytePacker);
        mDigitRotation.pack(mBytePacker);
        mDigitFormat.pack(mBytePacker);

        mBytePacker.put(mMinuteHandOverride);
        mMinuteHandShape.pack(mBytePacker);
        mMinuteHandLength.pack(mBytePacker);
        mMinuteHandThickness.pack(mBytePacker);
        mMinuteHandStalk.pack(mBytePacker);
        mMinuteHandCutout.pack(mBytePacker);
        mMinuteHandStyle.pack(mBytePacker);

        mBytePacker.put(mSecondHandOverride);
        // mSecondHandShape.pack(mBytePacker); // Don't worry about this.
        mSecondHandLength.pack(mBytePacker);
        mSecondHandThickness.pack(mBytePacker);
        mSecondHandStyle.pack(mBytePacker);

        mTicksDisplay.pack(mBytePacker);
        mTickMargin.pack(mBytePacker);
        mTickBackgroundStyle.pack(mBytePacker);

        mFourTickShape.pack(mBytePacker);
        mFourTickSize.pack(mBytePacker);
//        mFourTickThickness.pack(mBytePacker);
        mFourTickStyle.pack(mBytePacker);

        mBytePacker.put(mTwelveTickOverride);
        mTwelveTickShape.pack(mBytePacker);
        mTwelveTickSize.pack(mBytePacker);
//        mTwelveTickThickness.pack(mBytePacker);
        mTwelveTickStyle.pack(mBytePacker);

        mBytePacker.put(mSixtyTickOverride);
        mSixtyTickShape.pack(mBytePacker);
        mSixtyTickSize.pack(mBytePacker);
//        mSixtyTickThickness.pack(mBytePacker);
        mSixtyTickStyle.pack(mBytePacker);

        mFillHighlightStyleGradient.pack(mBytePacker);
        mFillHighlightStyleTexture.pack(mBytePacker);
        mAccentFillStyleGradient.pack(mBytePacker);
        mAccentFillStyleTexture.pack(mBytePacker);
        mAccentHighlightStyleGradient.pack(mBytePacker);
        mAccentHighlightStyleTexture.pack(mBytePacker);
        mBaseAccentStyleGradient.pack(mBytePacker);
        mBaseAccentStyleTexture.pack(mBytePacker);

        mBytePacker.putSixBitColor(mFillSixBitColor);
        mBytePacker.putSixBitColor(mHighlightSixBitColor);
        mBytePacker.putSixBitColor(mAccentSixBitColor);
        mBytePacker.putSixBitColor(mBaseSixBitColor);

        mHourHandCutoutStyle.pack(mBytePacker);
        mMinuteHandCutoutStyle.pack(mBytePacker);

        mBytePacker.finish();
    }

    @Override
    void unpack() {
        mBytePacker.rewind();

        int version = mBytePacker.get(3);
        switch (version) {
            case 0: {
                /* mBackgroundStyle = */
                Style.unpack3(mBytePacker);

                mHourHandShape = HandShape.unpack(mBytePacker);
                mHourHandLength = HandLength.unpack(mBytePacker);
                mHourHandThickness = HandThickness.unpack(mBytePacker);
                mHourHandStalk = HandStalk.unpack(mBytePacker);
                mHourHandCutout = HandCutout.unpack(mBytePacker);
                mHourHandStyle = Style.unpack3(mBytePacker);
                mHourHandCutoutStyle = Style.BASE_ACCENT;

                mDigitStyle = Style.unpack3(mBytePacker);
                mDigitDisplay = DigitDisplay.unpack(mBytePacker);
                mDigitRotation = DigitRotation.unpack(mBytePacker);
                mDigitFormat = DigitFormat.unpack2(mBytePacker);

                mMinuteHandOverride = mBytePacker.getBoolean();
                mMinuteHandShape = HandShape.unpack(mBytePacker);
                mMinuteHandLength = HandLength.unpack(mBytePacker);
                mMinuteHandThickness = HandThickness.unpack(mBytePacker);
                mMinuteHandStalk = HandStalk.unpack(mBytePacker);
                mMinuteHandCutout = HandCutout.unpack(mBytePacker);
                mMinuteHandStyle = Style.unpack3(mBytePacker);
                mMinuteHandCutoutStyle = Style.BASE_ACCENT;

                mSecondHandOverride = mBytePacker.getBoolean();
                mSecondHandShape = HandShape.STRAIGHT; // Hard-coded!
                mSecondHandLength = HandLength.unpack(mBytePacker);
                mSecondHandThickness = HandThickness.unpack(mBytePacker);
                mSecondHandStyle = Style.unpack3(mBytePacker);

                mTicksDisplay = TicksDisplay.unpack(mBytePacker);

                mFourTickShape = TickShape.unpack2(mBytePacker);
                mFourTickSize = TickSize.unpack2(mBytePacker);
                mBytePacker.get(2); // mFourTickThickness
                mBytePacker.get(2); // mFourTickRadiusPosition
                mFourTickStyle = Style.unpack3(mBytePacker);

                mTwelveTickOverride = mBytePacker.getBoolean();
                mTwelveTickShape = TickShape.unpack2(mBytePacker);
                mTwelveTickSize = TickSize.unpack2(mBytePacker);
                mBytePacker.get(2); // mTwelveTickThickness
                mBytePacker.get(2); // mTwelveTickRadiusPosition
                mTwelveTickStyle = Style.unpack3(mBytePacker);

                mSixtyTickOverride = mBytePacker.getBoolean();
                mSixtyTickShape = TickShape.unpack2(mBytePacker);
                mSixtyTickSize = TickSize.unpack2(mBytePacker);
                mBytePacker.get(2); // mSixtyTickThickness
                mBytePacker.get(2); // mSixtyTickRadiusPosition
                mSixtyTickStyle = Style.unpack3(mBytePacker);

                mFillHighlightStyleGradient = StyleGradient.unpack(mBytePacker);
                mFillHighlightStyleTexture = StyleTexture.unpack(mBytePacker);
                mAccentFillStyleGradient = StyleGradient.unpack(mBytePacker);
                mAccentFillStyleTexture = StyleTexture.unpack(mBytePacker);
                mAccentHighlightStyleGradient = StyleGradient.unpack(mBytePacker);
                mAccentHighlightStyleTexture = StyleTexture.unpack(mBytePacker);
                mBaseAccentStyleGradient = StyleGradient.unpack(mBytePacker);
                mBaseAccentStyleTexture = StyleTexture.unpack(mBytePacker);

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
                mHourHandCutout = HandCutout.unpack(mBytePacker);
                mHourHandStyle = Style.unpack(mBytePacker);
                mHourHandCutoutStyle = Style.BASE_ACCENT;

                mDigitStyle = Style.unpack(mBytePacker);
                mDigitDisplay = DigitDisplay.unpack(mBytePacker);
                mDigitRotation = DigitRotation.unpack(mBytePacker);
                mDigitFormat = DigitFormat.unpack2(mBytePacker);

                mMinuteHandOverride = mBytePacker.getBoolean();
                mMinuteHandShape = HandShape.unpack(mBytePacker);
                mMinuteHandLength = HandLength.unpack(mBytePacker);
                mMinuteHandThickness = HandThickness.unpack(mBytePacker);
                mMinuteHandStalk = HandStalk.unpack(mBytePacker);
                mMinuteHandCutout = HandCutout.unpack(mBytePacker);
                mMinuteHandStyle = Style.unpack(mBytePacker);
                mMinuteHandCutoutStyle = Style.BASE_ACCENT;

                mSecondHandOverride = mBytePacker.getBoolean();
                mSecondHandShape = HandShape.STRAIGHT; // Hard-coded!
                mSecondHandLength = HandLength.unpack(mBytePacker);
                mSecondHandThickness = HandThickness.unpack(mBytePacker);
                mSecondHandStyle = Style.unpack(mBytePacker);

                mTicksDisplay = TicksDisplay.unpack(mBytePacker);

                mFourTickShape = TickShape.unpack2(mBytePacker);
                mFourTickSize = TickSize.unpack2(mBytePacker);
                mBytePacker.get(2); // mFourTickThickness
                mBytePacker.get(2); // mFourTickRadiusPosition
                mFourTickStyle = Style.unpack(mBytePacker);

                mTwelveTickOverride = mBytePacker.getBoolean();
                mTwelveTickShape = TickShape.unpack2(mBytePacker);
                mTwelveTickSize = TickSize.unpack2(mBytePacker);
                mBytePacker.get(2); // mTwelveTickThickness
                mBytePacker.get(2); // mTwelveTickRadiusPosition
                mTwelveTickStyle = Style.unpack(mBytePacker);

                mSixtyTickOverride = mBytePacker.getBoolean();
                mSixtyTickShape = TickShape.unpack2(mBytePacker);
                mSixtyTickSize = TickSize.unpack2(mBytePacker);
                mBytePacker.get(2); // mSixtyTickThickness
                mBytePacker.get(2); // mSixtyTickRadiusPosition;
                mSixtyTickStyle = Style.unpack(mBytePacker);

                mFillHighlightStyleGradient = StyleGradient.unpack(mBytePacker);
                mFillHighlightStyleTexture = StyleTexture.unpack(mBytePacker);
                mAccentFillStyleGradient = StyleGradient.unpack(mBytePacker);
                mAccentFillStyleTexture = StyleTexture.unpack(mBytePacker);
                mAccentHighlightStyleGradient = StyleGradient.unpack(mBytePacker);
                mAccentHighlightStyleTexture = StyleTexture.unpack(mBytePacker);
                mBaseAccentStyleGradient = StyleGradient.unpack(mBytePacker);
                mBaseAccentStyleTexture = StyleTexture.unpack(mBytePacker);

                mFillSixBitColor = mBytePacker.getSixBitColor();
                mHighlightSixBitColor = mBytePacker.getSixBitColor();
                mAccentSixBitColor = mBytePacker.getSixBitColor();
                mBaseSixBitColor = mBytePacker.getSixBitColor();

                mTickMargin = TickMargin.unpack(mBytePacker);
                mTickBackgroundStyle = Style.unpack(mBytePacker);
                mDigitSize = DigitSize.unpack(mBytePacker);
                break;
            }
            case 2: {
                mHourHandShape = HandShape.unpack(mBytePacker);
                mHourHandLength = HandLength.unpack(mBytePacker);
                mHourHandThickness = HandThickness.unpack(mBytePacker);
                mHourHandStalk = HandStalk.unpack(mBytePacker);
                mHourHandCutout = HandCutout.unpack(mBytePacker);
                mHourHandStyle = Style.unpack(mBytePacker);
                mHourHandCutoutStyle = Style.BASE_ACCENT;

                mDigitStyle = Style.unpack(mBytePacker);
                mDigitDisplay = DigitDisplay.unpack(mBytePacker);
                mDigitSize = DigitSize.unpack(mBytePacker);
                mDigitRotation = DigitRotation.unpack(mBytePacker);
                mDigitFormat = DigitFormat.unpack(mBytePacker);

                mMinuteHandOverride = mBytePacker.getBoolean();
                mMinuteHandShape = HandShape.unpack(mBytePacker);
                mMinuteHandLength = HandLength.unpack(mBytePacker);
                mMinuteHandThickness = HandThickness.unpack(mBytePacker);
                mMinuteHandStalk = HandStalk.unpack(mBytePacker);
                mMinuteHandCutout = HandCutout.unpack(mBytePacker);
                mMinuteHandStyle = Style.unpack(mBytePacker);
                mMinuteHandCutoutStyle = Style.BASE_ACCENT;

                mSecondHandOverride = mBytePacker.getBoolean();
                mSecondHandShape = HandShape.STRAIGHT; // Hard-coded!
                mSecondHandLength = HandLength.unpack(mBytePacker);
                mSecondHandThickness = HandThickness.unpack(mBytePacker);
                mSecondHandStyle = Style.unpack(mBytePacker);

                mTicksDisplay = TicksDisplay.unpack(mBytePacker);
                mTickMargin = TickMargin.unpack(mBytePacker);
                mTickBackgroundStyle = Style.unpack(mBytePacker);

                mFourTickShape = TickShape.unpack2(mBytePacker);
                mFourTickSize = TickSize.unpack2(mBytePacker);
                mBytePacker.get(2); // mFourTickThickness
                mFourTickStyle = Style.unpack(mBytePacker);

                mTwelveTickOverride = mBytePacker.getBoolean();
                mTwelveTickShape = TickShape.unpack2(mBytePacker);
                mTwelveTickSize = TickSize.unpack2(mBytePacker);
                mBytePacker.get(2); // mTwelveTickThickness
                mTwelveTickStyle = Style.unpack(mBytePacker);

                mSixtyTickOverride = mBytePacker.getBoolean();
                mSixtyTickShape = TickShape.unpack2(mBytePacker);
                mSixtyTickSize = TickSize.unpack2(mBytePacker);
                mBytePacker.get(2); // mSixtyTickThickness
                mSixtyTickStyle = Style.unpack(mBytePacker);

                mFillHighlightStyleGradient = StyleGradient.unpack(mBytePacker);
                mFillHighlightStyleTexture = StyleTexture.unpack(mBytePacker);
                mAccentFillStyleGradient = StyleGradient.unpack(mBytePacker);
                mAccentFillStyleTexture = StyleTexture.unpack(mBytePacker);
                mAccentHighlightStyleGradient = StyleGradient.unpack(mBytePacker);
                mAccentHighlightStyleTexture = StyleTexture.unpack(mBytePacker);
                mBaseAccentStyleGradient = StyleGradient.unpack(mBytePacker);
                mBaseAccentStyleTexture = StyleTexture.unpack(mBytePacker);

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
                mHourHandCutout = HandCutout.unpack(mBytePacker);
                mHourHandStyle = Style.unpack(mBytePacker);

                mDigitStyle = Style.unpack(mBytePacker);
                mDigitDisplay = DigitDisplay.unpack(mBytePacker);
                mDigitSize = DigitSize.unpack(mBytePacker);
                mDigitRotation = DigitRotation.unpack(mBytePacker);
                mDigitFormat = DigitFormat.unpack(mBytePacker);

                mMinuteHandOverride = mBytePacker.getBoolean();
                mMinuteHandShape = HandShape.unpack(mBytePacker);
                mMinuteHandLength = HandLength.unpack(mBytePacker);
                mMinuteHandThickness = HandThickness.unpack(mBytePacker);
                mMinuteHandStalk = HandStalk.unpack(mBytePacker);
                mMinuteHandCutout = HandCutout.unpack(mBytePacker);
                mMinuteHandStyle = Style.unpack(mBytePacker);

                mSecondHandOverride = mBytePacker.getBoolean();
                mSecondHandShape = HandShape.STRAIGHT; // Hard-coded!
                mSecondHandLength = HandLength.unpack(mBytePacker);
                mSecondHandThickness = HandThickness.unpack(mBytePacker);
                mSecondHandStyle = Style.unpack(mBytePacker);

                mTicksDisplay = TicksDisplay.unpack(mBytePacker);
                mTickMargin = TickMargin.unpack(mBytePacker);
                mTickBackgroundStyle = Style.unpack(mBytePacker);

                mFourTickShape = TickShape.unpack(mBytePacker);
                mFourTickSize = TickSize.unpack(mBytePacker);
                mFourTickStyle = Style.unpack(mBytePacker);

                mTwelveTickOverride = mBytePacker.getBoolean();
                mTwelveTickShape = TickShape.unpack(mBytePacker);
                mTwelveTickSize = TickSize.unpack(mBytePacker);
                mTwelveTickStyle = Style.unpack(mBytePacker);

                mSixtyTickOverride = mBytePacker.getBoolean();
                mSixtyTickShape = TickShape.unpack(mBytePacker);
                mSixtyTickSize = TickSize.unpack(mBytePacker);
                mSixtyTickStyle = Style.unpack(mBytePacker);

                mFillHighlightStyleGradient = StyleGradient.unpack(mBytePacker);
                mFillHighlightStyleTexture = StyleTexture.unpack(mBytePacker);
                mAccentFillStyleGradient = StyleGradient.unpack(mBytePacker);
                mAccentFillStyleTexture = StyleTexture.unpack(mBytePacker);
                mAccentHighlightStyleGradient = StyleGradient.unpack(mBytePacker);
                mAccentHighlightStyleTexture = StyleTexture.unpack(mBytePacker);
                mBaseAccentStyleGradient = StyleGradient.unpack(mBytePacker);
                mBaseAccentStyleTexture = StyleTexture.unpack(mBytePacker);

                mFillSixBitColor = mBytePacker.getSixBitColor();
                mHighlightSixBitColor = mBytePacker.getSixBitColor();
                mAccentSixBitColor = mBytePacker.getSixBitColor();
                mBaseSixBitColor = mBytePacker.getSixBitColor();

                mHourHandCutoutStyle = Style.unpack(mBytePacker);
                mMinuteHandCutoutStyle = Style.unpack(mBytePacker);
                break;
            }
        }
    }
}
