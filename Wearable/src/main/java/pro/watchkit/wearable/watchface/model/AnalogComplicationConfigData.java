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
import android.text.Html;

import java.util.ArrayList;

import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import pro.watchkit.wearable.watchface.R;
import pro.watchkit.wearable.watchface.config.AnalogComplicationConfigActivity;
import pro.watchkit.wearable.watchface.config.AnalogComplicationConfigRecyclerViewAdapter;
import pro.watchkit.wearable.watchface.config.ColorSelectionActivity;
import pro.watchkit.wearable.watchface.config.WatchFacePresetSelectionActivity;
import pro.watchkit.wearable.watchface.watchface.AnalogComplicationWatchFaceService;

/**
 * Data represents different views for configuring the
 * {@link AnalogComplicationWatchFaceService} watch face's appearance and complications
 * via {@link AnalogComplicationConfigActivity}.
 */
public class AnalogComplicationConfigData {


    /**
     * Returns Watch Face Service class associated with configuration Activity.
     */
    public static Class getWatchFaceServiceClass() {
        return AnalogComplicationWatchFaceService.class;
    }

    /**
     * Includes all data to populate each of the 5 different custom
     * {@link ViewHolder} types in {@link AnalogComplicationConfigRecyclerViewAdapter}.
     */
    public static ArrayList<ConfigItemType> getDataToPopulateAdapter(Context context) {

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

        // Data for hour hand shape in settings Activity.
        settingsConfigData.add(new WatchFacePresetPickerConfigItem(
                context.getString(R.string.config_preset_hour_hand_shape),
                R.drawable.icn_styles,
                WatchFacePresetSelectionActivity.class,
                new WatchFacePresetMutator() {
                    @Override
                    public String[] permute(WatchFacePreset permutation) {
                        String[] result = new String[WatchFacePreset.HandShape.values().length];
                        int i = 0;
                        for (WatchFacePreset.HandShape h : WatchFacePreset.HandShape.values()) {
                            permutation.setHourHandShape(h);
                            result[i++] = permutation.getString();
                        }
                        return result;
                    }

                    public Enum getCurrentValue(WatchFacePreset currentPreset) {
                        return currentPreset.getHourHandShape();
                    }
                }));

        // Data for hour hand length in settings Activity.
        settingsConfigData.add(new WatchFacePresetPickerConfigItem(
                context.getString(R.string.config_preset_hour_hand_length),
                R.drawable.icn_styles,
                WatchFacePresetSelectionActivity.class,
                new WatchFacePresetMutator() {
                    @Override
                    public String[] permute(WatchFacePreset permutation) {
                        String[] result = new String[WatchFacePreset.HandLength.values().length];
                        int i = 0;
                        for (WatchFacePreset.HandLength h : WatchFacePreset.HandLength.values()) {
                            permutation.setHourHandLength(h);
                            result[i++] = permutation.getString();
                        }
                        return result;
                    }

                    public Enum getCurrentValue(WatchFacePreset currentPreset) {
                        return currentPreset.getHourHandLength();
                    }
                }));

        // Data for hour hand thickness in settings Activity.
        settingsConfigData.add(new WatchFacePresetPickerConfigItem(
                context.getString(R.string.config_preset_hour_hand_thickness),
                R.drawable.icn_styles,
                WatchFacePresetSelectionActivity.class,
                new WatchFacePresetMutator() {
                    @Override
                    public String[] permute(WatchFacePreset permutation) {
                        String[] result = new String[WatchFacePreset.HandThickness.values().length];
                        int i = 0;
                        for (WatchFacePreset.HandThickness h : WatchFacePreset.HandThickness.values()) {
                            permutation.setHourHandThickness(h);
                            result[i++] = permutation.getString();
                        }
                        return result;
                    }

                    public Enum getCurrentValue(WatchFacePreset currentPreset) {
                        return currentPreset.getHourHandThickness();
                    }
                }));

        // Data for hour hand stalk in settings Activity.
        settingsConfigData.add(new WatchFacePresetPickerConfigItem(
                context.getString(R.string.config_preset_hour_hand_stalk),
                R.drawable.icn_styles,
                WatchFacePresetSelectionActivity.class,
                new WatchFacePresetMutator() {
                    @Override
                    public String[] permute(WatchFacePreset permutation) {
                        String[] result = new String[WatchFacePreset.HandStalk.values().length];
                        int i = 0;
                        for (WatchFacePreset.HandStalk h : WatchFacePreset.HandStalk.values()) {
                            permutation.setHourHandStalk(h);
                            result[i++] = permutation.getString();
                        }
                        return result;
                    }

                    public Enum getCurrentValue(WatchFacePreset currentPreset) {
                        return currentPreset.getHourHandStalk();
                    }
                }));

        // Data for hour hand cutout in settings Activity.
        settingsConfigData.add(new WatchFacePresetPickerConfigItem(
                context.getString(R.string.config_preset_hour_hand_cutout),
                R.drawable.icn_styles,
                WatchFacePresetSelectionActivity.class,
                new WatchFacePresetMutator() {
                    @Override
                    public String[] permute(WatchFacePreset permutation) {
                        String[] result = new String[WatchFacePreset.HandCutout.values().length];
                        int i = 0;
                        for (WatchFacePreset.HandCutout h : WatchFacePreset.HandCutout.values()) {
                            permutation.setHourHandCutout(h);
                            result[i++] = permutation.getString();
                        }
                        return result;
                    }

                    public Enum getCurrentValue(WatchFacePreset currentPreset) {
                        return currentPreset.getHourHandCutout();
                    }
                }));

        // Data for hour hand style in settings Activity.
        settingsConfigData.add(new WatchFacePresetPickerConfigItem(
                context.getString(R.string.config_preset_hour_hand_style),
                R.drawable.icn_styles,
                WatchFacePresetSelectionActivity.class,
                new WatchFacePresetMutator() {
                    @Override
                    public String[] permute(WatchFacePreset permutation) {
                        String[] result = new String[WatchFacePreset.Style.values().length];
                        int i = 0;
                        for (WatchFacePreset.Style h : WatchFacePreset.Style.values()) {
                            permutation.setHourHandStyle(h);
                            result[i++] = permutation.getString();
                        }
                        return result;
                    }

                    public Enum getCurrentValue(WatchFacePreset currentPreset) {
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
                new WatchFacePresetMutator() {
                    @Override
                    public String[] permute(WatchFacePreset permutation) {
                        String[] result = new String[WatchFacePreset.HandShape.values().length];
                        int i = 0;
                        for (WatchFacePreset.HandShape h : WatchFacePreset.HandShape.values()) {
                            permutation.setMinuteHandShape(h);
                            result[i++] = permutation.getString();
                        }
                        return result;
                    }

                    public Enum getCurrentValue(WatchFacePreset currentPreset) {
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
                new WatchFacePresetMutator() {
                    @Override
                    public String[] permute(WatchFacePreset permutation) {
                        String[] result = new String[WatchFacePreset.HandLength.values().length];
                        int i = 0;
                        for (WatchFacePreset.HandLength h : WatchFacePreset.HandLength.values()) {
                            permutation.setMinuteHandLength(h);
                            result[i++] = permutation.getString();
                        }
                        return result;
                    }

                    public Enum getCurrentValue(WatchFacePreset currentPreset) {
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
                new WatchFacePresetMutator() {
                    @Override
                    public String[] permute(WatchFacePreset permutation) {
                        String[] result = new String[WatchFacePreset.HandThickness.values().length];
                        int i = 0;
                        for (WatchFacePreset.HandThickness h : WatchFacePreset.HandThickness.values()) {
                            permutation.setMinuteHandThickness(h);
                            result[i++] = permutation.getString();
                        }
                        return result;
                    }

                    public Enum getCurrentValue(WatchFacePreset currentPreset) {
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
                new WatchFacePresetMutator() {
                    @Override
                    public String[] permute(WatchFacePreset permutation) {
                        String[] result = new String[WatchFacePreset.HandStalk.values().length];
                        int i = 0;
                        for (WatchFacePreset.HandStalk h : WatchFacePreset.HandStalk.values()) {
                            permutation.setMinuteHandStalk(h);
                            result[i++] = permutation.getString();
                        }
                        return result;
                    }

                    public Enum getCurrentValue(WatchFacePreset currentPreset) {
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
                new WatchFacePresetMutator() {
                    @Override
                    public String[] permute(WatchFacePreset permutation) {
                        String[] result = new String[WatchFacePreset.HandCutout.values().length];
                        int i = 0;
                        for (WatchFacePreset.HandCutout h : WatchFacePreset.HandCutout.values()) {
                            permutation.setMinuteHandCutout(h);
                            result[i++] = permutation.getString();
                        }
                        return result;
                    }

                    public Enum getCurrentValue(WatchFacePreset currentPreset) {
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
                new WatchFacePresetMutator() {
                    @Override
                    public String[] permute(WatchFacePreset permutation) {
                        String[] result = new String[WatchFacePreset.Style.values().length];
                        int i = 0;
                        for (WatchFacePreset.Style h : WatchFacePreset.Style.values()) {
                            permutation.setMinuteHandStyle(h);
                            result[i++] = permutation.getString();
                        }
                        return result;
                    }

                    public Enum getCurrentValue(WatchFacePreset currentPreset) {
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
                new WatchFacePresetMutator() {
                    @Override
                    public String[] permute(WatchFacePreset permutation) {
                        String[] result = new String[WatchFacePreset.HandShape.values().length];
                        int i = 0;
                        for (WatchFacePreset.HandShape h : WatchFacePreset.HandShape.values()) {
                            permutation.setSecondHandShape(h);
                            result[i++] = permutation.getString();
                        }
                        return result;
                    }

                    public Enum getCurrentValue(WatchFacePreset currentPreset) {
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
                new WatchFacePresetMutator() {
                    @Override
                    public String[] permute(WatchFacePreset permutation) {
                        String[] result = new String[WatchFacePreset.HandLength.values().length];
                        int i = 0;
                        for (WatchFacePreset.HandLength h : WatchFacePreset.HandLength.values()) {
                            permutation.setSecondHandLength(h);
                            result[i++] = permutation.getString();
                        }
                        return result;
                    }

                    public Enum getCurrentValue(WatchFacePreset currentPreset) {
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
                new WatchFacePresetMutator() {
                    @Override
                    public String[] permute(WatchFacePreset permutation) {
                        String[] result = new String[WatchFacePreset.HandThickness.values().length];
                        int i = 0;
                        for (WatchFacePreset.HandThickness h : WatchFacePreset.HandThickness.values()) {
                            permutation.setSecondHandThickness(h);
                            result[i++] = permutation.getString();
                        }
                        return result;
                    }

                    public Enum getCurrentValue(WatchFacePreset currentPreset) {
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
                new WatchFacePresetMutator() {
                    @Override
                    public String[] permute(WatchFacePreset permutation) {
                        String[] result = new String[WatchFacePreset.Style.values().length];
                        int i = 0;
                        for (WatchFacePreset.Style h : WatchFacePreset.Style.values()) {
                            permutation.setSecondHandStyle(h);
                            result[i++] = permutation.getString();
                        }
                        return result;
                    }

                    public Enum getCurrentValue(WatchFacePreset currentPreset) {
                        return currentPreset.getSecondHandStyle();
                    }
                },
                new ConfigItemVisibilityCalculator() {
                    @Override
                    public boolean isVisible(WatchFacePreset currentPreset) {
                        return currentPreset.isSecondHandOverridden();
                    }
                }));

        // Data for ticks display in settings Activity.
        settingsConfigData.add(new WatchFacePresetPickerConfigItem(
                context.getString(R.string.config_preset_ticks_display),
                R.drawable.icn_styles,
                WatchFacePresetSelectionActivity.class,
                new WatchFacePresetMutator() {
                    @Override
                    public String[] permute(WatchFacePreset permutation) {
                        String[] result = new String[WatchFacePreset.TicksDisplay.values().length];
                        int i = 0;
                        for (WatchFacePreset.TicksDisplay h : WatchFacePreset.TicksDisplay.values()) {
                            permutation.setTicksDisplay(h);
                            result[i++] = permutation.getString();
                        }
                        return result;
                    }

                    public Enum getCurrentValue(WatchFacePreset currentPreset) {
                        return currentPreset.getTicksDisplay();
                    }
                }));

        // Data for four tick shape in settings Activity.
        settingsConfigData.add(new WatchFacePresetPickerConfigItem(
                context.getString(R.string.config_preset_four_tick_shape),
                R.drawable.icn_styles,
                WatchFacePresetSelectionActivity.class,
                new WatchFacePresetMutator() {
                    @Override
                    public String[] permute(WatchFacePreset permutation) {
                        String[] result = new String[WatchFacePreset.TickShape.values().length];
                        int i = 0;
                        for (WatchFacePreset.TickShape h : WatchFacePreset.TickShape.values()) {
                            permutation.setFourTickShape(h);
                            result[i++] = permutation.getString();
                        }
                        return result;
                    }

                    public Enum getCurrentValue(WatchFacePreset currentPreset) {
                        return currentPreset.getFourTickShape();
                    }
                },
                new ConfigItemVisibilityCalculator() {
                    @Override
                    public boolean isVisible(WatchFacePreset currentPreset) {
                        return currentPreset.isFourTicksVisible();
                    }
                }));

        // Data for four tick length in settings Activity.
        settingsConfigData.add(new WatchFacePresetPickerConfigItem(
                context.getString(R.string.config_preset_four_tick_length),
                R.drawable.icn_styles,
                WatchFacePresetSelectionActivity.class,
                new WatchFacePresetMutator() {
                    @Override
                    public String[] permute(WatchFacePreset permutation) {
                        String[] result = new String[WatchFacePreset.TickLength.values().length];
                        int i = 0;
                        for (WatchFacePreset.TickLength h : WatchFacePreset.TickLength.values()) {
                            permutation.setFourTickLength(h);
                            result[i++] = permutation.getString();
                        }
                        return result;
                    }

                    public Enum getCurrentValue(WatchFacePreset currentPreset) {
                        return currentPreset.getFourTickLength();
                    }
                },
                new ConfigItemVisibilityCalculator() {
                    @Override
                    public boolean isVisible(WatchFacePreset currentPreset) {
                        return currentPreset.isFourTicksVisible();
                    }
                }));

        // Data for four tick thickness in settings Activity.
        settingsConfigData.add(new WatchFacePresetPickerConfigItem(
                context.getString(R.string.config_preset_four_tick_thickness),
                R.drawable.icn_styles,
                WatchFacePresetSelectionActivity.class,
                new WatchFacePresetMutator() {
                    @Override
                    public String[] permute(WatchFacePreset permutation) {
                        String[] result = new String[WatchFacePreset.TickThickness.values().length];
                        int i = 0;
                        for (WatchFacePreset.TickThickness h : WatchFacePreset.TickThickness.values()) {
                            permutation.setFourTickThickness(h);
                            result[i++] = permutation.getString();
                        }
                        return result;
                    }

                    public Enum getCurrentValue(WatchFacePreset currentPreset) {
                        return currentPreset.getFourTickThickness();
                    }
                },
                new ConfigItemVisibilityCalculator() {
                    @Override
                    public boolean isVisible(WatchFacePreset currentPreset) {
                        return currentPreset.isFourTicksVisible();
                    }
                }));

        // Data for four tick radius position in settings Activity.
        settingsConfigData.add(new WatchFacePresetPickerConfigItem(
                context.getString(R.string.config_preset_four_tick_radius_position),
                R.drawable.icn_styles,
                WatchFacePresetSelectionActivity.class,
                new WatchFacePresetMutator() {
                    @Override
                    public String[] permute(WatchFacePreset permutation) {
                        String[] result = new String[WatchFacePreset.TickRadiusPosition.values().length];
                        int i = 0;
                        for (WatchFacePreset.TickRadiusPosition h : WatchFacePreset.TickRadiusPosition.values()) {
                            permutation.setFourTickRadiusPosition(h);
                            result[i++] = permutation.getString();
                        }
                        return result;
                    }

                    public Enum getCurrentValue(WatchFacePreset currentPreset) {
                        return currentPreset.getFourTickRadiusPosition();
                    }
                },
                new ConfigItemVisibilityCalculator() {
                    @Override
                    public boolean isVisible(WatchFacePreset currentPreset) {
                        return currentPreset.isFourTicksVisible();
                    }
                }));

        // Data for four tick style in settings Activity.
        settingsConfigData.add(new WatchFacePresetPickerConfigItem(
                context.getString(R.string.config_preset_four_tick_style),
                R.drawable.icn_styles,
                WatchFacePresetSelectionActivity.class,
                new WatchFacePresetMutator() {
                    @Override
                    public String[] permute(WatchFacePreset permutation) {
                        String[] result = new String[WatchFacePreset.Style.values().length];
                        int i = 0;
                        for (WatchFacePreset.Style h : WatchFacePreset.Style.values()) {
                            permutation.setFourTickStyle(h);
                            result[i++] = permutation.getString();
                        }
                        return result;
                    }

                    public Enum getCurrentValue(WatchFacePreset currentPreset) {
                        return currentPreset.getFourTickStyle();
                    }
                },
                new ConfigItemVisibilityCalculator() {
                    @Override
                    public boolean isVisible(WatchFacePreset currentPreset) {
                        return currentPreset.isFourTicksVisible();
                    }
                }));

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
                new WatchFacePresetMutator() {
                    @Override
                    public String[] permute(WatchFacePreset permutation) {
                        String[] result = new String[WatchFacePreset.TickShape.values().length];
                        int i = 0;
                        for (WatchFacePreset.TickShape h : WatchFacePreset.TickShape.values()) {
                            permutation.setTwelveTickShape(h);
                            result[i++] = permutation.getString();
                        }
                        return result;
                    }

                    public Enum getCurrentValue(WatchFacePreset currentPreset) {
                        return currentPreset.getTwelveTickShape();
                    }
                },
                new ConfigItemVisibilityCalculator() {
                    @Override
                    public boolean isVisible(WatchFacePreset currentPreset) {
                        return currentPreset.isTwelveTicksVisible() &&
                                currentPreset.isTwelveTickOverridden();
                    }
                }));

        // Data for twelve tick length in settings Activity.
        settingsConfigData.add(new WatchFacePresetPickerConfigItem(
                context.getString(R.string.config_preset_twelve_tick_length),
                R.drawable.icn_styles,
                WatchFacePresetSelectionActivity.class,
                new WatchFacePresetMutator() {
                    @Override
                    public String[] permute(WatchFacePreset permutation) {
                        String[] result = new String[WatchFacePreset.TickLength.values().length];
                        int i = 0;
                        for (WatchFacePreset.TickLength h : WatchFacePreset.TickLength.values()) {
                            permutation.setTwelveTickLength(h);
                            result[i++] = permutation.getString();
                        }
                        return result;
                    }

                    public Enum getCurrentValue(WatchFacePreset currentPreset) {
                        return currentPreset.getTwelveTickLength();
                    }
                },
                new ConfigItemVisibilityCalculator() {
                    @Override
                    public boolean isVisible(WatchFacePreset currentPreset) {
                        return currentPreset.isTwelveTicksVisible() &&
                                currentPreset.isTwelveTickOverridden();
                    }
                }));

        // Data for twelve tick thickness in settings Activity.
        settingsConfigData.add(new WatchFacePresetPickerConfigItem(
                context.getString(R.string.config_preset_twelve_tick_thickness),
                R.drawable.icn_styles,
                WatchFacePresetSelectionActivity.class,
                new WatchFacePresetMutator() {
                    @Override
                    public String[] permute(WatchFacePreset permutation) {
                        String[] result = new String[WatchFacePreset.TickThickness.values().length];
                        int i = 0;
                        for (WatchFacePreset.TickThickness h : WatchFacePreset.TickThickness.values()) {
                            permutation.setTwelveTickThickness(h);
                            result[i++] = permutation.getString();
                        }
                        return result;
                    }

                    public Enum getCurrentValue(WatchFacePreset currentPreset) {
                        return currentPreset.getTwelveTickThickness();
                    }
                },
                new ConfigItemVisibilityCalculator() {
                    @Override
                    public boolean isVisible(WatchFacePreset currentPreset) {
                        return currentPreset.isTwelveTicksVisible() &&
                                currentPreset.isTwelveTickOverridden();
                    }
                }));

        // Data for twelve tick radius position in settings Activity.
        settingsConfigData.add(new WatchFacePresetPickerConfigItem(
                context.getString(R.string.config_preset_twelve_tick_radius_position),
                R.drawable.icn_styles,
                WatchFacePresetSelectionActivity.class,
                new WatchFacePresetMutator() {
                    @Override
                    public String[] permute(WatchFacePreset permutation) {
                        String[] result = new String[WatchFacePreset.TickRadiusPosition.values().length];
                        int i = 0;
                        for (WatchFacePreset.TickRadiusPosition h : WatchFacePreset.TickRadiusPosition.values()) {
                            permutation.setTwelveTickRadiusPosition(h);
                            result[i++] = permutation.getString();
                        }
                        return result;
                    }

                    public Enum getCurrentValue(WatchFacePreset currentPreset) {
                        return currentPreset.getTwelveTickRadiusPosition();
                    }
                },
                new ConfigItemVisibilityCalculator() {
                    @Override
                    public boolean isVisible(WatchFacePreset currentPreset) {
                        return currentPreset.isTwelveTicksVisible() &&
                                currentPreset.isTwelveTickOverridden();
                    }
                }));

        // Data for twelve tick style in settings Activity.
        settingsConfigData.add(new WatchFacePresetPickerConfigItem(
                context.getString(R.string.config_preset_twelve_tick_style),
                R.drawable.icn_styles,
                WatchFacePresetSelectionActivity.class,
                new WatchFacePresetMutator() {
                    @Override
                    public String[] permute(WatchFacePreset permutation) {
                        String[] result = new String[WatchFacePreset.Style.values().length];
                        int i = 0;
                        for (WatchFacePreset.Style h : WatchFacePreset.Style.values()) {
                            permutation.setTwelveTickStyle(h);
                            result[i++] = permutation.getString();
                        }
                        return result;
                    }

                    public Enum getCurrentValue(WatchFacePreset currentPreset) {
                        return currentPreset.getTwelveTickStyle();
                    }
                },
                new ConfigItemVisibilityCalculator() {
                    @Override
                    public boolean isVisible(WatchFacePreset currentPreset) {
                        return currentPreset.isTwelveTicksVisible() &&
                                currentPreset.isTwelveTickOverridden();
                    }
                }));

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
                new WatchFacePresetMutator() {
                    @Override
                    public String[] permute(WatchFacePreset permutation) {
                        String[] result = new String[WatchFacePreset.TickShape.values().length];
                        int i = 0;
                        for (WatchFacePreset.TickShape h : WatchFacePreset.TickShape.values()) {
                            permutation.setSixtyTickShape(h);
                            result[i++] = permutation.getString();
                        }
                        return result;
                    }

                    public Enum getCurrentValue(WatchFacePreset currentPreset) {
                        return currentPreset.getSixtyTickShape();
                    }
                },
                new ConfigItemVisibilityCalculator() {
                    @Override
                    public boolean isVisible(WatchFacePreset currentPreset) {
                        return currentPreset.isSixtyTicksVisible() &&
                                currentPreset.isSixtyTickOverridden();
                    }
                }));

        // Data for sixty tick length in settings Activity.
        settingsConfigData.add(new WatchFacePresetPickerConfigItem(
                context.getString(R.string.config_preset_sixty_tick_length),
                R.drawable.icn_styles,
                WatchFacePresetSelectionActivity.class,
                new WatchFacePresetMutator() {
                    @Override
                    public String[] permute(WatchFacePreset permutation) {
                        String[] result = new String[WatchFacePreset.TickLength.values().length];
                        int i = 0;
                        for (WatchFacePreset.TickLength h : WatchFacePreset.TickLength.values()) {
                            permutation.setSixtyTickLength(h);
                            result[i++] = permutation.getString();
                        }
                        return result;
                    }

                    public Enum getCurrentValue(WatchFacePreset currentPreset) {
                        return currentPreset.getSixtyTickLength();
                    }
                },
                new ConfigItemVisibilityCalculator() {
                    @Override
                    public boolean isVisible(WatchFacePreset currentPreset) {
                        return currentPreset.isSixtyTicksVisible() &&
                                currentPreset.isSixtyTickOverridden();
                    }
                }));

        // Data for sixty tick thickness in settings Activity.
        settingsConfigData.add(new WatchFacePresetPickerConfigItem(
                context.getString(R.string.config_preset_sixty_tick_thickness),
                R.drawable.icn_styles,
                WatchFacePresetSelectionActivity.class,
                new WatchFacePresetMutator() {
                    @Override
                    public String[] permute(WatchFacePreset permutation) {
                        String[] result = new String[WatchFacePreset.TickThickness.values().length];
                        int i = 0;
                        for (WatchFacePreset.TickThickness h : WatchFacePreset.TickThickness.values()) {
                            permutation.setSixtyTickThickness(h);
                            result[i++] = permutation.getString();
                        }
                        return result;
                    }

                    public Enum getCurrentValue(WatchFacePreset currentPreset) {
                        return currentPreset.getSixtyTickThickness();
                    }
                },
                new ConfigItemVisibilityCalculator() {
                    @Override
                    public boolean isVisible(WatchFacePreset currentPreset) {
                        return currentPreset.isSixtyTicksVisible() &&
                                currentPreset.isSixtyTickOverridden();
                    }
                }));

        // Data for sixty tick radius position in settings Activity.
        settingsConfigData.add(new WatchFacePresetPickerConfigItem(
                context.getString(R.string.config_preset_sixty_tick_radius_position),
                R.drawable.icn_styles,
                WatchFacePresetSelectionActivity.class,
                new WatchFacePresetMutator() {
                    @Override
                    public String[] permute(WatchFacePreset permutation) {
                        String[] result = new String[WatchFacePreset.TickRadiusPosition.values().length];
                        int i = 0;
                        for (WatchFacePreset.TickRadiusPosition h : WatchFacePreset.TickRadiusPosition.values()) {
                            permutation.setSixtyTickRadiusPosition(h);
                            result[i++] = permutation.getString();
                        }
                        return result;
                    }

                    public Enum getCurrentValue(WatchFacePreset currentPreset) {
                        return currentPreset.getSixtyTickRadiusPosition();
                    }
                },
                new ConfigItemVisibilityCalculator() {
                    @Override
                    public boolean isVisible(WatchFacePreset currentPreset) {
                        return currentPreset.isSixtyTicksVisible() &&
                                currentPreset.isSixtyTickOverridden();
                    }
                }));

        // Data for sixty tick style in settings Activity.
        settingsConfigData.add(new WatchFacePresetPickerConfigItem(
                context.getString(R.string.config_preset_sixty_tick_style),
                R.drawable.icn_styles,
                WatchFacePresetSelectionActivity.class,
                new WatchFacePresetMutator() {
                    @Override
                    public String[] permute(WatchFacePreset permutation) {
                        String[] result = new String[WatchFacePreset.Style.values().length];
                        int i = 0;
                        for (WatchFacePreset.Style h : WatchFacePreset.Style.values()) {
                            permutation.setSixtyTickStyle(h);
                            result[i++] = permutation.getString();
                        }
                        return result;
                    }

                    public Enum getCurrentValue(WatchFacePreset currentPreset) {
                        return currentPreset.getSixtyTickStyle();
                    }
                },
                new ConfigItemVisibilityCalculator() {
                    @Override
                    public boolean isVisible(WatchFacePreset currentPreset) {
                        return currentPreset.isSixtyTicksVisible() &&
                                currentPreset.isSixtyTickOverridden();
                    }
                }));

        // Data for fill highlight style in settings Activity.
        settingsConfigData.add(new WatchFacePresetPickerConfigItem(
                context.getString(R.string.config_preset_fill_highlight_style),
                R.drawable.icn_styles,
                WatchFacePresetSelectionActivity.class,
                new WatchFacePresetMutator() {
                    @Override
                    public String[] permute(WatchFacePreset permutation) {
                        String[] result = new String[WatchFacePreset.GradientStyle.values().length];
                        int i = 0;
                        for (WatchFacePreset.GradientStyle h : WatchFacePreset.GradientStyle.values()) {
                            permutation.setFillHighlightStyle(h);
                            result[i++] = permutation.getString();
                        }
                        return result;
                    }

                    public Enum getCurrentValue(WatchFacePreset currentPreset) {
                        return currentPreset.getFillHighlightStyle();
                    }
                }));

        // Data for accent fill style in settings Activity.
        settingsConfigData.add(new WatchFacePresetPickerConfigItem(
                context.getString(R.string.config_preset_accent_fill_style),
                R.drawable.icn_styles,
                WatchFacePresetSelectionActivity.class,
                new WatchFacePresetMutator() {
                    @Override
                    public String[] permute(WatchFacePreset permutation) {
                        String[] result = new String[WatchFacePreset.GradientStyle.values().length];
                        int i = 0;
                        for (WatchFacePreset.GradientStyle h : WatchFacePreset.GradientStyle.values()) {
                            permutation.setAccentFillStyle(h);
                            result[i++] = permutation.getString();
                        }
                        return result;
                    }

                    public Enum getCurrentValue(WatchFacePreset currentPreset) {
                        return currentPreset.getAccentFillStyle();
                    }
                }));

        // Data for accent highlight style in settings Activity.
        settingsConfigData.add(new WatchFacePresetPickerConfigItem(
                context.getString(R.string.config_preset_accent_highlight_style),
                R.drawable.icn_styles,
                WatchFacePresetSelectionActivity.class,
                new WatchFacePresetMutator() {
                    @Override
                    public String[] permute(WatchFacePreset permutation) {
                        String[] result = new String[WatchFacePreset.GradientStyle.values().length];
                        int i = 0;
                        for (WatchFacePreset.GradientStyle h : WatchFacePreset.GradientStyle.values()) {
                            permutation.setAccentHighlightStyle(h);
                            result[i++] = permutation.getString();
                        }
                        return result;
                    }

                    public Enum getCurrentValue(WatchFacePreset currentPreset) {
                        return currentPreset.getAccentHighlightStyle();
                    }
                }));

        // Data for base accent style in settings Activity.
        settingsConfigData.add(new WatchFacePresetPickerConfigItem(
                context.getString(R.string.config_preset_base_accent_style),
                R.drawable.icn_styles,
                WatchFacePresetSelectionActivity.class,
                new WatchFacePresetMutator() {
                    @Override
                    public String[] permute(WatchFacePreset permutation) {
                        String[] result = new String[WatchFacePreset.GradientStyle.values().length];
                        int i = 0;
                        for (WatchFacePreset.GradientStyle h : WatchFacePreset.GradientStyle.values()) {
                            permutation.setBaseAccentStyle(h);
                            result[i++] = permutation.getString();
                        }
                        return result;
                    }

                    public Enum getCurrentValue(WatchFacePreset currentPreset) {
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

    /**
     * Interface all ConfigItems must implement so the {@link RecyclerView}'s Adapter associated
     * with the configuration activity knows what type of ViewHolder to inflate.
     */
    public interface ConfigItemType {
        int getConfigType();
    }

    /**
     * Data for Watch Face Preview with Complications Preview item in RecyclerView.
     */
    public static class PreviewAndComplicationsConfigItem implements ConfigItemType {

        private int defaultComplicationResourceId;

        PreviewAndComplicationsConfigItem(int defaultComplicationResourceId) {
            this.defaultComplicationResourceId = defaultComplicationResourceId;
        }

        public int getDefaultComplicationResourceId() {
            return defaultComplicationResourceId;
        }

        @Override
        public int getConfigType() {
            return AnalogComplicationConfigRecyclerViewAdapter.TYPE_PREVIEW_AND_COMPLICATIONS_CONFIG;
        }
    }

    /**
     * Data for "more options" item in RecyclerView.
     */
    public static class MoreOptionsConfigItem implements ConfigItemType {

        private int iconResourceId;

        MoreOptionsConfigItem(int iconResourceId) {
            this.iconResourceId = iconResourceId;
        }

        public int getIconResourceId() {
            return iconResourceId;
        }

        @Override
        public int getConfigType() {
            return AnalogComplicationConfigRecyclerViewAdapter.TYPE_MORE_OPTIONS;
        }
    }

    /**
     * Data for color picker item in RecyclerView.
     */
    public static class ColorPickerConfigItem implements ConfigItemType {

        private String name;
        private int iconResourceId;
        private WatchFacePreset.ColorType mColorType;
        private Class<ColorSelectionActivity> activityToChoosePreference;

        ColorPickerConfigItem(
                String name,
                int iconResourceId,
                WatchFacePreset.ColorType colorType,
                Class<ColorSelectionActivity> activity) {
            this.name = name;
            this.iconResourceId = iconResourceId;
            this.mColorType = colorType;
            this.activityToChoosePreference = activity;
        }

        public WatchFacePreset.ColorType getType() {
            return mColorType;
        }

        public String getName() {
            return name;
        }

        public int getIconResourceId() {
            return iconResourceId;
        }

//        public String getSharedPrefString() {
//            return sharedPrefString;
//        }

        public Class<ColorSelectionActivity> getActivityToChoosePreference() {
            return activityToChoosePreference;
        }

        @Override
        public int getConfigType() {
            return AnalogComplicationConfigRecyclerViewAdapter.TYPE_COLOR_PICKER_CONFIG;
        }
    }

    private interface WatchFacePresetMutator {
        /**
         * For the given WatchFacePreset (which must be a clone, since we'll modify it in the
         * process) return a String array with each permutation.
         *
         * @param permutation WatchFacePreset, which must be a clone, since we'll modify it
         * @return String array with each permutation
         */
        String[] permute(WatchFacePreset permutation);

        /**
         * For the given WatchFacePreset (which is our current preference) return the current
         * value.
         *
         * @param currentPreset WatchFacePreset of our current preference
         * @return Value that it's currently set to
         */
        Enum getCurrentValue(WatchFacePreset currentPreset);
    }

    /**
     * Objects inherit this interface to determine the visibility of a ConfigItem.
     * That is, implement this interface and put in some custom logic that determines
     * whether an item is visible or not.
     */
    private interface ConfigItemVisibilityCalculator {
        boolean isVisible(WatchFacePreset currentPreset);
    }

    public static class WatchFacePresetPickerConfigItem implements ConfigItemType {
        private String mName;
        private int mIconResourceId;
        private Class<WatchFacePresetSelectionActivity> mActivityToChoosePreference;
        private WatchFacePresetMutator mMutator;
        private ConfigItemVisibilityCalculator mConfigItemVisibilityCalculator;

        WatchFacePresetPickerConfigItem(
                String name,
                int iconResourceId,
                Class<WatchFacePresetSelectionActivity> activity,
                WatchFacePresetMutator mutator) {
            this(name, iconResourceId, activity, mutator, null);
        }

        WatchFacePresetPickerConfigItem(
                String name,
                int iconResourceId,
                Class<WatchFacePresetSelectionActivity> activity,
                WatchFacePresetMutator mutator,
                ConfigItemVisibilityCalculator configItemVisibilityCalculator) {
            mMutator = mutator;
            mName = name;
            mIconResourceId = iconResourceId;
            mActivityToChoosePreference = activity;
            mConfigItemVisibilityCalculator = configItemVisibilityCalculator;
        }

        public CharSequence getName(WatchFacePreset watchFacePreset, Context context) {
            Enum e = mMutator.getCurrentValue(watchFacePreset);

            if (e == null) {
                return mName;
            } else if (e instanceof WatchFacePreset.EnumResourceId) {
                WatchFacePreset.EnumResourceId f = (WatchFacePreset.EnumResourceId) e;
                return Html.fromHtml(mName + "<br/><small>" +
                        context.getResources().getStringArray(f.getNameResourceId())[e.ordinal()] +
                        "</small>", Html.FROM_HTML_MODE_LEGACY);
            } else {
                return Html.fromHtml(mName + "<br/><small>" +
                        e.getClass().getSimpleName() + " ~ " + e.name() +
                        "</small>", Html.FROM_HTML_MODE_LEGACY);
            }
        }

        public int getIconResourceId() {
            return mIconResourceId;
        }

        public Class<WatchFacePresetSelectionActivity> getActivityToChoosePreference() {
            return mActivityToChoosePreference;
        }

        @Override
        public int getConfigType() {
            return AnalogComplicationConfigRecyclerViewAdapter.TYPE_WATCH_FACE_PRESET_PICKER_CONFIG;
        }

        public String[] permute(WatchFacePreset watchFacePreset) {
            return mMutator.permute(watchFacePreset.clone());
        }

        public boolean isVisible(WatchFacePreset watchFacePreset) {
            return mConfigItemVisibilityCalculator == null ||
                    mConfigItemVisibilityCalculator.isVisible(watchFacePreset);
        }
    }

    /**
     * Data for Night Vision preference picker item in RecyclerView.
     */
    public static class WatchFacePresetToggleConfigItem implements ConfigItemType {

        private String name;
        private int iconEnabledResourceId;
        private int iconDisabledResourceId;
        private WatchFacePresetMutator mMutator;

        WatchFacePresetToggleConfigItem(
                String name,
                int iconEnabledResourceId,
                int iconDisabledResourceId,
                WatchFacePresetMutator mutator) {
            this.name = name;
            this.iconEnabledResourceId = iconEnabledResourceId;
            this.iconDisabledResourceId = iconDisabledResourceId;
            this.mMutator = mutator;
        }

        public String getName() {
            return name;
        }

        public int getIconEnabledResourceId() {
            return iconEnabledResourceId;
        }

        public int getIconDisabledResourceId() {
            return iconDisabledResourceId;
        }

        @Override
        public int getConfigType() {
            return AnalogComplicationConfigRecyclerViewAdapter.TYPE_WATCH_FACE_PRESET_TOGGLE_CONFIG;
        }

        public String[] permute(WatchFacePreset watchFacePreset) {
            return mMutator.permute(watchFacePreset.clone());
        }
    }

    /**
     * Data for Unread Notification preference picker item in RecyclerView.
     */
    public static class UnreadNotificationConfigItem implements ConfigItemType {

        private String name;
        private int iconEnabledResourceId;
        private int iconDisabledResourceId;
        private int sharedPrefId;

        UnreadNotificationConfigItem(
                String name,
                int iconEnabledResourceId,
                int iconDisabledResourceId,
                int sharedPrefId) {
            this.name = name;
            this.iconEnabledResourceId = iconEnabledResourceId;
            this.iconDisabledResourceId = iconDisabledResourceId;
            this.sharedPrefId = sharedPrefId;
        }

        public String getName() {
            return name;
        }

        public int getIconEnabledResourceId() {
            return iconEnabledResourceId;
        }

        public int getIconDisabledResourceId() {
            return iconDisabledResourceId;
        }

        public int getSharedPrefId() {
            return sharedPrefId;
        }

        @Override
        public int getConfigType() {
            return AnalogComplicationConfigRecyclerViewAdapter.TYPE_UNREAD_NOTIFICATION_CONFIG;
        }
    }

    /**
     * Data for background image complication picker item in RecyclerView.
     */
    public static class BackgroundComplicationConfigItem implements ConfigItemType {

        private String name;
        private int iconResourceId;

        BackgroundComplicationConfigItem(
                String name,
                int iconResourceId) {

            this.name = name;
            this.iconResourceId = iconResourceId;
        }

        public String getName() {
            return name;
        }

        public int getIconResourceId() {
            return iconResourceId;
        }

        @Override
        public int getConfigType() {
            return AnalogComplicationConfigRecyclerViewAdapter.TYPE_BACKGROUND_COMPLICATION_IMAGE_CONFIG;
        }
    }

    /**
     * Data for Night Vision preference picker item in RecyclerView.
     */
    public static class NightVisionConfigItem implements ConfigItemType {

        private String name;
        private int iconEnabledResourceId;
        private int iconDisabledResourceId;
        private int sharedPrefId;

        NightVisionConfigItem(
                String name,
                int iconEnabledResourceId,
                int iconDisabledResourceId,
                int sharedPrefId) {
            this.name = name;
            this.iconEnabledResourceId = iconEnabledResourceId;
            this.iconDisabledResourceId = iconDisabledResourceId;
            this.sharedPrefId = sharedPrefId;
        }

        public String getName() {
            return name;
        }

        public int getIconEnabledResourceId() {
            return iconEnabledResourceId;
        }

        public int getIconDisabledResourceId() {
            return iconDisabledResourceId;
        }

        public int getSharedPrefId() {
            return sharedPrefId;
        }

        @Override
        public int getConfigType() {
            return AnalogComplicationConfigRecyclerViewAdapter.TYPE_NIGHT_VISION_CONFIG;
        }
    }
}