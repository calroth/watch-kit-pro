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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Arrays;
import java.util.List;

import pro.watchkit.wearable.watchface.R;
import pro.watchkit.wearable.watchface.config.ColorSelectionActivity;
import pro.watchkit.wearable.watchface.config.WatchFaceSelectionActivity;
import pro.watchkit.wearable.watchface.watchface.WatchFaceGlobalDrawable;

public class SettingsConfigData extends ConfigData {
    @Override
    public List<ConfigItemType> getDataToPopulateAdapter(@NonNull Context context) {
        return Arrays.asList(
                // Title.
                new LabelConfigItem(R.string.config_configure_settings),

                // Data for 'Unread Notifications' UX (toggle) in settings Activity.
                new ToggleConfigItem(
                        R.string.config_unread_notifications_label,
                        R.drawable.ic_notifications_white_24dp,
                        R.drawable.ic_notifications_off_white_24dp,
                        new BooleanMutator(WatchFaceState::setShowUnreadNotifications)),

                // Data for 'Night Vision' UX (toggle) in settings Activity.
                new ToggleConfigItem(
                        R.string.config_night_vision_label,
                        R.drawable.ic_notifications_white_24dp,
                        R.drawable.ic_notifications_off_white_24dp,
                        new BooleanMutator(WatchFaceState::setNightVisionModeEnabled)),

                // Data for base color UX in settings Activity.
                new ColorPickerConfigItem(
                        R.string.config_ambient_day_color_label,
                        R.drawable.icn_styles,
                        PaintBox.ColorType.AMBIENT_DAY,
                        ColorSelectionActivity.class),

                // Data for base color UX in settings Activity.
                new ColorPickerConfigItem(
                        R.string.config_ambient_night_color_label,
                        R.drawable.icn_styles,
                        PaintBox.ColorType.AMBIENT_NIGHT,
                        ColorSelectionActivity.class),

                // Data for 'Developer Mode' UX (toggle) in settings Activity.
                new ToggleConfigItem(
                        R.string.config_developer_mode_label,
                        R.drawable.ic_notifications_white_24dp,
                        R.drawable.ic_notifications_off_white_24dp,
                        new BooleanMutator(WatchFaceState::setDeveloperMode)),

                // Git hash.
                new LabelConfigItem(R.string.config_git_hash, R.string.git_hash,
                        WatchFaceState::isDeveloperMode),

                // Git date.
                new LabelConfigItem(R.string.config_git_date, R.string.git_date,
                        WatchFaceState::isDeveloperMode),

                // Data for 'Stats' UX (toggle) in settings Activity.
                new ToggleConfigItem(
                        R.string.config_stats_label,
                        R.drawable.ic_notifications_white_24dp,
                        R.drawable.ic_notifications_off_white_24dp,
                        new BooleanMutator(WatchFaceState::setStats),
                        WatchFaceState::isDeveloperMode),

                // Data for 'Stats (Detailed)' UX (toggle) in settings Activity.
                new ToggleConfigItem(
                        R.string.config_stats_detail_label,
                        R.drawable.ic_notifications_white_24dp,
                        R.drawable.ic_notifications_off_white_24dp,
                        new BooleanMutator(WatchFaceState::setStatsDetail),
                        WatchFaceState::isDeveloperMode),

                // Data for 'Hide Ticks' UX (toggle) in settings Activity.
                new ToggleConfigItem(
                        R.string.config_hide_ticks_label,
                        R.drawable.ic_notifications_white_24dp,
                        R.drawable.ic_notifications_off_white_24dp,
                        new BooleanMutator(WatchFaceState::setHideTicks),
                        WatchFaceState::isDeveloperMode),

                // Data for 'Hide Hands' UX (toggle) in settings Activity.
                new ToggleConfigItem(
                        R.string.config_hide_hands_label,
                        R.drawable.ic_notifications_white_24dp,
                        R.drawable.ic_notifications_off_white_24dp,
                        new BooleanMutator(WatchFaceState::setHideHands),
                        WatchFaceState::isDeveloperMode),

                // Data for 'Alt Drawing' in settings Activity.
                new ToggleConfigItem(
                        R.string.config_alt_drawing_label,
                        R.drawable.ic_notifications_white_24dp,
                        R.drawable.ic_notifications_off_white_24dp,
                        new BooleanMutator(WatchFaceState::setAltDrawing),
                        WatchFaceState::isDeveloperMode),

                new PickerConfigItem(
                        R.string.config_reset_to_default,
                        R.drawable.icn_styles,
                        WatchFaceGlobalDrawable.PART_BACKGROUND |
                                WatchFaceGlobalDrawable.PART_TICKS |
                                WatchFaceGlobalDrawable.PART_HANDS |
                                WatchFaceGlobalDrawable.PART_RINGS_ALL,
                        WatchFaceSelectionActivity.class,
                        new Mutator() {
                            /**
                             * A custom Mutotor which "resets to default" by offering mutations
                             * corresponding to the default WatchFaceState for each slot
                             *
                             * @param permutation WatchFaceState, which must be a clone, but in
                             *                    this case we'll ignore it...
                             * @return Default WatchFaceState strings for all slots
                             */
                            @Override
                            public String[] permute(WatchFaceState permutation) {
                                return new String[]{
                                        context.getString(R.string.watch_kit_pro_a_default_string),
                                        context.getString(R.string.watch_kit_pro_b_default_string),
                                        context.getString(R.string.watch_kit_pro_c_default_string),
                                        context.getString(R.string.watch_kit_pro_d_default_string)
                                };
                            }

                            @Nullable
                            @Override
                            public Enum getCurrentValue(WatchFaceState currentPreset) {
                                return null;
                            }
                        },
                        WatchFaceState::isDeveloperMode)
                );
    }
}
