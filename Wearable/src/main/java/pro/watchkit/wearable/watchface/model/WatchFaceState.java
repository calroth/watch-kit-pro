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

package pro.watchkit.wearable.watchface.model;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.support.wearable.complications.ComplicationData;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class WatchFaceState {
    private WatchFacePreset mWatchFacePreset = new WatchFacePreset();
    public PaintBox paintBox;
    public Collection<ComplicationHolder> complications = new ArrayList<>();
    public int unreadNotifications;
    public int totalNotifications;
    public boolean ambient;
    private GregorianCalendar mCalendar = new GregorianCalendar();
    private LocationCalculator mLocationCalculator = new LocationCalculator(mCalendar);

    private static final int FOREGROUND_COMPLICATION_COUNT = 6;
    private static final int COMPLICATION_AMBIENT_WHITE =
            Color.argb(0xff, 0xff, 0xff, 0xff);
    private static final int COMPLICATION_AMBIENT_GREY =
            Color.argb(0xff, 0xaa, 0xaa, 0xaa);

    private int currentComplicationWhite, currentComplicationGrey;

    public WatchFaceState(Context context) {
        paintBox = new PaintBox(context, mWatchFacePreset);
    }

    /**
     * Get the current time as milliseconds from the calendar's time.
     *
     * @return Current number of milliseconds
     */
    public long getTimeInMillis() {
        return mCalendar.getTimeInMillis();
    }

    /**
     * Get the current number of seconds from the calendar's time, including the fractional part.
     *
     * @return Current number of seconds including fractional
     */
    public float getSecondsDecimal() {
        return (mCalendar.get(Calendar.SECOND) + mCalendar.get(Calendar.MILLISECOND) / 1000f);
    }

    /**
     * Get the current number of minutes from the calendar's time.
     *
     * @return Current number of minutes
     */
    public int getMinutes() {
        return mCalendar.get(Calendar.MINUTE);
    }

    /**
     * Get the current number of hours from the calendar's time.
     *
     * @return Current number of hours
     */
    public int getHours() {
        return mCalendar.get(Calendar.HOUR);
    }

    /**
     * Set the calendar's time zone to default. Call this if you suspect that the time zone may
     * have changed, daylight savings time has started or ended, etc.
     */
    public void setDefaultTimeZone() {
        mCalendar.setTimeZone(TimeZone.getDefault());
    }

    /**
     * Set the calendar's current time to now. An excellent thing to do before showing or
     * calculating the time.
     */
    public void setCurrentTimeToNow() {
        mCalendar.setTimeInMillis(System.currentTimeMillis());
    }

    public void onAmbientModeChanged(boolean inAmbientMode) {
        ambient = inAmbientMode;

        // Update drawable complications' ambient state.
        // Note: ComplicationDrawable handles switching between active/ambient colors, we just
        // have to inform it to enter ambient mode.
        for (ComplicationHolder complication : complications) {
            complication.setAmbientMode(inAmbientMode);
        }
    }

    public void onComplicationDataUpdate(
            int complicationId, ComplicationData complicationData) {
        // Updates correct ComplicationDrawable with updated data.
        for (ComplicationHolder complication : complications) {
            if (complication.getId() == complicationId) {
                switch (complicationData.getType()) {
                    case ComplicationData.TYPE_EMPTY:
                    case ComplicationData.TYPE_NO_DATA:
                    case ComplicationData.TYPE_NOT_CONFIGURED:
                    case ComplicationData.TYPE_NO_PERMISSION:
                        complication.isActive = false;
                        break;
                    default:
                        complication.isActive = true;
                }
                complication.setComplicationData(complicationData);
            }
        }
    }

    public boolean onComplicationTap(int x, int y) {
        // Try all foreground complications first, before background complications.
        for (ComplicationHolder complication : complications) {
            if (complication.isForeground) {
                boolean successfulTap = complication.onDrawableTap(x, y);

                if (successfulTap) {
                    return true;
                }
            }
        }
        // Try all background complications.
        for (ComplicationHolder complication : complications) {
            if (!complication.isForeground) {
                boolean successfulTap = complication.onDrawableTap(x, y);

                if (successfulTap) {
                    return true;
                }
            }
        }

        return false;
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

        complications.clear();
        {
            final ComplicationHolder b = new ComplicationHolder(context);
            b.isForeground = false;
            b.isActive = false;
            b.setDrawableCallback(invalidateCallback);
            complications.add(b);
        }

        for (int i = 0; i < FOREGROUND_COMPLICATION_COUNT; i++) {
            final ComplicationHolder f = new ComplicationHolder(context);
            f.isForeground = true;
            f.setDrawableCallback(invalidateCallback);
            complications.add(f);
        }

        // Adds new complications to a SparseArray to simplify setting styles and ambient
        // properties for all complications, i.e., iterate over them all.
        setComplicationsActiveAndAmbientColors();

        int[] complicationIds = new int[complications.size()];
        int i = 0;
        for (ComplicationHolder complication : complications) {
            complicationIds[i] = complication.getId();
            i++;
        }

        return complicationIds;
    }

    public void onSurfaceChanged(int width, int height) {
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
        for (ComplicationHolder complication : complications) {
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
    public void setComplicationsActiveAndAmbientColors() {
        setComplicationsActiveAndAmbientColors(
                paintBox.getColor(WatchFacePreset.ColorType.HIGHLIGHT));
    }

    /* Sets active/ambient mode colors for all complications.
     *
     * Note: With the rest of the watch face, we update the paint colors based on
     * ambient/active mode callbacks, but because the ComplicationDrawable handles
     * the active/ambient colors, we only set the colors twice. Once at initialization and
     * again if the user changes the highlight color via AnalogComplicationConfigActivity.
     */
    private void setComplicationsActiveAndAmbientColors(int primaryComplicationColor) {
        for (ComplicationHolder complication : complications) {
            complication.setColors(primaryComplicationColor);
        }
    }

    /**
     * Run this method if we're in ambient mode. Re-calculate our complication colors in ambient
     * mode and update them if they've changed.
     */
    public void preDrawAmbientCheck() {
        int newComplicationWhite = mLocationCalculator.getDuskDawnColor(COMPLICATION_AMBIENT_WHITE);
        int newComplicationGrey = mLocationCalculator.getDuskDawnColor(COMPLICATION_AMBIENT_GREY);

        if (currentComplicationWhite != newComplicationWhite
                || currentComplicationGrey != newComplicationGrey) {
            for (ComplicationHolder complication : complications) {
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

    public LocationCalculator getLocationCalculator() {
        return mLocationCalculator;
    }

    public WatchFacePreset getWatchFacePreset() {
        return mWatchFacePreset;
    }
}
