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
        int watchFaceGlobalDrawableFlagsStyle = watchFaceGlobalDrawableFlags |
                WatchFaceGlobalDrawable.PART_SWATCH;

        return Arrays.asList(
                // Title.
                new LabelConfigItem(R.string.config_configure_hands),

                // A preview of the current watch face.
                new WatchFaceDrawableConfigItem(watchFaceGlobalDrawableFlags),

                // Data for hour hand shape in settings Activity.
                new PickerConfigItem(
                        R.string.config_preset_hour_hand_shape,
                        R.drawable.ic_hands,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.HandShape.values(),
                                WatchFaceState::setHourHandShape,
                                WatchFaceState::getHourHandShape)),

                // Data for hour hand length in settings Activity.
                new PickerConfigItem(
                        R.string.config_preset_hour_hand_length,
                        R.drawable.ic_hands,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.HandLength.values(),
                                WatchFaceState::setHourHandLength,
                                WatchFaceState::getHourHandLength)),

                // Data for hour hand thickness in settings Activity.
                new PickerConfigItem(
                        R.string.config_preset_hour_hand_thickness,
                        R.drawable.ic_hands,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.HandThickness.values(),
                                WatchFaceState::setHourHandThickness,
                                WatchFaceState::getHourHandThickness)),

                // Data for hour hand stalk in settings Activity.
                new PickerConfigItem(
                        R.string.config_preset_hour_hand_stalk,
                        R.drawable.ic_hands,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.HandStalk.values(),
                                WatchFaceState::setHourHandStalk,
                                WatchFaceState::getHourHandStalk)),

                // Data for hour hand style in settings Activity.
                new PickerConfigItem(
                        R.string.config_preset_hour_hand_style,
                        R.drawable.ic_hands,
                        watchFaceGlobalDrawableFlagsStyle,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.Style.values(),
                                WatchFaceState::setHourHandStyle,
                                WatchFaceState::getHourHandStyle)),

                // Data for hour hand cutout shape in settings Activity.
                new PickerConfigItem(
                        R.string.config_preset_hour_hand_cutout_shape,
                        R.drawable.ic_hands,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.HandCutoutShape.values(),
                                WatchFaceState::setHourHandCutoutShape,
                                WatchFaceState::getHourHandCutoutShape)),

                // Data for hour hand cutout style in settings Activity.
                new PickerConfigItem(
                        R.string.config_preset_hour_hand_cutout_style,
                        R.drawable.ic_hands,
                        watchFaceGlobalDrawableFlagsStyle,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.Style.values(),
                                WatchFaceState::setHourHandCutoutStyle,
                                WatchFaceState::getHourHandCutoutStyle)),

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
                                BytePackable.HandShape.values(),
                                WatchFaceState::setMinuteHandShape,
                                WatchFaceState::getMinuteHandShape),
                        WatchFaceState::isMinuteHandOverridden),

                // Data for minute hand length in settings Activity.
                new PickerConfigItem(
                        R.string.config_preset_minute_hand_length,
                        R.drawable.ic_hands,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.HandLength.values(),
                                WatchFaceState::setMinuteHandLength,
                                WatchFaceState::getMinuteHandLength),
                        WatchFaceState::isMinuteHandOverridden),

                // Data for minute hand thickness in settings Activity.
                new PickerConfigItem(
                        R.string.config_preset_minute_hand_thickness,
                        R.drawable.ic_hands,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.HandThickness.values(),
                                WatchFaceState::setMinuteHandThickness,
                                WatchFaceState::getMinuteHandThickness),
                        WatchFaceState::isMinuteHandOverridden),

                // Data for minute hand stalk in settings Activity.
                new PickerConfigItem(
                        R.string.config_preset_minute_hand_stalk,
                        R.drawable.ic_hands,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.HandStalk.values(),
                                WatchFaceState::setMinuteHandStalk,
                                WatchFaceState::getMinuteHandStalk),
                        WatchFaceState::isMinuteHandOverridden),

                // Data for minute hand style in settings Activity.
                new PickerConfigItem(
                        R.string.config_preset_minute_hand_style,
                        R.drawable.ic_hands,
                        watchFaceGlobalDrawableFlagsStyle,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.Style.values(),
                                WatchFaceState::setMinuteHandStyle,
                                WatchFaceState::getMinuteHandStyle),
                        WatchFaceState::isMinuteHandOverridden),

                // Data for minute hand cutout shape in settings Activity.
                new PickerConfigItem(
                        R.string.config_preset_minute_hand_cutout_shape,
                        R.drawable.ic_hands,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.HandCutoutShape.values(),
                                WatchFaceState::setMinuteHandCutoutShape,
                                WatchFaceState::getMinuteHandCutoutShape),
                        WatchFaceState::isMinuteHandOverridden),

                // Data for minute hand cutout style in settings Activity.
                new PickerConfigItem(
                        R.string.config_preset_minute_hand_cutout_style,
                        R.drawable.ic_hands,
                        watchFaceGlobalDrawableFlagsStyle,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.Style.values(),
                                WatchFaceState::setMinuteHandCutoutStyle,
                                WatchFaceState::getMinuteHandCutoutStyle),
                        WatchFaceState::isMinuteHandOverridden),

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
                                BytePackable.HandShape.values(),
                                WatchFaceState::setSecondHandShape,
                                WatchFaceState::getSecondHandShape),
                        WatchFaceState::isSecondHandOverridden),

                // Data for second hand length in settings Activity.
                new PickerConfigItem(
                        R.string.config_preset_second_hand_length,
                        R.drawable.ic_hands,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.HandLength.values(),
                                WatchFaceState::setSecondHandLength,
                                WatchFaceState::getSecondHandLength),
                        WatchFaceState::isSecondHandOverridden),


                // Data for second hand thickness in settings Activity.
                new PickerConfigItem(
                        R.string.config_preset_second_hand_thickness,
                        R.drawable.ic_hands,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.HandThickness.values(),
                                WatchFaceState::setSecondHandThickness,
                                WatchFaceState::getSecondHandThickness),
                        WatchFaceState::isSecondHandOverridden),

                // Data for second hand style in settings Activity.
                new PickerConfigItem(
                        R.string.config_preset_second_hand_style,
                        R.drawable.ic_hands,
                        watchFaceGlobalDrawableFlagsStyle,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.Style.values(),
                                WatchFaceState::setSecondHandStyle,
                                WatchFaceState::getSecondHandStyle),
                        WatchFaceState::isSecondHandOverridden)
        );
    }
}
