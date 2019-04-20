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
public class AnalogComplicationConfigData extends BaseConfigData {


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
                new WatchFacePresetMutatorGeneric<WatchFacePreset.HandShape>() {
                    @Override
                    void permuteOne(WatchFacePreset permutation, WatchFacePreset.HandShape h) {
                        permutation.setHourHandShape(h);
                    }

                    @Override
                    WatchFacePreset.HandShape[] values() {
                        return WatchFacePreset.HandShape.values();
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
                new WatchFacePresetMutatorGeneric<WatchFacePreset.HandLength>() {
                    @Override
                    WatchFacePreset.HandLength[] values() {
                        return WatchFacePreset.HandLength.values();
                    }

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
                new WatchFacePresetMutatorGeneric<WatchFacePreset.HandThickness>() {
                    @Override
                    WatchFacePreset.HandThickness[] values() {
                        return WatchFacePreset.HandThickness.values();
                    }

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
                new WatchFacePresetMutatorGeneric<WatchFacePreset.HandStalk>() {
                    @Override
                    WatchFacePreset.HandStalk[] values() {
                        return WatchFacePreset.HandStalk.values();
                    }

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
                new WatchFacePresetMutatorGeneric<WatchFacePreset.HandCutout>() {
                    @Override
                    WatchFacePreset.HandCutout[] values() {
                        return WatchFacePreset.HandCutout.values();
                    }

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
                new WatchFacePresetMutatorGeneric<WatchFacePreset.Style>() {
                    @Override
                    WatchFacePreset.Style[] values() {
                        return WatchFacePreset.Style.values();
                    }

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
                new WatchFacePresetMutatorGeneric<WatchFacePreset.HandShape>() {
                    @Override
                    WatchFacePreset.HandShape[] values() {
                        return WatchFacePreset.HandShape.values();
                    }

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
                new WatchFacePresetMutatorGeneric<WatchFacePreset.HandLength>() {
                    @Override
                    WatchFacePreset.HandLength[] values() {
                        return WatchFacePreset.HandLength.values();
                    }

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
                new WatchFacePresetMutatorGeneric<WatchFacePreset.HandThickness>() {
                    @Override
                    WatchFacePreset.HandThickness[] values() {
                        return WatchFacePreset.HandThickness.values();
                    }

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
                new WatchFacePresetMutatorGeneric<WatchFacePreset.HandStalk>() {
                    @Override
                    WatchFacePreset.HandStalk[] values() {
                        return WatchFacePreset.HandStalk.values();
                    }

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
                new WatchFacePresetMutatorGeneric<WatchFacePreset.HandCutout>() {
                    @Override
                    WatchFacePreset.HandCutout[] values() {
                        return WatchFacePreset.HandCutout.values();
                    }

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
                new WatchFacePresetMutatorGeneric<WatchFacePreset.Style>() {
                    @Override
                    WatchFacePreset.Style[] values() {
                        return WatchFacePreset.Style.values();
                    }

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
                new WatchFacePresetMutatorGeneric<WatchFacePreset.HandShape>() {
                    @Override
                    WatchFacePreset.HandShape[] values() {
                        return WatchFacePreset.HandShape.values();
                    }

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
                new WatchFacePresetMutatorGeneric<WatchFacePreset.HandLength>() {
                    @Override
                    WatchFacePreset.HandLength[] values() {
                        return WatchFacePreset.HandLength.values();
                    }

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
                new WatchFacePresetMutatorGeneric<WatchFacePreset.HandThickness>() {
                    @Override
                    WatchFacePreset.HandThickness[] values() {
                        return WatchFacePreset.HandThickness.values();
                    }

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
                new WatchFacePresetMutatorGeneric<WatchFacePreset.Style>() {
                    @Override
                    WatchFacePreset.Style[] values() {
                        return WatchFacePreset.Style.values();
                    }

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

        // Data for ticks display in settings Activity.
        settingsConfigData.add(new WatchFacePresetPickerConfigItem(
                context.getString(R.string.config_preset_ticks_display),
                R.drawable.icn_styles,
                WatchFacePresetSelectionActivity.class,
                new WatchFacePresetMutatorGeneric<WatchFacePreset.TicksDisplay>() {
                    @Override
                    WatchFacePreset.TicksDisplay[] values() {
                        return WatchFacePreset.TicksDisplay.values();
                    }

                    @Override
                    void permuteOne(WatchFacePreset permutation, WatchFacePreset.TicksDisplay h) {
                        permutation.setTicksDisplay(h);
                    }

                    @Override
                    public WatchFacePreset.TicksDisplay getCurrentValue(WatchFacePreset currentPreset) {
                        return currentPreset.getTicksDisplay();
                    }
                }));

        // Data for four tick shape in settings Activity.
        settingsConfigData.add(new WatchFacePresetPickerConfigItem(
                context.getString(R.string.config_preset_four_tick_shape),
                R.drawable.icn_styles,
                WatchFacePresetSelectionActivity.class,
                new WatchFacePresetMutatorGeneric<WatchFacePreset.TickShape>() {
                    @Override
                    WatchFacePreset.TickShape[] values() {
                        return WatchFacePreset.TickShape.values();
                    }

                    @Override
                    void permuteOne(WatchFacePreset permutation, WatchFacePreset.TickShape h) {
                        permutation.setFourTickShape(h);
                    }

                    @Override
                    public WatchFacePreset.TickShape getCurrentValue(WatchFacePreset currentPreset) {
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
                new WatchFacePresetMutatorGeneric<WatchFacePreset.TickLength>() {
                    @Override
                    WatchFacePreset.TickLength[] values() {
                        return WatchFacePreset.TickLength.values();
                    }

                    @Override
                    void permuteOne(WatchFacePreset permutation, WatchFacePreset.TickLength h) {
                        permutation.setFourTickLength(h);
                    }

                    @Override
                    public WatchFacePreset.TickLength getCurrentValue(WatchFacePreset currentPreset) {
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
                new WatchFacePresetMutatorGeneric<WatchFacePreset.TickThickness>() {
                    @Override
                    WatchFacePreset.TickThickness[] values() {
                        return WatchFacePreset.TickThickness.values();
                    }

                    @Override
                    void permuteOne(WatchFacePreset permutation, WatchFacePreset.TickThickness h) {
                        permutation.setFourTickThickness(h);
                    }

                    @Override
                    public WatchFacePreset.TickThickness getCurrentValue(WatchFacePreset currentPreset) {
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
                new WatchFacePresetMutatorGeneric<WatchFacePreset.TickRadiusPosition>() {
                    @Override
                    WatchFacePreset.TickRadiusPosition[] values() {
                        return WatchFacePreset.TickRadiusPosition.values();
                    }

                    @Override
                    void permuteOne(WatchFacePreset permutation, WatchFacePreset.TickRadiusPosition h) {
                        permutation.setFourTickRadiusPosition(h);
                    }

                    @Override
                    public WatchFacePreset.TickRadiusPosition getCurrentValue(WatchFacePreset currentPreset) {
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
                new WatchFacePresetMutatorGeneric<WatchFacePreset.Style>() {
                    @Override
                    WatchFacePreset.Style[] values() {
                        return WatchFacePreset.Style.values();
                    }

                    @Override
                    void permuteOne(WatchFacePreset permutation, WatchFacePreset.Style h) {
                        permutation.setFourTickStyle(h);
                    }

                    @Override
                    public WatchFacePreset.Style getCurrentValue(WatchFacePreset currentPreset) {
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
                new WatchFacePresetMutatorGeneric<WatchFacePreset.TickShape>() {
                    @Override
                    WatchFacePreset.TickShape[] values() {
                        return WatchFacePreset.TickShape.values();
                    }

                    @Override
                    void permuteOne(WatchFacePreset permutation, WatchFacePreset.TickShape h) {
                        permutation.setTwelveTickShape(h);
                    }

                    @Override
                    public WatchFacePreset.TickShape getCurrentValue(WatchFacePreset currentPreset) {
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
                new WatchFacePresetMutatorGeneric<WatchFacePreset.TickLength>() {
                    @Override
                    WatchFacePreset.TickLength[] values() {
                        return WatchFacePreset.TickLength.values();
                    }

                    @Override
                    void permuteOne(WatchFacePreset permutation, WatchFacePreset.TickLength h) {
                        permutation.setTwelveTickLength(h);
                    }

                    @Override
                    public WatchFacePreset.TickLength getCurrentValue(WatchFacePreset currentPreset) {
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
                new WatchFacePresetMutatorGeneric<WatchFacePreset.TickThickness>() {
                    @Override
                    WatchFacePreset.TickThickness[] values() {
                        return WatchFacePreset.TickThickness.values();
                    }

                    @Override
                    void permuteOne(WatchFacePreset permutation, WatchFacePreset.TickThickness h) {
                        permutation.setTwelveTickThickness(h);
                    }

                    @Override
                    public WatchFacePreset.TickThickness getCurrentValue(WatchFacePreset currentPreset) {
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
                new WatchFacePresetMutatorGeneric<WatchFacePreset.TickRadiusPosition>() {
                    @Override
                    WatchFacePreset.TickRadiusPosition[] values() {
                        return WatchFacePreset.TickRadiusPosition.values();
                    }

                    @Override
                    void permuteOne(WatchFacePreset permutation, WatchFacePreset.TickRadiusPosition h) {
                        permutation.setTwelveTickRadiusPosition(h);
                    }

                    @Override
                    public WatchFacePreset.TickRadiusPosition getCurrentValue(WatchFacePreset currentPreset) {
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
                new WatchFacePresetMutatorGeneric<WatchFacePreset.Style>() {
                    @Override
                    WatchFacePreset.Style[] values() {
                        return WatchFacePreset.Style.values();
                    }

                    @Override
                    void permuteOne(WatchFacePreset permutation, WatchFacePreset.Style h) {
                        permutation.setTwelveTickStyle(h);
                    }

                    @Override
                    public WatchFacePreset.Style getCurrentValue(WatchFacePreset currentPreset) {
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
                new WatchFacePresetMutatorGeneric<WatchFacePreset.TickShape>() {
                    @Override
                    WatchFacePreset.TickShape[] values() {
                        return WatchFacePreset.TickShape.values();
                    }

                    @Override
                    void permuteOne(WatchFacePreset permutation, WatchFacePreset.TickShape h) {
                        permutation.setSixtyTickShape(h);
                    }

                    @Override
                    public WatchFacePreset.TickShape getCurrentValue(WatchFacePreset currentPreset) {
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
                new WatchFacePresetMutatorGeneric<WatchFacePreset.TickLength>() {
                    @Override
                    WatchFacePreset.TickLength[] values() {
                        return WatchFacePreset.TickLength.values();
                    }

                    @Override
                    void permuteOne(WatchFacePreset permutation, WatchFacePreset.TickLength h) {
                        permutation.setSixtyTickLength(h);
                    }

                    @Override
                    public WatchFacePreset.TickLength getCurrentValue(WatchFacePreset currentPreset) {
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
                new WatchFacePresetMutatorGeneric<WatchFacePreset.TickThickness>() {
                    @Override
                    WatchFacePreset.TickThickness[] values() {
                        return WatchFacePreset.TickThickness.values();
                    }

                    @Override
                    void permuteOne(WatchFacePreset permutation, WatchFacePreset.TickThickness h) {
                        permutation.setSixtyTickThickness(h);
                    }

                    @Override
                    public WatchFacePreset.TickThickness getCurrentValue(WatchFacePreset currentPreset) {
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
                new WatchFacePresetMutatorGeneric<WatchFacePreset.TickRadiusPosition>() {
                    @Override
                    WatchFacePreset.TickRadiusPosition[] values() {
                        return WatchFacePreset.TickRadiusPosition.values();
                    }

                    @Override
                    void permuteOne(WatchFacePreset permutation, WatchFacePreset.TickRadiusPosition h) {
                        permutation.setSixtyTickRadiusPosition(h);
                    }

                    @Override
                    public WatchFacePreset.TickRadiusPosition getCurrentValue(WatchFacePreset currentPreset) {
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
                new WatchFacePresetMutatorGeneric<WatchFacePreset.Style>() {
                    @Override
                    WatchFacePreset.Style[] values() {
                        return WatchFacePreset.Style.values();
                    }

                    @Override
                    void permuteOne(WatchFacePreset permutation, WatchFacePreset.Style h) {
                        permutation.setSixtyTickStyle(h);
                    }

                    @Override
                    public WatchFacePreset.Style getCurrentValue(WatchFacePreset currentPreset) {
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
                new WatchFacePresetMutatorGeneric<WatchFacePreset.GradientStyle>() {
                    @Override
                    WatchFacePreset.GradientStyle[] values() {
                        return WatchFacePreset.GradientStyle.values();
                    }

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
                new WatchFacePresetMutatorGeneric<WatchFacePreset.GradientStyle>() {
                    @Override
                    WatchFacePreset.GradientStyle[] values() {
                        return WatchFacePreset.GradientStyle.values();
                    }

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
                new WatchFacePresetMutatorGeneric<WatchFacePreset.GradientStyle>() {
                    @Override
                    WatchFacePreset.GradientStyle[] values() {
                        return WatchFacePreset.GradientStyle.values();
                    }

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
                new WatchFacePresetMutatorGeneric<WatchFacePreset.GradientStyle>() {
                    @Override
                    WatchFacePreset.GradientStyle[] values() {
                        return WatchFacePreset.GradientStyle.values();
                    }

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