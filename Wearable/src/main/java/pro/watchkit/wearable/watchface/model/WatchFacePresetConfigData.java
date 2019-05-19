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
import pro.watchkit.wearable.watchface.config.ConfigActivity;

public class WatchFacePresetConfigData extends ConfigData {
    @Override
    public List<ConfigItemType> getDataToPopulateAdapter(Context context) {
        return Arrays.asList(
                // Data for Configure Colors and Styles sub-activity in settings Activity.
                new ConfigActivityConfigItem(
                        context.getString(R.string.config_configure_colors_styles),
                        R.drawable.icn_styles,
                        ColorsStylesConfigData.class,
                        ConfigActivity.class),

                // Data for Configure Hands sub-activity in settings Activity.
                new ConfigActivityConfigItem(
                        context.getString(R.string.config_configure_hands),
                        R.drawable.ic_hands,
                        WatchPartHandsConfigData.class,
                        ConfigActivity.class),

                // Data for Configure Ticks sub-activity in settings Activity.
                new ConfigActivityConfigItem(
                        context.getString(R.string.config_configure_ticks),
                        R.drawable.ic_ticks,
                        WatchPartTicksConfigData.class,
                        ConfigActivity.class)
        );
    }
}
