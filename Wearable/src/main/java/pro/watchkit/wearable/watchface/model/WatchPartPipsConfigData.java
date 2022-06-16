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

public class WatchPartPipsConfigData extends ConfigData {
    @NonNull
    @Override
    public List<ConfigItemType> getDataToPopulateAdapter() {
        int watchFaceGlobalDrawableFlags = WatchFaceGlobalDrawable.PART_BACKGROUND |
                WatchFaceGlobalDrawable.PART_PIPS;
        int watchFaceGlobalDrawableFlagsSwatch = watchFaceGlobalDrawableFlags |
                WatchFaceGlobalDrawable.PART_SWATCH;

        return Arrays.asList(
                // Heading.
                new HeadingLabelConfigItem(R.string.config_configure_pips),

                // A preview of the current watch face.
                new WatchFaceDrawableConfigItem(watchFaceGlobalDrawableFlags),

                // Data for Random sub-activity in settings Activity.
                new PickerConfigItem(
                        R.string.config_view_random_pips,
                        R.drawable.ic_filter_tilt_shift,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new RandomMutator() {
                            /**
                             * A custom Mutator which offers random WatchFaceState permutations!
                             *
                             * @param clone WatchFaceState, which must be a clone, but in
                             *              this case we'll ignore it...
                             * @return A set of random permutations, good luck, have fun!
                             */
                            @NonNull
                            @Override
                            public Permutation[] getPermutations(@NonNull WatchFaceState clone) {
                                final int SIZE = 16;
                                Permutation[] p = new Permutation[SIZE];

                                // Slot 0 is the current selection.
                                p[0] = new Permutation(clone.getString(), clone.getWatchFaceName());

                                // Roll the dice and generate a bunch of random watch faces!
                                for (int i = 1; i < SIZE; i++) {
                                    String name = "Random Pips " + i;
                                    permuteRandomPips(clone);
                                    p[i] = new Permutation(clone.getString(), name);
                                }
                                return p;
                            }
                        }, WatchFaceState::isDeveloperMode),

                // Data for pips display in settings Activity.
                new PickerConfigItem(
                        R.string.config_preset_pips_display,
                        R.drawable.ic_pips,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.PipsDisplay.finalValues,
                                WatchFaceState::setPipsDisplay)),

                // Data for pip margin in settings Activity.
                new PickerConfigItem(
                        R.string.config_preset_pip_margin,
                        R.drawable.ic_pips,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.PipMargin.finalValues,
                                WatchFaceState::setPipMargin)),

                // Data for pip background material in settings Activity.
                new PickerConfigItem(
                        R.string.config_preset_pip_background_material,
                        R.drawable.ic_pips,
                        watchFaceGlobalDrawableFlagsSwatch,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.Material.finalValues,
                                WatchFaceState::setPipBackgroundMaterial)),

                // Data for digit display in settings Activity.
                new PickerConfigItem(
                        R.string.config_preset_digit_display,
                        R.drawable.ic_pips,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.DigitDisplay.finalValues,
                                WatchFaceState::setDigitDisplay)),

                // Data for digit size in settings Activity.
                new PickerConfigItem(
                        R.string.config_preset_digit_size,
                        R.drawable.ic_pips,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.DigitSize.finalValues,
                                WatchFaceState::setDigitSize),
                        WatchFaceState::isDigitVisible),

                // Data for digit rotation in settings Activity.
                new PickerConfigItem(
                        R.string.config_preset_digit_rotation,
                        R.drawable.ic_pips,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.DigitRotation.finalValues,
                                WatchFaceState::setDigitRotation),
                        WatchFaceState::isDigitVisible),

                // Data for digit format in settings Activity.
                new PickerConfigItem(
                        R.string.config_preset_digit_format,
                        R.drawable.ic_pips,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.DigitFormat.finalValues,
                                WatchFaceState::setDigitFormat),
                        WatchFaceState::isDigitVisible),

                // Data for digit material in settings Activity.
                new PickerConfigItem(
                        R.string.config_preset_digit_material,
                        R.drawable.ic_pips,
                        watchFaceGlobalDrawableFlagsSwatch,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.Material.finalValues,
                                WatchFaceState::setDigitMaterial),
                        WatchFaceState::isDigitVisible),

                // Data for quarter pip shape in settings Activity.
                new PickerConfigItem(
                        R.string.config_preset_quarter_pip_shape,
                        R.drawable.ic_pips,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.PipShape.finalValues,
                                WatchFaceState::setQuarterPipShape),
                        WatchFaceState::isQuarterPipsVisible),

                // Data for quarter pip length in settings Activity.
                new PickerConfigItem(
                        R.string.config_preset_quarter_pip_size,
                        R.drawable.ic_pips,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.PipSize.finalValues,
                                WatchFaceState::setQuarterPipSize),
                        WatchFaceState::isQuarterPipsVisible),

                // Data for quarter pip material in settings Activity.
                new PickerConfigItem(
                        R.string.config_preset_quarter_pip_material,
                        R.drawable.ic_pips,
                        watchFaceGlobalDrawableFlagsSwatch,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.Material.finalValues,
                                WatchFaceState::setQuarterPipMaterial),
                        WatchFaceState::isQuarterPipsVisible),

                // Data for hour pip override in settings Activity.
                new ToggleConfigItem(
                        R.string.config_preset_hour_pip_override,
                        R.drawable.ic_notifications,
                        R.drawable.ic_notifications_off,
                        new BooleanMutator(WatchFaceState::setHourPipOverride),
                        WatchFaceState::isHourPipsVisible),

                // Data for hour pip shape in settings Activity.
                new PickerConfigItem(
                        R.string.config_preset_hour_pip_shape,
                        R.drawable.ic_pips,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.PipShape.finalValues,
                                WatchFaceState::setHourPipShape),
                        WatchFaceState::isHourPipsOverridden),

                // Data for hour pip length in settings Activity.
                new PickerConfigItem(
                        R.string.config_preset_hour_pip_size,
                        R.drawable.ic_pips,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.PipSize.finalValues,
                                WatchFaceState::setHourPipSize),
                        WatchFaceState::isHourPipsOverridden),

                // Data for hour pip material in settings Activity.
                new PickerConfigItem(
                        R.string.config_preset_hour_pip_material,
                        R.drawable.ic_pips,
                        watchFaceGlobalDrawableFlagsSwatch,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.Material.finalValues,
                                WatchFaceState::setHourPipMaterial),
                        WatchFaceState::isHourPipsOverridden),

                // Data for minute pip override in settings Activity.
                new ToggleConfigItem(
                        R.string.config_preset_minute_pip_override,
                        R.drawable.ic_notifications,
                        R.drawable.ic_notifications_off,
                        new BooleanMutator(WatchFaceState::setMinutePipOverride),
                        WatchFaceState::isMinutePipsVisible),

                // Data for minute pip shape in settings Activity.
                new PickerConfigItem(
                        R.string.config_preset_minute_pip_shape,
                        R.drawable.ic_pips,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.PipShape.finalValues,
                                WatchFaceState::setMinutePipShape),
                        WatchFaceState::isMinutePipsOverridden),

                // Data for minute pip length in settings Activity.
                new PickerConfigItem(
                        R.string.config_preset_minute_pip_size,
                        R.drawable.ic_pips,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.PipSize.finalValues,
                                WatchFaceState::setMinutePipSize),
                        WatchFaceState::isMinutePipsOverridden),

                // Data for minute pip material in settings Activity.
                new PickerConfigItem(
                        R.string.config_preset_minute_pip_material,
                        R.drawable.ic_pips,
                        watchFaceGlobalDrawableFlagsSwatch,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.Material.finalValues,
                                WatchFaceState::setMinutePipMaterial),
                        WatchFaceState::isMinutePipsOverridden),

//                // Data for background material in settings Activity.
//                new PickerConfigItem(
//                        R.string.config_preset_background_material,
//                        R.drawable.ic_pips,
//                        watchFaceGlobalDrawableFlagsSwatch,
//                        WatchFaceSelectionActivity.class,
//                        new EnumMutator<>(
//                                BytePackable.Material.finalValues,
//                                WatchFaceState::setBackgroundMaterial)),

                // Help.
                new HelpLabelConfigItem(R.string.config_configure_pips_help)
        );
    }
}
