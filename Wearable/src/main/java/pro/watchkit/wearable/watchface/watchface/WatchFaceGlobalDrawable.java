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

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import java.util.TimeZone;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import pro.watchkit.wearable.watchface.model.ComplicationHolder;
import pro.watchkit.wearable.watchface.model.WatchFacePreset;
import pro.watchkit.wearable.watchface.model.WatchFaceState;

/**
 * A very basic Drawable that you feed a WatchFacePreset and a PaintBox and it
 * draws a watch face!
 */
public class WatchFaceGlobalDrawable extends Drawable {
    private static final int FOREGROUND_COMPLICATION_COUNT = 6;

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

    private final int COMPLICATION_AMBIENT_WHITE =
            Color.argb(0xff, 0xff, 0xff, 0xff);
    private final int COMPLICATION_AMBIENT_GREY =
            Color.argb(0xff, 0xaa, 0xaa, 0xaa);
    private int currentComplicationWhite, currentComplicationGrey;

    public void onAmbientModeChanged(boolean inAmbientMode) {
        mWatchFaceState.ambient = inAmbientMode;

        // Update drawable complications' ambient state.
        // Note: ComplicationDrawable handles switching between active/ambient colors, we just
        // have to inform it to enter ambient mode.
        for (ComplicationHolder complication : mWatchFaceState.complications) {
            complication.setAmbientMode(inAmbientMode);
        }
    }

    /**
     * Initialize our complications. Returns an array of complication IDs
     *
     * @param context Current application context
     * @return Array of complication IDs
     */
    public int[] initializeComplications(Context context, ComplicationHolder.InvalidateCallback invalidateCallback) {
        // Creates a ComplicationDrawable for each location where the user can render a
        // complication on the watch face. In this watch face, we create one for left, right,
        // and background, but you could add many more.
        ComplicationHolder.resetBaseId();

        mWatchFaceState.complications.clear();
        {
            final ComplicationHolder b = new ComplicationHolder(context);
            b.isForeground = false;
            b.isActive = false;
            b.setDrawableCallback(invalidateCallback);
            mWatchFaceState.complications.add(b);
        }

        for (int i = 0; i < FOREGROUND_COMPLICATION_COUNT; i++) {
            final ComplicationHolder f = new ComplicationHolder(context);
            f.isForeground = true;
            f.setDrawableCallback(invalidateCallback);
            mWatchFaceState.complications.add(f);
        }

        // Adds new complications to a SparseArray to simplify setting styles and ambient
        // properties for all complications, i.e., iterate over them all.
        setComplicationsActiveAndAmbientColors();

        int[] complicationIds = new int[mWatchFaceState.complications.size()];
        int i = 0;
        for (ComplicationHolder complication : mWatchFaceState.complications) {
            complicationIds[i] = complication.getId();
            i++;
        }

        return complicationIds;
    }

    void onSurfaceChanged(int width, int height) {
        /*
         * Calculates location bounds for right and left circular complications. Please note,
         * we are not demonstrating a long text complication in this watch face.
         *
         * We suggest using at least 1/4 of the screen width for circular (or squared)
         * complications and 2/3 of the screen width for wide rectangular complications for
         * better readability.
         */

        // For most Wear devices, width and height are the same, so we just chose one (width).
        int sizeOfComplication = width / 4;
        int midpointOfScreen = width / 2;

        int i = 0;
        for (ComplicationHolder complication : mWatchFaceState.complications) {
            if (complication.isForeground) {
                // Foreground
                float degrees = (float) ((i + 0.5f) * Math.PI * 2 / FOREGROUND_COMPLICATION_COUNT);

                float halfSize = sizeOfComplication / 2f;
                float offset = midpointOfScreen / 2f;

                float innerX = midpointOfScreen + (float) Math.sin(degrees) * offset;
                float innerY = midpointOfScreen - (float) Math.cos(degrees) * offset;

                Rect bounds =
                        // Left, Top, Right, Bottom
                        new Rect((int) (innerX - halfSize),
                                (int) (innerY - halfSize),
                                (int) (innerX + halfSize),
                                (int) (innerY + halfSize));

                complication.setBounds(bounds);
                i++;
            } else {
                // Background
                Rect screenForBackgroundBound =
                        // Left, Top, Right, Bottom
                        new Rect(0, 0, width, height);
                complication.setBounds(screenForBackgroundBound);
            }
        }
    }

    /* Sets active/ambient mode colors for all complications to WatchFacePreset.ColorType.HIGHLIGHT
     *
     * Note: With the rest of the watch face, we update the paint colors based on
     * ambient/active mode callbacks, but because the ComplicationDrawable handles
     * the active/ambient colors, we only set the colors twice. Once at initialization and
     * again if the user changes the highlight color via AnalogComplicationConfigActivity.
     */
    void setComplicationsActiveAndAmbientColors() {
        setComplicationsActiveAndAmbientColors(
                mWatchFaceState.paintBox.getColor(WatchFacePreset.ColorType.HIGHLIGHT));
    }

    /* Sets active/ambient mode colors for all complications.
     *
     * Note: With the rest of the watch face, we update the paint colors based on
     * ambient/active mode callbacks, but because the ComplicationDrawable handles
     * the active/ambient colors, we only set the colors twice. Once at initialization and
     * again if the user changes the highlight color via AnalogComplicationConfigActivity.
     */
    private void setComplicationsActiveAndAmbientColors(int primaryComplicationColor) {
        for (ComplicationHolder complication : mWatchFaceState.complications) {
            complication.setColors(primaryComplicationColor);
        }
    }

    /**
     * Run this method if we're in ambient mode. Re-calculate our complication colors in ambient
     * mode and update them if they've changed.
     */
    void preDrawAmbientCheck() {
        int newComplicationWhite = mWatchFaceState.mLocationCalculator.getDuskDawnColor(COMPLICATION_AMBIENT_WHITE);
        int newComplicationGrey = mWatchFaceState.mLocationCalculator.getDuskDawnColor(COMPLICATION_AMBIENT_GREY);

        if (currentComplicationWhite != newComplicationWhite
                || currentComplicationGrey != newComplicationGrey) {
            for (ComplicationHolder complication : mWatchFaceState.complications) {
                complication.setAmbientColors(
                        newComplicationWhite, newComplicationGrey, newComplicationGrey);
            }

            // Why go to the trouble of tracking current and new complication colors,
            // and only updating when it's changed?

            // Optimisation. We assume that setting colors on ComplicationDrawable is a
            // heinously slow operation (it probably isn't though) and so we avoid it...

            currentComplicationWhite = newComplicationWhite;
            currentComplicationGrey = newComplicationGrey;
        }
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