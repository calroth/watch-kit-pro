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

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;

import androidx.annotation.NonNull;

import java.util.Collection;

import pro.watchkit.wearable.watchface.model.ComplicationHolder;

final class WatchPartRingsDrawable extends WatchPartDrawable {
    @NonNull
    private final Path mPath = new Path();
    @NonNull
    private final Path mRings = new Path();
    @NonNull
    private final Path mHoles = new Path();
    @NonNull
    private final Path mBackground = new Path();

    private boolean mDrawAllRings = false;

    WatchPartRingsDrawable() {
        super();
    }

    WatchPartRingsDrawable(boolean drawAllRings) {
        super();
        mDrawAllRings = drawAllRings;
    }

    @NonNull
    @Override
    String getStatsName() {
        return "Rings";
    }

    @Override
    public void draw2(@NonNull Canvas canvas) {
        Collection<ComplicationHolder> complications =
                mWatchFaceState.getComplicationsForDrawing(getBounds());

        if (complications.size() == 0) {
            // Early finish if we don't actually have any complications.
            return;
        }

        if (hasStateChanged()) {
            mRings.reset();
            mHoles.reset();
            mBackground.reset();

            final float ringRadius = 1.05f;
            final float holeRadius = 0.95f;
            final float backgroundRadius = (ringRadius + holeRadius) / 2f;

            // Calculate our mRings and mHoles!
            complications.stream()
                    .filter(c -> c.isForeground && c.getBounds() != null && (c.isActive || mDrawAllRings))
                    .forEach(c -> {
                        Rect r = c.getBounds();
                        Path.Direction dir = getDirection();
                        mPath.reset();
                        mPath.addCircle(r.exactCenterX(), r.exactCenterY(),
                                ringRadius * r.width() / 2f, dir);
                        mRings.op(mPath, Path.Op.UNION);
                        mPath.reset();
                        mPath.addCircle(r.exactCenterX(), r.exactCenterY(),
                                holeRadius * r.width() / 2f, dir);
                        mHoles.op(mPath, Path.Op.UNION);
                        mPath.reset();
                        mPath.addCircle(r.exactCenterX(), r.exactCenterY(),
                                backgroundRadius * r.width() / 2f, dir);
                        mBackground.op(mPath, Path.Op.UNION);
                    });
            mPath.set(mRings);
        }

        // If not ambient, actually draw our complication mRings.
        if (!mWatchFaceState.isAmbient()) {
            final Paint backgroundPaint = mWatchFaceState.getPaintBox()
                    .getPaintFromPreset(mWatchFaceState.getBackgroundMaterial());
            final Paint complicationBackgroundPaint = mWatchFaceState.getPaintBox()
                    .getPaintFromPreset(mWatchFaceState.getComplicationBackgroundMaterial());
            final Paint complicationRingPaint = mWatchFaceState.getPaintBox()
                    .getPaintFromPreset(mWatchFaceState.getComplicationRingMaterial());

            // Some optimisations.

            // Paint the complication background, but only if it's different to the watch face
            // background (otherwise why bother?)
            if (!complicationBackgroundPaint.equals(backgroundPaint)) {
                // We do a direct "canvas.drawPath" here to avoid drawing bezels,
                // which are unnecessary as they'd be overdrawn by the rings anyway.
                canvas.drawPath(mBackground, complicationBackgroundPaint);
            }

            // Paint the rings.
            if (complicationBackgroundPaint.equals(complicationRingPaint)) {
                // The complication background and ring paints are the same.
                // Just draw the rings, but skip the holes.
                if (!complicationBackgroundPaint.equals(backgroundPaint)) {
                    drawPath(canvas, mPath, complicationRingPaint);
                }
                // If the complication background, complication ring, and watch face background
                // paints are all the same then don't do anything!
            } else {
                // The complication background and complication ring paints are different.
                // Draw the rings and the holes.
                mPath.op(mHoles, Path.Op.DIFFERENCE);
                drawPath(canvas, mPath, complicationRingPaint);
            }
        }

        // Add the mRings to our exclusion path.
        addExclusionPath(mRings, Path.Op.DIFFERENCE);
    }
}
