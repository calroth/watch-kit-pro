/*
 * Copyright (C) 2018-2022 Terence Tan
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

import android.app.AlarmManager;
import android.app.PendingIntent;
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
import android.os.Looper;
import android.os.Message;
import android.support.wearable.complications.ComplicationData;
import android.support.wearable.complications.SystemProviders;
import android.support.wearable.watchface.WatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.support.wearable.watchface.decomposition.WatchFaceDecomposition;
import android.view.SurfaceHolder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.lang.ref.WeakReference;
import java.util.concurrent.TimeUnit;

import pro.watchkit.wearable.watchface.model.WatchFaceState;
import pro.watchkit.wearable.watchface.util.SharedPref;
import pro.watchkit.wearable.watchface.util.Toaster;

public abstract class ProWatchFaceService extends HardwareAcceleratedCanvasWatchFaceService {
//    private static final String TAG = "ProWatchFaceService";

    /*
     * Update rate in milliseconds for interactive mode. We update once a second to advance the
     * second hand.
     */
    private static final long INTERACTIVE_UPDATE_RATE_MS = TimeUnit.SECONDS.toMillis(1);

    /**
     * An array with all default complication providers. Done on a per-slot basis. An array
     * of two-int pairs; the first is the provider, the second is the provider type (or
     * ComplicationData.TYPE_NOT_CONFIGURED if blank). The first two-int pair is for the
     * background complication; the remainder are for the foreground complications.
     *
     * @return Array with all default complication providers.
     */
    @NonNull
    abstract public int[][] getDefaultSystemComplicationProviders();

    @NonNull
    @Override
    public Engine onCreateEngine() {
        return new Engine();
    }

    private static class UpdateTimeHandler extends Handler {
        @NonNull
        private final WeakReference<ProWatchFaceService.Engine> mWeakReference;

        UpdateTimeHandler(@NonNull ProWatchFaceService.Engine reference) {
            super(Looper.getMainLooper());
            mWeakReference = new WeakReference<>(reference);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            ProWatchFaceService.Engine engine = mWeakReference.get();
            if (engine != null) {
                engine.updateTimeViaHandler();
            }
        }
    }

    private class Engine extends HardwareAcceleratedCanvasWatchFaceService.Engine {

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
                            WatchFaceGlobalDrawable.PART_BACKGROUND |
                                    WatchFaceGlobalDrawable.PART_NOTIFICATIONS |
                                    WatchFaceGlobalDrawable.PART_RINGS_ACTIVE |
                                    WatchFaceGlobalDrawable.PART_PIPS),
                    WatchFaceGlobalDrawable.PART_COMPLICATIONS |
                            WatchFaceGlobalDrawable.PART_HANDS |
                            WatchFaceGlobalDrawable.PART_STATS);

            loadSavedPreferences();
            setHardwareAccelerationEnabled(getWatchFaceState().isHardwareAccelerationEnabled());

            // Initialise complications
            int[] complicationIds = getWatchFaceState().initializeComplications(context);
            // Set our active complications
            setActiveComplications(complicationIds);
            // Set our default complications
            int[][] defaultComplicationProviders = getDefaultSystemComplicationProviders();
            for (int i = 0; i < complicationIds.length; i++) {
                // For each active complication, check for a corresponding default complication.
                // If it's there, set the system default complication provider accordingly.
                if (i < defaultComplicationProviders.length) {
                    int[] complicationProvider = defaultComplicationProviders[i];
                    if (complicationProvider.length >= 2 &&
                            complicationProvider[1] != ComplicationData.TYPE_NOT_CONFIGURED) {
                        setDefaultSystemComplicationProvider(complicationIds[i],
                                complicationProvider[0], complicationProvider[1]);
                    }
                }
            }

            setLocationListener();

            mDecomposableUpdatePendingIntent = PendingIntent.getBroadcast(
                    context, 0, new Intent(DECOMPOSABLE_UPDATE_ACTION),
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        }

        /**
         * Schedule the next alarm for our decomposition update. We only have one alarm at a time,
         * so cancels the current alarm if active.
         *
         * @param nextUpdateTime Time to schedule for.
         */
        private void scheduleNextUpdateDecomposableAlarm(long nextUpdateTime) {
            // Cancel if it's set, we'll reset.
            mDecomposableUpdateAlarmManager.cancel(mDecomposableUpdatePendingIntent);
            mDecomposableUpdateAlarmManager.setAlarmClock(
                    new AlarmManager.AlarmClockInfo(
                            nextUpdateTime, mDecomposableUpdatePendingIntent),
                    mDecomposableUpdatePendingIntent);
        }

        /**
         * Has a decomposition been sent to the offload processor?
         */
        private boolean mHasDecompositionBeenSent = false;

        /**
         * Our action for updating our decomposition.
         */
        @NonNull
        private static final String DECOMPOSABLE_UPDATE_ACTION =
                "pro.watchkit.wearable.watchface.action.DECOMPOSABLE_UPDATE";

        /**
         * Our IntentFilter for registering "mDecomposableUpdateBroadcastReceiver".
         */
        @NonNull
        private final IntentFilter mDecomposableUpdateIntentFilter =
                new IntentFilter(DECOMPOSABLE_UPDATE_ACTION);

        /**
         * Our AlarmManager for waking up the device to update the decomposition.
         */
        @NonNull
        private final AlarmManager mDecomposableUpdateAlarmManager =
                (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        /**
         * Our Intent that we raise when we need to update the decomposition.
         */
        @Nullable
        private PendingIntent mDecomposableUpdatePendingIntent;

        /**
         * Our BroadcastReceiver which runs when our Intent fires to update the decomposition.
         */
        @NonNull
        private final BroadcastReceiver mDecomposableUpdateBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                WatchPartStatsDrawable.mInvalidTrigger = WatchPartStatsDrawable.INVALID_ALARM;
                invalidate();
            }
        };

        /**
         * Our location client. Has a value if we have permissions and are currently receiving
         * location updates. Null otherwise.
         */
        @Nullable
        private FusedLocationProviderClient mLocationClient = null;

        /**
         * Our location request, with our parameters for receiving updates.
         */
        @NonNull
        private final LocationRequest mLocationRequest = LocationRequest.create();

        /**
         * Our location callback, the chunk of code that runs when we receive a new location
         * result, and shuffles it to our LocationCalculator.
         */
        @NonNull
        private final LocationCallback mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@Nullable LocationResult locationResult) {
                if (locationResult != null) {
                    updateLocation(locationResult.getLastLocation());
                }
            }
        };

        /**
         * Set our location listener for location updates if we have the permissions,
         * or remove it if we don't have the permissions.
         */
        private void setLocationListener() {
            Context context = getApplicationContext();
            int permission = context.checkSelfPermission(
                    android.Manifest.permission.ACCESS_COARSE_LOCATION);
            if (permission == PackageManager.PERMISSION_GRANTED && mLocationClient == null) {
                // Set up our location request.
                mLocationRequest.setInterval(1000 * 60 * 15); // 15 minutes
                mLocationRequest.setFastestInterval(1000 * 60); // 60 seconds
                mLocationRequest.setPriority(LocationRequest.PRIORITY_LOW_POWER); // "City" level
                mLocationRequest.setSmallestDisplacement(10000f); // 10 kilometres

                // Grab our location client...
                mLocationClient = LocationServices.getFusedLocationProviderClient(context);

                // Get the last location right away.
                mLocationClient.getLastLocation().addOnCompleteListener(
                        task -> updateLocation(task.getResult()));

                // Sign up for ongoing location reports.
                mLocationClient.requestLocationUpdates(
                        mLocationRequest, mLocationCallback, Looper.getMainLooper());
            } else if (permission == PackageManager.PERMISSION_DENIED && mLocationClient != null) {
                // Permission previously granted but now revoked? Null out the location client.
                // But first remove our location report update callback.
                mLocationClient.removeLocationUpdates(mLocationCallback);
                mLocationClient = null;
            }
        }

        /**
         * We've received a new location, or maybe null. Shuffle it to our LocationCalculator.
         *
         * @param location Our current location, or null if we don't know where we are
         */
        private void updateLocation(@Nullable Location location) {
            // Update UI with location data
            // ...
            // Note: can be null if we don't have permissions, don't have GPS reception,
            // or a number of other conditions; handle accordingly.
            getWatchFaceState().getLocationCalculator().setLocation(location);
            if (location != null) {
                WatchPartStatsDrawable.mInvalidTrigger = WatchPartStatsDrawable.INVALID_LOCATION;
                invalidate();
            }
        }

        // Pulls all user's preferences for watch face appearance.
        private void loadSavedPreferences() {
            getWatchFaceState().setString(mSharedPref.getWatchFaceStateString());
        }

//        @Override
//        public void onApplyWindowInsets(@NonNull WindowInsets insets) {
//            super.onApplyWindowInsets(insets);
//            SharedPref.setIsRoundScreen(insets.isRound());
//            // cutoutSize = insets.getSystemWindowInsetBottom();
//        }

        @Override
        public void onPropertiesChanged(@NonNull Bundle properties) {
            super.onPropertiesChanged(properties);

            // The properties that matter to us.
            boolean lowBitAmbient = properties.getBoolean(PROPERTY_LOW_BIT_AMBIENT, false);
            boolean burnInProtection = properties.getBoolean(PROPERTY_BURN_IN_PROTECTION, false);
            boolean offloadSupported = properties.getBoolean(PROPERTY_OFFLOAD_SUPPORTED, false);

            // Set low-bit ambient on our ambient watch face paint as required.
            getWatchFaceState().getPaintBox().getAmbientPaint().setAntiAlias(!lowBitAmbient);

            // Set low-bit ambient and burn-in protection on our complications as required.
            getWatchFaceState().getComplications().forEach(
                    c -> c.setLowBitAmbientBurnInProtection(lowBitAmbient, burnInProtection));

            // Set offload support!
            SharedPref.setIsOffloadSupported(offloadSupported);
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

            getWatchFaceState().setAmbient(inAmbientMode);

            // Check and trigger whether or not timer should be running (only in active mode).
            updateTimer();

            // Cancel any existing toast messages if we're going ambient.
            if (inAmbientMode) {
                Toaster.cancelCurrent();
            }

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

        /**
         * Can we draw a decomposition?
         *
         * @return Whether the hardware supports offload and the user has opted in to decomposition
         */
        private boolean canDrawDecomposition() {
            return SharedPref.isOffloadSupported() && getWatchFaceState().getUseDecomposition();
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
            if (isVisible()) {
                // Draw to the canvas if we're in active or ambient modes.
                mWatchFaceGlobalDrawable.draw(canvas);
            }

            if (canDrawDecomposition() && (!mHasDecompositionBeenSent ||
                    mWatchFaceGlobalDrawable.hasDecompositionUpdateAvailable())) {
                // If we can draw a decomposition, and we have something new to send...
                WatchFaceDecomposition.Builder builder = new WatchFaceDecomposition.Builder();
                // Build and update the decomposition.
                long nextUpdateTime = mWatchFaceGlobalDrawable.buildDecomposition(builder);
                updateDecomposition(builder.build());
                // Reschedule the alarm; we don't need it for another n milliseconds.
                scheduleNextUpdateDecomposableAlarm(nextUpdateTime);
                mHasDecompositionBeenSent = true;
            } else if (!canDrawDecomposition() && mHasDecompositionBeenSent) {
                // Remove any decomposition we may have had.
                updateDecomposition(null);
                mHasDecompositionBeenSent = false;
            }

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
                // Location permissions might have been granted or revoked since last time.
                setLocationListener();
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

            // Update our decomposable alarm: start or stop it as required.
            if (visible || !canDrawDecomposition()) {
                try {
                    unregisterReceiver(mDecomposableUpdateBroadcastReceiver);
                } catch (IllegalArgumentException e) {
                    // No action -- it wasn't registered yet.
                }
                mDecomposableUpdateAlarmManager.cancel(mDecomposableUpdatePendingIntent);
            } else {
                registerReceiver(
                        mDecomposableUpdateBroadcastReceiver, mDecomposableUpdateIntentFilter);
                invalidate();
            }
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
        /**
         * An array with all default complication providers. Done on a per-slot basis. An array
         * of two-int pairs; the first is the provider, the second is the provider type (or
         * ComplicationData.TYPE_NOT_CONFIGURED if blank). The first two-int pair is for the
         * background complication; the remainder are for the foreground complications.
         *
         * @return Array with all default complication providers.
         */
        @NonNull
        public int[][] getDefaultSystemComplicationProviders() {
            return new int[][]{
                    {0, ComplicationData.TYPE_NOT_CONFIGURED}, // Background
                    {SystemProviders.WATCH_BATTERY, ComplicationData.TYPE_RANGED_VALUE},
                    {0, ComplicationData.TYPE_NOT_CONFIGURED}, // Blank slot
                    {SystemProviders.TIME_AND_DATE, ComplicationData.TYPE_SHORT_TEXT},
            };
        }
    }

    public static final class B extends ProWatchFaceService {
        /**
         * An array with all default complication providers. Done on a per-slot basis. An array
         * of two-int pairs; the first is the provider, the second is the provider type (or
         * ComplicationData.TYPE_NOT_CONFIGURED if blank). The first two-int pair is for the
         * background complication; the remainder are for the foreground complications.
         *
         * @return Array with all default complication providers.
         */
        @NonNull
        public int[][] getDefaultSystemComplicationProviders() {
            return new int[][]{
                    {0, ComplicationData.TYPE_NOT_CONFIGURED}, // Background
                    {SystemProviders.WATCH_BATTERY, ComplicationData.TYPE_RANGED_VALUE},
                    {SystemProviders.TIME_AND_DATE, ComplicationData.TYPE_SHORT_TEXT},
                    {SystemProviders.WORLD_CLOCK, ComplicationData.TYPE_SHORT_TEXT},
            };
        }
    }

    public static final class C extends ProWatchFaceService {
        /**
         * An array with all default complication providers. Done on a per-slot basis. An array
         * of two-int pairs; the first is the provider, the second is the provider type (or
         * ComplicationData.TYPE_NOT_CONFIGURED if blank). The first two-int pair is for the
         * background complication; the remainder are for the foreground complications.
         *
         * @return Array with all default complication providers.
         */
        @NonNull
        public int[][] getDefaultSystemComplicationProviders() {
            return new int[][]{
                    {0, ComplicationData.TYPE_NOT_CONFIGURED}, // Background
                    {SystemProviders.STEP_COUNT, ComplicationData.TYPE_SHORT_TEXT},
                    {SystemProviders.TIME_AND_DATE, ComplicationData.TYPE_SHORT_TEXT},
                    {0, ComplicationData.TYPE_NOT_CONFIGURED}, // Blank slot
                    {SystemProviders.WATCH_BATTERY, ComplicationData.TYPE_RANGED_VALUE},
                    {0, ComplicationData.TYPE_NOT_CONFIGURED}, // Blank slot
                    {SystemProviders.WORLD_CLOCK, ComplicationData.TYPE_SHORT_TEXT},
                    {SystemProviders.WORLD_CLOCK, ComplicationData.TYPE_SHORT_TEXT},
                    {0, ComplicationData.TYPE_NOT_CONFIGURED}, // Blank slot
            };
        }
    }

    public static final class D extends ProWatchFaceService {
        /**
         * An array with all default complication providers. Done on a per-slot basis. An array
         * of two-int pairs; the first is the provider, the second is the provider type (or
         * ComplicationData.TYPE_NOT_CONFIGURED if blank). The first two-int pair is for the
         * background complication; the remainder are for the foreground complications.
         *
         * @return Array with all default complication providers.
         */
        @NonNull
        public int[][] getDefaultSystemComplicationProviders() {
            return new int[][]{
                    {0, ComplicationData.TYPE_NOT_CONFIGURED}, // Background
                    {SystemProviders.STEP_COUNT, ComplicationData.TYPE_SHORT_TEXT},
                    {SystemProviders.TIME_AND_DATE, ComplicationData.TYPE_SHORT_TEXT},
                    {SystemProviders.WORLD_CLOCK, ComplicationData.TYPE_SHORT_TEXT},
                    {SystemProviders.MOST_RECENT_APP, ComplicationData.TYPE_SMALL_IMAGE},
                    {SystemProviders.APP_SHORTCUT, ComplicationData.TYPE_SMALL_IMAGE},
                    {SystemProviders.WATCH_BATTERY, ComplicationData.TYPE_RANGED_VALUE },
            };
        }
    }
}
