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

import static java.util.stream.Collectors.toList;

import androidx.annotation.NonNull;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import pro.watchkit.wearable.watchface.R;
import pro.watchkit.wearable.watchface.config.ColorSelectionActivity;
import pro.watchkit.wearable.watchface.config.ConfigActivity;
import pro.watchkit.wearable.watchface.config.WatchFaceSelectionActivity;
import pro.watchkit.wearable.watchface.watchface.WatchFaceGlobalDrawable;

public class ColorsMaterialsConfigData extends ConfigData {
    @NonNull
    @Override
    public List<ConfigItemType> getDataToPopulateAdapter() {
        int watchFaceGlobalDrawableFlags = WatchFaceGlobalDrawable.PART_BACKGROUND |
                WatchFaceGlobalDrawable.PART_PIPS |
                WatchFaceGlobalDrawable.PART_RINGS_ALL |
                WatchFaceGlobalDrawable.PART_HANDS;

        return Arrays.asList(
                // Heading.
                new HeadingLabelConfigItem(R.string.config_configure_colors_materials),

                // A preview of the current watch face.
                new WatchFaceDrawableConfigItem(watchFaceGlobalDrawableFlags),

//                // The name of the colorway.
//                new LabelConfigItem(R.string.config_configure_colors_materials_colorway,
//                        WatchFaceState::getColorwayName),

                // Choose a new colorway?
                new PickerConfigItem(
                        R.string.config_configure_colors_materials_colorway,
                        R.drawable.ic_settings,
                        WatchFaceGlobalDrawable.PART_BACKGROUND |
                                WatchFaceGlobalDrawable.PART_PIPS |
                                WatchFaceGlobalDrawable.PART_HANDS |
                                WatchFaceGlobalDrawable.PART_RINGS_ALL,
                        WatchFaceSelectionActivity.class,
                        w -> {
                            String originalString = w.getString();
                            String originalName = w.getColorwayName();
                            // Have we found the current colorway in our list of original colorways?
                            AtomicBoolean found = new AtomicBoolean(false);
                            // Return an array with each permutation of colorway.
                            // Iterate over "originalColorways". Each key is a colorway.
                            // So for each key, mutate our WatchFaceState and get its string.
                            List<Permutation> s = w.getPaintBox().getOriginalColorways()
                                    .entrySet().stream().map(entry -> {
                                        // Set the colorway with this entry.
                                        w.setColorway(entry.getKey());
                                        // Mark "found" if this colorway is our current colorway.
                                        if (w.mostlyEquals(originalString)) {
                                            found.set(true);
                                        }
                                        return new Permutation(w.getString(), entry.getValue());
                                    }).collect(toList());

                            if (!found.get()) {
                                // Our current colorway isn't in the list. Put it at the top.
                                s.add(0, new Permutation(originalString, originalName));
                            }

                            return s.toArray(new Permutation[0]);
                        }),

                // Choose a new colorway variant?
                new PickerConfigItem(
                        R.string.config_configure_colors_materials_variant,
                        R.drawable.ic_settings,
                        WatchFaceGlobalDrawable.PART_BACKGROUND |
                                WatchFaceGlobalDrawable.PART_PIPS |
                                WatchFaceGlobalDrawable.PART_HANDS |
                                WatchFaceGlobalDrawable.PART_RINGS_ALL,
                        WatchFaceSelectionActivity.class,
                        w -> Arrays.stream(
                                // Return an array with each permutation of colorway.
                                // Iterate over "originalColorway". Each key is a colorway.
                                // So for each key, mutate our WatchFaceState and get its string.
                                w.getPaintBox().getColorwayVariants()).mapToObj(
                                colorway -> {
                                    // Set the colorway with this entry.
                                    w.setColorway(colorway);

                                    return new Permutation(
                                            w.getString(), w.getColorwayName());
                                }).toArray(Permutation[]::new)),

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

                // Mutate our colorways!.
                new PickerConfigItem(
                        R.string.config_configure_colors_materials_mutation,
                        R.drawable.ic_filter_tilt_shift,
                        WatchFaceGlobalDrawable.PART_BACKGROUND |
                                WatchFaceGlobalDrawable.PART_PIPS |
                                WatchFaceGlobalDrawable.PART_HANDS |
                                WatchFaceGlobalDrawable.PART_RINGS_ALL,
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
                                p[0] = new Permutation(clone.getString(), clone.getColorwayName());

                                int colorTypeShift = r.nextInt(4);

                                // Roll the dice and generate a bunch of random watch faces!
                                for (int i = 1; i < SIZE; i++) {
                                    String name = "Mutated Colorway " + i +
                                            " (" + clone.getColorwayName() + ")";

                                    // Take turns shifting the various color types.
                                    switch ((i + colorTypeShift) % 4) {
                                        case 0:
                                            permuteRandomColorType(clone,
                                                    PaintBox.ColorType.FILL);
                                            break;
                                        case 1:
                                            permuteRandomColorType(clone,
                                                    PaintBox.ColorType.ACCENT);
                                            break;
                                        case 2:
                                            permuteRandomColorType(clone,
                                                    PaintBox.ColorType.HIGHLIGHT);
                                            break;
                                        case 3:
                                        default:
                                            permuteRandomColorType(clone,
                                                    PaintBox.ColorType.BASE);
                                            break;
                                    }

                                    p[i] = new Permutation(clone.getString(), name);
                                }
                                return p;
                            }
                        }, WatchFaceState::isDeveloperMode),

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
                        ConfigActivity.class),

                // Help.
                new HelpLabelConfigItem(R.string.config_configure_colors_materials_help)
        );
    }
}
