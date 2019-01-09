package com.example.android.wearable.watchface.model;

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

import com.example.android.wearable.watchface.model.WatchFacePreset;

public final class Palette {
    public static final int AMBIENT_WHITE =
            Color.argb(0xff, 0xff, 0xff, 0xff);

    private static final float AMBIENT_PAINT_STROKE_WIDTH_PERCENT = 0.333f; // 0.333%
    private static final float PAINT_STROKE_WIDTH_PERCENT = 0.5f; // 0.5%

    private int height = -1, width = -1;
    private float pc = 0f; // percent, set to 0.01f * height, all units are based on percent
    private float mCenterX, mCenterY;

    private int mFillColor;
    private int mAccentColor;
    private int mHighlightColor;
    private int mBaseColor;

    private Paint mFillPaint;
    private Paint mAccentPaint;
    private Paint mHighlightPaint;
    private Paint mBasePaint;

    private Paint mAmbientPaint;
    private Paint mShadowPaint;

    private Paint mFillHighlightPaint;
    private Paint mAccentFillPaint, mAccentFillPaint2;
    private Paint mAccentHighlightPaint;
    private Paint mBaseAccentPaint;

    public Paint getAmbientPaint() {
        return mAmbientPaint;
    }

    public Paint getShadowPaint() {
        return mShadowPaint;
    }

    public Paint getFillHighlightPaint() {
        return mFillHighlightPaint;
    }

    public Paint getAccentFillPaint() {
        return mAccentFillPaint;
    }

    public Paint getAccentFillPaint2() {
        return mAccentFillPaint2;
    }

    public Paint getAccentHighlightPaint() {
        return mAccentHighlightPaint;
    }

    public Paint getBaseAccentPaint() {
        return mBaseAccentPaint;
    }

    public Paint getTickPaint() {
        return mTickPaint;
    }

    public Paint getBackgroundPaint() {
        return mBackgroundPaint;
    }

    //    private Paint mHandPaint;
    private Paint mTickPaint;
    private Paint mBackgroundPaint;

    public Palette() {
        mFillPaint = newDefaultPaint();
        mAccentPaint = newDefaultPaint();
        mHighlightPaint = newDefaultPaint();
        mBasePaint = newDefaultPaint();

        mFillHighlightPaint = newDefaultPaint();
        mAccentFillPaint = newDefaultPaint();
        mAccentFillPaint2 = newDefaultPaint();
        mAccentHighlightPaint = newDefaultPaint();
        mBaseAccentPaint = newDefaultPaint();

        mAmbientPaint = newDefaultPaint();
        mAmbientPaint.setStyle(Paint.Style.STROKE);
        mAmbientPaint.setColor(AMBIENT_WHITE);
//        mAmbientPaint.setShadowLayer(SHADOW_RADIUS, 0, 0, mBaseColor);

        mShadowPaint = newDefaultPaint();
        mShadowPaint.setColor(mBaseColor);
        mShadowPaint.setStyle(Paint.Style.FILL);
//        mShadowPaint.setShadowLayer(SHADOW_RADIUS, 0, 0, mWatchHandShadowColor);
//        mShadowPaint.setShadowLayer(SHADOW_RADIUS, 0, 0, Color.WHITE);
    }

    private static Paint newDefaultPaint() {
        Paint result = new Paint();
        result.setStrokeJoin(Paint.Join.ROUND);
        result.setStrokeCap(Paint.Cap.ROUND);
        result.setAntiAlias(true);
//        result.setPathEffect(new CornerPathEffect(3.2f));
        return result;
    }

    public void setPalette(
            int mFillColor, int mAccentColor, int mHighlightColor, int mBackgroundColor) {
        this.mFillColor = mFillColor;
        this.mAccentColor = mAccentColor;
        this.mHighlightColor = mHighlightColor;
        this.mBaseColor = mBackgroundColor;

        regeneratePaints();
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

        // Regenerate stroke widths based on value of "percent"
        mFillHighlightPaint.setStrokeWidth(PAINT_STROKE_WIDTH_PERCENT * pc);
        mAccentFillPaint.setStrokeWidth(PAINT_STROKE_WIDTH_PERCENT * pc);
        mAccentFillPaint2.setStrokeWidth(PAINT_STROKE_WIDTH_PERCENT * pc);
        mAccentHighlightPaint.setStrokeWidth(PAINT_STROKE_WIDTH_PERCENT * pc);
        mBaseAccentPaint.setStrokeWidth(PAINT_STROKE_WIDTH_PERCENT * pc);
        mAmbientPaint.setStrokeWidth(AMBIENT_PAINT_STROKE_WIDTH_PERCENT * pc);

        regeneratePaints();
    }

    private void regeneratePaints() {
        if (width <= 0 || height <= 0)
            return;

        mFillPaint.setColor(mFillColor);
        mAccentPaint.setColor(mAccentColor);
        mHighlightPaint.setColor(mHighlightColor);
        mBasePaint.setColor(mBaseColor);

        //addSweepGradient(mFillHighlightPaint, mFillColor, mHighlightColor);
        addRadialGradient(mFillHighlightPaint, mFillColor, mHighlightColor);
        addSpunMetalEffectToPaint(mFillHighlightPaint);
//        mHandPaint = mFillHighlightPaint;

        addSweepGradient(mAccentFillPaint, mAccentColor, mFillColor);

        addSweepGradient(mAccentFillPaint2, mFillColor, mAccentColor);

        addSweepGradient(mAccentHighlightPaint, mAccentColor, mHighlightColor);
        //addRadialGradient(mAccentHighlightPaint, mAccentColor, mHighlightColor);
        mTickPaint = mAccentHighlightPaint;

        addSweepGradient(mBaseAccentPaint, mBaseColor, mAccentColor);
        //addRadialGradient(mBaseAccentPaint, mAccentColor, mBaseColor);
        addSpunMetalEffectToPaint(mBaseAccentPaint);
        mBackgroundPaint = mBaseAccentPaint;
        // TODO: make the above only trigger when the colors actually change.
    }

    private void addSweepGradient(Paint paint, int colorA, int colorB) {
//        paint.setShader(new SweepGradient(mCenterX, mCenterY,
//                new int[]{colorA, colorB, colorA, colorB, colorA},
//                null));
        int[] gradient = new int[] {
                colorA,
                getIntermediateColor(colorA, colorB, 0.8d),
                getIntermediateColor(colorA, colorB, 0.6d),
                getIntermediateColor(colorA, colorB, 0.4d),
                getIntermediateColor(colorA, colorB, 0.2d),
                colorB,
                getIntermediateColor(colorA, colorB, 0.2d),
                getIntermediateColor(colorA, colorB, 0.4d),
                getIntermediateColor(colorA, colorB, 0.6d),
                getIntermediateColor(colorA, colorB, 0.8d),
                colorA,
                getIntermediateColor(colorA, colorB, 0.8d),
                getIntermediateColor(colorA, colorB, 0.6d),
                getIntermediateColor(colorA, colorB, 0.4d),
                getIntermediateColor(colorA, colorB, 0.2d),
                colorB,
                getIntermediateColor(colorA, colorB, 0.2d),
                getIntermediateColor(colorA, colorB, 0.4d),
                getIntermediateColor(colorA, colorB, 0.6d),
                getIntermediateColor(colorA, colorB, 0.8d),
                colorA
        };
        paint.setShader(new SweepGradient(mCenterX, mCenterY, gradient, null));
    }

    private void addRadialGradient(Paint paint, int colorA, int colorB) {
//        paint.setShader(new RadialGradient(mCenterX, mCenterY, mCenterY,
//                new int[]{colorB, colorB, colorB, colorA, colorA},
//                null, Shader.TileMode.CLAMP));

        int[] gradient = new int[] {
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
        paint.setShader(new RadialGradient(mCenterX, mCenterY, mCenterY, gradient, null,
                Shader.TileMode.CLAMP));
    }

    /**
     * Given two colors A and B, return an intermediate color between the two. The distance
     * between the two is given by "d"; 1.0 means return "colorA", 0.0 means return "colorB",
     * 0.5 means return something evenly between the two.
     *
     * For SDK 26 (Android O) and above, the calculation is done in the LAB color space for
     * extra perceptual accuracy!
     * @param colorA One color to calculate
     * @param colorB The other color
     * @param d The distance from colorB, between 0.0 and 1.0
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

    private void addSpunMetalEffectToPaint(Paint paint) {
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
            canvas.drawPath(p, paint);
        }

        paint.setShader(new BitmapShader(mAccentBasePaintBitmap,
                Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));
    }

    public Paint getPaintFromPreset(WatchFacePreset.Palette palette) {
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

//    public int getFillColor() {
//        return mFillColor;
//    }
//
//    public int getAccentColor() {
//        return mAccentColor;
//    }

    @Deprecated
    public int getHighlightColor() {
        return mHighlightColor;
    }

//    public int getBaseColor() {
//        return mBaseColor;
//    }
}
