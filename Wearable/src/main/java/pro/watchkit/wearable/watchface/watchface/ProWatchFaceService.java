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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.wearable.complications.ComplicationData;
import android.support.wearable.watchface.WatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.WindowInsets;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.lang.ref.WeakReference;
import java.util.concurrent.TimeUnit;

import pro.watchkit.wearable.watchface.model.ComplicationHolder;
import pro.watchkit.wearable.watchface.model.WatchFaceState;
import pro.watchkit.wearable.watchface.util.SharedPref;

public abstract class ProWatchFaceService extends HardwareAcceleratedCanvasWatchFaceService {
    //public class AnalogComplicationWatchFaceService extends WatchFaceService {
    private static final String TAG = "AnalogWatchFace";

    // Unique IDs for each complication. The settings activity that supports allowing users
    // to select their complication data provider requires numbers to be >= 0.
//    private static final int BACKGROUND_COMPLICATION_ID = 99;

    /*
     * Update rate in milliseconds for interactive mode. We update once a second to advance the
     * second hand.
     */
    private static final long INTERACTIVE_UPDATE_RATE_MS = TimeUnit.SECONDS.toMillis(1);

    @NonNull
    @Override
    public Engine onCreateEngine() {
        return new Engine();
    }

    private static class UpdateTimeHandler extends Handler {
        @NonNull
        private final WeakReference<ProWatchFaceService.Engine> mWeakReference;

        UpdateTimeHandler(ProWatchFaceService.Engine reference) {
            mWeakReference = new WeakReference<>(reference);
        }

        @Override
        public void handleMessage(Message msg) {
            ProWatchFaceService.Engine engine = mWeakReference.get();
            if (engine != null) {
                engine.updateTimeViaHandler();
            }
        }
    }

    private class Engine extends HardwareAcceleratedCanvasWatchFaceService.Engine
            implements ComplicationHolder.InvalidateCallback {

        private static final int MSG_UPDATE_TIME = 0;

        /**
         * Handler to update the time once a second in interactive mode.
         */
        private final Handler mUpdateTimeHandler = new UpdateTimeHandler(this);

        private final BroadcastReceiver mTimeZoneReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                getWatchFaceState().setDefaultTimeZone();
                WatchPartStatsDrawable.mInvalidTrigger = WatchPartStatsDrawable.INVALID_TIMEZONE;
                invalidate();
            }
        };
        private final IntentFilter mActionTimezoneChangedIntentFilter =
                new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED);
        private WatchFaceGlobalDrawable mWatchFaceGlobalDrawable;

        /**
         * SharedPreferences object to retrieve our WatchFaceState out of preferences.
         */
        private SharedPref mSharedPref;

        private boolean mRegisteredTimeZoneReceiver = false;
        private boolean mMuteMode;

        @Override
        protected void beforeDoFrame(int invalidated) {
            if (WatchPartStatsDrawable.invalid < invalidated) {
                // Set painter invalid display to the maximum we've seen for a while
                WatchPartStatsDrawable.invalid = invalidated;
            }
        }

        @Override
        protected void afterDoFrame(int invalidated) {
            if (invalidated == 0) {
                WatchPartStatsDrawable.invalid = 0;
            }
        }

        private void updateTimeViaHandler() {
            WatchPartStatsDrawable.mInvalidTrigger = WatchPartStatsDrawable.INVALID_TIMER_HANDLER;
            invalidate();
            if (shouldTimerBeRunning()) {
                long timeMs = System.currentTimeMillis();
                long delayMs = INTERACTIVE_UPDATE_RATE_MS - (timeMs % INTERACTIVE_UPDATE_RATE_MS);
                mUpdateTimeHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIME, delayMs);
            }
        }

        /**
         * Gets the current watch face state. Convenience method for
         * "mWatchFaceGlobalDrawable.getWatchFaceState()".
         *
         * @return Current watch face state
         */
        @NonNull
        private WatchFaceState getWatchFaceState() {
            return mWatchFaceGlobalDrawable.getWatchFaceState();
        }

        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);

            // Used throughout watch face to pull user's preferences.
            Context context = getApplicationContext();
            mSharedPref = new SharedPref(context, ProWatchFaceService.this.getClass());

            setWatchFaceStyle(
                    new WatchFaceStyle.Builder(ProWatchFaceService.this)
                            .setAcceptsTapEvents(true)
                            .setHideNotificationIndicator(true)
                            .setViewProtectionMode(WatchFaceStyle.PROTECT_STATUS_BAR)
                            .build());

            mWatchFaceGlobalDrawable = new WatchFaceGlobalDrawable(context,
                    new WatchFaceGlobalCacheDrawable(
                            WatchFaceGlobalDrawable.PART_BACKGROUND_FULL_CANVAS |
                                    WatchFaceGlobalDrawable.PART_NOTIFICATIONS |
                                    WatchFaceGlobalDrawable.PART_RINGS_ACTIVE |
                                    WatchFaceGlobalDrawable.PART_TICKS),
                    WatchFaceGlobalDrawable.PART_COMPLICATIONS |
                            WatchFaceGlobalDrawable.PART_HANDS |
                            WatchFaceGlobalDrawable.PART_STATS);

            loadSavedPreferences();
            setHardwareAccelerationEnabled(getWatchFaceState().isHardwareAccelerationEnabled());

            // Initialise complications
            setActiveComplications(getWatchFaceState().initializeComplications(context, this));

            final FusedLocationProviderClient locationClient =
                    LocationServices.getFusedLocationProviderClient(context);

            // Set the default location to Terence's city.
            // Remove for release...
//            locationClient.setMockMode(true);
            Location mockLocation = new Location("?");
            mockLocation.setLatitude(-35d);
            mockLocation.setLongitude(147d);
            mockLocation.setAltitude(600d);
            mockLocation.setAccuracy(100f);
            mockLocation.setTime(System.currentTimeMillis());
            mockLocation.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
            updateLocation(mockLocation);
//            locationClient.setMockLocation(mockLocation);

            if (context.checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED) {
                LocationRequest req = new LocationRequest();
                req.setInterval(1000 * 60 * 15);
                req.setFastestInterval(1000 * 60);
                req.setPriority(LocationRequest.PRIORITY_LOW_POWER);

                // Get the last location right away.
                locationClient.getLastLocation().addOnCompleteListener(
                        task -> updateLocation(task.getResult())
                );

                // Sign up for ongoing location reports.
                locationClient.requestLocationUpdates(req, new LocationCallback() {
                            @Override
                            public void onLocationResult(@Nullable LocationResult locationResult) {
                                if (locationResult == null) {
                                    Log.d(TAG, "onLocationResult: no locations (it's null)");
                                } else {
                                    updateLocation(locationResult.getLastLocation());
                                }
                            }
                        },
                        null).addOnCompleteListener(
                        task -> {
                            if (task.isSuccessful()) {
                                // Task completed successfully
                                Log.d(TAG, "requestLocationUpdates onComplete isSuccessful: " + task.getResult());
                            } else {
                                // Task failed with an exception
                                Log.d(TAG, "requestLocationUpdates onComplete exception: ", task.getException());
                            }
                        });
            }
        }

        private void updateLocation(@Nullable Location location) {
            // Update UI with location data
            // ...
            // Note: can be null in rare situations; handle accordingly.
            Log.w(TAG, "onLocationResult: Got last location! It's " +
                    (location == null ? "null..." : location.getLatitude() + " / " +
                            location.getLongitude() + " / " + location.getAltitude()));
            if (location != null) {
                getWatchFaceState().getLocationCalculator().setLocation(location);
                WatchPartStatsDrawable.mInvalidTrigger = WatchPartStatsDrawable.INVALID_LOCATION;
                postInvalidate();
            }
        }

        // Pulls all user's preferences for watch face appearance.
        private void loadSavedPreferences() {
            getWatchFaceState().setString(mSharedPref.getWatchFaceStateString());
        }

        @Override
        public void onApplyWindowInsets(@NonNull WindowInsets insets) {
            super.onApplyWindowInsets(insets);
            SharedPref.setIsRoundScreen(insets.isRound());
            // cutoutSize = insets.getSystemWindowInsetBottom();
        }

        @Override
        public void onPropertiesChanged(@NonNull Bundle properties) {
            super.onPropertiesChanged(properties);

            // The properties that matter to us.
            boolean lowBitAmbient = properties.getBoolean(PROPERTY_LOW_BIT_AMBIENT, false);
            boolean burnInProtection = properties.getBoolean(PROPERTY_BURN_IN_PROTECTION, false);

            // Set low-bit ambient on our ambient watch face paint as required.
            getWatchFaceState().getPaintBox().getAmbientPaint().setAntiAlias(!lowBitAmbient);

            // Set low-bit ambient and burn-in protection on our complications as required.
            getWatchFaceState().getComplications().forEach(
                    c -> c.setLowBitAmbientBurnInProtection(lowBitAmbient, burnInProtection));
        }

        /*
         * Called when there is updated data for a complication id.
         */
        @Override
        public void onComplicationDataUpdate(
                int complicationId, @NonNull ComplicationData complicationData) {

            // Adds/updates active complication data in the array.
            getWatchFaceState().onComplicationDataUpdate(complicationId, complicationData);

            WatchPartStatsDrawable.mInvalidTrigger = WatchPartStatsDrawable.INVALID_COMPLICATION;
            invalidate();
        }

        @Override
        public void onTapCommand(int tapType, int x, int y, long eventTime) {
            boolean handled = false;
            if (tapType == TAP_TYPE_TAP) {
                handled = getWatchFaceState().onComplicationTap(x, y);
            }
//            switch (tapType) {
//                case TAP_TYPE_TAP:
//                    if (getWatchFaceState().onComplicationTap(x, y)) {
//                        return;
//                    }
//                    break;
//            }
            if (!handled) {
                super.onTapCommand(tapType, x, y, eventTime);
            }
        }

        @Override
        public void onTimeTick() {
            super.onTimeTick();
            WatchPartStatsDrawable.mInvalidTrigger = WatchPartStatsDrawable.INVALID_TIME_TICK;
            invalidate();
        }

        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);

            getWatchFaceState().onAmbientModeChanged(inAmbientMode);

            // Check and trigger whether or not timer should be running (only in active mode).
            updateTimer();

            WatchPartStatsDrawable.mInvalidTrigger = WatchPartStatsDrawable.INVALID_AMBIENT;
            invalidate();
        }

        @Override
        public void onInterruptionFilterChanged(int interruptionFilter) {
            super.onInterruptionFilterChanged(interruptionFilter);
            boolean inMuteMode = (interruptionFilter == WatchFaceService.INTERRUPTION_FILTER_NONE);

            /* Dim display in mute mode. */
            if (mMuteMode != inMuteMode) {
                mMuteMode = inMuteMode;
                //mHourPaint.setAlpha(inMuteMode ? 100 : 255);
                //mMinutePaint.setAlpha(inMuteMode ? 100 : 255);
                //mSecondAndHighlightPaint.setAlpha(inMuteMode ? 80 : 255);
                WatchPartStatsDrawable.mInvalidTrigger = WatchPartStatsDrawable.INVALID_INTERRUPTION;
                invalidate();
            }
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);

            /*
             * Calculates location bounds for right and left circular complications. Please note,
             * we are not demonstrating a long text complication in this watch face.
             *
             * We suggest using at least 1/4 of the screen width for circular (or squared)
             * complications and 2/3 of the screen width for wide rectangular complications for
             * better readability.
             */

            WatchPartStatsDrawable.mInvalidTrigger = WatchPartStatsDrawable.INVALID_SURFACE;
            invalidate();
        }

        @Override
        public void onDraw(@NonNull Canvas canvas, @NonNull Rect bounds) {
            boolean prevAmbient = getWatchFaceState().isAmbient();
            super.onDraw(canvas, bounds);

            int unreadNotifications = getWatchFaceState().isShowUnreadNotifications() ? getUnreadCount() : 0;
            int totalNotifications = getWatchFaceState().isShowUnreadNotifications() ? getNotificationCount() : 0;

            // Draw all our drawables.
            // First set all our state objects.
            getWatchFaceState().setCurrentTimeToNow();
            getWatchFaceState().setNotifications(unreadNotifications, totalNotifications);

            // Propagate our size to our drawable. Turns out this isn't as slow as I imagined.
            mWatchFaceGlobalDrawable.setBounds(bounds);
            mWatchFaceGlobalDrawable.draw(canvas);

            if (prevAmbient != getWatchFaceState().isAmbient()) {
                WatchPartStatsDrawable.mInvalidTrigger = WatchPartStatsDrawable.INVALID_WTF;
                invalidate();
            }
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);

            if (visible) {
                // Preferences might have changed since last time watch face was visible.
                loadSavedPreferences();
                setHardwareAccelerationEnabled(getWatchFaceState().isHardwareAccelerationEnabled());

                // With the rest of the watch face, we update the paint colors based on
                // ambient/active mode callbacks, but because the ComplicationDrawable handles
                // the active/ambient colors, we only need to update the complications' colors when
                // the user actually makes a change to the highlight color, not when the watch goes
                // in and out of ambient mode.
                getWatchFaceState().setComplicationColors();

                // Register the time zone receiver.
                if (!mRegisteredTimeZoneReceiver) {
                    mRegisteredTimeZoneReceiver = true;
                    registerReceiver(mTimeZoneReceiver, mActionTimezoneChangedIntentFilter);
                }

                // Update time zone in case it changed while we weren't visible.
                getWatchFaceState().setDefaultTimeZone();
                WatchPartStatsDrawable.mInvalidTrigger = WatchPartStatsDrawable.INVALID_TIMEZONE;
                invalidate();
            } else if (mRegisteredTimeZoneReceiver) {
                // Unregister the time zone receiver.
                mRegisteredTimeZoneReceiver = false;
                unregisterReceiver(mTimeZoneReceiver);
            }

            /* Check and trigger whether or not timer should be running (only in active mode). */
            updateTimer();
        }

        @Override
        public void onNotificationCountChanged(int count) {
            if (getWatchFaceState().isShowUnreadNotifications()) {
                WatchPartStatsDrawable.mInvalidTrigger = WatchPartStatsDrawable.INVALID_NOTIFICATION;
                invalidate();
            }
        }

        @Override
        public void onUnreadCountChanged(int count) {
            if (getWatchFaceState().isShowUnreadNotifications()) {
                WatchPartStatsDrawable.mInvalidTrigger = WatchPartStatsDrawable.INVALID_NOTIFICATION;
                invalidate();
            }
        }

        /**
         * Starts/stops the {@link #mUpdateTimeHandler} timer based on the state of the watch face.
         */
        private void updateTimer() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            if (shouldTimerBeRunning()) {
                mUpdateTimeHandler.sendEmptyMessage(MSG_UPDATE_TIME);
            }
        }

        /**
         * Returns whether the {@link #mUpdateTimeHandler} timer should be running. The timer should
         * only run in active mode.
         */
        private boolean shouldTimerBeRunning() {
            return isVisible() && !isInAmbientMode();
        }
    }

    public static final class A extends ProWatchFaceService {
    }

    public static final class B extends ProWatchFaceService {
    }

    public static final class C extends ProWatchFaceService {
    }

    public static final class D extends ProWatchFaceService {
    }
}
