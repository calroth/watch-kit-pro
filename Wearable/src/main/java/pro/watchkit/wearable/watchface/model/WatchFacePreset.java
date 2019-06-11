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

public final class WatchFacePreset extends BytePackable implements Cloneable {
    Style mBackgroundStyle;
    boolean mMinuteHandOverride;
    boolean mSecondHandOverride;
    HandShape mHourHandShape, mMinuteHandShape, mSecondHandShape;
    HandLength mHourHandLength, mMinuteHandLength, mSecondHandLength;
    HandThickness mHourHandThickness, mMinuteHandThickness, mSecondHandThickness;
    HandStalk mHourHandStalk, mMinuteHandStalk;
    HandCutout mHourHandCutout, mMinuteHandCutout;
    Style mHourHandStyle, mMinuteHandStyle, mSecondHandStyle;
    TicksDisplay mTicksDisplay;
    boolean mTwelveTickOverride, mSixtyTickOverride;
    TickShape mFourTickShape, mTwelveTickShape, mSixtyTickShape;
    TickLength mFourTickLength, mTwelveTickLength, mSixtyTickLength;
    TickThickness mFourTickThickness, mTwelveTickThickness, mSixtyTickThickness;
    TickRadiusPosition mFourTickRadiusPosition, mTwelveTickRadiusPosition, mSixtyTickRadiusPosition;
    Style mFourTickStyle;
    Style mTwelveTickStyle;
    Style mSixtyTickStyle;
    int mFillSixBitColor;
    int mAccentSixBitColor;
    int mHighlightSixBitColor;
    int mBaseSixBitColor;
    GradientStyle mFillHighlightStyle;
    GradientStyle mAccentFillStyle;
    GradientStyle mAccentHighlightStyle;
    GradientStyle mBaseAccentStyle;

    @Override
    public int hashCode() {
        return Objects.hash(
                super.hashCode(),
                mBackgroundStyle,
                mMinuteHandOverride,
                mSecondHandOverride,
                mHourHandShape, mMinuteHandShape, mSecondHandShape,
                mHourHandLength, mMinuteHandLength, mSecondHandLength,
                mHourHandThickness, mMinuteHandThickness, mSecondHandThickness,
                mHourHandStalk, mMinuteHandStalk,
                mHourHandCutout, mMinuteHandCutout,
                mHourHandStyle, mMinuteHandStyle, mSecondHandStyle,
                mTicksDisplay,
                mTwelveTickOverride, mSixtyTickOverride,
                mFourTickShape, mTwelveTickShape, mSixtyTickShape,
                mFourTickLength, mTwelveTickLength, mSixtyTickLength,
                mFourTickThickness, mTwelveTickThickness, mSixtyTickThickness,
                mFourTickRadiusPosition, mTwelveTickRadiusPosition, mSixtyTickRadiusPosition,
                mFourTickStyle,
                mTwelveTickStyle,
                mSixtyTickStyle,
                mFillSixBitColor,
                mAccentSixBitColor,
                mHighlightSixBitColor,
                mBaseSixBitColor,
                mFillHighlightStyle,
                mAccentFillStyle,
                mAccentHighlightStyle,
                mBaseAccentStyle);
    }

    public WatchFacePreset clone() {
        WatchFacePreset result;
        try {
            result = (WatchFacePreset) cloneInternal();
        } catch (CloneNotSupportedException e) {
            result = new WatchFacePreset();
            result.setString(getString());
        }
        return result;
    }

    @Override
    void pack() {
        mBytePacker.rewind();

        mBackgroundStyle.pack(mBytePacker);

        mHourHandShape.pack(mBytePacker);
        mHourHandLength.pack(mBytePacker);
        mHourHandThickness.pack(mBytePacker);
        mHourHandStalk.pack(mBytePacker);
        mHourHandStyle.pack(mBytePacker);

        mBytePacker.put(mMinuteHandOverride);
        mMinuteHandShape.pack(mBytePacker);
        mMinuteHandLength.pack(mBytePacker);
        mMinuteHandThickness.pack(mBytePacker);
        mMinuteHandStalk.pack(mBytePacker);
        mMinuteHandStyle.pack(mBytePacker);

        mBytePacker.put(mSecondHandOverride);
        mSecondHandShape.pack(mBytePacker);
        mSecondHandLength.pack(mBytePacker);
        mSecondHandThickness.pack(mBytePacker);
        mSecondHandStyle.pack(mBytePacker);

        mTicksDisplay.pack(mBytePacker);

        mFourTickShape.pack(mBytePacker);
        mFourTickLength.pack(mBytePacker);
        mFourTickThickness.pack(mBytePacker);
        mFourTickRadiusPosition.pack(mBytePacker);
        mFourTickStyle.pack(mBytePacker);

        mBytePacker.put(mTwelveTickOverride);
        mTwelveTickShape.pack(mBytePacker);
        mTwelveTickLength.pack(mBytePacker);
        mTwelveTickThickness.pack(mBytePacker);
        mTwelveTickRadiusPosition.pack(mBytePacker);
        mTwelveTickStyle.pack(mBytePacker);

        mBytePacker.put(mSixtyTickOverride);
        mSixtyTickShape.pack(mBytePacker);
        mSixtyTickLength.pack(mBytePacker);
        mSixtyTickThickness.pack(mBytePacker);
        mSixtyTickRadiusPosition.pack(mBytePacker);
        mSixtyTickStyle.pack(mBytePacker);

        mFillHighlightStyle.pack(mBytePacker);
        mAccentFillStyle.pack(mBytePacker);
        mAccentHighlightStyle.pack(mBytePacker);
        mBaseAccentStyle.pack(mBytePacker);

        mBytePacker.putSixBitColor(mFillSixBitColor);
        mBytePacker.putSixBitColor(mHighlightSixBitColor);
        mBytePacker.putSixBitColor(mAccentSixBitColor);
        mBytePacker.putSixBitColor(mBaseSixBitColor);

        // TODO: rearrange these into their right place
        mHourHandCutout.pack(mBytePacker);
        mMinuteHandCutout.pack(mBytePacker);

        mBytePacker.finish();
    }

    @Override
    void unpack() {
        mBytePacker.rewind();

        mBackgroundStyle = Style.unpack(mBytePacker);

        mHourHandShape = HandShape.unpack(mBytePacker);
        mHourHandLength = HandLength.unpack(mBytePacker);
        mHourHandThickness = HandThickness.unpack(mBytePacker);
        mHourHandStalk = HandStalk.unpack(mBytePacker);
        mHourHandStyle = Style.unpack(mBytePacker);

        mMinuteHandOverride = mBytePacker.getBoolean();
        mMinuteHandShape = HandShape.unpack(mBytePacker);
        mMinuteHandLength = HandLength.unpack(mBytePacker);
        mMinuteHandThickness = HandThickness.unpack(mBytePacker);
        mMinuteHandStalk = HandStalk.unpack(mBytePacker);
        mMinuteHandStyle = Style.unpack(mBytePacker);

        mSecondHandOverride = mBytePacker.getBoolean();
        mSecondHandShape = HandShape.unpack(mBytePacker);
        mSecondHandLength = HandLength.unpack(mBytePacker);
        mSecondHandThickness = HandThickness.unpack(mBytePacker);
        mSecondHandStyle = Style.unpack(mBytePacker);

        mTicksDisplay = TicksDisplay.unpack(mBytePacker);

        mFourTickShape = TickShape.unpack(mBytePacker);
        mFourTickLength = TickLength.unpack(mBytePacker);
        mFourTickThickness = TickThickness.unpack(mBytePacker);
        mFourTickRadiusPosition = TickRadiusPosition.unpack(mBytePacker);
        mFourTickStyle = Style.unpack(mBytePacker);

        mTwelveTickOverride = mBytePacker.getBoolean();
        mTwelveTickShape = TickShape.unpack(mBytePacker);
        mTwelveTickLength = TickLength.unpack(mBytePacker);
        mTwelveTickThickness = TickThickness.unpack(mBytePacker);
        mTwelveTickRadiusPosition = TickRadiusPosition.unpack(mBytePacker);
        mTwelveTickStyle = Style.unpack(mBytePacker);

        mSixtyTickOverride = mBytePacker.getBoolean();
        mSixtyTickShape = TickShape.unpack(mBytePacker);
        mSixtyTickLength = TickLength.unpack(mBytePacker);
        mSixtyTickThickness = TickThickness.unpack(mBytePacker);
        mSixtyTickRadiusPosition = TickRadiusPosition.unpack(mBytePacker);
        mSixtyTickStyle = Style.unpack(mBytePacker);

        mFillHighlightStyle = GradientStyle.unpack(mBytePacker);
        mAccentFillStyle = GradientStyle.unpack(mBytePacker);
        mAccentHighlightStyle = GradientStyle.unpack(mBytePacker);
        mBaseAccentStyle = GradientStyle.unpack(mBytePacker);

        mFillSixBitColor = mBytePacker.getSixBitColor();
        mHighlightSixBitColor = mBytePacker.getSixBitColor();
        mAccentSixBitColor = mBytePacker.getSixBitColor();
        mBaseSixBitColor = mBytePacker.getSixBitColor();

        // TODO: rearrange these into their right place
        mHourHandCutout = HandCutout.unpack(mBytePacker);
        mMinuteHandCutout = HandCutout.unpack(mBytePacker);
    }
}
