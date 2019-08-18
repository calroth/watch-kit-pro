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

package pro.watchkit.wearable.watchface.watchface;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import pro.watchkit.wearable.watchface.model.WatchFaceState;

abstract class WatchPartDrawable extends Drawable {
    WatchFaceState mWatchFaceState;
    float pc = 0f; // percent, set to 0.01f * height, all units are based on percent
    float mCenterX, mCenterY;

    private Path mExclusionPath;
    /**
     * Our current direction. Static, so shared amongst all our accessors.
     */
    @NonNull
    private static Path.Direction mDirection = Path.Direction.CCW;
    @NonNull
    private Path mResetExclusionActivePath = new Path();
    @NonNull
    private Path mResetExclusionAmbientPath = new Path();
    @NonNull
    private Path p5 = new Path();
    @NonNull
    private Path p6 = new Path();
    @NonNull
    private Path p7 = new Path();
    @NonNull
    private Path p8 = new Path();
    @NonNull
    private Path mPrimaryBezel = new Path();
    @NonNull
    private Path mSecondaryBezel = new Path();
    @NonNull
    private Path mIntersectionBezel = new Path();
    @NonNull
    private Path mInnerGlowPath = new Path();

    /**
     * Reset our current direction. Call this before starting any drawing, so we get consistency
     * from draw to draw.
     */
    static void resetDirection() {
        mDirection = Path.Direction.CCW;
    }

    @NonNull
    private Matrix m1 = new Matrix();

    private final float mBevelOffset = 0.3333333f; // 0.33%

    private static Canvas mBezelCanvas;
    private static Paint mBezelBitmapPaint;

    // Stats start
    long mLastStatsTime = 0;
    @NonNull
    private Matrix m2 = new Matrix();
    // Stats end

    @Override
    final public void draw(@NonNull Canvas canvas) {
        // Stats start
        long start = SystemClock.elapsedRealtimeNanos();
        // Stats end

        draw2(canvas);

        // Stats start
        mLastStatsTime = SystemClock.elapsedRealtimeNanos() - start;
        // Stats end
    }

    abstract void draw2(@NonNull Canvas canvas);

    /**
     * Flip and get our current direction.
     * We alternate between clockwise and anticlockwise drawing.
     *
     * @return Our current direction, which is flipped from the last call to this method
     */
    @NonNull
    Path.Direction getDirection() {
        mDirection = mDirection == Path.Direction.CCW ? Path.Direction.CW : Path.Direction.CCW;
        return mDirection;
    }

    @NonNull
    abstract String getStatsName();

    void fastDrawPath(@NonNull Canvas canvas, @NonNull Path p, @NonNull Paint paint, float degrees) {
        m2.reset();
        m2.postRotate(degrees, mCenterX, mCenterY);

        if (!mWatchFaceState.isAmbient()) {
            // Here we treat "mIntersectionBezel" as a cheap throwaway path.
            p.transform(m2, mIntersectionBezel);
            // Shadow
            canvas.drawPath(mIntersectionBezel, mWatchFaceState.getPaintBox().getShadowPaint());

            // The path.
            paint.setStyle(Paint.Style.FILL);
            canvas.drawPath(mIntersectionBezel, paint);

            // Retrieve our bezel paints and set them to fill.
            Paint bezelPaint1 = mWatchFaceState.getPaintBox().getBezelPaint1();
            Paint bezelPaint2 = mWatchFaceState.getPaintBox().getBezelPaint2();
            bezelPaint1.setStyle(Paint.Style.FILL);
            bezelPaint2.setStyle(Paint.Style.FILL);

            // The bezels.
            // Again we treat "mIntersectionBezel" as a cheap throwaway path.
            mPrimaryBezel.transform(m2, mIntersectionBezel);
            canvas.drawPath(mIntersectionBezel, bezelPaint1);
            mSecondaryBezel.transform(m2, mIntersectionBezel);
            canvas.drawPath(mIntersectionBezel, bezelPaint2);

        } else {
            // Ambient.
            // The path itself.
            Paint ambientPaint = mWatchFaceState.getPaintBox().getAmbientPaint();

            // Here we treat "mIntersectionBezel" as a cheap throwaway path.
            p.transform(m2, mIntersectionBezel);
            canvas.drawPath(mIntersectionBezel, ambientPaint);
        }
    }

    void generateBezels(@NonNull Path p, float degrees) {
        if (mWatchFaceState.isAmbient()) {
            return;
        }

        // The bezels.

        // Draw the bezels in the right position, using matrices.
        // By: rotating to the angle we want, offsetting them, then rotating back.

        m1.reset();
        m1.postRotate(degrees, mCenterX, mCenterY);
        m1.postTranslate(-(mBevelOffset * pc), -(mBevelOffset * pc));
        m1.postRotate(-degrees, mCenterX, mCenterY);

        m2.reset();
        m2.postRotate(degrees, mCenterX, mCenterY);
        m2.postTranslate(mBevelOffset * pc, mBevelOffset * pc);
        m2.postRotate(-degrees, mCenterX, mCenterY);

        p.transform(m1, mPrimaryBezel);
        p.transform(m2, mSecondaryBezel);

        // Draw primary and secondary bezels as paths.

        // Calculate the intersection of primary and secondary bezels.
        mIntersectionBezel.set(mPrimaryBezel);
        mIntersectionBezel.op(mSecondaryBezel, Path.Op.INTERSECT);

        // Punch that intersection out of primary and secondary bevels.
        mPrimaryBezel.op(mIntersectionBezel, Path.Op.DIFFERENCE);
        mSecondaryBezel.op(mIntersectionBezel, Path.Op.DIFFERENCE);

        // And clip the primary and secondary bezels to the original paths.
        mPrimaryBezel.op(p, Path.Op.INTERSECT);
        mSecondaryBezel.op(p, Path.Op.INTERSECT);

        // Right, all done, draw them!
//        canvas.drawPath(mPrimaryBezel, bezelPaint1);
//        canvas.drawPath(mSecondaryBezel, bezelPaint1);
    }

    private Paint mInnerGlowPaint;

    void drawInnerGlowPath(@NonNull Canvas canvas, Paint paint) {
        if (mWatchFaceState.isInnerGlow()) {
            if (mInnerGlowPaint == null) {
                mInnerGlowPaint = new Paint();
                mInnerGlowPaint.setStyle(Paint.Style.FILL);
                mInnerGlowPaint.setColor(Color.RED);
                mInnerGlowPaint.setShadowLayer(
                        5f * pc, 0f, 0f, Color.argb(250, 0, 0, 255));
            }
            canvas.drawPath(mInnerGlowPath, mInnerGlowPaint);
        }
    }

    void drawPath(@NonNull Canvas canvas, @NonNull Path p, @NonNull Paint paint) {
        // Apply the exclusion path.
        p.op(mExclusionPath, Path.Op.INTERSECT);

        if (mWatchFaceState.isInnerGlow()) {
            // Apply the inner glow path.
            mInnerGlowPath.op(p, Path.Op.DIFFERENCE);
        }

//        int seconds = (int)(mWatchFaceState.getSecondsDecimal());
//        seconds = seconds % 2;

        // 4 layers:
        // Shadow
        // Primary bevel
        // Secondary bevel
        // And finally the path itself.
        boolean altDrawing = mWatchFaceState.isDeveloperMode() && mWatchFaceState.isAltDrawing();
        if (!mWatchFaceState.isAmbient()) {
            // Shadow
//            canvas.drawPath(p, mWatchFaceState.getPaintBox().getShadowPaint());

            // The path itself.
            paint.setStyle(Paint.Style.FILL);
            canvas.drawPath(p, paint);

            // The bezels.

            // Create our offset paths for our primary and secondary bezels.
            p.offset(-(mBevelOffset * pc), -(mBevelOffset * pc), mPrimaryBezel);
            p.offset((mBevelOffset * pc), (mBevelOffset * pc), mSecondaryBezel);

            // Retrieve our paints and set them to fill.
            Paint bezelPaint1 = mWatchFaceState.getPaintBox().getBezelPaint1();
            Paint bezelPaint2 = mWatchFaceState.getPaintBox().getBezelPaint2();
            bezelPaint1.setStyle(Paint.Style.FILL);
            bezelPaint2.setStyle(Paint.Style.FILL);

            // Draw primary and secondary bezels as paths.
            if (!altDrawing) {
                // Calculate the intersection of primary and secondary bezels.
                mIntersectionBezel.set(mPrimaryBezel);
                mIntersectionBezel.op(mSecondaryBezel, Path.Op.INTERSECT);

                // Punch that intersection out of primary and secondary bevels.
                mPrimaryBezel.op(mIntersectionBezel, Path.Op.DIFFERENCE);
                mSecondaryBezel.op(mIntersectionBezel, Path.Op.DIFFERENCE);

                // And clip the primary and secondary bezels to the original paths.
                mPrimaryBezel.op(p, Path.Op.INTERSECT);
                mSecondaryBezel.op(p, Path.Op.INTERSECT);

                // Right, all done, draw them!
                canvas.drawPath(mPrimaryBezel, bezelPaint1);
                canvas.drawPath(mSecondaryBezel, bezelPaint2);
            }
            // Draw primary and secondary bevels as strokes from a bitmap-shader paint.
            if (altDrawing) {
                // Draw our bevels to a temporary bitmap.
                // Clear the bezel canvas first.
                mBezelCanvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);

                // And clip the primary and secondary bezels to the original paths.
                mPrimaryBezel.op(p, Path.Op.INTERSECT);
                mSecondaryBezel.op(p, Path.Op.INTERSECT);

                // Draw primary and secondary bevels to temporary bitmap.
                mBezelCanvas.drawPath(mPrimaryBezel, bezelPaint1);
                mBezelCanvas.drawPath(mSecondaryBezel, bezelPaint2);

                // Draw a stroke with our new bitmap-shader paint.
                canvas.drawPath(p, mBezelBitmapPaint);
            }
        } else {
            // Ambient.
            // The path itself.
            Paint ambientPaint = mWatchFaceState.getPaintBox().getAmbientPaint();

//            int currentNightVisionTint =
//                    mWatchFaceState.getLocationCalculator().getAmbientTint(PaintBox.AMBIENT_WHITE);
//
//            ambientPaint.setColorFilter(currentNightVisionTint != PaintBox.AMBIENT_WHITE
//                    ? new LightingColorFilter(currentNightVisionTint, 0) : null);
            canvas.drawPath(p, ambientPaint);
        }
    }

    @Override
    protected void onBoundsChange(@NonNull Rect bounds) {
        super.onBoundsChange(bounds);

        int width = bounds.width();
        int height = bounds.height();
        pc = 0.01f * Math.min(height, width);
        /*
         * Find the coordinates of the center point on the screen, and ignore the window
         * insets, so that, on round watches with a "chin", the watch face is centered on the
         * entire screen, not just the usable portion.
         */
        mCenterX = width / 2f;
        mCenterY = height / 2f;

        // Set up our bezel bitmap, canvas and paint structures.
        if (width > 0 && height > 0) {
            Bitmap bezelBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            mBezelCanvas = new Canvas(bezelBitmap);

            // Create a new paint with our temporary bitmap as a shader.
            mBezelBitmapPaint = new Paint();
            mBezelBitmapPaint.setStyle(Paint.Style.STROKE);
            mBezelBitmapPaint.setAntiAlias(true);
            mBezelBitmapPaint.setShader(new BitmapShader(bezelBitmap,
                    Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));
            mBezelBitmapPaint.setStrokeWidth(mBevelOffset * pc * 2);
        }

        // Check width and height.
        mWatchFaceState.getPaintBox().onWidthAndHeightChanged(width, height);

        // Set up reset exclusion paths for ambient and active.

        // For ambient...
        // We can't draw here because Wear OS shifts our watchface +/- 6px in each direction
        // and it gets cut off, so just don't try drawing there.

        final int exclusion = 6;

        p5.reset();
        p6.reset();
        p7.reset();
        p8.reset();
        p5.addCircle(mCenterX + exclusion, mCenterY + exclusion, pc * 50f, getDirection());
        p6.addCircle(mCenterX + exclusion, mCenterY - exclusion, pc * 50f, getDirection());
        p7.addCircle(mCenterX - exclusion, mCenterY + exclusion, pc * 50f, getDirection());
        p8.addCircle(mCenterX - exclusion, mCenterY - exclusion, pc * 50f, getDirection());

        p5.op(p6, Path.Op.INTERSECT);
        p5.op(p7, Path.Op.INTERSECT);
        p5.op(p8, Path.Op.INTERSECT);

        mResetExclusionAmbientPath.reset();
        mResetExclusionAmbientPath.addPath(p5);
        addExclusionPath(mResetExclusionAmbientPath, Path.Op.UNION);

        // For active, set an exclusion path of just the entire watchface.
        // Will need to revisit when we start supporting square devices.

        mResetExclusionActivePath.reset();
        mResetExclusionActivePath.addCircle(mCenterX, mCenterY, pc * 50f, getDirection());
        addExclusionPath(mResetExclusionActivePath, Path.Op.UNION);

        // Reset "mInnerGlowPath" to our bounds, outset by 10%.
        mInnerGlowPath.reset();
        RectF boundsF = new RectF(bounds);
        boundsF.inset(-10f * pc, -10f * pc);
        mInnerGlowPath.addRect(boundsF, getDirection());
    }

    @Override
    public void setAlpha(int alpha) {
        // TODO: fill this in
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        // TODO: fill this in
    }

    @Override
    public int getOpacity() {
        // TODO: fill this in
        return PixelFormat.UNKNOWN;
//        return 0;
    }

    void setWatchFaceState(@NonNull WatchFaceState watchFaceState, @NonNull Path exclusionPath,
                           @NonNull Path innerGlowPath) {
        mWatchFaceState = watchFaceState;
        mExclusionPath = exclusionPath;
        mInnerGlowPath = innerGlowPath;
    }

    void addExclusionPath(@NonNull Path path, Path.Op op) {
        mExclusionPath.op(path, op);
    }

    /**
     * Reset our exclusion path to default, which respects the device's borders.
     */
    void resetExclusionPath() {
        mExclusionPath.set(mWatchFaceState.isAmbient() ?
                mResetExclusionAmbientPath : mResetExclusionActivePath);
    }

    /**
     * Reset our exclusion path totally. For when we don't care about drawing outside borders!
     */
    void resetExclusionPathTotally() {
        mExclusionPath.reset();
        mExclusionPath.addCircle(mCenterX, mCenterY, mCenterX * 3f, getDirection());
    }

    void drawRect(Path path, float left, float top, float right, float bottom, float scale) {
        float x0 = (right - left) * 0.5f * scale;
        float y0 = (bottom - top) * 0.5f * scale;

        path.addRect(left + x0, top + y0, right - x0, bottom - y0,
                getDirection());
    }

    /**
     * Better implementation of drawRect. Draws a rectangle in the specified bounds (or extending
     * past them). Notionally the area of this rectangle is k of the area of the bounds. Plus it
     * has a property that the inset is equal on all sides.
     * <p>
     * Where "k" is the golden ratio 2nd term ≈ 0.38196601125...
     * <p>
     * For example, pass in bounds of 6x4 (24 area) and it will calculate a rectangle of 4x2 (12
     * area) with an inset of 1 on all sides.
     * <p>
     * If offsetTop or offsetBottom is 1.0, then it uses the calculations as specified. If it's
     * greater than 1.0, the top or bottom is moved outwards. If less than 1.0, inwards.
     *
     * @param path         Path to draw into
     * @param left         Left boundary
     * @param top          Top boundary
     * @param right        Right boundary
     * @param bottom       Bottom boundary
     * @param offsetTop    Factor to move the top border, 1.0f is no change
     * @param offsetBottom Factor to move the bottom border, 1.0f is no change.
     */
    void drawRectInset(Path path, float left, float top, float right, float bottom,
                       float offsetTop, float offsetBottom) {
        // Inset calculation:
        //   k = golden ratio 2nd term
        //     = (3 − √5) / 2
        //     ≈ 0.38196601125...
        //  xy = ((x - n)(y - n)) / k
        //   n = (x + y − √( x² + y² + (4 − 2√5)xy)) / 2
        // And then...
        //   offset = n / 2
        float x = (right - left);
        float y = (bottom - top);
        float n = (x + y - (float) Math.sqrt(x * x + y * y + (4f - 2f * Math.sqrt(5d)) * x * y)) * 0.5f;
        float offset = n / 2f;

        float newTop = bottom - (y * offsetTop);
        float newBottom = top + (y * offsetBottom);

        path.addRect(left + offset, newTop + offset,
                right - offset, newBottom - offset, getDirection());
    }

    void drawRoundRect(Path path, float left, float top, float right, float bottom,
                       float cornerRadius, float scale) {
        float x0 = (right - left) * 0.5f * scale;
        float y0 = (bottom - top) * 0.5f * scale;
        float v = cornerRadius * (1f - scale);

        path.addRoundRect(left + x0, top + y0, right - x0, bottom - y0,
                v, v, getDirection());
    }

    /**
     * Better implementation of drawRoundRect. Draws a round rectangle in the specified bounds (or
     * extending past them). Notionally the area of this round rectangle is k of the area of the
     * bounds. Plus it has a property that the inset is equal on all sides.
     * <p>
     * Where "k" is the golden ratio 2nd term ≈ 0.38196601125...
     * <p>
     * For example, pass in bounds of 6x4 (24 area) and it will calculate a rectangle of 4x2 (12
     * area) with an inset of 1 on all sides.
     * <p>
     * If offsetTop or offsetBottom is 1.0, then it uses the calculations as specified. If it's
     * greater than 1.0, the top or bottom is moved outwards. If less than 1.0, inwards.
     *
     * @param path         Path to draw into
     * @param left         Left boundary
     * @param top          Top boundary
     * @param right        Right boundary
     * @param bottom       Bottom boundary
     * @param cornerRadius Corner radius of round rectangle
     * @param offsetTop    Factor to move the top border, 1.0f is no change
     * @param offsetBottom Factor to move the bottom border, 1.0f is no change.
     */
    void drawRoundRectInset(Path path, float left, float top, float right, float bottom,
                            float cornerRadius, float offsetTop, float offsetBottom) {
        // Inset calculation:
        //   k = golden ratio 2nd term
        //     = (3 − √5) / 2
        //     ≈ 0.38196601125...
        //  xy = ((x - n)(y - n)) / k
        //   n = (x + y − √( x² + y² + (4 − 2√5)xy)) / 2
        // And then...
        //   offset = n / 2
        float x = (right - left);
        float y = (bottom - top);
        float n = (x + y - (float) Math.sqrt(x * x + y * y + (4f - 2f * Math.sqrt(5d)) * x * y)) * 0.5f;
        float offset = n / 2f;

        float newTop = bottom - (y * offsetTop);
        float newBottom = top + (y * offsetBottom);

        float v = cornerRadius - n;
        v = v < 0 ? 0 : v; // Cap minimum at 0.

        path.addRoundRect(left + offset, newTop + offset,
                right - offset, newBottom - offset, v, v, getDirection());
    }

    void drawDiamond(@NonNull Path path, float left, float top, float right, float bottom,
                     float scale, float midpoint) {
        drawDiamond(path, left, top, right, bottom, scale, midpoint, true, true);
    }

    void drawDiamond(@NonNull Path path, float left, float top, float right, float bottom,
                     float scale, float midpoint, boolean drawTopHalf,
                     boolean drawBottomHalf) {
        final float diamondMidpoint = (top * midpoint) + (bottom * (1 - midpoint));

        // Scale factor. Ignored if scale == 0f
        final float x0 = (right - left) * 0.5f * scale;
        final float y1 = (diamondMidpoint - top) * scale;
        final float y2 = (bottom - diamondMidpoint) * scale;

        if (getDirection() == Path.Direction.CW) {
            path.moveTo(left + x0, diamondMidpoint); // Left
            if (drawTopHalf) {
                path.lineTo(mCenterX, top + y1); // Top
            }
            path.lineTo(right - x0, diamondMidpoint); // Right
            if (drawBottomHalf) {
                path.lineTo(mCenterX, bottom - y2); // Bottom: extend past the hub
            }
        } else {
            path.moveTo(right - x0, diamondMidpoint); // Right
            if (drawTopHalf) {
                path.lineTo(mCenterX, top + y1); // Top
            }
            path.lineTo(left + x0, diamondMidpoint); // Left
            if (drawBottomHalf) {
                path.lineTo(mCenterX, bottom - y2); // Bottom: extend past the hub
            }
        }
        path.close();
    }

    void drawTriangle(@NonNull Path path, float left, float top, float right, float bottom,
                      float scale) {
        drawTriangle(path, left, top, right, bottom, scale, true, true);
    }

    void drawTriangle(@NonNull Path path, float left, float top, float right, float bottom,
                      float scale, boolean drawTopHalf, boolean drawBottomHalf) {
        // Scale factor. Ignored if scale == 0f
        final double h = (double) (bottom - top);
        final double w = (double) (right - left);
        final double w1 = w - (w * Math.sqrt(1d - (double) scale));
        final double z = Math.sin(Math.atan(h / w) / 2d) * w1;
        final double z1 = h - (h * Math.sqrt(1d - (double) scale)) - z;

        if (getDirection() == Path.Direction.CW) {
            path.moveTo(left + (float) w1, bottom - (float) z); // Left
            path.lineTo(mCenterX, top + (float) z1); // Top
            path.lineTo(right - (float) w1, bottom - (float) z); // Right
        } else {
            path.moveTo(right - (float) w1, bottom - (float) z); // Right
            path.lineTo(mCenterX, top + (float) z1); // Top
            path.lineTo(left + (float) w1, bottom - (float) z); // Left
        }
        path.close();

        // Top and bottom halves.
        // Shamelessly re-use the "mPrimaryBezel" path for this.
        if (drawTopHalf && !drawBottomHalf) {
            mPrimaryBezel.reset();
            // Make "mPrimaryBezel" a bit bigger than top half, then intersect with "path".
            mPrimaryBezel.addRect(
                    left - 1f * pc, top - 1 * pc,
                    right + 1f * pc, (top + bottom) / 2f, getDirection());
            path.op(mPrimaryBezel, Path.Op.INTERSECT);
        } else if (drawBottomHalf && !drawTopHalf) {
            mPrimaryBezel.reset();
            // Make "mPrimaryBezel" a bit bigger than bottom half, then intersect with "path".
            mPrimaryBezel.addRect(
                    left - 1f * pc, (top + bottom) / 2f,
                    right + 1f * pc, bottom + 1f * pc, getDirection());
            path.op(mPrimaryBezel, Path.Op.INTERSECT);
        }
    }
}
