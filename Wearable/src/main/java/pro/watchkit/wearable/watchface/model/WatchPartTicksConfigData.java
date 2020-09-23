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

public class WatchPartTicksConfigData extends ConfigData {
    @NonNull
    @Override
    public List<ConfigItemType> getDataToPopulateAdapter() {
        int watchFaceGlobalDrawableFlags = WatchFaceGlobalDrawable.PART_BACKGROUND |
                WatchFaceGlobalDrawable.PART_TICKS;
        int watchFaceGlobalDrawableFlagsSwatch = watchFaceGlobalDrawableFlags |
                WatchFaceGlobalDrawable.PART_SWATCH;

        return Arrays.asList(
                // Title.
                new LabelConfigItem(R.string.config_configure_ticks),

                // A preview of the current watch face.
                new WatchFaceDrawableConfigItem(watchFaceGlobalDrawableFlags),

                // Data for ticks display in settings Activity.
                new PickerConfigItem(
                        R.string.config_preset_ticks_display,
                        R.drawable.ic_ticks,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.TicksDisplay.values(),
                                WatchFaceState::setTicksDisplay,
                                WatchFaceState::getTicksDisplay)),

                // Data for tick margin in settings Activity.
                new PickerConfigItem(
                        R.string.config_preset_tick_margin,
                        R.drawable.ic_ticks,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.TickMargin.values(),
                                WatchFaceState::setTickMargin,
                                WatchFaceState::getTickMargin)),

                // Data for tick background material in settings Activity.
                new PickerConfigItem(
                        R.string.config_preset_tick_background_material,
                        R.drawable.ic_ticks,
                        watchFaceGlobalDrawableFlagsSwatch,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.Material.values(),
                                WatchFaceState::setTickBackgroundMaterial,
                                WatchFaceState::getTickBackgroundMaterial)),

                // Data for digit display in settings Activity.
                new PickerConfigItem(
                        R.string.config_preset_digit_display,
                        R.drawable.ic_ticks,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.DigitDisplay.values(),
                                WatchFaceState::setDigitDisplay,
                                WatchFaceState::getDigitDisplay)),

                // Data for digit size in settings Activity.
                new PickerConfigItem(
                        R.string.config_preset_digit_size,
                        R.drawable.ic_ticks,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.DigitSize.values(),
                                WatchFaceState::setDigitSize,
                                WatchFaceState::getDigitSize),
                        WatchFaceState::isDigitVisible),

                // Data for digit rotation in settings Activity.
                new PickerConfigItem(
                        R.string.config_preset_digit_rotation,
                        R.drawable.ic_ticks,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.DigitRotation.values(),
                                WatchFaceState::setDigitRotation,
                                WatchFaceState::getDigitRotation),
                        WatchFaceState::isDigitVisible),

                // Data for digit format in settings Activity.
                new PickerConfigItem(
                        R.string.config_preset_digit_format,
                        R.drawable.ic_ticks,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.DigitFormat.values(),
                                WatchFaceState::setDigitFormat,
                                WatchFaceState::getDigitFormat),
                        WatchFaceState::isDigitVisible),

                // Data for digit material in settings Activity.
                new PickerConfigItem(
                        R.string.config_preset_digit_material,
                        R.drawable.ic_ticks,
                        watchFaceGlobalDrawableFlagsSwatch,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.Material.values(),
                                WatchFaceState::setDigitMaterial,
                                WatchFaceState::getDigitMaterial),
                        WatchFaceState::isDigitVisible),

                // Data for four tick shape in settings Activity.
                new PickerConfigItem(
                        R.string.config_preset_four_tick_shape,
                        R.drawable.ic_ticks,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.TickShape.values(),
                                WatchFaceState::setFourTickShape,
                                WatchFaceState::getFourTickShape),
                        WatchFaceState::isFourTicksVisible),

                // Data for four tick length in settings Activity.
                new PickerConfigItem(
                        R.string.config_preset_four_tick_size,
                        R.drawable.ic_ticks,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.TickSize.values(),
                                WatchFaceState::setFourTickSize,
                                WatchFaceState::getFourTickSize),
                        WatchFaceState::isFourTicksVisible),

                // Data for four tick material in settings Activity.
                new PickerConfigItem(
                        R.string.config_preset_four_tick_material,
                        R.drawable.ic_ticks,
                        watchFaceGlobalDrawableFlagsSwatch,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.Material.values(),
                                WatchFaceState::setFourTickMaterial,
                                WatchFaceState::getFourTickMaterial),
                        WatchFaceState::isFourTicksVisible),

                // Data for twelve tick override in settings Activity.
                new ToggleConfigItem(
                        R.string.config_preset_twelve_tick_override,
                        R.drawable.ic_notifications_white_24dp,
                        R.drawable.ic_notifications_off_white_24dp,
                        new BooleanMutator(WatchFaceState::setTwelveTickOverride),
                        WatchFaceState::isTwelveTicksVisible),

                // Data for twelve tick shape in settings Activity.
                new PickerConfigItem(
                        R.string.config_preset_twelve_tick_shape,
                        R.drawable.ic_ticks,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.TickShape.values(),
                                WatchFaceState::setTwelveTickShape,
                                WatchFaceState::getTwelveTickShape),
                        WatchFaceState::isTwelveTicksOverridden),

                // Data for twelve tick length in settings Activity.
                new PickerConfigItem(
                        R.string.config_preset_twelve_tick_size,
                        R.drawable.ic_ticks,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.TickSize.values(),
                                WatchFaceState::setTwelveTickSize,
                                WatchFaceState::getTwelveTickSize),
                        WatchFaceState::isTwelveTicksOverridden),

                // Data for twelve tick material in settings Activity.
                new PickerConfigItem(
                        R.string.config_preset_twelve_tick_material,
                        R.drawable.ic_ticks,
                        watchFaceGlobalDrawableFlagsSwatch,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.Material.values(),
                                WatchFaceState::setTwelveTickMaterial,
                                WatchFaceState::getTwelveTickMaterial),
                        WatchFaceState::isTwelveTicksOverridden),

                // Data for sixty tick override in settings Activity.
                new ToggleConfigItem(
                        R.string.config_preset_sixty_tick_override,
                        R.drawable.ic_notifications_white_24dp,
                        R.drawable.ic_notifications_off_white_24dp,
                        new BooleanMutator(WatchFaceState::setSixtyTickOverride),
                        WatchFaceState::isSixtyTicksVisible),

                // Data for sixty tick shape in settings Activity.
                new PickerConfigItem(
                        R.string.config_preset_sixty_tick_shape,
                        R.drawable.ic_ticks,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.TickShape.values(),
                                WatchFaceState::setSixtyTickShape,
                                WatchFaceState::getSixtyTickShape),
                        WatchFaceState::isSixtyTicksOverridden),

                // Data for sixty tick length in settings Activity.
                new PickerConfigItem(
                        R.string.config_preset_sixty_tick_size,
                        R.drawable.ic_ticks,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.TickSize.values(),
                                WatchFaceState::setSixtyTickSize,
                                WatchFaceState::getSixtyTickSize),
                        WatchFaceState::isSixtyTicksOverridden),

                // Data for sixty tick material in settings Activity.
                new PickerConfigItem(
                        R.string.config_preset_sixty_tick_material,
                        R.drawable.ic_ticks,
                        watchFaceGlobalDrawableFlagsSwatch,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.Material.values(),
                                WatchFaceState::setSixtyTickMaterial,
                                WatchFaceState::getSixtyTickMaterial),
                        WatchFaceState::isSixtyTicksOverridden),

                // Data for background material in settings Activity.
                new PickerConfigItem(
                        R.string.config_preset_background_material,
                        R.drawable.ic_ticks,
                        watchFaceGlobalDrawableFlagsSwatch,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.Material.values(),
                                WatchFaceState::setBackgroundMaterial,
                                WatchFaceState::getBackgroundMaterial)),

                // Help.
                new LabelConfigItem(R.string.config_configure_help,
                        R.string.config_configure_ticks_help)
        );
    }
}
