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
                                Set<Integer> colorways =
                                        clone.getPaintBox().getOriginalColorways().keySet();

                                // Slot 0 is the current selection.
                                p[0] = new Permutation(clone.getString(), "Current Watch Face");

                                // Roll the dice and generate a bunch of random watch faces!
                                for (int i = 1; i < SIZE; i++) {
                                    String name = "Random Watch Face " + i;

                                    // Get a random colorway and variant.
                                    permuteRandomColorwayAndVariant(clone, colorways);
                                    // Then permute that variant further!
                                    permuteRandomColorType(clone, PaintBox.ColorType.FILL);
                                    permuteRandomColorType(clone, PaintBox.ColorType.ACCENT);
                                    permuteRandomColorType(clone, PaintBox.ColorType.HIGHLIGHT);
                                    permuteRandomColorType(clone, PaintBox.ColorType.BASE);

                                    permuteRandomMaterials(clone);
                                    permuteRandomHands(clone);
                                    permuteRandomPips(clone);

                                    p[i] = new Permutation(clone.getString(), name);
                                }
                                return p;
                            }
                        }, WatchFaceState::isDeveloperMode),

                // Help.
                new HelpLabelConfigItem(R.string.config_configure_watch_face_preset_help)
        );
    }
}
