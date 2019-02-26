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

public final class WatchFacePreset {
    private BytePacker bytePacker = new BytePacker(16);
    private Style backgroundStyle;
    private boolean minuteHandOverride;
    private boolean secondHandOverride;
    private HandShape hourHandShape, minuteHandShape, secondHandShape;
    private HandLength hourHandLength, minuteHandLength, secondHandLength;
    private HandThickness hourHandThickness, minuteHandThickness, secondHandThickness;
    private HandStalk hourHandStalk;
    private HandStalk minuteHandStalk;
    private Style hourHandStyle, minuteHandStyle, secondHandStyle;
    private boolean twelveTickOverride, sixtyTickOverride;
    private boolean twelveTickHidden, sixtyTickHidden;
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

        setSixBitColor(ColorType.FILL, 16);
        setSixBitColor(ColorType.ACCENT, 24);
        setSixBitColor(ColorType.HIGHLIGHT, 32);
        setSixBitColor(ColorType.BASE, 40);

        setBackgroundStyle(Style.ACCENT_BASE);

        setHourHandShape(HandShape.DIAMOND);
        setHourHandLength(HandLength.LONG);
        setHourHandThickness(HandThickness.REGULAR);
        setHourHandStalk(HandStalk.SHORT);
        setHourHandStyle(Style.FILL_HIGHLIGHT);

        setMinuteHandOverride(true);
        setMinuteHandShape(HandShape.DIAMOND);
        setMinuteHandLength(HandLength.LONG);
        setMinuteHandThickness(HandThickness.REGULAR);
        setMinuteHandStalk(HandStalk.SHORT);
        setMinuteHandStyle(Style.FILL_HIGHLIGHT);

        setSecondHandOverride(false);
        setSecondHandShape(HandShape.STRAIGHT);
        setSecondHandLength(HandLength.SHORT);
        setSecondHandThickness(HandThickness.THIN);
        setSecondHandStyle(Style.FILL);

        setFourTickShape(TickShape.BAR);
        setFourTickLength(TickLength.MEDIUM);
        setFourTickThickness(TickThickness.THIN);
        setFourTickRadiusPosition(TickRadiusPosition.X_LONG);
        setFourTickStyle(Style.ACCENT_HIGHLIGHT);

        setTwelveTickHidden(false);
        setTwelveTickOverride(false);
        setTwelveTickShape(TickShape.BAR);
        setTwelveTickLength(TickLength.MEDIUM);
        setTwelveTickThickness(TickThickness.THIN);
        setTwelveTickRadiusPosition(TickRadiusPosition.X_LONG);
        setTwelveTickStyle(Style.ACCENT_HIGHLIGHT);

        setSixtyTickHidden(false);
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
        if (s == null || s == "") return;
        try {
            bytePacker.setStringFast(s);
            unpack();
        } catch (java.lang.StringIndexOutOfBoundsException e) {
            Log.d("AnalogWatchFace", "It failed: " + s);
            Log.d("AnalogWatchFace", "It failed: " + e.toString());
        }
    }

    public void pack() {
        bytePacker.rewind();

        backgroundStyle.pack(bytePacker);

        hourHandShape.pack(bytePacker);
        hourHandLength.pack(bytePacker);
        hourHandThickness.pack(bytePacker);
        hourHandStalk.pack(bytePacker);
        hourHandStyle.pack(bytePacker);

        // TODO: Fix minuteHandOverride and secondHandOverride
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

        fourTickShape.pack(bytePacker);
        fourTickLength.pack(bytePacker);
        fourTickThickness.pack(bytePacker);
        fourTickRadiusPosition.pack(bytePacker);
        fourTickStyle.pack(bytePacker);

        bytePacker.put(twelveTickHidden);
        bytePacker.put(twelveTickOverride);
        twelveTickShape.pack(bytePacker);
        twelveTickLength.pack(bytePacker);
        twelveTickThickness.pack(bytePacker);
        twelveTickRadiusPosition.pack(bytePacker);
        twelveTickStyle.pack(bytePacker);

        bytePacker.put(sixtyTickHidden);
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

        //String s = bytePacker.getString();
    }

    public void unpack() {
        bytePacker.rewind();

        backgroundStyle = Style.unpack(bytePacker);

        hourHandShape = HandShape.unpack(bytePacker);
        hourHandLength = HandLength.unpack(bytePacker);
        hourHandThickness = HandThickness.unpack(bytePacker);
        hourHandStalk = HandStalk.unpack(bytePacker);
        hourHandStyle = Style.unpack(bytePacker);

        // TODO: Fix minuteHandOverride and secondHandOverride
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

        fourTickShape = TickShape.unpack(bytePacker);
        fourTickLength = TickLength.unpack(bytePacker);
        fourTickThickness = TickThickness.unpack(bytePacker);
        fourTickRadiusPosition = TickRadiusPosition.unpack(bytePacker);
        fourTickStyle = Style.unpack(bytePacker);

        twelveTickHidden = bytePacker.getBoolean();
        twelveTickOverride = bytePacker.getBoolean();
        twelveTickShape = TickShape.unpack(bytePacker);
        twelveTickLength = TickLength.unpack(bytePacker);
        twelveTickThickness = TickThickness.unpack(bytePacker);
        twelveTickRadiusPosition = TickRadiusPosition.unpack(bytePacker);
        twelveTickStyle = Style.unpack(bytePacker);

        sixtyTickHidden = bytePacker.getBoolean();
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
    }

//    public void setPalette(
//            int fillColor, int accentColor, int highlightColor, int baseColor) {
//        setFillSixBitColor(fillColor);
//        setAccentSixBitColor(accentColor);
//        setHighlightSixBitColor(highlightColor);
//        setBaseSixBitColor(baseColor);
//    }

    public void setMinuteHandOverride(boolean minuteHandOverride) {
        this.minuteHandOverride = minuteHandOverride;
    }

    public void setSecondHandOverride(boolean secondHandOverride) {
        this.secondHandOverride = secondHandOverride;
    }

    public void setTwelveTickOverride(boolean twelveTickOverride) {
        this.twelveTickOverride = twelveTickOverride;
    }

    public void setSixtyTickOverride(boolean sixtyTickOverride) {
        this.sixtyTickOverride = sixtyTickOverride;
    }

    public Style getBackgroundStyle() {
        return backgroundStyle;
    }

    public void setBackgroundStyle(Style backgroundStyle) {
        this.backgroundStyle = backgroundStyle;
    }

    public HandShape getHourHandShape() {
        return hourHandShape;
    }

    public void setHourHandShape(HandShape hourHandShape) {
        this.hourHandShape = hourHandShape;
    }

    public HandShape getMinuteHandShape() {
        return minuteHandOverride ? minuteHandShape : hourHandShape;
    }

    public void setMinuteHandShape(HandShape minuteHandShape) {
        this.minuteHandShape = minuteHandShape;
    }

    public HandShape getSecondHandShape() {
        // If not overridden, the default is just a plain and regular second hand.
        return secondHandOverride ? secondHandShape : HandShape.STRAIGHT;
    }

    public void setSecondHandShape(HandShape secondHandShape) {
        this.secondHandShape = secondHandShape;
    }

    public HandLength getHourHandLength() {
        return hourHandLength;
    }

    public void setHourHandLength(HandLength hourHandLength) {
        this.hourHandLength = hourHandLength;
    }

    public HandLength getMinuteHandLength() {
        return minuteHandOverride ? minuteHandLength : hourHandLength;
    }

    public void setMinuteHandLength(HandLength minuteHandLength) {
        this.minuteHandLength = minuteHandLength;
    }

    public HandLength getSecondHandLength() {
        // If not overridden, the default is just a plain and regular second hand.
        return secondHandOverride ? secondHandLength : HandLength.LONG;
    }

    public void setSecondHandLength(HandLength secondHandLength) {
        this.secondHandLength = secondHandLength;
    }

    public HandThickness getHourHandThickness() {
        return hourHandThickness;
    }

    public void setHourHandThickness(HandThickness hourHandThickness) {
        this.hourHandThickness = hourHandThickness;
    }

    public HandThickness getMinuteHandThickness() {
        return minuteHandOverride ? minuteHandThickness : hourHandThickness;
    }

    public void setMinuteHandThickness(HandThickness minuteHandThickness) {
        this.minuteHandThickness = minuteHandThickness;
    }

    public HandThickness getSecondHandThickness() {
        // If not overridden, the default is just a plain and regular second hand.
        return secondHandOverride ? secondHandThickness : HandThickness.THIN;
    }

    public void setSecondHandThickness(HandThickness secondHandThickness) {
        this.secondHandThickness = secondHandThickness;
    }

    public Style getHourHandStyle() {
        return hourHandStyle;
    }

    public void setHourHandStyle(Style hourHandStyle) {
        this.hourHandStyle = hourHandStyle;
    }

    public Style getMinuteHandStyle() {
        return minuteHandOverride ? minuteHandStyle : hourHandStyle;
    }

    public void setMinuteHandStyle(Style minuteHandStyle) {
        this.minuteHandStyle = minuteHandStyle;
    }

    public Style getSecondHandStyle() {
        // If not overridden, the default is just a plain and regular second hand.
        return secondHandOverride ? secondHandStyle : Style.HIGHLIGHT;
    }

    public void setSecondHandStyle(Style secondHandStyle) {
        this.secondHandStyle = secondHandStyle;
    }

    public boolean isTwelveTickHidden() {
        return twelveTickHidden;
    }

    public void setTwelveTickHidden(boolean twelveTickHidden) {
        this.twelveTickHidden = twelveTickHidden;
    }

    public boolean isSixtyTickHidden() {
        return sixtyTickHidden;
    }

    public void setSixtyTickHidden(boolean sixtyTickHidden) {
        this.sixtyTickHidden = sixtyTickHidden;
    }

    public TickShape getFourTickShape() {
        return fourTickShape;
    }

    public void setFourTickShape(TickShape fourTickShape) {
        this.fourTickShape = fourTickShape;
    }

    public TickShape getTwelveTickShape() {
        return twelveTickOverride ? twelveTickShape : fourTickShape;
    }

    public void setTwelveTickShape(TickShape twelveTickShape) {
        this.twelveTickShape = twelveTickShape;
    }

    public TickShape getSixtyTickShape() {
        return sixtyTickOverride ? sixtyTickShape : fourTickShape;
    }

    public void setSixtyTickShape(TickShape sixtyTickShape) {
        this.sixtyTickShape = sixtyTickShape;
    }

    public TickLength getFourTickLength() {
        return fourTickLength;
    }

    public void setFourTickLength(TickLength fourTickLength) {
        this.fourTickLength = fourTickLength;
    }

    public TickLength getTwelveTickLength() {
        return twelveTickOverride ? twelveTickLength : fourTickLength;
    }

    public void setTwelveTickLength(TickLength twelveTickLength) {
        this.twelveTickLength = twelveTickLength;
    }

    public TickLength getSixtyTickLength() {
        return sixtyTickOverride ? sixtyTickLength : fourTickLength;
    }

    public void setSixtyTickLength(TickLength sixtyTickLength) {
        this.sixtyTickLength = sixtyTickLength;
    }

    public TickThickness getFourTickThickness() {
        return fourTickThickness;
    }

    public void setFourTickThickness(TickThickness fourTickThickness) {
        this.fourTickThickness = fourTickThickness;
    }

    public TickThickness getTwelveTickThickness() {
        return twelveTickOverride ? twelveTickThickness : fourTickThickness;
    }

    public void setTwelveTickThickness(TickThickness twelveTickThickness) {
        this.twelveTickThickness = twelveTickThickness;
    }

    public TickThickness getSixtyTickThickness() {
        return sixtyTickOverride ? sixtyTickThickness : fourTickThickness;
    }

    public void setSixtyTickThickness(TickThickness sixtyTickThickness) {
        this.sixtyTickThickness = sixtyTickThickness;
    }

    public TickRadiusPosition getFourTickRadiusPosition() {
        return fourTickRadiusPosition;
    }

    public void setFourTickRadiusPosition(TickRadiusPosition fourTickRadiusPosition) {
        this.fourTickRadiusPosition = fourTickRadiusPosition;
    }

    public TickRadiusPosition getTwelveTickRadiusPosition() {
        return twelveTickOverride ? twelveTickRadiusPosition : twelveTickRadiusPosition;
    }

    public void setTwelveTickRadiusPosition(TickRadiusPosition twelveTickRadiusPosition) {
        this.twelveTickRadiusPosition = twelveTickRadiusPosition;
    }

    public TickRadiusPosition getSixtyTickRadiusPosition() {
        return sixtyTickOverride ? sixtyTickRadiusPosition : sixtyTickRadiusPosition;
    }

    public void setSixtyTickRadiusPosition(TickRadiusPosition sixtyTickRadiusPosition) {
        this.sixtyTickRadiusPosition = sixtyTickRadiusPosition;
    }

    public Style getFourTickStyle() {
        return fourTickStyle;
    }

    public void setFourTickStyle(Style fourTickStyle) {
        this.fourTickStyle = fourTickStyle;
    }

    public Style getTwelveTickStyle() {
        return twelveTickOverride ? twelveTickStyle : fourTickStyle;
    }

    public void setTwelveTickStyle(Style twelveTickStyle) {
        this.twelveTickStyle = twelveTickStyle;
    }

    public Style getSixtyTickStyle() {
        return sixtyTickOverride ? sixtyTickStyle : fourTickStyle;
    }

    public void setSixtyTickStyle(Style sixtyTickStyle) {
        this.sixtyTickStyle = sixtyTickStyle;
    }

    public GradientStyle getFillHighlightStyle() {
        return fillHighlightStyle;
    }

    public void setFillHighlightStyle(GradientStyle fillHighlightStyle) {
        this.fillHighlightStyle = fillHighlightStyle;
    }

    public GradientStyle getAccentFillStyle() {
        return accentFillStyle;
    }

    public void setAccentFillStyle(GradientStyle accentFillStyle) {
        this.accentFillStyle = accentFillStyle;
    }

    public GradientStyle getAccentHighlightStyle() {
        return accentHighlightStyle;
    }

    public void setAccentHighlightStyle(GradientStyle accentHighlightStyle) {
        this.accentHighlightStyle = accentHighlightStyle;
    }

    public GradientStyle getBaseAccentStyle() {
        return baseAccentStyle;
    }

    public void setBaseAccentStyle(GradientStyle baseAccentStyle) {
        this.baseAccentStyle = baseAccentStyle;
    }

    public HandStalk getHourHandStalk() {
        return hourHandStalk;
    }

    public void setHourHandStalk(HandStalk hourHandStalk) {
        this.hourHandStalk = hourHandStalk;
    }

    public HandStalk getMinuteHandStalk() {
        return minuteHandStalk;
    }

    public void setMinuteHandStalk(HandStalk minuteHandStalk) {
        this.minuteHandStalk = minuteHandStalk;
    }

    public int getSixBitColor(ColorType colorType) {
        switch (colorType) {
            case FILL:
                return mFillSixBitColor;
            case ACCENT:
                return mAccentSixBitColor;
            case HIGHLIGHT:
                return mHighlightSixBitColor;
            case BASE:
            default:
                return mBaseSixBitColor;
        }
    }

    public void setSixBitColor(ColorType colorType, int sixBitColor) {
        switch (colorType) {
            case FILL:
                this.mFillSixBitColor = sixBitColor;
                break;
            case ACCENT:
                this.mHighlightSixBitColor = sixBitColor;
                break;
            case HIGHLIGHT:
                this.mHighlightSixBitColor = sixBitColor;
                break;
            case BASE:
            default:
                this.mBaseSixBitColor = sixBitColor;
                break;
        }
    }

    public enum ColorType {FILL, ACCENT, HIGHLIGHT, BASE}

    public enum HandShape {
        STRAIGHT, ROUNDED, DIAMOND, UNKNOWN1;

        private static final int bits = 2;

        static HandShape unpack(BytePacker bytePacker) {
            return values()[bytePacker.get(bits)];
        }

        void pack(BytePacker bytePacker) {
            bytePacker.put(bits, values(), this);
        }
    }

    public enum HandLength {
        SHORT, MEDIUM, LONG, X_LONG;

        private static final int bits = 2;

        static HandLength unpack(BytePacker bytePacker) {
            return values()[bytePacker.get(bits)];
        }

        void pack(BytePacker bytePacker) {
            bytePacker.put(bits, values(), this);
        }
    }
    public enum HandThickness {
        THIN, REGULAR, THICK, X_THICK;

        private static final int bits = 2;

        static HandThickness unpack(BytePacker bytePacker) {
            return values()[bytePacker.get(bits)];
        }

        void pack(BytePacker bytePacker) {
            bytePacker.put(bits, values(), this);
        }
    }
    public enum HandStalk {
        NEGATIVE, NONE, SHORT, MEDIUM;

        private static final int bits = 2;

        static HandStalk unpack(BytePacker bytePacker) {
            return values()[bytePacker.get(bits)];
        }

        void pack(BytePacker bytePacker) {
            bytePacker.put(bits, values(), this);
        }
    }
    public enum TickShape {
        BAR, DOT, TRIANGLE, DIAMOND;

        private static final int bits = 2;

        static TickShape unpack(BytePacker bytePacker) {
            return values()[bytePacker.get(bits)];
        }

        void pack(BytePacker bytePacker) {
            bytePacker.put(bits, values(), this);
        }
    }

    public enum TickLength {
        SHORT, MEDIUM, LONG, X_LONG;

        private static final int bits = 2;

        static TickLength unpack(BytePacker bytePacker) {
            return values()[bytePacker.get(bits)];
        }

        void pack(BytePacker bytePacker) {
            bytePacker.put(bits, values(), this);
        }
    }

    public enum TickThickness {
        THIN, REGULAR, THICK, X_THICK;

        private static final int bits = 2;

        static TickThickness unpack(BytePacker bytePacker) {
            return values()[bytePacker.get(bits)];
        }

        void pack(BytePacker bytePacker) {
            bytePacker.put(bits, values(), this);
        }
    }
    public enum TickRadiusPosition {
        SHORT, MEDIUM, LONG, X_LONG;

        private static final int bits = 2;

        static TickRadiusPosition unpack(BytePacker bytePacker) {
            return values()[bytePacker.get(bits)];
        }

        void pack(BytePacker bytePacker) {
            bytePacker.put(bits, values(), this);
        }
    }

    public enum Style {
        FILL, ACCENT, HIGHLIGHT, BASE, FILL_HIGHLIGHT, ACCENT_FILL, ACCENT_HIGHLIGHT, ACCENT_BASE;

        private static final int bits = 3;

        static Style unpack(BytePacker bytePacker) {
            return values()[bytePacker.get(bits)];
        }

        void pack(BytePacker bytePacker) {
            bytePacker.put(bits, values(), this);
        }
    }
    public enum GradientStyle {
        SWEEP, SWEEP_BRUSHED, RADIAL, RADIAL_BRUSHED;

        private static final int bits = 2;

        static GradientStyle unpack(BytePacker bytePacker) {
            return values()[bytePacker.get(bits)];
        }

        void pack(BytePacker bytePacker) {
            bytePacker.put(bits, values(), this);
        }
    }
}
