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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorSpace;
import android.graphics.ComposeShader;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.graphics.Typeface;
import android.graphics.Xfermode;
import android.os.Build;
import android.util.SparseArray;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.ref.WeakReference;
import java.util.Objects;

import pro.watchkit.wearable.watchface.R;
import pro.watchkit.wearable.watchface.model.BytePackable.DigitSize;
import pro.watchkit.wearable.watchface.model.BytePackable.Style;
import pro.watchkit.wearable.watchface.model.BytePackable.StyleGradient;
import pro.watchkit.wearable.watchface.model.BytePackable.StyleTexture;
import pro.watchkit.wearable.watchface.model.BytePackable.TextStyle;

public final class PaintBox {
    private static final float AMBIENT_PAINT_STROKE_WIDTH_PERCENT = 0.333f; // 0.333%
    private static final float PAINT_STROKE_WIDTH_PERCENT = 0.5f; // 0.5%
    private int height = -1, width = -1;

    private float pc = 0f; // percent, set to 0.01f * height, all units are based on percent
    private float mCenterX, mCenterY;
    private Paint mFillPaint;
    private Paint mAccentPaint;
    private Paint mHighlightPaint;
    private Paint mBasePaint;
    private Paint mAmbientPaint;
    private Paint mShadowPaint;
    @NonNull
    private static SparseArray<WeakReference<Bitmap>> mBitmapCache = new SparseArray<>();
    @NonNull
    private GradientPaint mFillHighlightPaint = new GradientPaint();
    @NonNull
    private GradientPaint mAccentFillPaint = new GradientPaint();
    private GradientPaint mBezelPaint1;
    @NonNull
    private GradientPaint mBezelPaint2 = new GradientPaint();
    @NonNull
    private GradientPaint mAccentHighlightPaint = new GradientPaint();
    @NonNull
    private GradientPaint mBaseAccentPaint = new GradientPaint();
    private int mPreviousSerial = -1;
    private Context mContext;

    static {
        System.loadLibrary("native-lib");
    }

    native void nativeMapBitmap(Bitmap bitmap, int[] cLUT);

    PaintBox(Context context) {
        mContext = context;
        mFillPaint = newDefaultPaint();
        mAccentPaint = newDefaultPaint();
        mHighlightPaint = newDefaultPaint();
        mBasePaint = newDefaultPaint();

        mAmbientPaint = newDefaultPaint();
        mAmbientPaint.setStyle(Paint.Style.STROKE);
        mAmbientPaint.setColor(Color.WHITE); // Ambient is always white, we'll tint it in post.
//        mAmbientPaint.setShadowLayer(SHADOW_RADIUS, 0, 0, mBaseColor);

        mShadowPaint = newDefaultPaint();
        mShadowPaint.setStyle(Paint.Style.FILL);
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
    @ColorInt
    static int getIntermediateColor(@ColorInt int colorA, @ColorInt int colorB, double d) {
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

    /**
     * Given two colors A and B, fill an array with intermediate colors. The distance
     * between the two is given by "d"; 1.0 means return "colorA", 0.0 means return "colorB",
     * 0.5 means return something evenly between the two.
     * <p>
     * For SDK 26 (Android O) and above, the calculation is done in the LAB color space for
     * extra perceptual accuracy!
     *
     * @param colorA One color to calculate
     * @param colorB The other color
     * @param cLUT   The array to put the results into
     */
    private static void getIntermediateColor(
            @ColorInt int colorA, @ColorInt int colorB, @ColorInt int[] cLUT) {
        double j = (double) (cLUT.length - 1);

        // The "long colors" feature is only available in SDK 26 onwards!
        if (Build.VERSION.SDK_INT >= 26) {
            ColorSpace CIE_LAB = ColorSpace.get(ColorSpace.Named.CIE_LAB);
            ColorSpace sRGB = ColorSpace.get(ColorSpace.Named.SRGB);

            // Convert colors to LAB color space.
            long colorAL = Color.convert(colorA, CIE_LAB);
            long colorBL = Color.convert(colorB, CIE_LAB);

            for (int i = 0; i < cLUT.length; i++) {
                double d = (double) i / j;
                double e = 1d - d;

                // Generate a new color that is between the two.
                float a = (float) (Color.alpha(colorAL) * d + Color.alpha(colorBL) * e);
                float r = (float) (Color.red(colorAL) * d + Color.red(colorBL) * e);
                float g = (float) (Color.green(colorAL) * d + Color.green(colorBL) * e);
                float b = (float) (Color.blue(colorAL) * d + Color.blue(colorBL) * e);

                // Convert back to sRGB.
                cLUT[i] = Color.toArgb(Color.convert(r, g, b, a, CIE_LAB, sRGB));
            }
        } else {
            for (int i = 0; i < cLUT.length; i++) {
                double d = (double) i / j;
                double e = 1d - d;

                // Generate a new color that is between the two.
                int a = (int) (Color.alpha(colorA) * d + Color.alpha(colorB) * e);
                int r = (int) (Color.red(colorA) * d + Color.red(colorB) * e);
                int g = (int) (Color.green(colorA) * d + Color.green(colorB) * e);
                int b = (int) (Color.blue(colorA) * d + Color.blue(colorB) * e);

                // And add to array.
                cLUT[i] = Color.argb(a, r, g, b);
            }
        }
    }

    private int mFillSixBitColor, mAccentSixBitColor, mHighlightSixBitColor, mBaseSixBitColor;
    private int mAmbientDaySixBitColor, mAmbientNightSixBitColor;
    private StyleGradient mFillHighlightStyleGradient;
    private float mTextSize;
    @Nullable
    private Typeface mTypeface;

    @NonNull
    private static Paint newDefaultPaint() {
        Paint result = new Paint();
        result.setStrokeJoin(Paint.Join.ROUND);
        result.setStrokeCap(Paint.Cap.ROUND);
        result.setAntiAlias(true);
        result.setTextAlign(Paint.Align.CENTER);
        return result;
    }

    public void onWidthAndHeightChanged(int width, int height) {
        if (this.width == width && this.height == height) {
            return;
        }

        this.width = width;
        this.height = height;
        pc = 0.01f * Math.min(height, width);
        mCenterX = width / 2f;
        mCenterY = height / 2f;
    }

    /**
     * Get the given color from our 6-bit (64-color) palette. Returns a ColorInt.
     *
     * @param sixBitColor Index of the color from the palette, between 0 and 63
     * @return Color from our palette as a ColorInt
     */
    @ColorInt
    public int getColor(int sixBitColor) {
        return mContext.getResources().getIntArray(R.array.six_bit_colors)[sixBitColor];
    }

    /**
     * Get the name of the given color from our 6-bit (64-color) palette. Returns a ColorInt.
     *
     * @param sixBitColor Index of the color from the palette, between 0 and 63
     * @return Name of the color from our palette as a ColorInt
     */
    public String getColorName(int sixBitColor) {
        return mContext.getResources().getStringArray(R.array.six_bit_color_names)[sixBitColor];
    }

    private StyleGradient mAccentFillStyleGradient;
    private StyleGradient mAccentHighlightStyleGradient;
    private StyleGradient mBaseAccentStyleGradient;
    private StyleTexture mFillHighlightStyleTexture;
    private StyleTexture mAccentFillStyleTexture;
    private StyleTexture mAccentHighlightStyleTexture;
    private StyleTexture mBaseAccentStyleTexture;

    public Paint getAmbientPaint() {
        regeneratePaints2();
        return mAmbientPaint;
    }

    public Paint getShadowPaint() {
        regeneratePaints2();
        return mShadowPaint;
    }

    @NonNull
    public Paint getBezelPaint1() {
        regeneratePaints2();
        return mBezelPaint1;
    }

    @NonNull
    public Paint getBezelPaint2() {
        regeneratePaints2();
        return mBezelPaint2;
    }

    @NonNull
    Paint getPaintFromPreset(@NonNull TextStyle style) {
        regeneratePaints2();
        switch (style) {
            case FILL: {
                return mFillPaint;
            }
            case ACCENT: {
                return mAccentPaint;
            }
            case HIGHLIGHT: {
                return mHighlightPaint;
            }
            default:
            case BASE: {
                return mBasePaint;
            }
        }
    }

    @NonNull
    public Paint getPaintFromPreset(@NonNull Style style) {
        regeneratePaints2();
        switch (style) {
            case FILL_HIGHLIGHT: {
                return mFillHighlightPaint;
            }
            case ACCENT_FILL: {
                return mAccentFillPaint;
            }
            case ACCENT_HIGHLIGHT: {
                return mAccentHighlightPaint;
            }
            default:
            case BASE_ACCENT: {
                return mBaseAccentPaint;
            }
        }
    }

    void regeneratePaints(int fillSixBitColor, int accentSixBitColor,
                          int highlightSixBitColor, int baseSixBitColor,
                          int ambientDaySixBitColor, int ambientNightSixBitColor,
                          @NonNull StyleGradient fillHighlightStyleGradient,
                          @NonNull StyleGradient accentFillStyleGradient,
                          @NonNull StyleGradient accentHighlightStyleGradient,
                          @NonNull StyleGradient baseAccentStyleGradient,
                          @NonNull StyleTexture fillHighlightStyleTexture,
                          @NonNull StyleTexture accentFillStyleTexture,
                          @NonNull StyleTexture accentHighlightStyleTexture,
                          @NonNull StyleTexture baseAccentStyleTexture,
                          @NonNull DigitSize digitSize,
                          @Nullable Typeface typeface) {
        mFillSixBitColor = fillSixBitColor;
        mAccentSixBitColor = accentSixBitColor;
        mHighlightSixBitColor = highlightSixBitColor;
        mBaseSixBitColor = baseSixBitColor;
        mAmbientDaySixBitColor = ambientDaySixBitColor;
        mAmbientNightSixBitColor = ambientNightSixBitColor;
        mFillHighlightStyleGradient = fillHighlightStyleGradient;
        mAccentFillStyleGradient = accentFillStyleGradient;
        mAccentHighlightStyleGradient = accentHighlightStyleGradient;
        mBaseAccentStyleGradient = baseAccentStyleGradient;
        mFillHighlightStyleTexture = fillHighlightStyleTexture;
        mAccentFillStyleTexture = accentFillStyleTexture;
        mAccentHighlightStyleTexture = accentHighlightStyleTexture;
        mBaseAccentStyleTexture = baseAccentStyleTexture;

        // Set digit sizes.
        switch (digitSize) {
            case SMALL: {
                mTextSize = 4f * pc;
                break;
            }
            case MEDIUM: {
                mTextSize = 6f * pc;
                break;
            }
            case LARGE: {
                mTextSize = 8f * pc;
                break;
            }
            default:
            case X_LARGE: {
                mTextSize = 10f * pc;
                break;
            }
        }
        mTypeface = typeface;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                super.hashCode(),
                mFillSixBitColor,
                mAccentSixBitColor,
                mHighlightSixBitColor,
                mBaseSixBitColor,
                mAmbientDaySixBitColor,
                mAmbientNightSixBitColor,
                mFillHighlightStyleGradient,
                mAccentFillStyleGradient,
                mAccentHighlightStyleGradient,
                mBaseAccentStyleGradient,
                mFillHighlightStyleTexture,
                mAccentFillStyleTexture,
                mAccentHighlightStyleTexture,
                mBaseAccentStyleTexture,
                mTextSize,
                mTypeface,
                pc,
                height, width);
    }

    private void regeneratePaints2() {
        // Invalidate if any of our colors or styles have changed.
        int currentSerial = hashCode();
        if (mPreviousSerial == currentSerial || width <= 0 || height <= 0) {
            return;
        }

        mPreviousSerial = currentSerial;

        mFillPaint.setColor(getColor(mFillSixBitColor));
        mAccentPaint.setColor(getColor(mAccentSixBitColor));
        mHighlightPaint.setColor(getColor(mHighlightSixBitColor));
        mBasePaint.setColor(getColor(mBaseSixBitColor));

        mFillHighlightPaint.setColors(mFillSixBitColor, mHighlightSixBitColor,
                mFillHighlightStyleGradient, mFillHighlightStyleTexture);
        mAccentFillPaint.setColors(mAccentSixBitColor, mFillSixBitColor,
                mAccentFillStyleGradient, mAccentFillStyleTexture);
        mBezelPaint1 = mAccentFillPaint;
        mBezelPaint2.setColors(mFillSixBitColor, mAccentSixBitColor,
                mAccentFillStyleGradient, mAccentFillStyleTexture);
        mAccentHighlightPaint.setColors(mAccentSixBitColor, mHighlightSixBitColor,
                mAccentHighlightStyleGradient, mAccentHighlightStyleTexture);
        mBaseAccentPaint.setColors(mBaseSixBitColor, mAccentSixBitColor,
                mBaseAccentStyleGradient, mBaseAccentStyleTexture);

        mShadowPaint.setColor(getColor(mBaseSixBitColor));
        mShadowPaint.setShadowLayer(2f * pc, 0f, 0f, getColor(mBaseSixBitColor));

        // Regenerate stroke widths based on value of "percent"
        mFillHighlightPaint.setStrokeWidth(PAINT_STROKE_WIDTH_PERCENT * pc);
        mAccentFillPaint.setStrokeWidth(PAINT_STROKE_WIDTH_PERCENT * pc);
        mBezelPaint2.setStrokeWidth(PAINT_STROKE_WIDTH_PERCENT * pc);
        mAccentHighlightPaint.setStrokeWidth(PAINT_STROKE_WIDTH_PERCENT * pc);
        mBaseAccentPaint.setStrokeWidth(PAINT_STROKE_WIDTH_PERCENT * pc);
        mAmbientPaint.setStrokeWidth(AMBIENT_PAINT_STROKE_WIDTH_PERCENT * pc);

        setPaintTextAttributes(mFillPaint);
        setPaintTextAttributes(mAccentPaint);
        setPaintTextAttributes(mHighlightPaint);
        setPaintTextAttributes(mBasePaint);
        setPaintTextAttributes(mAmbientPaint);
        setPaintTextAttributes(mShadowPaint);

        setPaintTextAttributes(mFillHighlightPaint);
        setPaintTextAttributes(mAccentFillPaint);
        setPaintTextAttributes(mBezelPaint1);
        setPaintTextAttributes(mBezelPaint2);
        setPaintTextAttributes(mAccentHighlightPaint);
        setPaintTextAttributes(mBaseAccentPaint);
    }

    private void setPaintTextAttributes(Paint paint) {
        paint.setTextSize(mTextSize);
        paint.setTypeface(mTypeface);
        paint.setTextAlign(Paint.Align.CENTER);
    }

    private static Bitmap mTempBitmap;
    private static Canvas mTempCanvas;

    public enum ColorType {FILL, ACCENT, HIGHLIGHT, BASE, AMBIENT_DAY, AMBIENT_NIGHT}

    @NonNull
    private final Paint mBrushedEffectPaint = new Paint();
    @NonNull
    private final Path mBrushedEffectPath = new Path();
    @NonNull
    private final Paint mGradientH = new Paint();
    @NonNull
    private final Paint mGradientV = new Paint();
    @NonNull
    private final Xfermode mGradientTransferMode = new PorterDuffXfermode(Mode.DST_IN);
    @NonNull
    private final Xfermode mClearMode = new PorterDuffXfermode(Mode.CLEAR);
    @NonNull
    private final Paint mShadowLight = new Paint();
    @NonNull
    private final Paint mLightShadow = new Paint();

    /**
     * Prepare the temp bitmap and canvas for use. Call before using "mTempBitmap" or
     * "mTempCanvas".
     */
    private void prepareTempBitmapForUse() {
        if (mTempBitmap == null) {
            // Initialise on first use.
            mTempBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            mTempCanvas = new Canvas(mTempBitmap);
        } else if (mTempBitmap.getWidth() == width && mTempBitmap.getHeight() == height) {
            // Do nothing, our current bitmap is just right.
            return;
        } else if (mTempBitmap.getAllocationByteCount() <= width * height) {
            // Width and height changed and we can reconfigure to re-use this object.
            mTempCanvas.setBitmap(null);
            // Not sure above is technically needed but may cure esoteric bugs?
            mTempBitmap.reconfigure(width, height, Bitmap.Config.ARGB_8888);
            mTempCanvas.setBitmap(mTempBitmap);
        } else {
            // Width and height changed and we can't re-use this object, need a new one.
            mTempCanvas.setBitmap(null);
            // Not sure above is technically needed but may cure esoteric bugs?
            mTempBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            mTempCanvas.setBitmap(mTempBitmap);
        }
    }

    private class GradientPaint extends Paint {
        private int mCustomHashCode = -1;

        GradientPaint() {
            super();

            // From "newDefaultPaint".
            this.setStrokeJoin(Paint.Join.ROUND);
            this.setStrokeCap(Paint.Cap.ROUND);
            this.setAntiAlias(true);
            this.setTextAlign(Paint.Align.CENTER);
        }

        private void addSweepGradient(int colorA, int colorB) {
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
            int[] gradient = new int[]{
                    colorB, // Original
                    colorB,
                    colorB,
                    colorB,
                    colorB,
                    colorB, // Original
                    colorB,
                    colorB,
                    getIntermediateColor(colorA, colorB, 0.025d), // Taper it in
                    getIntermediateColor(colorA, colorB, 0.05d),
                    getIntermediateColor(colorA, colorB, 0.1d), // Not original
                    getIntermediateColor(colorA, colorB, 0.2d),
                    getIntermediateColor(colorA, colorB, 0.4d),
                    getIntermediateColor(colorA, colorB, 0.6d),
                    getIntermediateColor(colorA, colorB, 0.8d),
                    getIntermediateColor(colorA, colorB, 0.9d), // Not original
                    getIntermediateColor(colorA, colorB, 0.95d),
                    getIntermediateColor(colorA, colorB, 0.975d), // Taper it out
                    colorA,
                    colorA,
                    colorA // Original
            };
            setShader(new RadialGradient(mCenterX, mCenterY, mCenterY, gradient, null,
                    Shader.TileMode.CLAMP));
        }

        @SuppressWarnings("unused")
        private void addTriangleGradientFast(int colorA, int colorB) {
            // Fast version which uses sRGB gradients, which aren't very nice-looking.
            // The constants here can be tweaked a lot. Here's an initial implementation.
            int colorC = Color.TRANSPARENT;
            int[] gradient = new int[]{
                    getIntermediateColor(colorA, colorC, 0.8d),
                    getIntermediateColor(colorA, colorC, 1.0d), // Original
                    getIntermediateColor(colorA, colorC, 0.9d),
                    getIntermediateColor(colorA, colorC, 1.0d), // Original
                    getIntermediateColor(colorA, colorC, 0.9d),
                    getIntermediateColor(colorA, colorC, 0.7d),
                    getIntermediateColor(colorA, colorC, 0.8d),
                    getIntermediateColor(colorA, colorC, 0.6d),
                    getIntermediateColor(colorA, colorC, 0.4d), // Ripples!
                    getIntermediateColor(colorA, colorC, 0.5d),
                    getIntermediateColor(colorA, colorC, 0.2d), // Slightly out
                    getIntermediateColor(colorA, colorC, 0.3d), // of place!
                    getIntermediateColor(colorA, colorC, 0.1d),
                    getIntermediateColor(colorA, colorC, 0.0d), // Original
                    getIntermediateColor(colorA, colorC, 0.0d) // Original
            };
            float x1 = mCenterX - (mCenterX * (float) Math.sqrt(3) / 2f);
            float x2 = mCenterX + (mCenterX * (float) Math.sqrt(3) / 2f);
            float y = mCenterY + (mCenterY / 2f);
            float radius = mCenterY * 1.33333333333f;
            // Gradients A, B and C have an origin at the 12, 4 and 8 o'clock positions.
            Shader gradientA = new RadialGradient(
                    mCenterX, 0f, radius, gradient, null, Shader.TileMode.CLAMP);
            Shader gradientB = new RadialGradient(
                    x1, y, radius, gradient, null, Shader.TileMode.CLAMP);
            Shader gradientC = new RadialGradient(
                    x2, y, radius, gradient, null, Shader.TileMode.CLAMP);
            // The base shader is just one that's a flat colorB.
            Shader base = new RadialGradient(
                    mCenterX, mCenterY, radius, colorB, colorB, Shader.TileMode.CLAMP);
//            setShader(new ComposeShader(gradientA, new ComposeShader(
//                    gradientB, gradientC, Mode.SCREEN), Mode.DARKEN));
//            setShader(gradientA);
            setShader(
                    new ComposeShader(gradientA,
                            new ComposeShader(gradientB,
                                    new ComposeShader(gradientC, base, Mode.OVERLAY),
                                    Mode.OVERLAY),
                            Mode.OVERLAY));
        }

        private void addTriangleGradient(int colorA, int colorB) {
            setShader(new BitmapShader(generateTriangleGradient(colorA, colorB),
                    Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));
        }

        private Bitmap generateTriangleGradient(int colorA, int colorB) {
            // Calculate a modified hash code where mStyleTexture == StyleTexture.NONE.
            int modifiedCustomHashCode = Objects.hash(
                    colorA, colorB, StyleGradient.TRIANGLE, StyleTexture.NONE, height, width);
            // Attempt to return an existing bitmap from the cache if we have one.
            WeakReference<Bitmap> cache = mBitmapCache.get(modifiedCustomHashCode);
            if (cache != null) {
                // Well, we have an existing bitmap, but it may have been garbage collected...
                Bitmap result = cache.get();
                if (result != null) {
                    android.util.Log.d("Paint", "Returning cached triangle bitmap " +
                            modifiedCustomHashCode);
                    // It wasn't garbage collected! Return it.
                    return result;
                }
            }

            // Generate a new bitmap.
            Bitmap triangleBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas triangleCanvas = new Canvas(triangleBitmap);

            // Cache it for next time's use.
            mBitmapCache.put(modifiedCustomHashCode, new WeakReference<>(triangleBitmap));

            // Slow version which uses CIE LAB gradients, which look excellent.
            // We draw a black-to-white gradient then map that to a cLUT with the CIE LAB gradient.
            long time = System.nanoTime();
            StringBuilder sb = new StringBuilder();
            // The constants here can be tweaked a lot. Here's an initial implementation.
            // Colors range from between Color.BLACK and Color.TRANSPARENT.
            int[] gradient = new int[]{
                    Color.argb((int) (0.9f * 255f + 0.5f), 0, 0, 0),
                    Color.argb((int) (1.0f * 255f + 0.5f), 0, 0, 0), // Original
                    Color.argb((int) (0.9f * 255f + 0.5f), 0, 0, 0),
                    Color.argb((int) (0.7f * 255f + 0.5f), 0, 0, 0),
                    Color.argb((int) (0.8f * 255f + 0.5f), 0, 0, 0),
                    Color.argb((int) (0.6f * 255f + 0.5f), 0, 0, 0),
                    Color.argb((int) (0.4f * 255f + 0.5f), 0, 0, 0), // Ripples!
                    Color.argb((int) (0.5f * 255f + 0.5f), 0, 0, 0),
                    Color.argb((int) (0.2f * 255f + 0.5f), 0, 0, 0), // Slightly out
                    Color.argb((int) (0.3f * 255f + 0.5f), 0, 0, 0), // of place!
                    Color.argb((int) (0.1f * 255f + 0.5f), 0, 0, 0),
                    Color.argb((int) (0.0f * 255f + 0.5f), 0, 0, 0), // Original
                    Color.argb((int) (0.0f * 255f + 0.5f), 0, 0, 0) // Original
            };
            float x1 = mCenterX - (mCenterX * (float) Math.sqrt(3) / 2f);
            float x2 = mCenterX + (mCenterX * (float) Math.sqrt(3) / 2f);
            float y = mCenterY + (mCenterY / 2f);
            float radius = mCenterY * 1.33333333333f;
            // Gradients A, B and C have an origin at the 12, 4 and 8 o'clock positions.
            Shader gradientA = new RadialGradient(
                    mCenterX, 0f, radius, gradient, null, Shader.TileMode.CLAMP);
            Shader gradientB = new RadialGradient(
                    x1, y, radius, gradient, null, Shader.TileMode.CLAMP);
            Shader gradientC = new RadialGradient(
                    x2, y, radius, gradient, null, Shader.TileMode.CLAMP);

            mBrushedEffectPaint.reset();
            mBrushedEffectPaint.setShader(new ComposeShader(gradientA, new ComposeShader(
                    gradientB, gradientC, Mode.OVERLAY), Mode.OVERLAY));

//            prepareTempBitmapForUse();

            // Draw the gradient to the temp bitmap.
            triangleCanvas.drawColor(Color.WHITE);
            triangleCanvas.drawPaint(mBrushedEffectPaint);

            sb.append("Gradient: ").append((System.nanoTime() - time) / 1000000f);
            time = System.nanoTime();

            @ColorInt int[] cLUT = new int[256];
            getIntermediateColor(colorB, colorA, cLUT);

            sb.append(" ~ cLUT: ").append((System.nanoTime() - time) / 1000000f);
            time = System.nanoTime();

            nativeMapBitmap(triangleBitmap, cLUT);

//            // Go line by line through "triangleBitmap".
//            // For each line, get its pixels, convert it, then write it back.
//            // We unroll the loop to do 8 pixels at a time, which seems to help.
//            int widthMod8 = width + (width % 8 == 0 ? 0 : 8 - width % 8);
//            @ColorInt int[] pixels = new int[widthMod8];
//            IntStream.iterate(0, j -> j += 1).limit(height).forEach(j -> {
//                triangleBitmap.getPixels(pixels, 0, width, 0, j, width, 1);
//                IntStream.iterate(0, i -> i += 8).limit(widthMod8 / 8)
//                        .forEach(i -> {
//                            pixels[i] = cLUT[pixels[i] & 0xFF];
//                            pixels[i + 1] = cLUT[pixels[i + 1] & 0xFF];
//                            pixels[i + 2] = cLUT[pixels[i + 2] & 0xFF];
//                            pixels[i + 3] = cLUT[pixels[i + 3] & 0xFF];
//                            pixels[i + 4] = cLUT[pixels[i + 4] & 0xFF];
//                            pixels[i + 5] = cLUT[pixels[i + 5] & 0xFF];
//                            pixels[i + 6] = cLUT[pixels[i + 6] & 0xFF];
//                            pixels[i + 7] = cLUT[pixels[i + 7] & 0xFF];
//                        });
//                triangleBitmap.setPixels(pixels, 0, width, 0, j, width, 1);
//            });

            sb.append(" ~ p1: ").append((System.nanoTime() - time) / 1000000f);
            android.util.Log.d("Paint", sb.toString());

            return triangleBitmap;
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), mCustomHashCode);
        }

        void setColors(int sixBitColorA, int sixBitColorB,
                       @NonNull StyleGradient styleGradient,
                       @NonNull StyleTexture styleTexture) {
            @ColorInt int colorA = PaintBox.this.getColor(sixBitColorA);
            @ColorInt int colorB = PaintBox.this.getColor(sixBitColorB);

            mCustomHashCode = Objects.hash(
                    colorA, colorB, styleGradient, styleTexture, height, width);

            switch (styleGradient) {
                case FLAT:
                    // Set to "colorA", except if this is mAccentHighlightPaint.
                    // So our four paints have four distinct colors.
                    setColor(this == mAccentHighlightPaint ? colorB : colorA);
                    break;
                case SWEEP:
                    addSweepGradient(colorA, colorB);
                    break;
                case RADIAL:
                    addRadialGradient(colorA, colorB);
                    break;
                case TRIANGLE:
                    addTriangleGradient(colorA, colorB);
                    break;
            }

            switch (styleTexture) {
                case NONE:
                    break;
                case SPUN:
                    addSpunEffect();
                    break;
                case WEAVE:
                    addWeaveEffect();
                    break;
                case HEX:
                    break;
            }
        }

        private void addSpunEffect() {
            Bitmap brushedEffectBitmap = generateSpunEffect();

            setShader(new BitmapShader(brushedEffectBitmap,
                    Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));
        }

        private void addWeaveEffect() {
            Bitmap brushedEffectBitmap = generateWeaveEffect();

            setShader(new BitmapShader(brushedEffectBitmap,
                    Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));
        }

        private Bitmap generateSpunEffect() {
            // Attempt to return an existing bitmap from the cache if we have one.
            WeakReference<Bitmap> cache = mBitmapCache.get(mCustomHashCode);
            if (cache != null) {
                // Well, we have an existing bitmap, but it may have been garbage collected...
                Bitmap result = cache.get();
                if (result != null) {
                    // It wasn't garbage collected! Return it.
                    return result;
                }
            }

            // Generate a new bitmap.
            Bitmap brushedEffectBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas brushedEffectCanvas = new Canvas(brushedEffectBitmap);

            // Cache it for next time's use.
            mBitmapCache.put(mCustomHashCode, new WeakReference<>(brushedEffectBitmap));

            float percent = mCenterX / 50f;
            float offset = 0.5f * percent;
            int alpha = 50;
            float mCenter = Math.min(mCenterX, mCenterY);

            mBrushedEffectPaint.reset();
            mBrushedEffectPaint.setStyle(Style.STROKE);
            mBrushedEffectPaint.setStrokeWidth(offset);
            mBrushedEffectPaint.setStrokeJoin(Join.ROUND);
            mBrushedEffectPaint.setAntiAlias(true);

//            brushedEffectCanvas.drawPaint(this);

            // Spun metal circles?
            // 71 to cover the entire surface to the corners of a square device.
            float sqrt2 = (float) (Math.sqrt(2d));
            for (float max = 71f, i = max; i > 0f; i--) {
                mBrushedEffectPath.reset();
                mBrushedEffectPath.addCircle(mCenterX, mCenterY,
                        mCenter * sqrt2 * (i - 0.5f) / max, Path.Direction.CW);

                mBrushedEffectPath.offset(-offset, -offset);
                mBrushedEffectPaint.setColor(Color.WHITE);
                mBrushedEffectPaint.setAlpha(alpha);
                brushedEffectCanvas.drawPath(mBrushedEffectPath, mBrushedEffectPaint);

                mBrushedEffectPath.offset(2f * offset, 2f * offset);
                mBrushedEffectPaint.setColor(Color.BLACK);
                mBrushedEffectPaint.setAlpha(alpha);
                brushedEffectCanvas.drawPath(mBrushedEffectPath, mBrushedEffectPaint);

                mBrushedEffectPath.offset(-offset, -offset);
                brushedEffectCanvas.drawPath(mBrushedEffectPath, this);
            }
            return brushedEffectBitmap;
        }

        private Bitmap generateWeaveEffect() {
            // Attempt to return an existing bitmap from the cache if we have one.
            WeakReference<Bitmap> cache = mBitmapCache.get(mCustomHashCode);
            if (cache != null) {
                // Well, we have an existing bitmap, but it may have been garbage collected...
                Bitmap result = cache.get();
                if (result != null) {
                    // It wasn't garbage collected! Return it.
                    return result;
                }
            }

            // Generate a new bitmap.
            Bitmap brushedEffectBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas brushedEffectCanvas = new Canvas(brushedEffectBitmap);

            // Cache it for next time's use.
            mBitmapCache.put(mCustomHashCode, new WeakReference<>(brushedEffectBitmap));

            float percent = mCenterX / 50f;
            float offset = 0.25f * percent;
            float alphaMax = 70f;
            float alphaExtra = 40f;
//            float mCenter = Math.min(mCenterX, mCenterY);

            int prevAlpha = getAlpha();
            int weaves = 9, fibres = 7;

            float weaveSize = (float) width / (float) weaves;

            Shader vignette = new RadialGradient(
                    mCenterX, mCenterY, mCenterY,
                    new int[]{Color.BLACK, Color.BLACK, Color.TRANSPARENT},
                    new float[]{0f, 0.8f, 0.95f}, Shader.TileMode.CLAMP);

            mGradientH.reset();
            mGradientH.setShader(new ComposeShader(
                    vignette,
                    new LinearGradient(
                            width * 0.3f, 0f, width * 0.7f, height,
                            new int[]{Color.TRANSPARENT, Color.BLACK, Color.TRANSPARENT},
                            new float[]{0.2f, 0.5f, 0.8f}, Shader.TileMode.CLAMP),
                    Mode.SRC_IN));
            mGradientH.setXfermode(mGradientTransferMode);

            mGradientV.reset();
            mGradientV.setShader(new ComposeShader(
                    vignette,
                    new LinearGradient(
                            0f, height * 0.7f, width, height * 0.3f,
                            new int[]{Color.TRANSPARENT, Color.BLACK, Color.TRANSPARENT},
                            new float[]{0.2f, 0.5f, 0.8f}, Shader.TileMode.CLAMP),
                    Mode.SRC_IN));
            mGradientV.setXfermode(mGradientTransferMode);

//            ribH.reset();
//            ribH.setShader(new LinearGradient(
//                    mCenterX, mCenterY, mCenterX, mCenterY + weaveSize,
//                    new int[]{0x1F000000, 0x1FFFFFFF, 0x1F000000},
//                    new float[]{0f, 0.5f, 1f}, Shader.TileMode.REPEAT));
//
//            ribV.reset();
//            ribV.setShader(new LinearGradient(
//                    mCenterX, mCenterY, mCenterX + weaveSize, mCenterY,
//                    new int[]{0x1F000000, 0x1FFFFFFF, 0x1F000000},
//                    new float[]{0f, 0.5f, 1f}, Shader.TileMode.REPEAT));

            int cBlack = 0x33000000, cWhite = 0x33ffffff, cTrans = Color.TRANSPARENT;
            int[] shadow = new int[]{cBlack, cTrans, cTrans, cBlack};
            int[] light = new int[]{cWhite, cTrans, cTrans, cWhite};
            float[] stops = new float[]{0f, 0.1f, 0.9f, 1f};

            mShadowLight.reset();
            mShadowLight.setShader(new ComposeShader(
                    new LinearGradient(
                            0f, 0f, 0f, weaveSize,
                            shadow, stops, Shader.TileMode.REPEAT),
                    new LinearGradient(
                            0f, 0f, weaveSize, 0f,
                            light, stops, Shader.TileMode.REPEAT),
                    Mode.XOR));

            mLightShadow.reset();
            mLightShadow.setShader(new ComposeShader(
                    new LinearGradient(
                            0f, 0f, 0f, weaveSize,
                            light, stops, Shader.TileMode.REPEAT),
                    new LinearGradient(
                            0f, 0f, weaveSize, 0f,
                            shadow, stops, Shader.TileMode.REPEAT),
                    Mode.XOR));

            mBrushedEffectPaint.reset();
            mBrushedEffectPaint.setStyle(Style.STROKE);
            mBrushedEffectPaint.setStrokeWidth(offset);
            mBrushedEffectPaint.setStrokeJoin(Join.ROUND);
            mBrushedEffectPaint.setAntiAlias(true);

            brushedEffectCanvas.drawPaint(this);

            prepareTempBitmapForUse();

            // Zero out the temp canvas in preparation for next.
            mTempCanvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);

            // Horizontal
            for (int i = 0; i < weaves; i += 1) {
                float heightI = height / (float) weaves;
                float center = ((float) i + 0.5f) * heightI;

                for (int j = fibres * 2 - 1; j > 0; j -= 2) {
                    // Height = 100
                    // Fibres = 5;

                    // j = 9, 7, 5, 3, 1
                    // Heights = 100, 77, 55, 33, 11
                    float weightJ = (float) j / ((float) fibres * 2f - 1f);
                    float heightJ = weightJ * heightI;
                    int alpha = (int) (alphaMax - alphaExtra * weightJ);

                    float h = heightJ / 2f;
                    mBrushedEffectPaint.setStyle(Style.STROKE);

                    mBrushedEffectPath.reset();
                    mBrushedEffectPath.addRect(
                            0, center - h, width, center + h, Path.Direction.CW);

                    mBrushedEffectPath.offset(-offset, -offset);
                    mBrushedEffectPaint.setColor(Color.WHITE);
                    mBrushedEffectPaint.setAlpha(alpha);
                    mTempCanvas.drawPath(mBrushedEffectPath, mBrushedEffectPaint);

                    mBrushedEffectPath.offset(2f * offset, 2f * offset);
                    mBrushedEffectPaint.setColor(Color.BLACK);
                    mBrushedEffectPaint.setAlpha(alpha);
                    mTempCanvas.drawPath(mBrushedEffectPath, mBrushedEffectPaint);

                    mBrushedEffectPath.offset(-offset, -offset);
                    setAlpha(alpha);
                    mTempCanvas.drawPath(mBrushedEffectPath, this);
                }
            }

            // Apply ribs.
            mTempCanvas.drawPaint(mLightShadow);

            // Apply a gradient transfer mode.
            mTempCanvas.drawPaint(mGradientH);

            // Erase every 2nd square of the bitmap, and apply a transfer mode.
            mBrushedEffectPaint.setColor(Color.BLACK);
            mBrushedEffectPaint.setStyle(Style.FILL);
            mBrushedEffectPaint.setXfermode(mClearMode);
            for (int i = 0; i < weaves; i++) {
                float heightI = height / (float) weaves;
                float centerI = ((float) i + 0.5f) * heightI;
                float top = centerI - (heightI / 2f);
                float bottom = centerI + (heightI / 2f);

                for (int j = 0; j < weaves; j++) {
                    float widthJ = width / (float) weaves;
                    float centerJ = ((float) j + 0.5f) * widthJ;
                    float left = centerJ - (widthJ / 2f);
                    float right = centerJ + (widthJ / 2f);

                    if (i % 2 == j % 2) {
//                        Log.d("Erasing", "(" + i + "," + j + ")");
                        // Only every 2nd square
                        mTempCanvas.drawRect(left, top, right, bottom, mBrushedEffectPaint);
                    }
                }
            }
            mBrushedEffectPaint.setXfermode(null);

            // OK, transfer the horizontal stripes in "mTempCanvas" to "brushedEffectCanvas".
            brushedEffectCanvas.drawBitmap(mTempBitmap, 0f, 0f, null);

            // Apply a destination atop transfer mode to only draw into transparent bits.
//            Xfermode dstMode = new PorterDuffXfermode(Mode.DST_OVER);
//            mBrushedEffectPaint.setXfermode(dstMode);
//            mBrushedEffectPaint.setColor(Color.GREEN);
//            brushedEffectCanvas.drawPaint(mBrushedEffectPaint);

            // Zero out the temp canvas in preparation for next.
            mTempCanvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);

            // Vertical
            for (int i = 0; i < weaves; i += 1) {
                float widthI = width / (float) weaves;
                float center = ((float) i + 0.5f) * widthI;

                for (int j = fibres * 2 - 1; j > 0; j -= 2) {
                    // Height = 100
                    // Fibres = 5;

                    // j = 9, 7, 5, 3, 1
                    // Heights = 100, 77, 55, 33, 11
                    float weightJ = (float) j / ((float) fibres * 2f - 1f);
                    float widthJ = weightJ * widthI;
                    int alpha = (int) (alphaMax - alphaExtra * weightJ);

                    float w = widthJ / 2f;
                    mBrushedEffectPaint.setStyle(Style.STROKE);

                    mBrushedEffectPath.reset();
                    mBrushedEffectPath.addRect(
                            center - w, 0, center + w, height, Path.Direction.CW);

                    mBrushedEffectPath.offset(-offset, -offset);
                    mBrushedEffectPaint.setColor(Color.WHITE);
                    mBrushedEffectPaint.setAlpha(alpha);
                    mTempCanvas.drawPath(mBrushedEffectPath, mBrushedEffectPaint);

                    mBrushedEffectPath.offset(2f * offset, 2f * offset);
                    mBrushedEffectPaint.setColor(Color.BLACK);
                    mBrushedEffectPaint.setAlpha(alpha);
                    mTempCanvas.drawPath(mBrushedEffectPath, mBrushedEffectPaint);

                    mBrushedEffectPath.offset(-offset, -offset);
                    setAlpha(alpha);
                    mTempCanvas.drawPath(mBrushedEffectPath, this);
                }
            }

            // Apply ribs.
            mTempCanvas.drawPaint(mShadowLight);

            // Apply a gradient transfer mode.
            mTempCanvas.drawPaint(mGradientV);

            // Erase every OTHER other 2nd square.
            mBrushedEffectPaint.setColor(Color.BLACK);
            mBrushedEffectPaint.setStyle(Style.FILL);
            mBrushedEffectPaint.setXfermode(mClearMode);
            for (int i = 0; i < weaves; i++) {
                float heightI = height / (float) weaves;
                float centerI = ((float) i + 0.5f) * heightI;
                float top = centerI - (heightI / 2f);
                float bottom = centerI + (heightI / 2f);

                for (int j = 0; j < weaves; j++) {
                    float widthJ = width / (float) weaves;
                    float centerJ = ((float) j + 0.5f) * widthJ;
                    float left = centerJ - (widthJ / 2f);
                    float right = centerJ + (widthJ / 2f);

                    if (i % 2 != j % 2) { // Other!
//                        Log.d("Erasing", "(" + i + "," + j + ")");
                        // Only every 2nd square
                        mTempCanvas.drawRect(left, top, right, bottom, mBrushedEffectPaint);
                    }
                }
            }
            mBrushedEffectPaint.setXfermode(null);

            // OK, transfer the vertical stripes in "mTempCanvas" to "brushedEffectCanvas".
            brushedEffectCanvas.drawBitmap(mTempBitmap, 0f, 0f, null);

            setAlpha(prevAlpha);

            return brushedEffectBitmap;
        }
    }
}
