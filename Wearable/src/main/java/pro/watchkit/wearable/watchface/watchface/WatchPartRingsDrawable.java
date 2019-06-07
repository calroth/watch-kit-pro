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
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;

import androidx.annotation.NonNull;

import java.util.Collection;

import pro.watchkit.wearable.watchface.model.ComplicationHolder;

final class WatchPartRingsDrawable extends WatchPartDrawable {
    private Path mPath = new Path();
    private Path mRings = new Path();
    private Path mHoles = new Path();
    private Path mBackground = new Path();

    private boolean mDrawAllRings = false;

    WatchPartRingsDrawable() {
        super();
    }

    WatchPartRingsDrawable(boolean drawAllRings) {
        super();
        mDrawAllRings = drawAllRings;
    }

    @Override
    String getStatsName() {
        return "Rings";
    }

    @Override
    public void draw2(@NonNull Canvas canvas) {
        Collection<ComplicationHolder> complications =
                mWatchFaceState.getComplicationsForDrawing(getBounds());

        if (complications == null || complications.size() == 0) {
            // Early finish if we don't actually have any complications.
            return;
        }

        mRings.reset();
        mHoles.reset();
        mBackground.reset();

        // Calculate our mRings and mHoles!
        complications.stream()
                .filter(c -> c.isForeground && c.getBounds() != null && (c.isActive || mDrawAllRings))
                .forEach(c -> {
                    Rect r = c.getBounds();
                    mRings.addCircle(r.exactCenterX(), r.exactCenterY(),
                            1.05f * r.width() / 2f, getDirection());
                    mHoles.addCircle(r.exactCenterX(), r.exactCenterY(),
                            1.01f * r.width() / 2f, getDirection());
                    mPath.reset(); // Using "mPath" as a temporary variable here...
                    mPath.addCircle(r.exactCenterX(), r.exactCenterY(),
                            1.03f * r.width() / 2f, getDirection());
                    mBackground.op(mPath, Path.Op.UNION);
                });

        // If not ambient, actually draw our complication mRings.
        if (!mWatchFaceState.isAmbient()) {
            Paint watchFacePaint = mWatchFaceState.getPaintBox().getPaintFromPreset(
                    mWatchFaceState.getWatchFacePreset().getBackgroundStyle());
            Paint backgroundPaint = mWatchFaceState.getPaintBox().getPaintFromPreset(
                    mWatchFaceState.getSettings().getComplicationBackgroundStyle());
            Paint ringPaint = mWatchFaceState.getPaintBox().getPaintFromPreset(
                    mWatchFaceState.getSettings().getComplicationRingStyle());

            // Some optimisations.
            if (ringPaint.equals(backgroundPaint) || ringPaint.equals(watchFacePaint)) {
                // If the ring and complication mBackground paints are equal,
                // or the ring and watch face mBackground paints are equal,
                // draw the mRings but skip the mHoles.
                drawPath(canvas, mBackground, backgroundPaint);
            } else {
                // Paint the mBackground if it's different.
                if (!backgroundPaint.equals(watchFacePaint)) {
                    drawPath(canvas, mBackground, backgroundPaint);
                }

                // Paint with our complication ring style.
                mPath.reset();
                mPath.op(mRings, Path.Op.UNION);
                mPath.op(mHoles, Path.Op.DIFFERENCE);
                drawPath(canvas, mPath, ringPaint);
            }
        }

        // Add the mRings to our exclusion path.
        addExclusionPath(mRings, Path.Op.DIFFERENCE);
    }
}
