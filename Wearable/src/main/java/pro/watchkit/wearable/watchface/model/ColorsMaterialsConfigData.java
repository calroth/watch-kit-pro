/*
 * Copyright (C) 2018-2020 Terence Tan
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
import pro.watchkit.wearable.watchface.config.ColorSelectionActivity;
import pro.watchkit.wearable.watchface.config.ConfigActivity;
import pro.watchkit.wearable.watchface.watchface.WatchFaceGlobalDrawable;

public class ColorsMaterialsConfigData extends ConfigData {
    @NonNull
    @Override
    public List<ConfigItemType> getDataToPopulateAdapter() {
        int watchFaceGlobalDrawableFlags = WatchFaceGlobalDrawable.PART_BACKGROUND |
                WatchFaceGlobalDrawable.PART_TICKS |
                WatchFaceGlobalDrawable.PART_RINGS_ALL |
                WatchFaceGlobalDrawable.PART_HANDS;

        return Arrays.asList(
                // Title.
                new LabelConfigItem(R.string.config_configure_colors_materials),

                // A preview of the current watch face.
                new WatchFaceDrawableConfigItem(watchFaceGlobalDrawableFlags),

                // Data for fill color UX in settings Activity.
                new ColorPickerConfigItem(
                        R.string.config_fill_color_label,
                        R.drawable.ic_color_lens,
                        PaintBox.ColorType.FILL,
                        ColorSelectionActivity.class),

                // Data for accent color UX in settings Activity.
                new ColorPickerConfigItem(
                        R.string.config_accent_color_label,
                        R.drawable.ic_color_lens,
                        PaintBox.ColorType.ACCENT,
                        ColorSelectionActivity.class),

                // Data for highlight/marker (second hand) color UX in settings Activity.
                new ColorPickerConfigItem(
                        R.string.config_marker_color_label,
                        R.drawable.ic_color_lens,
                        PaintBox.ColorType.HIGHLIGHT,
                        ColorSelectionActivity.class),

                // Data for base color UX in settings Activity.
                new ColorPickerConfigItem(
                        R.string.config_base_color_label,
                        R.drawable.ic_color_lens,
                        PaintBox.ColorType.BASE,
                        ColorSelectionActivity.class),

                // Data for Fill Highlight Material sub-activity in settings Activity.
                new ConfigActivityConfigItem(
                        R.string.config_preset_fill_highlight_material,
                        R.drawable.ic_settings,
                        MaterialConfigData.FillHighlight.class,
                        ConfigActivity.class),

                // Data for Accent Fill Material sub-activity in settings Activity.
                new ConfigActivityConfigItem(
                        R.string.config_preset_accent_fill_material,
                        R.drawable.ic_settings,
                        MaterialConfigData.AccentFill.class,
                        ConfigActivity.class),

                // Data for Accent Highlight Material sub-activity in settings Activity.
                new ConfigActivityConfigItem(
                        R.string.config_preset_accent_highlight_material,
                        R.drawable.ic_settings,
                        MaterialConfigData.AccentHighlight.class,
                        ConfigActivity.class),

                // Data for Base Accent Material sub-activity in settings Activity.
                new ConfigActivityConfigItem(
                        R.string.config_preset_base_accent_material,
                        R.drawable.ic_settings,
                        MaterialConfigData.BaseAccent.class,
                        ConfigActivity.class)
        );
    }
}
