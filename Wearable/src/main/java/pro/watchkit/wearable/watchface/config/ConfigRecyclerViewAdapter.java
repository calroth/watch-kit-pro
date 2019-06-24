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

import android.content.Context;
import android.content.SharedPreferences;
import android.support.wearable.complications.ComplicationProviderInfo;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import pro.watchkit.wearable.watchface.R;
import pro.watchkit.wearable.watchface.model.ComplicationHolder;
import pro.watchkit.wearable.watchface.model.ConfigData.ColorPickerConfigItem;
import pro.watchkit.wearable.watchface.model.ConfigData.ComplicationConfigItem;
import pro.watchkit.wearable.watchface.model.ConfigData.ConfigActivityConfigItem;
import pro.watchkit.wearable.watchface.model.ConfigData.ConfigItemType;
import pro.watchkit.wearable.watchface.model.ConfigData.PickerConfigItem;
import pro.watchkit.wearable.watchface.model.ConfigData.ToggleConfigItem;
import pro.watchkit.wearable.watchface.model.ConfigData.WatchFaceDrawableConfigItem;

/**
 * Displays different layouts for configuring watch face's complications and appearance settings
 * (highlight color [second arm], background color, unread notifications, etc.).
 *
 * <p>All appearance settings are saved via {@link SharedPreferences}.
 *
 * <p>Layouts provided by this adapter are split into 5 main view types.
 *
 * <p>A watch face preview including complications. Allows user to tap on the complications to
 * change the complication data and see a live preview of the watch face.
 *
 * <p>Simple arrow to indicate there are more options below the fold.
 *
 * <p>Color configuration options for both highlight (seconds hand) and background color.
 *
 * <p>Toggle for unread notifications.
 *
 * <p>Background image complication configuration for changing background image of watch face.
 */
public class ConfigRecyclerViewAdapter extends BaseRecyclerViewAdapter {

    public static final int TYPE_COLOR_PICKER_CONFIG = 0;
    public static final int TYPE_WATCH_FACE_DRAWABLE_CONFIG = 1;
    public static final int TYPE_COMPLICATION_CONFIG = 2;
    public static final int TYPE_PICKER_CONFIG = 3;
    public static final int TYPE_TOGGLE_CONFIG = 4;
    public static final int TYPE_CONFIG_ACTIVITY_CONFIG = 5;
    @NonNull
    private final List<ConfigItemType> mSettingsDataSet;
    private final List<WatchFaceStateListener> mWatchFaceStateListeners = new ArrayList<>();
    private final List<ComplicationProviderInfoListener> mComplicationProviderInfoListeners =
            new ArrayList<>();

    ConfigRecyclerViewAdapter(
            @NonNull Context context,
            @NonNull Class watchFaceServiceClass,
            @NonNull List<ConfigItemType> settingsDataSet) {
        super(context, watchFaceServiceClass);
        mSettingsDataSet = settingsDataSet;

        // Default value is invalid (only changed when user taps to change complication).
        mSelectedComplication = null;

        regenerateCurrentWatchFaceState();
    }

    @Override
    @NonNull
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ComplicationHolder.resetBaseId();

        RecyclerView.ViewHolder viewHolder;

        switch (viewType) {
            case TYPE_COLOR_PICKER_CONFIG: {
                viewHolder =
                        new ColorPickerViewHolder(
                                LayoutInflater.from(parent.getContext())
                                        .inflate(R.layout.config_list_color_item, parent, false));
                break;
            }

            case TYPE_CONFIG_ACTIVITY_CONFIG: {
                viewHolder =
                        new ConfigActivityViewHolder(
                                LayoutInflater.from(parent.getContext())
                                        .inflate(R.layout.config_list_color_item, parent, false));
                break;
            }

            case TYPE_WATCH_FACE_DRAWABLE_CONFIG: {
                viewHolder =
                        new WatchFaceDrawableViewHolder(
                                LayoutInflater.from(parent.getContext())
                                        .inflate(R.layout.watch_face_preset_config_list_item, parent, false));
                break;
            }

            case TYPE_COMPLICATION_CONFIG: {
                viewHolder =
                        new ComplicationViewHolder(
                                LayoutInflater.from(parent.getContext())
                                        .inflate(R.layout.watch_face_preset_config_list_item, parent, false));
                break;
            }

            case TYPE_PICKER_CONFIG: {
                viewHolder =
                        new PickerViewHolder(
                                LayoutInflater.from(parent.getContext())
                                        .inflate(R.layout.config_list_watch_face_preset_item, parent, false));
                break;
            }

            default: // Default case. Probably shouldn't happen.
            case TYPE_TOGGLE_CONFIG: {
                viewHolder =
                        new ToggleViewHolder(
                                LayoutInflater.from(parent.getContext())
                                        .inflate(R.layout.config_list_toggle, parent, false));
                break;
            }
        }

        if (viewHolder instanceof WatchFaceStateListener) {
            mWatchFaceStateListeners.add((WatchFaceStateListener) viewHolder);
        }

        if (viewHolder instanceof ComplicationProviderInfoListener) {
            mComplicationProviderInfoListeners.add((ComplicationProviderInfoListener) viewHolder);
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        // Pulls all data required for creating the UX for the specific setting option.
        ConfigItemType configItemType = mSettingsDataSet.get(position);

        switch (viewHolder.getItemViewType()) {
            case TYPE_COLOR_PICKER_CONFIG: {
                ColorPickerViewHolder colorPickerViewHolder = (ColorPickerViewHolder) viewHolder;
                ColorPickerConfigItem colorPickerConfigItem = (ColorPickerConfigItem) configItemType;
                colorPickerViewHolder.bind(colorPickerConfigItem);
                break;
            }

            case TYPE_CONFIG_ACTIVITY_CONFIG: {
                ConfigActivityViewHolder configActivityViewHolder = (ConfigActivityViewHolder) viewHolder;
                ConfigActivityConfigItem configActivityConfigItem = (ConfigActivityConfigItem) configItemType;
                configActivityViewHolder.bind(configActivityConfigItem);
                break;
            }

            case TYPE_WATCH_FACE_DRAWABLE_CONFIG: {
                WatchFaceDrawableViewHolder watchFaceDrawableViewHolder =
                        (WatchFaceDrawableViewHolder) viewHolder;
                WatchFaceDrawableConfigItem watchFaceDrawableConfigItem =
                        (WatchFaceDrawableConfigItem) configItemType;
                watchFaceDrawableViewHolder.setWatchFaceGlobalDrawableFlags(watchFaceDrawableConfigItem.getFlags());
                watchFaceDrawableViewHolder.onWatchFaceStateChanged();
                break;
            }

            case TYPE_COMPLICATION_CONFIG: {
                ComplicationViewHolder complicationViewHolder =
                        (ComplicationViewHolder) viewHolder;
                ComplicationConfigItem complicationConfigItem =
                        (ComplicationConfigItem) configItemType;

                int defaultComplicationResourceId =
                        complicationConfigItem.getDefaultComplicationResourceId();
                complicationViewHolder.setDefaultComplicationDrawable(
                        defaultComplicationResourceId);
                complicationViewHolder.bind(complicationConfigItem);
                break;
            }

            case TYPE_PICKER_CONFIG: {
                PickerViewHolder pickerViewHolder = (PickerViewHolder) viewHolder;
                PickerConfigItem pickerConfigItem = (PickerConfigItem) configItemType;
                pickerViewHolder.bind(pickerConfigItem);
                break;
            }

            case TYPE_TOGGLE_CONFIG: {
                ToggleViewHolder toggleViewHolder = (ToggleViewHolder) viewHolder;
                ToggleConfigItem toggleConfigItem = (ToggleConfigItem) configItemType;
                toggleViewHolder.bind(toggleConfigItem);
                break;
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        return mSettingsDataSet.get(position).getConfigType();
    }

    @Override
    public int getItemCount() {
        return mSettingsDataSet.size();
    }

    /**
     * Updates the selected complication id saved earlier with the new information.
     */
    void updateSelectedComplication(ComplicationProviderInfo complicationProviderInfo) {
        mComplicationProviderInfoListeners.forEach(
                c -> c.onComplicationProviderInfo(mSelectedComplication, complicationProviderInfo));
    }

    @Override
    void onWatchFaceStateChanged() {
        regenerateCurrentWatchFaceState();
        // Update our WatchFaceStateListener objects.
        mWatchFaceStateListeners.forEach(WatchFaceStateListener::onWatchFaceStateChanged);
    }
}
