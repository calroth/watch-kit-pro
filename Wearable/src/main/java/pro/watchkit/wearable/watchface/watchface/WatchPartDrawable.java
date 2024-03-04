/*
 * Copyright (C) 2018-2021 Terence Tan
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
import android.graphics.drawable.Drawable;
import android.os.SystemClock;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;

import pro.watchkit.wearable.watchface.model.WatchFaceState;
import pro.watchkit.wearable.watchface.util.SharedPref;

abstract class WatchPartDrawable extends Drawable {
    WatchFaceState mWatchFaceState;
    float pc = 0f; // percent, set to 0.01f * height, all units are based on percent
    float mCenterX, mCenterY;

    /**
     * Cutout scale. Scaled to the golden ratio. The outer cutout is a ratio larger than 1.
     */
    final static float CUTOUT_SCALE_OUTER = (float) Math.pow((1d + Math.sqrt(5d)) / 2d, 1d);
    /**
     * Cutout scale. Scaled to the golden ratio. The inner cutout is a ratio smaller than 1.
     */
    final static float CUTOUT_SCALE_INNER = (float) Math.pow((1d + Math.sqrt(5d)) / 2d, -1d);

    private Path mExclusionPath;
    /**
     * Our current direction. Static, so shared amongst all our accessors.
     */
    @NonNull
    private static Path.Direction mDirection = Path.Direction.CCW;
    @NonNull
    private final Path mResetExclusionActivePath = new Path();
    @NonNull
    private final Path mResetExclusionAmbientPath = new Path();
    @NonNull
    private final Path p5 = new Path();
    @NonNull
    private final Path p6 = new Path();
    @NonNull
    private final Path p7 = new Path();
    @NonNull
    private final Path p8 = new Path();
    @NonNull
    private final Path mNorthWestBezel = new Path();
    @NonNull
    private final Path mSouthEastBezel = new Path();
    @NonNull
    private final Path mNorthEastBezel = new Path();
    @NonNull
    private final Path mSouthWestBezel = new Path();
    @NonNull
    private final Path mShapeCutout = new Path();
    @NonNull
    private final Path mTempPath = new Path();
    @NonNull
    private Path mInnerGlowPath = new Path();
    @NonNull
    private final Path mDrawPath = new Path();

    /**
     * Reset our current direction. Call this before starting any drawing, so we get consistency
     * from draw to draw.
     */
    static void resetDirection() {
        mDirection = Path.Direction.CCW;
    }


    private static final float mBevelOffset = 0.25f; // 0.25%

    // Stats start
    long mLastStatsTime = 0;
    // Stats end

    @NonNull
    private final Matrix mTempMatrix1 = new Matrix();
    @NonNull
    private final Matrix mTempMatrix2 = new Matrix();

    private static Canvas mWriteCanvas;
    private static Bitmap mWriteBitmap;

    @Override
    final public void draw(@NonNull Canvas canvas) {
        // Stats start
        long start = SystemClock.elapsedRealtimeNanos();
        // Stats end

        Rect bounds = canvas.getClipBounds();
        if (SharedPref.mWriteLayersToDisk && bounds.width() != 0 && bounds.height() != 0) {
            // Create "mWriteBitmap" on first use or dimension change.
            if (mWriteBitmap == null || bounds.width() != mWriteBitmap.getWidth() ||
                    bounds.height() != mWriteBitmap.getHeight()) {
                mWriteBitmap = Bitmap.createBitmap(
                        bounds.height(), bounds.width(), Bitmap.Config.ARGB_8888);
                mWriteCanvas = new Canvas(mWriteBitmap);
            }

            // Clear out "mWriteCanvas".
            mWriteCanvas.drawColor(Color.BLACK, Mode.CLEAR);

            // Draw to "mWriteCanvas" too!
            draw2(mWriteCanvas);

            try {
                FileOutputStream out = mWatchFaceState.openFileOutput(
                        SystemClock.elapsedRealtimeNanos() +
                                "-" + this.getClass().getSimpleName() +
                                ".png");
                mWriteBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            draw2(canvas);
        }

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

    private int mPreviousSerial = -1;

    boolean hasStateChanged() {
        int currentSerial = Objects.hash(mWatchFaceState);
        if (currentSerial == mPreviousSerial) {
            return false;
        } else {
            mPreviousSerial = currentSerial;
            return true;
        }
    }

    /**
     * Do we draw shadows for our paths?
     *
     * @return Whether we draw shadows for our paths; false by default.
     */
    boolean enablePathShadows() {
        return false;
    }

    private float mLastDegrees = -360f;

    void regenerateBezels() {
        mLastDegrees = -360f;
    }

    /**
     * The degrees clockwise to rotate the paths in this drawable when drawing.
     * <p>
     * By default it's 0 (actually it's -360, but who's counting).
     *
     * @return The degrees clockwise to rotate the paths in this drawable
     */
    float getDegreesRotation() {
        return -360f;
    }

    /**
     * Pre-generate the bezels in "mNorthWestBezel" and "mSouthEastBezel" (and their alternates
     * in "mNorthEastBezel" and "mSouthWestBezel").
     * <p>
     * This can be called on every draw, but it's expensive (lots of intersections and path
     * manipulation) so it's been spun out into this method. Call it every time or not!
     *
     * @param p       The path to generate bezels for
     * @param degrees Degrees clockwise to rotate "p", or 0f for no rotation
     */
    private void generateBezels(@NonNull Path p, float degrees) {
        if (mWatchFaceState.isAmbient()) {
            return;
        }

        // The bezels. Draw NW and SE bezels as paths.
        {
            // Draw the bezels in the right position, using matrices.
            // By: rotating to the angle we want, offsetting them, then rotating back.

            mTempMatrix1.reset();
            mTempMatrix2.reset();

            mTempMatrix1.postRotate(degrees, mCenterX, mCenterY);
            mTempMatrix1.postTranslate(-(mBevelOffset * pc), -(mBevelOffset * pc));
            mTempMatrix1.postRotate(-degrees, mCenterX, mCenterY);

            mTempMatrix2.postRotate(degrees, mCenterX, mCenterY);
            mTempMatrix2.postTranslate(mBevelOffset * pc, mBevelOffset * pc);
            mTempMatrix2.postRotate(-degrees, mCenterX, mCenterY);

            p.transform(mTempMatrix1, mNorthWestBezel);
            p.transform(mTempMatrix2, mSouthEastBezel);

            // Calculate the intersection of NW and SE bezels.
            mTempPath.set(mNorthWestBezel);
            mTempPath.op(mSouthEastBezel, Path.Op.INTERSECT);

            // Punch that intersection out of NW and SE bevels.
            mNorthWestBezel.op(mTempPath, Path.Op.DIFFERENCE);
            mSouthEastBezel.op(mTempPath, Path.Op.DIFFERENCE);

            // And clip the NW and SE bezels to the original paths.
            mNorthWestBezel.op(p, Path.Op.INTERSECT);
            mSouthEastBezel.op(p, Path.Op.INTERSECT);
        }

//        boolean altDrawing = mWatchFaceState.isDeveloperMode() && mWatchFaceState.isAltDrawing();
//        if (!altDrawing) {
        // Do it again: only this time for NE and SW bezels

        mTempMatrix1.reset();
        mTempMatrix2.reset();

        mTempMatrix1.postRotate(degrees, mCenterX, mCenterY);
        mTempMatrix1.postTranslate(mBevelOffset * pc, -(mBevelOffset * pc));
        mTempMatrix1.postRotate(-degrees, mCenterX, mCenterY);

        mTempMatrix2.postRotate(degrees, mCenterX, mCenterY);
        mTempMatrix2.postTranslate(-(mBevelOffset * pc), mBevelOffset * pc);
        mTempMatrix2.postRotate(-degrees, mCenterX, mCenterY);

        p.transform(mTempMatrix1, mNorthEastBezel);
        p.transform(mTempMatrix2, mSouthWestBezel);

        // Draw NW and SE bezels as paths.

        // Calculate the intersection of NE and SW bezels.
        mTempPath.set(mNorthEastBezel);
        mTempPath.op(mSouthWestBezel, Path.Op.INTERSECT);

        // Punch that intersection out of NE and SW bevels.
        mNorthEastBezel.op(mTempPath, Path.Op.DIFFERENCE);
        mSouthWestBezel.op(mTempPath, Path.Op.DIFFERENCE);

        // And clip the NE and SW bezels to the original paths.
        mNorthEastBezel.op(p, Path.Op.INTERSECT);
        mSouthWestBezel.op(p, Path.Op.INTERSECT);
//        }
    }

    private Paint mInnerGlowPaint;

    void drawInnerGlowPath(@NonNull Canvas canvas, Paint paint) {
        if (mWatchFaceState.isDeveloperMode() && mWatchFaceState.isInnerGlow()) {
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

    void drawPath(@NonNull Canvas canvas, @NonNull Path path, @NonNull Paint paint) {
        mDrawPath.set(path);
        // Apply the exclusion path.
        mDrawPath.op(mExclusionPath, Path.Op.INTERSECT);

        if (mWatchFaceState.isDeveloperMode() && mWatchFaceState.isInnerGlow()) {
            // Apply the inner glow path.
            mInnerGlowPath.op(mDrawPath, Path.Op.DIFFERENCE);
        }

        // Regenerate the bezels if needed.
        float degrees = getDegreesRotation();
        if (degrees == -360f || (degrees - mLastDegrees) % 360f > 6f || hasStateChanged()) {
            // Generate a new bezel if the current one is more than 6 degrees (1 minute) out.
            // Or, always generate a new bezel if it's -360f (the default).
            generateBezels(mDrawPath, degrees);
            mLastDegrees = degrees;
        }

        mTempMatrix2.reset();
        mTempMatrix2.postRotate(degrees, mCenterX, mCenterY);

//        int seconds = (int)(mWatchFaceState.getSecondsDecimal());
//        seconds = seconds % 2;

        // 6 layers:
        // Shadow
        // The path itself
        // Primary bevel 2 and secondary bevel 2, which are light and dark highlights
        // Primary bevel and secondary bevel
//        boolean altDrawing = mWatchFaceState.isDeveloperMode() && mWatchFaceState.isAltDrawing();
        if (!mWatchFaceState.isAmbient()) {
            // Shadow
//            canvas.drawPath(p, mWatchFaceState.getPaintBox().getShadowPaint());

            // The path itself.
            paint.setStyle(Paint.Style.FILL);
            mDrawPath.transform(mTempMatrix2, mTempPath);
            // Shadow
            if (enablePathShadows()) {
                canvas.drawPath(mTempPath,
                        mWatchFaceState.getPaintBox().getShadowPaint());
            }
            canvas.drawPath(mTempPath, paint);

//            // Draw NE and SW bezels as paths.
//            // They're drawn first so they're overdrawn by NW and SE.
//            if (!altDrawing) {
            {
                // Retrieve our paints and set them to fill.
                Paint bezelPaint1 = mWatchFaceState.getPaintBox().getBezelPaint1();
                Paint bezelPaint2 = mWatchFaceState.getPaintBox().getBezelPaint2();
                bezelPaint1.setStyle(Paint.Style.FILL);
                int alpha1 = bezelPaint1.getAlpha();
                bezelPaint1.setAlpha(127); // Draw at half-intensity
                bezelPaint2.setStyle(Paint.Style.FILL);
                int alpha2 = bezelPaint2.getAlpha();
                bezelPaint2.setAlpha(127);

//                // Right, all done, draw them!
                mNorthEastBezel.transform(mTempMatrix2, mTempPath);
                canvas.drawPath(mTempPath, bezelPaint1);
                mSouthWestBezel.transform(mTempMatrix2, mTempPath);
                canvas.drawPath(mTempPath, bezelPaint2);

                bezelPaint1.setAlpha(alpha1);
                bezelPaint2.setAlpha(alpha2);
            }

            // Draw NW and SE bezels as paths.
            {
                // Retrieve our paints and set them to fill.
                Paint bezelPaint1 = mWatchFaceState.getPaintBox().getBezelPaint1();
                Paint bezelPaint2 = mWatchFaceState.getPaintBox().getBezelPaint2();
                bezelPaint1.setStyle(Paint.Style.FILL);
                bezelPaint2.setStyle(Paint.Style.FILL);

                // Right, all done, draw them!
                mNorthWestBezel.transform(mTempMatrix2, mTempPath);
                canvas.drawPath(mTempPath, bezelPaint1);
                mSouthEastBezel.transform(mTempMatrix2, mTempPath);
                canvas.drawPath(mTempPath, bezelPaint2);
            }
        } else {
            // Ambient.
            // The path itself.
            mDrawPath.transform(mTempMatrix2, mTempPath);
            canvas.drawPath(mTempPath, getAmbientPaint());
        }
    }

    @NonNull
    protected Paint getAmbientPaint() {
        return mWatchFaceState.getPaintBox().getAmbientPaint();
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

        // For active, set an exclusion path of just the entire watchface.
        // Set it as a rect that's 1% bigger than the screen on all sides.
        // This should cater for rectangular and circular screens alike.

        mResetExclusionActivePath.reset();
        mResetExclusionActivePath.addRect(-pc, -pc, width + pc, height + pc, getDirection());

        // Reset "mInnerGlowPath" to our bounds, outset by 10%.
        mInnerGlowPath.reset();
        RectF boundsF = new RectF(bounds);
        boundsF.inset(-10f * pc, -10f * pc);
        mInnerGlowPath.addRect(boundsF, getDirection());
    }

    @Override
    public void setAlpha(int alpha) {
        // No op.
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        // No op.
    }

    @Override
    public int getOpacity() {
        // No op.
        return PixelFormat.UNKNOWN;
    }

    void setWatchFaceState(@NonNull WatchFaceState watchFaceState, @NonNull Path exclusionPath,
                           @NonNull Path innerGlowPath) {
        mWatchFaceState = watchFaceState;
        mExclusionPath = exclusionPath;
        mInnerGlowPath = innerGlowPath;
    }

    void addExclusionPath(@NonNull Path path, @NonNull Path.Op op) {
        mExclusionPath.op(path, op);
    }

    /**
     * Reset our exclusion path to default, which respects the device's borders.
     */
    void resetExclusionPath() {
        mExclusionPath.set(mWatchFaceState.isAmbient() && mWatchFaceState.getBurnInProtection() ?
                mResetExclusionAmbientPath : mResetExclusionActivePath);
    }

    /**
     * Reset our exclusion path totally. For when we don't care about drawing outside borders!
     */
    void resetExclusionPathTotally() {
        mExclusionPath.reset();
        mExclusionPath.addCircle(mCenterX, mCenterY, mCenterX * 3f, getDirection());
    }

    /**
     * Draw a rectangle into "path". The rect's dimensions are set by the given bounds "left",
     * "top", "right" and "bottom".
     * <p>
     * At scale 1.0f this method draws a rectangle exactly up to these bounds.
     * <p>
     * At scales less than 1.0f this method draws a shape inset into a "1.0f shape" in such a way
     * that each edge is a constant distance from the "1.0f shape", and at the same time, such that
     * the shape's area is "scale" the size of the "1.0f shape".
     * <p>
     * At scales greater than 1.0f, it works the same way, except the shape drawn will be
     * correspondingly larger (and outside of the bounds).
     *
     * @param path   The path to draw into
     * @param left   Left bound
     * @param top    Top bound
     * @param right  Right bound
     * @param bottom Bottom bound
     * @param scale  Scale of the shape
     */
    void drawRect(@NonNull Path path, float left, float top, float right, float bottom,
                  float scale) {
        drawRect(path, left, top, right, bottom, scale, 0f, 0f);
    }

    /**
     * Draw a rectangle into "path". The rect's dimensions are set by the given bounds "left",
     * "top", "right" and "bottom".
     * <p>
     * At scale 1.0f this method draws a rectangle exactly up to these bounds.
     * <p>
     * At scales less than 1.0f this method draws a shape inset into a "1.0f shape" in such a way
     * that each edge is a constant distance from the "1.0f shape", and at the same time, such that
     * the shape's area is "scale" the size of the "1.0f shape".
     * <p>
     * At scales greater than 1.0f, it works the same way, except the shape drawn will be
     * correspondingly larger (and outside of the bounds).
     *
     * @param path   The path to draw into
     * @param left   Left bound
     * @param top    Top bound
     * @param right  Right bound
     * @param bottom Bottom bound
     * @param scale  Scale of the shape
     * @param offsetTop Percentage from top to start drawing; 0.0f for no offset
     * @param offsetBottom Percentage to bottom to finish drawing; 0.0f for no offset
     */
    void drawRect(@NonNull Path path, float left, float top, float right, float bottom,
                  float scale, float offsetTop, float offsetBottom) {
        // Inset calculation:
        //  xy = ((x - n)(y - n)) / scale
        //   n = (x + y − √( x² + y² + (4 * scale - 2)xy)) / 2
        // And then...
        //   offset = n / 2
        final float x = right - left;
        final float y = bottom - top;
        final float n =
                (x + y - (float) Math.sqrt(x * x + y * y + (4f * scale - 2f) * x * y)) * 0.5f;
        final float offset = n / 2f;

        path.addRect(left + offset, top + offset,
                right - offset, bottom - offset, getDirection());

        // Top and bottom halves
        if (offsetTop != 0f || offsetBottom != 0f) {
            float t = top + offset; // Top of inner shape
            float b = bottom - offset; // Bottom of inner shape
            float h2 = b - t; // Height of inner shape

            // Apply the offsets.
            t += h2 * offsetTop;
            b -= h2 * offsetBottom;

            mShapeCutout.reset();
            // Make "mShapeCutout" a bit wider than the shape, then intersect with "path".
            mShapeCutout.addRect(
                    left - 1f * pc, t, right + 1f * pc, b, getDirection());
            path.op(mShapeCutout, Path.Op.INTERSECT);
        }
    }

    /**
     * Draw a round rect into "path". The rect's dimensions are set by the given bounds "left",
     * "top", "right" and "bottom".
     * <p>
     * At scale 1.0f this method draws a round rect exactly up to these bounds.
     * <p>
     * At scales less than 1.0f this method draws a shape inset into a "1.0f shape" in such a way
     * that each edge is a constant distance from the "1.0f shape", and at the same time, such that
     * the shape's area is "scale" the size of the "1.0f shape".
     * <p>
     * At scales greater than 1.0f, it works the same way, except the shape drawn will be
     * correspondingly larger (and outside of the bounds).
     *
     * @param path         The path to draw into
     * @param left         Left bound
     * @param top          Top bound
     * @param right        Right bound
     * @param bottom       Bottom bound
     * @param cornerRadius Corner radius of round rect
     * @param scale        Scale of the shape
     */
    void drawRoundRect(@NonNull Path path, float left, float top, float right, float bottom,
                       float cornerRadius, float scale) {
        drawRoundRect(path, left, top, right, bottom, cornerRadius, scale, 0f, 0f);
    }

    /**
     * Draw a round rect into "path". The rect's dimensions are set by the given bounds "left",
     * "top", "right" and "bottom".
     * <p>
     * At scale 1.0f this method draws a round rect exactly up to these bounds.
     * <p>
     * At scales less than 1.0f this method draws a shape inset into a "1.0f shape" in such a way
     * that each edge is a constant distance from the "1.0f shape", and at the same time, such that
     * the shape's area is "scale" the size of the "1.0f shape".
     * <p>
     * At scales greater than 1.0f, it works the same way, except the shape drawn will be
     * correspondingly larger (and outside of the bounds).
     *
     * @param path   The path to draw into
     * @param left   Left bound
     * @param top    Top bound
     * @param right  Right bound
     * @param bottom Bottom bound
     * @param cornerRadius Corner radius of round rect
     * @param scale  Scale of the shape
     * @param offsetTop Percentage from top to start drawing; 0.0f for no offset
     * @param offsetBottom Percentage to bottom to finish drawing; 0.0f for no offset
     */
    void drawRoundRect(@NonNull Path path, float left, float top, float right, float bottom,
                       float cornerRadius, float scale, float offsetTop, float offsetBottom) {
        // Inset calculation:
        //  xy = ((x - n)(y - n)) / scale
        //   n = (x + y − √( x² + y² + (4 * scale - 2)xy)) / 2
        // And then...
        //   offset = n / 2
        final float x = right - left;
        final float y = bottom - top;
        final float n =
                (x + y - (float) Math.sqrt(x * x + y * y + (4f * scale - 2f) * x * y)) * 0.5f;
        final float offset = n / 2f;
        final float v = cornerRadius * scale;

        path.addRoundRect(left + offset, top + offset,
                right - offset, bottom - offset, v, v, getDirection());

        // Top and bottom halves
        if (offsetTop != 0f || offsetBottom != 0f) {
            float t = top + offset; // Top of inner shape
            float b = bottom - offset; // Bottom of inner shape
            float h2 = b - t; // Height of inner shape

            // Apply the offsets.
            t += h2 * offsetTop;
            b -= h2 * offsetBottom;

            mShapeCutout.reset();
            // Make "mShapeCutout" a bit wider than the shape, then intersect with "path".
            mShapeCutout.addRect(
                    left - 1f * pc, t, right + 1f * pc, b, getDirection());
            path.op(mShapeCutout, Path.Op.INTERSECT);
        }
    }

    /**
     * Draw an ellipse into "path". The ellipse's dimensions are set by the given bounds "left",
     * "top", "right" and "bottom".
     * <p>
     * At scale 1.0f this method draws a ellipse exactly up to these bounds.
     * <p>
     * At scales less than 1.0f this method draws a shape inset into a "1.0f shape" in such a way
     * that each edge is a constant distance from the "1.0f shape", and at the same time, such that
     * the shape's area is "scale" the size of the "1.0f shape".
     * <p>
     * At scales greater than 1.0f, it works the same way, except the shape drawn will be
     * correspondingly larger (and outside of the bounds).
     * <p>
     * Scales other than 1.0f likely only work well for circles. For non-circle ellipses this will
     * be inaccurate because we need to draw an oval with some curve I can't be bothered deriving.
     *
     * @param path   The path to draw into
     * @param left   Left bound
     * @param top    Top bound
     * @param right  Right bound
     * @param bottom Bottom bound
     * @param scale  Scale of the shape
     */
    void drawEllipse(@NonNull Path path, float left, float top, float right, float bottom,
                     float scale) {
        // Inset calculation:
        //  xy = ((x - n)(y - n)) / scale
        //   n = (x + y − √( x² + y² + (4 * scale - 2)xy)) / 2
        // And then...
        //   offset = n / 2
        final float x = right - left;
        final float y = bottom - top;
        final float n =
                (x + y - (float) Math.sqrt(x * x + y * y + (4f * scale - 2f) * x * y)) * 0.5f;
        final float offset = n / 2f;

        path.addOval(left + offset, top + offset,
                right - offset, bottom - offset, getDirection());

        path.close();
    }

    /**
     * Draw a diamond into "path". The diamond's dimensions are set by the given bounds "left",
     * "top", "right" and "bottom".
     * <p>
     * At scale 1.0f this method draws a diamond exactly up to these bounds.
     * <p>
     * At scales less than 1.0f this method draws a shape inset into a "1.0f shape" in such a way
     * that each edge is a constant distance from the "1.0f shape", and at the same time, such that
     * the shape's area is "scale" the size of the "1.0f shape".
     * <p>
     * At scales greater than 1.0f, it works the same way, except the shape drawn will be
     * correspondingly larger (and outside of the bounds).
     * <p>
     * The vertical midpoint of the diamond is given by "midpoint". It ranges between 0.0f (the
     * top) and 1.0f (the bottom). Pass 0.5f for the vertical centre.
     *
     * @param path     The path to draw into
     * @param left     Left bound
     * @param top      Top bound
     * @param right    Right bound
     * @param bottom   Bottom bound
     * @param scale    Scale of the shape
     * @param midpoint The vertical midpoint of the diamond
     */
    void drawDiamond(@NonNull Path path, float left, float top, float right, float bottom,
                     float scale, float midpoint) {
        drawDiamond(path, left, top, right, bottom, scale, midpoint, 0f, 0f);
    }

    /**
     * Draw a diamond into "path". The diamond's dimensions are set by the given bounds "left",
     * "top", "right" and "bottom".
     *
     * At scale 1.0f this method draws a diamond exactly up to these bounds.
     *
     * At scales less than 1.0f this method draws a shape inset into a "1.0f shape" in such a way
     * that each edge is a constant distance from the "1.0f shape", and at the same time, such that
     * the shape's area is "scale" the size of the "1.0f shape".
     *
     * At scales greater than 1.0f, it works the same way, except the shape drawn will be
     * correspondingly larger (and outside of the bounds).
     *
     * The vertical midpoint of the diamond is given by "midpoint". It ranges between 0.0f (the
     * top) and 1.0f (the bottom). Pass 0.5f for the vertical centre.
     * @param path The path to draw into
     * @param left Left bound
     * @param top Top bound
     * @param right Right bound
     * @param bottom Bottom bound
     * @param scale Scale of the shape
     * @param midpoint The vertical midpoint of the diamond
     * @param offsetTop Percentage from top to start drawing; 0.0f for no offset
     * @param offsetBottom Percentage to bottom to finish drawing; 0.0f for no offset
     */
    void drawDiamond(@NonNull Path path, float left, float top, float right, float bottom,
                     float scale, float midpoint, float offsetTop, float offsetBottom) {
        final float diamondMidpoint = (top * midpoint) + (bottom * (1f - midpoint));

        // Scale factor. Ignored if scale == 1.0f
        final float x0 = (right - left) * 0.5f * (1f - scale);
        final float y1 = (diamondMidpoint - top) * (1f - scale);
        final float y2 = (bottom - diamondMidpoint) * (1f - scale);

        final float leftX = left + x0;
        final float rightX = right - x0;
        final float bottomY = bottom - y2;
        final float topY = top + y1;


        // Bottom: extend past the hub
        if (getDirection() == Path.Direction.CW) {
            path.moveTo(leftX, diamondMidpoint); // Left
            path.lineTo(mCenterX, topY); // Top
            path.lineTo(rightX, diamondMidpoint); // Right
        } else {
            path.moveTo(rightX, diamondMidpoint); // Right
            path.lineTo(mCenterX, topY); // Top
            path.lineTo(leftX, diamondMidpoint); // Left
        }
        path.lineTo(mCenterX, bottomY); // Bottom: extend past the hub
        path.close();

        // Top and bottom halves
        if (offsetTop != 0f || offsetBottom != 0f) {
            float t = Math.min(topY, bottomY); // Top of inner shape
            float b = Math.max(topY, bottomY); // Bottom of inner shape
            float h2 = b - t; // Height of inner shape

            // Apply the offsets. Because this is a triangle, apply the square root
            // so that we keep the proportions of the triangle
            t += h2 * (float) Math.sqrt(offsetTop);
            b -= h2 * (1f - (float) Math.sqrt(1f - offsetBottom));

            mShapeCutout.reset();
            // Make "mShapeCutout" a bit wider than the shape, then intersect with "path".
            mShapeCutout.addRect(
                    left - 1f * pc, t, right + 1f * pc, b, getDirection());
            path.op(mShapeCutout, Path.Op.INTERSECT);
        }
    }

    /**
     * Draw a triangle into "path". The triangle's dimensions are set by the given bounds "left",
     * "top", "right" and "bottom".
     *
     * At scale 1.0f this method draws a triangle exactly up to these bounds.
     *
     * At scales less than 1.0f this method draws a shape inset into a "1.0f shape" in such a way
     * that each edge is a constant distance from the "1.0f shape", and at the same time, such that
     * the shape's area is "scale" the size of the "1.0f shape".
     *
     * At scales greater than 1.0f, it works the same way, except the shape drawn will be
     * correspondingly larger (and outside of the bounds).
     *
     * If "top" is above "bottom" (the usual way) then the triangle is drawn pointing up. If "top"
     * is below "bottom" then the triangle is drawn pointing down.
     * @param path The path to draw into
     * @param left Left bound
     * @param top Top bound
     * @param right Right bound
     * @param bottom Bottom bound
     * @param scale Scale of the shape
     */
    void drawTriangle(@NonNull Path path, float left, float top, float right, float bottom,
                      float scale) {
        drawTriangle(path, left, top, right, bottom, scale, 0f, 0f);
    }

    /**
     * Draw a triangle into "path". The triangle's dimensions are set by the given bounds "left",
     * "top", "right" and "bottom".
     *
     * At scale 1.0f this method draws a triangle exactly up to these bounds.
     *
     * At scales less than 1.0f this method draws a shape inset into a "1.0f shape" in such a way
     * that each edge is a constant distance from the "1.0f shape", and at the same time, such that
     * the shape's area is "scale" the size of the "1.0f shape".
     *
     * At scales greater than 1.0f, it works the same way, except the shape drawn will be
     * correspondingly larger (and outside of the bounds).
     *
     * If "top" is above "bottom" (the usual way) then the triangle is drawn pointing up. If "top"
     * is below "bottom" then the triangle is drawn pointing down.
     * @param path The path to draw into
     * @param left Left bound
     * @param top Top bound
     * @param right Right bound
     * @param bottom Bottom bound
     * @param scale Scale of the shape
     * @param offsetTop Percentage from top to start drawing; 0.0f for no offset
     * @param offsetBottom Percentage to bottom to finish drawing; 0.0f for no offset
     */
    void drawTriangle(@NonNull Path path, float left, float top, float right, float bottom,
                      float scale, float offsetTop, float offsetBottom) {
        // If bottom is above top, invert the triangle!
        final boolean invert = bottom < top;
        // Scale factor. Ignored if scale == 0f
        final double h = invert ? top - bottom : bottom - top;
        final double w = (double) (right - left) * 0.5d;
        final double w1 = w - (w * Math.sqrt(scale));
        final double z = Math.sin(Math.atan(h / w) / 2d) * w1;
        final double z1 = h - (h * Math.sqrt(scale)) - z;

        final float leftX = left + (float) w1;
        final float rightX = right - (float) w1;
        final float bottomY = invert ? bottom + (float) z : bottom - (float) z;
        final float topX = mCenterX;
        final float topY = invert ? top - (float) z1 : top + (float) z1;

        if (getDirection() == Path.Direction.CW) {
            path.moveTo(leftX, bottomY); // Left
            path.lineTo(topX, topY); // Top
            path.lineTo(rightX, bottomY); // Right
        } else {
            path.moveTo(rightX, bottomY); // Right
            path.lineTo(topX, topY); // Top
            path.lineTo(leftX, bottomY); // Left
        }
        path.close();

        // Top and bottom halves
        if (offsetTop != 0f || offsetBottom != 0f) {
            float t = Math.min(topY, bottomY); // Top of inner shape
            float b = Math.max(topY, bottomY); // Bottom of inner shape
            float h2 = b - t; // Height of inner shape

            // Apply the offsets. Because this is a triangle, apply the square root
            // so that we keep the proportions of the triangle
            t += h2 * (float) Math.sqrt(offsetTop);
            b -= h2 * (1f - (float) Math.sqrt(1f - offsetBottom));

            mShapeCutout.reset();
            // Make "mShapeCutout" a bit wider than the shape, then intersect with "path".
            mShapeCutout.addRect(
                    left - 1f * pc, t, right + 1f * pc, b, getDirection());
            path.op(mShapeCutout, Path.Op.INTERSECT);
        }
    }
}
