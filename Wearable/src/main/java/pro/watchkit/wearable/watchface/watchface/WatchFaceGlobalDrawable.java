/*
 * Copyright (C) 2019 Terence Tan
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
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;

import java.util.TimeZone;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import pro.watchkit.wearable.watchface.model.WatchFaceState;

/**
 * A very basic Drawable that you feed a WatchFacePreset and a PaintBox and it
 * draws a watch face!
 */
public class WatchFaceGlobalDrawable extends Drawable {
    private WatchPartDrawable[] mWatchPartDrawables;
    private WatchFaceState mWatchFaceState = new WatchFaceState();

    public WatchFaceGlobalDrawable(WatchPartDrawable[] watchPartDrawables) {
        mWatchPartDrawables = watchPartDrawables;
        for (WatchPartDrawable d : mWatchPartDrawables) {
            d.setState(mWatchFaceState);
        }
    }

    public WatchFaceGlobalDrawable() {
        mWatchPartDrawables = new WatchPartDrawable[]{
                new WatchPartBackgroundDrawable(),
                new WatchPartTicksRingsDrawable(),
                new WatchPartHandsDrawable()};

        for (WatchPartDrawable d : mWatchPartDrawables) {
            d.setState(mWatchFaceState);
        }
    }

    public WatchFaceState getWatchFaceState() {
        return mWatchFaceState;
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        // Set the current date and time.
        mWatchFaceState.mCalendar.setTimeZone(TimeZone.getDefault());
        mWatchFaceState.mCalendar.setTimeInMillis(System.currentTimeMillis());
        for (WatchPartDrawable d : mWatchPartDrawables) {
            // For each of our drawables: draw it!
            d.draw(canvas);
        }
    }

    @Override
    public void setAlpha(int alpha) {
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
    }

    @Override
    public int getOpacity() {
        return PixelFormat.OPAQUE;
    }
}