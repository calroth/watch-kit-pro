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

import java.util.Arrays;
import java.util.List;

import pro.watchkit.wearable.watchface.R;
import pro.watchkit.wearable.watchface.model.BytePackable.Typeface;

public class TypefaceConfigData extends ConfigData {
    @Override
    public List<ConfigItemType> getDataToPopulateAdapter() {
        return Arrays.asList(
                // Title.
                new LabelConfigItem(R.string.config_configure_ticks),

                new LabelConfigItem(R.string.config_typeface_0, R.string.config_typeface_sample,
                        c -> c.getTypefaceObject(Typeface.DROID_SANS) != null),

                new LabelConfigItem(R.string.config_typeface_1, R.string.config_typeface_sample,
                        c -> c.getTypefaceObject(Typeface.DROID_SANS_BOLD) != null),

                new LabelConfigItem(R.string.config_typeface_2, R.string.config_typeface_sample,
                        c -> c.getTypefaceObject(Typeface.NOTO_SERIF) != null),

                new LabelConfigItem(R.string.config_typeface_3, R.string.config_typeface_sample,
                        c -> c.getTypefaceObject(Typeface.NOTO_SERIF_BOLD) != null),

                new LabelConfigItem(R.string.config_typeface_4, R.string.config_typeface_sample,
                        c -> c.getTypefaceObject(Typeface.DROID_SANS_MONO) != null),

                new LabelConfigItem(R.string.config_typeface_5, R.string.config_typeface_sample,
                        c -> c.getTypefaceObject(Typeface.ROBOTO) != null),

                new LabelConfigItem(R.string.config_typeface_6, R.string.config_typeface_sample,
                        c -> c.getTypefaceObject(Typeface.ROBOTO_BOLD) != null),

                new LabelConfigItem(R.string.config_typeface_7, R.string.config_typeface_sample,
                        c -> c.getTypefaceObject(Typeface.PRODUCT_SANS) != null),

                new LabelConfigItem(R.string.config_typeface_8, R.string.config_typeface_sample,
                        c -> c.getTypefaceObject(Typeface.PRODUCT_SANS_BOLD) != null),

                new LabelConfigItem(R.string.config_typeface_9, R.string.config_typeface_sample,
                        c -> c.getTypefaceObject(Typeface.ROBOTO_BLACK) != null)
        );
    }
}
