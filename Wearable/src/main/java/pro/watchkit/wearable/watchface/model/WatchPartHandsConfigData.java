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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Arrays;
import java.util.List;

import pro.watchkit.wearable.watchface.R;
import pro.watchkit.wearable.watchface.config.WatchFaceSelectionActivity;
import pro.watchkit.wearable.watchface.watchface.WatchFaceGlobalDrawable;

public class WatchPartHandsConfigData extends ConfigData {
    @Override
    public List<ConfigItemType> getDataToPopulateAdapter(@NonNull Context context) {
        int watchFaceGlobalDrawableFlags = WatchFaceGlobalDrawable.PART_BACKGROUND |
                WatchFaceGlobalDrawable.PART_HANDS;
        return Arrays.asList(
                // A preview of the current watch face.
                new WatchFaceDrawableConfigItem(watchFaceGlobalDrawableFlags),

                // Data for hour hand shape in settings Activity.
                new PickerConfigItem(
                        context.getString(R.string.config_preset_hour_hand_shape),
                        R.drawable.icn_styles,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.HandShape.values(),
                                WatchFaceState::setHourHandShape,
                                WatchFaceState::getHourHandShape)),

                // Data for hour hand length in settings Activity.
                new PickerConfigItem(
                        context.getString(R.string.config_preset_hour_hand_length),
                        R.drawable.icn_styles,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.HandLength.values(),
                                WatchFaceState::setHourHandLength,
                                WatchFaceState::getHourHandLength)),

                // Data for hour hand thickness in settings Activity.
                new PickerConfigItem(
                        context.getString(R.string.config_preset_hour_hand_thickness),
                        R.drawable.icn_styles,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.HandThickness.values(),
                                WatchFaceState::setHourHandThickness,
                                WatchFaceState::getHourHandThickness)),

                // Data for hour hand stalk in settings Activity.
                new PickerConfigItem(
                        context.getString(R.string.config_preset_hour_hand_stalk),
                        R.drawable.icn_styles,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.HandStalk.values(),
                                WatchFaceState::setHourHandStalk,
                                WatchFaceState::getHourHandStalk)),

                // Data for hour hand cutout in settings Activity.
                new PickerConfigItem(
                        context.getString(R.string.config_preset_hour_hand_cutout),
                        R.drawable.icn_styles,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.HandCutout.values(),
                                WatchFaceState::setHourHandCutout,
                                WatchFaceState::getHourHandCutout)),

                // Data for hour hand style in settings Activity.
                new PickerConfigItem(
                        context.getString(R.string.config_preset_hour_hand_style),
                        R.drawable.icn_styles,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.Style.values(),
                                WatchFaceState::setHourHandStyle,
                                WatchFaceState::getHourHandStyle)),

                // Data for minute hand override in settings Activity.
                new ToggleConfigItem(
                        context.getString(R.string.config_preset_minute_hand_override),
                        R.drawable.ic_notifications_white_24dp,
                        R.drawable.ic_notifications_off_white_24dp,
                        new Mutator() {
                            @NonNull
                            @Override
                            public String[] permute(@NonNull WatchFaceState permutation) {
                                String[] result = new String[2];
                                permutation.setMinuteHandOverride(false);
                                result[0] = permutation.getString();
                                permutation.setMinuteHandOverride(true);
                                result[1] = permutation.getString();
                                return result;
                            }

                            @Nullable
                            public Enum getCurrentValue(WatchFaceState currentPreset) {
                                return null;
                            }
                        }),

                // Data for minute hand shape in settings Activity.
                new PickerConfigItem(
                        context.getString(R.string.config_preset_minute_hand_shape),
                        R.drawable.icn_styles,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.HandShape.values(),
                                WatchFaceState::setMinuteHandShape,
                                WatchFaceState::getMinuteHandShape),
                        WatchFaceState::isMinuteHandOverridden),

                // Data for minute hand length in settings Activity.
                new PickerConfigItem(
                        context.getString(R.string.config_preset_minute_hand_length),
                        R.drawable.icn_styles,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.HandLength.values(),
                                WatchFaceState::setMinuteHandLength,
                                WatchFaceState::getMinuteHandLength),
                        WatchFaceState::isMinuteHandOverridden),

                // Data for minute hand thickness in settings Activity.
                new PickerConfigItem(
                        context.getString(R.string.config_preset_minute_hand_thickness),
                        R.drawable.icn_styles,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.HandThickness.values(),
                                WatchFaceState::setMinuteHandThickness,
                                WatchFaceState::getMinuteHandThickness),
                        WatchFaceState::isMinuteHandOverridden),

                // Data for minute hand stalk in settings Activity.
                new PickerConfigItem(
                        context.getString(R.string.config_preset_minute_hand_stalk),
                        R.drawable.icn_styles,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.HandStalk.values(),
                                WatchFaceState::setMinuteHandStalk,
                                WatchFaceState::getMinuteHandStalk),
                        WatchFaceState::isMinuteHandOverridden),

                // Data for minute hand cutout in settings Activity.
                new PickerConfigItem(
                        context.getString(R.string.config_preset_minute_hand_cutout),
                        R.drawable.icn_styles,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.HandCutout.values(),
                                WatchFaceState::setMinuteHandCutout,
                                WatchFaceState::getMinuteHandCutout),
                        WatchFaceState::isMinuteHandOverridden),

                // Data for minute hand style in settings Activity.
                new PickerConfigItem(
                        context.getString(R.string.config_preset_minute_hand_style),
                        R.drawable.icn_styles,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.Style.values(),
                                WatchFaceState::setMinuteHandStyle,
                                WatchFaceState::getMinuteHandStyle),
                        WatchFaceState::isMinuteHandOverridden),

                // Data for second hand override in settings Activity.
                new ToggleConfigItem(
                        context.getString(R.string.config_preset_second_hand_override),
                        R.drawable.ic_notifications_white_24dp,
                        R.drawable.ic_notifications_off_white_24dp,
                        new Mutator() {
                            @NonNull
                            @Override
                            public String[] permute(@NonNull WatchFaceState permutation) {
                                String[] result = new String[2];
                                permutation.setSecondHandOverride(false);
                                result[0] = permutation.getString();
                                permutation.setSecondHandOverride(true);
                                result[1] = permutation.getString();
                                return result;
                            }

                            @Nullable
                            public Enum getCurrentValue(WatchFaceState currentPreset) {
                                return null;
                            }
                        }),

                // Data for second hand shape in settings Activity.
                new PickerConfigItem(
                        context.getString(R.string.config_preset_second_hand_shape),
                        R.drawable.icn_styles,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.HandShape.values(),
                                WatchFaceState::setSecondHandShape,
                                WatchFaceState::getSecondHandShape),
                        WatchFaceState::isSecondHandOverridden),

                // Data for second hand length in settings Activity.
                new PickerConfigItem(
                        context.getString(R.string.config_preset_second_hand_length),
                        R.drawable.icn_styles,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.HandLength.values(),
                                WatchFaceState::setSecondHandLength,
                                WatchFaceState::getSecondHandLength),
                        WatchFaceState::isSecondHandOverridden),


                // Data for second hand thickness in settings Activity.
                new PickerConfigItem(
                        context.getString(R.string.config_preset_second_hand_thickness),
                        R.drawable.icn_styles,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.HandThickness.values(),
                                WatchFaceState::setSecondHandThickness,
                                WatchFaceState::getSecondHandThickness),
                        WatchFaceState::isSecondHandOverridden),

                // Data for second hand style in settings Activity.
                new PickerConfigItem(
                        context.getString(R.string.config_preset_second_hand_style),
                        R.drawable.icn_styles,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.Style.values(),
                                WatchFaceState::setSecondHandStyle,
                                WatchFaceState::getSecondHandStyle),
                        WatchFaceState::isSecondHandOverridden)
        );
    }
}
