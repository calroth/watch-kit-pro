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
 *
 */

package pro.watchkit.wearable.watchface.util;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import pro.watchkit.wearable.watchface.R;
import pro.watchkit.wearable.watchface.watchface.ProWatchFaceServiceB;
import pro.watchkit.wearable.watchface.watchface.ProWatchFaceServiceC;
import pro.watchkit.wearable.watchface.watchface.ProWatchFaceServiceD;

/**
 * A wrapper for SharedPreferences that encapsulates all our slot-related logic
 * and presents neat interfaces for getting and putting any preference we might want.
 */
public final class SharedPref {
    /**
     * Internal copy of our Context for future reference.
     */
    @NonNull
    private Context mContext;

    /**
     * The SharedPreferences we're wrapping.
     */
    @NonNull
    private SharedPreferences mSharedPreferences;

    /**
     * The default WatchFaceState string for this slot.
     */
    private String mDefaultWatchFaceStateString;

    /**
     * Create a new SharedPref for the given watch face slot.
     *
     * @param context               Context for our resource lookups
     * @param watchFaceServiceClass The watch face slot to access
     */
    public SharedPref(@NonNull Context context, @NonNull Class watchFaceServiceClass) {
        mContext = context;

        @StringRes int prefFileKeyStringResId, prefDefaultStringResId;
        if (watchFaceServiceClass.equals(ProWatchFaceServiceB.class)) {
            prefFileKeyStringResId = R.string.watch_kit_pro_b_preference_file_key;
            prefDefaultStringResId = R.string.watch_kit_pro_b_default_string;
        } else if (watchFaceServiceClass.equals(ProWatchFaceServiceC.class)) {
            prefFileKeyStringResId = R.string.watch_kit_pro_c_preference_file_key;
            prefDefaultStringResId = R.string.watch_kit_pro_c_default_string;
        } else if (watchFaceServiceClass.equals(ProWatchFaceServiceD.class)) {
            prefFileKeyStringResId = R.string.watch_kit_pro_d_preference_file_key;
            prefDefaultStringResId = R.string.watch_kit_pro_d_default_string;
        } else {
            prefFileKeyStringResId = R.string.watch_kit_pro_a_preference_file_key;
            prefDefaultStringResId = R.string.watch_kit_pro_a_default_string;
        }

        mSharedPreferences = context.getSharedPreferences(
                context.getString(prefFileKeyStringResId), Context.MODE_PRIVATE);

        mDefaultWatchFaceStateString = mContext.getString(prefDefaultStringResId);
    }

    /**
     * Get the current WatchFaceState string from preferences
     *
     * @return the current WatchFaceState string
     */
    public String getWatchFaceStateString() {
        return mSharedPreferences.getString(
                mContext.getString(R.string.saved_watch_face_state), mDefaultWatchFaceStateString);
    }

    /**
     * Store the given WatchFaceState string into preferences
     *
     * @param value the WatchFaceState string to store
     */
    public void putWatchFaceStateString(@NonNull String value) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(mContext.getString(R.string.saved_watch_face_state), value);
        editor.apply();
    }

    /**
     * Get the most recent config page string from preferences
     *
     * @return the most recent config page string
     */
    public String getMostRecentConfigPageString() {
        return mSharedPreferences.getString(
                mContext.getString(R.string.saved_most_recent_config_page), null);
    }

    /**
     * Store the given recent config page string into preferences
     *
     * @param value the recent config page string to store
     */
    public void putMostRecentConfigPageString(@NonNull String value) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(mContext.getString(R.string.saved_most_recent_config_page), value);
        editor.apply();
    }
}
