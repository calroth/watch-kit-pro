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
 */

package pro.watchkit.wearable.watchface.model;

import android.content.Context;

import java.util.Arrays;
import java.util.List;

import pro.watchkit.wearable.watchface.R;
import pro.watchkit.wearable.watchface.config.WatchFaceSelectionActivity;
import pro.watchkit.wearable.watchface.watchface.WatchFaceGlobalDrawable;

public class WatchPartHandsConfigData extends ConfigData {
    @Override
    public List<ConfigItemType> getDataToPopulateAdapter(Context context) {
        int watchFaceGlobalDrawableFlags = WatchFaceGlobalDrawable.PART_BACKGROUND |
                WatchFaceGlobalDrawable.PART_HANDS;
        return Arrays.asList(
                // A preview of the current watch face.
                new WatchFaceDrawableConfigItem(watchFaceGlobalDrawableFlags),

                // Data for hour hand shape in settings Activity.
                new WatchFacePickerConfigItem(
                        context.getString(R.string.config_preset_hour_hand_shape),
                        R.drawable.icn_styles,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new MutatorImpl<>(
                                BytePackable.HandShape.values(),
                                WatchFacePreset::setHourHandShape,
                                WatchFacePreset::getHourHandShape)),

                // Data for hour hand length in settings Activity.
                new WatchFacePickerConfigItem(
                        context.getString(R.string.config_preset_hour_hand_length),
                        R.drawable.icn_styles,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new MutatorImpl<>(
                                BytePackable.HandLength.values(),
                                WatchFacePreset::setHourHandLength,
                                WatchFacePreset::getHourHandLength)),

                // Data for hour hand thickness in settings Activity.
                new WatchFacePickerConfigItem(
                        context.getString(R.string.config_preset_hour_hand_thickness),
                        R.drawable.icn_styles,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new MutatorImpl<>(
                                BytePackable.HandThickness.values(),
                                WatchFacePreset::setHourHandThickness,
                                WatchFacePreset::getHourHandThickness)),

                // Data for hour hand stalk in settings Activity.
                new WatchFacePickerConfigItem(
                        context.getString(R.string.config_preset_hour_hand_stalk),
                        R.drawable.icn_styles,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new MutatorImpl<>(
                                BytePackable.HandStalk.values(),
                                WatchFacePreset::setHourHandStalk,
                                WatchFacePreset::getHourHandStalk)),

                // Data for hour hand cutout in settings Activity.
                new WatchFacePickerConfigItem(
                        context.getString(R.string.config_preset_hour_hand_cutout),
                        R.drawable.icn_styles,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new MutatorImpl<>(
                                BytePackable.HandCutout.values(),
                                WatchFacePreset::setHourHandCutout,
                                WatchFacePreset::getHourHandCutout)),

                // Data for hour hand style in settings Activity.
                new WatchFacePickerConfigItem(
                        context.getString(R.string.config_preset_hour_hand_style),
                        R.drawable.icn_styles,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new MutatorImpl<>(
                                BytePackable.Style.values(),
                                WatchFacePreset::setHourHandStyle,
                                WatchFacePreset::getHourHandStyle)),

                // Data for minute hand override in settings Activity.
                new ToggleConfigItem<>(
                        context.getString(R.string.config_preset_minute_hand_override),
                        R.drawable.ic_notifications_white_24dp,
                        R.drawable.ic_notifications_off_white_24dp,
                        new Mutator<WatchFacePreset>() {
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
                        }),

                // Data for minute hand shape in settings Activity.
                new WatchFacePickerConfigItem(
                        context.getString(R.string.config_preset_minute_hand_shape),
                        R.drawable.icn_styles,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new MutatorImpl<>(
                                BytePackable.HandShape.values(),
                                WatchFacePreset::setMinuteHandShape,
                                WatchFacePreset::getMinuteHandShape),
                        WatchFacePreset::isMinuteHandOverridden),

                // Data for minute hand length in settings Activity.
                new WatchFacePickerConfigItem(
                        context.getString(R.string.config_preset_minute_hand_length),
                        R.drawable.icn_styles,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new MutatorImpl<>(
                                BytePackable.HandLength.values(),
                                WatchFacePreset::setMinuteHandLength,
                                WatchFacePreset::getMinuteHandLength),
                        WatchFacePreset::isMinuteHandOverridden),

                // Data for minute hand thickness in settings Activity.
                new WatchFacePickerConfigItem(
                        context.getString(R.string.config_preset_minute_hand_thickness),
                        R.drawable.icn_styles,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new MutatorImpl<>(
                                BytePackable.HandThickness.values(),
                                WatchFacePreset::setMinuteHandThickness,
                                WatchFacePreset::getMinuteHandThickness),
                        WatchFacePreset::isMinuteHandOverridden),

                // Data for minute hand stalk in settings Activity.
                new WatchFacePickerConfigItem(
                        context.getString(R.string.config_preset_minute_hand_stalk),
                        R.drawable.icn_styles,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new MutatorImpl<>(
                                BytePackable.HandStalk.values(),
                                WatchFacePreset::setMinuteHandStalk,
                                WatchFacePreset::getMinuteHandStalk),
                        WatchFacePreset::isMinuteHandOverridden),

                // Data for minute hand cutout in settings Activity.
                new WatchFacePickerConfigItem(
                        context.getString(R.string.config_preset_minute_hand_cutout),
                        R.drawable.icn_styles,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new MutatorImpl<>(
                                BytePackable.HandCutout.values(),
                                WatchFacePreset::setMinuteHandCutout,
                                WatchFacePreset::getMinuteHandCutout),
                        WatchFacePreset::isMinuteHandOverridden),

                // Data for minute hand style in settings Activity.
                new WatchFacePickerConfigItem(
                        context.getString(R.string.config_preset_minute_hand_style),
                        R.drawable.icn_styles,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new MutatorImpl<>(
                                BytePackable.Style.values(),
                                WatchFacePreset::setMinuteHandStyle,
                                WatchFacePreset::getMinuteHandStyle),
                        WatchFacePreset::isMinuteHandOverridden),

                // Data for second hand override in settings Activity.
                new ToggleConfigItem<>(
                        context.getString(R.string.config_preset_second_hand_override),
                        R.drawable.ic_notifications_white_24dp,
                        R.drawable.ic_notifications_off_white_24dp,
                        new Mutator<WatchFacePreset>() {
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
                        }),

                // Data for second hand shape in settings Activity.
                new WatchFacePickerConfigItem(
                        context.getString(R.string.config_preset_second_hand_shape),
                        R.drawable.icn_styles,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new MutatorImpl<>(
                                BytePackable.HandShape.values(),
                                WatchFacePreset::setSecondHandShape,
                                WatchFacePreset::getSecondHandShape),
                        WatchFacePreset::isSecondHandOverridden),

                // Data for second hand length in settings Activity.
                new WatchFacePickerConfigItem(
                        context.getString(R.string.config_preset_second_hand_length),
                        R.drawable.icn_styles,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new MutatorImpl<>(
                                BytePackable.HandLength.values(),
                                WatchFacePreset::setSecondHandLength,
                                WatchFacePreset::getSecondHandLength),
                        WatchFacePreset::isSecondHandOverridden),


                // Data for second hand thickness in settings Activity.
                new WatchFacePickerConfigItem(
                        context.getString(R.string.config_preset_second_hand_thickness),
                        R.drawable.icn_styles,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new MutatorImpl<>(
                                BytePackable.HandThickness.values(),
                                WatchFacePreset::setSecondHandThickness,
                                WatchFacePreset::getSecondHandThickness),
                        WatchFacePreset::isSecondHandOverridden),

                // Data for second hand style in settings Activity.
                new WatchFacePickerConfigItem(
                        context.getString(R.string.config_preset_second_hand_style),
                        R.drawable.icn_styles,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new MutatorImpl<>(
                                BytePackable.Style.values(),
                                WatchFacePreset::setSecondHandStyle,
                                WatchFacePreset::getSecondHandStyle),
                        WatchFacePreset::isSecondHandOverridden)
        );
    }
}
