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

import java.util.Arrays;
import java.util.List;

import pro.watchkit.wearable.watchface.R;
import pro.watchkit.wearable.watchface.config.WatchFaceSelectionActivity;
import pro.watchkit.wearable.watchface.watchface.WatchFaceGlobalDrawable;

public class WatchPartTicksConfigData extends ConfigData {
    @Override
    public List<ConfigItemType> getDataToPopulateAdapter(@NonNull Context context) {
        int watchFaceGlobalDrawableFlags = WatchFaceGlobalDrawable.PART_BACKGROUND |
                WatchFaceGlobalDrawable.PART_TICKS;
        int watchFaceGlobalDrawableFlagsStyle = watchFaceGlobalDrawableFlags |
                WatchFaceGlobalDrawable.PART_SWATCH;

        return Arrays.asList(
                // Title.
                new LabelConfigItem(R.string.config_configure_ticks),

                // A preview of the current watch face.
                new WatchFaceDrawableConfigItem(watchFaceGlobalDrawableFlags),

                // Data for ticks display in settings Activity.
                new PickerConfigItem(
                        context.getString(R.string.config_preset_ticks_display),
                        R.drawable.icn_styles,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.TicksDisplay.values(),
                                WatchFaceState::setTicksDisplay,
                                WatchFaceState::getTicksDisplay)),

                // Data for four tick shape in settings Activity.
                new PickerConfigItem(
                        context.getString(R.string.config_preset_four_tick_shape),
                        R.drawable.icn_styles,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.TickShape.values(),
                                WatchFaceState::setFourTickShape,
                                WatchFaceState::getFourTickShape),
                        WatchFaceState::isFourTicksVisible),

                // Data for four tick length in settings Activity.
                new PickerConfigItem(
                        context.getString(R.string.config_preset_four_tick_length),
                        R.drawable.icn_styles,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.TickLength.values(),
                                WatchFaceState::setFourTickLength,
                                WatchFaceState::getFourTickLength),
                        WatchFaceState::isFourTicksVisible),

                // Data for four tick thickness in settings Activity.
                new PickerConfigItem(
                        context.getString(R.string.config_preset_four_tick_thickness),
                        R.drawable.icn_styles,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.TickThickness.values(),
                                WatchFaceState::setFourTickThickness,
                                WatchFaceState::getFourTickThickness),
                        WatchFaceState::isFourTicksVisible),

                // Data for four tick radius position in settings Activity.
                new PickerConfigItem(
                        context.getString(R.string.config_preset_four_tick_radius_position),
                        R.drawable.icn_styles,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.TickRadiusPosition.values(),
                                WatchFaceState::setFourTickRadiusPosition,
                                WatchFaceState::getFourTickRadiusPosition),
                        WatchFaceState::isFourTicksVisible),

                // Data for four tick style in settings Activity.
                new PickerConfigItem(
                        context.getString(R.string.config_preset_four_tick_style),
                        R.drawable.icn_styles,
                        watchFaceGlobalDrawableFlagsStyle,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.Style.values(),
                                WatchFaceState::setFourTickStyle,
                                WatchFaceState::getFourTickStyle),
                        WatchFaceState::isFourTicksVisible),

                // Data for digit display in settings Activity.
                new PickerConfigItem(
                        context.getString(R.string.config_preset_digit_display),
                        R.drawable.icn_styles,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.DigitDisplay.values(),
                                WatchFaceState::setDigitDisplay,
                                WatchFaceState::getDigitDisplay)),

                // Data for digit rotation in settings Activity.
                new PickerConfigItem(
                        context.getString(R.string.config_preset_digit_rotation),
                        R.drawable.icn_styles,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.DigitRotation.values(),
                                WatchFaceState::setDigitRotation,
                                WatchFaceState::getDigitRotation),
                        WatchFaceState::isDigitVisible),

                // Data for digit format in settings Activity.
                new PickerConfigItem(
                        context.getString(R.string.config_preset_digit_format),
                        R.drawable.icn_styles,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.DigitFormat.values(),
                                WatchFaceState::setDigitFormat,
                                WatchFaceState::getDigitFormat),
                        WatchFaceState::isDigitVisible),

                // Data for digit style in settings Activity.
                new PickerConfigItem(
                        context.getString(R.string.config_preset_digit_style),
                        R.drawable.icn_styles,
                        watchFaceGlobalDrawableFlagsStyle,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.Style.values(),
                                WatchFaceState::setDigitStyle,
                                WatchFaceState::getDigitStyle),
                        WatchFaceState::isDigitVisible),

                // Data for twelve tick override in settings Activity.
                new ToggleConfigItem(
                        context.getString(R.string.config_preset_twelve_tick_override),
                        R.drawable.ic_notifications_white_24dp,
                        R.drawable.ic_notifications_off_white_24dp,
                        new BooleanMutator(WatchFaceState::setTwelveTickOverride)),

                // Data for twelve tick shape in settings Activity.
                new PickerConfigItem(
                        context.getString(R.string.config_preset_twelve_tick_shape),
                        R.drawable.icn_styles,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.TickShape.values(),
                                WatchFaceState::setTwelveTickShape,
                                WatchFaceState::getTwelveTickShape),
                        WatchFaceState::isTwelveTicksOverridden),

                // Data for twelve tick length in settings Activity.
                new PickerConfigItem(
                        context.getString(R.string.config_preset_twelve_tick_length),
                        R.drawable.icn_styles,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.TickLength.values(),
                                WatchFaceState::setTwelveTickLength,
                                WatchFaceState::getTwelveTickLength),
                        WatchFaceState::isTwelveTicksOverridden),

                // Data for twelve tick thickness in settings Activity.
                new PickerConfigItem(
                        context.getString(R.string.config_preset_twelve_tick_thickness),
                        R.drawable.icn_styles,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.TickThickness.values(),
                                WatchFaceState::setTwelveTickThickness,
                                WatchFaceState::getTwelveTickThickness),
                        WatchFaceState::isTwelveTicksOverridden),

                // Data for twelve tick radius position in settings Activity.
                new PickerConfigItem(
                        context.getString(R.string.config_preset_twelve_tick_radius_position),
                        R.drawable.icn_styles,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.TickRadiusPosition.values(),
                                WatchFaceState::setTwelveTickRadiusPosition,
                                WatchFaceState::getTwelveTickRadiusPosition),
                        WatchFaceState::isTwelveTicksOverridden),

                // Data for twelve tick style in settings Activity.
                new PickerConfigItem(
                        context.getString(R.string.config_preset_twelve_tick_style),
                        R.drawable.icn_styles,
                        watchFaceGlobalDrawableFlagsStyle,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.Style.values(),
                                WatchFaceState::setTwelveTickStyle,
                                WatchFaceState::getTwelveTickStyle),
                        WatchFaceState::isTwelveTicksOverridden),

                // Data for sixty tick override in settings Activity.
                new ToggleConfigItem(
                        context.getString(R.string.config_preset_sixty_tick_override),
                        R.drawable.ic_notifications_white_24dp,
                        R.drawable.ic_notifications_off_white_24dp,
                        new BooleanMutator(WatchFaceState::setSixtyTickOverride)),

                // Data for sixty tick shape in settings Activity.
                new PickerConfigItem(
                        context.getString(R.string.config_preset_sixty_tick_shape),
                        R.drawable.icn_styles,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.TickShape.values(),
                                WatchFaceState::setSixtyTickShape,
                                WatchFaceState::getSixtyTickShape),
                        WatchFaceState::isSixtyTicksOverridden),

                // Data for sixty tick length in settings Activity.
                new PickerConfigItem(
                        context.getString(R.string.config_preset_sixty_tick_length),
                        R.drawable.icn_styles,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.TickLength.values(),
                                WatchFaceState::setSixtyTickLength,
                                WatchFaceState::getSixtyTickLength),
                        WatchFaceState::isSixtyTicksOverridden),

                // Data for sixty tick thickness in settings Activity.
                new PickerConfigItem(
                        context.getString(R.string.config_preset_sixty_tick_thickness),
                        R.drawable.icn_styles,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.TickThickness.values(),
                                WatchFaceState::setSixtyTickThickness,
                                WatchFaceState::getSixtyTickThickness),
                        WatchFaceState::isSixtyTicksOverridden),

                // Data for sixty tick radius position in settings Activity.
                new PickerConfigItem(
                        context.getString(R.string.config_preset_sixty_tick_radius_position),
                        R.drawable.icn_styles,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.TickRadiusPosition.values(),
                                WatchFaceState::setSixtyTickRadiusPosition,
                                WatchFaceState::getSixtyTickRadiusPosition),
                        WatchFaceState::isSixtyTicksOverridden),

                // Data for sixty tick style in settings Activity.
                new PickerConfigItem(
                        context.getString(R.string.config_preset_sixty_tick_style),
                        R.drawable.icn_styles,
                        watchFaceGlobalDrawableFlagsStyle,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.Style.values(),
                                WatchFaceState::setSixtyTickStyle,
                                WatchFaceState::getSixtyTickStyle),
                        WatchFaceState::isSixtyTicksOverridden),

                // Data for background style in settings Activity.
                new PickerConfigItem(
                        context.getString(R.string.config_preset_background_style),
                        R.drawable.icn_styles,
                        watchFaceGlobalDrawableFlagsStyle,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.Style.values(),
                                WatchFaceState::setBackgroundStyle,
                                WatchFaceState::getBackgroundStyle))
        );
    }
}
