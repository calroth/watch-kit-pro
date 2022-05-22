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
import androidx.annotation.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import pro.watchkit.wearable.watchface.R;
import pro.watchkit.wearable.watchface.config.ConfigActivity;
import pro.watchkit.wearable.watchface.config.WatchFaceSelectionActivity;
import pro.watchkit.wearable.watchface.util.SharedPref;
import pro.watchkit.wearable.watchface.watchface.WatchFaceGlobalDrawable;

public class WatchFacePresetConfigData extends ConfigData {
    @NonNull
    @Override
    public List<ConfigItemType> getDataToPopulateAdapter() {
        return Arrays.asList(
                // Heading.
                new HeadingLabelConfigItem(R.string.config_configure_watch_face_preset),

                // A preview of the current watch face.
                new WatchFaceDrawableConfigItem(WatchFaceGlobalDrawable.PART_BACKGROUND |
                        WatchFaceGlobalDrawable.PART_PIPS |
                        WatchFaceGlobalDrawable.PART_HANDS),

                // Data for Configure Colors and Materials sub-activity in settings Activity.
                new ConfigActivityConfigItem(
                        R.string.config_configure_colors_materials,
                        R.drawable.ic_color_lens,
                        ColorsMaterialsConfigData.class,
                        ConfigActivity.class),

                // Data for Configure Hands sub-activity in settings Activity.
                new ConfigActivityConfigItem(
                        R.string.config_configure_hands,
                        R.drawable.ic_hands,
                        WatchPartHandsConfigData.class,
                        ConfigActivity.class),

                // Data for Configure Pips sub-activity in settings Activity.
                new ConfigActivityConfigItem(
                        R.string.config_configure_pips,
                        R.drawable.ic_pips,
                        WatchPartPipsConfigData.class,
                        ConfigActivity.class),

                // Data for View History sub-activity in settings Activity.
                new PickerConfigItem(
                        R.string.config_view_history,
                        R.drawable.ic_history,
                        WatchFaceGlobalDrawable.PART_BACKGROUND |
                                WatchFaceGlobalDrawable.PART_PIPS |
                                WatchFaceGlobalDrawable.PART_HANDS |
                                WatchFaceGlobalDrawable.PART_RINGS_ALL,
                        WatchFaceSelectionActivity.class,
                        new MutatorWithPrefsAccess() {
                            /**
                             * A custom Mutator which allows you to "view history" by offering
                             * mutations corresponding every WatchFaceState in prefs history.
                             *
                             * @param clone WatchFaceState, which must be a clone, but in
                             *              this case we'll ignore it...
                             * @return Default WatchFaceState permutations for all slots
                             */
                            @NonNull
                            @Override
                            public Permutation[] getPermutations(@NonNull WatchFaceState clone) {
                                if (mSharedPref != null) {
                                    AtomicInteger i = new AtomicInteger();
                                    return Arrays.stream(mSharedPref.getWatchFaceStateHistory())
                                            .map(s -> new Permutation(s, "History " + i.getAndIncrement()))
                                            .toArray(Permutation[]::new);
                                } else {
                                    return new Permutation[0];
                                }
                            }

                            @Nullable
                            private SharedPref mSharedPref;

                            @Override
                            public void setSharedPref(@NonNull SharedPref sharedPref) {
                                mSharedPref = sharedPref;
                            }
                        }),

                // Data for Random sub-activity in settings Activity.
                new PickerConfigItem(
                        R.string.config_view_random_watch_face_presets,
                        R.drawable.ic_filter_tilt_shift,
                        WatchFaceGlobalDrawable.PART_BACKGROUND |
                                WatchFaceGlobalDrawable.PART_PIPS |
                                WatchFaceGlobalDrawable.PART_HANDS |
                                WatchFaceGlobalDrawable.PART_RINGS_ALL,
                        WatchFaceSelectionActivity.class,
                        new Mutator() {
                            final Random r = new Random();

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
                                Set<Integer> colorways =
                                        clone.getPaintBox().getOriginalColorways().keySet();

                                // Slot 0 is the current selection.
                                p[0] = new Permutation(clone.getString(), "Current Watch Face");

                                // Roll the dice and generate a bunch of random watch faces!
                                for (int i = 1; i < SIZE; i++) {
                                    String name = "Random Watch Face " + i;

                                    // Permute colorway. Get a random element of "colorways",
                                    // or (if it fails for whatever reason) a random number.
                                    clone.setColorway(colorways.stream()
                                            .skip(r.nextInt(colorways.size()))
                                            .findFirst().orElse(r.nextInt()));
                                    // Permute to a variant...
                                    int[] variants = clone.getPaintBox().getColorwayVariants();
                                    clone.setColorway(variants[r.nextInt(variants.length)]);
                                    // Then permute that variant further!
                                    int fillSixBitColor =
                                            clone.getSixBitColor(PaintBox.ColorType.FILL);
                                    int accentSixBitColor =
                                            clone.getSixBitColor(PaintBox.ColorType.ACCENT);
                                    int highlightSixBitColor =
                                            clone.getSixBitColor(PaintBox.ColorType.HIGHLIGHT);
                                    int baseSixBitColor =
                                            clone.getSixBitColor(PaintBox.ColorType.BASE);
                                    // Roll a dice. On 4 or less, permute colors and roll again...
                                    while (r.nextInt(6) < 4) {
                                        fillSixBitColor = clone.getPaintBox().
                                                getNearbySixBitColor(fillSixBitColor, r);
                                        accentSixBitColor = clone.getPaintBox().
                                                getNearbySixBitColor(accentSixBitColor, r);
                                        highlightSixBitColor = clone.getPaintBox().
                                                getNearbySixBitColor(highlightSixBitColor, r);
                                        baseSixBitColor = clone.getPaintBox().
                                                getNearbySixBitColor(baseSixBitColor, r);
                                    }
                                    clone.setSixBitColor(
                                            PaintBox.ColorType.FILL, fillSixBitColor);
                                    clone.setSixBitColor(
                                            PaintBox.ColorType.ACCENT, accentSixBitColor);
                                    clone.setSixBitColor(
                                            PaintBox.ColorType.HIGHLIGHT, highlightSixBitColor);
                                    clone.setSixBitColor(
                                            PaintBox.ColorType.BASE, baseSixBitColor);

                                    // Permute materials.
                                    clone.setFillHighlightMaterialGradient(
                                            random(BytePackable.MaterialGradient.randomValues));
                                    clone.setFillHighlightMaterialTexture(
                                            random(BytePackable.MaterialTexture.randomValues));
                                    clone.setAccentFillMaterialGradient(
                                            random(BytePackable.MaterialGradient.randomValues));
                                    clone.setAccentFillMaterialTexture(
                                            random(BytePackable.MaterialTexture.randomValues));
                                    clone.setAccentHighlightMaterialGradient(
                                            random(BytePackable.MaterialGradient.randomValues));
                                    clone.setAccentHighlightMaterialTexture(
                                            random(BytePackable.MaterialTexture.randomValues));
                                    clone.setBaseAccentMaterialGradient(
                                            random(BytePackable.MaterialGradient.randomValues));
                                    clone.setBaseAccentMaterialTexture(
                                            random(BytePackable.MaterialTexture.randomValues));

                                    // Permute hands.
                                    // Hour hand.
                                    clone.setHourHandShape(
                                            random(BytePackable.HandShape.randomValues));
                                    clone.setHourHandLength(
                                            random(BytePackable.HandLength.randomValues));
                                    clone.setHourHandThickness(
                                            random(BytePackable.HandThickness.randomValues));
                                    clone.setHourHandStalk(
                                            random(BytePackable.HandStalk.randomValues));
                                    clone.setHourHandMaterial(
                                            random(BytePackable.Material.randomValues));
                                    clone.setHourHandCutout(randomBoolean());
                                    clone.setHourHandCutoutShape(
                                            random(BytePackable.HandCutoutShape.randomValues));
                                    clone.setHourHandCutoutMaterial(
                                            random(BytePackable.HandCutoutMaterial.randomValues));
                                    // Minute hand.
                                    clone.setMinuteHandOverride(false); // Normie!
                                    // Second hand.
                                    clone.setSecondHandOverride(false); // Normie!

                                    // Permute pips.
                                    // General pip settings.
                                    clone.setPipsDisplay(
                                            random(BytePackable.PipsDisplay.randomValues));
                                    clone.setPipMargin(
                                            random(BytePackable.PipMargin.randomValues));
                                    if (r.nextInt(100) < 25) {
                                        // 25% chance of having a pip "ring".
                                        clone.setPipBackgroundMaterial(
                                                random(BytePackable.Material.randomValues));
                                    } else {
                                        // 75% chance of having no pip "ring".
                                        clone.setPipBackgroundMaterial(
                                                BytePackable.Material.BASE_ACCENT); // Background
                                    }
                                    // Digits.
                                    clone.setDigitDisplay(
                                            random(BytePackable.DigitDisplay.randomValues));
                                    clone.setDigitSize(
                                            random(BytePackable.DigitSize.randomValues));
                                    clone.setDigitRotation(
                                            random(BytePackable.DigitRotation.randomValues));
                                    clone.setDigitFormat(
                                            random(BytePackable.DigitFormat.randomValues));
                                    clone.setDigitMaterial(
                                            random(BytePackable.Material.randomValues));
                                    // Quarter pips.
                                    clone.setQuarterPipShape(
                                            random(BytePackable.PipShape.randomValues));
                                    clone.setQuarterPipSize(
                                            random(BytePackable.PipSize.randomValues));
                                    clone.setQuarterPipMaterial(
                                            random(BytePackable.Material.randomValues));
                                    // Hour pips.
                                    clone.setHourPipOverride(true);
                                    clone.setHourPipShape(
                                            clone.getQuarterPipShape());
                                    clone.setHourPipSize(
                                            nextSizeDown(clone.getQuarterPipSize()));
                                    clone.setHourPipMaterial(
                                            random(BytePackable.Material.randomValues));
                                    // Minute pips.
                                    clone.setMinutePipOverride(true);
                                    clone.setMinutePipShape(
                                            clone.getHourPipShape());
                                    clone.setMinutePipSize(
                                            nextSizeDown(clone.getHourPipSize()));
                                    clone.setMinutePipMaterial(
                                            random(BytePackable.Material.randomValues));

                                    p[i] = new Permutation(clone.getString(), name);
                                }
                                return p;
                            }

                            private BytePackable.PipSize nextSizeDown(BytePackable.PipSize p) {
                                switch (p) {
                                    case XXX_LONG:
                                        return BytePackable.PipSize.XX_LONG;
                                    case XX_LONG:
                                        return BytePackable.PipSize.X_LONG;
                                    case X_LONG:
                                        return BytePackable.PipSize.LONG;
                                    case LONG:
                                        return BytePackable.PipSize.MEDIUM;
                                    case MEDIUM:
                                        return BytePackable.PipSize.SHORT;
                                    case SHORT:
                                        return BytePackable.PipSize.X_SHORT;
                                    case X_SHORT:
                                    case XX_SHORT:
                                    default:
                                        return BytePackable.PipSize.XX_SHORT;
                                }
                            }

                            /**
                             * Flip a coin! True or false?
                             *
                             * @return True or false at random.
                             */
                            private boolean randomBoolean() {
                                return r.nextBoolean();
                            }

                            /**
                             * Pick a random element from "finalValues" and return it.
                             * Generic edition!
                             *
                             * @param finalValues Array of all values
                             * @param <T>         Type of "finalValues" elements
                             * @return Random element from "FinalValues"
                             */
                            private <T> T random(T[] finalValues) {
                                return finalValues[r.nextInt(finalValues.length)];
                            }
                        }, WatchFaceState::isDeveloperMode),

                // Help.
                new HelpLabelConfigItem(R.string.config_configure_watch_face_preset_help)
        );
    }
}
