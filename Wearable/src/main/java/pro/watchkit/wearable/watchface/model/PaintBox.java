/*
 * Copyright (C) 2018-2020 Terence Tan
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
import android.renderscript.Allocation;
import android.renderscript.RenderScript;
import android.renderscript.Short4;
import android.util.SparseArray;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import pro.watchkit.wearable.watchface.R;
import pro.watchkit.wearable.watchface.model.BytePackable.DigitSize;
import pro.watchkit.wearable.watchface.model.BytePackable.Material;
import pro.watchkit.wearable.watchface.model.BytePackable.MaterialGradient;
import pro.watchkit.wearable.watchface.model.BytePackable.MaterialTexture;
import pro.watchkit.wearable.watchface.model.BytePackable.TextStyle;

public final class PaintBox {
    // private static final String TAG = "PaintBox";
    private static final float AMBIENT_PAINT_STROKE_WIDTH_PERCENT = 0.333f; // 0.333%
    private static final float PAINT_STROKE_WIDTH_PERCENT = 0.5f; // 0.5%
    private int height = -1, width = -1;

    private float pc = 0f; // percent, set to 0.01f * height, all units are based on percent
    private float mCenterX, mCenterY;
    @NonNull
    private final Paint mFillPaint;
    @NonNull
    private final Paint mAccentPaint;
    @NonNull
    private final Paint mHighlightPaint;
    @NonNull
    private final Paint mBasePaint;
    @NonNull
    private final Paint mAmbientPaint;
    @NonNull
    private final Paint mAmbientPaintFaded;
    @NonNull
    private final Paint mShadowPaint;
    @NonNull
    private static final SparseArray<WeakReference<Bitmap>> mBitmapCache = new SparseArray<>();
    @NonNull
    private static final SparseArray<WeakReference<BitmapShader>> mBitmapShaderCache = new SparseArray<>();
    @NonNull
    private final GradientPaint mFillHighlightPaint = new GradientPaint();
    @NonNull
    private final GradientPaint mAccentFillPaint = new GradientPaint();
    private GradientPaint mBezelPaint1;
    @NonNull
    private final GradientPaint mBezelPaint2 = new GradientPaint();
    @NonNull
    private final GradientPaint mAccentHighlightPaint = new GradientPaint();
    @NonNull
    private final GradientPaint mBaseAccentPaint = new GradientPaint();
    private int mPreviousSerial = -1;
    private final Context mContext;

    private static RenderScript mRenderScript;
    private static ScriptC_mapBitmap mScriptC_mapBitmap;
    @Nullable
    private static WeakReference<Bitmap> mTriangleGradientBitmapRef;

//    static {
//        System.loadLibrary("native-lib");
//    }

//    native void nativeMapBitmap(Bitmap bitmap, int[] cLUT);

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

        mAmbientPaintFaded = newDefaultPaint();
        mAmbientPaintFaded.setStyle(Paint.Style.STROKE);
        mAmbientPaintFaded.setColor(Color.GRAY); // Ambient dim is always gray.

        mShadowPaint = newDefaultPaint();
        mShadowPaint.setStyle(Paint.Style.FILL);
    }

    /**
     * Reference constants for the CIELUV colorspace conversions. D65 illuminant, 2 degrees.
     */
    private final static double Reference_X = 95.047d,
            Reference_Y = 100.000d, Reference_Z = 108.883d;

    /**
     * Reference constant "ref_U" for the CIELUV colorspace conversions.
     */
    private final static double ref_U =
            (4d * Reference_X) / (Reference_X + (15d * Reference_Y) + (3d * Reference_Z));

    /**
     * Reference constant "ref_V" for the CIELUV colorspace conversions.
     */
    private final static double ref_V =
            (9d * Reference_Y) / (Reference_X + (15d * Reference_Y) + (3d * Reference_Z));

    /**
     * Converts the given color from the sRGB colorspace to CIELUV.
     *
     * @param color The color as an sRGB ColorInt
     * @return The color in the CIELUV colorspace as a four-element array, representing alpha, L,
     * u and v channels
     */
    @NonNull
    private static double[] convertSRGBToLUV(@ColorInt int color) {
        int sRGB_A = Color.alpha(color);
        int sRGB_R = Color.red(color);
        int sRGB_G = Color.green(color);
        int sRGB_B = Color.blue(color);

        // Shortcut -- black causes divide-by-zero, so just return zero.
        if (sRGB_R + sRGB_G + sRGB_B == 0) {
            return new double[]{(double) sRGB_A, 0d, 0d, 0d};
        }

        // Convert sRGB to XYZ...
        double var_R = (double) sRGB_R / 255d;
        double var_G = (double) sRGB_G / 255d;
        double var_B = (double) sRGB_B / 255d;

        var_R = 100d * (var_R > 0.04045d ?
                Math.pow((var_R + 0.055d) / 1.055d, 2.4d) : var_R / 12.92d);
        var_G = 100d * (var_G > 0.04045d ?
                Math.pow((var_G + 0.055d) / 1.055d, 2.4d) : var_G / 12.92d);
        var_B = 100d * (var_B > 0.04045d ?
                Math.pow((var_B + 0.055d) / 1.055d, 2.4d) : var_B / 12.92d);

        double X = var_R * 0.4124d + var_G * 0.3576d + var_B * 0.1805d;
        double Y = var_R * 0.2126d + var_G * 0.7152d + var_B * 0.0722d;
        double Z = var_R * 0.0193d + var_G * 0.1192d + var_B * 0.9505d;

        // Convert XYZ to LUV...
        double var_U = (4d * X) / (X + (15d * Y) + (3d * Z));
        double var_V = (9d * Y) / (X + (15d * Y) + (3d * Z));

        double var_Y = Y / 100d;
        var_Y = var_Y > (216d / 24389d) ?
                Math.pow(var_Y, 1d / 3d) : ((var_Y * 24389d / 3132d) + (16d / 116d));

        double CIE_L = (116d * var_Y) - 16d;
        double CIE_u = 13d * CIE_L * (var_U - ref_U);
        double CIE_v = 13d * CIE_L * (var_V - ref_V);

        return new double[]{(double) sRGB_A, CIE_L, CIE_u, CIE_v};
    }

    /**
     * Converts the given color from the CIELUV colorspace to sRGB.
     *
     * @param LUV The color in the CIELUV colorspace as a four-element array, representing alpha,
     *            L, u and v channels
     * @return The color as an sRGB ColorInt
     */
    @ColorInt
    private static int convertLUVToSRGB(double[] LUV) {
        // Convert LUV to XYZ...
        double var_Y = (LUV[1] + 16d) / 116d;
        var_Y = Math.pow(var_Y, 3d) > (216d / 24389d) ?
                Math.pow(var_Y, 3d) : ((var_Y - 16d / 116d) * 3132d / 24389d);

        double var_U = LUV[2] / (13d * LUV[1]) + ref_U;
        double var_V = LUV[3] / (13d * LUV[1]) + ref_V;

        double Y = var_Y * 100d;
        double X = 0d - (9d * Y * var_U) / ((var_U - 4d) * var_V - var_U * var_V);
        double Z = (9d * Y - (15d * var_V * Y) - (var_V * X)) / (3d * var_V);

        // Convert XYZ to sRGB...
        double var_X = X / 100d;
        var_Y = Y / 100d;
        double var_Z = Z / 100d;

        double var_R = var_X * 3.2406d + var_Y * -1.5372d + var_Z * -0.4986d;
        double var_G = var_X * -0.9689d + var_Y * 1.8758d + var_Z * 0.0415d;
        double var_B = var_X * 0.0557d + var_Y * -0.2040d + var_Z * 1.0570d;

        var_R = var_R > 0.0031308d ?
                1.055 * Math.pow(var_R, 1d / 2.4d) - 0.055d : 12.92d * var_R;
        var_G = var_G > 0.0031308d ?
                1.055 * Math.pow(var_G, 1d / 2.4d) - 0.055d : 12.92d * var_G;
        var_B = var_B > 0.0031308d ?
                1.055 * Math.pow(var_B, 1d / 2.4d) - 0.055d : 12.92d * var_B;

        return Color.argb(
                (int) LUV[0], (int) (var_R * 255d), (int) (var_G * 255d), (int) (var_B * 255d));
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

        if (mUseLUV) {
            double[] colorA2 = convertSRGBToLUV(colorA);
            double[] colorB2 = convertSRGBToLUV(colorB);
            double[] colorC2 = {0d, 0d, 0d, 0d};
            colorC2[0] = colorA2[0] * d + colorB2[0] * e;
            colorC2[1] = colorA2[1] * d + colorB2[1] * e;
            colorC2[2] = colorA2[2] * d + colorB2[2] * e;
            colorC2[3] = colorA2[3] * d + colorB2[3] * e;
            return convertLUVToSRGB(colorC2);
        } else if (Build.VERSION.SDK_INT >= 26) {
            // The "long colors" feature is only available in SDK 26 onwards!
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
            @ColorInt int colorA, @ColorInt int colorB, @NonNull @ColorInt int[] cLUT) {
        double j = cLUT.length - 1;

        if (mUseLUV) {
            double[] colorA2 = convertSRGBToLUV(colorA);
            double[] colorB2 = convertSRGBToLUV(colorB);
            double[] colorC2 = {0d, 0d, 0d, 0d};

            for (int i = 0; i < cLUT.length; i++) {
                double d = (double) i / j;
                double e = 1d - d;
                colorC2[0] = colorA2[0] * d + colorB2[0] * e;
                colorC2[1] = colorA2[1] * d + colorB2[1] * e;
                colorC2[2] = colorA2[2] * d + colorB2[2] * e;
                colorC2[3] = colorA2[3] * d + colorB2[3] * e;
                cLUT[i] = convertLUVToSRGB(colorC2);
            }
        } else if (Build.VERSION.SDK_INT >= 26) {
            // The "long colors" feature is only available in SDK 26 onwards!
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
    private MaterialGradient mFillHighlightMaterialGradient;
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
        if (mUseLUV) {
            return mContext.getResources().getIntArray(R.array.six_bit_colors)[sixBitColor];
        } else {
            return mContext.getResources().getIntArray(R.array.six_bit_colors_v1)[sixBitColor];
        }
    }

    /**
     * Get the name of the given color from our 6-bit (64-color) palette. Returns a ColorInt.
     *
     * @param sixBitColor Index of the color from the palette, between 0 and 63
     * @return Name of the color from our palette as a ColorInt
     */
    public String getColorName(int sixBitColor) {
        if (mUseLUV) {
            return mContext.getResources().getStringArray(
                    R.array.six_bit_color_names)[sixBitColor];
        } else {
            return mContext.getResources().getStringArray(
                    R.array.six_bit_color_names_v1)[sixBitColor];
        }
    }

    private MaterialGradient mAccentFillMaterialGradient;
    private MaterialGradient mAccentHighlightMaterialGradient;
    private MaterialGradient mBaseAccentMaterialGradient;
    private MaterialTexture mFillHighlightMaterialTexture;
    private MaterialTexture mAccentFillMaterialTexture;
    private MaterialTexture mAccentHighlightMaterialTexture;
    private MaterialTexture mBaseAccentMaterialTexture;

    @NonNull
    public Paint getAmbientPaint() {
        regeneratePaints2();
        return mAmbientPaint;
    }

    @NonNull
    public Paint getAmbientPaintFaded() {
        regeneratePaints2();
        return mAmbientPaintFaded;
    }

    @NonNull
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
    Paint getPaintFromPreset(@NonNull TextStyle textStyle) {
        regeneratePaints2();
        switch (textStyle) {
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
    public Paint getPaintFromPreset(@NonNull Material material) {
        regeneratePaints2();
        switch (material) {
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
                          @NonNull MaterialGradient fillHighlightMaterialGradient,
                          @NonNull MaterialGradient accentFillMaterialGradient,
                          @NonNull MaterialGradient accentHighlightMaterialGradient,
                          @NonNull MaterialGradient baseAccentMaterialGradient,
                          @NonNull MaterialTexture fillHighlightMaterialTexture,
                          @NonNull MaterialTexture accentFillMaterialTexture,
                          @NonNull MaterialTexture accentHighlightMaterialTexture,
                          @NonNull MaterialTexture baseAccentMaterialTexture,
                          @NonNull DigitSize digitSize,
                          @Nullable Typeface typeface) {
        mFillSixBitColor = fillSixBitColor;
        mAccentSixBitColor = accentSixBitColor;
        mHighlightSixBitColor = highlightSixBitColor;
        mBaseSixBitColor = baseSixBitColor;
        mAmbientDaySixBitColor = ambientDaySixBitColor;
        mAmbientNightSixBitColor = ambientNightSixBitColor;
        mFillHighlightMaterialGradient = fillHighlightMaterialGradient;
        mAccentFillMaterialGradient = accentFillMaterialGradient;
        mAccentHighlightMaterialGradient = accentHighlightMaterialGradient;
        mBaseAccentMaterialGradient = baseAccentMaterialGradient;
        mFillHighlightMaterialTexture = fillHighlightMaterialTexture;
        mAccentFillMaterialTexture = accentFillMaterialTexture;
        mAccentHighlightMaterialTexture = accentHighlightMaterialTexture;
        mBaseAccentMaterialTexture = baseAccentMaterialTexture;

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
                mFillHighlightMaterialGradient,
                mAccentFillMaterialGradient,
                mAccentHighlightMaterialGradient,
                mBaseAccentMaterialGradient,
                mFillHighlightMaterialTexture,
                mAccentFillMaterialTexture,
                mAccentHighlightMaterialTexture,
                mBaseAccentMaterialTexture,
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
                mFillHighlightMaterialGradient, mFillHighlightMaterialTexture);
        mAccentFillPaint.setColors(mAccentSixBitColor, mFillSixBitColor,
                mAccentFillMaterialGradient, mAccentFillMaterialTexture);
        mBezelPaint1 = mAccentFillPaint;
        mBezelPaint2.setColors(mFillSixBitColor, mAccentSixBitColor,
                mAccentFillMaterialGradient, mAccentFillMaterialTexture);
        mAccentHighlightPaint.setColors(mAccentSixBitColor, mHighlightSixBitColor,
                mAccentHighlightMaterialGradient, mAccentHighlightMaterialTexture);
        mBaseAccentPaint.setColors(mBaseSixBitColor, mAccentSixBitColor,
                mBaseAccentMaterialGradient, mBaseAccentMaterialTexture);

        mShadowPaint.setColor(getColor(mBaseSixBitColor));
        mShadowPaint.setShadowLayer(2f * pc, 0f, 0f, getColor(mBaseSixBitColor));

        // Regenerate stroke widths based on value of "percent"
        mFillHighlightPaint.setStrokeWidth(PAINT_STROKE_WIDTH_PERCENT * pc);
        mAccentFillPaint.setStrokeWidth(PAINT_STROKE_WIDTH_PERCENT * pc);
        mBezelPaint2.setStrokeWidth(PAINT_STROKE_WIDTH_PERCENT * pc);
        mAccentHighlightPaint.setStrokeWidth(PAINT_STROKE_WIDTH_PERCENT * pc);
        mBaseAccentPaint.setStrokeWidth(PAINT_STROKE_WIDTH_PERCENT * pc);
        mAmbientPaint.setStrokeWidth(AMBIENT_PAINT_STROKE_WIDTH_PERCENT * pc);
        mAmbientPaintFaded.setStrokeWidth(AMBIENT_PAINT_STROKE_WIDTH_PERCENT * pc);

        setPaintTextAttributes(mFillPaint);
        setPaintTextAttributes(mAccentPaint);
        setPaintTextAttributes(mHighlightPaint);
        setPaintTextAttributes(mBasePaint);
        setPaintTextAttributes(mAmbientPaint);
        setPaintTextAttributes(mAmbientPaintFaded);
        setPaintTextAttributes(mShadowPaint);

        setPaintTextAttributes(mFillHighlightPaint);
        setPaintTextAttributes(mAccentFillPaint);
        setPaintTextAttributes(mBezelPaint1);
        setPaintTextAttributes(mBezelPaint2);
        setPaintTextAttributes(mAccentHighlightPaint);
        setPaintTextAttributes(mBaseAccentPaint);

//        android.util.Log.d("PaintBox", String.format(
//                "regeneratePaints: %d %d %d %d --> #%03x%03x --> %s",
//                mFillSixBitColor, mAccentSixBitColor, mHighlightSixBitColor, mBaseSixBitColor,
//                (mFillSixBitColor << 6) + mAccentSixBitColor,
//                (mHighlightSixBitColor << 6) + mBaseSixBitColor,
//                getPaletteName(mFillSixBitColor, mAccentSixBitColor, mHighlightSixBitColor, mBaseSixBitColor)));
    }

    /**
     * Our palette lookup table to map palette value to palette name.
     */
    private static Map<Integer, String> mPalettes;

    /**
     * Our hard-coded palette variant names.
     */
    private final static char[] mPaletteVariants = {
            ' ', // Variant 0 isn't used as it's not a variant!
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
            'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'
    };

    /**
     * Our hard-coded default palette names. Corresponds to the palette values.
     * TODO: move this to resources.
     */
    private final static String[] mDefaultPaletteNames = {
            "Pacific Sunset", "Amethyst Amber", "Country Club", "Orchid Sky"
    };

    /**
     * Our hard-coded default palette values. Corresponds to the palette names.
     * TODO: move this to resources.
     */
    private final static int[] mDefaultPaletteValues = {
            0x6c5e01, 0x813e00, 0xf84e00, 0x993fca
    };

    @NonNull
    String getPaletteName() {
        return PaintBox.getPaletteName(
                mFillSixBitColor, mAccentSixBitColor, mHighlightSixBitColor, mBaseSixBitColor);
    }

    /**
     * Given a series of palette value, return its name if it has one, or a generic
     * string if it hasn't.
     *
     * @param w Color 1 in the palette
     * @param x Color 2 in the palette
     * @param y Color 3 in the palette
     * @param z Color 4 in the palette
     * @return Name of palette
     */
    @NonNull
    private static String getPaletteName(int w, int x, int y, int z) {
        return getPaletteName(combinePalette(w, x, y, z));
    }

    /**
     * Given a palette value, return its name if it has one, or a generic
     * string if it hasn't/
     *
     * @param palette Palette to name
     * @return Name of palette
     */
    @NonNull
    private static String getPaletteName(int palette) {
        if (mPalettes == null) {
            // Initialise on first use
            mPalettes = new HashMap<>();
            StringBuilder sb = new StringBuilder();

            // Loop through each default palette name and value.
            // Add original named palettes first, before we add any permutations.
            for (int i = 0; i < mDefaultPaletteNames.length; i++) {
                mPalettes.putIfAbsent(mDefaultPaletteValues[i], mDefaultPaletteNames[i]);
            }

            // Loop through each default palette name and value.
            // Add all permutations.
            for (int i = 0; i < mDefaultPaletteNames.length; i++) {
                // Generate heaps of permutations.
                int[] p = getPalettePermutations(mDefaultPaletteValues[i]);
                for (int j = 1; j < p.length; j++) {
                    // We start j at 1, as 0 is the unmodified permutation (already added).
                    sb.setLength(0); // Clear the StringBuilder
                    // Generate the palette name plus permutations if applicable.
                    sb.append(mDefaultPaletteNames[i]);
                    // Append variant name.
                    sb.append(" (variant ").append(mPaletteVariants[j]).append(')');
                    mPalettes.putIfAbsent(p[j], sb.toString());
                }
            }

//            sb.setLength(0);
//            for (Map.Entry<Integer, String> e : mPalettes.entrySet()) {
//                sb.append(String.format("#%06x", e.getKey()));
//                sb.append(" --> ").append(e.getValue()).append(System.lineSeparator());
//            }
//            android.util.Log.d("PaintBox", sb.toString());
        }

        if (mPalettes.containsKey(palette)) {
            return Objects.requireNonNull(mPalettes.get(palette));
        } else {
            return String.format("#%06x", palette);
        }
    }

    /**
     * Given a palette value, return an array with it and the 23 other possible
     * permutations of this palette
     *
     * @param palette Palette to permute
     * @return Array of 24 palettes which are permutations of the input
     */
    @NonNull
    private static int[] getPalettePermutations(int palette) {
        // Separate "palette" into our 4 six-bit colours.
        int a = (palette >> 18) & 63;
        int b = (palette >> 12) & 63;
        int c = (palette >> 6) & 63;
        int d = palette & 63;

        // The permutations below have a very particular order
        // Each permutation swaps the position of two values from the permutation above,
        // leaving two values unchanged.
        // And, every every second permutation is a swap of 1&2, leaving 3&4 unchanged.
        // That's important because 3&4 are the background, so the background won't change.

        return new int[]{
                palette, //    a, b, c, d              swap 2&3
                combinePalette(b, a, c, d), // swap 1&2
                combinePalette(d, a, c, b), //         swap 1&4
                combinePalette(a, d, c, b), // swap 1&2
                combinePalette(b, d, c, a), //         swap 1&4
                combinePalette(d, b, c, a), // swap 1&2
                combinePalette(c, b, d, a), //         swap 1&3
                combinePalette(b, c, d, a), // swap 1&2
                combinePalette(b, a, d, c), //         swap 2&4
                combinePalette(a, b, d, c), // swap 1&2
                combinePalette(a, c, d, b), //         swap 2&4
                combinePalette(c, a, d, b), // swap 1&2
                combinePalette(c, d, a, b), //         swap 2&3
                combinePalette(d, c, a, b), // swap 1&2
                combinePalette(b, c, a, d), //         swap 1&4
                combinePalette(c, b, a, d), // swap 1&2
                combinePalette(d, b, a, c), //         swap 1&4
                combinePalette(b, d, a, c), // swap 1&2
                combinePalette(a, d, b, c), //         swap 1&3
                combinePalette(d, a, b, c), // swap 1&2
                combinePalette(d, c, b, a), //         swap 2&4
                combinePalette(c, d, b, a), // swap 1&2
                combinePalette(c, a, b, d), //         swap 2&4
                combinePalette(a, c, b, d)  // swap 1&2
        };
    }

    /**
     * Convenience function to pack w, x, y, z into a single 24-bit int for palettes.
     *
     * @param w Color 1 in the palette
     * @param x Color 2 in the palette
     * @param y Color 3 in the palette
     * @param z Color 4 in the palette
     * @return Packed palette value of w, x, y and z.
     */
    private static int combinePalette(int w, int x, int y, int z) {
        return ((w << 18) + (x << 12) + (y << 6) + z) & 0xFFFFFF;
    }

    private void setPaintTextAttributes(@NonNull Paint paint) {
        paint.setTextSize(mTextSize);
        paint.setTypeface(mTypeface);
        paint.setTextAlign(Paint.Align.CENTER);
    }

    private static Bitmap mTempBitmap;
    private static Canvas mTempCanvas;

    private boolean mUseLegacyEffects;

    void setUseLegacyEffects(boolean useLegacyEffects) {
        mUseLegacyEffects = useLegacyEffects;
    }

    private static boolean mUseLUV;

    void setUseLegacyColorDrawingNotLUV(boolean useLegacyColorDrawing) {
        mUseLUV = !useLegacyColorDrawing;
    }

    public enum ColorType {FILL, ACCENT, HIGHLIGHT, BASE, AMBIENT_DAY, AMBIENT_NIGHT}

    @NonNull
    private final Paint mBrushedEffectPaint = new Paint();
    @NonNull
    private final Path mBrushedEffectPath = new Path();
    @NonNull
    private final Path mBrushedEffectPathUpper = new Path();
    @NonNull
    private final Path mBrushedEffectPathLower = new Path();
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
            // (Turn off overly nit-picky inspection. The logic reads better this way.)
            //noinspection UnnecessaryReturnStatement
            return;
        } else if (mTempBitmap.getAllocationByteCount() > width * height) {
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
            // Calculate a modified hash code where mMaterialTexture == MaterialTexture.NONE.
            int modifiedCustomHashCode = Objects.hash(
                    colorA, colorB, MaterialGradient.TRIANGLE, MaterialTexture.NONE, height, width);
            // Attempt to return an existing bitmap from the cache if we have one.
            WeakReference<Bitmap> cache = mBitmapCache.get(modifiedCustomHashCode);
            if (cache != null) {
                // Well, we have an existing bitmap, but it may have been garbage collected...
                Bitmap result = cache.get();
                if (result != null) {
//                    Log.d(TAG, "Returning cached triangle bitmap " + modifiedCustomHashCode);
                    // It wasn't garbage collected! Return it.
                    return result;
                }
            }

            long time = System.nanoTime();
            StringBuilder sb = new StringBuilder();

            // Generate a new bitmap.
            // Extra padding to make width mod 4, for RenderScript.
            int extra = width % 4 == 0 ? 0 : 4 - width % 4;
            Bitmap triangleBitmap = Bitmap.createBitmap(
                    width + extra, height, Bitmap.Config.ARGB_8888);
            // Cache it for next time's use.
            mBitmapCache.put(modifiedCustomHashCode, new WeakReference<>(triangleBitmap));

            // A new bitmap for the triangle gradient pattern.
            // Unlike the triangle bitmap above (which will be transformed with our colors),
            // this triangle gradient bitmap doesn't change from run to run. Therefore we cache it.
            Bitmap triangleGradientBitmap = null;
            if (mTriangleGradientBitmapRef != null) {
                triangleGradientBitmap = mTriangleGradientBitmapRef.get();
            }
//            if (mTriangleGradientBitmapRef == null) {
//                sb.append("(no weak ref) ");
//            } else if (mTriangleGradientBitmapRef.get() == null) {
//                sb.append("(garbage collected) ");
//            } else if (triangleGradientBitmap.getHeight() != height) {
//                sb.append("(height ").append(triangleGradientBitmap.getHeight());
//                sb.append(" != ").append(height).append(") ");
//            } else if (triangleGradientBitmap.getWidth() != width + extra) {
//                sb.append("(width ").append(triangleGradientBitmap.getWidth());
//                sb.append(" != ").append(width + extra).append(") ");
//            }
            if (triangleGradientBitmap == null ||
                    triangleGradientBitmap.getHeight() != height ||
                    triangleGradientBitmap.getWidth() != width + extra) {
                triangleGradientBitmap = Bitmap.createBitmap(
                        width + extra, height, Bitmap.Config.ARGB_8888);
                mTriangleGradientBitmapRef = new WeakReference<>(triangleGradientBitmap);
                Canvas triangleCanvas = new Canvas(triangleGradientBitmap);

                // Slow version which uses CIE LAB gradients, which look excellent. We draw
                // a black-to-white gradient then map that to a cLUT with the CIE LAB gradient.
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
            }

            sb.append("Gradient: ").append((System.nanoTime() - time) / 1000000f);
            time = System.nanoTime();

            @ColorInt int[] cLUT = new int[256];
            getIntermediateColor(colorB, colorA, cLUT);

            sb.append(" ~ cLUT: ").append((System.nanoTime() - time) / 1000000f);
            time = System.nanoTime();

//            nativeMapBitmap(triangleBitmap, cLUT);

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
            time = System.nanoTime();

            if (mRenderScript == null) {
                mRenderScript = RenderScript.create(mContext);
            }
            if (mScriptC_mapBitmap == null) {
                mScriptC_mapBitmap = new ScriptC_mapBitmap(mRenderScript);
            }
            sb.append(" ~ p2.0: ").append((System.nanoTime() - time) / 1000000f);
            time = System.nanoTime();
            mScriptC_mapBitmap.set_mapping(cLUT);
            sb.append(" ~ p2.1: ").append((System.nanoTime() - time) / 1000000f);
            time = System.nanoTime();
            mScriptC_mapBitmap.invoke_convertMapping();
//            for (int i = 0; i < 256; i++) {
//                mScriptC_mapBitmap.invoke_setMapping(i, cLUT[i]);
//            }
            sb.append(" ~ p2.2: ").append((System.nanoTime() - time) / 1000000f);
            time = System.nanoTime();

            Allocation in = Allocation.createFromBitmap(mRenderScript, triangleGradientBitmap);
            Allocation out = Allocation.createFromBitmap(mRenderScript, triangleBitmap);
            mScriptC_mapBitmap.forEach_mapBitmap(in, out);
            out.copyTo(triangleBitmap);
            in.destroy();
            out.destroy();

            sb.append(" ~ p2.3: ").append((System.nanoTime() - time) / 1000000f);
//            Log.d(TAG, sb.toString());

            triangleBitmap.prepareToDraw();

            return triangleBitmap;
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), mCustomHashCode);
        }

        void setColors(int sixBitColorA, int sixBitColorB,
                       @NonNull MaterialGradient materialGradient,
                       @NonNull MaterialTexture materialTexture) {
            @ColorInt int colorA = PaintBox.this.getColor(sixBitColorA);
            @ColorInt int colorB = PaintBox.this.getColor(sixBitColorB);

            mCustomHashCode = Objects.hash(
                    colorA, colorB, materialGradient, materialTexture, height, width);

            switch (materialGradient) {
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

            switch (materialTexture) {
                case NONE:
                    break;
                case SPUN:
                    setShader(generateSpunEffect());
                    break;
                case WEAVE:
                    setShader(mUseLegacyEffects ? generateWeaveEffect() : generateCrosshatchEffect());
                    break;
                case HEX:
                    setShader(mUseLegacyEffects ? generateHexEffect() : generateSparkleEffect());
                    break;
            }
        }

        private BitmapShader generateSpunEffect() {
            // Attempt to return an existing BitmapShader from the cache if we have one.
            WeakReference<BitmapShader> cache = mBitmapShaderCache.get(mCustomHashCode);
            if (cache != null) {
                // Well, we have an existing BitmapShader, but it may have been garbage collected...
                BitmapShader result = cache.get();
                if (result != null) {
                    // It wasn't garbage collected! Return it.
                    return result;
                }
            }

            // Generate a new bitmap.
            Bitmap brushedEffectBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas brushedEffectCanvas = new Canvas(brushedEffectBitmap);

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

            brushedEffectBitmap.prepareToDraw();

            BitmapShader result = new BitmapShader(brushedEffectBitmap,
                    Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);

            // Cache it for next time's use.
            mBitmapShaderCache.put(mCustomHashCode, new WeakReference<>(result));
            return result;
        }

        private BitmapShader generateCrosshatchEffect() {
            // Attempt to return an existing BitmapShader from the cache if we have one.
            WeakReference<BitmapShader> cache = mBitmapShaderCache.get(mCustomHashCode);
            if (cache != null) {
                // Well, we have an existing BitmapShader, but it may have been garbage collected...
                BitmapShader result = cache.get();
                if (result != null) {
                    // It wasn't garbage collected! Return it.
                    return result;
                }
            }

            // Generate a new bitmap.
            Bitmap brushedEffectBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas brushedEffectCanvas = new Canvas(brushedEffectBitmap);

            float percent = mCenterX / 50f;
            float offset = 0.5f * percent;
            int alpha = 20;

            mBrushedEffectPaint.reset();
            mBrushedEffectPaint.setStyle(Style.STROKE);
            mBrushedEffectPaint.setStrokeWidth(offset);
            mBrushedEffectPaint.setStrokeJoin(Join.ROUND);
            mBrushedEffectPaint.setAntiAlias(true);

            brushedEffectCanvas.drawPaint(this);

            // Crosshatch!
            for (float y = 0f - width; y <= height; y += height / 75f) {
                // Draw top left to bottom right
                mBrushedEffectPath.reset();
                mBrushedEffectPath.moveTo(0, y);
                mBrushedEffectPath.lineTo(width, y + width);
                mBrushedEffectPaint.setColor(Math.random() < 0.5d ? Color.WHITE : Color.BLACK);
                mBrushedEffectPaint.setAlpha(alpha);
                brushedEffectCanvas.drawPath(mBrushedEffectPath, mBrushedEffectPaint);

                // Draw top right to bottom left
                mBrushedEffectPath.reset();
                mBrushedEffectPath.moveTo(width, y);
                mBrushedEffectPath.lineTo(0, y + width);
                mBrushedEffectPaint.setColor(Math.random() < 0.5d ? Color.BLACK : Color.WHITE);
                mBrushedEffectPaint.setAlpha(alpha);
                brushedEffectCanvas.drawPath(mBrushedEffectPath, mBrushedEffectPaint);
            }

            brushedEffectBitmap.prepareToDraw();

            BitmapShader result = new BitmapShader(brushedEffectBitmap,
                    Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);

            // Cache it for next time's use.
            mBitmapShaderCache.put(mCustomHashCode, new WeakReference<>(result));
            return result;
        }

        private BitmapShader generateWeaveEffect() {
            // Attempt to return an existing BitmapShader from the cache if we have one.
            WeakReference<BitmapShader> cache = mBitmapShaderCache.get(mCustomHashCode);
            if (cache != null) {
                // Well, we have an existing BitmapShader, but it may have been garbage collected...
                BitmapShader result = cache.get();
                if (result != null) {
                    // It wasn't garbage collected! Return it.
                    return result;
                }
            }

            // Generate a new bitmap.
            Bitmap brushedEffectBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas brushedEffectCanvas = new Canvas(brushedEffectBitmap);

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
//                        Log.d(TAG, "Erasing (" + i + "," + j + ")");
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
//                        Log.d(TAG, "Erasing (" + i + "," + j + ")");
                        // Only every 2nd square
                        mTempCanvas.drawRect(left, top, right, bottom, mBrushedEffectPaint);
                    }
                }
            }
            mBrushedEffectPaint.setXfermode(null);

            // OK, transfer the vertical stripes in "mTempCanvas" to "brushedEffectCanvas".
            brushedEffectCanvas.drawBitmap(mTempBitmap, 0f, 0f, null);

            setAlpha(prevAlpha);

            brushedEffectBitmap.prepareToDraw();

            BitmapShader result = new BitmapShader(brushedEffectBitmap,
                    Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);

            // Cache it for next time's use.
            mBitmapShaderCache.put(mCustomHashCode, new WeakReference<>(result));
            return result;
        }

        private BitmapShader generateHexEffect() {
            // Attempt to return an existing BitmapShader from the cache if we have one.
            WeakReference<BitmapShader> cache = mBitmapShaderCache.get(mCustomHashCode);
            if (cache != null) {
                // Well, we have an existing BitmapShader, but it may have been garbage collected...
                BitmapShader result = cache.get();
                if (result != null) {
                    // It wasn't garbage collected! Return it.
                    return result;
                }
            }

            // Generate a new bitmap.
            Bitmap hexEffectBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas hexEffectCanvas = new Canvas(hexEffectBitmap);

            // Alpha value of the bezels.
            int alpha = 50;

            // The height and width of a single hex.
            final float hexWidth = 7f * pc;
            final float hexHeight = hexWidth * (float) Math.sqrt(3d) / 2f;

            // The spacing between hexes, centre to centre.
            final float hexSpacingX = 7.5f * pc;
            final float hexSpacingY = hexSpacingX * (float) Math.sqrt(3d) / 2f;

            // The number of rows and columns of hexes.
            final int cols0 = (int) Math.ceil((double) width / (double) hexSpacingX);
            final int cols = cols0 + cols0 % 2; // Make it always even, rounded up.
            final int rows0 = (int) Math.ceil((double) height / (double) hexSpacingY);
            final int rows = rows0 + 1 - rows0 % 2; // Make it always odd, rounded up.

            // The initial offset of the first row and column, so there's always a hex in centre.
            // These figures are negative because there's more hexes than can fit within bounds.
            final float offsetX = ((float) width - ((float) (cols - 1) * hexSpacingX)) / 2f;
            final float offsetY = ((float) height - ((float) (rows - 1) * hexSpacingY)) / 2f;

//            Log.d(TAG, "cols = " + cols +
//                    ", rows = " + rows +
//                    ", cols0 = " + cols0 +
//                    ", rows0 = " + rows0 +
//                    ", cols0 = " + ((double) hexSpacingX / (double) width) +
//                    ", rows0 = " + ((double) hexSpacingY / (double) height) +
//                    ", hexSpacingX = " + hexSpacingX +
//                    ", hexSpacingY = " + hexSpacingY);

            mBrushedEffectPaint.reset();
            mBrushedEffectPaint.setStyle(Style.FILL);
//            mBrushedEffectPaint.setStrokeWidth(1f * pc);
            mBrushedEffectPaint.setStrokeJoin(Join.ROUND);
            mBrushedEffectPaint.setColor(Color.WHITE);
            mBrushedEffectPaint.setAntiAlias(true);

            mBrushedEffectPath.reset();

            hexEffectCanvas.drawPaint(this);

            final int mod = ((rows - 1) / 2) % 2;

            for (int y = 0; y < rows; y++) {
                int modCols = cols;
                float modOffsetX = offsetX;
                if (y % 2 == mod) {
                    // For "mod" rows, draw more hexes and indent them all back by half a hex.
                    // Make it so the central row is always a "mod" row with odd number of hexes.
                    modCols += 1;
                    modOffsetX -= hexSpacingX / 2f;
                }
                for (int x = 0; x < modCols; x++) {
                    final float hX = modOffsetX + (hexSpacingX * (float) x);
                    final float hY = offsetY + (hexSpacingY * (float) y);
                    final float w = hexWidth / 2f;
                    final float w2 = hexHeight / (float) Math.sqrt(3d) / 2f;
                    final float h = w / (float) Math.sqrt(3) * 2f;

                    // TODO: one day we might like to have circles instead of hexes. The below:
                    // hexEffectCanvas.drawCircle(hX, hY, w, mBrushedEffectPaint);

                    // Draw a hexagon! Clockwise starting from 12 o'clock:
                    mBrushedEffectPath.moveTo(hX, hY - h);
                    // 2 o'clock
                    mBrushedEffectPath.lineTo(hX + w, hY - w2);
                    // 4 o'clock
                    mBrushedEffectPath.lineTo(hX + w, hY + w2);
                    // 6 o'clock
                    mBrushedEffectPath.lineTo(hX, hY + h);
                    // 8 o'clock
                    mBrushedEffectPath.lineTo(hX - w, hY + w2);
                    // 10 o'clock
                    mBrushedEffectPath.lineTo(hX - w, hY - w2);
                    // And close the hexagon at 12 o'clock
                    mBrushedEffectPath.close();
                }
            }

            final float bezelOffset = 0.25f * pc;

            mBrushedEffectPaint.setColor(Color.WHITE);
            mBrushedEffectPaint.setAlpha(alpha);
            mBrushedEffectPath.offset(-bezelOffset, -bezelOffset, mBrushedEffectPathUpper);
            mBrushedEffectPathUpper.op(mBrushedEffectPath, Path.Op.DIFFERENCE);
            hexEffectCanvas.drawPath(mBrushedEffectPathUpper, mBrushedEffectPaint);

            mBrushedEffectPaint.setColor(Color.BLACK);
            mBrushedEffectPaint.setAlpha(alpha);
            mBrushedEffectPath.offset(bezelOffset, bezelOffset, mBrushedEffectPathLower);
            mBrushedEffectPathLower.op(mBrushedEffectPath, Path.Op.DIFFERENCE);
            hexEffectCanvas.drawPath(mBrushedEffectPathLower, mBrushedEffectPaint);

            hexEffectBitmap.prepareToDraw();

            BitmapShader result = new BitmapShader(hexEffectBitmap,
                    Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);

            // Cache it for next time's use.
            mBitmapShaderCache.put(mCustomHashCode, new WeakReference<>(result));
            return result;
        }

        private static final double SPARKLE_GAMMA = 1.0d;
        private static final double SPARKLE_RANGE = 255d;

        /**
         * Convenience function to derive a sparkle mapping, based on gamma 2.2.
         *
         * @param i      sRGB value to map between 0 and 255
         * @param offset Offset for luminance (brightness) between 0 and 1
         * @return Short4 with x, y and z sRGB elements to map to
         */
        @NonNull
        private Short4 deriveMultiSparkleMapping(int i, double offset) {
            Short4 s4 = new Short4();
            s4.z = deriveSparkleMapping(i, offset / 3d);
            s4.y = deriveSparkleMapping(i, offset / 2d);
            s4.x = deriveSparkleMapping(i, offset);
            s4.w = 0; // Unused padding.
            return s4;
        }

        /**
         * Convenience function to derive a sparkle mapping, based on gamma 2.2.
         *
         * @param i      sRGB value to map between 0 and 255
         * @param offset Offset for luminance (brightness) between 0 and 1
         * @return sRGB value to map to
         */
        private short deriveSparkleMapping(int i, double offset) {
            // Derive the luminance (brightness) by applying gamma function, then offset.
            double lum = Math.pow((double) i / SPARKLE_RANGE, SPARKLE_GAMMA) + offset;
            // Note we don't do "proper" sRGB transfer function, only low-effort Math.pow.

            // Clamp to [0, 1]
            if (lum < 0d)
                lum = 0d;
            else if (lum > 1d)
                lum = 1d;

            // Convert the luminance back to sRGB by applying reverse gamma.
            return (short) (Math.pow(lum, 1d / SPARKLE_GAMMA) * SPARKLE_RANGE);
        }

        private boolean mIsSparkleEffectSetup = false;

        /**
         * Set up our sparkle effect by deriving all our mapping tables.
         */
        private void setupSparkleEffect() {
            if (mIsSparkleEffectSetup)
                return;

            Short4[] mA = new Short4[256], mB = new Short4[256], mC = new Short4[256];
            Short4[] mD = new Short4[256], mE = new Short4[256], mF = new Short4[256];

            for (int i = 0; i < 256; i++) {
                mA[i] = deriveMultiSparkleMapping(i, -0.48d);
                mB[i] = deriveMultiSparkleMapping(i, -0.16d);
                mC[i] = deriveMultiSparkleMapping(i, -0.08d);
                mD[i] = deriveMultiSparkleMapping(i, 0.08d);
                mE[i] = deriveMultiSparkleMapping(i, 0.16d);
                mF[i] = deriveMultiSparkleMapping(i, 0.48d);

//                android.util.Log.d("PaintBox", String.format(
//                        "setupSparkleEffect: %d --> %d %d %d --> %d %d %d",
//                        i, mA[i].x, mB[i].x, mC[i].x, mD[i].x, mE[i].x, mF[i].x));
            }
            mScriptC_mapBitmap.set_sparkleMappingA(mA);
            mScriptC_mapBitmap.set_sparkleMappingB(mB);
            mScriptC_mapBitmap.set_sparkleMappingC(mC);
            mScriptC_mapBitmap.set_sparkleMappingD(mD);
            mScriptC_mapBitmap.set_sparkleMappingE(mE);
            mScriptC_mapBitmap.set_sparkleMappingF(mF);
            mIsSparkleEffectSetup = true;
        }

        private BitmapShader generateSparkleEffect() {
            // Attempt to return an existing BitmapShader from the cache if we have one.
            WeakReference<BitmapShader> cache = mBitmapShaderCache.get(mCustomHashCode);
            if (cache != null) {
                // Well, we have an existing BitmapShader, but it may have been garbage collected...
                BitmapShader result = cache.get();
                if (result != null) {
                    // It wasn't garbage collected! Return it.
                    return result;
                }
            }

            // Generate a new bitmap.
            Bitmap sparkleEffectBitmap =
                    Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            prepareTempBitmapForUse();
            mTempCanvas.drawPaint(this);

            if (mRenderScript == null) {
                mRenderScript = RenderScript.create(mContext);
            }
            if (mScriptC_mapBitmap == null) {
                mScriptC_mapBitmap = new ScriptC_mapBitmap(mRenderScript);
            }

            setupSparkleEffect();

            Allocation in = Allocation.createFromBitmap(mRenderScript, mTempBitmap);
            Allocation out = Allocation.createFromBitmap(mRenderScript, sparkleEffectBitmap);
            mScriptC_mapBitmap.forEach_sparkle(in, out);
            out.copyTo(sparkleEffectBitmap);
            in.destroy();
            out.destroy();

            sparkleEffectBitmap.prepareToDraw();

            BitmapShader result = new BitmapShader(sparkleEffectBitmap,
                    Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);

            // Cache it for next time's use.
            mBitmapShaderCache.put(mCustomHashCode, new WeakReference<>(result));
            return result;
        }
    }
}
