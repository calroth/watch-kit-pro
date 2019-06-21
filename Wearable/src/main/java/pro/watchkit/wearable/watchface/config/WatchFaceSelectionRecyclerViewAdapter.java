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

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import pro.watchkit.wearable.watchface.R;
import pro.watchkit.wearable.watchface.watchface.ProWatchFaceService;

import static pro.watchkit.wearable.watchface.watchface.WatchFaceGlobalDrawable.PART_COMPLICATIONS;

/**
 * Provides a binding from WatchFacePreset selection data set to views that are displayed within
 * {@link WatchFaceSelectionActivity}.
 * WatchFacePreset options change appearance for the item specified on the watch face. Value is saved to a
 * {@link SharedPreferences} value passed to the class.
 */

public class WatchFaceSelectionRecyclerViewAdapter extends BaseRecyclerViewAdapter {
    private String[] mWatchFaceStateStrings;
    private int mFlags;

    WatchFaceSelectionRecyclerViewAdapter(
            @NonNull Context context, String[] watchFaceStateStrings, int flags) {
        // TODO: don't hard-code AnalogComplicationWatchFaceService.class
        super(context, ProWatchFaceService.class);
        mWatchFaceStateStrings = watchFaceStateStrings;
        mFlags = flags;
    }

    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new WatchFacePresetSelectionViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.watch_face_preset_config_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        String watchFaceStateString =
                mWatchFaceStateStrings != null && mWatchFaceStateStrings.length > position ?
                        mWatchFaceStateStrings[position] : null;

        WatchFacePresetSelectionViewHolder holder = (WatchFacePresetSelectionViewHolder) viewHolder;
        holder.setWatchFaceGlobalDrawableFlags(mFlags);
        holder.setPreset(watchFaceStateString);

        if ((mFlags & PART_COMPLICATIONS) > 0) {
            holder.retrieveProviderInfo();
        }
    }

    @Override
    public int getItemCount() {
        return mWatchFaceStateStrings != null ? mWatchFaceStateStrings.length : 0;
    }
}
