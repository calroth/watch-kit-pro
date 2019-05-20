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

public class ComplicationConfigData extends ConfigData {
    @Override
    public List<ConfigItemType> getDataToPopulateAdapter(Context context) {
        return Arrays.asList(
                // Complication picker from watch face drawable.
                new ComplicationConfigItem(R.drawable.add_complication)
        );
    }
}
