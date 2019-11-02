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
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import pro.watchkit.wearable.watchface.R;
import pro.watchkit.wearable.watchface.watchface.ProWatchFaceService;

/**
 * A wrapper for SharedPreferences that encapsulates all our slot-related logic
 * and presents neat interfaces for getting and putting any preference we might want.
 */
public final class SharedPref {
    @Nullable
    public static Context mWriteLayersToDiskContext;
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
        if (watchFaceServiceClass.equals(ProWatchFaceService.B.class)) {
            prefFileKeyStringResId = R.string.watch_kit_pro_b_preference_file_key;
            prefDefaultStringResId = R.string.watch_kit_pro_b_default_string;
        } else if (watchFaceServiceClass.equals(ProWatchFaceService.C.class)) {
            prefFileKeyStringResId = R.string.watch_kit_pro_c_preference_file_key;
            prefDefaultStringResId = R.string.watch_kit_pro_c_default_string;
        } else if (watchFaceServiceClass.equals(ProWatchFaceService.D.class)) {
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
    @Nullable
    public String getWatchFaceStateString() {
        return mSharedPreferences.getString(
                mContext.getString(R.string.saved_watch_face_state), mDefaultWatchFaceStateString);
    }

    @NonNull
    private static SimpleDateFormat mIso8601 =
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US);

    @NonNull
    private static ArrayList<JSONObject> mWatchFaceStateHistory = new ArrayList<>();

    @NonNull
    private final static StringBuilder mHistoryStringBuilder = new StringBuilder();

    public String[] getWatchFaceStateHistory() {
        // Grab the current string into "oldValue".
        String currentValue = getWatchFaceStateString();
        if (currentValue == null) {
            return new String[0];
        }
        String[] split = currentValue.split("~");
        if (split.length == 0) {
            return new String[0];
        }

        ArrayList<String> result = new ArrayList<>();
        result.add(currentValue);

        // Get the history, so when we overwrite the old value, we store it in history first.
        try {
            // OK, get the history array from prefs.
            JSONArray jsonArray = new JSONArray(mSharedPreferences.getString(
                    mContext.getString(R.string.saved_watch_face_state_history), "[]"));

            // Re-inflate our history. Max 255 entries.
            for (int i = 0; i < jsonArray.length() && i < 256; i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                // If our string value is in history, remove it (or rather, don't add it)...
                if (!jsonObject.getString("value").startsWith(split[0])) {
                    mHistoryStringBuilder.setLength(0);
                    mHistoryStringBuilder.append(jsonObject.getString("value"));
                    // Combine the history WatchFacePreset with the other
                    // parts (Settings and extra) from the current WatchFaceState.
                    for (int j = 1; j < split.length; j++) {
                        mHistoryStringBuilder.append("~").append(split[j]);
                    }
                    result.add(mHistoryStringBuilder.toString());
                }
            }
        } catch (JSONException e) {
            // No action, leave "history" blank (or however far we made it).
        }

        return result.toArray(new String[0]);
    }

    /**
     * Store the given WatchFaceState string into preferences
     *
     * @param value the WatchFaceState string to store
     */
    public void putWatchFaceStateString(@NonNull String value) {
        boolean historyNeedsUpdating = false;
        // Get the history, so when we overwrite the old value, we store it in history first.
        try {
            // Store the old value (that we're overwriting) into history for posterity.
            String oldValue = getWatchFaceStateString();
            if (oldValue == null) {
                throw new JSONException("no old value?");
            }
            String[] split = oldValue.split("~");
            if (split.length == 0) {
                throw new JSONException("no usable old value?");
            }
            // Make "oldValue" just the WatchFacePreset component.
            oldValue = split[0];

            // OK, get the history array from prefs.
            JSONArray jsonArray = new JSONArray(mSharedPreferences.getString(
                    mContext.getString(R.string.saved_watch_face_state_history), "[]"));

            // Don't store the old value into history unless it's been more than 5 minutes.
            // If we've made multiple changes, wait until we've had a stable value for 5 minutes
            // before storing that in history.
            if (jsonArray.length() > 0) {
                // Get the time since the last change.
                JSONObject jsonObject = jsonArray.getJSONObject(0);
                try {
                    Date lastChange = mIso8601.parse(jsonObject.getString("date"));
                    long millis = System.currentTimeMillis() - lastChange.getTime();
                    long FIVE_MINUTES_IN_MILLISECONDS = 5L * 60L * 1000L;
                    historyNeedsUpdating = millis > FIVE_MINUTES_IN_MILLISECONDS;
                } catch (ParseException e) {
                    // Can't parse it? Don't worry about it.
                } catch (JSONException e) {
                    // Something weird with the JSON? Don't worry about this either.
                }
            } else {
                // Nothing in history. Let this be our first!
                historyNeedsUpdating = true;
            }

            if (historyNeedsUpdating) {
                mWatchFaceStateHistory.clear();
                // Re-inflate our history. Max 255 entries.
                for (int i = 0; i < jsonArray.length() && i < 256; i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    // If our string value is in history, remove it (or rather, don't add it)...
                    if (!jsonObject.getString("value").startsWith(oldValue)) {
                        mWatchFaceStateHistory.add(jsonObject);
                    }
                }
                // Then append our string value at the top with the current date.
                {
                    JSONObject jsonObject = new JSONObject();
                    mIso8601.setTimeZone(TimeZone.getTimeZone("UTC"));
                    jsonObject.put("date", mIso8601.format(new Date()));
                    jsonObject.put("value", oldValue);
                    mWatchFaceStateHistory.add(0, jsonObject);
                }
            }
        } catch (JSONException e) {
            // No action, leave "history" blank (or however far we made it).
        }

        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(mContext.getString(R.string.saved_watch_face_state), value);
        if (historyNeedsUpdating) {
            JSONArray jsonArray = new JSONArray(mWatchFaceStateHistory);
            editor.putString(mContext.getString(R.string.saved_watch_face_state_history),
                    jsonArray.toString());
        }
        editor.apply();
    }

    /**
     * Get the most recent config page string from preferences
     *
     * @return the most recent config page string
     */
    @Nullable
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

    /**
     * Are we using a device with a round screen? Static shared preference as it's unlikely
     * to change...
     */
    private static boolean mIsRoundScreen = true;

    public static boolean isRoundScreen() {
        return mIsRoundScreen;
    }

    public static void setIsRoundScreen(boolean isRoundScreen) {
        mIsRoundScreen = isRoundScreen;
    }
}
