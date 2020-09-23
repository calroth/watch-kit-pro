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

import androidx.annotation.NonNull;

import java.util.Arrays;
import java.util.List;

import pro.watchkit.wearable.watchface.R;
import pro.watchkit.wearable.watchface.model.BytePackable.Typeface;

public class TypefaceConfigData extends ConfigData {
    @NonNull
    @Override
    public List<ConfigItemType> getDataToPopulateAdapter() {
        return Arrays.asList(
                // Title.
                new LabelConfigItem(R.string.config_configure_typeface),

                new TypefaceConfigItem(Typeface.SANS_THIN),

                new TypefaceConfigItem(Typeface.SANS_LIGHT),

                new TypefaceConfigItem(Typeface.SANS_REGULAR),

                new TypefaceConfigItem(Typeface.SANS_MEDIUM),

                new TypefaceConfigItem(Typeface.SANS_BOLD),

                new TypefaceConfigItem(Typeface.SANS_BLACK),

                new TypefaceConfigItem(Typeface.CONDENSED_LIGHT),

                new TypefaceConfigItem(Typeface.CONDENSED_REGULAR),

                new TypefaceConfigItem(Typeface.CONDENSED_MEDIUM),

                new TypefaceConfigItem(Typeface.CONDENSED_BOLD),

                new TypefaceConfigItem(Typeface.SERIF_REGULAR),

                new TypefaceConfigItem(Typeface.SERIF_BOLD),

                new TypefaceConfigItem(Typeface.MONO_REGULAR),

                new TypefaceConfigItem(Typeface.PRODUCT_SANS_REGULAR),

                new TypefaceConfigItem(Typeface.PRODUCT_SANS_MEDIUM),

                new TypefaceConfigItem(Typeface.PRODUCT_SANS_BOLD),

                // Help.
                new LabelConfigItem(R.string.config_configure_help,
                        R.string.config_configure_typeface_help)
        );
    }
}
