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
package pro.watchkit.wearable.watchface.config;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.complications.ComplicationProviderInfo;
import android.support.wearable.complications.ProviderChooserIntent;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.splashscreen.SplashScreen;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.wear.widget.WearableRecyclerView;

import java.util.Arrays;

import pro.watchkit.wearable.watchface.R;
import pro.watchkit.wearable.watchface.model.AttributionConfigData;
import pro.watchkit.wearable.watchface.model.ColorsMaterialsConfigData;
import pro.watchkit.wearable.watchface.model.ComplicationConfigData;
import pro.watchkit.wearable.watchface.model.ConfigData;
import pro.watchkit.wearable.watchface.model.ConfigurationConfigData;
import pro.watchkit.wearable.watchface.model.MaterialConfigData;
import pro.watchkit.wearable.watchface.model.SettingsConfigData;
import pro.watchkit.wearable.watchface.model.TypefaceConfigData;
import pro.watchkit.wearable.watchface.model.WatchFacePresetConfigData;
import pro.watchkit.wearable.watchface.model.WatchPartDialConfigData;
import pro.watchkit.wearable.watchface.model.WatchPartHandsConfigData;
import pro.watchkit.wearable.watchface.watchface.ProWatchFaceService;

/**
 * The watch-side config activity for {@link ProWatchFaceService}, which
 * allows for setting the left and right complications of watch face along with the second's marker
 * color, background color, unread notifications toggle, and background complication image.
 */
public class ConfigActivity extends Activity {

    static final int COMPLICATION_CONFIG_REQUEST_CODE = 1001;
    static final int UPDATED_CONFIG_REDRAW_PLEASE_REQUEST_CODE = 1002;
    static final int UPDATED_CONFIG_REDRAW_NO_MATTER_WHAT_RESULT_CODE = 1003;
    static final String CONFIG_DATA =
            ConfigActivity.class.getSimpleName() + ".CONFIG_DATA";
    private ConfigRecyclerViewAdapter mAdapter;
    private ConfigSubActivity mCurrentSubActivity;
    @Nullable
    private ConfigData mConfigData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);

        // Try to get the watch face slot from our activity intent.
        Class<? extends ProWatchFaceService> watchFaceServiceClass = ProWatchFaceService.A.class;
        if (getIntent().getAction() != null) {
            switch (getIntent().getAction()) {
                case "pro.watchkit.wearable.watchface.CONFIG_WATCH_KIT_PRO_B": {
                    watchFaceServiceClass = ProWatchFaceService.B.class;
                    break;
                }
                case "pro.watchkit.wearable.watchface.CONFIG_WATCH_KIT_PRO_C": {
                    watchFaceServiceClass = ProWatchFaceService.C.class;
                    break;
                }
                case "pro.watchkit.wearable.watchface.CONFIG_WATCH_KIT_PRO_D": {
                    watchFaceServiceClass = ProWatchFaceService.D.class;
                    break;
                }
            }
        }

        // Try to get mCurrentSubActivity from our activity intent.
        if (mCurrentSubActivity == null) {
            String configDataString = getIntent().getStringExtra(CONFIG_DATA);
            Arrays.stream(ConfigSubActivity.finalValues)
                    .filter(c -> c.mClassName.equals(configDataString))
                    .findFirst()
                    .ifPresent(c -> mCurrentSubActivity = c);
        }

        // Well, if we don't have mCurrentSubActivity by now, set it to default...
        if (mCurrentSubActivity == null) {
            mCurrentSubActivity = ConfigSubActivity.Configuration;
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
    }

    @Override
    protected void onDestroy() {
        // Unset the adapter. This cleans it up.
        WearableRecyclerView view = findViewById(R.id.wearable_recycler_view);
        view.setAdapter(null);
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @NonNull Intent data) {
        if (requestCode == COMPLICATION_CONFIG_REQUEST_CODE && resultCode == RESULT_OK) {
            // Retrieves information for selected Complication provider.
            ComplicationProviderInfo complicationProviderInfo =
                    data.getParcelableExtra(ProviderChooserIntent.EXTRA_PROVIDER_INFO);

            // Updates preview with new complication information for selected complication id.
            // Note: complication id is saved and tracked in the adapter class.
            mAdapter.updateSelectedComplication(complicationProviderInfo);
        } else if (requestCode == UPDATED_CONFIG_REDRAW_PLEASE_REQUEST_CODE
                && resultCode == RESULT_OK) {
            // Config has been updated. Redraw if the result code was successful.
            // Updates highlight and background colors based on the user preference.
            mAdapter.onWatchFaceStateChanged();
        } else if (requestCode == UPDATED_CONFIG_REDRAW_NO_MATTER_WHAT_RESULT_CODE) {
            // Config has been updated. Ignore the result code, and just redraw!
            // Updates highlight and background colors based on the user preference.
            mAdapter.onWatchFaceStateChanged();
        }
    }

    private enum ConfigSubActivity {
        Settings(SettingsConfigData.class, R.string.config_configure_settings, R.drawable.ic_settings),
        WatchFacePresets(WatchFacePresetConfigData.class, R.string.config_configure_watch_face_preset, R.drawable.ic_hands_pips),
        Complications(ComplicationConfigData.class, R.string.config_configure_complications, R.drawable.ic_complications),
        // N.B. As a shortcut, put items NOT in the NavigationAdapter at the end of this list.
        Configuration(ConfigurationConfigData.class, R.string.config_configure_configuration, -1),
        ColorsMaterials(ColorsMaterialsConfigData.class, R.string.config_configure_colors_materials, -1),
        MaterialFillHighlight(MaterialConfigData.FillHighlight.class, R.string.config_configure_material, -1),
        MaterialAccentFill(MaterialConfigData.AccentFill.class, R.string.config_configure_material, -1),
        MaterialAccentHighlight(MaterialConfigData.AccentHighlight.class, R.string.config_configure_material, -1),
        MaterialBaseAccent(MaterialConfigData.BaseAccent.class, R.string.config_configure_material, -1),
        WatchPartHands(WatchPartHandsConfigData.class, R.string.config_configure_hands, -1),
        WatchPartPips(WatchPartDialConfigData.class, R.string.config_configure_dial, -1),
        Typeface(TypefaceConfigData.class, R.string.config_configure_typeface, -1),
        Attribution(AttributionConfigData.class, R.string.config_licence, -1);

        @NonNull
        static final ConfigSubActivity[] finalValues = values();

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
            } catch (@NonNull IllegalAccessException | InstantiationException e) {
                return null;
            }
        }
    }
}
