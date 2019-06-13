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

public class WatchPartTicksConfigData extends ConfigData {
    @Override
    public List<ConfigItemType> getDataToPopulateAdapter(Context context) {
        int watchFaceGlobalDrawableFlags = WatchFaceGlobalDrawable.PART_BACKGROUND |
                WatchFaceGlobalDrawable.PART_TICKS;
        return Arrays.asList(
                // A preview of the current watch face.
                new WatchFaceDrawableConfigItem(watchFaceGlobalDrawableFlags),

                // Data for ticks display in settings Activity.
                new WatchFacePickerConfigItem(
                        context.getString(R.string.config_preset_ticks_display),
                        R.drawable.icn_styles,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new MutatorImpl<>(
                                BytePackable.TicksDisplay.values(),
                                WatchFaceState::setTicksDisplay,
                                WatchFaceState::getTicksDisplay)),

                // Data for four tick shape in settings Activity.
                new WatchFacePickerConfigItem(
                        context.getString(R.string.config_preset_four_tick_shape),
                        R.drawable.icn_styles,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new MutatorImpl<>(
                                BytePackable.TickShape.values(),
                                WatchFaceState::setFourTickShape,
                                WatchFaceState::getFourTickShape),
                        WatchFaceState::isFourTicksVisible),

                // Data for four tick length in settings Activity.
                new WatchFacePickerConfigItem(
                        context.getString(R.string.config_preset_four_tick_length),
                        R.drawable.icn_styles,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new MutatorImpl<>(
                                BytePackable.TickLength.values(),
                                WatchFaceState::setFourTickLength,
                                WatchFaceState::getFourTickLength),
                        WatchFaceState::isFourTicksVisible),

                // Data for four tick thickness in settings Activity.
                new WatchFacePickerConfigItem(
                        context.getString(R.string.config_preset_four_tick_thickness),
                        R.drawable.icn_styles,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new MutatorImpl<>(
                                BytePackable.TickThickness.values(),
                                WatchFaceState::setFourTickThickness,
                                WatchFaceState::getFourTickThickness),
                        WatchFaceState::isFourTicksVisible),

                // Data for four tick radius position in settings Activity.
                new WatchFacePickerConfigItem(
                        context.getString(R.string.config_preset_four_tick_radius_position),
                        R.drawable.icn_styles,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new MutatorImpl<>(
                                BytePackable.TickRadiusPosition.values(),
                                WatchFaceState::setFourTickRadiusPosition,
                                WatchFaceState::getFourTickRadiusPosition),
                        WatchFaceState::isFourTicksVisible),

                // Data for four tick style in settings Activity.
                new WatchFacePickerConfigItem(
                        context.getString(R.string.config_preset_four_tick_style),
                        R.drawable.icn_styles,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new MutatorImpl<>(
                                BytePackable.Style.values(),
                                WatchFaceState::setFourTickStyle,
                                WatchFaceState::getFourTickStyle),
                        WatchFaceState::isFourTicksVisible),

                // Data for twelve tick override in settings Activity.
                new ToggleConfigItem(
                        context.getString(R.string.config_preset_twelve_tick_override),
                        R.drawable.ic_notifications_white_24dp,
                        R.drawable.ic_notifications_off_white_24dp,
                        new Mutator() {
                            @Override
                            public String[] permute(WatchFaceState permutation) {
                                String[] result = new String[2];
                                permutation.setTwelveTickOverride(false);
                                result[0] = permutation.getString();
                                permutation.setTwelveTickOverride(true);
                                result[1] = permutation.getString();
                                return result;
                            }

                            public Enum getCurrentValue(WatchFaceState currentPreset) {
                                return null;
                            }
                        }),

                // Data for twelve tick shape in settings Activity.
                new WatchFacePickerConfigItem(
                        context.getString(R.string.config_preset_twelve_tick_shape),
                        R.drawable.icn_styles,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new MutatorImpl<>(
                                BytePackable.TickShape.values(),
                                WatchFaceState::setTwelveTickShape,
                                WatchFaceState::getTwelveTickShape),
                        WatchFaceState::isTwelveTicksOverridden),

                // Data for twelve tick length in settings Activity.
                new WatchFacePickerConfigItem(
                        context.getString(R.string.config_preset_twelve_tick_length),
                        R.drawable.icn_styles,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new MutatorImpl<>(
                                BytePackable.TickLength.values(),
                                WatchFaceState::setTwelveTickLength,
                                WatchFaceState::getTwelveTickLength),
                        WatchFaceState::isTwelveTicksOverridden),

                // Data for twelve tick thickness in settings Activity.
                new WatchFacePickerConfigItem(
                        context.getString(R.string.config_preset_twelve_tick_thickness),
                        R.drawable.icn_styles,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new MutatorImpl<>(
                                BytePackable.TickThickness.values(),
                                WatchFaceState::setTwelveTickThickness,
                                WatchFaceState::getTwelveTickThickness),
                        WatchFaceState::isTwelveTicksOverridden),

                // Data for twelve tick radius position in settings Activity.
                new WatchFacePickerConfigItem(
                        context.getString(R.string.config_preset_twelve_tick_radius_position),
                        R.drawable.icn_styles,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new MutatorImpl<>(
                                BytePackable.TickRadiusPosition.values(),
                                WatchFaceState::setTwelveTickRadiusPosition,
                                WatchFaceState::getTwelveTickRadiusPosition),
                        WatchFaceState::isTwelveTicksOverridden),

                // Data for twelve tick style in settings Activity.
                new WatchFacePickerConfigItem(
                        context.getString(R.string.config_preset_twelve_tick_style),
                        R.drawable.icn_styles,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new MutatorImpl<>(
                                BytePackable.Style.values(),
                                WatchFaceState::setTwelveTickStyle,
                                WatchFaceState::getTwelveTickStyle),
                        WatchFaceState::isTwelveTicksOverridden),

                // Data for sixty tick override in settings Activity.
                new ToggleConfigItem(
                        context.getString(R.string.config_preset_sixty_tick_override),
                        R.drawable.ic_notifications_white_24dp,
                        R.drawable.ic_notifications_off_white_24dp,
                        new Mutator() {
                            @Override
                            public String[] permute(WatchFaceState permutation) {
                                String[] result = new String[2];
                                permutation.setSixtyTickOverride(false);
                                result[0] = permutation.getString();
                                permutation.setSixtyTickOverride(true);
                                result[1] = permutation.getString();
                                return result;
                            }

                            public Enum getCurrentValue(WatchFaceState currentPreset) {
                                return null;
                            }
                        }),

                // Data for sixty tick shape in settings Activity.
                new WatchFacePickerConfigItem(
                        context.getString(R.string.config_preset_sixty_tick_shape),
                        R.drawable.icn_styles,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new MutatorImpl<>(
                                BytePackable.TickShape.values(),
                                WatchFaceState::setSixtyTickShape,
                                WatchFaceState::getSixtyTickShape),
                        WatchFaceState::isSixtyTicksOverridden),

                // Data for sixty tick length in settings Activity.
                new WatchFacePickerConfigItem(
                        context.getString(R.string.config_preset_sixty_tick_length),
                        R.drawable.icn_styles,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new MutatorImpl<>(
                                BytePackable.TickLength.values(),
                                WatchFaceState::setSixtyTickLength,
                                WatchFaceState::getSixtyTickLength),
                        WatchFaceState::isSixtyTicksOverridden),

                // Data for sixty tick thickness in settings Activity.
                new WatchFacePickerConfigItem(
                        context.getString(R.string.config_preset_sixty_tick_thickness),
                        R.drawable.icn_styles,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new MutatorImpl<>(
                                BytePackable.TickThickness.values(),
                                WatchFaceState::setSixtyTickThickness,
                                WatchFaceState::getSixtyTickThickness),
                        WatchFaceState::isSixtyTicksOverridden),

                // Data for sixty tick radius position in settings Activity.
                new WatchFacePickerConfigItem(
                        context.getString(R.string.config_preset_sixty_tick_radius_position),
                        R.drawable.icn_styles,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new MutatorImpl<>(
                                BytePackable.TickRadiusPosition.values(),
                                WatchFaceState::setSixtyTickRadiusPosition,
                                WatchFaceState::getSixtyTickRadiusPosition),
                        WatchFaceState::isSixtyTicksOverridden),

                // Data for sixty tick style in settings Activity.
                new WatchFacePickerConfigItem(
                        context.getString(R.string.config_preset_sixty_tick_style),
                        R.drawable.icn_styles,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new MutatorImpl<>(
                                BytePackable.Style.values(),
                                WatchFaceState::setSixtyTickStyle,
                                WatchFaceState::getSixtyTickStyle),
                        WatchFaceState::isSixtyTicksOverridden),

                // Data for background style in settings Activity.
                new WatchFacePickerConfigItem(
                        context.getString(R.string.config_preset_background_style),
                        R.drawable.icn_styles,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new MutatorImpl<>(
                                BytePackable.Style.values(),
                                WatchFaceState::setBackgroundStyle,
                                WatchFaceState::getBackgroundStyle))
        );
    }
}
