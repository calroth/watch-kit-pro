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

package pro.watchkit.wearable.watchface.model;

import android.location.Location;

import androidx.annotation.Nullable;

import net.e175.klaus.solarpositioning.DeltaT;
import net.e175.klaus.solarpositioning.SPA;

import java.util.GregorianCalendar;

public final class LocationCalculator {
    private final GregorianCalendar mCalendar;
    @Nullable
    private Location mLocation = null;
    private long previousSunAltitudeTime = 0;
    private double previousSunAltitude = 90d;

    LocationCalculator(GregorianCalendar mCalendar) {
        this.mCalendar = mCalendar;
    }

    public void setLocation(@Nullable Location location) {
        // Only set location if not null (and has changed).
        // If null, preserve previous location.
        if (location != null && !location.equals(mLocation)) {
            mLocation = location;
            // Invalidate previous sun altitude.
            previousSunAltitudeTime = 0;
        }
    }

    /**
     * Get the elevation of the sun, given the current location and date/time.
     * If location is unknown, return 90.
     *
     * @return Elevation of the sun, between 90 and -90 degrees.
     */
    public double getSunAltitude() {
        if (mLocation == null) {
            // No location. Assume sun is directly overhead!
            return 90d;
        }

        // Regenerate every 900 ms. Calls prior to this return cached value.
        boolean regenerateSunAltitude =
                mCalendar.getTimeInMillis() > previousSunAltitudeTime + 900;

        if (regenerateSunAltitude) {
            previousSunAltitude = 90d - SPA.calculateSolarPosition(
                    mCalendar.toZonedDateTime(),
                    mLocation.getLatitude(),
                    mLocation.getLongitude(),
                    mLocation.getAltitude(),
                    DeltaT.estimate(mCalendar.toZonedDateTime().toLocalDate()),
                    1000,
                    20).zenithAngle();
            previousSunAltitudeTime = mCalendar.getTimeInMillis();
        }

        return previousSunAltitude;
    }

    /**
     * Get the ambient night tint multiplier, between 0.0d and 1.0d.
     * 0.0 means no tint. 1.0 means maximum tint.
     *
     * @return Ambient night tint multiplier
     */
    double getDuskDawnMultiplier() {
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
}
