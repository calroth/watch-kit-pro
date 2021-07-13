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
 */

package pro.watchkit.wearable.watchface.model;

import androidx.annotation.NonNull;

import java.util.Arrays;
import java.util.List;

import pro.watchkit.wearable.watchface.R;
import pro.watchkit.wearable.watchface.config.WatchFaceSelectionActivity;
import pro.watchkit.wearable.watchface.watchface.WatchFaceGlobalDrawable;

public class WatchPartPipsConfigData extends ConfigData {
    @NonNull
    @Override
    public List<ConfigItemType> getDataToPopulateAdapter() {
        int watchFaceGlobalDrawableFlags = WatchFaceGlobalDrawable.PART_BACKGROUND |
                WatchFaceGlobalDrawable.PART_PIPS;
        int watchFaceGlobalDrawableFlagsSwatch = watchFaceGlobalDrawableFlags |
                WatchFaceGlobalDrawable.PART_SWATCH;

        return Arrays.asList(
                // Title.
                new LabelConfigItem(R.string.config_configure_pips),

                // A preview of the current watch face.
                new WatchFaceDrawableConfigItem(watchFaceGlobalDrawableFlags),

                // Data for pips display in settings Activity.
                new PickerConfigItem(
                        R.string.config_preset_pips_display,
                        R.drawable.ic_pips,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.PipsDisplay.values(),
                                WatchFaceState::setPipsDisplay,
                                WatchFaceState::getPipsDisplay)),

                // Data for pip margin in settings Activity.
                new PickerConfigItem(
                        R.string.config_preset_pip_margin,
                        R.drawable.ic_pips,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.PipMargin.values(),
                                WatchFaceState::setPipMargin,
                                WatchFaceState::getPipMargin)),

                // Data for pip background material in settings Activity.
                new PickerConfigItem(
                        R.string.config_preset_pip_background_material,
                        R.drawable.ic_pips,
                        watchFaceGlobalDrawableFlagsSwatch,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.Material.values(),
                                WatchFaceState::setPipBackgroundMaterial,
                                WatchFaceState::getPipBackgroundMaterial)),

                // Data for digit display in settings Activity.
                new PickerConfigItem(
                        R.string.config_preset_digit_display,
                        R.drawable.ic_pips,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.DigitDisplay.values(),
                                WatchFaceState::setDigitDisplay,
                                WatchFaceState::getDigitDisplay)),

                // Data for digit size in settings Activity.
                new PickerConfigItem(
                        R.string.config_preset_digit_size,
                        R.drawable.ic_pips,
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
                        R.drawable.ic_pips,
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
                        R.drawable.ic_pips,
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
                        R.drawable.ic_pips,
                        watchFaceGlobalDrawableFlagsSwatch,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.Material.values(),
                                WatchFaceState::setDigitMaterial,
                                WatchFaceState::getDigitMaterial),
                        WatchFaceState::isDigitVisible),

                // Data for four pip shape in settings Activity.
                new PickerConfigItem(
                        R.string.config_preset_four_pip_shape,
                        R.drawable.ic_pips,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.PipShape.values(),
                                WatchFaceState::setFourPipShape,
                                WatchFaceState::getFourPipShape),
                        WatchFaceState::isFourPipsVisible),

                // Data for four pip length in settings Activity.
                new PickerConfigItem(
                        R.string.config_preset_four_pip_size,
                        R.drawable.ic_pips,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.PipSize.values(),
                                WatchFaceState::setFourPipSize,
                                WatchFaceState::getFourPipSize),
                        WatchFaceState::isFourPipsVisible),

                // Data for four pip material in settings Activity.
                new PickerConfigItem(
                        R.string.config_preset_four_pip_material,
                        R.drawable.ic_pips,
                        watchFaceGlobalDrawableFlagsSwatch,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.Material.values(),
                                WatchFaceState::setFourPipMaterial,
                                WatchFaceState::getFourPipMaterial),
                        WatchFaceState::isFourPipsVisible),

                // Data for twelve pip override in settings Activity.
                new ToggleConfigItem(
                        R.string.config_preset_twelve_pip_override,
                        R.drawable.ic_notifications_white_24dp,
                        R.drawable.ic_notifications_off_white_24dp,
                        new BooleanMutator(WatchFaceState::setTwelvePipOverride),
                        WatchFaceState::isTwelvePipsVisible),

                // Data for twelve pip shape in settings Activity.
                new PickerConfigItem(
                        R.string.config_preset_twelve_pip_shape,
                        R.drawable.ic_pips,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.PipShape.values(),
                                WatchFaceState::setTwelvePipShape,
                                WatchFaceState::getTwelvePipShape),
                        WatchFaceState::isTwelvePipsOverridden),

                // Data for twelve pip length in settings Activity.
                new PickerConfigItem(
                        R.string.config_preset_twelve_pip_size,
                        R.drawable.ic_pips,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.PipSize.values(),
                                WatchFaceState::setTwelvePipSize,
                                WatchFaceState::getTwelvePipSize),
                        WatchFaceState::isTwelvePipsOverridden),

                // Data for twelve pip material in settings Activity.
                new PickerConfigItem(
                        R.string.config_preset_twelve_pip_material,
                        R.drawable.ic_pips,
                        watchFaceGlobalDrawableFlagsSwatch,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.Material.values(),
                                WatchFaceState::setTwelvePipMaterial,
                                WatchFaceState::getTwelvePipMaterial),
                        WatchFaceState::isTwelvePipsOverridden),

                // Data for sixty pip override in settings Activity.
                new ToggleConfigItem(
                        R.string.config_preset_sixty_pip_override,
                        R.drawable.ic_notifications_white_24dp,
                        R.drawable.ic_notifications_off_white_24dp,
                        new BooleanMutator(WatchFaceState::setSixtyPipOverride),
                        WatchFaceState::isSixtyPipsVisible),

                // Data for sixty pip shape in settings Activity.
                new PickerConfigItem(
                        R.string.config_preset_sixty_pip_shape,
                        R.drawable.ic_pips,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.PipShape.values(),
                                WatchFaceState::setSixtyPipShape,
                                WatchFaceState::getSixtyPipShape),
                        WatchFaceState::isSixtyPipsOverridden),

                // Data for sixty pip length in settings Activity.
                new PickerConfigItem(
                        R.string.config_preset_sixty_pip_size,
                        R.drawable.ic_pips,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.PipSize.values(),
                                WatchFaceState::setSixtyPipSize,
                                WatchFaceState::getSixtyPipSize),
                        WatchFaceState::isSixtyPipsOverridden),

                // Data for sixty pip material in settings Activity.
                new PickerConfigItem(
                        R.string.config_preset_sixty_pip_material,
                        R.drawable.ic_pips,
                        watchFaceGlobalDrawableFlagsSwatch,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.Material.values(),
                                WatchFaceState::setSixtyPipMaterial,
                                WatchFaceState::getSixtyPipMaterial),
                        WatchFaceState::isSixtyPipsOverridden),

//                // Data for background material in settings Activity.
//                new PickerConfigItem(
//                        R.string.config_preset_background_material,
//                        R.drawable.ic_pips,
//                        watchFaceGlobalDrawableFlagsSwatch,
//                        WatchFaceSelectionActivity.class,
//                        new EnumMutator<>(
//                                BytePackable.Material.values(),
//                                WatchFaceState::setBackgroundMaterial,
//                                WatchFaceState::getBackgroundMaterial)),

                // Help.
                new LabelConfigItem(R.string.config_configure_help,
                        R.string.config_configure_pips_help)
        );
    }
}
