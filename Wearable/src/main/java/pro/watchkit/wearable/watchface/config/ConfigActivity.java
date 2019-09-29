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
package pro.watchkit.wearable.watchface.config;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.wearable.complications.ComplicationProviderInfo;
import android.support.wearable.complications.ProviderChooserIntent;
import android.util.Log;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.wear.widget.WearableRecyclerView;
import androidx.wear.widget.drawer.WearableNavigationDrawerView;

import java.util.Arrays;

import pro.watchkit.wearable.watchface.R;
import pro.watchkit.wearable.watchface.model.ColorsStylesConfigData;
import pro.watchkit.wearable.watchface.model.ComplicationConfigData;
import pro.watchkit.wearable.watchface.model.ConfigData;
import pro.watchkit.wearable.watchface.model.SettingsConfigData;
import pro.watchkit.wearable.watchface.model.TypefaceConfigData;
import pro.watchkit.wearable.watchface.model.WatchFacePresetConfigData;
import pro.watchkit.wearable.watchface.model.WatchPartHandsConfigData;
import pro.watchkit.wearable.watchface.model.WatchPartTicksConfigData;
import pro.watchkit.wearable.watchface.util.SharedPref;
import pro.watchkit.wearable.watchface.watchface.ProWatchFaceService;

/**
 * The watch-side config activity for {@link ProWatchFaceService}, which
 * allows for setting the left and right complications of watch face along with the second's marker
 * color, background color, unread notifications toggle, and background complication image.
 */
public class ConfigActivity extends Activity {

    static final int COMPLICATION_CONFIG_REQUEST_CODE = 1001;
    static final int UPDATED_CONFIG_REDRAW_PLEASE_REQUEST_CODE = 1002;
    static final String CONFIG_DATA =
            ConfigActivity.class.getSimpleName() + ".CONFIG_DATA";
    private static final String TAG = ConfigActivity.class.getSimpleName();
    private ConfigRecyclerViewAdapter mAdapter;
    private ConfigSubActivity mCurrentSubActivity;
    @Nullable
    private ConfigData mConfigData;

    private WearableNavigationDrawerView mWearableNavigationDrawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        Log.d(TAG, "Intent: " + getIntent().getAction());

        // Try to get the watch face slot from our activity intent.
        Class watchFaceServiceClass = ProWatchFaceService.A.class;
        @StringRes int slotLabel = R.string.watch_face_service_short_label_a;
        if (getIntent().getAction() != null) {
            switch (getIntent().getAction()) {
                case "pro.watchkit.wearable.watchface.CONFIG_WATCH_KIT_PRO_B": {
                    watchFaceServiceClass = ProWatchFaceService.B.class;
                    slotLabel = R.string.watch_face_service_short_label_b;
                    break;
                }
                case "pro.watchkit.wearable.watchface.CONFIG_WATCH_KIT_PRO_C": {
                    watchFaceServiceClass = ProWatchFaceService.C.class;
                    slotLabel = R.string.watch_face_service_short_label_c;
                    break;
                }
                case "pro.watchkit.wearable.watchface.CONFIG_WATCH_KIT_PRO_D": {
                    watchFaceServiceClass = ProWatchFaceService.D.class;
                    slotLabel = R.string.watch_face_service_short_label_d;
                    break;
                }
            }
        }

        SharedPref sharedPref = new SharedPref(this, watchFaceServiceClass);

        // Try to get mCurrentSubActivity from our activity intent.
        if (mCurrentSubActivity == null) {
            String configDataString = getIntent().getStringExtra(CONFIG_DATA);
            Arrays.stream(ConfigSubActivity.values())
                    .filter(c -> c.mClassName.equals(configDataString))
                    .findFirst()
                    .ifPresent(c -> mCurrentSubActivity = c);
        }

        // If we couldn't get mCurrentSubActivity, try to get it from preferences.
        if (mCurrentSubActivity == null) {
            String configDataString = sharedPref.getMostRecentConfigPageString();
            Arrays.stream(ConfigSubActivity.values())
                    .filter(c -> c.mClassName.equals(configDataString))
                    .filter(c -> c.mDrawableId != -1) // Filter these "non-navigation" activities
                    .findFirst()
                    .ifPresent(c -> mCurrentSubActivity = c);
        }

        // Well, if we don't have mCurrentSubActivity by now, set it to default...
        if (mCurrentSubActivity == null) {
            mCurrentSubActivity = ConfigSubActivity.Settings;
        }

        // Save out our most recent selected config page, for next time.
        if (mCurrentSubActivity.mDrawableId != -1) {
            // Only if we're in the navigation. Otherwise we'll be trapped there with no
            // way to leave...
            sharedPref.putMostRecentConfigPageString(mCurrentSubActivity.mClassName);
        }

        if (mConfigData == null) {
            mConfigData = mCurrentSubActivity.getNewInstance();
        }

        setContentView(R.layout.activity_config);

        if (mConfigData != null) {
            mAdapter = new ConfigRecyclerViewAdapter(this, watchFaceServiceClass,
                    mConfigData.getDataToPopulateAdapter());
        }

        WearableRecyclerView wearableRecyclerView =
                findViewById(R.id.wearable_recycler_view);

        wearableRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Because we can add or remove items dynamically, we set this to false.
        // It makes things a little slower as the RecyclerView can't optimise some things.
        wearableRecyclerView.setHasFixedSize(false);

        wearableRecyclerView.setAdapter(mAdapter);

        mWearableNavigationDrawer = findViewById(R.id.top_navigation_drawer);
        // Only if we're part of the main navigation.
        if (mCurrentSubActivity.mDrawableId != -1) {
            // Set up our navigation drawer at the top of the view.
            mWearableNavigationDrawer.setAdapter(new NavigationAdapter(this, slotLabel));
            mWearableNavigationDrawer.setCurrentItem(mCurrentSubActivity.ordinal(), false);
            mWearableNavigationDrawer.addOnItemSelectedListener(pos -> {
                String configData = ConfigSubActivity.values()[pos].mClassName;

                Intent launchIntent =
                        new Intent(mWearableNavigationDrawer.getContext(), ConfigActivity.class);

                // Add an intent to the launch to point it towards our sub-activity.
                launchIntent.putExtra(CONFIG_DATA, configData);
                launchIntent.setAction(getIntent().getAction());

                Activity activity = (Activity) mWearableNavigationDrawer.getContext();
                activity.startActivity(launchIntent);
                finish(); // Remove this from the "back" stack, so it's a direct switch.
            });

            // Give a hint it's there.
            mWearableNavigationDrawer.getController().peekDrawer();
        } else {
            mWearableNavigationDrawer.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onStop() {
        // Unset the adapter. This cleans it up.
        WearableRecyclerView view = findViewById(R.id.wearable_recycler_view);
        view.setAdapter(null);
        super.onStop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @NonNull Intent data) {
        if (requestCode == COMPLICATION_CONFIG_REQUEST_CODE
                && resultCode == RESULT_OK) {

            // Retrieves information for selected Complication provider.
            ComplicationProviderInfo complicationProviderInfo =
                    data.getParcelableExtra(ProviderChooserIntent.EXTRA_PROVIDER_INFO);
            Log.d(TAG, "Provider: " + complicationProviderInfo);

            // Updates preview with new complication information for selected complication id.
            // Note: complication id is saved and tracked in the adapter class.
            mAdapter.updateSelectedComplication(complicationProviderInfo);

        } else if (requestCode == UPDATED_CONFIG_REDRAW_PLEASE_REQUEST_CODE
                && resultCode == RESULT_OK) {

            // Updates highlight and background colors based on the user preference.
            mAdapter.onWatchFaceStateChanged();
        }
    }

    private enum ConfigSubActivity {
        Settings(SettingsConfigData.class, R.string.config_configure_settings, R.drawable.ic_settings),
        WatchFacePresets(WatchFacePresetConfigData.class, R.string.config_configure_watch_face_preset, R.drawable.ic_hands_ticks),
        Complications(ComplicationConfigData.class, R.string.config_configure_complications, R.drawable.ic_complications),
        // N.B. As a shortcut, put items NOT in the NavigationAdapter at the end of this list.
        ColorsStyles(ColorsStylesConfigData.class, R.string.config_configure_colors_styles, -1),
        WatchPartHands(WatchPartHandsConfigData.class, R.string.config_configure_hands, -1),
        WatchPartTicks(WatchPartTicksConfigData.class, R.string.config_configure_ticks, -1),
        Typeface(TypefaceConfigData.class, R.string.config_configure_typeface, -1);

        @NonNull
        final String mClassName;
        @NonNull
        final Class<? extends ConfigData> mClass;
        @StringRes
        final int mTitleId;
        @DrawableRes
        final int mDrawableId; // Or -1 if not part of NavigationAdapter.

        ConfigSubActivity(@NonNull final Class<? extends ConfigData> c,
                          @StringRes final int titleId, @DrawableRes final int drawableId) {
            mClass = c;
            mClassName = c.getSimpleName();
            mTitleId = titleId;
            mDrawableId = drawableId;
        }

        @Nullable
        ConfigData getNewInstance() {
            try {
                return mClass.newInstance();
            } catch (IllegalAccessException | InstantiationException e) {
                return null;
            }
        }
    }

    private final class NavigationAdapter
            extends WearableNavigationDrawerView.WearableNavigationDrawerAdapter {

        private final Context mContext;
        @StringRes
        private final int mSlotLabel;

        NavigationAdapter(final Context context, final int slotLabel) {
            mContext = context;
            mSlotLabel = slotLabel;
        }

        /**
         * Just the ConfigSubActivity objects we actually want to present in this
         * NavigationAdapter.
         */
        private final ConfigSubActivity[] mNavigationActivities = new ConfigSubActivity[]{
                ConfigSubActivity.Settings,
                ConfigSubActivity.WatchFacePresets,
                ConfigSubActivity.Complications
        };

        @NonNull
        @Override
        public String getItemText(int index) {
            return mContext.getString(mSlotLabel) + " " +
                    mContext.getString(mNavigationActivities[index].mTitleId);
        }

        @ColorInt
        private final static int mDrawableTint = Color.BLACK;

        @Nullable
        @Override
        public Drawable getItemDrawable(int index) {
            Drawable result = mContext.getDrawable(mNavigationActivities[index].mDrawableId);
            if (result != null) {
                result.setTint(mDrawableTint);
            }
            return result;
        }

        @Override
        public int getCount() {
            return mNavigationActivities.length;
        }
    }
}
