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

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;

import java.util.GregorianCalendar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import pro.watchkit.wearable.watchface.model.LocationCalculator;
import pro.watchkit.wearable.watchface.model.WatchFaceState;

abstract class WatchPartDrawable extends Drawable {
    WatchFaceState mWatchFaceState;
    GregorianCalendar mCalendar;
    LocationCalculator mLocationCalculator;
    int height = 0, width = 0;
    float pc = 0f; // percent, set to 0.01f * height, all units are based on percent
    float mCenterX, mCenterY;

    void setState(WatchFaceState mWatchFaceState, GregorianCalendar mCalendar,
                  LocationCalculator mLocationCalculator) {
        this.mWatchFaceState = mWatchFaceState;
        this.mCalendar = mCalendar;
        this.mLocationCalculator = mLocationCalculator;
    }

//    boolean ambient;

//    private int mWatchHandShadowColor = Color.BLACK;
//    private float mBevelOffset = 0.2f; // 0.2%

    void drawPath(Canvas canvas, Path p, Paint paint) {
        // 4 layers:
        // Shadow
        // Primary bevel
        // Secondary bevel
        // And finally the path itself.
        if (!mWatchFaceState.ambient) {
            float mBevelOffset = 0.2f; // 0.2%

            // Shadow
            canvas.drawPath(p, mWatchFaceState.paintBox.getShadowPaint());
            // Primary bevel, offset to the top left
            {
                Paint bezelPaint1 = mWatchFaceState.paintBox.getBezelPaint1();
                Path primaryP = new Path();
                p.offset(-(mBevelOffset * pc), -(mBevelOffset * pc), primaryP);
                bezelPaint1.setStyle(Paint.Style.FILL_AND_STROKE);
                canvas.drawPath(primaryP, bezelPaint1);
            }
            // Secondary bevel, offset to the top right
            {
                Paint bezelPaint2 = mWatchFaceState.paintBox.getBezelPaint2();
                Path secondaryP = new Path();
                p.offset((mBevelOffset * pc), (mBevelOffset * pc), secondaryP);
                bezelPaint2.setStyle(Paint.Style.FILL_AND_STROKE);
                canvas.drawPath(secondaryP, bezelPaint2);
            }
            // The path itself.
            paint.setStyle(Paint.Style.FILL);
            canvas.drawPath(p, paint);
        } else {
            // Ambient.
            // The path itself.
            canvas.drawPath(p, mWatchFaceState.paintBox.getAmbientPaint());
        }
    }

    void onWidthAndHeightChanged(Canvas canvas) {
        width = canvas.getWidth();
        height = canvas.getHeight();
        pc = 0.01f * height;
        /*
         * Find the coordinates of the center point on the screen, and ignore the window
         * insets, so that, on round watches with a "chin", the watch face is centered on the
         * entire screen, not just the usable portion.
         */
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
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        // Check width and height.
        mWatchFaceState.paintBox.onWidthAndHeightChanged(canvas.getWidth(), canvas.getHeight());
        if (canvas.getWidth() != width || canvas.getHeight() != height) {
            onWidthAndHeightChanged(canvas);
        }

        // Override this but call super(canvas) first.
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
