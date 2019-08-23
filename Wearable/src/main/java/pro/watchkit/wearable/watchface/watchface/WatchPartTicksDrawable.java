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

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;

import androidx.annotation.NonNull;

import pro.watchkit.wearable.watchface.model.BytePackable.Style;
import pro.watchkit.wearable.watchface.model.BytePackable.TickShape;
import pro.watchkit.wearable.watchface.model.BytePackable.TickSize;
import pro.watchkit.wearable.watchface.model.WatchFaceState;

abstract class WatchPartTicksDrawable extends WatchPartDrawable {
    abstract protected boolean isVisible(int tickIndex);

    abstract protected float getMod();

    abstract protected TickShape getTickShape();

    abstract protected TickSize getTickSize();

    abstract protected Style getTickStyle();

    @NonNull
    private final Path p = new Path();
    @NonNull
    private final Path p2 = new Path();
    @NonNull
    private final Path t2 = new Path();
    @NonNull
    private final Path temp = new Path();
    @NonNull
    private final Path cutout = new Path();
    @NonNull
    private final Matrix mTempMatrix = new Matrix();

    @Override
    public void draw2(@NonNull Canvas canvas) {
        if (mWatchFaceState.isDeveloperMode() && mWatchFaceState.isHideTicks()) {
            // If we set developer mode "hide ticks", then just return!
            return;
        }

        Paint tickPaint = mWatchFaceState.getPaintBox().getPaintFromPreset(getTickStyle());

        p.reset();
        p2.reset();

        int numTicks = 60;
        for (int tickIndex = 0; tickIndex < numTicks; tickIndex++) {
            if (!isVisible(tickIndex)) {
                // Tick is not visible. Continue.
                continue;
            }

            float mCenter = Math.min(mCenterX, mCenterY);
            TickShape tickShape = getTickShape();
            TickSize tickSize = getTickSize();
//            TickThickness tickThickness = getTickThickness();
            // Modifiers: four ticks are one size up; sixty ticks one size down.
            float mod = getMod() / getMod(); // You know what, turn this off.

            // Draw the tick.

            // Get our dimensions.
            float tickWidth =
                    WatchFaceState.getTickThickness(tickShape, tickSize) * pc * mod;
            float tickSizeDimen =
                    WatchFaceState.getTickHalfLength(tickShape, tickSize) * pc * mod;
            float tickBandStart = mWatchFaceState.getTickBandStart(pc) * pc * mod;
            float tickBandHeight = mWatchFaceState.getTickBandHeight(pc) * pc * mod;
            float tickRadiusPositionDimen = tickBandStart + (tickBandHeight / 2f);

            float centerTickRadius = mCenter - tickRadiusPositionDimen;
            float tickDegrees = ((float) tickIndex / (float) numTicks) * 360f;

            float x = mCenterX;
            float y = mCenterY - centerTickRadius;

            temp.reset();
            cutout.reset();

            final float left = x - tickWidth;
            final float right = x + tickWidth;
            final float top = y - tickSizeDimen;
            final float bottom = y + tickSizeDimen;

            // Draw the object at 12 o'clock, then rotate it to desired location.
            switch (tickShape) {
                case SQUARE:
                case SQUARE_WIDE:
                case BAR_1_2:
                case BAR_1_4:
                case BAR_1_8: {
                    // Draw a square.
                    drawRect(temp, left, top, right, bottom, 1f);
                    break;
                }
                case SQUARE_CUTOUT: {
                    drawRect(temp, left, top, right, bottom, CUTOUT_SCALE_OUTER);
                    drawRect(cutout, left, top, right, bottom, CUTOUT_SCALE_INNER);
                    temp.op(cutout, Path.Op.DIFFERENCE);
                    break;
                }
                case SECTOR: {
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
                    t2.reset();
                    t2.addCircle(mCenterX, mCenterY,
                            mCenterY - y + tickSizeDimen, getDirection());
                    temp.op(t2, Path.Op.INTERSECT);
                    t2.reset();
                    t2.addCircle(mCenterX, mCenterY,
                            mCenterY - y - tickSizeDimen, getDirection());
                    temp.op(t2, Path.Op.DIFFERENCE);

                    break;
                }
                case DOT:
                case DOT_THIN: {
                    drawEllipse(temp, left, top, right, bottom, 1f);
                    break;
                }
                case DOT_CUTOUT: {
                    drawEllipse(temp, left, top, right, bottom, CUTOUT_SCALE_OUTER);
                    drawEllipse(cutout, left, top, right, bottom, CUTOUT_SCALE_INNER);
                    temp.op(cutout, Path.Op.DIFFERENCE);
                    break;
                }
                case TRIANGLE:
                case TRIANGLE_THIN: {
                    // Invert bottom and top to draw upside down!
                    drawTriangle(temp, left, bottom, right, top, 1f);
                    break;
                }
                case TRIANGLE_CUTOUT: {
                    // Invert bottom and top to draw upside down!
                    drawTriangle(temp, left, bottom, right, top, CUTOUT_SCALE_OUTER);
                    drawTriangle(cutout, left, bottom, right, top, CUTOUT_SCALE_INNER);
                    temp.op(cutout, Path.Op.DIFFERENCE);
                    break;
                }
                case DIAMOND:
                case DIAMOND_THIN: {
                    drawDiamond(temp, left, top, right, bottom, 1f, 0.5f);
                    break;
                }
                case DIAMOND_CUTOUT: {
                    drawDiamond(temp, left, top, right, bottom, CUTOUT_SCALE_OUTER, 0.5f);
                    drawDiamond(cutout, left, top, right, bottom, CUTOUT_SCALE_INNER, 0.5f);
                    temp.op(cutout, Path.Op.DIFFERENCE);
                    break;
                }
                default: {
                    break;
                }
            }

            mTempMatrix.reset();
            mTempMatrix.setRotate(tickDegrees, mCenterX, mCenterY);
            temp.transform(mTempMatrix);

            if (tickIndex % 2 == 0) {
                // Draw every 2nd tick into p2. This makes it so the bezels don't "stick together"
                // if butted up close to each other.
                // Generally this only happens for sixty ticks, but since the output is cached
                // we don't mind making the other cases slower for code clarity.
                p.op(temp, Path.Op.UNION);
            } else {
                p2.op(temp, Path.Op.UNION);
            }
        }

        drawPath(canvas, p, tickPaint);
        drawPath(canvas, p2, tickPaint);
    }
}
