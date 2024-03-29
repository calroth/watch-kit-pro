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

public class ComplicationConfigData extends ConfigData {
    @NonNull
    @Override
    public List<ConfigItemType> getDataToPopulateAdapter() {
        int watchFaceGlobalDrawableFlags = WatchFaceGlobalDrawable.PART_BACKGROUND |
                WatchFaceGlobalDrawable.PART_HANDS |
                WatchFaceGlobalDrawable.PART_RINGS_ALL |
                WatchFaceGlobalDrawable.PART_COMPLICATIONS;
        int watchFaceGlobalDrawableFlagsSwatch = watchFaceGlobalDrawableFlags |
                WatchFaceGlobalDrawable.PART_SWATCH;

        return Arrays.asList(
                // Heading.
                new HeadingLabelConfigItem(R.string.config_configure_complications),

                // Complication picker from watch face drawable.
                new ComplicationConfigItem(R.drawable.ic_add_complication),

                // Data for complication count in settings Activity.
                new PickerConfigItem(
                        R.string.config_complication_count,
                        R.drawable.ic_complications,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.ComplicationCount.finalValues,
                                WatchFaceState::setComplicationCount)),

                // Data for complication rotation in settings Activity.
                new PickerConfigItem(
                        R.string.config_complication_rotation,
                        R.drawable.ic_complications,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.ComplicationRotation.finalValues,
                                WatchFaceState::setComplicationRotation)),

                // Data for complication size in settings Activity.
                new PickerConfigItem(
                        R.string.config_complication_size,
                        R.drawable.ic_complications,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.ComplicationSize.finalValues,
                                WatchFaceState::setComplicationSize)),

                // Data for complication overlap in settings Activity.
                new PickerConfigItem(
                        R.string.config_complication_scale,
                        R.drawable.ic_complications,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.ComplicationScale.finalValues,
                                WatchFaceState::setComplicationScale)),

                // Data for complication ring material in settings Activity.
                new PickerConfigItem(
                        R.string.config_complication_ring_material,
                        R.drawable.ic_complications,
                        watchFaceGlobalDrawableFlagsSwatch,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.Material.finalValues,
                                WatchFaceState::setComplicationRingMaterial)),

                // Data for complication background material in settings Activity.
                new PickerConfigItem(
                        R.string.config_complication_background_material,
                        R.drawable.ic_complications,
                        watchFaceGlobalDrawableFlagsSwatch,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.Material.finalValues,
                                WatchFaceState::setComplicationBackgroundMaterial)),

                // Help.
                new HelpLabelConfigItem(R.string.config_configure_complications_help)
        );
    }
}
