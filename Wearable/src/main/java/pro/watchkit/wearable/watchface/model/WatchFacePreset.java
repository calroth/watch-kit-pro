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

public final class WatchFacePreset {
    private BytePacker bytePacker = new BytePacker(16);
    private Palette backgroundPalette;
    private boolean minuteHandOverride;
    private boolean secondHandOverride;
    private HandShape hourHandShape, minuteHandShape, secondHandShape;
    private HandLength hourHandLength, minuteHandLength, secondHandLength;
    private HandThickness hourHandThickness, minuteHandThickness, secondHandThickness;
    private HandStalk hourHandStalk;
    private HandStalk minuteHandStalk;
    private Palette hourHandPalette, minuteHandPalette, secondHandPalette;
    private boolean twelveTickOverride, sixtyTickOverride;
    private boolean twelveTickHidden, sixtyTickHidden;
    private TickShape fourTickShape, twelveTickShape, sixtyTickShape;
    private TickLength fourTickLength, twelveTickLength, sixtyTickLength;
    private TickThickness fourTickThickness, twelveTickThickness, sixtyTickThickness;
    private TickRadiusPosition fourTickRadiusPosition, twelveTickRadiusPosition, sixtyTickRadiusPosition;
    private Palette fourTickPalette;
    private Palette twelveTickPalette;
    private Palette sixtyTickPalette;
    private int mFillColor;
    private int mAccentColor;
    private int mHighlightColor;
    private int mBaseColor;
    private GradientStyle fillHighlightStyle;
    private GradientStyle accentFillStyle;
    private GradientStyle accentHighlightStyle;
    private GradientStyle baseAccentStyle;

    public WatchFacePreset() {
//        bytePacker.setString("2a4c845ec530d34bffa86609f82f6407");
//        unpack();

        setFillColor(-1);
        setAccentColor(-10011977);
        setHighlightColor(-43230);
        setBaseColor(-16777216);

        setBackgroundPalette(Palette.ACCENT_BASE);

        setHourHandShape(HandShape.DIAMOND);
        setHourHandLength(HandLength.LONG);
        setHourHandThickness(HandThickness.REGULAR);
        setHourHandStalk(HandStalk.SHORT);
        setHourHandPalette(Palette.FILL_HIGHLIGHT);
        setMinuteHandOverride(true);
        setMinuteHandShape(HandShape.DIAMOND);
        setMinuteHandLength(HandLength.LONG);
        setMinuteHandThickness(HandThickness.REGULAR);
        setMinuteHandStalk(HandStalk.SHORT);
        setMinuteHandPalette(Palette.FILL_HIGHLIGHT);
        setSecondHandOverride(false);
        setSecondHandShape(HandShape.STRAIGHT);
        setSecondHandLength(HandLength.SHORT);
        setSecondHandThickness(HandThickness.THIN);
        setSecondHandPalette(Palette.FILL);

        setFourTickShape(TickShape.BAR);
        setFourTickLength(TickLength.MEDIUM);
        setFourTickThickness(TickThickness.THIN);
        setFourTickRadiusPosition(TickRadiusPosition.X_LONG);
        setFourTickPalette(Palette.ACCENT_HIGHLIGHT);
        setTwelveTickHidden(false);
        setTwelveTickOverride(false);
        setSixtyTickHidden(false);
        setSixtyTickOverride(false);

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
        bytePacker.setStringFast(s);
        unpack();
    }

    public void pack() {
        bytePacker.rewind();

        backgroundPalette.pack(bytePacker);

        hourHandShape.pack(bytePacker);
        hourHandLength.pack(bytePacker);
        hourHandThickness.pack(bytePacker);
        hourHandStalk.pack(bytePacker);
        hourHandPalette.pack(bytePacker);
        // TODO: Fix minuteHandOverride and secondHandOverride
        bytePacker.put(minuteHandOverride);
        bytePacker.put(secondHandOverride);

        minuteHandShape.pack(bytePacker);
        minuteHandLength.pack(bytePacker);
        minuteHandThickness.pack(bytePacker);
        minuteHandStalk.pack(bytePacker);
        minuteHandPalette.pack(bytePacker);

        secondHandShape.pack(bytePacker);
        secondHandLength.pack(bytePacker);
        secondHandThickness.pack(bytePacker);
        secondHandPalette.pack(bytePacker);

        fillHighlightStyle.pack(bytePacker);
        accentFillStyle.pack(bytePacker);
        accentHighlightStyle.pack(bytePacker);
        baseAccentStyle.pack(bytePacker);

        String s = bytePacker.getString();
    }

    public void unpack() {
        bytePacker.rewind();

        backgroundPalette = Palette.unpack(bytePacker);

        hourHandShape = HandShape.unpack(bytePacker);
        hourHandLength = HandLength.unpack(bytePacker);
        hourHandThickness = HandThickness.unpack(bytePacker);
        hourHandStalk = HandStalk.unpack(bytePacker);
        hourHandPalette = Palette.unpack(bytePacker);
        // TODO: Fix minuteHandOverride and secondHandOverride
        minuteHandOverride = bytePacker.getBoolean();
        secondHandOverride = bytePacker.getBoolean();

        minuteHandShape = HandShape.unpack(bytePacker);
        minuteHandLength = HandLength.unpack(bytePacker);
        minuteHandThickness = HandThickness.unpack(bytePacker);
        minuteHandStalk = HandStalk.unpack(bytePacker);
        minuteHandPalette = Palette.unpack(bytePacker);

        secondHandShape = HandShape.unpack(bytePacker);
        secondHandLength = HandLength.unpack(bytePacker);
        secondHandThickness = HandThickness.unpack(bytePacker);
        secondHandPalette = Palette.unpack(bytePacker);

        fillHighlightStyle = GradientStyle.unpack(bytePacker);
        accentFillStyle = GradientStyle.unpack(bytePacker);
        accentHighlightStyle = GradientStyle.unpack(bytePacker);
        baseAccentStyle = GradientStyle.unpack(bytePacker);
    }

    public void setPalette(
            int fillColor, int accentColor, int highlightColor, int baseColor) {
        setFillColor(fillColor);
        setAccentColor(accentColor);
        setHighlightColor(highlightColor);
        setBaseColor(baseColor);
    }

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

    public Palette getBackgroundPalette() {
        return backgroundPalette;
    }

    public void setBackgroundPalette(Palette backgroundPalette) {
        this.backgroundPalette = backgroundPalette;
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

    public Palette getHourHandPalette() {
        return hourHandPalette;
    }

    public void setHourHandPalette(Palette hourHandPalette) {
        this.hourHandPalette = hourHandPalette;
    }

    public Palette getMinuteHandPalette() {
        return minuteHandOverride ? minuteHandPalette : hourHandPalette;
    }

    public void setMinuteHandPalette(Palette minuteHandPalette) {
        this.minuteHandPalette = minuteHandPalette;
    }

    public Palette getSecondHandPalette() {
        // If not overridden, the default is just a plain and regular second hand.
        return secondHandOverride ? secondHandPalette : Palette.HIGHLIGHT;
    }

    public void setSecondHandPalette(Palette secondHandPalette) {
        this.secondHandPalette = secondHandPalette;
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

    public TickShape getSixtyTickShape() {
        return sixtyTickOverride ? sixtyTickShape : fourTickShape;
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

    public TickLength getSixtyTickLength() {
        return sixtyTickOverride ? sixtyTickLength : fourTickLength;
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

    public TickThickness getSixtyTickThickness() {
        return sixtyTickOverride ? sixtyTickThickness : fourTickThickness;
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

    public TickRadiusPosition getSixtyTickRadiusPosition() {
        return sixtyTickOverride ? sixtyTickRadiusPosition : sixtyTickRadiusPosition;
    }

    public Palette getFourTickPalette() {
        return fourTickPalette;
    }

    public void setFourTickPalette(Palette fourTickPalette) {
        this.fourTickPalette = fourTickPalette;
    }

    public Palette getTwelveTickPalette() {
        return twelveTickOverride ? twelveTickPalette : fourTickPalette;
    }

    public void setTwelveTickPalette(Palette twelveTickPalette) {
        this.twelveTickPalette = twelveTickPalette;
    }

    public Palette getSixtyTickPalette() {
        return sixtyTickOverride ? sixtyTickPalette : fourTickPalette;
    }

    public void setSixtyTickPalette(Palette sixtyTickPalette) {
        this.sixtyTickPalette = sixtyTickPalette;
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

    public int getFillColor() {
        return mFillColor;
    }

    public void setFillColor(int mFillColor) {
        this.mFillColor = mFillColor;
    }

    public int getAccentColor() {
        return mAccentColor;
    }

    public void setAccentColor(int mAccentColor) {
        this.mAccentColor = mAccentColor;
    }

    public int getHighlightColor() {
        return mHighlightColor;
    }

    public void setHighlightColor(int mHighlightColor) {
        this.mHighlightColor = mHighlightColor;
    }

    public int getBaseColor() {
        return mBaseColor;
    }

    public void setBaseColor(int mBaseColor) {
        this.mBaseColor = mBaseColor;
    }

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
    public enum Palette {
        FILL, ACCENT, HIGHLIGHT, BASE, FILL_HIGHLIGHT, ACCENT_FILL, ACCENT_HIGHLIGHT, ACCENT_BASE;

        private static final int bits = 3;

        static Palette unpack(BytePacker bytePacker) {
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
