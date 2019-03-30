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
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.Log;

import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;
import pro.watchkit.wearable.watchface.model.ComplicationHolder;
import pro.watchkit.wearable.watchface.model.PaintBox;
import pro.watchkit.wearable.watchface.model.WatchFacePreset.TickLength;
import pro.watchkit.wearable.watchface.model.WatchFacePreset.TickRadiusPosition;
import pro.watchkit.wearable.watchface.model.WatchFacePreset.TickShape;
import pro.watchkit.wearable.watchface.model.WatchFacePreset.TickThickness;

abstract class WatchPartTicksRingsDrawable extends WatchPartDrawable {
    private static final boolean useNewBackgroundCachingMethod = true;
    private int mPreviousSerial = -1;
    private int mPreviousNightVisionTint = -1;
    private Bitmap mTicksActiveBitmap = null;
    private Bitmap mTicksAmbientBitmap = null;
    private boolean mTicksAmbientBitmapInvalidated = true;
    private boolean mTicksActiveBitmapInvalidated = true;
    private Path mAmbientExclusionPath;
    private Paint mAmbientColorShiftPaint = new Paint();

    private Map<Pair<TickShape, TickThickness>, Float> mTickThicknessDimens = new Hashtable<>();
    private Map<Pair<TickShape, TickLength>, Float> mTickLengthDimens = new Hashtable<>();
    private Map<Pair<TickShape, TickRadiusPosition>, Float> mTickRadiusPositionDimens = new Hashtable<>();

    abstract protected boolean isVisible(int tickIndex);

    abstract protected float getMod();

    abstract protected TickShape getTickShape();

    abstract protected TickLength getTickLength();

    abstract protected TickThickness getTickThickness();

    abstract protected TickRadiusPosition getTickRadiusPosition();

    WatchPartTicksRingsDrawable() {
        super();

        float barThicknessScale = (float) (Math.PI / 60d);
        float barLengthScale = 3f;
        float triangleFactor = (float) (Math.sqrt(3d) / 2d); // Height of an equilateral triangle.

        float globalScale = 1.0f;

        // f0, f1, f2, f3 are a geometric series!
        float f0 = globalScale * (float) (1d / Math.sqrt(2d));
        float f1 = globalScale * 1f;
        float f2 = globalScale * (float) Math.sqrt(2d);
        float f3 = globalScale * 2f;

        // Scaling factors for dot, triangle and diamond.
        // Relative to a square of side 1. So all greater than 1.
        float dotScale = 2f / (float) Math.sqrt(Math.PI);
        float triangleScale = 2f / (float) Math.sqrt(Math.sqrt(3d));
        float diamondScale = (float) Math.sqrt(2d);

        mTickThicknessDimens.put(Pair.create(TickShape.BAR, TickThickness.THIN), barThicknessScale * 0.125f);
        mTickThicknessDimens.put(Pair.create(TickShape.BAR, TickThickness.REGULAR), barThicknessScale * 0.25f);
        mTickThicknessDimens.put(Pair.create(TickShape.BAR, TickThickness.THICK), barThicknessScale * 0.5f);
        mTickThicknessDimens.put(Pair.create(TickShape.BAR, TickThickness.X_THICK), barThicknessScale * 1.0f);

        mTickThicknessDimens.put(Pair.create(TickShape.DOT, TickThickness.THIN), dotScale * f0);
        mTickThicknessDimens.put(Pair.create(TickShape.DOT, TickThickness.REGULAR), dotScale * f1);
        mTickThicknessDimens.put(Pair.create(TickShape.DOT, TickThickness.THICK), dotScale * f2);
        mTickThicknessDimens.put(Pair.create(TickShape.DOT, TickThickness.X_THICK), dotScale * f3);

        mTickThicknessDimens.put(Pair.create(TickShape.TRIANGLE, TickThickness.THIN), triangleScale * f0);
        mTickThicknessDimens.put(Pair.create(TickShape.TRIANGLE, TickThickness.REGULAR), triangleScale * f1);
        mTickThicknessDimens.put(Pair.create(TickShape.TRIANGLE, TickThickness.THICK), triangleScale * f2);
        mTickThicknessDimens.put(Pair.create(TickShape.TRIANGLE, TickThickness.X_THICK), triangleScale * f3);

        mTickThicknessDimens.put(Pair.create(TickShape.DIAMOND, TickThickness.THIN), diamondScale * f0);
        mTickThicknessDimens.put(Pair.create(TickShape.DIAMOND, TickThickness.REGULAR), diamondScale * f1);
        mTickThicknessDimens.put(Pair.create(TickShape.DIAMOND, TickThickness.THICK), diamondScale * f2);
        mTickThicknessDimens.put(Pair.create(TickShape.DIAMOND, TickThickness.X_THICK), diamondScale * f3);

        mTickLengthDimens.put(Pair.create(TickShape.BAR, TickLength.SHORT), barLengthScale * f0);
        mTickLengthDimens.put(Pair.create(TickShape.BAR, TickLength.MEDIUM), barLengthScale * f1);
        mTickLengthDimens.put(Pair.create(TickShape.BAR, TickLength.LONG), barLengthScale * f2);
        mTickLengthDimens.put(Pair.create(TickShape.BAR, TickLength.X_LONG), barLengthScale * f3);

        mTickLengthDimens.put(Pair.create(TickShape.DOT, TickLength.SHORT), dotScale * f0);
        mTickLengthDimens.put(Pair.create(TickShape.DOT, TickLength.MEDIUM), dotScale * f1);
        mTickLengthDimens.put(Pair.create(TickShape.DOT, TickLength.LONG), dotScale * f2);
        mTickLengthDimens.put(Pair.create(TickShape.DOT, TickLength.X_LONG), dotScale * f3);

        mTickLengthDimens.put(Pair.create(TickShape.TRIANGLE, TickLength.SHORT), triangleFactor * triangleScale * f0);
        mTickLengthDimens.put(Pair.create(TickShape.TRIANGLE, TickLength.MEDIUM), triangleFactor * triangleScale * f1);
        mTickLengthDimens.put(Pair.create(TickShape.TRIANGLE, TickLength.LONG), triangleFactor * triangleScale * f2);
        mTickLengthDimens.put(Pair.create(TickShape.TRIANGLE, TickLength.X_LONG), triangleFactor * triangleScale * f3);

        mTickLengthDimens.put(Pair.create(TickShape.DIAMOND, TickLength.SHORT), diamondScale * f0);
        mTickLengthDimens.put(Pair.create(TickShape.DIAMOND, TickLength.MEDIUM), diamondScale * f1);
        mTickLengthDimens.put(Pair.create(TickShape.DIAMOND, TickLength.LONG), diamondScale * f2);
        mTickLengthDimens.put(Pair.create(TickShape.DIAMOND, TickLength.X_LONG), diamondScale * f3);

        // TODO: Make sure that (dot, triangle, diamond) are normalised, so if we select...
        // THIN/SHORT or X_THICK/X_LONG for (dot, triangle, diamond), their AREA is the same.
        // Simple geometry.

        mTickRadiusPositionDimens.put(Pair.create(TickShape.BAR, TickRadiusPosition.SHORT), 0f);
        mTickRadiusPositionDimens.put(Pair.create(TickShape.BAR, TickRadiusPosition.MEDIUM), 3f);
        mTickRadiusPositionDimens.put(Pair.create(TickShape.BAR, TickRadiusPosition.LONG), 6f);
        mTickRadiusPositionDimens.put(Pair.create(TickShape.BAR, TickRadiusPosition.X_LONG), 9f);

        mTickRadiusPositionDimens.put(Pair.create(TickShape.DOT, TickRadiusPosition.SHORT), 0f);
        mTickRadiusPositionDimens.put(Pair.create(TickShape.DOT, TickRadiusPosition.MEDIUM), 3f);
        mTickRadiusPositionDimens.put(Pair.create(TickShape.DOT, TickRadiusPosition.LONG), 6f);
        mTickRadiusPositionDimens.put(Pair.create(TickShape.DOT, TickRadiusPosition.X_LONG), 9f);

        mTickRadiusPositionDimens.put(Pair.create(TickShape.TRIANGLE, TickRadiusPosition.SHORT), 0f);
        mTickRadiusPositionDimens.put(Pair.create(TickShape.TRIANGLE, TickRadiusPosition.MEDIUM), 3f);
        mTickRadiusPositionDimens.put(Pair.create(TickShape.TRIANGLE, TickRadiusPosition.LONG), 6f);
        mTickRadiusPositionDimens.put(Pair.create(TickShape.TRIANGLE, TickRadiusPosition.X_LONG), 9f);

        mTickRadiusPositionDimens.put(Pair.create(TickShape.DIAMOND, TickRadiusPosition.SHORT), 0f);
        mTickRadiusPositionDimens.put(Pair.create(TickShape.DIAMOND, TickRadiusPosition.MEDIUM), 3f);
        mTickRadiusPositionDimens.put(Pair.create(TickShape.DIAMOND, TickRadiusPosition.LONG), 6f);
        mTickRadiusPositionDimens.put(Pair.create(TickShape.DIAMOND, TickRadiusPosition.X_LONG), 9f);
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        // Invalidate our ticks bitmaps. They'll be regenerated next time around.
        mTicksAmbientBitmapInvalidated = true;
        mTicksActiveBitmapInvalidated = true;
    }

    private Path p = new Path();
    private Path temp = new Path();

    @Override
    public void draw(@NonNull Canvas canvas) {
        boolean cacheHit = true;
        Bitmap ticksBitmap;

        int original = 0, currentNightVisionTint = 0;

        int unreadNotifications = mWatchFaceState.getUnreadNotifications();
        int totalNotifications = mWatchFaceState.getTotalNotifications();
        Collection<ComplicationHolder> complications = mWatchFaceState.getComplications();
        Paint twelveTickPaint = mWatchFaceState.getPaintBox().getPaintFromPreset(mWatchFaceState.getWatchFacePreset().getTwelveTickStyle());

        // Invalidate if complications, unread notifications or total notifications have changed.
        // Or the entire preset.
        int currentSerial = Objects.hash(mWatchFaceState.getWatchFacePreset(), twelveTickPaint, complications, unreadNotifications, totalNotifications);
        if (mPreviousSerial != currentSerial) {
            mTicksActiveBitmapInvalidated = true;
            mTicksAmbientBitmapInvalidated = true;
            mPreviousSerial = currentSerial;
        }

        // Invalidate if our night vision tint has changed
        if (mWatchFaceState.isAmbient()) {
            original = PaintBox.AMBIENT_WHITE;
            currentNightVisionTint = mWatchFaceState.getLocationCalculator().getDuskDawnColor(original);
            if (mPreviousNightVisionTint != currentNightVisionTint) {
                Log.d("AnalogWatchFace", "currentNightVisionTint: was "
                        + mPreviousNightVisionTint + ", now " + currentNightVisionTint);
                mTicksAmbientBitmapInvalidated = true;
                mPreviousNightVisionTint = currentNightVisionTint;
            }
        }

        // If we've been invalidated, regenerate and/or clear our bitmaps.
        if (mWatchFaceState.isAmbient()) {
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
            p.reset();
            temp.reset();
            int numTicks = 60;
            for (int tickIndex = 0; tickIndex < numTicks; tickIndex++) {
                float mCenter = Math.min(mCenterX, mCenterY);
                boolean isTickVisible = isVisible(tickIndex);
                TickShape tickShape = getTickShape();
                TickLength tickLength = getTickLength();
                TickThickness tickThickness = getTickThickness();
                TickRadiusPosition tickRadiusPosition = getTickRadiusPosition();
                // Modifiers: four ticks are one size up; sixty ticks one size down.
                float mod = getMod();

                boolean isSixOClock = tickIndex == numTicks / 2;

                boolean drawUnreadNotification = totalNotifications > 0;

                if (isSixOClock && drawUnreadNotification) {
                    // TODO: Don't draw this as a tick, draw our ticks then punch this out.
                    float tickRadiusPositionDimen =
                            mTickRadiusPositionDimens.get(Pair.create(tickShape, tickRadiusPosition)) * pc;
                    float centerTickRadius = mCenter - tickRadiusPositionDimen;
                    float x = mCenterX;// + (innerX + outerX) / 2f;
                    float y = mCenterY + centerTickRadius;//+ (innerY + outerY) / 2f;

                    p.addCircle(x, y, 4f * pc, Path.Direction.CW);
                    if (!mWatchFaceState.isAmbient()) {
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
                } else if (isTickVisible) {
                    // Draw the tick.

                    // Get our dimensions.
                    float tickWidth =
                            mTickThicknessDimens.get(Pair.create(tickShape, tickThickness)) * pc * mod;
                    float tickLengthDimen =
                            mTickLengthDimens.get(Pair.create(tickShape, tickLength)) * pc * mod;
                    float tickRadiusPositionDimen =
                            mTickRadiusPositionDimens.get(Pair.create(tickShape, tickRadiusPosition)) * pc;

                    float centerTickRadius = mCenter - tickRadiusPositionDimen;
                    float tickDegrees = ((float) tickIndex / (float) numTicks) * 360f;

                    float x = mCenterX;
                    float y = mCenterY - centerTickRadius;

                    temp.reset();

                    // Draw the object at 12 o'clock, then rotate it to desired location.
                    switch (tickShape) {
                        case BAR: {
                            // Draw a really large triangle, then crop it with two
                            // circles to give us a wedge shape with arc top and bottom.
                            // Height "2 * mCenterY", centered on (mCenterX, mCenterY)
                            temp.moveTo(mCenterX, mCenterY);
                            // Assume "tickWidth" is radians. Undo the multiplication by "pc".
                            // Also undo the multiplication by "mod". Assume we don't mod that.
                            double offsetRadians = tickWidth / (pc * mod);
                            float offsetX = (float) Math.sin(offsetRadians) * 2 * mCenterY;
                            if (getDirection() == Path.Direction.CW) {
                                // Line to top left.
                                temp.lineTo(x - offsetX, 0f - mCenterY);
                                // Line to top right.
                                temp.lineTo(x + offsetX, 0f - mCenterY);
                            } else {
                                // Line to top right.
                                temp.lineTo(x + offsetX, 0f - mCenterY);
                                // Line to top left.
                                temp.lineTo(x - offsetX, 0f - mCenterY);
                            }
                            // And line back to origin.
                            temp.close();

                            // Crop it with our top circle and bottom circle.
                            Path t2 = new Path();
                            t2.addCircle(mCenterX, mCenterY,
                                    mCenterY - y + tickLengthDimen, getDirection());
                            temp.op(t2, Path.Op.INTERSECT);
                            t2.reset();
                            t2.addCircle(mCenterX, mCenterY,
                                    mCenterY - y - tickLengthDimen, getDirection());
                            temp.op(t2, Path.Op.DIFFERENCE);

                            break;
                        }
                        case DOT: {
                            // Draw an oval.
                            temp.addOval(
                                    x - tickWidth,
                                    y - tickLengthDimen,
                                    x + tickWidth,
                                    y + tickLengthDimen,
                                    getDirection());
                            break;
                        }
                        case TRIANGLE: {
                            // Move to top left.
                            temp.moveTo(x - tickWidth, y - tickLengthDimen);
                            if (getDirection() == Path.Direction.CW) {
                                // Line to top right.
                                temp.lineTo(x + tickWidth, y - tickLengthDimen);
                                // Line to bottom centre.
                                temp.lineTo(x, y + tickLengthDimen);
                            } else {
                                // Line to bottom centre.
                                temp.lineTo(x, y + tickLengthDimen);
                                // Line to top right.
                                temp.lineTo(x + tickWidth, y - tickLengthDimen);
                            }
                            // And line back to origin.
                            temp.close();
                            break;
                        }
                        case DIAMOND: {
                            // Move to top centre.
                            temp.moveTo(x, y - tickLengthDimen);
                            if (getDirection() == Path.Direction.CW) {
                                // Line to centre right.
                                temp.lineTo(x + tickWidth, y);
                                // Line to bottom centre.
                                temp.lineTo(x, y + tickLengthDimen);
                                // Line to centre left.
                                temp.lineTo(x - tickWidth, y);
                            } else {
                                // Line to centre left.
                                temp.lineTo(x - tickWidth, y);
                                // Line to bottom centre.
                                temp.lineTo(x, y + tickLengthDimen);
                                // Line to centre right.
                                temp.lineTo(x + tickWidth, y);
                            }
                            // And line back to origin.
                            temp.close();
                            break;
                        }
                        default: {
                            break;
                        }
                    }

                    Matrix m = new Matrix();
                    m.setRotate(tickDegrees, mCenterX, mCenterY);
                    temp.transform(m);

                    p.op(temp, Path.Op.UNION);
                }
            }

            // Test: if ambient, draw our burn-in exclusion rings
            if (mWatchFaceState.isAmbient()) {
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
                if (mWatchFaceState.isAmbient()) {
                    color = mWatchFaceState.getPaintBox().getAmbientPaint().getColor();
                    mWatchFaceState.getPaintBox().getAmbientPaint().setColor(PaintBox.AMBIENT_WHITE);
                }
                drawPath(tempCanvas, p, twelveTickPaint);
//                drawPath(tempCanvas, p, mPaintBox.getTickPaint());
                if (mWatchFaceState.isAmbient()) {
                    mWatchFaceState.getPaintBox().getAmbientPaint().setColor(color);
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
//                drawPath(canvas, p, mPaintBox.getTickPaint());
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

            Paint paint = mWatchFaceState.isAmbient() ? mAmbientColorShiftPaint : null;
            canvas.drawBitmap(ticksBitmap, 0, 0, paint);
        }
    }
}
