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

import android.util.Log;

import androidx.annotation.ArrayRes;

import java.util.Objects;

import pro.watchkit.wearable.watchface.R;

public final class WatchFacePreset {
    private BytePacker bytePacker = new BytePacker();
    private Style backgroundStyle;
    private boolean minuteHandOverride;
    private boolean secondHandOverride;
    private HandShape hourHandShape, minuteHandShape, secondHandShape;
    private HandLength hourHandLength, minuteHandLength, secondHandLength;
    private HandThickness hourHandThickness, minuteHandThickness, secondHandThickness;
    private HandStalk hourHandStalk, minuteHandStalk;
    private HandCutout hourHandCutout, minuteHandCutout;
    private Style hourHandStyle, minuteHandStyle, secondHandStyle;
    private TicksDisplay ticksDisplay;
    private boolean twelveTickOverride, sixtyTickOverride;
    private TickShape fourTickShape, twelveTickShape, sixtyTickShape;
    private TickLength fourTickLength, twelveTickLength, sixtyTickLength;
    private TickThickness fourTickThickness, twelveTickThickness, sixtyTickThickness;
    private TickRadiusPosition fourTickRadiusPosition, twelveTickRadiusPosition, sixtyTickRadiusPosition;
    private Style fourTickStyle;
    private Style twelveTickStyle;
    private Style sixtyTickStyle;
    private int mFillSixBitColor;
    private int mAccentSixBitColor;
    private int mHighlightSixBitColor;
    private int mBaseSixBitColor;
    private GradientStyle fillHighlightStyle;
    private GradientStyle accentFillStyle;
    private GradientStyle accentHighlightStyle;
    private GradientStyle baseAccentStyle;

    public WatchFacePreset() {
//        bytePacker.setString("2a4c845ec530d34bffa86609f82f6407");
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

//        bytePacker.unitTest();

        pack();
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                super.hashCode(),
                backgroundStyle,
                minuteHandOverride,
                secondHandOverride,
                hourHandShape, minuteHandShape, secondHandShape,
                hourHandLength, minuteHandLength, secondHandLength,
                hourHandThickness, minuteHandThickness, secondHandThickness,
                hourHandStalk, minuteHandStalk,
                hourHandCutout, minuteHandCutout,
                hourHandStyle, minuteHandStyle, secondHandStyle,
                ticksDisplay,
                twelveTickOverride, sixtyTickOverride,
                fourTickShape, twelveTickShape, sixtyTickShape,
                fourTickLength, twelveTickLength, sixtyTickLength,
                fourTickThickness, twelveTickThickness, sixtyTickThickness,
                fourTickRadiusPosition, twelveTickRadiusPosition, sixtyTickRadiusPosition,
                fourTickStyle,
                twelveTickStyle,
                sixtyTickStyle,
                mFillSixBitColor,
                mAccentSixBitColor,
                mHighlightSixBitColor,
                mBaseSixBitColor,
                fillHighlightStyle,
                accentFillStyle,
                accentHighlightStyle,
                baseAccentStyle);
    }

    public WatchFacePreset clone() {
        // Clone this object by getting its string, creating
        // a new object, and setting its string to this one's.
        WatchFacePreset result = new WatchFacePreset();
        result.setString(getString());
        return result;
    }

    public String getString() {
        pack();
        return bytePacker.getStringFast();
    }

    public void setString(String s) {
        if (s == null || s.length() == 0) return;
        try {
            bytePacker.setStringFast(s);
            unpack();
        } catch (java.lang.StringIndexOutOfBoundsException e) {
            Log.d("AnalogWatchFace", "It failed: " + s);
            Log.d("AnalogWatchFace", "It failed: " + e.toString());
        }
    }

    private void pack() {
        bytePacker.rewind();

        backgroundStyle.pack(bytePacker);

        hourHandShape.pack(bytePacker);
        hourHandLength.pack(bytePacker);
        hourHandThickness.pack(bytePacker);
        hourHandStalk.pack(bytePacker);
        hourHandStyle.pack(bytePacker);

        bytePacker.put(minuteHandOverride);
        minuteHandShape.pack(bytePacker);
        minuteHandLength.pack(bytePacker);
        minuteHandThickness.pack(bytePacker);
        minuteHandStalk.pack(bytePacker);
        minuteHandStyle.pack(bytePacker);

        bytePacker.put(secondHandOverride);
        secondHandShape.pack(bytePacker);
        secondHandLength.pack(bytePacker);
        secondHandThickness.pack(bytePacker);
        secondHandStyle.pack(bytePacker);

        ticksDisplay.pack(bytePacker);

        fourTickShape.pack(bytePacker);
        fourTickLength.pack(bytePacker);
        fourTickThickness.pack(bytePacker);
        fourTickRadiusPosition.pack(bytePacker);
        fourTickStyle.pack(bytePacker);

        bytePacker.put(twelveTickOverride);
        twelveTickShape.pack(bytePacker);
        twelveTickLength.pack(bytePacker);
        twelveTickThickness.pack(bytePacker);
        twelveTickRadiusPosition.pack(bytePacker);
        twelveTickStyle.pack(bytePacker);

        bytePacker.put(sixtyTickOverride);
        sixtyTickShape.pack(bytePacker);
        sixtyTickLength.pack(bytePacker);
        sixtyTickThickness.pack(bytePacker);
        sixtyTickRadiusPosition.pack(bytePacker);
        sixtyTickStyle.pack(bytePacker);

        fillHighlightStyle.pack(bytePacker);
        accentFillStyle.pack(bytePacker);
        accentHighlightStyle.pack(bytePacker);
        baseAccentStyle.pack(bytePacker);

        bytePacker.putSixBitColor(mFillSixBitColor);
        bytePacker.putSixBitColor(mHighlightSixBitColor);
        bytePacker.putSixBitColor(mAccentSixBitColor);
        bytePacker.putSixBitColor(mBaseSixBitColor);

        // TODO: rearrange these into their right place
        hourHandCutout.pack(bytePacker);
        minuteHandCutout.pack(bytePacker);

        bytePacker.finish();
    }

    private void unpack() {
        bytePacker.rewind();

        backgroundStyle = Style.unpack(bytePacker);

        hourHandShape = HandShape.unpack(bytePacker);
        hourHandLength = HandLength.unpack(bytePacker);
        hourHandThickness = HandThickness.unpack(bytePacker);
        hourHandStalk = HandStalk.unpack(bytePacker);
        hourHandStyle = Style.unpack(bytePacker);

        minuteHandOverride = bytePacker.getBoolean();
        minuteHandShape = HandShape.unpack(bytePacker);
        minuteHandLength = HandLength.unpack(bytePacker);
        minuteHandThickness = HandThickness.unpack(bytePacker);
        minuteHandStalk = HandStalk.unpack(bytePacker);
        minuteHandStyle = Style.unpack(bytePacker);

        secondHandOverride = bytePacker.getBoolean();
        secondHandShape = HandShape.unpack(bytePacker);
        secondHandLength = HandLength.unpack(bytePacker);
        secondHandThickness = HandThickness.unpack(bytePacker);
        secondHandStyle = Style.unpack(bytePacker);

        ticksDisplay = TicksDisplay.unpack(bytePacker);

        fourTickShape = TickShape.unpack(bytePacker);
        fourTickLength = TickLength.unpack(bytePacker);
        fourTickThickness = TickThickness.unpack(bytePacker);
        fourTickRadiusPosition = TickRadiusPosition.unpack(bytePacker);
        fourTickStyle = Style.unpack(bytePacker);

        twelveTickOverride = bytePacker.getBoolean();
        twelveTickShape = TickShape.unpack(bytePacker);
        twelveTickLength = TickLength.unpack(bytePacker);
        twelveTickThickness = TickThickness.unpack(bytePacker);
        twelveTickRadiusPosition = TickRadiusPosition.unpack(bytePacker);
        twelveTickStyle = Style.unpack(bytePacker);

        sixtyTickOverride = bytePacker.getBoolean();
        sixtyTickShape = TickShape.unpack(bytePacker);
        sixtyTickLength = TickLength.unpack(bytePacker);
        sixtyTickThickness = TickThickness.unpack(bytePacker);
        sixtyTickRadiusPosition = TickRadiusPosition.unpack(bytePacker);
        sixtyTickStyle = Style.unpack(bytePacker);

        fillHighlightStyle = GradientStyle.unpack(bytePacker);
        accentFillStyle = GradientStyle.unpack(bytePacker);
        accentHighlightStyle = GradientStyle.unpack(bytePacker);
        baseAccentStyle = GradientStyle.unpack(bytePacker);

        mFillSixBitColor = bytePacker.getSixBitColor();
        mHighlightSixBitColor = bytePacker.getSixBitColor();
        mAccentSixBitColor = bytePacker.getSixBitColor();
        mBaseSixBitColor = bytePacker.getSixBitColor();

        // TODO: rearrange these into their right place
        hourHandCutout = HandCutout.unpack(bytePacker);
        minuteHandCutout = HandCutout.unpack(bytePacker);
    }

    void setMinuteHandOverride(boolean minuteHandOverride) {
        this.minuteHandOverride = minuteHandOverride;
    }

    boolean isMinuteHandOverridden() {
        return minuteHandOverride;
    }

    void setSecondHandOverride(boolean secondHandOverride) {
        this.secondHandOverride = secondHandOverride;
    }

    boolean isSecondHandOverridden() {
        return secondHandOverride;
    }

    void setTwelveTickOverride(boolean twelveTickOverride) {
        this.twelveTickOverride = twelveTickOverride;
    }

    boolean isTwelveTicksOverridden() {
        return isTwelveTicksVisible() && twelveTickOverride;
    }

    void setSixtyTickOverride(boolean sixtyTickOverride) {
        this.sixtyTickOverride = sixtyTickOverride;
    }

    boolean isSixtyTicksOverridden() {
        return isSixtyTicksVisible() && sixtyTickOverride;
    }

    public Style getBackgroundStyle() {
        return backgroundStyle;
    }

    private void setBackgroundStyle(Style backgroundStyle) {
        this.backgroundStyle = backgroundStyle;
    }

    public HandShape getHourHandShape() {
        return hourHandShape;
    }

    void setHourHandShape(HandShape hourHandShape) {
        this.hourHandShape = hourHandShape;
    }

    public HandShape getMinuteHandShape() {
        return minuteHandOverride ? minuteHandShape : hourHandShape;
    }

    void setMinuteHandShape(HandShape minuteHandShape) {
        this.minuteHandShape = minuteHandShape;
    }

    public HandShape getSecondHandShape() {
        // If not overridden, the default is just a plain and regular second hand.
        return secondHandOverride ? secondHandShape : HandShape.STRAIGHT;
    }

    void setSecondHandShape(HandShape secondHandShape) {
        this.secondHandShape = secondHandShape;
    }

    public HandLength getHourHandLength() {
        return hourHandLength;
    }

    void setHourHandLength(HandLength hourHandLength) {
        this.hourHandLength = hourHandLength;
    }

    public HandLength getMinuteHandLength() {
        return minuteHandOverride ? minuteHandLength : hourHandLength;
    }

    private void setMinuteHandLength(HandLength minuteHandLength) {
        this.minuteHandLength = minuteHandLength;
    }

    public HandLength getSecondHandLength() {
        // If not overridden, the default is just a plain and regular second hand.
        return secondHandOverride ? secondHandLength : HandLength.LONG;
    }

    void setSecondHandLength(HandLength secondHandLength) {
        this.secondHandLength = secondHandLength;
    }

    public HandThickness getHourHandThickness() {
        return hourHandThickness;
    }

    void setHourHandThickness(HandThickness hourHandThickness) {
        this.hourHandThickness = hourHandThickness;
    }

    public HandThickness getMinuteHandThickness() {
        return minuteHandOverride ? minuteHandThickness : hourHandThickness;
    }

    void setMinuteHandThickness(HandThickness minuteHandThickness) {
        this.minuteHandThickness = minuteHandThickness;
    }

    public HandThickness getSecondHandThickness() {
        // If not overridden, the default is just a plain and regular second hand.
        return secondHandOverride ? secondHandThickness : HandThickness.THIN;
    }

    void setSecondHandThickness(HandThickness secondHandThickness) {
        this.secondHandThickness = secondHandThickness;
    }

    public Style getHourHandStyle() {
        return hourHandStyle;
    }

    void setHourHandStyle(Style hourHandStyle) {
        this.hourHandStyle = hourHandStyle;
    }

    public Style getMinuteHandStyle() {
        return minuteHandOverride ? minuteHandStyle : hourHandStyle;
    }

    void setMinuteHandStyle(Style minuteHandStyle) {
        this.minuteHandStyle = minuteHandStyle;
    }

    public Style getSecondHandStyle() {
        // If not overridden, the default is just a plain and regular second hand.
        return secondHandOverride ? secondHandStyle : Style.HIGHLIGHT;
    }

    void setSecondHandStyle(Style secondHandStyle) {
        this.secondHandStyle = secondHandStyle;
    }

    void setTicksDisplay(TicksDisplay ticksDisplay) {
        this.ticksDisplay = ticksDisplay;
    }

    TicksDisplay getTicksDisplay() {
        return this.ticksDisplay;
    }

    public boolean isFourTicksVisible() {
        return this.ticksDisplay != TicksDisplay.NONE;
    }

    public boolean isTwelveTicksVisible() {
        return this.ticksDisplay == TicksDisplay.FOUR_TWELVE ||
                this.ticksDisplay == TicksDisplay.FOUR_TWELVE_60;
    }

    public boolean isSixtyTicksVisible() {
        return this.ticksDisplay == TicksDisplay.FOUR_TWELVE_60;
    }

    public enum TicksDisplay implements EnumResourceId {
        NONE, FOUR, FOUR_TWELVE, FOUR_TWELVE_60;

        private static final int bits = 2;

        static TicksDisplay unpack(BytePacker bytePacker) {
            return values()[bytePacker.get(bits)];
        }

        void pack(BytePacker bytePacker) {
            bytePacker.put(bits, values(), this);
        }

        @Override
        @ArrayRes
        public int getNameResourceId() {
            return R.array.WatchFacePreset_TicksDisplay;
        }
    }

    public TickShape getFourTickShape() {
        return fourTickShape;
    }

    void setFourTickShape(TickShape fourTickShape) {
        this.fourTickShape = fourTickShape;
    }

    public TickShape getTwelveTickShape() {
        return twelveTickOverride ? twelveTickShape : fourTickShape;
    }

    void setTwelveTickShape(TickShape twelveTickShape) {
        this.twelveTickShape = twelveTickShape;
    }

    public TickShape getSixtyTickShape() {
        return sixtyTickOverride ? sixtyTickShape : fourTickShape;
    }

    void setSixtyTickShape(TickShape sixtyTickShape) {
        this.sixtyTickShape = sixtyTickShape;
    }

    public TickLength getFourTickLength() {
        return fourTickLength;
    }

    void setFourTickLength(TickLength fourTickLength) {
        this.fourTickLength = fourTickLength;
    }

    public TickLength getTwelveTickLength() {
        return twelveTickOverride ? twelveTickLength : fourTickLength;
    }

    void setTwelveTickLength(TickLength twelveTickLength) {
        this.twelveTickLength = twelveTickLength;
    }

    public TickLength getSixtyTickLength() {
        return sixtyTickOverride ? sixtyTickLength : fourTickLength;
    }

    void setSixtyTickLength(TickLength sixtyTickLength) {
        this.sixtyTickLength = sixtyTickLength;
    }

    public TickThickness getFourTickThickness() {
        return fourTickThickness;
    }

    void setFourTickThickness(TickThickness fourTickThickness) {
        this.fourTickThickness = fourTickThickness;
    }

    public TickThickness getTwelveTickThickness() {
        return twelveTickOverride ? twelveTickThickness : fourTickThickness;
    }

    void setTwelveTickThickness(TickThickness twelveTickThickness) {
        this.twelveTickThickness = twelveTickThickness;
    }

    public TickThickness getSixtyTickThickness() {
        return sixtyTickOverride ? sixtyTickThickness : fourTickThickness;
    }

    void setSixtyTickThickness(TickThickness sixtyTickThickness) {
        this.sixtyTickThickness = sixtyTickThickness;
    }

    public TickRadiusPosition getFourTickRadiusPosition() {
        return fourTickRadiusPosition;
    }

    void setFourTickRadiusPosition(TickRadiusPosition fourTickRadiusPosition) {
        this.fourTickRadiusPosition = fourTickRadiusPosition;
    }

    public TickRadiusPosition getTwelveTickRadiusPosition() {
        return twelveTickOverride ? twelveTickRadiusPosition : fourTickRadiusPosition;
    }

    void setTwelveTickRadiusPosition(TickRadiusPosition twelveTickRadiusPosition) {
        this.twelveTickRadiusPosition = twelveTickRadiusPosition;
    }

    public TickRadiusPosition getSixtyTickRadiusPosition() {
        return sixtyTickOverride ? sixtyTickRadiusPosition : fourTickRadiusPosition;
    }

    void setSixtyTickRadiusPosition(TickRadiusPosition sixtyTickRadiusPosition) {
        this.sixtyTickRadiusPosition = sixtyTickRadiusPosition;
    }

    public Style getFourTickStyle() {
        return fourTickStyle;
    }

    void setFourTickStyle(Style fourTickStyle) {
        this.fourTickStyle = fourTickStyle;
    }

    public Style getTwelveTickStyle() {
        return twelveTickOverride ? twelveTickStyle : fourTickStyle;
    }

    void setTwelveTickStyle(Style twelveTickStyle) {
        this.twelveTickStyle = twelveTickStyle;
    }

    public Style getSixtyTickStyle() {
        return sixtyTickOverride ? sixtyTickStyle : fourTickStyle;
    }

    void setSixtyTickStyle(Style sixtyTickStyle) {
        this.sixtyTickStyle = sixtyTickStyle;
    }

    GradientStyle getFillHighlightStyle() {
        return fillHighlightStyle;
    }

    void setFillHighlightStyle(GradientStyle fillHighlightStyle) {
        this.fillHighlightStyle = fillHighlightStyle;
    }

    GradientStyle getAccentFillStyle() {
        return accentFillStyle;
    }

    void setAccentFillStyle(GradientStyle accentFillStyle) {
        this.accentFillStyle = accentFillStyle;
    }

    GradientStyle getAccentHighlightStyle() {
        return accentHighlightStyle;
    }

    void setAccentHighlightStyle(GradientStyle accentHighlightStyle) {
        this.accentHighlightStyle = accentHighlightStyle;
    }

    GradientStyle getBaseAccentStyle() {
        return baseAccentStyle;
    }

    void setBaseAccentStyle(GradientStyle baseAccentStyle) {
        this.baseAccentStyle = baseAccentStyle;
    }

    public HandStalk getHourHandStalk() {
        return hourHandStalk;
    }

    void setHourHandStalk(HandStalk hourHandStalk) {
        this.hourHandStalk = hourHandStalk;
    }

    public HandStalk getMinuteHandStalk() {
        return minuteHandOverride ? minuteHandStalk : hourHandStalk;
    }

    void setMinuteHandStalk(HandStalk minuteHandStalk) {
        this.minuteHandStalk = minuteHandStalk;
    }

    public HandCutout getHourHandCutout() {
        return hourHandCutout;
    }

    void setHourHandCutout(HandCutout hourHandCutout) {
        this.hourHandCutout = hourHandCutout;
    }

    public HandCutout getMinuteHandCutout() {
        return minuteHandOverride ? minuteHandCutout : hourHandCutout;
    }

    void setMinuteHandCutout(HandCutout minuteHandCutout) {
        this.minuteHandCutout = minuteHandCutout;
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

    public enum HandShape implements EnumResourceId {
        STRAIGHT, ROUNDED, DIAMOND, UNKNOWN1;

        private static final int bits = 2;

        static HandShape unpack(BytePacker bytePacker) {
            return values()[bytePacker.get(bits)];
        }

        void pack(BytePacker bytePacker) {
            bytePacker.put(bits, values(), this);
        }

        @Override
        @ArrayRes
        public int getNameResourceId() {
            return R.array.WatchFacePreset_HandShape;
        }
    }

    public enum HandLength implements EnumResourceId {
        SHORT, MEDIUM, LONG, X_LONG;

        private static final int bits = 2;

        static HandLength unpack(BytePacker bytePacker) {
            return values()[bytePacker.get(bits)];
        }

        void pack(BytePacker bytePacker) {
            bytePacker.put(bits, values(), this);
        }

        @Override
        @ArrayRes
        public int getNameResourceId() {
            return R.array.WatchFacePreset_HandLength;
        }
    }

    public enum HandThickness implements EnumResourceId {
        THIN, REGULAR, THICK, X_THICK;

        private static final int bits = 2;

        static HandThickness unpack(BytePacker bytePacker) {
            return values()[bytePacker.get(bits)];
        }

        void pack(BytePacker bytePacker) {
            bytePacker.put(bits, values(), this);
        }

        @Override
        @ArrayRes
        public int getNameResourceId() {
            return R.array.WatchFacePreset_HandThickness;
        }
    }

    public enum HandStalk implements EnumResourceId {
        NEGATIVE, NONE, SHORT, MEDIUM;

        private static final int bits = 2;

        static HandStalk unpack(BytePacker bytePacker) {
            return values()[bytePacker.get(bits)];
        }

        void pack(BytePacker bytePacker) {
            bytePacker.put(bits, values(), this);
        }

        @Override
        @ArrayRes
        public int getNameResourceId() {
            return R.array.WatchFacePreset_HandStalk;
        }
    }

    public enum HandCutout implements EnumResourceId {
        NONE, HAND, STALK, HAND_STALK;

        private static final int bits = 2;

        static HandCutout unpack(BytePacker bytePacker) {
            return values()[bytePacker.get(bits)];
        }

        void pack(BytePacker bytePacker) {
            bytePacker.put(bits, values(), this);
        }

        @Override
        @ArrayRes
        public int getNameResourceId() {
            return R.array.WatchFacePreset_HandCutout;
        }
    }

    public enum TickShape implements EnumResourceId {
        BAR, DOT, TRIANGLE, DIAMOND;

        private static final int bits = 2;

        static TickShape unpack(BytePacker bytePacker) {
            return values()[bytePacker.get(bits)];
        }

        void pack(BytePacker bytePacker) {
            bytePacker.put(bits, values(), this);
        }

        @Override
        @ArrayRes
        public int getNameResourceId() {
            return R.array.WatchFacePreset_TickShape;
        }
    }

    public enum TickLength implements EnumResourceId {
        SHORT, MEDIUM, LONG, X_LONG;

        private static final int bits = 2;

        static TickLength unpack(BytePacker bytePacker) {
            return values()[bytePacker.get(bits)];
        }

        void pack(BytePacker bytePacker) {
            bytePacker.put(bits, values(), this);
        }

        @Override
        @ArrayRes
        public int getNameResourceId() {
            return R.array.WatchFacePreset_TickLength;
        }
    }

    public enum TickThickness implements EnumResourceId {
        THIN, REGULAR, THICK, X_THICK;

        private static final int bits = 2;

        static TickThickness unpack(BytePacker bytePacker) {
            return values()[bytePacker.get(bits)];
        }

        void pack(BytePacker bytePacker) {
            bytePacker.put(bits, values(), this);
        }

        @Override
        @ArrayRes
        public int getNameResourceId() {
            return R.array.WatchFacePreset_TickThickness;
        }
    }

    public enum TickRadiusPosition implements EnumResourceId {
        SHORT, MEDIUM, LONG, X_LONG;

        private static final int bits = 2;

        static TickRadiusPosition unpack(BytePacker bytePacker) {
            return values()[bytePacker.get(bits)];
        }

        void pack(BytePacker bytePacker) {
            bytePacker.put(bits, values(), this);
        }

        @Override
        @ArrayRes
        public int getNameResourceId() {
            return R.array.WatchFacePreset_TickRadiusPosition;
        }
    }

    public enum Style implements EnumResourceId {
        FILL, ACCENT, HIGHLIGHT, BASE, FILL_HIGHLIGHT, ACCENT_FILL, ACCENT_HIGHLIGHT, ACCENT_BASE;

        private static final int bits = 3;

        static Style unpack(BytePacker bytePacker) {
            return values()[bytePacker.get(bits)];
        }

        void pack(BytePacker bytePacker) {
            bytePacker.put(bits, values(), this);
        }

        @Override
        @ArrayRes
        public int getNameResourceId() {
            return R.array.WatchFacePreset_Style;
        }
    }

    public enum GradientStyle implements EnumResourceId {
        SWEEP, SWEEP_BRUSHED, RADIAL, RADIAL_BRUSHED;

        private static final int bits = 2;

        static GradientStyle unpack(BytePacker bytePacker) {
            return values()[bytePacker.get(bits)];
        }

        void pack(BytePacker bytePacker) {
            bytePacker.put(bits, values(), this);
        }

        @Override
        @ArrayRes
        public int getNameResourceId() {
            return R.array.WatchFacePreset_GradientStyle;
        }
    }

    public interface EnumResourceId {
        @ArrayRes
        int getNameResourceId();
    }
}
