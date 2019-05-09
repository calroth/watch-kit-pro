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
    private Path p = new Path();
    private Path rings = new Path();
    private Path holes = new Path();

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
        Collection<ComplicationHolder> complications = mWatchFaceState.getComplications();

        if (complications == null || complications.size() == 0) {
            // Early finish if we don't actually have any complications.
            return;
        }

        rings.reset();
        holes.reset();

        // Calculate our rings and holes!
        complications.stream().filter(c -> c.isForeground && (c.isActive || mDrawAllRings))
                .forEach(c -> {
                    Rect r = c.getBounds();
                    rings.addCircle(r.exactCenterX(), r.exactCenterY(),
                            1.05f * r.width() / 2f, getDirection());
                    holes.addCircle(r.exactCenterX(), r.exactCenterY(),
                            1.01f * r.width() / 2f, getDirection());
                });

        // If not ambient, actually draw our complication rings.
        if (!mWatchFaceState.isAmbient()) {
            // Draw with our four tick style.
            // Maybe in the future, allow this to be customised.
            Paint paint = mWatchFaceState.getPaintBox().getPaintFromPreset(
                    mWatchFaceState.getWatchFacePreset().getFourTickStyle());

            p.reset();
            p.op(rings, Path.Op.UNION);
            p.op(holes, Path.Op.DIFFERENCE);
            drawPath(canvas, p, paint);
        }

        // Add the rings to our exclusion path.
        addExclusionPath(rings, Path.Op.DIFFERENCE);
    }
}
