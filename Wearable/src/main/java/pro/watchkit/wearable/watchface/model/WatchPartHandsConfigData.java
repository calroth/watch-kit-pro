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

import java.util.ArrayList;

import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import pro.watchkit.wearable.watchface.R;
import pro.watchkit.wearable.watchface.config.ConfigActivity;
import pro.watchkit.wearable.watchface.config.ConfigRecyclerViewAdapter;
import pro.watchkit.wearable.watchface.config.WatchFacePresetSelectionActivity;
import pro.watchkit.wearable.watchface.watchface.AnalogComplicationWatchFaceService;

/**
 * Data represents different views for configuring the
 * {@link AnalogComplicationWatchFaceService} watch face's appearance and complications
 * via {@link ConfigActivity}.
 */
public class WatchPartHandsConfigData extends BaseConfigData {
    /**
     * Returns Watch Face Service class associated with configuration Activity.
     */
    @Override
    public Class getWatchFaceServiceClass() {
        return WatchPartHandsConfigData.class;
    }

    /**
     * Includes all data to populate each of the 5 different custom
     * {@link ViewHolder} types in {@link ConfigRecyclerViewAdapter}.
     */
    @Override
    public ArrayList<ConfigItemType> getDataToPopulateAdapter(Context context) {

        ArrayList<ConfigItemType> settingsConfigData = new ArrayList<>();

        // Data for hour hand shape in settings Activity.
        settingsConfigData.add(new WatchFacePresetPickerConfigItem(
                context.getString(R.string.config_preset_hour_hand_shape),
                R.drawable.icn_styles,
                WatchFacePresetSelectionActivity.class,
                new WatchFacePresetMutatorGeneric<WatchFacePreset.HandShape>(WatchFacePreset.HandShape.values()) {

                    @Override
                    void permuteOne(WatchFacePreset permutation, WatchFacePreset.HandShape h) {
                        permutation.setHourHandShape(h);
                    }

                    @Override
                    public WatchFacePreset.HandShape getCurrentValue(WatchFacePreset currentPreset) {
                        return currentPreset.getHourHandShape();
                    }
                }));

        // Data for hour hand length in settings Activity.
        settingsConfigData.add(new WatchFacePresetPickerConfigItem(
                context.getString(R.string.config_preset_hour_hand_length),
                R.drawable.icn_styles,
                WatchFacePresetSelectionActivity.class,
                new WatchFacePresetMutatorGeneric<WatchFacePreset.HandLength>(WatchFacePreset.HandLength.values()) {

                    @Override
                    void permuteOne(WatchFacePreset permutation, WatchFacePreset.HandLength h) {
                        permutation.setHourHandLength(h);
                    }

                    @Override
                    public WatchFacePreset.HandLength getCurrentValue(WatchFacePreset currentPreset) {
                        return currentPreset.getHourHandLength();
                    }
                }));

        // Data for hour hand thickness in settings Activity.
        settingsConfigData.add(new WatchFacePresetPickerConfigItem(
                context.getString(R.string.config_preset_hour_hand_thickness),
                R.drawable.icn_styles,
                WatchFacePresetSelectionActivity.class,
                new WatchFacePresetMutatorGeneric<WatchFacePreset.HandThickness>(WatchFacePreset.HandThickness.values()) {

                    @Override
                    void permuteOne(WatchFacePreset permutation, WatchFacePreset.HandThickness h) {
                        permutation.setHourHandThickness(h);
                    }

                    @Override
                    public WatchFacePreset.HandThickness getCurrentValue(WatchFacePreset currentPreset) {
                        return currentPreset.getHourHandThickness();
                    }
                }));

        // Data for hour hand stalk in settings Activity.
        settingsConfigData.add(new WatchFacePresetPickerConfigItem(
                context.getString(R.string.config_preset_hour_hand_stalk),
                R.drawable.icn_styles,
                WatchFacePresetSelectionActivity.class,
                new WatchFacePresetMutatorGeneric<WatchFacePreset.HandStalk>(WatchFacePreset.HandStalk.values()) {

                    @Override
                    void permuteOne(WatchFacePreset permutation, WatchFacePreset.HandStalk h) {
                        permutation.setHourHandStalk(h);
                    }

                    @Override
                    public WatchFacePreset.HandStalk getCurrentValue(WatchFacePreset currentPreset) {
                        return currentPreset.getHourHandStalk();
                    }
                }));

        // Data for hour hand cutout in settings Activity.
        settingsConfigData.add(new WatchFacePresetPickerConfigItem(
                context.getString(R.string.config_preset_hour_hand_cutout),
                R.drawable.icn_styles,
                WatchFacePresetSelectionActivity.class,
                new WatchFacePresetMutatorGeneric<WatchFacePreset.HandCutout>(WatchFacePreset.HandCutout.values()) {

                    @Override
                    void permuteOne(WatchFacePreset permutation, WatchFacePreset.HandCutout h) {
                        permutation.setHourHandCutout(h);
                    }

                    @Override
                    public WatchFacePreset.HandCutout getCurrentValue(WatchFacePreset currentPreset) {
                        return currentPreset.getHourHandCutout();
                    }
                }));

        // Data for hour hand style in settings Activity.
        settingsConfigData.add(new WatchFacePresetPickerConfigItem(
                context.getString(R.string.config_preset_hour_hand_style),
                R.drawable.icn_styles,
                WatchFacePresetSelectionActivity.class,
                new WatchFacePresetMutatorGeneric<WatchFacePreset.Style>(WatchFacePreset.Style.values()) {

                    @Override
                    void permuteOne(WatchFacePreset permutation, WatchFacePreset.Style h) {
                        permutation.setHourHandStyle(h);
                    }

                    @Override
                    public WatchFacePreset.Style getCurrentValue(WatchFacePreset currentPreset) {
                        return currentPreset.getHourHandStyle();
                    }
                }));

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
        settingsConfigData.add(new WatchFacePresetPickerConfigItem(
                context.getString(R.string.config_preset_minute_hand_shape),
                R.drawable.icn_styles,
                WatchFacePresetSelectionActivity.class,
                new WatchFacePresetMutatorGeneric<WatchFacePreset.HandShape>(WatchFacePreset.HandShape.values()) {

                    @Override
                    void permuteOne(WatchFacePreset permutation, WatchFacePreset.HandShape h) {
                        permutation.setMinuteHandShape(h);
                    }

                    @Override
                    public WatchFacePreset.HandShape getCurrentValue(WatchFacePreset currentPreset) {
                        return currentPreset.getMinuteHandShape();
                    }
                },
                new ConfigItemVisibilityCalculator() {
                    @Override
                    public boolean isVisible(WatchFacePreset currentPreset) {
                        return currentPreset.isMinuteHandOverridden();
                    }
                }));

        // Data for minute hand length in settings Activity.
        settingsConfigData.add(new WatchFacePresetPickerConfigItem(
                context.getString(R.string.config_preset_minute_hand_length),
                R.drawable.icn_styles,
                WatchFacePresetSelectionActivity.class,
                new WatchFacePresetMutatorGeneric<WatchFacePreset.HandLength>(WatchFacePreset.HandLength.values()) {

                    @Override
                    void permuteOne(WatchFacePreset permutation, WatchFacePreset.HandLength h) {
                        permutation.setMinuteHandLength(h);
                    }

                    @Override
                    public WatchFacePreset.HandLength getCurrentValue(WatchFacePreset currentPreset) {
                        return currentPreset.getMinuteHandLength();
                    }
                },
                new ConfigItemVisibilityCalculator() {
                    @Override
                    public boolean isVisible(WatchFacePreset currentPreset) {
                        return currentPreset.isMinuteHandOverridden();
                    }
                }));

        // Data for minute hand thickness in settings Activity.
        settingsConfigData.add(new WatchFacePresetPickerConfigItem(
                context.getString(R.string.config_preset_minute_hand_thickness),
                R.drawable.icn_styles,
                WatchFacePresetSelectionActivity.class,
                new WatchFacePresetMutatorGeneric<WatchFacePreset.HandThickness>(WatchFacePreset.HandThickness.values()) {

                    @Override
                    void permuteOne(WatchFacePreset permutation, WatchFacePreset.HandThickness h) {
                        permutation.setMinuteHandThickness(h);
                    }

                    @Override
                    public WatchFacePreset.HandThickness getCurrentValue(WatchFacePreset currentPreset) {
                        return currentPreset.getMinuteHandThickness();
                    }
                },
                new ConfigItemVisibilityCalculator() {
                    @Override
                    public boolean isVisible(WatchFacePreset currentPreset) {
                        return currentPreset.isMinuteHandOverridden();
                    }
                }));

        // Data for minute hand stalk in settings Activity.
        settingsConfigData.add(new WatchFacePresetPickerConfigItem(
                context.getString(R.string.config_preset_minute_hand_stalk),
                R.drawable.icn_styles,
                WatchFacePresetSelectionActivity.class,
                new WatchFacePresetMutatorGeneric<WatchFacePreset.HandStalk>(WatchFacePreset.HandStalk.values()) {

                    @Override
                    void permuteOne(WatchFacePreset permutation, WatchFacePreset.HandStalk h) {
                        permutation.setMinuteHandStalk(h);
                    }

                    @Override
                    public WatchFacePreset.HandStalk getCurrentValue(WatchFacePreset currentPreset) {
                        return currentPreset.getMinuteHandStalk();
                    }
                },
                new ConfigItemVisibilityCalculator() {
                    @Override
                    public boolean isVisible(WatchFacePreset currentPreset) {
                        return currentPreset.isMinuteHandOverridden();
                    }
                }));

        // Data for minute hand cutout in settings Activity.
        settingsConfigData.add(new WatchFacePresetPickerConfigItem(
                context.getString(R.string.config_preset_minute_hand_cutout),
                R.drawable.icn_styles,
                WatchFacePresetSelectionActivity.class,
                new WatchFacePresetMutatorGeneric<WatchFacePreset.HandCutout>(WatchFacePreset.HandCutout.values()) {

                    @Override
                    void permuteOne(WatchFacePreset permutation, WatchFacePreset.HandCutout h) {
                        permutation.setMinuteHandCutout(h);
                    }

                    @Override
                    public WatchFacePreset.HandCutout getCurrentValue(WatchFacePreset currentPreset) {
                        return currentPreset.getMinuteHandCutout();
                    }
                },
                new ConfigItemVisibilityCalculator() {
                    @Override
                    public boolean isVisible(WatchFacePreset currentPreset) {
                        return currentPreset.isMinuteHandOverridden();
                    }
                }));

        // Data for minute hand style in settings Activity.
        settingsConfigData.add(new WatchFacePresetPickerConfigItem(
                context.getString(R.string.config_preset_minute_hand_style),
                R.drawable.icn_styles,
                WatchFacePresetSelectionActivity.class,
                new WatchFacePresetMutatorGeneric<WatchFacePreset.Style>(WatchFacePreset.Style.values()) {

                    @Override
                    void permuteOne(WatchFacePreset permutation, WatchFacePreset.Style h) {
                        permutation.setMinuteHandStyle(h);
                    }

                    @Override
                    public WatchFacePreset.Style getCurrentValue(WatchFacePreset currentPreset) {
                        return currentPreset.getMinuteHandStyle();
                    }
                },
                new ConfigItemVisibilityCalculator() {
                    @Override
                    public boolean isVisible(WatchFacePreset currentPreset) {
                        return currentPreset.isMinuteHandOverridden();
                    }
                }));

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
        settingsConfigData.add(new WatchFacePresetPickerConfigItem(
                context.getString(R.string.config_preset_second_hand_shape),
                R.drawable.icn_styles,
                WatchFacePresetSelectionActivity.class,
                new WatchFacePresetMutatorGeneric<WatchFacePreset.HandShape>(WatchFacePreset.HandShape.values()) {

                    @Override
                    void permuteOne(WatchFacePreset permutation, WatchFacePreset.HandShape h) {
                        permutation.setSecondHandShape(h);
                    }

                    @Override
                    public WatchFacePreset.HandShape getCurrentValue(WatchFacePreset currentPreset) {
                        return currentPreset.getSecondHandShape();
                    }
                },
                new ConfigItemVisibilityCalculator() {
                    @Override
                    public boolean isVisible(WatchFacePreset currentPreset) {
                        return currentPreset.isSecondHandOverridden();
                    }
                }));

        // Data for second hand length in settings Activity.
        settingsConfigData.add(new WatchFacePresetPickerConfigItem(
                context.getString(R.string.config_preset_second_hand_length),
                R.drawable.icn_styles,
                WatchFacePresetSelectionActivity.class,
                new WatchFacePresetMutatorGeneric<WatchFacePreset.HandLength>(WatchFacePreset.HandLength.values()) {

                    @Override
                    void permuteOne(WatchFacePreset permutation, WatchFacePreset.HandLength h) {
                        permutation.setSecondHandLength(h);
                    }

                    @Override
                    public WatchFacePreset.HandLength getCurrentValue(WatchFacePreset currentPreset) {
                        return currentPreset.getSecondHandLength();
                    }
                },
                new ConfigItemVisibilityCalculator() {
                    @Override
                    public boolean isVisible(WatchFacePreset currentPreset) {
                        return currentPreset.isSecondHandOverridden();
                    }
                }));

        // Data for second hand thickness in settings Activity.
        settingsConfigData.add(new WatchFacePresetPickerConfigItem(
                context.getString(R.string.config_preset_second_hand_thickness),
                R.drawable.icn_styles,
                WatchFacePresetSelectionActivity.class,
                new WatchFacePresetMutatorGeneric<WatchFacePreset.HandThickness>(WatchFacePreset.HandThickness.values()) {

                    @Override
                    void permuteOne(WatchFacePreset permutation, WatchFacePreset.HandThickness h) {
                        permutation.setSecondHandThickness(h);
                    }

                    @Override
                    public WatchFacePreset.HandThickness getCurrentValue(WatchFacePreset currentPreset) {
                        return currentPreset.getSecondHandThickness();
                    }
                },
                new ConfigItemVisibilityCalculator() {
                    @Override
                    public boolean isVisible(WatchFacePreset currentPreset) {
                        return currentPreset.isSecondHandOverridden();
                    }
                }));

        // Data for second hand style in settings Activity.
        settingsConfigData.add(new WatchFacePresetPickerConfigItem(
                context.getString(R.string.config_preset_second_hand_style),
                R.drawable.icn_styles,
                WatchFacePresetSelectionActivity.class,
                new WatchFacePresetMutatorGeneric<WatchFacePreset.Style>(WatchFacePreset.Style.values()) {

                    @Override
                    void permuteOne(WatchFacePreset permutation, WatchFacePreset.Style h) {
                        permutation.setSecondHandStyle(h);
                    }

                    @Override
                    public WatchFacePreset.Style getCurrentValue(WatchFacePreset currentPreset) {
                        return currentPreset.getSecondHandStyle();
                    }
                },
                new ConfigItemVisibilityCalculator() {
                    @Override
                    public boolean isVisible(WatchFacePreset currentPreset) {
                        return currentPreset.isSecondHandOverridden();
                    }
                }));

        return settingsConfigData;
    }
}