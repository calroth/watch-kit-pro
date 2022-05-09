/*
 * Copyright (C) 2018-2022 Terence Tan
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

import androidx.annotation.NonNull;

import java.util.Arrays;
import java.util.List;

import pro.watchkit.wearable.watchface.R;
import pro.watchkit.wearable.watchface.config.WatchFaceSelectionActivity;
import pro.watchkit.wearable.watchface.watchface.WatchFaceGlobalDrawable;

public class WatchPartHandsConfigData extends ConfigData {
    @NonNull
    @Override
    public List<ConfigItemType> getDataToPopulateAdapter() {
        int watchFaceGlobalDrawableFlags = WatchFaceGlobalDrawable.PART_BACKGROUND |
                WatchFaceGlobalDrawable.PART_HANDS;
        int watchFaceGlobalDrawableFlagsSwatch = watchFaceGlobalDrawableFlags |
                WatchFaceGlobalDrawable.PART_SWATCH;

        return Arrays.asList(
                // Heading.
                new HeadingLabelConfigItem(R.string.config_configure_hands),

                // A preview of the current watch face.
                new WatchFaceDrawableConfigItem(watchFaceGlobalDrawableFlags),

                // Data for hour hand shape in settings Activity.
                new PickerConfigItem(
                        R.string.config_preset_hour_hand_shape,
                        R.drawable.ic_hands,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.HandShape.finalValues,
                                WatchFaceState::setHourHandShape)),

                // Data for hour hand length in settings Activity.
                new PickerConfigItem(
                        R.string.config_preset_hour_hand_length,
                        R.drawable.ic_hands,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.HandLength.finalValues,
                                WatchFaceState::setHourHandLength)),

                // Data for hour hand thickness in settings Activity.
                new PickerConfigItem(
                        R.string.config_preset_hour_hand_thickness,
                        R.drawable.ic_hands,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.HandThickness.finalValues,
                                WatchFaceState::setHourHandThickness)),

                // Data for hour hand stalk in settings Activity.
                new PickerConfigItem(
                        R.string.config_preset_hour_hand_stalk,
                        R.drawable.ic_hands,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.HandStalk.finalValues,
                                WatchFaceState::setHourHandStalk)),

                // Data for hour hand material in settings Activity.
                new PickerConfigItem(
                        R.string.config_preset_hour_hand_material,
                        R.drawable.ic_hands,
                        watchFaceGlobalDrawableFlagsSwatch,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.Material.finalValues,
                                WatchFaceState::setHourHandMaterial)),

                // Data for hour hand cutout in settings Activity.
                new ToggleConfigItem(
                        R.string.config_preset_hour_hand_cutout,
                        R.drawable.ic_notifications_white_24dp,
                        R.drawable.ic_notifications_off_white_24dp,
                        new BooleanMutator(WatchFaceState::setHourHandCutout)),

                // Data for hour hand cutout shape in settings Activity.
                new PickerConfigItem(
                        R.string.config_preset_hour_hand_cutout_shape,
                        R.drawable.ic_hands,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.HandCutoutShape.finalValues,
                                WatchFaceState::setHourHandCutoutShape),
                        WatchFaceState::isHourHandCutout),

                // Data for hour hand cutout material in settings Activity.
                new PickerConfigItem(
                        R.string.config_preset_hour_hand_cutout_material,
                        R.drawable.ic_hands,
                        watchFaceGlobalDrawableFlagsSwatch,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.HandCutoutMaterial.finalValues,
                                WatchFaceState::setHourHandCutoutMaterial),
                        WatchFaceState::isHourHandCutout),

                // Data for minute hand override in settings Activity.
                new ToggleConfigItem(
                        R.string.config_preset_minute_hand_override,
                        R.drawable.ic_notifications_white_24dp,
                        R.drawable.ic_notifications_off_white_24dp,
                        new BooleanMutator(WatchFaceState::setMinuteHandOverride)),

                // Data for minute hand shape in settings Activity.
                new PickerConfigItem(
                        R.string.config_preset_minute_hand_shape,
                        R.drawable.ic_hands,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.HandShape.finalValues,
                                WatchFaceState::setMinuteHandShape),
                        WatchFaceState::isMinuteHandOverridden),

                // Data for minute hand length in settings Activity.
                new PickerConfigItem(
                        R.string.config_preset_minute_hand_length,
                        R.drawable.ic_hands,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.HandLength.finalValues,
                                WatchFaceState::setMinuteHandLength),
                        WatchFaceState::isMinuteHandOverridden),

                // Data for minute hand thickness in settings Activity.
                new PickerConfigItem(
                        R.string.config_preset_minute_hand_thickness,
                        R.drawable.ic_hands,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.HandThickness.finalValues,
                                WatchFaceState::setMinuteHandThickness),
                        WatchFaceState::isMinuteHandOverridden),

                // Data for minute hand stalk in settings Activity.
                new PickerConfigItem(
                        R.string.config_preset_minute_hand_stalk,
                        R.drawable.ic_hands,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.HandStalk.finalValues,
                                WatchFaceState::setMinuteHandStalk),
                        WatchFaceState::isMinuteHandOverridden),

                // Data for minute hand material in settings Activity.
                new PickerConfigItem(
                        R.string.config_preset_minute_hand_material,
                        R.drawable.ic_hands,
                        watchFaceGlobalDrawableFlagsSwatch,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.Material.finalValues,
                                WatchFaceState::setMinuteHandMaterial),
                        WatchFaceState::isMinuteHandOverridden),

                // Data for minute hand cutout in settings Activity.
                new ToggleConfigItem(
                        R.string.config_preset_minute_hand_cutout,
                        R.drawable.ic_notifications_white_24dp,
                        R.drawable.ic_notifications_off_white_24dp,
                        new BooleanMutator(WatchFaceState::setMinuteHandCutout),
                        WatchFaceState::isMinuteHandOverridden),

                // Data for minute hand cutout shape in settings Activity.
                new PickerConfigItem(
                        R.string.config_preset_minute_hand_cutout_shape,
                        R.drawable.ic_hands,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.HandCutoutShape.finalValues,
                                WatchFaceState::setMinuteHandCutoutShape),
                        WatchFaceState::isMinuteHandOverriddenAndCutout),

                // Data for minute hand cutout material in settings Activity.
                new PickerConfigItem(
                        R.string.config_preset_minute_hand_cutout_material,
                        R.drawable.ic_hands,
                        watchFaceGlobalDrawableFlagsSwatch,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.HandCutoutMaterial.finalValues,
                                WatchFaceState::setMinuteHandCutoutMaterial),
                        WatchFaceState::isMinuteHandOverriddenAndCutout),

                // Data for second hand override in settings Activity.
                new ToggleConfigItem(
                        R.string.config_preset_second_hand_override,
                        R.drawable.ic_notifications_white_24dp,
                        R.drawable.ic_notifications_off_white_24dp,
                        new BooleanMutator(WatchFaceState::setSecondHandOverride)),

                // Data for second hand shape in settings Activity.
                new PickerConfigItem(
                        R.string.config_preset_second_hand_shape,
                        R.drawable.ic_hands,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.HandShape.finalValues,
                                WatchFaceState::setSecondHandShape),
                        WatchFaceState::isSecondHandOverridden),

                // Data for second hand length in settings Activity.
                new PickerConfigItem(
                        R.string.config_preset_second_hand_length,
                        R.drawable.ic_hands,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.HandLength.finalValues,
                                WatchFaceState::setSecondHandLength),
                        WatchFaceState::isSecondHandOverridden),


                // Data for second hand thickness in settings Activity.
                new PickerConfigItem(
                        R.string.config_preset_second_hand_thickness,
                        R.drawable.ic_hands,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.HandThickness.finalValues,
                                WatchFaceState::setSecondHandThickness),
                        WatchFaceState::isSecondHandOverridden),

                // Data for second hand material in settings Activity.
                new PickerConfigItem(
                        R.string.config_preset_second_hand_material,
                        R.drawable.ic_hands,
                        watchFaceGlobalDrawableFlagsSwatch,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.Material.finalValues,
                                WatchFaceState::setSecondHandMaterial),
                        WatchFaceState::isSecondHandOverridden),

                // Help.
                new HelpLabelConfigItem(R.string.config_configure_hands_help)
        );
    }
}
