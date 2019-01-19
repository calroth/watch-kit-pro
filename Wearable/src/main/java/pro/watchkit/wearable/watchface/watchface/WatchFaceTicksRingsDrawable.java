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
 * This file incorporates work covered by the following copyright and
 * permission notice:
 *
 *     Copyright (C) 2017 The Android Open Source Project
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */

package pro.watchkit.wearable.watchface.watchface;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.wearable.complications.rendering.ComplicationDrawable;
import android.util.Log;

import java.util.Collection;
import java.util.Objects;

import androidx.annotation.NonNull;
import pro.watchkit.wearable.watchface.model.ComplicationHolder;
import pro.watchkit.wearable.watchface.model.Palette;

final class WatchFaceTicksRingsDrawable extends WatchFaceDrawable {
    private static final boolean useNewBackgroundCachingMethod = true;
    private static final float TICK_WIDTH_PERCENT = 0.05f; // 0.05%
    private int mPreviousSerial = -1;
    private int mPreviousNightVisionTint = -1;
    private Bitmap mTicksActiveBitmap = null;
    private Bitmap mTicksAmbientBitmap = null;
    private boolean mTicksAmbientBitmapInvalidated = true;
    private boolean mTicksActiveBitmapInvalidated = true;
    private Path mAmbientExclusionPath;
    private Paint mAmbientColorShiftPaint = new Paint();

    @Override
    void onWidthAndHeightChanged(Canvas canvas) {
        super.onWidthAndHeightChanged(canvas);

        // Invalidate our ticks bitmaps. They'll be regenerated next time around.
        mTicksAmbientBitmapInvalidated = true;
        mTicksActiveBitmapInvalidated = true;
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        super.draw(canvas);

        boolean cacheHit = true;
        Bitmap ticksBitmap;

        int original = 0, currentNightVisionTint = 0;

        int unreadNotifications = mStateObject.unreadNotifications;
        int totalNotifications = mStateObject.totalNotifications;
        Collection<ComplicationHolder> complications = mStateObject.complications;
        Paint twelveTickPaint = mPalette.getPaintFromPreset(mStateObject.preset.getTwelveTickPalette());

        // Invalidate if complications, unread notifications or total notifications have changed.
        int currentSerial = Objects.hash(twelveTickPaint, complications, unreadNotifications, totalNotifications);
        if (mPreviousSerial != currentSerial) {
            mTicksActiveBitmapInvalidated = true;
            mTicksAmbientBitmapInvalidated = true;
            mPreviousSerial = currentSerial;
        }

        // Invalidate if our night vision tint has changed
        if (mStateObject.ambient) {
            original = Palette.AMBIENT_WHITE;
            currentNightVisionTint = mLocationCalculator.getDuskDawnColor(original);
            if (mPreviousNightVisionTint != currentNightVisionTint) {
                Log.d("AnalogWatchFace", "currentNightVisionTint: was "
                        + mPreviousNightVisionTint + ", now " + currentNightVisionTint);
                mTicksAmbientBitmapInvalidated = true;
                mPreviousNightVisionTint = currentNightVisionTint;
            }
        }

        // If we've been invalidated, regenerate and/or clear our bitmaps.
        if (mStateObject.ambient) {
            if (mTicksAmbientBitmapInvalidated) {
                // Initialise bitmap on first use or if our width/height have changed.
                if (mTicksAmbientBitmap == null) {
                    mTicksAmbientBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                } else if (mTicksAmbientBitmap.getWidth() != width ||
                        mTicksAmbientBitmap.getHeight() != height) {
                    mTicksAmbientBitmap.setWidth(width);
                    mTicksAmbientBitmap.setHeight(height);
                    mTicksAmbientBitmap.eraseColor(Color.TRANSPARENT);
                } else {
                    mTicksAmbientBitmap.eraseColor(Color.TRANSPARENT);
                }

                cacheHit = false;
                mTicksAmbientBitmapInvalidated = false;
            }

            ticksBitmap = mTicksAmbientBitmap;
        } else {
            if (mTicksActiveBitmapInvalidated) {
                // Initialise bitmap on first use or if our width/height have changed.
                if (mTicksActiveBitmap == null) {
                    mTicksActiveBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                } else if (mTicksActiveBitmap.getWidth() != width ||
                        mTicksActiveBitmap.getHeight() != height) {
                    mTicksActiveBitmap.setWidth(width);
                    mTicksActiveBitmap.setHeight(height);
                    mTicksActiveBitmap.eraseColor(Color.TRANSPARENT);
                } else {
                    mTicksActiveBitmap.eraseColor(Color.TRANSPARENT);
                }

                cacheHit = false;
                mTicksActiveBitmapInvalidated = false;
//                Log.d("AnalogWatchFace", "BigInvalidated!");
            }

            ticksBitmap = mTicksActiveBitmap;
        }

//        if (mTicksPreset != null) {
//            cacheHit = preset.getMinuteHandShape() == mTicksPreset.getMinuteHandShape() &
//                    preset.getMinuteHandLength() == mTicksPreset.getMinuteHandLength() &&
//                    preset.getMinuteHandThickness() == mTicksPreset.getMinuteHandThickness() &&
//                    preset.getMinuteHandStalk() == mTicksPreset.getMinuteHandStalk();
//            // TODO: Fix that...
//        }

        if (!cacheHit) {

//        Paint textPaint = ambient ? mAmbientPaint : mFillHighlightPaint;
//        String notification = unreadNotifications + "/" + totalNotifications;
//        float width = textPaint.measureText(notification);
//        canvas.drawText(notification, mCenterX - (width / 2f), mCenterY / 2f, textPaint);

            RectF oval = new RectF();
            float startAngle, sweepRadius;
            Path p = new Path();
            /*
             * Draw ticks. Usually you will want to bake this directly into the photo, but in
             * cases where you want to allow users to select their own photos, this dynamically
             * creates them on top of the photo.
             */
            float innerTickRadius; // = mCenterX - 10;
            float outerTickRadius; // = mCenterX;
            int numTicks = 60;
            //if (ambient) {
            //    mTickAndCirclePaint.setStrokeWidth(0);
            //    outerTickRadius -= 5;
            //} else {
            //    mTickAndCirclePaint.setStrokeWidth(PAINT_STROKE_WIDTH_PERCENT);
            //}
//        Log.d("AnalogWatchFace", "Start ticks");
            for (int tickIndex = 0; tickIndex < numTicks; tickIndex++) {
                int majorTickDegrees = 90; // One major tick every 90 degrees.
                int minorTickDegrees = 30; // One minor tick every 30 degrees.
                if (tickIndex * (360 / numTicks) % majorTickDegrees == 0) {
                    //innerTickRadius = outerTickRadius - 20;
//                outerTickRadius = mCenterX - (3f * pc);// - 10;
//                innerTickRadius = mCenterX - (12f * pc);// - 40;
//                    outerTickRadius = mCenterX - (3f * pc);// - 10;
//                    innerTickRadius = mCenterX - (15f * pc);// - 40;
                    outerTickRadius = mCenterX - (1f * pc);
                    innerTickRadius = mCenterX - (13f * pc);
                } else if (tickIndex * (360 / numTicks) % minorTickDegrees == 0) {
                    //innerTickRadius = outerTickRadius - 10;
//                outerTickRadius = mCenterX - (5f * pc);// 15;
//                innerTickRadius = mCenterX - (12f * pc);// 40;
//                    outerTickRadius = mCenterX - (5f * pc);// 15;
//                    innerTickRadius = mCenterX - (12f * pc);// 40;
                    outerTickRadius = mCenterX - (3f * pc);
                    innerTickRadius = mCenterX - (10f * pc);
                } else {
                    //innerTickRadius = outerTickRadius - 5;
//                outerTickRadius = mCenterX - (5f * pc);// 15;
//                innerTickRadius = mCenterX - (10f * pc);// 30;
//                    outerTickRadius = mCenterX - (7f * pc);// 15;
//                    innerTickRadius = mCenterX - (9f * pc);// 30;
                    outerTickRadius = mCenterX - (5f * pc);
                    innerTickRadius = mCenterX - (7f * pc);
                }

                boolean isSixOClock = tickIndex == numTicks / 2;

                float tickRot = (float) (tickIndex * Math.PI * 2 / numTicks);
                float innerX = (float) Math.sin(tickRot) * innerTickRadius;
                float innerY = (float) -Math.cos(tickRot) * innerTickRadius;
                float outerX = (float) Math.sin(tickRot) * outerTickRadius;
                float outerY = (float) -Math.cos(tickRot) * outerTickRadius;
                /*canvas.drawLine(
                        mCenterX + innerX,
                        mCenterY + innerY,
                        mCenterX + outerX,
                        mCenterY + outerY,
                        mTickAndCirclePaint);*/

                boolean drawUnreadNotification = totalNotifications > 0;

                if (isSixOClock && drawUnreadNotification) {
                    float x = mCenterX + (innerX + outerX) / 2f;
                    float y = mCenterY + (innerY + outerY) / 2f;
                    p.addCircle(x, y, 4f * pc, Path.Direction.CW);
                    if (!mStateObject.ambient) {
                        // Punch a hole in the circle to make it a donut.
                        Path p2 = new Path();
                        p2.addCircle(x, y, 3f * pc, Path.Direction.CW);
                        p.op(p2, Path.Op.DIFFERENCE);
                    }
                    if (unreadNotifications > 0) {
                        // Extra circle for unread notifications.
                        p.addCircle(x, y, 2f * pc, Path.Direction.CW);
                    }
                } else if (numTicks == 60 && (tickIndex == 29 || tickIndex == 31) && drawUnreadNotification) {
                    // Don't draw seconds ticks 29 or 31 (either side of the notification at 30).
                    // (Do nothing.)
                } else {
                    // Draw the tick.

                    float tickWidth = TICK_WIDTH_PERCENT * pc;
                    float halfTickWidth = tickWidth / 2f;

                    // Draw the anticlockwise-side line and the outer curve.
                    tickRot = (float) ((tickIndex - halfTickWidth) * Math.PI * 2 / numTicks);
                    innerX = (float) Math.sin(tickRot) * innerTickRadius;
                    innerY = (float) -Math.cos(tickRot) * innerTickRadius;
                    outerX = (float) Math.sin(tickRot) * outerTickRadius;
                    outerY = (float) -Math.cos(tickRot) * outerTickRadius;
                    startAngle = ((tickIndex - halfTickWidth) * 360 / numTicks) - 90f;
                    sweepRadius = tickWidth * 360f / numTicks;
                    oval.set(mCenterX - outerTickRadius, mCenterY - outerTickRadius,
                            mCenterX + outerTickRadius, mCenterY + outerTickRadius);

                    p.moveTo(mCenterX + innerX, mCenterY + innerY);
                    p.lineTo(mCenterX + outerX, mCenterY + outerY);
                    p.arcTo(oval, startAngle, sweepRadius);

                    // Draw the clockwise-side line and the inner curve back to the start.
                    tickRot = (float) ((tickIndex + halfTickWidth) * Math.PI * 2 / numTicks);
                    innerX = (float) Math.sin(tickRot) * innerTickRadius;
                    innerY = (float) -Math.cos(tickRot) * innerTickRadius;

                    startAngle = ((tickIndex + halfTickWidth) * 360 / numTicks) - 90f;
                    sweepRadius = tickWidth * (-360f) / numTicks;
                    p.lineTo(mCenterX + innerX, mCenterY + innerY);
                    oval.set(mCenterX - innerTickRadius, mCenterY - innerTickRadius,
                            mCenterX + innerTickRadius, mCenterY + innerTickRadius);
                    p.arcTo(oval, startAngle, sweepRadius);
                }
            }

            // If not ambient, draw our complication rings.
            if (!mStateObject.ambient && complications != null) {
                Path rings = new Path();
                Path holes = new Path();

                Path.Direction d = Path.Direction.CW;
                for (ComplicationHolder complication : complications) {
                    if (complication.isForeground && complication.isActive) {
                        Rect r = complication.getBounds();
                        rings.addCircle(r.exactCenterX(), r.exactCenterY(),
                                1.05f * r.width() / 2f, d);
                        // Reverse direction!
                        d = d == Path.Direction.CW ? Path.Direction.CCW : Path.Direction.CW;
                        holes.addCircle(r.exactCenterX(), r.exactCenterY(),
                                1.01f * r.width() / 2f, d);
                        complication.setBorderStyleActive(
                                ComplicationDrawable.BORDER_STYLE_NONE);
                    }
                }

                p.op(rings, Path.Op.UNION);
                p.op(holes, Path.Op.DIFFERENCE);
//                p.op(getComplicationRings(complications), Path.Op.UNION);
//                p.op(getComplicationHoles(complications), Path.Op.DIFFERENCE);
            }

            // Test: if ambient, draw our burn-in exclusion rings
            if (mStateObject.ambient) {
//            p.addCircle(mCenterX + 10, mCenterY + 10, mCenterX, Path.Direction.CW);
//            p.addCircle(mCenterX + 10, mCenterY - 10, mCenterX, Path.Direction.CW);
//            p.addCircle(mCenterX - 10, mCenterY + 10, mCenterX, Path.Direction.CW);
//            p.addCircle(mCenterX - 10, mCenterY - 10, mCenterX, Path.Direction.CW);

                if (mAmbientExclusionPath == null) {
                    mAmbientExclusionPath = new Path();
//                    mAmbientExclusionPath.addCircle(mCenterX + 10, mCenterY + 10, mCenterX, Path.Direction.CW);
//                    Path p2 = new Path();
//                    p2.addCircle(mCenterX + 10, mCenterY - 10, mCenterX, Path.Direction.CW);
//                    Path p3 = new Path();
//                    p3.addCircle(mCenterX - 10, mCenterY + 10, mCenterX, Path.Direction.CW);
//                    Path p4 = new Path();
//                    p4.addCircle(mCenterX - 10, mCenterY - 10, mCenterX, Path.Direction.CW);
//
//                    mAmbientExclusionPath.op(p2, Path.Op.INTERSECT);
//                    mAmbientExclusionPath.op(p3, Path.Op.INTERSECT);
//                    mAmbientExclusionPath.op(p4, Path.Op.INTERSECT);

                    final int exclusion = 6;

                    Path p5 = new Path();
                    p5.addCircle(mCenterX + exclusion, mCenterY + exclusion, mCenterX, Path.Direction.CW);
                    Path p6 = new Path();
                    p6.addCircle(mCenterX + exclusion, mCenterY - exclusion, mCenterX, Path.Direction.CW);
                    Path p7 = new Path();
                    p7.addCircle(mCenterX - exclusion, mCenterY + exclusion, mCenterX, Path.Direction.CW);
                    Path p8 = new Path();
                    p8.addCircle(mCenterX - exclusion, mCenterY - exclusion, mCenterX, Path.Direction.CW);

                    p5.op(p6, Path.Op.INTERSECT);
                    p5.op(p7, Path.Op.INTERSECT);
                    p5.op(p8, Path.Op.INTERSECT);

                    mAmbientExclusionPath.addPath(p5);
                }

//                p.addPath(mAmbientExclusionPath);
                p.op(mAmbientExclusionPath, Path.Op.INTERSECT);

//            p.addCircle(mCenterX, mCenterY, mCenterX - 20f, Path.Direction.CW);
            }

            //drawPath(canvas, mTickAndCirclePaint, p);

            if (useNewBackgroundCachingMethod) {
                Canvas tempCanvas = new Canvas(ticksBitmap);
                int color = -1;
                // Save and restore ambient color; for caching we always use white.
                if (mStateObject.ambient) {
                    color = mPalette.getAmbientPaint().getColor();
                    mPalette.getAmbientPaint().setColor(Palette.AMBIENT_WHITE);
                }
                drawPath(tempCanvas, p, twelveTickPaint);
//                drawPath(tempCanvas, p, mPalette.getTickPaint());
                if (mStateObject.ambient) {
                    mPalette.getAmbientPaint().setColor(color);
                }

                // Hardware Bitmap Power
//                if (Build.VERSION.SDK_INT >= 26 && canvas.isHardwareAccelerated()) {
//                    if (ambient) {
//                        mTicksAmbientBitmap = ticksBitmap.copy(Bitmap.Config.HARDWARE, false);
//                        ticksBitmap = mTicksAmbientBitmap;
//                    } else {
//                        mTicksActiveBitmap = ticksBitmap.copy(Bitmap.Config.HARDWARE, false);
//                        ticksBitmap = mTicksActiveBitmap;
//                    }
//                }
            } else {
                drawPath(canvas, p, twelveTickPaint);
//                drawPath(canvas, p, mPalette.getTickPaint());
            }

//        Log.d("AnalogWatchFace", "End ticks");
        }

//        Log.d("AnalogWatchFace", "drawTicksAndComplicationRings: drawing " +
//                (ambient ? "ambient" : "active") + " from " +
//                (cacheHit ? "cache" : "populating the cache with a new graphic"));

        if (useNewBackgroundCachingMethod) {
            //ColorFilter f = new PorterDuffColorFilter(mAmbientPaint.getColor(), PorterDuff.Mode.SRC_ATOP);
            mAmbientColorShiftPaint.setColorFilter(currentNightVisionTint != original
                    ? new LightingColorFilter(currentNightVisionTint, 0) : null);

            Paint paint = mStateObject.ambient ? mAmbientColorShiftPaint : null;
            canvas.drawBitmap(ticksBitmap, 0, 0, paint);
        }
    }
}
