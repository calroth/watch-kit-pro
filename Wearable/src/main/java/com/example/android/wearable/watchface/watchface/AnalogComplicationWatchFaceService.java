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

package com.example.android.wearable.watchface.watchface;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.wearable.complications.ComplicationData;
import android.support.wearable.watchface.WatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.util.Log;
import android.view.SurfaceHolder;

import com.example.android.wearable.watchface.R;
import com.example.android.wearable.watchface.model.ComplicationHolder;
import com.example.android.wearable.watchface.model.LocationCalculator;
import com.example.android.wearable.watchface.model.Palette;
import com.example.android.wearable.watchface.model.WatchFacePreset;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class AnalogComplicationWatchFaceService extends HardwareAcceleratedCanvasWatchFaceService {
//public class AnalogComplicationWatchFaceService extends WatchFaceService {
    private static final String TAG = "AnalogWatchFace";

    // Unique IDs for each complication. The settings activity that supports allowing users
    // to select their complication data provider requires numbers to be >= 0.
//    private static final int BACKGROUND_COMPLICATION_ID = 99;

    private static final int FOREGROUND_COMPLICATION_COUNT = 6;

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

        @Override
        protected void beforeDoFrame(int invalidated) {
            WatchFaceStatsDrawable stats = (WatchFaceStatsDrawable)(mWatchFaceDrawables[4]);
            if (stats.invalid < invalidated) {
                // Set painter invalid display to the maximum we've seen for a while
                stats.invalid = invalidated;
            }
        }

        @Override
        protected void afterDoFrame(int invalidated) {
            WatchFaceStatsDrawable stats = (WatchFaceStatsDrawable)(mWatchFaceDrawables[4]);
            stats.invalid = 0;
        }

        private WatchFacePreset preset = new WatchFacePreset();

        private WatchFaceDrawable.StateObject mStateObject;
        private Palette mPalette = new Palette();
        private GregorianCalendar mCalendar = new GregorianCalendar();
        private LocationCalculator mLocationCalculator = new LocationCalculator(mCalendar);

        private WatchFaceDrawable[] mWatchFaceDrawables = new WatchFaceDrawable[] {
                new WatchFaceBackgroundDrawable(),
                new WatchFaceTicksRingsDrawable(),
                new WatchFaceComplicationsDrawable(),
                new WatchFaceHandsDrawable(),
                new WatchFaceStatsDrawable()
        };

        private static final int MSG_UPDATE_TIME = 0;

        private boolean mRegisteredTimeZoneReceiver = false;
        private boolean mMuteMode;

        /* Maps active complication ids to the data for that complication. Note: Data will only be
         * present if the user has chosen a provider via the settings activity for the watch face.
         */
//        private SparseArray<ComplicationData> mActiveComplicationDataSparseArray;

        /* Maps complication ids to corresponding ComplicationDrawable that renders the
         * the complication data on the watch face.
         */
        //private SparseArray<ComplicationDrawable> mComplicationDrawableSparseArray;
        private Collection<ComplicationHolder> complications;

        // Used to pull user's preferences for background color, highlight color, and visual
        // indicating there are unread notifications.
        SharedPreferences mSharedPref;

        // User's preference for if they want visual shown to indicate unread notifications.
        private boolean mUnreadNotificationsPreference;

        private final BroadcastReceiver mTimeZoneReceiver =
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        mCalendar.setTimeZone(TimeZone.getDefault());
                        invalidate();
                    }
                };

        // Handler to update the time once a second in interactive mode.
        private final Handler mUpdateTimeHandler = new UpdateTimeHandler(this);

        private void updateTimeViaHandler() {
            invalidate();
            if (shouldTimerBeRunning()) {
                long timeMs = System.currentTimeMillis();
                long delayMs = INTERACTIVE_UPDATE_RATE_MS - (timeMs % INTERACTIVE_UPDATE_RATE_MS);
                mUpdateTimeHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIME, delayMs);
            }
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

            loadSavedPreferences();
            initializeComplications();

            mStateObject = mWatchFaceDrawables[0].new StateObject();

            for (WatchFaceDrawable d : mWatchFaceDrawables) {
                d.setState(mStateObject, mPalette, mCalendar, mLocationCalculator);
            }

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
                                    mLocationCalculator.setLocation(location);
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
            mPalette.setPalette(
                    mSharedPref.getInt(
                            getApplicationContext().getString(R.string.saved_fill_color),
                            Color.WHITE),
                    mSharedPref.getInt(
                            getApplicationContext().getString(R.string.saved_accent_color),
                            Color.BLUE),
                    mSharedPref.getInt(
                            getApplicationContext().getString(R.string.saved_marker_color),
                            Color.RED),
                    mSharedPref.getInt(
                            getApplicationContext().getString(R.string.saved_base_color),
                            Color.BLACK));


//            painter.setPalette(-1, -10011977, -43230, -16777216);
//            painter.setPalette(preset);

            String unreadNotificationPreferenceResourceName =
                    getApplicationContext().getString(R.string.saved_unread_notifications_pref);

            mUnreadNotificationsPreference =
                    mSharedPref.getBoolean(unreadNotificationPreferenceResourceName, true);
        }

        private void initializeComplications() {
            Log.d(TAG, "initializeComplications()");

            // Creates a ComplicationDrawable for each location where the user can render a
            // complication on the watch face. In this watch face, we create one for left, right,
            // and background, but you could add many more.
            ComplicationHolder.resetBaseId();

            Context context = getApplicationContext();

            complications = new ArrayList<>();
            {
                final ComplicationHolder b = new ComplicationHolder(context);
                b.isForeground = false;
                b.isActive = false;
                b.setDrawableCallback(this);
                complications.add(b);
            }

            for (int i = 0; i < FOREGROUND_COMPLICATION_COUNT; i++) {
                final ComplicationHolder f = new ComplicationHolder(context);
                f.isForeground = true;
                f.setDrawableCallback(this);
                complications.add(f);
            }

            // Adds new complications to a SparseArray to simplify setting styles and ambient
            // properties for all complications, i.e., iterate over them all.
            setComplicationsActiveAndAmbientColors(mPalette.getHighlightColor());

            int[] complicationIds = new int[complications.size()];
            int i = 0;
            for (ComplicationHolder complication : complications) {
                complicationIds[i] = complication.getId();
                i++;
            }

            setActiveComplications(complicationIds);
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

        @Override
        public void onPropertiesChanged(Bundle properties) {
            super.onPropertiesChanged(properties);
            //Log.d(TAG, "onPropertiesChanged: low-bit ambient = " + mLowBitAmbient);

            boolean lowBitAmbient = properties.getBoolean(PROPERTY_LOW_BIT_AMBIENT, false);
            boolean burnInProtection = properties.getBoolean(PROPERTY_BURN_IN_PROTECTION, false);

//            painter.setLowBitAmbientBurnInProtection(lowBitAmbient, burnInProtection);
            mPalette.getAmbientPaint().setAntiAlias(!lowBitAmbient);

            // Updates complications to properly render in ambient mode based on the
            // screen's capabilities.
            for (ComplicationHolder complication : complications) {
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
            invalidate();
        }

        @Override
        public void onTapCommand(int tapType, int x, int y, long eventTime) {
//            Log.d(TAG, "OnTapCommand()");
            switch (tapType) {
                case TAP_TYPE_TAP:
                    // Try all foreground complications first, before background complications.
                    for (ComplicationHolder complication : complications) {
                        if (complication.isForeground) {
                            boolean successfulTap = complication.onDrawableTap(x, y);

                            if (successfulTap) {
                                return;
                            }
                        }
                    }
                    // Try all background complications.
                    for (ComplicationHolder complication : complications) {
                        if (!complication.isForeground) {
                            boolean successfulTap = complication.onDrawableTap(x, y);

                            if (successfulTap) {
                                return;
                            }
                        }
                    }
                    break;
            }
        }

        @Override
        public void onTimeTick() {
            super.onTimeTick();
            invalidate();
        }

        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);
//            Log.d(TAG, "onAmbientModeChanged: " + inAmbientMode);

//            painter.onAmbientModeChanged(inAmbientMode);
            mStateObject.ambient = inAmbientMode;

            // Update drawable complications' ambient state.
            // Note: ComplicationDrawable handles switching between active/ambient colors, we just
            // have to inform it to enter ambient mode.
            for (ComplicationHolder complication : complications) {
                complication.setAmbientMode(inAmbientMode);
            }

            // Check and trigger whether or not timer should be running (only in active mode).
            updateTimer();

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

            invalidate();
        }

        private final int COMPLICATION_AMBIENT_WHITE =
                Color.argb(0xff, 0xff, 0xff, 0xff);
        private final int COMPLICATION_AMBIENT_GREY =
                Color.argb(0xff, 0xaa, 0xaa, 0xaa);

        private int currentComplicationWhite, currentComplicationGrey;

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            super.onDraw(canvas, bounds);
            long now = System.currentTimeMillis();

            int unreadNotifications = mUnreadNotificationsPreference ? getUnreadCount() : 0;
            int totalNotifications = mUnreadNotificationsPreference ? getNotificationCount() : 0;

            if (isInAmbientMode()) {
//                Log.d(TAG, "Draw (ambient)");
                int newComplicationWhite = mLocationCalculator.getDuskDawnColor(COMPLICATION_AMBIENT_WHITE);
                int newComplicationGrey = mLocationCalculator.getDuskDawnColor(COMPLICATION_AMBIENT_GREY);

                if (currentComplicationWhite != newComplicationWhite
                        || currentComplicationGrey != newComplicationGrey) {
                    for (ComplicationHolder complication : complications) {
                        complication.setAmbientColors(newComplicationWhite, newComplicationGrey,
                                newComplicationGrey);
//                        complication.drawable.setTextColorAmbient(newComplicationWhite);
//                        complication.drawable.setTitleColorAmbient(newComplicationGrey);
//                        complication.drawable.setIconColorAmbient(newComplicationGrey);
                    }

                    // Why go to the trouble of tracking current and new complication colors,
                    // and only updating when it's changed?

                    // Optimisation. We assume that setting colors on ComplicationDrawable is a
                    // heinously slow operation (it probably isn't though) and so we avoid it...

                    currentComplicationWhite = newComplicationWhite;
                    currentComplicationGrey = newComplicationGrey;
                }
            }

//            painter.drawAll(canvas, preset, now, complications, unreadNotifications, totalNotifications);
            {
                // Draw all our drawables.
                // First set all our state objects.
                mCalendar.setTimeInMillis(now);
                mStateObject.unreadNotifications = unreadNotifications;
                mStateObject.totalNotifications = totalNotifications;
                mStateObject.complications = complications;
                mStateObject.preset = preset;

                WatchFaceStatsDrawable stats = (WatchFaceStatsDrawable)(mWatchFaceDrawables[4]);
                int s = 0;
                long now0 = SystemClock.elapsedRealtimeNanos();
                for (WatchFaceDrawable d : mWatchFaceDrawables) {
                    // For each of our drawables: draw it!
                    d.draw(canvas);
                    long now1 = SystemClock.elapsedRealtimeNanos();
                    stats.now[s] = now1 - now0;
                    now0 = now1;
                    s++;
                }
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
                setComplicationsActiveAndAmbientColors(mPalette.getHighlightColor());

                registerReceiver();
                // Update time zone in case it changed while we weren't visible.
                mCalendar.setTimeZone(TimeZone.getDefault());
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
                invalidate();
            }
        }

        @Override
        public void onUnreadCountChanged(int count) {
            Log.d(TAG, "onUnreadCountChanged(): " + count);

            if (mUnreadNotificationsPreference) {
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
