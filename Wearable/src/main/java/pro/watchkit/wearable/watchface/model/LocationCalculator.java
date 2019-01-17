/*
 * Copyright (C) 2018 Terence Tan
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

import android.graphics.Color;
import android.location.Location;

import net.e175.klaus.solarpositioning.DeltaT;
import net.e175.klaus.solarpositioning.SPA;

import java.util.GregorianCalendar;

public final class LocationCalculator {
    private static final int AMBIENT_WHITE_R =
            Color.argb(0xff, 0x99, 0x00, 0x00);

    private GregorianCalendar mCalendar;

    public LocationCalculator(GregorianCalendar mCalendar) {
        this.mCalendar = mCalendar;
    }

    private Location mLocation = null;

    public void setLocation(Location location) {
        // Only set location if not null (and has changed).
        // If null, preserve previous location.
        if (location != null && !location.equals(mLocation)) {
            mLocation = location;
            // Invalidate previous sun altitude.
            previousSunAltitudeTime = 0;
        }
    }

    private long previousSunAltitudeTime = 0;

    private double previousSunAltitude = 90d;

    /**
     * Get the elevation of the sun, given the current location and date/time.
     * If location is unknown, return 90.
     * @return Elevation of the sun, between 90 and -90 degrees.
     */
    public double getSunAltitude() {
        if (mLocation == null) {
            // No location. Assume sun is directly overhead!
            return 90d;
        }

        boolean regenerateSunAltitude = false;

        // Regenerate every 900 ms. Calls prior to this return cached value.
        if (mCalendar.getTimeInMillis() > previousSunAltitudeTime + 900) {
            regenerateSunAltitude = true;
        }

        if (regenerateSunAltitude) {
            previousSunAltitude = 90d - SPA.calculateSolarPosition(
                    mCalendar,
                    mLocation.getLatitude(),
                    mLocation.getLongitude(),
                    mLocation.getAltitude(),
                    DeltaT.estimate(mCalendar),
                    1000,
                    20).getZenithAngle();
            previousSunAltitudeTime = mCalendar.getTimeInMillis();
        }

//        Log.d("AnalogWatchFace", "getSunAltitude: cache = "
//                + (regenerateSunAltitude ? "true" : "false")
//                + ", value = " + previousSunAltitude);

        return previousSunAltitude;
    }

    /**
     * Get the night vision tint multiplier, between 0.0d and 1.0d.
     * 0.0 means no tint. 1.0 means maximum tint.
     * @return Night vision tint multiplier
     */
    private double getDuskDawnMultiplier() {
        double altitude = getSunAltitude();
        if (altitude < -12d) {
            // Night
            return 1d;
        } else if (altitude < 0d) {
            // Dawn
            return altitude / (-12d);
        } else {
            // Day
            return 0d;
        }
    }

    public int getDuskDawnColor(int original) {
        return Palette.getIntermediateColor(AMBIENT_WHITE_R, original, getDuskDawnMultiplier());

//        double d = getDuskDawnMultiplier();
//        double e = 1d - d;
//        int a = (int) (Color.alpha(AMBIENT_WHITE_R) * d + Color.alpha(original) * e);
//        int r = (int) (Color.red(AMBIENT_WHITE_R) * d + Color.red(original) * e);
//        int g = (int) (Color.green(AMBIENT_WHITE_R) * d + Color.green(original) * e);
//        int b = (int) (Color.blue(AMBIENT_WHITE_R) * d + Color.blue(original) * e);
//        return Color.argb(a, r, g, b);
    }
}