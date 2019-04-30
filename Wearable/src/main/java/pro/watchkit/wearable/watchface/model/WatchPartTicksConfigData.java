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
import pro.watchkit.wearable.watchface.config.WatchFacePresetSelectionActivity;
import pro.watchkit.wearable.watchface.watchface.AnalogComplicationWatchFaceService;

/**
 * Data represents different views for configuring the
 * {@link AnalogComplicationWatchFaceService} watch face's appearance and complications
 * via {@link ConfigActivity}.
 */
public class WatchPartTicksConfigData extends ConfigData {
    /**
     * Returns Watch Face Service class associated with configuration Activity.
     */
    @Override
    public Class getWatchFaceServiceClass() {
        return WatchPartTicksConfigData.class;
    }

    /**
     * Includes all data to populate each of the 5 different custom
     * {@link ViewHolder} types in {@link ConfigRecyclerViewAdapter}.
     */
    @Override
    public ArrayList<ConfigItemType> getDataToPopulateAdapter(Context context) {

        ArrayList<ConfigItemType> settingsConfigData = new ArrayList<>();

        // Data for ticks display in settings Activity.
        settingsConfigData.add(new WatchFacePresetPickerConfigItem(
                context.getString(R.string.config_preset_ticks_display),
                R.drawable.icn_styles,
                WatchFacePresetSelectionActivity.class,
                new WatchFacePresetMutatorImpl<>(
                        WatchFacePreset.TicksDisplay.values(),
                        WatchFacePreset::setTicksDisplay,
                        WatchFacePreset::getTicksDisplay)));

        // Data for four tick shape in settings Activity.
        settingsConfigData.add(new WatchFacePresetPickerConfigItem(
                context.getString(R.string.config_preset_four_tick_shape),
                R.drawable.icn_styles,
                WatchFacePresetSelectionActivity.class,
                new WatchFacePresetMutatorImpl<>(
                        WatchFacePreset.TickShape.values(),
                        WatchFacePreset::setFourTickShape,
                        WatchFacePreset::getFourTickShape),
                WatchFacePreset::isFourTicksVisible));

        // Data for four tick length in settings Activity.
        settingsConfigData.add(new WatchFacePresetPickerConfigItem(
                context.getString(R.string.config_preset_four_tick_length),
                R.drawable.icn_styles,
                WatchFacePresetSelectionActivity.class,
                new WatchFacePresetMutatorImpl<>(
                        WatchFacePreset.TickLength.values(),
                        WatchFacePreset::setFourTickLength,
                        WatchFacePreset::getFourTickLength),
                WatchFacePreset::isFourTicksVisible));

        // Data for four tick thickness in settings Activity.
        settingsConfigData.add(new WatchFacePresetPickerConfigItem(
                context.getString(R.string.config_preset_four_tick_thickness),
                R.drawable.icn_styles,
                WatchFacePresetSelectionActivity.class,
                new WatchFacePresetMutatorImpl<>(
                        WatchFacePreset.TickThickness.values(),
                        WatchFacePreset::setFourTickThickness,
                        WatchFacePreset::getFourTickThickness),
                WatchFacePreset::isFourTicksVisible));

        // Data for four tick radius position in settings Activity.
        settingsConfigData.add(new WatchFacePresetPickerConfigItem(
                context.getString(R.string.config_preset_four_tick_radius_position),
                R.drawable.icn_styles,
                WatchFacePresetSelectionActivity.class,
                new WatchFacePresetMutatorImpl<>(
                        WatchFacePreset.TickRadiusPosition.values(),
                        WatchFacePreset::setFourTickRadiusPosition,
                        WatchFacePreset::getFourTickRadiusPosition),
                WatchFacePreset::isFourTicksVisible));

        // Data for four tick style in settings Activity.
        settingsConfigData.add(new WatchFacePresetPickerConfigItem(
                context.getString(R.string.config_preset_four_tick_style),
                R.drawable.icn_styles,
                WatchFacePresetSelectionActivity.class,
                new WatchFacePresetMutatorImpl<>(
                        WatchFacePreset.Style.values(),
                        WatchFacePreset::setFourTickStyle,
                        WatchFacePreset::getFourTickStyle),
                WatchFacePreset::isFourTicksVisible));

        // Data for twelve tick override in settings Activity.
        settingsConfigData.add(new WatchFacePresetToggleConfigItem(
                context.getString(R.string.config_preset_twelve_tick_override),
                R.drawable.ic_notifications_white_24dp,
                R.drawable.ic_notifications_off_white_24dp,
                new WatchFacePresetMutator() {
                    @Override
                    public String[] permute(WatchFacePreset permutation) {
                        String[] result = new String[2];
                        permutation.setTwelveTickOverride(false);
                        result[0] = permutation.getString();
                        permutation.setTwelveTickOverride(true);
                        result[1] = permutation.getString();
                        return result;
                    }

                    public Enum getCurrentValue(WatchFacePreset currentPreset) {
                        return null;
                    }
                }));

        // Data for twelve tick shape in settings Activity.
        settingsConfigData.add(new WatchFacePresetPickerConfigItem(
                context.getString(R.string.config_preset_twelve_tick_shape),
                R.drawable.icn_styles,
                WatchFacePresetSelectionActivity.class,
                new WatchFacePresetMutatorImpl<>(
                                  WatchFacePreset.TickShape.values(),
                        WatchFacePreset::setTwelveTickShape,
                        WatchFacePreset::getTwelveTickShape),
                WatchFacePreset::isTwelveTicksOverridden));

        // Data for twelve tick length in settings Activity.
        settingsConfigData.add(new WatchFacePresetPickerConfigItem(
                context.getString(R.string.config_preset_twelve_tick_length),
                R.drawable.icn_styles,
                WatchFacePresetSelectionActivity.class,
                new WatchFacePresetMutatorImpl<>(
                        WatchFacePreset.TickLength.values(),
                        WatchFacePreset::setTwelveTickLength,
                        WatchFacePreset::getTwelveTickLength),
                WatchFacePreset::isTwelveTicksOverridden));

        // Data for twelve tick thickness in settings Activity.
        settingsConfigData.add(new WatchFacePresetPickerConfigItem(
                context.getString(R.string.config_preset_twelve_tick_thickness),
                R.drawable.icn_styles,
                WatchFacePresetSelectionActivity.class,
                new WatchFacePresetMutatorImpl<>(
                        WatchFacePreset.TickThickness.values(),
                        WatchFacePreset::setTwelveTickThickness,
                        WatchFacePreset::getTwelveTickThickness),
                WatchFacePreset::isTwelveTicksOverridden));

        // Data for twelve tick radius position in settings Activity.
        settingsConfigData.add(new WatchFacePresetPickerConfigItem(
                context.getString(R.string.config_preset_twelve_tick_radius_position),
                R.drawable.icn_styles,
                WatchFacePresetSelectionActivity.class,
                new WatchFacePresetMutatorImpl<>(
                        WatchFacePreset.TickRadiusPosition.values(),
                        WatchFacePreset::setTwelveTickRadiusPosition,
                        WatchFacePreset::getTwelveTickRadiusPosition),
                WatchFacePreset::isTwelveTicksOverridden));

        // Data for twelve tick style in settings Activity.
        settingsConfigData.add(new WatchFacePresetPickerConfigItem(
                context.getString(R.string.config_preset_twelve_tick_style),
                R.drawable.icn_styles,
                WatchFacePresetSelectionActivity.class,
                new WatchFacePresetMutatorImpl<>(
                        WatchFacePreset.Style.values(),
                        WatchFacePreset::setTwelveTickStyle,
                        WatchFacePreset::getTwelveTickStyle),
                WatchFacePreset::isTwelveTicksOverridden));

        // Data for sixty tick override in settings Activity.
        settingsConfigData.add(new WatchFacePresetToggleConfigItem(
                context.getString(R.string.config_preset_sixty_tick_override),
                R.drawable.ic_notifications_white_24dp,
                R.drawable.ic_notifications_off_white_24dp,
                new WatchFacePresetMutator() {
                    @Override
                    public String[] permute(WatchFacePreset permutation) {
                        String[] result = new String[2];
                        permutation.setSixtyTickOverride(false);
                        result[0] = permutation.getString();
                        permutation.setSixtyTickOverride(true);
                        result[1] = permutation.getString();
                        return result;
                    }

                    public Enum getCurrentValue(WatchFacePreset currentPreset) {
                        return null;
                    }
                }));

        // Data for sixty tick shape in settings Activity.
        settingsConfigData.add(new WatchFacePresetPickerConfigItem(
                context.getString(R.string.config_preset_sixty_tick_shape),
                R.drawable.icn_styles,
                WatchFacePresetSelectionActivity.class,
                new WatchFacePresetMutatorImpl<>(
                        WatchFacePreset.TickShape.values(),
                        WatchFacePreset::setSixtyTickShape,
                        WatchFacePreset::getSixtyTickShape),
                WatchFacePreset::isSixtyTicksOverridden));

        // Data for sixty tick length in settings Activity.
        settingsConfigData.add(new WatchFacePresetPickerConfigItem(
                context.getString(R.string.config_preset_sixty_tick_length),
                R.drawable.icn_styles,
                WatchFacePresetSelectionActivity.class,
                new WatchFacePresetMutatorImpl<>(
                        WatchFacePreset.TickLength.values(),
                        WatchFacePreset::setSixtyTickLength,
                        WatchFacePreset::getSixtyTickLength),
                WatchFacePreset::isSixtyTicksOverridden));

        // Data for sixty tick thickness in settings Activity.
        settingsConfigData.add(new WatchFacePresetPickerConfigItem(
                context.getString(R.string.config_preset_sixty_tick_thickness),
                R.drawable.icn_styles,
                WatchFacePresetSelectionActivity.class,
                new WatchFacePresetMutatorImpl<>(
                        WatchFacePreset.TickThickness.values(),
                        WatchFacePreset::setSixtyTickThickness,
                        WatchFacePreset::getSixtyTickThickness),
                WatchFacePreset::isSixtyTicksOverridden));

        // Data for sixty tick radius position in settings Activity.
        settingsConfigData.add(new WatchFacePresetPickerConfigItem(
                context.getString(R.string.config_preset_sixty_tick_radius_position),
                R.drawable.icn_styles,
                WatchFacePresetSelectionActivity.class,
                new WatchFacePresetMutatorImpl<>(
                        WatchFacePreset.TickRadiusPosition.values(),
                        WatchFacePreset::setSixtyTickRadiusPosition,
                        WatchFacePreset::getSixtyTickRadiusPosition),
                WatchFacePreset::isSixtyTicksOverridden));

        // Data for sixty tick style in settings Activity.
        settingsConfigData.add(new WatchFacePresetPickerConfigItem(
                context.getString(R.string.config_preset_sixty_tick_style),
                R.drawable.icn_styles,
                WatchFacePresetSelectionActivity.class,
                new WatchFacePresetMutatorImpl<>(
                        WatchFacePreset.Style.values(),
                        WatchFacePreset::setSixtyTickStyle,
                        WatchFacePreset::getSixtyTickStyle),
                WatchFacePreset::isSixtyTicksOverridden));

        return settingsConfigData;
    }
}
