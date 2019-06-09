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
    private Style mBackgroundStyle;
    private boolean mMinuteHandOverride;
    private boolean mSecondHandOverride;
    private HandShape mHourHandShape, mMinuteHandShape, mSecondHandShape;
    private HandLength mHourHandLength, mMinuteHandLength, mSecondHandLength;
    private HandThickness mHourHandThickness, mMinuteHandThickness, mSecondHandThickness;
    private HandStalk mHourHandStalk, mMinuteHandStalk;
    private HandCutout mHourHandCutout, mMinuteHandCutout;
    private Style mHourHandStyle, mMinuteHandStyle, mSecondHandStyle;
    private TicksDisplay mTicksDisplay;
    private boolean mTwelveTickOverride, mSixtyTickOverride;
    private TickShape mFourTickShape, mTwelveTickShape, mSixtyTickShape;
    private TickLength mFourTickLength, mTwelveTickLength, mSixtyTickLength;
    private TickThickness mFourTickThickness, mTwelveTickThickness, mSixtyTickThickness;
    private TickRadiusPosition
            mFourTickRadiusPosition, mTwelveTickRadiusPosition, mSixtyTickRadiusPosition;
    private Style mFourTickStyle;
    private Style mTwelveTickStyle;
    private Style mSixtyTickStyle;
    private int mFillSixBitColor;
    private int mAccentSixBitColor;
    private int mHighlightSixBitColor;
    private int mBaseSixBitColor;
    private GradientStyle mFillHighlightStyle;
    private GradientStyle mAccentFillStyle;
    private GradientStyle mAccentHighlightStyle;
    private GradientStyle mBaseAccentStyle;

    public WatchFacePreset() {
//        mBytePacker.setString("2a4c845ec530d34bffa86609f82f6407");
//        unpack();

        setFillSixBitColor(16);
        setAccentSixBitColor(24);
        setHighlightSixBitColor(32);
        setBaseSixBitColor(40);

        setBackgroundStyle(Style.ACCENT_BASE);

        setHourHandShape(HandShape.DIAMOND);
        setHourHandLength(HandLength.LONG);
        setHourHandThickness(HandThickness.REGULAR);
        setHourHandStalk(HandStalk.SHORT);
        setHourHandCutout(HandCutout.HAND_STALK);
        setHourHandStyle(Style.FILL_HIGHLIGHT);

        setMinuteHandOverride(true);
        setMinuteHandShape(HandShape.DIAMOND);
        setMinuteHandLength(HandLength.LONG);
        setMinuteHandThickness(HandThickness.REGULAR);
        setMinuteHandStalk(HandStalk.SHORT);
        setMinuteHandCutout(HandCutout.HAND_STALK);
        setMinuteHandStyle(Style.FILL_HIGHLIGHT);

        setSecondHandOverride(false);
        setSecondHandShape(HandShape.STRAIGHT);
        setSecondHandLength(HandLength.SHORT);
        setSecondHandThickness(HandThickness.THIN);
        setSecondHandStyle(Style.FILL);

        setTicksDisplay(TicksDisplay.FOUR_TWELVE_60);

        setFourTickShape(TickShape.BAR);
        setFourTickLength(TickLength.MEDIUM);
        setFourTickThickness(TickThickness.THIN);
        setFourTickRadiusPosition(TickRadiusPosition.X_LONG);
        setFourTickStyle(Style.ACCENT_HIGHLIGHT);

        setTwelveTickOverride(false);
        setTwelveTickShape(TickShape.BAR);
        setTwelveTickLength(TickLength.MEDIUM);
        setTwelveTickThickness(TickThickness.THIN);
        setTwelveTickRadiusPosition(TickRadiusPosition.X_LONG);
        setTwelveTickStyle(Style.ACCENT_HIGHLIGHT);

        setSixtyTickOverride(false);
        setSixtyTickShape(TickShape.BAR);
        setSixtyTickLength(TickLength.MEDIUM);
        setSixtyTickThickness(TickThickness.THIN);
        setSixtyTickRadiusPosition(TickRadiusPosition.X_LONG);
        setSixtyTickStyle(Style.ACCENT_HIGHLIGHT);

        setFillHighlightStyle(GradientStyle.RADIAL_BRUSHED);
        setAccentFillStyle(GradientStyle.SWEEP);
        setAccentHighlightStyle(GradientStyle.SWEEP);
        setBaseAccentStyle(GradientStyle.SWEEP_BRUSHED);

//        mBytePacker.unitTest();

        pack();
    }

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

    void setMinuteHandOverride(boolean minuteHandOverride) {
        mMinuteHandOverride = minuteHandOverride;
    }

    boolean isMinuteHandOverridden() {
        return mMinuteHandOverride;
    }

    void setSecondHandOverride(boolean secondHandOverride) {
        mSecondHandOverride = secondHandOverride;
    }

    boolean isSecondHandOverridden() {
        return mSecondHandOverride;
    }

    void setTwelveTickOverride(boolean twelveTickOverride) {
        mTwelveTickOverride = twelveTickOverride;
    }

    boolean isTwelveTicksOverridden() {
        return isTwelveTicksVisible() && mTwelveTickOverride;
    }

    void setSixtyTickOverride(boolean sixtyTickOverride) {
        mSixtyTickOverride = sixtyTickOverride;
    }

    boolean isSixtyTicksOverridden() {
        return isSixtyTicksVisible() && mSixtyTickOverride;
    }

    public Style getBackgroundStyle() {
        return mBackgroundStyle;
    }

    void setBackgroundStyle(Style backgroundStyle) {
        mBackgroundStyle = backgroundStyle;
    }

    public HandShape getHourHandShape() {
        return mHourHandShape;
    }

    void setHourHandShape(HandShape hourHandShape) {
        mHourHandShape = hourHandShape;
    }

    public HandShape getMinuteHandShape() {
        return mMinuteHandOverride ? mMinuteHandShape : mHourHandShape;
    }

    void setMinuteHandShape(HandShape minuteHandShape) {
        mMinuteHandShape = minuteHandShape;
    }

    public HandShape getSecondHandShape() {
        // If not overridden, the default is just a plain and regular second hand.
        return mSecondHandOverride ? mSecondHandShape : HandShape.STRAIGHT;
    }

    void setSecondHandShape(HandShape secondHandShape) {
        mSecondHandShape = secondHandShape;
    }

    public HandLength getHourHandLength() {
        return mHourHandLength;
    }

    void setHourHandLength(HandLength hourHandLength) {
        mHourHandLength = hourHandLength;
    }

    public HandLength getMinuteHandLength() {
        return mMinuteHandOverride ? mMinuteHandLength : mHourHandLength;
    }

    void setMinuteHandLength(HandLength minuteHandLength) {
        mMinuteHandLength = minuteHandLength;
    }

    public HandLength getSecondHandLength() {
        // If not overridden, the default is just a plain and regular second hand.
        return mSecondHandOverride ? mSecondHandLength : HandLength.LONG;
    }

    void setSecondHandLength(HandLength secondHandLength) {
        mSecondHandLength = secondHandLength;
    }

    public HandThickness getHourHandThickness() {
        return mHourHandThickness;
    }

    void setHourHandThickness(HandThickness hourHandThickness) {
        mHourHandThickness = hourHandThickness;
    }

    public HandThickness getMinuteHandThickness() {
        return mMinuteHandOverride ? mMinuteHandThickness : mHourHandThickness;
    }

    void setMinuteHandThickness(HandThickness minuteHandThickness) {
        mMinuteHandThickness = minuteHandThickness;
    }

    public HandThickness getSecondHandThickness() {
        // If not overridden, the default is just a plain and regular second hand.
        return mSecondHandOverride ? mSecondHandThickness : HandThickness.THIN;
    }

    void setSecondHandThickness(HandThickness secondHandThickness) {
        mSecondHandThickness = secondHandThickness;
    }

    public Style getHourHandStyle() {
        return mHourHandStyle;
    }

    void setHourHandStyle(Style hourHandStyle) {
        mHourHandStyle = hourHandStyle;
    }

    public Style getMinuteHandStyle() {
        return mMinuteHandOverride ? mMinuteHandStyle : mHourHandStyle;
    }

    void setMinuteHandStyle(Style minuteHandStyle) {
        mMinuteHandStyle = minuteHandStyle;
    }

    public Style getSecondHandStyle() {
        // If not overridden, the default is just a plain and regular second hand.
        return mSecondHandOverride ? mSecondHandStyle : Style.HIGHLIGHT;
    }

    void setSecondHandStyle(Style secondHandStyle) {
        mSecondHandStyle = secondHandStyle;
    }

    TicksDisplay getTicksDisplay() {
        return mTicksDisplay;
    }

    void setTicksDisplay(TicksDisplay ticksDisplay) {
        mTicksDisplay = ticksDisplay;
    }

    public boolean isFourTicksVisible() {
        return mTicksDisplay != TicksDisplay.NONE;
    }

    public boolean isTwelveTicksVisible() {
        return mTicksDisplay == TicksDisplay.FOUR_TWELVE ||
                mTicksDisplay == TicksDisplay.FOUR_TWELVE_60;
    }

    public boolean isSixtyTicksVisible() {
        return mTicksDisplay == TicksDisplay.FOUR_TWELVE_60;
    }

    public TickShape getFourTickShape() {
        return mFourTickShape;
    }

    void setFourTickShape(TickShape fourTickShape) {
        mFourTickShape = fourTickShape;
    }

    public TickShape getTwelveTickShape() {
        return mTwelveTickOverride ? mTwelveTickShape : mFourTickShape;
    }

    void setTwelveTickShape(TickShape twelveTickShape) {
        mTwelveTickShape = twelveTickShape;
    }

    public TickShape getSixtyTickShape() {
        return mSixtyTickOverride ? mSixtyTickShape : mFourTickShape;
    }

    void setSixtyTickShape(TickShape sixtyTickShape) {
        mSixtyTickShape = sixtyTickShape;
    }

    public TickLength getFourTickLength() {
        return mFourTickLength;
    }

    void setFourTickLength(TickLength fourTickLength) {
        mFourTickLength = fourTickLength;
    }

    public TickLength getTwelveTickLength() {
        return mTwelveTickOverride ? mTwelveTickLength : mFourTickLength;
    }

    void setTwelveTickLength(TickLength twelveTickLength) {
        mTwelveTickLength = twelveTickLength;
    }

    public TickLength getSixtyTickLength() {
        return mSixtyTickOverride ? mSixtyTickLength : mFourTickLength;
    }

    void setSixtyTickLength(TickLength sixtyTickLength) {
        mSixtyTickLength = sixtyTickLength;
    }

    public TickThickness getFourTickThickness() {
        return mFourTickThickness;
    }

    void setFourTickThickness(TickThickness fourTickThickness) {
        mFourTickThickness = fourTickThickness;
    }

    public TickThickness getTwelveTickThickness() {
        return mTwelveTickOverride ? mTwelveTickThickness : mFourTickThickness;
    }

    void setTwelveTickThickness(TickThickness twelveTickThickness) {
        mTwelveTickThickness = twelveTickThickness;
    }

    public TickThickness getSixtyTickThickness() {
        return mSixtyTickOverride ? mSixtyTickThickness : mFourTickThickness;
    }

    void setSixtyTickThickness(TickThickness sixtyTickThickness) {
        mSixtyTickThickness = sixtyTickThickness;
    }

    public TickRadiusPosition getFourTickRadiusPosition() {
        return mFourTickRadiusPosition;
    }

    void setFourTickRadiusPosition(TickRadiusPosition fourTickRadiusPosition) {
        mFourTickRadiusPosition = fourTickRadiusPosition;
    }

    public TickRadiusPosition getTwelveTickRadiusPosition() {
        return mTwelveTickOverride ? mTwelveTickRadiusPosition : mFourTickRadiusPosition;
    }

    void setTwelveTickRadiusPosition(TickRadiusPosition twelveTickRadiusPosition) {
        mTwelveTickRadiusPosition = twelveTickRadiusPosition;
    }

    public TickRadiusPosition getSixtyTickRadiusPosition() {
        return mSixtyTickOverride ? mSixtyTickRadiusPosition : mFourTickRadiusPosition;
    }

    void setSixtyTickRadiusPosition(TickRadiusPosition sixtyTickRadiusPosition) {
        mSixtyTickRadiusPosition = sixtyTickRadiusPosition;
    }

    public Style getFourTickStyle() {
        return mFourTickStyle;
    }

    void setFourTickStyle(Style fourTickStyle) {
        mFourTickStyle = fourTickStyle;
    }

    public Style getTwelveTickStyle() {
        return mTwelveTickOverride ? mTwelveTickStyle : mFourTickStyle;
    }

    void setTwelveTickStyle(Style twelveTickStyle) {
        mTwelveTickStyle = twelveTickStyle;
    }

    public Style getSixtyTickStyle() {
        return mSixtyTickOverride ? mSixtyTickStyle : mFourTickStyle;
    }

    void setSixtyTickStyle(Style sixtyTickStyle) {
        mSixtyTickStyle = sixtyTickStyle;
    }

    GradientStyle getFillHighlightStyle() {
        return mFillHighlightStyle;
    }

    void setFillHighlightStyle(GradientStyle fillHighlightStyle) {
        mFillHighlightStyle = fillHighlightStyle;
    }

    GradientStyle getAccentFillStyle() {
        return mAccentFillStyle;
    }

    void setAccentFillStyle(GradientStyle accentFillStyle) {
        mAccentFillStyle = accentFillStyle;
    }

    GradientStyle getAccentHighlightStyle() {
        return mAccentHighlightStyle;
    }

    void setAccentHighlightStyle(GradientStyle accentHighlightStyle) {
        mAccentHighlightStyle = accentHighlightStyle;
    }

    GradientStyle getBaseAccentStyle() {
        return mBaseAccentStyle;
    }

    void setBaseAccentStyle(GradientStyle baseAccentStyle) {
        mBaseAccentStyle = baseAccentStyle;
    }

    public HandStalk getHourHandStalk() {
        return mHourHandStalk;
    }

    void setHourHandStalk(HandStalk hourHandStalk) {
        mHourHandStalk = hourHandStalk;
    }

    public HandStalk getMinuteHandStalk() {
        return mMinuteHandOverride ? mMinuteHandStalk : mHourHandStalk;
    }

    void setMinuteHandStalk(HandStalk minuteHandStalk) {
        mMinuteHandStalk = minuteHandStalk;
    }

    public HandCutout getHourHandCutout() {
        return mHourHandCutout;
    }

    void setHourHandCutout(HandCutout hourHandCutout) {
        mHourHandCutout = hourHandCutout;
    }

    public HandCutout getMinuteHandCutout() {
        return mMinuteHandOverride ? mMinuteHandCutout : mHourHandCutout;
    }

    void setMinuteHandCutout(HandCutout minuteHandCutout) {
        mMinuteHandCutout = minuteHandCutout;
    }

    int getFillSixBitColor() {
        return mFillSixBitColor;
    }

    void setFillSixBitColor(int fillSixBitColor) {
        mFillSixBitColor = fillSixBitColor;
    }

    int getAccentSixBitColor() {
        return mAccentSixBitColor;
    }

    void setAccentSixBitColor(int accentSixBitColor) {
        mAccentSixBitColor = accentSixBitColor;
    }

    int getHighlightSixBitColor() {
        return mHighlightSixBitColor;
    }

    void setHighlightSixBitColor(int highlightSixBitColor) {
        mHighlightSixBitColor = highlightSixBitColor;
    }

    int getBaseSixBitColor() {
        return mBaseSixBitColor;
    }

    void setBaseSixBitColor(int baseSixBitColor) {
        mBaseSixBitColor = baseSixBitColor;
    }

}
