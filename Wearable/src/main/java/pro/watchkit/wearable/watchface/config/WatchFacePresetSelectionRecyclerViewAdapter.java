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

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.wearable.view.CircledImageView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;
import pro.watchkit.wearable.watchface.R;
import pro.watchkit.wearable.watchface.model.PaintBox;
import pro.watchkit.wearable.watchface.model.WatchFacePreset;
import pro.watchkit.wearable.watchface.watchface.WatchFaceGlobalDrawable;

/**
 * Provides a binding from WatchFacePreset selection data set to views that are displayed within
 * {@link WatchFacePresetSelectionActivity}.
 * WatchFacePreset options change appearance for the item specified on the watch face. Value is saved to a
 * {@link SharedPreferences} value passed to the class.
 */

public class WatchFacePresetSelectionRecyclerViewAdapter extends
        RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = WatchFacePresetSelectionRecyclerViewAdapter.class.getSimpleName();

    private WatchFacePreset[] mWatchFacePresets;
    private PaintBox[] mPaintBoxes;

    public WatchFacePresetSelectionRecyclerViewAdapter(WatchFacePreset[] watchFacePresets,
                                                       PaintBox[] paintBoxes) {
        mWatchFacePresets = watchFacePresets;
        mPaintBoxes = paintBoxes;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder(): viewType: " + viewType);

        RecyclerView.ViewHolder viewHolder =
                new WatchFacePresetViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.color_config_list_item, parent, false));
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        Log.d(TAG, "Element " + position + " set.");

        WatchFacePresetViewHolder watchFacePresetViewHolder = (WatchFacePresetViewHolder) viewHolder;
        watchFacePresetViewHolder.setPreset(mWatchFacePresets[position], mPaintBoxes[position]);
    }

    @Override
    public int getItemCount() {
        return mWatchFacePresets.length;
    }

    /**
     * Displays color options for an item on the watch face and saves value to the
     * SharedPreference associated with it.
     */
    public class WatchFacePresetViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        private CircledImageView mColorCircleImageView;

        public WatchFacePresetViewHolder(final View view) {
            super(view);
            mColorCircleImageView = view.findViewById(R.id.color);
            view.setOnClickListener(this);
        }

        public void setPreset(WatchFacePreset watchFacePreset, PaintBox paintBox) {
            WatchFaceGlobalDrawable drawable = new WatchFaceGlobalDrawable();
            drawable.getWatchFaceState().preset = watchFacePreset;
            drawable.getWatchFaceState().paintBox = paintBox;
            drawable.getWatchFaceState().unreadNotifications = 0;
            drawable.getWatchFaceState().totalNotifications = 0;
            drawable.getWatchFaceState().ambient = false;
            mColorCircleImageView.setImageDrawable(drawable);
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            WatchFacePreset watchFacePreset = mWatchFacePresets[position];

            Log.d(TAG, "WatchFacePreset: " + watchFacePreset + " onClick() position: " + position);

            Activity activity = (Activity) view.getContext();

            SharedPreferences preferences = activity.getSharedPreferences(
                    activity.getString(R.string.analog_complication_preference_file_key),
                    Context.MODE_PRIVATE);

            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(activity.getString(R.string.saved_watch_face_preset), watchFacePreset.getString());
            editor.commit();

            // Lets Complication Config Activity know there was an update to colors.
            activity.setResult(Activity.RESULT_OK);

            // Show a toast popup with the color we just selected.
//            toastText = toastText.replace('\n', ' ') +
//                    ":\n" + paintBox.getColorName(sixBitColor);
//            Toast.makeText(this, toastText, Toast.LENGTH_LONG).show();

            activity.finish();
        }
    }
}