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

import java.util.Arrays;
import java.util.List;

import pro.watchkit.wearable.watchface.R;
import pro.watchkit.wearable.watchface.config.ColorSelectionActivity;

public class SettingsConfigData extends ConfigData {
    @Override
    public List<ConfigItemType> getDataToPopulateAdapter(Context context) {
        return Arrays.asList(
                // Data for 'Unread Notifications' UX (toggle) in settings Activity.
                new UnreadNotificationConfigItem(
                        context.getString(R.string.config_unread_notifications_label),
                        R.drawable.ic_notifications_white_24dp,
                        R.drawable.ic_notifications_off_white_24dp),

                // Data for 'Night Vision' UX (toggle) in settings Activity.
                new NightVisionConfigItem(
                        context.getString(R.string.config_night_vision_label),
                        R.drawable.ic_notifications_white_24dp,
                        R.drawable.ic_notifications_off_white_24dp),

                // Data for base color UX in settings Activity.
                new ColorPickerConfigItem(
                        context.getString(R.string.config_ambient_day_color_label),
                        R.drawable.icn_styles,
                        PaintBox.ColorType.AMBIENT_DAY,
                        ColorSelectionActivity.class),

                // Data for base color UX in settings Activity.
                new ColorPickerConfigItem(
                        context.getString(R.string.config_ambient_night_color_label),
                        R.drawable.icn_styles,
                        PaintBox.ColorType.AMBIENT_NIGHT,
                        ColorSelectionActivity.class),

                // Data for developer mode in settings Activity.
                new ToggleConfigItem<>(
                        context.getString(R.string.config_developer_mode_label),
                        R.drawable.ic_notifications_white_24dp,
                        R.drawable.ic_notifications_off_white_24dp,
                        new Mutator<Settings>() {
                            @Override
                            public String[] permute(Settings permutation) {
                                String[] result = new String[2];
                                permutation.setDeveloperMode(false);
                                result[0] = permutation.getString();
                                permutation.setDeveloperMode(true);
                                result[1] = permutation.getString();
                                return result;
                            }

                            public Enum getCurrentValue(Settings currentPreset) {
                                return null;
                            }
                        }),

                // Data for stats mode in settings Activity.
                new ToggleConfigItem<>(
                        context.getString(R.string.config_stats_label),
                        R.drawable.ic_notifications_white_24dp,
                        R.drawable.ic_notifications_off_white_24dp,
                        new Mutator<Settings>() {
                            @Override
                            public String[] permute(Settings permutation) {
                                String[] result = new String[2];
                                permutation.setStats(false);
                                result[0] = permutation.getString();
                                permutation.setStats(true);
                                result[1] = permutation.getString();
                                return result;
                            }

                            public Enum getCurrentValue(Settings currentPreset) {
                                return null;
                            }
                        }),

                // Data for stats detail mode in settings Activity.
                new ToggleConfigItem<>(
                        context.getString(R.string.config_stats_detail_label),
                        R.drawable.ic_notifications_white_24dp,
                        R.drawable.ic_notifications_off_white_24dp,
                        new Mutator<Settings>() {
                            @Override
                            public String[] permute(Settings permutation) {
                                String[] result = new String[2];
                                permutation.setStatsDetail(false);
                                result[0] = permutation.getString();
                                permutation.setStatsDetail(true);
                                result[1] = permutation.getString();
                                return result;
                            }

                            public Enum getCurrentValue(Settings currentPreset) {
                                return null;
                            }
                        })
        );
    }
}
