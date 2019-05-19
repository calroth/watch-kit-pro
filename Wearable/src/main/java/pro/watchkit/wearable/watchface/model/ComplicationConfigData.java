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
 *
 * This file incorporates work covered by the following copyright and
 * permission notice:
 *
 *     Copyright (C) 2017 The Android Open Source Project
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */

package pro.watchkit.wearable.watchface.model;

import android.content.Context;

import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import java.util.ArrayList;

import pro.watchkit.wearable.watchface.R;
import pro.watchkit.wearable.watchface.config.ConfigActivity;
import pro.watchkit.wearable.watchface.config.ConfigRecyclerViewAdapter;
import pro.watchkit.wearable.watchface.watchface.AnalogComplicationWatchFaceService;

/**
 * Data represents different views for configuring the
 * {@link AnalogComplicationWatchFaceService} watch face's appearance and complications
 * via {@link ConfigActivity}.
 */
public class ComplicationConfigData extends ConfigData {
    /**
     * Includes all data to populate each of the 5 different custom
     * {@link ViewHolder} types in {@link ConfigRecyclerViewAdapter}.
     */
    @Override
    public ArrayList<ConfigItemType> getDataToPopulateAdapter(Context context) {

        ArrayList<ConfigItemType> settingsConfigData = new ArrayList<>();

        // Complication picker from watch face drawable.
        settingsConfigData.add(new WatchFaceDrawableConfigItem(R.drawable.add_complication));

        // Data for 'Unread Notifications' UX (toggle) in settings Activity.
        ConfigItemType unreadNotificationsConfigItem =
                new UnreadNotificationConfigItem(
                        context.getString(R.string.config_unread_notifications_label),
                        R.drawable.ic_notifications_white_24dp,
                        R.drawable.ic_notifications_off_white_24dp/*,
                        R.string.saved_unread_notifications_pref*/);
        settingsConfigData.add(unreadNotificationsConfigItem);

        // Data for 'Night Vision' UX (toggle) in settings Activity.
        ConfigItemType nightVisionConfigItem =
                new NightVisionConfigItem(
                        context.getString(R.string.config_night_vision_label),
                        R.drawable.ic_notifications_white_24dp,
                        R.drawable.ic_notifications_off_white_24dp/*,
                        R.string.saved_night_vision_pref*/);
        settingsConfigData.add(nightVisionConfigItem);

        return settingsConfigData;
    }
}
