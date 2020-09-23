/*
 * Copyright (C) 2020 Terence Tan
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
import androidx.annotation.StringRes;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import pro.watchkit.wearable.watchface.R;
import pro.watchkit.wearable.watchface.config.ColorSelectionActivity;
import pro.watchkit.wearable.watchface.config.WatchFaceSelectionActivity;
import pro.watchkit.wearable.watchface.model.BytePackable.Material;
import pro.watchkit.wearable.watchface.watchface.WatchFaceGlobalDrawable;

public abstract class MaterialConfigData extends ConfigData {

    @NonNull
    @Override
    public List<ConfigItemType> getDataToPopulateAdapter() {
        int watchFaceGlobalDrawableFlags = WatchFaceGlobalDrawable.PART_BACKGROUND |
                WatchFaceGlobalDrawable.PART_TICKS |
                WatchFaceGlobalDrawable.PART_RINGS_ALL |
                WatchFaceGlobalDrawable.PART_HANDS;
        int watchFaceGlobalDrawableFlagsSwatch = watchFaceGlobalDrawableFlags |
                WatchFaceGlobalDrawable.PART_SWATCH;

        return Arrays.asList(
                // Title.
                new LabelConfigItem(getTitleResourceId()),

                // Data for primary color UX settings Activity.
                new ColorPickerConfigItem(
                        getPrimaryNameResourceId(),
                        R.drawable.ic_color_lens,
                        getPrimaryColorType(),
                        ColorSelectionActivity.class),

                // Data for secondary color UX in settings Activity.
                new ColorPickerConfigItem(
                        getSecondaryNameResourceId(),
                        R.drawable.ic_color_lens,
                        getSecondaryColorType(),
                        ColorSelectionActivity.class),

                // Data for material gradient in settings Activity.
                new PickerConfigItem(
                        getMaterialGradientNameResourceId(),
                        R.drawable.ic_color_lens,
                        watchFaceGlobalDrawableFlagsSwatch,
                        WatchFaceSelectionActivity.class,
                        getMaterial(),
                        new EnumMutator<>(
                                BytePackable.MaterialGradient.values(),
                                getMaterialGradientSetter(),
                                getMaterialGradientGetter())),

                // Data for material texture in settings Activity.
                new PickerConfigItem(
                        getMaterialTextureNameResourceId(),
                        R.drawable.ic_color_lens,
                        watchFaceGlobalDrawableFlagsSwatch,
                        WatchFaceSelectionActivity.class,
                        new EnumMutator<>(
                                BytePackable.MaterialTexture.values(),
                                getMaterialTextureSetter(),
                                getMaterialTextureGetter())),

                // Help.
                new LabelConfigItem(R.string.config_configure_help,
                        R.string.config_configure_material_help)
        );
    }

    /**
     * The Material we're configuring.
     *
     * @return the Material we're configuring
     */
    @NonNull
    abstract Material getMaterial();

    /**
     * The ResourceId for the title's string.
     *
     * @return ResourceId for the title's string
     */
    @StringRes
    abstract int getTitleResourceId();

    /**
     * The ResourceId for the primary color name's string.
     *
     * @return ResourceId for the primary color name's string
     */
    @StringRes
    abstract int getPrimaryNameResourceId();

    /**
     * The ColorType of the primary color.
     *
     * @return the ColorType of the primary color
     */
    @NonNull
    abstract PaintBox.ColorType getPrimaryColorType();

    /**
     * The ResourceId for the secondary color name's string.
     *
     * @return ResourceId for the primary color name's string
     */
    @StringRes
    abstract int getSecondaryNameResourceId();

    /**
     * The ColorType of the secondary color.
     *
     * @return the ColorType of the primary color
     */
    @NonNull
    abstract PaintBox.ColorType getSecondaryColorType();

    /**
     * The ResourceId for the material gradient name's string.
     *
     * @return ResourceId for the material gradient name's string
     */
    @StringRes
    abstract int getMaterialGradientNameResourceId();

    /**
     * The function which does the setting of the material gradient
     *
     * @return a lambda which sets the MaterialGradient of the given WatchFaceState
     */
    @NonNull
    abstract BiConsumer<WatchFaceState, BytePackable.MaterialGradient> getMaterialGradientSetter();

    /**
     * The function which does the getting of the material gradient
     *
     * @return a lambda which gets the MaterialGradient of the given WatchFaceState
     */
    @NonNull
    abstract Function<WatchFaceState, BytePackable.MaterialGradient> getMaterialGradientGetter();

    /**
     * The ResourceId for the material texture name's string.
     *
     * @return ResourceId for the material texture name's string
     */
    @StringRes
    abstract int getMaterialTextureNameResourceId();

    /**
     * The function which does the setting of the material texture
     *
     * @return a lambda which sets the MaterialTexture of the given WatchFaceState
     */
    @NonNull
    abstract BiConsumer<WatchFaceState, BytePackable.MaterialTexture> getMaterialTextureSetter();

    /**
     * The function which does the getting of the material texture
     *
     * @return a lambda which gets the MaterialTexture of the given WatchFaceState
     */
    @NonNull
    abstract Function<WatchFaceState, BytePackable.MaterialTexture> getMaterialTextureGetter();

    public static final class FillHighlight extends MaterialConfigData {
        @NonNull
        @Override
        Material getMaterial() {
            return Material.FILL_HIGHLIGHT;
        }

        @Override
        int getTitleResourceId() {
            return R.string.config_preset_fill_highlight_material;
        }

        @Override
        int getPrimaryNameResourceId() {
            return R.string.config_fill_color_label;
        }

        @NonNull
        @Override
        PaintBox.ColorType getPrimaryColorType() {
            return PaintBox.ColorType.FILL;
        }

        @Override
        int getSecondaryNameResourceId() {
            return R.string.config_marker_color_label;
        }

        @NonNull
        @Override
        PaintBox.ColorType getSecondaryColorType() {
            return PaintBox.ColorType.HIGHLIGHT;
        }

        @Override
        int getMaterialGradientNameResourceId() {
            return R.string.config_preset_fill_highlight_material_gradient;
        }

        @NonNull
        @Override
        BiConsumer<WatchFaceState, BytePackable.MaterialGradient> getMaterialGradientSetter() {
            return WatchFaceState::setFillHighlightMaterialGradient;
        }

        @NonNull
        @Override
        Function<WatchFaceState, BytePackable.MaterialGradient> getMaterialGradientGetter() {
            return WatchFaceState::getFillHighlightMaterialGradient;
        }

        @Override
        int getMaterialTextureNameResourceId() {
            return R.string.config_preset_fill_highlight_material_texture;
        }

        @NonNull
        @Override
        BiConsumer<WatchFaceState, BytePackable.MaterialTexture> getMaterialTextureSetter() {
            return WatchFaceState::setFillHighlightMaterialTexture;
        }

        @NonNull
        @Override
        Function<WatchFaceState, BytePackable.MaterialTexture> getMaterialTextureGetter() {
            return WatchFaceState::getFillHighlightMaterialTexture;
        }
    }

    public static final class AccentFill extends MaterialConfigData {
        @NonNull
        @Override
        Material getMaterial() {
            return Material.ACCENT_FILL;
        }

        @Override
        int getTitleResourceId() {
            return R.string.config_preset_accent_fill_material;
        }

        @Override
        int getPrimaryNameResourceId() {
            return R.string.config_accent_color_label;
        }

        @NonNull
        @Override
        PaintBox.ColorType getPrimaryColorType() {
            return PaintBox.ColorType.ACCENT;
        }

        @Override
        int getSecondaryNameResourceId() {
            return R.string.config_fill_color_label;
        }

        @NonNull
        @Override
        PaintBox.ColorType getSecondaryColorType() {
            return PaintBox.ColorType.FILL;
        }

        @Override
        int getMaterialGradientNameResourceId() {
            return R.string.config_preset_accent_fill_material_gradient;
        }

        @NonNull
        @Override
        BiConsumer<WatchFaceState, BytePackable.MaterialGradient> getMaterialGradientSetter() {
            return WatchFaceState::setAccentFillMaterialGradient;
        }

        @NonNull
        @Override
        Function<WatchFaceState, BytePackable.MaterialGradient> getMaterialGradientGetter() {
            return WatchFaceState::getAccentFillMaterialGradient;
        }

        @Override
        int getMaterialTextureNameResourceId() {
            return R.string.config_preset_accent_fill_material_texture;
        }

        @NonNull
        @Override
        BiConsumer<WatchFaceState, BytePackable.MaterialTexture> getMaterialTextureSetter() {
            return WatchFaceState::setAccentFillMaterialTexture;
        }

        @NonNull
        @Override
        Function<WatchFaceState, BytePackable.MaterialTexture> getMaterialTextureGetter() {
            return WatchFaceState::getAccentFillMaterialTexture;
        }
    }

    public static final class AccentHighlight extends MaterialConfigData {
        @NonNull
        @Override
        Material getMaterial() {
            return Material.ACCENT_HIGHLIGHT;
        }

        @Override
        int getTitleResourceId() {
            return R.string.config_preset_accent_highlight_material;
        }

        @Override
        int getPrimaryNameResourceId() {
            return R.string.config_accent_color_label;
        }

        @NonNull
        @Override
        PaintBox.ColorType getPrimaryColorType() {
            return PaintBox.ColorType.ACCENT;
        }

        @Override
        int getSecondaryNameResourceId() {
            return R.string.config_marker_color_label;
        }

        @NonNull
        @Override
        PaintBox.ColorType getSecondaryColorType() {
            return PaintBox.ColorType.HIGHLIGHT;
        }

        @Override
        int getMaterialGradientNameResourceId() {
            return R.string.config_preset_accent_highlight_material_gradient;
        }

        @NonNull
        @Override
        BiConsumer<WatchFaceState, BytePackable.MaterialGradient> getMaterialGradientSetter() {
            return WatchFaceState::setAccentHighlightMaterialGradient;
        }

        @NonNull
        @Override
        Function<WatchFaceState, BytePackable.MaterialGradient> getMaterialGradientGetter() {
            return WatchFaceState::getAccentHighlightMaterialGradient;
        }

        @Override
        int getMaterialTextureNameResourceId() {
            return R.string.config_preset_accent_highlight_material_texture;
        }

        @NonNull
        @Override
        BiConsumer<WatchFaceState, BytePackable.MaterialTexture> getMaterialTextureSetter() {
            return WatchFaceState::setAccentHighlightMaterialTexture;
        }

        @NonNull
        @Override
        Function<WatchFaceState, BytePackable.MaterialTexture> getMaterialTextureGetter() {
            return WatchFaceState::getAccentHighlightMaterialTexture;
        }
    }

    public static final class BaseAccent extends MaterialConfigData {
        @NonNull
        @Override
        Material getMaterial() {
            return Material.BASE_ACCENT;
        }

        @Override
        int getTitleResourceId() {
            return R.string.config_preset_base_accent_material;
        }

        @Override
        int getPrimaryNameResourceId() {
            return R.string.config_base_color_label;
        }

        @NonNull
        @Override
        PaintBox.ColorType getPrimaryColorType() {
            return PaintBox.ColorType.BASE;
        }

        @Override
        int getSecondaryNameResourceId() {
            return R.string.config_accent_color_label;
        }

        @NonNull
        @Override
        PaintBox.ColorType getSecondaryColorType() {
            return PaintBox.ColorType.ACCENT;
        }

        @Override
        int getMaterialGradientNameResourceId() {
            return R.string.config_preset_base_accent_material_gradient;
        }

        @NonNull
        @Override
        BiConsumer<WatchFaceState, BytePackable.MaterialGradient> getMaterialGradientSetter() {
            return WatchFaceState::setBaseAccentMaterialGradient;
        }

        @NonNull
        @Override
        Function<WatchFaceState, BytePackable.MaterialGradient> getMaterialGradientGetter() {
            return WatchFaceState::getBaseAccentMaterialGradient;
        }

        @Override
        int getMaterialTextureNameResourceId() {
            return R.string.config_preset_base_accent_material_texture;
        }

        @NonNull
        @Override
        BiConsumer<WatchFaceState, BytePackable.MaterialTexture> getMaterialTextureSetter() {
            return WatchFaceState::setBaseAccentMaterialTexture;
        }

        @NonNull
        @Override
        Function<WatchFaceState, BytePackable.MaterialTexture> getMaterialTextureGetter() {
            return WatchFaceState::getBaseAccentMaterialTexture;
        }
    }
}
