package pro.watchkit.wearable.watchface.model;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorSpace;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.os.Build;

import java.util.Objects;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

public final class Palette {
    public static final int AMBIENT_WHITE =
            Color.argb(0xff, 0xff, 0xff, 0xff);

    private static final float AMBIENT_PAINT_STROKE_WIDTH_PERCENT = 0.333f; // 0.333%
    private static final float PAINT_STROKE_WIDTH_PERCENT = 0.5f; // 0.5%

    private int height = -1, width = -1;
    private float pc = 0f; // percent, set to 0.01f * height, all units are based on percent
    private float mCenterX, mCenterY;

//    private int mFillColor;
//    private int mAccentColor;
//    private int mHighlightColor;
//    private int mBaseColor;

    private Paint mFillPaint;
    private Paint mAccentPaint;
    private Paint mHighlightPaint;
    private Paint mBasePaint;

    private Paint mAmbientPaint;
    private Paint mShadowPaint;

    private GradientPaint mFillHighlightPaint = new GradientPaint();
    private GradientPaint mAccentFillPaint = new GradientPaint(),
            mBezelPaint1,
            mBezelPaint2 = new GradientPaint();
    private GradientPaint mAccentHighlightPaint = new GradientPaint();
    private GradientPaint mBaseAccentPaint = new GradientPaint();
    private WatchFacePreset preset;
    private int mPreviousSerial = -1;

    public Palette(WatchFacePreset preset) {
        this.preset = preset;
        mFillPaint = newDefaultPaint();
        mAccentPaint = newDefaultPaint();
        mHighlightPaint = newDefaultPaint();
        mBasePaint = newDefaultPaint();

//        mFillHighlightPaint = newDefaultPaint();
//        mAccentFillPaint = newDefaultPaint();
//        mBezelPaint2 = newDefaultPaint();
//        mAccentHighlightPaint = newDefaultPaint();
//        mBaseAccentPaint = newDefaultPaint();

        mAmbientPaint = newDefaultPaint();
        mAmbientPaint.setStyle(Paint.Style.STROKE);
        mAmbientPaint.setColor(AMBIENT_WHITE);
//        mAmbientPaint.setShadowLayer(SHADOW_RADIUS, 0, 0, mBaseColor);

        mShadowPaint = newDefaultPaint();
        mShadowPaint.setStyle(Paint.Style.FILL);
//        mShadowPaint.setShadowLayer(SHADOW_RADIUS, 0, 0, mWatchHandShadowColor);
//        mShadowPaint.setShadowLayer(SHADOW_RADIUS, 0, 0, Color.WHITE);

//        generatePalette();
//        generateTuples();
    }

    private static Paint newDefaultPaint() {
        Paint result = new Paint();
        result.setStrokeJoin(Paint.Join.ROUND);
        result.setStrokeCap(Paint.Cap.ROUND);
        result.setAntiAlias(true);
//        result.setPathEffect(new CornerPathEffect(3.2f));
        return result;
    }

    private static void generatePalette() {
        if (Build.VERSION.SDK_INT >= 26) {
            int[] i = new int[]{255, 170, 85, 0};
            String[] s = new String[]{"FF", "AA", "55", "00"};
            ColorSpace CIE_LAB = ColorSpace.get(ColorSpace.Named.CIE_LAB);
//            ColorSpace sRGB = ColorSpace.get(ColorSpace.Named.SRGB);
//            ColorSpace.Connector connector =
//                    ColorSpace.connect(sRGB, CIE_LAB, ColorSpace.RenderIntent.PERCEPTUAL);

            StringBuilder sb = new StringBuilder();
            for (int r = 0; r < i.length; r++) {
                for (int g = 0; g < i.length; g++) {
                    for (int b = 0; b < i.length; b++) {
                        long lab = Color.convert(Color.argb(0, i[r], i[g], i[b]), CIE_LAB);

//                        sb.append(String.format("(%d, %d, %d) → (%d, %d, %d)",
//                                r, g, b,
//                                (int)Color.red(lab), (int)Color.green(lab), (int)Color.blue(lab)
//                                ));

                        sb.append(String.format("{\"group\": \"#%s%s%s\", \"x\": %d, \"y\": %d, \"z\": %d},",
                                s[r], s[g], s[b], (int) Color.red(lab), (int) Color.green(lab), (int) Color.blue(lab)
                        ));
                        sb.append(System.lineSeparator());
                    }
                }
                android.util.Log.d("AnalogWatchFace", sb.toString());
                sb.setLength(0);
            }
        }
    }

//    public Paint getAccentHighlightPaint() {
//        regeneratePaints2();
//        return mAccentHighlightPaint;
//    }
//
//    public Paint getBaseAccentPaint() {
//        regeneratePaints2();
//        return mBaseAccentPaint;
//    }

//    public Paint getTickPaint() {
//        regeneratePaints2();
//        return mTickPaint;
//    }

//    public Paint getBackgroundPaint() {
//        regeneratePaints2();
//        return mBackgroundPaint;
//    }

    //    private Paint mHandPaint;
//    private Paint mTickPaint;
//    private Paint mBackgroundPaint;

    private static void generatePalette1() {
        if (Build.VERSION.SDK_INT >= 26) {
            float[] ls = new float[]{100f, 75f, 50f, 25f, 0f};
//            float [] as = new float[] {-128f, -64f, 0f, 64f, 128f};
//            float [] bs = new float[] {-128f, -64f, 0f, 64f, 128f};
            float[] bs = new float[]{128f, 96f, 64f, 32f, 0f, -32f, -64f, -96f, -128f};
            float[] as = new float[]{-128f, -96f, -64f, -32f, 0f, 32f, 64f, 96f, 128f};

            ColorSpace CIE_LAB = ColorSpace.get(ColorSpace.Named.CIE_LAB);
            ColorSpace.Connector connector =
                    ColorSpace.connect(CIE_LAB, ColorSpace.RenderIntent.PERCEPTUAL);

            for (int lc = 0; lc < ls.length; lc++) {
                StringBuilder sb = new StringBuilder();
                for (int bc = 0; bc < bs.length; bc++) {
                    sb.append("<div>").append(System.lineSeparator()).append("    ");
                    for (int ac = 0; ac < as.length; ac++) {
                        float[] f = connector.transform(ls[lc], as[ac], bs[bc]);
                        int r = Color.toArgb(Color.pack(f[0], f[1], f[2]));
//                        Color c = Color.valueOf(ls[lc], as[ac], bs[bc], 0f, CIE_LAB);
//                        int r = Color.toArgb(Color.convert(c.pack(), connector));
                        sb.append("<div class=\"A\" style=\"background-color: ");
                        sb.append(String.format("#%06X", (0xFFFFFF & r)));
                        sb.append("\"></div>");
                    }
                    sb.append("</div>").append(System.lineSeparator());

                    android.util.Log.d("AnalogWatchFace", sb.toString());
                    sb.setLength(0);
                }
                sb.append("<div><hr></div>").append(System.lineSeparator());
                android.util.Log.d("AnalogWatchFace", sb.toString());
            }
        }
    }

    /**
     * Given two colors A and B, return an intermediate color between the two. The distance
     * between the two is given by "d"; 1.0 means return "colorA", 0.0 means return "colorB",
     * 0.5 means return something evenly between the two.
     * <p>
     * For SDK 26 (Android O) and above, the calculation is done in the LAB color space for
     * extra perceptual accuracy!
     *
     * @param colorA One color to calculate
     * @param colorB The other color
     * @param d      The distance from colorB, between 0.0 and 1.0
     * @return A color between colorA and colorB
     */
    static int getIntermediateColor(int colorA, int colorB, double d) {
        // Clamp to [0, 1]
        if (d < 0) d = 0;
        else if (d > 1) d = 1;
        double e = 1d - d;

        // The "long colors" feature is only available in SDK 26 onwards!
        if (Build.VERSION.SDK_INT >= 26) {
            ColorSpace CIE_LAB = ColorSpace.get(ColorSpace.Named.CIE_LAB);
            ColorSpace sRGB = ColorSpace.get(ColorSpace.Named.SRGB);

            // Convert colors to LAB color space.
            long colorAL = Color.convert(colorA, CIE_LAB);
            long colorBL = Color.convert(colorB, CIE_LAB);

            // Generate a new color that is between the two.
            float a = (float) (Color.alpha(colorAL) * d + Color.alpha(colorBL) * e);
            float r = (float) (Color.red(colorAL) * d + Color.red(colorBL) * e);
            float g = (float) (Color.green(colorAL) * d + Color.green(colorBL) * e);
            float b = (float) (Color.blue(colorAL) * d + Color.blue(colorBL) * e);

            // Convert back to sRGB and return.
            return Color.toArgb(Color.convert(r, g, b, a, CIE_LAB, sRGB));
        } else {
            // Generate a new color that is between the two.
            int a = (int) (Color.alpha(colorA) * d + Color.alpha(colorB) * e);
            int r = (int) (Color.red(colorA) * d + Color.red(colorB) * e);
            int g = (int) (Color.green(colorA) * d + Color.green(colorB) * e);
            int b = (int) (Color.blue(colorA) * d + Color.blue(colorB) * e);

            // And return
            return Color.argb(a, r, g, b);
        }
    }

    public Paint getAmbientPaint() {
        regeneratePaints2();
        return mAmbientPaint;
    }

//    public void setPalette(WatchFacePreset preset) {
//        this.mFillColor = preset.getFillColor();
//        this.mAccentColor = preset.getAccentColor();
//        this.mHighlightColor = preset.getHighlightColor();
//        this.mBaseColor = preset.getBaseColor();
//
//        regeneratePaints();
//    }

    public Paint getShadowPaint() {
        regeneratePaints2();
        return mShadowPaint;
    }

//    private void regeneratePaints() {
//        if (width <= 0 || height <= 0)
//            return;
//
//        mFillPaint.setColor(mFillColor);
//        mAccentPaint.setColor(mAccentColor);
//        mHighlightPaint.setColor(mHighlightColor);
//        mBasePaint.setColor(mBaseColor);
//
//        mFillHighlightPaint.setColors(mFillColor, mHighlightColor, WatchFacePreset.GradientStyle.RADIAL_BRUSHED);
//        mAccentFillPaint.setColors(mAccentColor, mFillColor, WatchFacePreset.GradientStyle.SWEEP);
//        mBezelPaint2.setColors(mFillColor, mAccentColor, WatchFacePreset.GradientStyle.SWEEP);
//        mAccentHighlightPaint.setColors(mAccentColor, mHighlightColor, WatchFacePreset.GradientStyle.SWEEP);
//        mBaseAccentPaint.setColors(mBaseColor, mAccentColor, WatchFacePreset.GradientStyle.SWEEP_BRUSHED);
//
//        mTickPaint = mAccentHighlightPaint;
//        mBackgroundPaint = mBaseAccentPaint;
//        // TODO: make the above only trigger when the colors actually change.
//        // TODO: actually, just hook it up to the WatchFacePreset code...
//    }

    public Paint getFillHighlightPaint() {
        regeneratePaints2();
        return mFillHighlightPaint;
    }

    public Paint getBezelPaint1() {
        regeneratePaints2();
        return mBezelPaint1;
    }

    public Paint getBezelPaint2() {
        regeneratePaints2();
        return mBezelPaint2;
    }

    public void onWidthAndHeightChanged(int width, int height) {
        if (this.width == width && this.height == height) {
            return;
        }

        this.width = width;
        this.height = height;
        pc = 0.01f * height;
        mCenterX = width / 2f;
        mCenterY = height / 2f;

//            /*
//             * Calculate lengths of different hands based on watch screen size.
//             */
//            mSecondHandLength = 43.75f * pc; // 43.75%
//            mMinuteHandLength = 37.5f * pc; // 37.5%
//            mHourHandLength = 25f * pc; // 25%
//            // I changed my mind...
//            mSecondHandLength = 45f * pc; // 45%
//            mMinuteHandLength = 40f * pc; // 40%
//            mHourHandLength = 30f * pc; // 30%

//        // Regenerate stroke widths based on value of "percent"
//        mFillHighlightPaint.setStrokeWidth(PAINT_STROKE_WIDTH_PERCENT * pc);
//        mAccentFillPaint.setStrokeWidth(PAINT_STROKE_WIDTH_PERCENT * pc);
//        mBezelPaint2.setStrokeWidth(PAINT_STROKE_WIDTH_PERCENT * pc);
//        mAccentHighlightPaint.setStrokeWidth(PAINT_STROKE_WIDTH_PERCENT * pc);
//        mBaseAccentPaint.setStrokeWidth(PAINT_STROKE_WIDTH_PERCENT * pc);
//        mAmbientPaint.setStrokeWidth(AMBIENT_PAINT_STROKE_WIDTH_PERCENT * pc);
//
//        regeneratePaints();
    }

    private void regeneratePaints2() {
        // Invalidate if our preset has changed.
        int currentSerial = Objects.hash(
                preset.getFillColor(),
                preset.getAccentColor(),
                preset.getHighlightColor(),
                preset.getBaseColor(),
                preset.getFillHighlightStyle(),
                preset.getAccentFillStyle(),
                preset.getAccentHighlightStyle(),
                preset.getBaseAccentStyle(),
                pc);
        if (mPreviousSerial == currentSerial || width <= 0 || height <= 0) {
            return;
        }
//        android.util.Log.d("AnalogWatchFace", String.format("BigInvalidated! Serial: Old: %d, New: %d", mPreviousSerial, currentSerial));

        mPreviousSerial = currentSerial;

        mFillPaint.setColor(preset.getFillColor());
        mAccentPaint.setColor(preset.getAccentColor());
        mHighlightPaint.setColor(preset.getHighlightColor());
        mBasePaint.setColor(preset.getBaseColor());

        mFillHighlightPaint.setColors(preset.getFillColor(), preset.getHighlightColor(), preset.getFillHighlightStyle());
        mAccentFillPaint.setColors(preset.getAccentColor(), preset.getFillColor(), preset.getAccentFillStyle());
        mBezelPaint1 = mAccentFillPaint;
        mBezelPaint2.setColors(preset.getFillColor(), preset.getAccentColor(), preset.getAccentFillStyle());
        mAccentHighlightPaint.setColors(preset.getAccentColor(), preset.getHighlightColor(), preset.getAccentHighlightStyle());
        mBaseAccentPaint.setColors(preset.getBaseColor(), preset.getAccentColor(), preset.getBaseAccentStyle());

//        mTickPaint = mAccentHighlightPaint;
//        mBackgroundPaint = mBaseAccentPaint;
        mShadowPaint.setColor(preset.getBaseColor());

        // Regenerate stroke widths based on value of "percent"
        mFillHighlightPaint.setStrokeWidth(PAINT_STROKE_WIDTH_PERCENT * pc);
        mAccentFillPaint.setStrokeWidth(PAINT_STROKE_WIDTH_PERCENT * pc);
        mBezelPaint2.setStrokeWidth(PAINT_STROKE_WIDTH_PERCENT * pc);
        mAccentHighlightPaint.setStrokeWidth(PAINT_STROKE_WIDTH_PERCENT * pc);
        mBaseAccentPaint.setStrokeWidth(PAINT_STROKE_WIDTH_PERCENT * pc);
        mAmbientPaint.setStrokeWidth(AMBIENT_PAINT_STROKE_WIDTH_PERCENT * pc);
    }

    private Tuple generateMidPoint(Tuple a1, Tuple b1, float d) {
        float e = 1f - d;
        float a = (a1.a * e) + (b1.a * d);
        float b = (a1.b * e) + (b1.b * d);
        float c = (a1.c * e) + (b1.c * d);
        float x = (a1.x * e) + (b1.x * d);
        float y = (a1.y * e) + (b1.y * d);
        float z = (a1.z * e) + (b1.z * d);
        return new Tuple(Math.round(a), Math.round(b), Math.round(c), x, y, z);
    }

    private Tuple generateMidPoint(Tuple a1, Tuple b1, float d1, Tuple a2, Tuple b2, float d2) {
        Tuple t1 = generateMidPoint(a1, b1, d1);
        Tuple t2 = generateMidPoint(a2, b2, d2);
        if (t1.a != t2.a || t2.b != t2.b || t1.c != t2.c) {
            android.util.Log.d("AnalogWatchFace", String.format("Error: (%d, %d, %d) != (%d, %d, %d)", t1.a, t1.b, t1.c, t2.a, t2.b, t2.c));
        }
        return generateMidPoint(t1, t2, 0.5f);
    }

    private Tuple generateMidPoint(Tuple a1, Tuple b1, float d1, Tuple a2, Tuple b2, float d2, Tuple a3, Tuple b3, float d3) {
        Tuple t1 = generateMidPoint(a1, b1, d1);
        Tuple t2 = generateMidPoint(a2, b2, d2);
        Tuple t3 = generateMidPoint(a3, b3, d3);
        return new Tuple(t1.a, t1.b, t1.c,
                (t1.x + t2.x + t3.x) / 3f,
                (t1.y + t2.y + t3.y) / 3f,
                (t1.z + t2.z + t3.z) / 3f);
    }

    private void generateTuples() {
        if (Build.VERSION.SDK_INT < 26) return;

        ColorSpace CIE_LAB = ColorSpace.get(ColorSpace.Named.CIE_LAB);
        long lab333 = Color.convert(Color.argb(255, 255, 255, 255), CIE_LAB);
        long lab330 = Color.convert(Color.argb(255, 255, 255, 0), CIE_LAB);
        long lab303 = Color.convert(Color.argb(255, 255, 0, 255), CIE_LAB);
        long lab300 = Color.convert(Color.argb(255, 255, 0, 0), CIE_LAB);
        long lab033 = Color.convert(Color.argb(255, 0, 255, 255), CIE_LAB);
        long lab030 = Color.convert(Color.argb(255, 0, 255, 0), CIE_LAB);
        long lab003 = Color.convert(Color.argb(255, 0, 0, 255), CIE_LAB);
        long lab000 = Color.convert(Color.argb(255, 0, 0, 0), CIE_LAB);

        Tuple t333 = new Tuple(3, 3, 3, Color.red(lab333), Color.green(lab333), Color.blue(lab333)).log();
        Tuple t330 = new Tuple(3, 3, 0, Color.red(lab330), Color.green(lab330), Color.blue(lab330)).log();
        Tuple t303 = new Tuple(3, 0, 3, Color.red(lab303), Color.green(lab303), Color.blue(lab303)).log();
        Tuple t300 = new Tuple(3, 0, 0, Color.red(lab300), Color.green(lab300), Color.blue(lab300)).log();
        Tuple t033 = new Tuple(0, 3, 3, Color.red(lab033), Color.green(lab033), Color.blue(lab033)).log();
        Tuple t030 = new Tuple(0, 3, 0, Color.red(lab030), Color.green(lab030), Color.blue(lab030)).log();
        Tuple t003 = new Tuple(0, 0, 3, Color.red(lab003), Color.green(lab003), Color.blue(lab003)).log();
        Tuple t000 = new Tuple(0, 0, 0, Color.red(lab000), Color.green(lab000), Color.blue(lab000)).log();

        // Edges
        Tuple t331 = generateMidPoint(t330, t333, 0.333333f).log();
        Tuple t332 = generateMidPoint(t330, t333, 0.666667f).log();
        Tuple t313 = generateMidPoint(t303, t333, 0.333333f).log();
        Tuple t323 = generateMidPoint(t303, t333, 0.666667f).log();
        Tuple t301 = generateMidPoint(t300, t303, 0.333333f).log();
        Tuple t302 = generateMidPoint(t300, t303, 0.666667f).log();
        Tuple t310 = generateMidPoint(t300, t330, 0.333333f).log();
        Tuple t320 = generateMidPoint(t300, t330, 0.666667f).log();

        Tuple t031 = generateMidPoint(t030, t033, 0.333333f).log();
        Tuple t032 = generateMidPoint(t030, t033, 0.666667f).log();
        Tuple t013 = generateMidPoint(t003, t033, 0.333333f).log();
        Tuple t023 = generateMidPoint(t003, t033, 0.666667f).log();
        Tuple t001 = generateMidPoint(t000, t003, 0.333333f).log();
        Tuple t002 = generateMidPoint(t000, t003, 0.666667f).log();
        Tuple t010 = generateMidPoint(t000, t030, 0.333333f).log();
        Tuple t020 = generateMidPoint(t000, t030, 0.666667f).log();

        Tuple t133 = generateMidPoint(t033, t333, 0.333333f).log();
        Tuple t130 = generateMidPoint(t030, t330, 0.333333f).log();
        Tuple t103 = generateMidPoint(t003, t303, 0.333333f).log();
        Tuple t100 = generateMidPoint(t000, t300, 0.333333f).log();
        Tuple t233 = generateMidPoint(t033, t333, 0.666667f).log();
        Tuple t230 = generateMidPoint(t030, t330, 0.666667f).log();
        Tuple t203 = generateMidPoint(t003, t303, 0.666667f).log();
        Tuple t200 = generateMidPoint(t000, t300, 0.666667f).log();

        // Faces
        Tuple t311 = generateMidPoint(t310, t313, 0.333333f, t301, t331, 0.333333f).log();
        Tuple t312 = generateMidPoint(t310, t313, 0.666667f, t302, t332, 0.333333f).log();
        Tuple t321 = generateMidPoint(t320, t323, 0.333333f, t301, t331, 0.666667f).log();
        Tuple t322 = generateMidPoint(t320, t323, 0.666667f, t302, t332, 0.666667f).log();

        Tuple t011 = generateMidPoint(t010, t013, 0.333333f, t001, t031, 0.333333f).log();
        Tuple t012 = generateMidPoint(t010, t013, 0.666667f, t002, t032, 0.333333f).log();
        Tuple t021 = generateMidPoint(t020, t023, 0.333333f, t001, t031, 0.666667f).log();
        Tuple t022 = generateMidPoint(t020, t023, 0.666667f, t002, t032, 0.666667f).log();

        Tuple t101 = generateMidPoint(t100, t103, 0.333333f, t001, t301, 0.333333f).log();
        Tuple t102 = generateMidPoint(t100, t103, 0.666667f, t002, t302, 0.333333f).log();
        Tuple t201 = generateMidPoint(t200, t203, 0.333333f, t001, t301, 0.666667f).log();
        Tuple t202 = generateMidPoint(t200, t203, 0.666667f, t002, t302, 0.666667f).log();

        Tuple t131 = generateMidPoint(t130, t133, 0.333333f, t031, t331, 0.333333f).log();
        Tuple t132 = generateMidPoint(t130, t133, 0.666667f, t032, t332, 0.333333f).log();
        Tuple t231 = generateMidPoint(t230, t233, 0.333333f, t031, t331, 0.666667f).log();
        Tuple t232 = generateMidPoint(t230, t233, 0.666667f, t032, t332, 0.666667f).log();

        Tuple t110 = generateMidPoint(t100, t130, 0.333333f, t010, t310, 0.333333f).log();
        Tuple t120 = generateMidPoint(t100, t130, 0.666667f, t020, t320, 0.333333f).log();
        Tuple t210 = generateMidPoint(t200, t230, 0.333333f, t010, t310, 0.666667f).log();
        Tuple t220 = generateMidPoint(t200, t230, 0.666667f, t020, t320, 0.666667f).log();

        Tuple t113 = generateMidPoint(t103, t133, 0.333333f, t013, t313, 0.333333f).log();
        Tuple t123 = generateMidPoint(t103, t133, 0.666667f, t023, t323, 0.333333f).log();
        Tuple t213 = generateMidPoint(t203, t233, 0.333333f, t013, t313, 0.666667f).log();
        Tuple t223 = generateMidPoint(t203, t233, 0.666667f, t023, t323, 0.666667f).log();

        // Inside
        Tuple t111 = generateMidPoint(t110, t113, 0.333333f, t101, t131, 0.333333f, t011, t311, 0.333333f).log();
        Tuple t112 = generateMidPoint(t110, t113, 0.666667f, t102, t132, 0.333333f, t012, t312, 0.333333f).log();
        Tuple t121 = generateMidPoint(t120, t123, 0.333333f, t101, t131, 0.666667f, t021, t321, 0.333333f).log();
        Tuple t122 = generateMidPoint(t120, t123, 0.666667f, t102, t132, 0.666667f, t022, t322, 0.333333f).log();
        Tuple t211 = generateMidPoint(t210, t213, 0.333333f, t201, t231, 0.333333f, t011, t311, 0.666667f).log();
        Tuple t212 = generateMidPoint(t210, t213, 0.666667f, t202, t232, 0.333333f, t012, t312, 0.666667f).log();
        Tuple t221 = generateMidPoint(t220, t223, 0.333333f, t201, t231, 0.666667f, t021, t321, 0.666667f).log();
        Tuple t222 = generateMidPoint(t220, t223, 0.666667f, t202, t232, 0.666667f, t022, t322, 0.666667f).log();
    }

    public Paint getPaintFromPreset(WatchFacePreset.Palette palette) {
        regeneratePaints2();
        switch (palette) {
            case FILL:
                return mFillPaint;
            case ACCENT:
                return mAccentPaint;
            case HIGHLIGHT:
                return mHighlightPaint;
            case BASE:
                return mBasePaint;
            case FILL_HIGHLIGHT:
                return mFillHighlightPaint;
            case ACCENT_FILL:
                return mAccentFillPaint;
            case ACCENT_HIGHLIGHT:
                return mAccentHighlightPaint;
            case ACCENT_BASE:
                return mBaseAccentPaint;
            default:
                // Should never hit this.
                return mFillPaint;
        }
    }

    private class GradientPaint extends Paint {
        private int mCustomHashCode = -1;

        public GradientPaint() {
            super();

            // From "newDefaultPaint".
            this.setStrokeJoin(Paint.Join.ROUND);
            this.setStrokeCap(Paint.Cap.ROUND);
            this.setAntiAlias(true);
//            this.setPathEffect(new CornerPathEffect(3.2f));
        }

        @Override
        public int hashCode() {
            return super.hashCode() + mCustomHashCode;
        }

        public void setColors(@ColorInt int colorA, @ColorInt int colorB,
                              WatchFacePreset.GradientStyle gradientStyle) {
            switch (gradientStyle) {
                case SWEEP:
                    addSweepGradient(colorA, colorB);
                    break;
                case RADIAL:
                    addRadialGradient(colorA, colorB);
                    break;
                case SWEEP_BRUSHED:
                    addSweepGradient(colorA, colorB);
                    addBrushedEffect();
                    break;
                case RADIAL_BRUSHED:
                    addRadialGradient(colorA, colorB);
                    addBrushedEffect();
                    break;
                default:
                    // Should never hit this.
                    break;
            }

            mCustomHashCode = Objects.hash(colorA, colorB, gradientStyle);
        }

        private void addSweepGradient(int colorA, int colorB) {
//        paint.setShader(new SweepGradient(mCenterX, mCenterY,
//                new int[]{colorA, colorB, colorA, colorB, colorA},
//                null));
            int[] gradient = new int[]{
                    getIntermediateColor(colorA, colorB, 1.0d), // Original
                    getIntermediateColor(colorA, colorB, 0.8d),
                    getIntermediateColor(colorA, colorB, 0.6d),
                    getIntermediateColor(colorA, colorB, 0.4d),
                    getIntermediateColor(colorA, colorB, 0.2d),
                    getIntermediateColor(colorA, colorB, 0.0d), // Original
                    getIntermediateColor(colorA, colorB, 0.2d),
                    getIntermediateColor(colorA, colorB, 0.4d),
                    getIntermediateColor(colorA, colorB, 0.6d),
                    getIntermediateColor(colorA, colorB, 0.8d),
                    getIntermediateColor(colorA, colorB, 1.0d), // Original
                    getIntermediateColor(colorA, colorB, 0.8d),
                    getIntermediateColor(colorA, colorB, 0.6d),
                    getIntermediateColor(colorA, colorB, 0.4d),
                    getIntermediateColor(colorA, colorB, 0.2d),
                    getIntermediateColor(colorA, colorB, 0.0d), // Original
                    getIntermediateColor(colorA, colorB, 0.2d),
                    getIntermediateColor(colorA, colorB, 0.4d),
                    getIntermediateColor(colorA, colorB, 0.6d),
                    getIntermediateColor(colorA, colorB, 0.8d),
                    getIntermediateColor(colorA, colorB, 1.0d), // Original
            };
            setShader(new SweepGradient(mCenterX, mCenterY, gradient, null));
        }

        private void addRadialGradient(int colorA, int colorB) {
//        paint.setShader(new RadialGradient(mCenterX, mCenterY, mCenterY,
//                new int[]{colorB, colorB, colorB, colorA, colorA},
//                null, Shader.TileMode.CLAMP));

            int[] gradient = new int[]{
                    colorB, // Original
                    colorB,
                    colorB,
                    colorB,
                    colorB,
                    colorB, // Original
                    colorB,
                    colorB,
                    colorB,
                    colorB,
                    colorB, // Original
                    getIntermediateColor(colorA, colorB, 0.2d),
                    getIntermediateColor(colorA, colorB, 0.4d),
                    getIntermediateColor(colorA, colorB, 0.6d),
                    getIntermediateColor(colorA, colorB, 0.8d),
                    colorA, // Original
                    colorA,
                    colorA,
                    colorA,
                    colorA,
                    colorA // Original
            };
            setShader(new RadialGradient(mCenterX, mCenterY, mCenterY, gradient, null,
                    Shader.TileMode.CLAMP));
        }

        private void addBrushedEffect() {
            Bitmap mAccentBasePaintBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(mAccentBasePaintBitmap);

            float percent = mCenterX / 50f;
            float offset = 0.5f * percent;
            int alpha = 50;

            Paint circlePaint = new Paint();
            circlePaint.setStyle(Paint.Style.STROKE);
            circlePaint.setStrokeWidth(offset);
            circlePaint.setStrokeJoin(Paint.Join.ROUND);
            circlePaint.setAntiAlias(true);

            // Spun metal circles?
            for (int max = 50, i = max; i > 0; i--) {
                Path p = new Path();
                p.addCircle(mCenterX, mCenterY, mCenterX * i / max, Path.Direction.CW);

                p.offset(-offset, -offset);
                circlePaint.setColor(Color.WHITE);
                circlePaint.setAlpha(alpha);
                canvas.drawPath(p, circlePaint);

                p.offset(2f * offset, 2f * offset);
                circlePaint.setColor(Color.BLACK);
                circlePaint.setAlpha(alpha);
                canvas.drawPath(p, circlePaint);

                p.offset(-offset, -offset);
                canvas.drawPath(p, this);
            }

            setShader(new BitmapShader(mAccentBasePaintBitmap,
                    Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));
        }
    }

    private class Tuple {
        public int a, b, c;
        public float x, y, z;

        Tuple(int a, int b, int c, float x, float y, float z) {
            this.a = a;
            this.b = b;
            this.c = c;
            this.x = x;
            this.y = y;
            this.z = z;
        }

        @NonNull
        @Override
        public String toString() {
            String group;
            if (Build.VERSION.SDK_INT >= 26) {
                ColorSpace CIE_LAB = ColorSpace.get(ColorSpace.Named.CIE_LAB);
                ColorSpace sRGB = ColorSpace.get(ColorSpace.Named.SRGB);
                ColorSpace.Connector connector =
                        ColorSpace.connect(CIE_LAB, sRGB, ColorSpace.RenderIntent.ABSOLUTE);

                long col = Color.convert(Color.pack(x, y, z, 1.0f, CIE_LAB), connector);

//                long col = Color.convert(x, y, z, 1.0f,
//                        ColorSpace.get(ColorSpace.Named.CIE_LAB),
//                        ColorSpace.get(ColorSpace.Named.SRGB));
                group = String.format("0x%06X", (0xFFFFFF & Color.toArgb(col)));
            } else {
                group = "0x000000";
            }

            String[] hexes = new String[]{"00", "55", "AA", "FF"};
            String original = "0x" + hexes[a] + hexes[b] + hexes[c];

            return String.format("{\"a\": %d, \"b\": %d, \"c\": %d, \"group\": \"%s\", \"original\": \"%s\", \"x\": %f, \"y\": %f, \"z\": %f},",
                    a, b, c, group, original, x, y, z);
        }

        public Tuple log() {
            android.util.Log.d("AnalogWatchFace", toString());
            return this;
        }
    }

//    public int getFillColor() {
//        return mFillColor;
//    }
//
//    public int getAccentColor() {
//        return mAccentColor;
//    }

//    @Deprecated
//    public int getHighlightColor() {
//        regeneratePaints2();
//        return preset.getHighlightColor();
//    }

//    public int getBaseColor() {
//        return mBaseColor;
//    }
}
