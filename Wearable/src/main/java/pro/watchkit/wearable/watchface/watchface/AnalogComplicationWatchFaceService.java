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
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.wearable.complications.ComplicationData;
import android.support.wearable.watchface.WatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.util.Log;
import android.view.SurfaceHolder;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.lang.ref.WeakReference;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import pro.watchkit.wearable.watchface.R;
import pro.watchkit.wearable.watchface.model.ComplicationHolder;
import pro.watchkit.wearable.watchface.model.WatchFaceState;

public class AnalogComplicationWatchFaceService extends HardwareAcceleratedCanvasWatchFaceService {
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

    @Override
    public Engine onCreateEngine() {
        return new Engine();
    }


    private static class UpdateTimeHandler extends Handler {
        private final WeakReference<AnalogComplicationWatchFaceService.Engine> mWeakReference;

        UpdateTimeHandler(AnalogComplicationWatchFaceService.Engine reference) {
            mWeakReference = new WeakReference<>(reference);
        }

        @Override
        public void handleMessage(Message msg) {
            AnalogComplicationWatchFaceService.Engine engine = mWeakReference.get();
            if (engine != null) {
                engine.updateTimeViaHandler();
            }
        }
    }

    private class Engine extends HardwareAcceleratedCanvasWatchFaceService.Engine implements ComplicationHolder.InvalidateCallback {

        private static final int MSG_UPDATE_TIME = 0;
        // Handler to update the time once a second in interactive mode.
        private final Handler mUpdateTimeHandler = new UpdateTimeHandler(this);

        private final BroadcastReceiver mTimeZoneReceiver =
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        getWatchFaceState().setDefaultTimeZone();
                        WatchPartStatsDrawable.mInvalidTrigger = WatchPartStatsDrawable.INVALID_TIMEZONE;
                        invalidate();
                    }
                };
        private WatchFaceGlobalDrawable mWatchFaceGlobalDrawable;
        // Used to pull user's preferences for background color, highlight color, and visual
        // indicating there are unread notifications.
        SharedPreferences mSharedPref;

        private boolean mRegisteredTimeZoneReceiver = false;
        private boolean mMuteMode;
        // User's preference for if they want visual shown to indicate unread notifications.
        private boolean mUnreadNotificationsPreference;

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
        private WatchFaceState getWatchFaceState() {
            return mWatchFaceGlobalDrawable.getWatchFaceState();
        }

        @Override
        public void onCreate(SurfaceHolder holder) {
            Log.d(TAG, "onCreate");

            super.onCreate(holder);

            // Used throughout watch face to pull user's preferences.
            Context context = getApplicationContext();
            mSharedPref =
                    context.getSharedPreferences(
                            getString(R.string.analog_complication_preference_file_key),
                            Context.MODE_PRIVATE);

            setWatchFaceStyle(
                    new WatchFaceStyle.Builder(AnalogComplicationWatchFaceService.this)
                            .setAcceptsTapEvents(true)
                            .setHideNotificationIndicator(true)
                            .setViewProtectionMode(WatchFaceStyle.PROTECT_STATUS_BAR)
                            .build());

            mWatchFaceGlobalDrawable = new WatchFaceGlobalDrawable(context,
                    new WatchPartDrawable[]{
                            new WatchPartBackgroundDrawable(),
                            new WatchPartTicksRingsDrawable(),
                            new WatchPartComplicationsDrawable(),
                            new WatchPartHandsDrawable(),
                            new WatchPartStatsDrawable()});

            loadSavedPreferences();
            initializeComplications();

            FusedLocationProviderClient locationClient
                    = LocationServices.getFusedLocationProviderClient(context);

            if (context.checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                LocationRequest req = new LocationRequest();
                req.setInterval(1000 * 60 * 15);
                req.setFastestInterval(1000 * 60);
                req.setPriority(LocationRequest.PRIORITY_LOW_POWER);

                locationClient.requestLocationUpdates(req, new LocationCallback() {
                            @Override
                            public void onLocationResult(LocationResult locationResult) {
                                if (locationResult == null) {
                                    Log.d(TAG, "onLocationResult: no locations (it's null)");
                                    return;
                                }
                                for (Location location : locationResult.getLocations()) {
                                    // Update UI with location data
                                    // ...
                                    Log.w(TAG, "onLocationResult: Got last location! It's "
                                            + (location == null ? "null..." :
                                            location.getLatitude() + " / "
                                                    + location.getLongitude()
                                                    + " / " + location.getAltitude()));
                                    // Note: can be null in rare situations; handle accordingly.
                                    getWatchFaceState().getLocationCalculator().setLocation(location);
                                    WatchPartStatsDrawable.mInvalidTrigger = WatchPartStatsDrawable.INVALID_LOCATION;
                                    postInvalidate();
                                }
                            }
                        },
                        null).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            // Task completed successfully
                            Log.d(TAG, "requestLocationUpdates onComplete isSuccessful: " + task.getResult());
                        } else {
                            // Task failed with an exception
                            Log.d(TAG, "requestLocationUpdates onComplete exception: ", task.getException());
                        }

                    }
                });
            }
        }

        // Pulls all user's preferences for watch face appearance.
        private void loadSavedPreferences() {
            getWatchFaceState().getWatchFacePreset().setString(mSharedPref.getString(
                    getApplicationContext().getString(R.string.saved_watch_face_preset),
                    null));

            String unreadNotificationPreferenceResourceName =
                    getApplicationContext().getString(R.string.saved_unread_notifications_pref);

            mUnreadNotificationsPreference =
                    mSharedPref.getBoolean(unreadNotificationPreferenceResourceName, true);
        }

        private void initializeComplications() {
            Log.d(TAG, "initializeComplications()");

            Context context = getApplicationContext();
            int[] complicationIds = getWatchFaceState().initializeComplications(context, this);
            setActiveComplications(complicationIds);
        }

        @Override
        public void onPropertiesChanged(Bundle properties) {
            super.onPropertiesChanged(properties);
            //Log.d(TAG, "onPropertiesChanged: low-bit ambient = " + mLowBitAmbient);

            boolean lowBitAmbient = properties.getBoolean(PROPERTY_LOW_BIT_AMBIENT, false);
            boolean burnInProtection = properties.getBoolean(PROPERTY_BURN_IN_PROTECTION, false);

//            painter.setLowBitAmbientBurnInProtection(lowBitAmbient, burnInProtection);
            getWatchFaceState().getPaintBox().getAmbientPaint().setAntiAlias(!lowBitAmbient);

            // Updates complications to properly render in ambient mode based on the
            // screen's capabilities.
            for (ComplicationHolder complication : getWatchFaceState().getComplications()) {
                complication.setLowBitAmbientBurnInProtection(lowBitAmbient, burnInProtection);
            }
        }

        /*
         * Called when there is updated data for a complication id.
         */
        @Override
        public void onComplicationDataUpdate(
                int complicationId, ComplicationData complicationData) {
//            Log.d(TAG, "onComplicationDataUpdate() id: " + complicationId + " / " + complicationData.getType());

            // Adds/updates active complication data in the array.
            getWatchFaceState().onComplicationDataUpdate(complicationId, complicationData);

            WatchPartStatsDrawable.mInvalidTrigger = WatchPartStatsDrawable.INVALID_COMPLICATION;
            invalidate();
        }

        @Override
        public void onTapCommand(int tapType, int x, int y, long eventTime) {
//            Log.d(TAG, "OnTapCommand()");
            switch (tapType) {
                case TAP_TYPE_TAP:
                    if (getWatchFaceState().onComplicationTap(x, y)) {
                        return;
                    }
                    break;
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
//            Log.d(TAG, "onAmbientModeChanged: " + inAmbientMode);

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

            // Propagate our size to our drawable.
            mWatchFaceGlobalDrawable.setBounds(0, 0, width, height);

            /*
             * Calculates location bounds for right and left circular complications. Please note,
             * we are not demonstrating a long text complication in this watch face.
             *
             * We suggest using at least 1/4 of the screen width for circular (or squared)
             * complications and 2/3 of the screen width for wide rectangular complications for
             * better readability.
             */

            getWatchFaceState().onSurfaceChanged(width, height);

            WatchPartStatsDrawable.mInvalidTrigger = WatchPartStatsDrawable.INVALID_SURFACE;
            invalidate();
        }

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            boolean prevAmbient = getWatchFaceState().isAmbient();
            super.onDraw(canvas, bounds);

            int unreadNotifications = mUnreadNotificationsPreference ? getUnreadCount() : 0;
            int totalNotifications = mUnreadNotificationsPreference ? getNotificationCount() : 0;

            if (isInAmbientMode()) {
                getWatchFaceState().preDrawAmbientCheck();
            }
            // Draw all our drawables.
            // First set all our state objects.
            getWatchFaceState().setCurrentTimeToNow();
            getWatchFaceState().setNotifications(unreadNotifications, totalNotifications);
//          getWatchFaceState().preset = preset;

            mWatchFaceGlobalDrawable.draw(canvas);
//          int s = 0;
//          long now0 = SystemClock.elapsedRealtimeNanos();
//          for (WatchPartDrawable d : mWatchPartDrawables) {
//              // For each of our drawables: draw it!
//              d.draw(canvas);
//              long now1 = SystemClock.elapsedRealtimeNanos();
//              WatchPartStatsDrawable.now[s] = now1 - now0;
//              now0 = now1;
//              s++;
//          }

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

                // With the rest of the watch face, we update the paint colors based on
                // ambient/active mode callbacks, but because the ComplicationDrawable handles
                // the active/ambient colors, we only need to update the complications' colors when
                // the user actually makes a change to the highlight color, not when the watch goes
                // in and out of ambient mode.
                getWatchFaceState().setComplicationsActiveAndAmbientColors();

                registerReceiver();
                // Update time zone in case it changed while we weren't visible.
                getWatchFaceState().setDefaultTimeZone();
                WatchPartStatsDrawable.mInvalidTrigger = WatchPartStatsDrawable.INVALID_TIMEZONE;
                invalidate();
            } else {
                unregisterReceiver();
            }

            /* Check and trigger whether or not timer should be running (only in active mode). */
            updateTimer();
        }

        @Override
        public void onNotificationCountChanged(int count) {
            Log.d(TAG, "onNotificationCountChanged(): " + count);

            if (mUnreadNotificationsPreference) {
                WatchPartStatsDrawable.mInvalidTrigger = WatchPartStatsDrawable.INVALID_NOTIFICATION;
                invalidate();
            }
        }

        @Override
        public void onUnreadCountChanged(int count) {
            Log.d(TAG, "onUnreadCountChanged(): " + count);

            if (mUnreadNotificationsPreference) {
                WatchPartStatsDrawable.mInvalidTrigger = WatchPartStatsDrawable.INVALID_NOTIFICATION;
                invalidate();
            }
        }

        private void registerReceiver() {
            if (mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = true;
            IntentFilter filter = new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED);
            AnalogComplicationWatchFaceService.this.registerReceiver(mTimeZoneReceiver, filter);
        }

        private void unregisterReceiver() {
            if (!mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = false;
            AnalogComplicationWatchFaceService.this.unregisterReceiver(mTimeZoneReceiver);
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
}
