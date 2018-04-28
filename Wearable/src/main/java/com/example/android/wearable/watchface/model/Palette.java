package com.example.android.wearable.watchface.model;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.SweepGradient;

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
        paint.setShader(new SweepGradient(mCenterX, mCenterY,
                new int[]{colorA, colorB, colorA, colorB, colorA},
                null));
    }

    private void addRadialGradient(Paint paint, int colorA, int colorB) {
        paint.setShader(new RadialGradient(mCenterX, mCenterY, mCenterY,
                new int[]{colorB, colorB, colorB, colorA, colorA},
                null, Shader.TileMode.CLAMP));
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
