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

package pro.watchkit.wearable.watchface.model;

import android.content.Context;

import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import java.util.ArrayList;

import pro.watchkit.wearable.watchface.R;
import pro.watchkit.wearable.watchface.config.ConfigActivity;
import pro.watchkit.wearable.watchface.config.ConfigRecyclerViewAdapter;
import pro.watchkit.wearable.watchface.config.WatchFaceSelectionActivity;
import pro.watchkit.wearable.watchface.watchface.AnalogComplicationWatchFaceService;

/**
 * Data represents different views for configuring the
 * {@link AnalogComplicationWatchFaceService} watch face's appearance and complications
 * via {@link ConfigActivity}.
 */
public class WatchPartHandsConfigData extends ConfigData {
    /**
     * Includes all data to populate each of the 5 different custom
     * {@link ViewHolder} types in {@link ConfigRecyclerViewAdapter}.
     */
    @Override
    public ArrayList<ConfigItemType> getDataToPopulateAdapter(Context context) {

        ArrayList<ConfigItemType> settingsConfigData = new ArrayList<>();

        // Data for hour hand shape in settings Activity.
        settingsConfigData.add(new WatchFacePickerConfigItem(
                context.getString(R.string.config_preset_hour_hand_shape),
                R.drawable.icn_styles,
                WatchFaceSelectionActivity.class,
                new WatchFacePresetMutatorImpl<>(
                        WatchFacePreset.HandShape.values(),
                        WatchFacePreset::setHourHandShape,
                        WatchFacePreset::getHourHandShape)));

        // Data for hour hand length in settings Activity.
        settingsConfigData.add(new WatchFacePickerConfigItem(
                context.getString(R.string.config_preset_hour_hand_length),
                R.drawable.icn_styles,
                WatchFaceSelectionActivity.class,
                new WatchFacePresetMutatorImpl<>(
                        WatchFacePreset.HandLength.values(),
                        WatchFacePreset::setHourHandLength,
                        WatchFacePreset::getHourHandLength)));

        // Data for hour hand thickness in settings Activity.
        settingsConfigData.add(new WatchFacePickerConfigItem(
                context.getString(R.string.config_preset_hour_hand_thickness),
                R.drawable.icn_styles,
                WatchFaceSelectionActivity.class,
                new WatchFacePresetMutatorImpl<>(
                        WatchFacePreset.HandThickness.values(),
                        WatchFacePreset::setHourHandThickness,
                        WatchFacePreset::getHourHandThickness)));

        // Data for hour hand stalk in settings Activity.
        settingsConfigData.add(new WatchFacePickerConfigItem(
                context.getString(R.string.config_preset_hour_hand_stalk),
                R.drawable.icn_styles,
                WatchFaceSelectionActivity.class,
                new WatchFacePresetMutatorImpl<>(
                        WatchFacePreset.HandStalk.values(),
                        WatchFacePreset::setHourHandStalk,
                        WatchFacePreset::getHourHandStalk)));

        // Data for hour hand cutout in settings Activity.
        settingsConfigData.add(new WatchFacePickerConfigItem(
                context.getString(R.string.config_preset_hour_hand_cutout),
                R.drawable.icn_styles,
                WatchFaceSelectionActivity.class,
                new WatchFacePresetMutatorImpl<>(
                        WatchFacePreset.HandCutout.values(),
                        WatchFacePreset::setHourHandCutout,
                        WatchFacePreset::getHourHandCutout)));

        // Data for hour hand style in settings Activity.
        settingsConfigData.add(new WatchFacePickerConfigItem(
                context.getString(R.string.config_preset_hour_hand_style),
                R.drawable.icn_styles,
                WatchFaceSelectionActivity.class,
                new WatchFacePresetMutatorImpl<>(
                        WatchFacePreset.Style.values(),
                        WatchFacePreset::setHourHandStyle,
                        WatchFacePreset::getHourHandStyle)));

        // Data for minute hand override in settings Activity.
        settingsConfigData.add(new WatchFacePresetToggleConfigItem(
                context.getString(R.string.config_preset_minute_hand_override),
                R.drawable.ic_notifications_white_24dp,
                R.drawable.ic_notifications_off_white_24dp,
                new WatchFacePresetMutator() {
                    @Override
                    public String[] permute(WatchFacePreset permutation) {
                        String[] result = new String[2];
                        permutation.setMinuteHandOverride(false);
                        result[0] = permutation.getString();
                        permutation.setMinuteHandOverride(true);
                        result[1] = permutation.getString();
                        return result;
                    }

                    public Enum getCurrentValue(WatchFacePreset currentPreset) {
                        return null;
                    }
                }));

        // Data for minute hand shape in settings Activity.
        settingsConfigData.add(new WatchFacePickerConfigItem(
                context.getString(R.string.config_preset_minute_hand_shape),
                R.drawable.icn_styles,
                WatchFaceSelectionActivity.class,
                new WatchFacePresetMutatorImpl<>(
                        WatchFacePreset.HandShape.values(),
                        WatchFacePreset::setMinuteHandShape,
                        WatchFacePreset::getMinuteHandShape)));

        // Data for minute hand length in settings Activity.
        settingsConfigData.add(new WatchFacePickerConfigItem(
                context.getString(R.string.config_preset_minute_hand_length),
                R.drawable.icn_styles,
                WatchFaceSelectionActivity.class,
                new WatchFacePresetMutatorImpl<>(
                        WatchFacePreset.HandShape.values(),
                        WatchFacePreset::setMinuteHandShape,
                        WatchFacePreset::getMinuteHandShape),
                WatchFacePreset::isMinuteHandOverridden));

        // Data for minute hand thickness in settings Activity.
        settingsConfigData.add(new WatchFacePickerConfigItem(
                context.getString(R.string.config_preset_minute_hand_thickness),
                R.drawable.icn_styles,
                WatchFaceSelectionActivity.class,
                new WatchFacePresetMutatorImpl<>(
                        WatchFacePreset.HandThickness.values(),
                        WatchFacePreset::setMinuteHandThickness,
                        WatchFacePreset::getMinuteHandThickness),
                WatchFacePreset::isMinuteHandOverridden));

        // Data for minute hand stalk in settings Activity.
        settingsConfigData.add(new WatchFacePickerConfigItem(
                context.getString(R.string.config_preset_minute_hand_stalk),
                R.drawable.icn_styles,
                WatchFaceSelectionActivity.class,
                new WatchFacePresetMutatorImpl<>(
                        WatchFacePreset.HandStalk.values(),
                        WatchFacePreset::setMinuteHandStalk,
                        WatchFacePreset::getMinuteHandStalk),
                WatchFacePreset::isMinuteHandOverridden));

        // Data for minute hand cutout in settings Activity.
        settingsConfigData.add(new WatchFacePickerConfigItem(
                context.getString(R.string.config_preset_minute_hand_cutout),
                R.drawable.icn_styles,
                WatchFaceSelectionActivity.class,
                new WatchFacePresetMutatorImpl<>(
                        WatchFacePreset.HandCutout.values(),
                        WatchFacePreset::setMinuteHandCutout,
                        WatchFacePreset::getMinuteHandCutout),
                WatchFacePreset::isMinuteHandOverridden));

        // Data for minute hand style in settings Activity.
        settingsConfigData.add(new WatchFacePickerConfigItem(
                context.getString(R.string.config_preset_minute_hand_style),
                R.drawable.icn_styles,
                WatchFaceSelectionActivity.class,
                new WatchFacePresetMutatorImpl<>(
                        WatchFacePreset.Style.values(),
                        WatchFacePreset::setMinuteHandStyle,
                        WatchFacePreset::getMinuteHandStyle),
                WatchFacePreset::isMinuteHandOverridden));

        // Data for second hand override in settings Activity.
        settingsConfigData.add(new WatchFacePresetToggleConfigItem(
                context.getString(R.string.config_preset_second_hand_override),
                R.drawable.ic_notifications_white_24dp,
                R.drawable.ic_notifications_off_white_24dp,
                new WatchFacePresetMutator() {
                    @Override
                    public String[] permute(WatchFacePreset permutation) {
                        String[] result = new String[2];
                        permutation.setSecondHandOverride(false);
                        result[0] = permutation.getString();
                        permutation.setSecondHandOverride(true);
                        result[1] = permutation.getString();
                        return result;
                    }

                    public Enum getCurrentValue(WatchFacePreset currentPreset) {
                        return null;
                    }
                }));

        // Data for second hand shape in settings Activity.
        settingsConfigData.add(new WatchFacePickerConfigItem(
                context.getString(R.string.config_preset_second_hand_shape),
                R.drawable.icn_styles,
                WatchFaceSelectionActivity.class,
                new WatchFacePresetMutatorImpl<>(
                        WatchFacePreset.HandShape.values(),
                        WatchFacePreset::setSecondHandShape,
                        WatchFacePreset::getSecondHandShape),
                WatchFacePreset::isSecondHandOverridden));

        // Data for second hand length in settings Activity.
        settingsConfigData.add(new WatchFacePickerConfigItem(
                context.getString(R.string.config_preset_second_hand_length),
                R.drawable.icn_styles,
                WatchFaceSelectionActivity.class,
                new WatchFacePresetMutatorImpl<>(
                        WatchFacePreset.HandLength.values(),
                        WatchFacePreset::setSecondHandLength,
                        WatchFacePreset::getSecondHandLength),
                WatchFacePreset::isSecondHandOverridden));


        // Data for second hand thickness in settings Activity.
        settingsConfigData.add(new WatchFacePickerConfigItem(
                context.getString(R.string.config_preset_second_hand_thickness),
                R.drawable.icn_styles,
                WatchFaceSelectionActivity.class,
                new WatchFacePresetMutatorImpl<>(
                        WatchFacePreset.HandThickness.values(),
                        WatchFacePreset::setSecondHandThickness,
                        WatchFacePreset::getSecondHandThickness),
                WatchFacePreset::isSecondHandOverridden));

        // Data for second hand style in settings Activity.
        settingsConfigData.add(new WatchFacePickerConfigItem(
                context.getString(R.string.config_preset_second_hand_style),
                R.drawable.icn_styles,
                WatchFaceSelectionActivity.class,
                new WatchFacePresetMutatorImpl<>(
                        WatchFacePreset.Style.values(),
                        WatchFacePreset::setSecondHandStyle,
                        WatchFacePreset::getSecondHandStyle),
                WatchFacePreset::isSecondHandOverridden));

        return settingsConfigData;
    }
}
