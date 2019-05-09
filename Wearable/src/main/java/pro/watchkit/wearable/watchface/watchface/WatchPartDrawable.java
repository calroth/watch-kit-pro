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
import android.graphics.Rect;
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
    private Path mResetExclusionActivePath = new Path();
    private Path mResetExclusionAmbientPath = new Path();

    private Path p5 = new Path();
    private Path p6 = new Path();
    private Path p7 = new Path();
    private Path p8 = new Path();

    private Path mPrimaryBezel = new Path();
    private Path mSecondaryBezel = new Path();
    private Path mIntersectionBezel = new Path();

    /**
     * Our current direction. Static, so shared amongst all our accessors.
     */
    private static Path.Direction mDirection = Path.Direction.CCW;

    /**
     * Reset our current direction. Call this before starting any drawing, so we get consistency
     * from draw to draw.
     */
    static void resetDirection() {
        mDirection = Path.Direction.CCW;
    }

    /**
     * Flip and get our current direction.
     * We alternate between clockwise and anticlockwise drawing.
     *
     * @return Our current direction, which is flipped from the last call to this method
     */
    Path.Direction getDirection() {
        mDirection = mDirection == Path.Direction.CCW ? Path.Direction.CW : Path.Direction.CCW;
        return mDirection;
    }

    private final float mBevelOffset = 0.3333333f; // 0.33%

    private static Canvas mBezelCanvas;
    private static Paint mBezelBitmapPaint;

    // Stats start
    long mLastStatsTime = 0;
    abstract String getStatsName();
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

    private Matrix m1 = new Matrix();
    private Matrix m2 = new Matrix();

    void fastDrawPath(Canvas canvas, Path p, Paint paint, float degrees) {
        canvas.save();
        canvas.rotate(degrees, mCenterX, mCenterY);

        if (!mWatchFaceState.isAmbient()) {
            // Shadow
            canvas.drawPath(p, mWatchFaceState.getPaintBox().getShadowPaint());

            // The path.
            paint.setStyle(Paint.Style.FILL);
            canvas.drawPath(p, paint);

            // Retrieve our bezel paints and set them to fill.
            Paint bezelPaint1 = mWatchFaceState.getPaintBox().getBezelPaint1();
            Paint bezelPaint2 = mWatchFaceState.getPaintBox().getBezelPaint2();
            bezelPaint1.setStyle(Paint.Style.FILL);
            bezelPaint2.setStyle(Paint.Style.FILL);

            // The bezels.
            canvas.drawPath(mPrimaryBezel, bezelPaint1);
            canvas.drawPath(mSecondaryBezel, bezelPaint2);

        } else {
            // Ambient.
            // The path itself.
            Paint ambientPaint = mWatchFaceState.getPaintBox().getAmbientPaint();

            canvas.drawPath(p, ambientPaint);
        }

        canvas.restore();
    }

    void generateBezels(Path p, float degrees) {
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

    void drawPath(Canvas canvas, Path p, Paint paint) {
        // Apply the exclusion path.
        p.op(mExclusionPath, Path.Op.INTERSECT);

//        int seconds = (int)(mWatchFaceState.getSecondsDecimal());
//        seconds = seconds % 2;

        // 4 layers:
        // Shadow
        // Primary bevel
        // Secondary bevel
        // And finally the path itself.
        boolean olde = true; // seconds % 2 == 0;
        if (!mWatchFaceState.isAmbient()) {
            // Shadow
            canvas.drawPath(p, mWatchFaceState.getPaintBox().getShadowPaint());

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
            if (olde) {
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
            if (!olde) {
                // Draw our bevels to a temporary bitmap.
                // Clear the bezel canvas first.
                mBezelCanvas.drawColor(Color.TRANSPARENT);

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
//                    mWatchFaceState.getLocationCalculator().getDuskDawnColor(PaintBox.AMBIENT_WHITE);
//
//            ambientPaint.setColorFilter(currentNightVisionTint != PaintBox.AMBIENT_WHITE
//                    ? new LightingColorFilter(currentNightVisionTint, 0) : null);
            canvas.drawPath(p, ambientPaint);
        }
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
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

    void setWatchFaceState(@NonNull WatchFaceState watchFaceState, @NonNull Path path) {
        mWatchFaceState = watchFaceState;
        mExclusionPath = path;
    }

    void addExclusionPath(@NonNull Path path, Path.Op op) {
        mExclusionPath.op(path, op);
    }

    void resetExclusionPath() {
        mExclusionPath.set(mWatchFaceState.isAmbient() ?
                mResetExclusionAmbientPath : mResetExclusionActivePath);
    }
}
