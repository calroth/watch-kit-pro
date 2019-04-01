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
    int height = 0, width = 0;
    float pc = 0f; // percent, set to 0.01f * height, all units are based on percent
    float mCenterX, mCenterY;

    void setWatchFaceState(@NonNull WatchFaceState watchFaceState) {
        mWatchFaceState = watchFaceState;
    }

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
    protected Path.Direction getDirection() {
        mDirection = mDirection == Path.Direction.CCW ? Path.Direction.CW : Path.Direction.CCW;
        return mDirection;
    }

    private final float mBevelOffset = 0.2f; // 0.2%

    private static Canvas mBezelCanvas;
    private static Paint mBezelBitmapPaint;

    boolean canBeCached() {
        return false;
    }

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

    void drawPath(Canvas canvas, Path p, Paint paint) {
        // 4 layers:
        // Shadow
        // Primary bevel
        // Secondary bevel
        // And finally the path itself.
        final boolean olde = false;
        if (!mWatchFaceState.isAmbient()) {

            // Shadow
            canvas.drawPath(p, mWatchFaceState.getPaintBox().getShadowPaint());
            // Primary bevel, offset to the top left
            if (olde) {
                Paint bezelPaint1 = mWatchFaceState.getPaintBox().getBezelPaint1();
                Path primaryP = new Path();
                p.offset(-(mBevelOffset * pc), -(mBevelOffset * pc), primaryP);
                bezelPaint1.setStyle(Paint.Style.FILL_AND_STROKE);
                canvas.drawPath(primaryP, bezelPaint1);
            }
            // Secondary bevel, offset to the top right
            if (olde) {
                Paint bezelPaint2 = mWatchFaceState.getPaintBox().getBezelPaint2();
                Path secondaryP = new Path();
                p.offset((mBevelOffset * pc), (mBevelOffset * pc), secondaryP);
                bezelPaint2.setStyle(Paint.Style.FILL_AND_STROKE);
                canvas.drawPath(secondaryP, bezelPaint2);
            }
            // The path itself.
            paint.setStyle(Paint.Style.FILL);
            canvas.drawPath(p, paint);
            // Primary and secondary bevels as stroke.
            if (!olde) {
                // Draw our bevels to a temporary bitmap.
                // Clear the bezel canvas first.
                mBezelCanvas.drawColor(Color.TRANSPARENT);

                // Draw primary bevel.
                Paint bezelPaint1 = mWatchFaceState.getPaintBox().getBezelPaint1();
                Path primaryP = new Path();
                p.offset(-(mBevelOffset * pc), -(mBevelOffset * pc), primaryP);
                bezelPaint1.setStyle(Paint.Style.FILL);
                mBezelCanvas.drawPath(primaryP, bezelPaint1);

                // Draw secondary bevel.
                Paint bezelPaint2 = mWatchFaceState.getPaintBox().getBezelPaint2();
                Path secondaryP = new Path();
                p.offset((mBevelOffset * pc), (mBevelOffset * pc), secondaryP);
                bezelPaint2.setStyle(Paint.Style.FILL);
                mBezelCanvas.drawPath(secondaryP, bezelPaint2);

                // Draw a stroke with our new bitmap-shader paint.
                canvas.drawPath(p, mBezelBitmapPaint);
            }
        } else {
            // Ambient.
            // The path itself.
            canvas.drawPath(p, mWatchFaceState.getPaintBox().getAmbientPaint());
        }
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);

        width = bounds.width();
        height = bounds.height();
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

}