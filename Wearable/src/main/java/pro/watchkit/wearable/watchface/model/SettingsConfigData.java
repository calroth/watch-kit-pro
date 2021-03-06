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
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import pro.watchkit.wearable.watchface.BuildConfig;
import pro.watchkit.wearable.watchface.R;
import pro.watchkit.wearable.watchface.config.ColorSelectionActivity;
import pro.watchkit.wearable.watchface.config.ConfigActivity;
import pro.watchkit.wearable.watchface.config.WatchFaceSelectionActivity;
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
                // Title.
                new LabelConfigItem(R.string.config_configure_settings),

                // Data for 'Unread Notifications' UX (toggle) in settings Activity.
                new ToggleConfigItem(
                        R.string.config_unread_notifications_label,
                        R.drawable.ic_notifications_white_24dp,
                        R.drawable.ic_notifications_off_white_24dp,
                        new BooleanMutator(WatchFaceState::setShowUnreadNotifications)),

                // Data for Configure Typeface sub-activity in settings Activity.
                new ConfigActivityConfigItem(
                        R.string.config_configure_typeface,
                        R.drawable.ic_settings,
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

                // Help.
                new LabelConfigItem(R.string.config_configure_help,
                        R.string.config_configure_settings_help),

                // About!
                new LabelConfigItem(R.string.config_about_heading, R.string.version_name),

                // Credits!
                new LabelConfigItem(R.string.config_credits_1, R.string.config_credits_2),

                // Australia Represent
                new LabelConfigItem(R.string.config_country_1, R.string.config_country_2),

                // Git hash.
                new LabelConfigItem(R.string.config_git_hash, R.string.git_hash),

                // Git date.
                new LabelConfigItem(R.string.config_git_date, R.string.git_date),

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
                        R.string.config_use_legacy_color_drawing_label,
                        R.drawable.ic_settings,
                        R.drawable.ic_settings,
                        new BooleanMutator(WatchFaceState::setUseLegacyColorDrawing),
                        WatchFaceState::isDeveloperMode),

                // Data for 'Sparkle Effect' in settings Activity.
                new ToggleConfigItem(
                        R.string.config_use_legacy_effects_label,
                        R.drawable.ic_settings,
                        R.drawable.ic_settings,
                        new BooleanMutator(WatchFaceState::setUseLegacyEffects),
                        WatchFaceState::isDeveloperMode),

                // Generate icon files.
                new LabelConfigItem(-1,
                        R.string.config_generate_icon_files,
                        watchFaceState -> BuildConfig.DEBUG && watchFaceState.isDeveloperMode()),

                new PickerConfigItem(
                        R.string.config_reset_to_default,
                        R.drawable.ic_settings,
                        WatchFaceGlobalDrawable.PART_BACKGROUND |
                                WatchFaceGlobalDrawable.PART_PIPS |
                                WatchFaceGlobalDrawable.PART_HANDS |
                                WatchFaceGlobalDrawable.PART_RINGS_ALL,
                        WatchFaceSelectionActivity.class,
                        new Mutator() {
                            /**
                             * A custom Mutator which "resets to default" by offering mutations
                             * corresponding to the default WatchFaceState for each slot
                             *
                             * @param permutation WatchFaceState, which must be a clone, but in
                             *                    this case we'll ignore it...
                             * @return Default WatchFaceState strings for all slots
                             */
                            @NonNull
                            @Override
                            public String[] permute(@NonNull WatchFaceState permutation) {
                                return new String[]{
                                        permutation.getStringResource(
                                                R.string.watch_kit_pro_a_default_string),
                                        permutation.getStringResource(
                                                R.string.watch_kit_pro_b_default_string),
                                        permutation.getStringResource(
                                                R.string.watch_kit_pro_c_default_string),
                                        permutation.getStringResource(
                                                R.string.watch_kit_pro_d_default_string)
                                };
                            }

                            @Nullable
                            @Override
                            public Enum<?> getCurrentValue(WatchFaceState currentPreset) {
                                return null;
                            }
                        },
                        WatchFaceState::isDeveloperMode),

                // Scripture.
                new LabelConfigItem(R.string.config_scripture_0, randomScriptureVerse)
        );
    }
}
