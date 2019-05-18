/*
 * Copyright (C) 2019 Terence Tan
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

package pro.watchkit.wearable.watchface.config;

import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import pro.watchkit.wearable.watchface.R;

/**
 * Provides a binding from WatchFacePreset selection data set to views that are displayed within
 * {@link WatchFaceSelectionActivity}.
 * WatchFacePreset options change appearance for the item specified on the watch face. Value is saved to a
 * {@link SharedPreferences} value passed to the class.
 */

public class WatchFaceSelectionRecyclerViewAdapter extends BaseRecyclerViewAdapter {
    private String[] mWatchFacePresetStrings;
    private String[] mSettingsStrings;

    WatchFaceSelectionRecyclerViewAdapter(
            String[] watchFacePresetStrings, String[] settingsStrings) {
        mWatchFacePresetStrings = watchFacePresetStrings;
        mSettingsStrings = settingsStrings;
    }

    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new WatchFacePresetSelectionViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.watch_face_preset_config_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        String watchFacePresetString =
                mWatchFacePresetStrings != null && mWatchFacePresetStrings.length > position ?
                        mWatchFacePresetStrings[position] : null;
        String settingsString =
                mSettingsStrings != null && mSettingsStrings.length > position ?
                        mSettingsStrings[position] : null;

        WatchFacePresetSelectionViewHolder holder = (WatchFacePresetSelectionViewHolder) viewHolder;
        holder.setPreset(watchFacePresetString, settingsString);
    }

    @Override
    public int getItemCount() {
        return mWatchFacePresetStrings.length;
    }
}
