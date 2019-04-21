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
import pro.watchkit.wearable.watchface.config.ColorSelectionActivity;
import pro.watchkit.wearable.watchface.config.ConfigActivity;
import pro.watchkit.wearable.watchface.config.ConfigRecyclerViewAdapter;
import pro.watchkit.wearable.watchface.config.WatchFacePresetSelectionActivity;
import pro.watchkit.wearable.watchface.watchface.AnalogComplicationWatchFaceService;

/**
 * Data represents different views for configuring the
 * {@link AnalogComplicationWatchFaceService} watch face's appearance and complications
 * via {@link ConfigActivity}.
 */
public class AnalogComplicationConfigData extends ConfigData {
    /**
     * Returns Watch Face Service class associated with configuration Activity.
     */
    @Override
    public Class getWatchFaceServiceClass() {
        return AnalogComplicationWatchFaceService.class;
    }

    /**
     * Includes all data to populate each of the 5 different custom
     * {@link ViewHolder} types in {@link ConfigRecyclerViewAdapter}.
     */
    @Override
    public ArrayList<ConfigItemType> getDataToPopulateAdapter(Context context) {

        ArrayList<ConfigItemType> settingsConfigData = new ArrayList<>();

        // Data for watch face preview and complications UX in settings Activity.
        ConfigItemType complicationConfigItem =
                new PreviewAndComplicationsConfigItem(R.drawable.add_complication);
        settingsConfigData.add(complicationConfigItem);

        // Data for "more options" UX in settings Activity.
        ConfigItemType moreOptionsConfigItem =
                new MoreOptionsConfigItem(R.drawable.ic_expand_more_white_18dp);
        settingsConfigData.add(moreOptionsConfigItem);

        // Data for fill color UX in settings Activity.
        settingsConfigData.add(new ColorPickerConfigItem(
                context.getString(R.string.config_fill_color_label),
                R.drawable.icn_styles,
                WatchFacePreset.ColorType.FILL,
//                context.getString(R.string.saved_fill_color),
                ColorSelectionActivity.class));

        // Data for accent color UX in settings Activity.
        settingsConfigData.add(new ColorPickerConfigItem(
                context.getString(R.string.config_accent_color_label),
                R.drawable.icn_styles,
                WatchFacePreset.ColorType.ACCENT,
//                context.getString(R.string.saved_accent_color),
                ColorSelectionActivity.class));

        // Data for highlight/marker (second hand) color UX in settings Activity.
        settingsConfigData.add(new ColorPickerConfigItem(
                context.getString(R.string.config_marker_color_label),
                R.drawable.icn_styles,
                WatchFacePreset.ColorType.HIGHLIGHT,
//                        context.getString(R.string.saved_marker_color),
                ColorSelectionActivity.class));

        // Data for base color UX in settings Activity.
        settingsConfigData.add(new ColorPickerConfigItem(
                context.getString(R.string.config_base_color_label),
                R.drawable.icn_styles,
                WatchFacePreset.ColorType.BASE,
//                context.getString(R.string.saved_base_color),
                ColorSelectionActivity.class));

        // Data for Configure Hands sub-activity in settings Activity.
        settingsConfigData.add(new ConfigActivityConfigItem(
                context.getString(R.string.config_configure_hands),
                R.drawable.icn_styles,
                WatchPartHandsConfigData.class,
                ConfigActivity.class));

        // Data for Configure Ticks sub-activity in settings Activity.
        settingsConfigData.add(new ConfigActivityConfigItem(
                context.getString(R.string.config_configure_ticks),
                R.drawable.icn_styles,
                WatchPartTicksConfigData.class,
                ConfigActivity.class));

        // Data for fill highlight style in settings Activity.
        settingsConfigData.add(new WatchFacePresetPickerConfigItem(
                context.getString(R.string.config_preset_fill_highlight_style),
                R.drawable.icn_styles,
                WatchFacePresetSelectionActivity.class,
                new WatchFacePresetMutatorGeneric<WatchFacePreset.GradientStyle>(WatchFacePreset.GradientStyle.values()) {

                    @Override
                    void permuteOne(WatchFacePreset permutation, WatchFacePreset.GradientStyle h) {
                        permutation.setFillHighlightStyle(h);
                    }

                    @Override
                    public WatchFacePreset.GradientStyle getCurrentValue(WatchFacePreset currentPreset) {
                        return currentPreset.getFillHighlightStyle();
                    }
                }));

        // Data for accent fill style in settings Activity.
        settingsConfigData.add(new WatchFacePresetPickerConfigItem(
                context.getString(R.string.config_preset_accent_fill_style),
                R.drawable.icn_styles,
                WatchFacePresetSelectionActivity.class,
                new WatchFacePresetMutatorGeneric<WatchFacePreset.GradientStyle>(WatchFacePreset.GradientStyle.values()) {

                    @Override
                    void permuteOne(WatchFacePreset permutation, WatchFacePreset.GradientStyle h) {
                        permutation.setAccentFillStyle(h);
                    }

                    @Override
                    public WatchFacePreset.GradientStyle getCurrentValue(WatchFacePreset currentPreset) {
                        return currentPreset.getAccentFillStyle();
                    }
                }));

        // Data for accent highlight style in settings Activity.
        settingsConfigData.add(new WatchFacePresetPickerConfigItem(
                context.getString(R.string.config_preset_accent_highlight_style),
                R.drawable.icn_styles,
                WatchFacePresetSelectionActivity.class,
                new WatchFacePresetMutatorGeneric<WatchFacePreset.GradientStyle>(WatchFacePreset.GradientStyle.values()) {

                    @Override
                    void permuteOne(WatchFacePreset permutation, WatchFacePreset.GradientStyle h) {
                        permutation.setAccentHighlightStyle(h);
                    }

                    @Override
                    public WatchFacePreset.GradientStyle getCurrentValue(WatchFacePreset currentPreset) {
                        return currentPreset.getAccentHighlightStyle();
                    }
                }));

        // Data for base accent style in settings Activity.
        settingsConfigData.add(new WatchFacePresetPickerConfigItem(
                context.getString(R.string.config_preset_base_accent_style),
                R.drawable.icn_styles,
                WatchFacePresetSelectionActivity.class,
                new WatchFacePresetMutatorGeneric<WatchFacePreset.GradientStyle>(WatchFacePreset.GradientStyle.values()) {

                    @Override
                    void permuteOne(WatchFacePreset permutation, WatchFacePreset.GradientStyle h) {
                        permutation.setBaseAccentStyle(h);
                    }

                    @Override
                    public WatchFacePreset.GradientStyle getCurrentValue(WatchFacePreset currentPreset) {
                        return currentPreset.getBaseAccentStyle();
                    }
                }));

        // Data for Background color UX in settings Activity.
//        ConfigItemType backgroundColorConfigItem =
//                new ColorPickerConfigItem(
//                        context.getString(R.string.config_background_color_label),
//                        R.drawable.icn_styles,
//                        context.getString(R.string.saved_background_color),
//                        ColorSelectionActivity.class);
//        settingsConfigData.add(backgroundColorConfigItem);

        // Data for 'Unread Notifications' UX (toggle) in settings Activity.
        ConfigItemType unreadNotificationsConfigItem =
                new UnreadNotificationConfigItem(
                        context.getString(R.string.config_unread_notifications_label),
                        R.drawable.ic_notifications_white_24dp,
                        R.drawable.ic_notifications_off_white_24dp,
                        R.string.saved_unread_notifications_pref);
        settingsConfigData.add(unreadNotificationsConfigItem);

        // Data for background complications UX in settings Activity.
        ConfigItemType backgroundImageComplicationConfigItem =
                // TODO (jewalker): Revised in another CL to support background complication.
                new BackgroundComplicationConfigItem(
                        context.getString(R.string.config_background_image_complication_label),
                        R.drawable.ic_landscape_white);
        settingsConfigData.add(backgroundImageComplicationConfigItem);

        // Data for 'Night Vision' UX (toggle) in settings Activity.
        ConfigItemType nightVisionConfigItem =
                new NightVisionConfigItem(
                        context.getString(R.string.config_night_vision_label),
                        R.drawable.ic_notifications_white_24dp,
                        R.drawable.ic_notifications_off_white_24dp,
                        R.string.saved_night_vision_pref);
        settingsConfigData.add(nightVisionConfigItem);

        return settingsConfigData;
    }
}
