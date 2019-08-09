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
                new LabelConfigItem(R.string.config_configure_typeface),

                new TypefaceConfigItem(R.string.config_typeface_DROID_SANS, Typeface.DROID_SANS),

                new TypefaceConfigItem(R.string.config_typeface_ROBOTO, Typeface.ROBOTO),

                new TypefaceConfigItem(R.string.config_typeface_DROID_SANS_BOLD, Typeface.DROID_SANS_BOLD),

                new TypefaceConfigItem(R.string.config_typeface_ROBOTO_BOLD, Typeface.ROBOTO_BOLD),

                new TypefaceConfigItem(R.string.config_typeface_ROBOTO_BLACK, Typeface.ROBOTO_BLACK),

                new TypefaceConfigItem(R.string.config_typeface_NOTO_SERIF, Typeface.NOTO_SERIF),

                new TypefaceConfigItem(R.string.config_typeface_NOTO_SERIF_BOLD, Typeface.NOTO_SERIF_BOLD),

                new TypefaceConfigItem(R.string.config_typeface_DROID_SANS_MONO, Typeface.DROID_SANS_MONO),

                new TypefaceConfigItem(R.string.config_typeface_PRODUCT_SANS, Typeface.PRODUCT_SANS),

                new TypefaceConfigItem(R.string.config_typeface_PRODUCT_SANS_BOLD, Typeface.PRODUCT_SANS_BOLD)
        );
    }
}
