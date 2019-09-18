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

public class ComplicationConfigData extends ConfigData {
    @NonNull
    @Override
    public List<ConfigItemType> getDataToPopulateAdapter() {
        int watchFaceGlobalDrawableFlags = WatchFaceGlobalDrawable.PART_BACKGROUND |
                WatchFaceGlobalDrawable.PART_HANDS |
                WatchFaceGlobalDrawable.PART_RINGS_ALL |
                WatchFaceGlobalDrawable.PART_COMPLICATIONS;
        int watchFaceGlobalDrawableFlagsStyle = watchFaceGlobalDrawableFlags |
                WatchFaceGlobalDrawable.PART_SWATCH;

        return Arrays.asList(
                // Title.
                new LabelConfigItem(R.string.config_configure_complications),

                // Complication picker from watch face drawable.
                new ComplicationConfigItem(R.drawable.add_complication),

                // Data for complication count in settings Activity.
                new PickerConfigItem(
                        R.string.config_complication_count,
                        R.drawable.ic_complications,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.ComplicationCount.values(),
                                WatchFaceState::setComplicationCount,
                                WatchFaceState::getComplicationCount)),

                // Data for complication rotation in settings Activity.
                new PickerConfigItem(
                        R.string.config_complication_rotation,
                        R.drawable.ic_complications,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.ComplicationRotation.values(),
                                WatchFaceState::setComplicationRotation,
                                WatchFaceState::getComplicationRotation)),

                // Data for complication size in settings Activity.
                new PickerConfigItem(
                        R.string.config_complication_size,
                        R.drawable.ic_complications,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.ComplicationSize.values(),
                                WatchFaceState::setComplicationSize,
                                WatchFaceState::getComplicationSize)),

                // Data for complication overlap in settings Activity.
                new PickerConfigItem(
                        R.string.config_complication_scale,
                        R.drawable.ic_complications,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.ComplicationScale.values(),
                                WatchFaceState::setComplicationScale,
                                WatchFaceState::getComplicationScale)),

                // Data for complication text style in settings Activity.
                new PickerConfigItem(
                        R.string.config_complication_text_style,
                        R.drawable.ic_complications,
                        watchFaceGlobalDrawableFlagsStyle,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.TextStyle.values(),
                                WatchFaceState::setComplicationTextStyle,
                                WatchFaceState::getComplicationTextStyle)),

                // Data for complication ring style in settings Activity.
                new PickerConfigItem(
                        R.string.config_complication_ring_style,
                        R.drawable.ic_complications,
                        watchFaceGlobalDrawableFlagsStyle,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.Style.values(),
                                WatchFaceState::setComplicationRingStyle,
                                WatchFaceState::getComplicationRingStyle)),

                // Data for complication background style in settings Activity.
                new PickerConfigItem(
                        R.string.config_complication_background_style,
                        R.drawable.ic_complications,
                        watchFaceGlobalDrawableFlagsStyle,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.Style.values(),
                                WatchFaceState::setComplicationBackgroundStyle,
                                WatchFaceState::getComplicationBackgroundStyle))
        );
    }
}
