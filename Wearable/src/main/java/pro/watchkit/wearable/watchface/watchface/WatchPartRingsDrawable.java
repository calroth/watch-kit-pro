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
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.support.wearable.complications.rendering.ComplicationDrawable;
import android.util.Log;

import java.util.Collection;
import java.util.Objects;

import androidx.annotation.NonNull;
import pro.watchkit.wearable.watchface.model.ComplicationHolder;
import pro.watchkit.wearable.watchface.model.PaintBox;

final class WatchPartRingsDrawable extends WatchPartDrawable {
    private static final boolean useNewBackgroundCachingMethod = true;
    private int mPreviousSerial = -1;
    private int mPreviousNightVisionTint = -1;
    private Bitmap mTicksActiveBitmap = null;
    private Bitmap mTicksAmbientBitmap = null;
    private boolean mTicksAmbientBitmapInvalidated = true;
    private boolean mTicksActiveBitmapInvalidated = true;
    private Paint mAmbientColorShiftPaint = new Paint();
    private Path p = new Path();
    private Path rings = new Path();
    private Path holes = new Path();

    @Override
    String getStatsName() {
        return "Rings";
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        // Invalidate our ticks bitmaps. They'll be regenerated next time around.
        mTicksAmbientBitmapInvalidated = true;
        mTicksActiveBitmapInvalidated = true;
    }

    @Override
    public void draw2(@NonNull Canvas canvas) {
        boolean cacheHit = true;
        Bitmap ticksBitmap;

        int original = 0, currentNightVisionTint = 0;

        Collection<ComplicationHolder> complications = mWatchFaceState.getComplications();
        Paint twelveTickPaint = mWatchFaceState.getPaintBox().getPaintFromPreset(mWatchFaceState.getWatchFacePreset().getTwelveTickStyle());

        // Invalidate if complications, unread notifications or total notifications have changed.
        // Or the entire preset.
        int currentSerial = Objects.hash(mWatchFaceState.getWatchFacePreset(), twelveTickPaint, complications);
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
            }

            ticksBitmap = mTicksActiveBitmap;
        }

        if (!cacheHit) {
            p.reset();

            // If not ambient, draw our complication rings.
            if (!mWatchFaceState.isAmbient() && complications != null) {
                rings.reset();
                holes.reset();

                for (ComplicationHolder complication : complications) {
                    if (complication.isForeground && complication.isActive) {
                        Rect r = complication.getBounds();
                        rings.addCircle(r.exactCenterX(), r.exactCenterY(),
                                1.05f * r.width() / 2f, getDirection());
                        holes.addCircle(r.exactCenterX(), r.exactCenterY(),
                                1.01f * r.width() / 2f, getDirection());
                        complication.setBorderStyleActive(
                                ComplicationDrawable.BORDER_STYLE_NONE);
                    }
                }

                p.op(rings, Path.Op.UNION);
                p.op(holes, Path.Op.DIFFERENCE);
            }

            if (useNewBackgroundCachingMethod) {
                Canvas tempCanvas = new Canvas(ticksBitmap);
                int color = -1;
                // Save and restore ambient color; for caching we always use white.
                if (mWatchFaceState.isAmbient()) {
                    color = mWatchFaceState.getPaintBox().getAmbientPaint().getColor();
                    mWatchFaceState.getPaintBox().getAmbientPaint().setColor(PaintBox.AMBIENT_WHITE);
                }
                drawPath(tempCanvas, p, twelveTickPaint);
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
            }
        }

        if (useNewBackgroundCachingMethod) {
            //ColorFilter f = new PorterDuffColorFilter(mAmbientPaint.getColor(), PorterDuff.Mode.SRC_ATOP);
            mAmbientColorShiftPaint.setColorFilter(currentNightVisionTint != original
                    ? new LightingColorFilter(currentNightVisionTint, 0) : null);

            Paint paint = mWatchFaceState.isAmbient() ? mAmbientColorShiftPaint : null;
            canvas.drawBitmap(ticksBitmap, 0, 0, paint);
        }
    }
}
