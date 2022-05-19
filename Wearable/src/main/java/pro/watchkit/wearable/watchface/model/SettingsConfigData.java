/*
 * Copyright (C) 2018-2021 Terence Tan
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

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import pro.watchkit.wearable.watchface.BuildConfig;
import pro.watchkit.wearable.watchface.R;
import pro.watchkit.wearable.watchface.config.ColorSelectionActivity;
import pro.watchkit.wearable.watchface.config.ConfigActivity;
import pro.watchkit.wearable.watchface.config.WatchFaceSelectionActivity;
import pro.watchkit.wearable.watchface.util.SharedPref;
import pro.watchkit.wearable.watchface.watchface.WatchFaceGlobalDrawable;

public class SettingsConfigData extends ConfigData {
    @NonNull
    @Override
    public List<ConfigItemType> getDataToPopulateAdapter() {
        @StringRes int[] scriptureVerses = new int[]{
                R.string.config_scripture_1,
                R.string.config_scripture_2,
                R.string.config_scripture_3,
                R.string.config_scripture_4};
        @StringRes int randomScriptureVerse =
                scriptureVerses[new Random().nextInt(scriptureVerses.length)];
        return Arrays.asList(
                // Heading.
                new HeadingLabelConfigItem(R.string.config_configure_settings),

                // Data for 'Unread Notifications' UX (toggle) in settings Activity.
                new ToggleConfigItem(
                        R.string.config_unread_notifications_label,
                        R.drawable.ic_notifications,
                        R.drawable.ic_notifications_off,
                        new BooleanMutator(WatchFaceState::setShowUnreadNotifications)),

                // Data for Configure Typeface sub-activity in settings Activity.
                new ConfigActivityConfigItem(
                        R.string.config_configure_typeface,
                        R.drawable.ic_font,
                        TypefaceConfigData.class,
                        ConfigActivity.class),

                // Data for base color UX in settings Activity.
                new ColorPickerConfigItem(
                        R.string.config_ambient_day_color_label,
                        R.drawable.ic_color_lens,
                        PaintBox.ColorType.AMBIENT_DAY,
                        ColorSelectionActivity.class),

                // Data for base color UX in settings Activity.
                new ColorPickerConfigItem(
                        R.string.config_ambient_night_color_label,
                        R.drawable.ic_color_lens,
                        PaintBox.ColorType.AMBIENT_NIGHT,
                        ColorSelectionActivity.class),

                // Second Hand in Ambient Mode
                new ToggleConfigItem(
                        R.string.config_ambient_second_label,
                        R.string.config_ambient_second_alert_message,
                        R.drawable.ic_settings_outline,
                        R.drawable.ic_settings,
                        new BooleanMutator(WatchFaceState::setUseDecomposition),
                        SharedPref::isOffloadSupported),

                // Help.
                new HelpLabelConfigItem(R.string.config_configure_settings_help),

                // About!
                new TitleLabelConfigItem(R.string.config_about_heading, R.string.version_name),

                // Credits!
                new TitleLabelConfigItem(R.string.config_credits_1, R.string.config_credits_2),

                // Australia Represent
                new TitleLabelConfigItem(R.string.config_country_1, R.string.config_country_2),

                // Git hash.
                new TitleLabelConfigItem(
                        BuildConfig.DEBUG ? R.string.config_git_debug : R.string.config_git_hash,
                        R.string.git_hash),

                // Git date.
                new TitleLabelConfigItem(R.string.config_git_date, R.string.git_date),

                // Data for 'Developer Mode' UX (toggle) in settings Activity.
                new ToggleConfigItem(
                        R.string.config_developer_mode_label,
                        R.drawable.ic_settings,
                        R.drawable.ic_settings,
                        new BooleanMutator(WatchFaceState::setDeveloperMode),
                        WatchFaceState::isDeveloperMode),

                // Data for 'Stats' UX (toggle) in settings Activity.
                new ToggleConfigItem(
                        R.string.config_stats_label,
                        R.drawable.ic_settings,
                        R.drawable.ic_settings,
                        new BooleanMutator(WatchFaceState::setStats),
                        WatchFaceState::isDeveloperMode),

                // Data for 'Stats (Detailed)' UX (toggle) in settings Activity.
                new ToggleConfigItem(
                        R.string.config_stats_detail_label,
                        R.drawable.ic_settings,
                        R.drawable.ic_settings,
                        new BooleanMutator(WatchFaceState::setStatsDetail),
                        WatchFaceState::isDeveloperMode),

                // Data for 'Hide Pips' UX (toggle) in settings Activity.
                new ToggleConfigItem(
                        R.string.config_hide_pips_label,
                        R.drawable.ic_settings,
                        R.drawable.ic_settings,
                        new BooleanMutator(WatchFaceState::setHidePips),
                        WatchFaceState::isDeveloperMode),

                // Data for 'Hide Hands' UX (toggle) in settings Activity.
                new ToggleConfigItem(
                        R.string.config_hide_hands_label,
                        R.drawable.ic_settings,
                        R.drawable.ic_settings,
                        new BooleanMutator(WatchFaceState::setHideHands),
                        WatchFaceState::isDeveloperMode),

                // Data for 'Alt Drawing' in settings Activity.
                new ToggleConfigItem(
                        R.string.config_use_legacy_material_drawing_label,
                        R.drawable.ic_settings,
                        R.drawable.ic_settings,
                        new BooleanMutator(WatchFaceState::setUseLegacyMaterialDrawing),
                        WatchFaceState::isDeveloperMode),

                // Data for 'Sparkle Effect' in settings Activity.
                new ToggleConfigItem(
                        R.string.config_use_legacy_effects_label,
                        R.drawable.ic_settings,
                        R.drawable.ic_settings,
                        new BooleanMutator(WatchFaceState::setUseLegacyEffects),
                        WatchFaceState::isDeveloperMode),

                // Generate icon files.
                new LabelConfigItem(R.string.config_generate_icon_files,
                        watchFaceState -> BuildConfig.DEBUG && watchFaceState.isDeveloperMode()),

                new PickerConfigItem(
                        R.string.config_reset_to_default,
                        R.drawable.ic_settings,
                        WatchFaceGlobalDrawable.PART_BACKGROUND |
                                WatchFaceGlobalDrawable.PART_PIPS |
                                WatchFaceGlobalDrawable.PART_HANDS |
                                WatchFaceGlobalDrawable.PART_RINGS_ALL,
                        WatchFaceSelectionActivity.class,
                        w -> new Permutation[]{
                                /*
                                 * A custom Mutator which "resets to default" by offering mutations
                                 * corresponding to the default WatchFaceState for each slot
                                 */
                                new Permutation(
                                        w.getStringResource(
                                                R.string.watch_kit_pro_a_default_string),
                                        w.getStringResource(
                                                R.string.watch_face_service_label_a)),
                                new Permutation(
                                        w.getStringResource(
                                                R.string.watch_kit_pro_b_default_string),
                                        w.getStringResource(
                                                R.string.watch_face_service_label_b)),
                                new Permutation(
                                        w.getStringResource(
                                                R.string.watch_kit_pro_c_default_string),
                                        w.getStringResource(
                                                R.string.watch_face_service_label_c)),
                                new Permutation(
                                        w.getStringResource(
                                                R.string.watch_kit_pro_d_default_string),
                                        w.getStringResource(
                                                R.string.watch_face_service_label_d))
                        },
                        WatchFaceState::isDeveloperMode),

                // Scripture.
                new TitleLabelConfigItem(R.string.config_scripture_0, randomScriptureVerse)
        );
    }
}
