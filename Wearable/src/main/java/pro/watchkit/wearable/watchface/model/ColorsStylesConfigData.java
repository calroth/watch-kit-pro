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
import pro.watchkit.wearable.watchface.config.ColorSelectionActivity;
import pro.watchkit.wearable.watchface.config.WatchFaceSelectionActivity;
import pro.watchkit.wearable.watchface.watchface.WatchFaceGlobalDrawable;

public class ColorsStylesConfigData extends ConfigData {
    @Override
    public List<ConfigItemType> getDataToPopulateAdapter(@NonNull Context context) {
        int watchFaceGlobalDrawableFlags = WatchFaceGlobalDrawable.PART_BACKGROUND |
                WatchFaceGlobalDrawable.PART_TICKS |
                WatchFaceGlobalDrawable.PART_RINGS_ALL |
                WatchFaceGlobalDrawable.PART_HANDS;
        return Arrays.asList(
                // A preview of the current watch face.
                new WatchFaceDrawableConfigItem(watchFaceGlobalDrawableFlags),

                // Data for fill color UX in settings Activity.
                new ColorPickerConfigItem(
                        context.getString(R.string.config_fill_color_label),
                        R.drawable.icn_styles,
                        PaintBox.ColorType.FILL,
                        ColorSelectionActivity.class),

                // Data for accent color UX in settings Activity.
                new ColorPickerConfigItem(
                        context.getString(R.string.config_accent_color_label),
                        R.drawable.icn_styles,
                        PaintBox.ColorType.ACCENT,
                        ColorSelectionActivity.class),

                // Data for highlight/marker (second hand) color UX in settings Activity.
                new ColorPickerConfigItem(
                        context.getString(R.string.config_marker_color_label),
                        R.drawable.icn_styles,
                        PaintBox.ColorType.HIGHLIGHT,
                        ColorSelectionActivity.class),

                // Data for base color UX in settings Activity.
                new ColorPickerConfigItem(
                        context.getString(R.string.config_base_color_label),
                        R.drawable.icn_styles,
                        PaintBox.ColorType.BASE,
                        ColorSelectionActivity.class),

                // Data for fill highlight style gradient in settings Activity.
                new PickerConfigItem(
                        context.getString(R.string.config_preset_fill_highlight_style_gradient),
                        R.drawable.icn_styles,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.StyleGradient.values(),
                                WatchFaceState::setFillHighlightStyleGradient,
                                WatchFaceState::getFillHighlightStyleGradient)),

                // Data for fill highlight style texture in settings Activity.
                new PickerConfigItem(
                        context.getString(R.string.config_preset_fill_highlight_style_gradient),
                        R.drawable.icn_styles,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.StyleTexture.values(),
                                WatchFaceState::setFillHighlightStyleTexture,
                                WatchFaceState::getFillHighlightStyleTexture)),

                // Data for accent fill style gradient in settings Activity.
                new PickerConfigItem(
                        context.getString(R.string.config_preset_accent_fill_style_gradient),
                        R.drawable.icn_styles,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.StyleGradient.values(),
                                WatchFaceState::setAccentFillStyleGradient,
                                WatchFaceState::getAccentFillStyleGradient)),

                // Data for accent fill style texture in settings Activity.
                new PickerConfigItem(
                        context.getString(R.string.config_preset_accent_fill_style_gradient),
                        R.drawable.icn_styles,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.StyleTexture.values(),
                                WatchFaceState::setAccentFillStyleTexture,
                                WatchFaceState::getAccentFillStyleTexture)),

                // Data for accent highlight style gradient in settings Activity.
                new PickerConfigItem(
                        context.getString(R.string.config_preset_accent_highlight_style_gradient),
                        R.drawable.icn_styles,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.StyleGradient.values(),
                                WatchFaceState::setAccentHighlightStyleGradient,
                                WatchFaceState::getAccentHighlightStyleGradient)),

                // Data for accent highlight style texture in settings Activity.
                new PickerConfigItem(
                        context.getString(R.string.config_preset_accent_highlight_style_gradient),
                        R.drawable.icn_styles,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.StyleTexture.values(),
                                WatchFaceState::setAccentHighlightStyleTexture,
                                WatchFaceState::getAccentHighlightStyleTexture)),

                // Data for base accent style gradient in settings Activity.
                new PickerConfigItem(
                        context.getString(R.string.config_preset_base_accent_style_gradient),
                        R.drawable.icn_styles,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.StyleGradient.values(),
                                WatchFaceState::setBaseAccentStyleGradient,
                                WatchFaceState::getBaseAccentStyleGradient)),

                // Data for base accent style texture in settings Activity.
                new PickerConfigItem(
                        context.getString(R.string.config_preset_base_accent_style_gradient),
                        R.drawable.icn_styles,
                        watchFaceGlobalDrawableFlags,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.StyleTexture.values(),
                                WatchFaceState::setBaseAccentStyleTexture,
                                WatchFaceState::getBaseAccentStyleTexture))
        );
    }
}
