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

public class ComplicationConfigData extends ConfigData {
    @Override
    public List<ConfigItemType> getDataToPopulateAdapter(Context context) {
        int watchFaceGlobalDrawableFlags = WatchFaceGlobalDrawable.PART_BACKGROUND |
                WatchFaceGlobalDrawable.PART_HANDS |
                WatchFaceGlobalDrawable.PART_RINGS_ALL;

        return Arrays.asList(
                // Complication picker from watch face drawable.
                new ComplicationConfigItem(R.drawable.add_complication),

                // Data for complication count in settings Activity.
                new WatchFacePickerConfigItem(
                        context.getString(R.string.config_complication_count),
                        R.drawable.icn_styles,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new SettingsMutatorImpl<>(
                                BytePackable.ComplicationCount.values(),
                                Settings::setComplicationCountEnum,
                                Settings::getComplicationCountEnum), 0),

                // Data for complication rotation in settings Activity.
                new WatchFacePickerConfigItem(
                        context.getString(R.string.config_complication_rotation),
                        R.drawable.icn_styles,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new SettingsMutatorImpl<>(
                                BytePackable.ComplicationRotation.values(),
                                Settings::setComplicationRotation,
                                Settings::getComplicationRotation), 0),

                // Data for complication ring style in settings Activity.
                new WatchFacePickerConfigItem(
                        context.getString(R.string.config_complication_ring_style),
                        R.drawable.icn_styles,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new SettingsMutatorImpl<>(
                                BytePackable.Style.values(),
                                Settings::setComplicationRingStyle,
                                Settings::getComplicationRingStyle), 0),

                // Data for complication background style in settings Activity.
                new WatchFacePickerConfigItem(
                        context.getString(R.string.config_complication_background_style),
                        R.drawable.icn_styles,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new SettingsMutatorImpl<>(
                                BytePackable.Style.values(),
                                Settings::setComplicationBackgroundStyle,
                                Settings::getComplicationBackgroundStyle), 0)
        );
    }
}
