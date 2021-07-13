/*
 * Copyright (C) 2018-2021 Terence Tan
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
                // Title.
                new LabelConfigItem(R.string.config_configure_watch_face_preset),

                // A preview of the current watch face.
                new WatchFaceDrawableConfigItem(WatchFaceGlobalDrawable.PART_BACKGROUND |
                        WatchFaceGlobalDrawable.PART_PIPS |
                        WatchFaceGlobalDrawable.PART_HANDS),

                // Data for Configure Colors and Materials sub-activity in settings Activity.
                new ConfigActivityConfigItem(
                        R.string.config_configure_colors_materials,
                        R.drawable.icn_styles,
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
                             * @param permutation WatchFaceState, which must be a clone, but in
                             *                    this case we'll ignore it...
                             * @return Default WatchFaceState strings for all slots
                             */
                            @NonNull
                            @Override
                            public String[] permute(@NonNull WatchFaceState permutation) {
                                if (mSharedPref != null) {
                                    return mSharedPref.getWatchFaceStateHistory();
                                } else {
                                    return new String[0];
                                }
                            }

                            @Nullable
                            @Override
                            public Enum<?> getCurrentValue(WatchFaceState currentPreset) {
                                return null;
                            }

                            @Nullable
                            private SharedPref mSharedPref;

                            @Override
                            public void setSharedPref(@NonNull SharedPref sharedPref) {
                                mSharedPref = sharedPref;
                            }
                        }),

                // Help.
                new LabelConfigItem(R.string.config_configure_help,
                        R.string.config_configure_watch_face_preset_help)
        );
    }
}
