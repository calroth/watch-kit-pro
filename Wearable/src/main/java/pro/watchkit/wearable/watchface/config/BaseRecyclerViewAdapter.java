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

package pro.watchkit.wearable.watchface.config;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.support.wearable.complications.ComplicationHelperActivity;
import android.support.wearable.complications.ComplicationProviderInfo;
import android.support.wearable.complications.ProviderInfoRetriever;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Comparator;
import java.util.Optional;

import pro.watchkit.wearable.watchface.R;
import pro.watchkit.wearable.watchface.model.ComplicationHolder;
import pro.watchkit.wearable.watchface.model.ConfigData;
import pro.watchkit.wearable.watchface.model.Settings;
import pro.watchkit.wearable.watchface.model.WatchFacePreset;
import pro.watchkit.wearable.watchface.model.WatchFaceState;
import pro.watchkit.wearable.watchface.watchface.AnalogComplicationWatchFaceService;
import pro.watchkit.wearable.watchface.watchface.WatchFaceGlobalDrawable;

abstract class BaseRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = BaseRecyclerViewAdapter.class.getSimpleName();

    /**
     * The current user-selected Settings with what's currently stored in preferences
     */
    Settings mCurrentSettings = new Settings();

    /**
     * The current user-selected WatchFacePreset with what's currently stored in preferences.
     */
    WatchFacePreset mCurrentWatchFacePreset = new WatchFacePreset();

    /**
     * When the user selects a complication to launch the choose-a-complication-provider Activity,
     * this object holds the complication that we'll assign the provider to.
     */
    ComplicationHolder mSelectedComplication;

    /**
     * The object that retrieves complication data for us to preview our complications with.
     */
    ProviderInfoRetriever mProviderInfoRetriever;

    /**
     * The ComponentName of our WatchFaceService. We use this to find what complications have been
     * set for this WatchFaceService. It's different for slot A, B, C etc. as we allow the user to
     * have different complication setups per slot!
     */
    ComponentName mWatchFaceComponentName;

    /**
     * A object implementing WatchFacePresetListener is interested in receiving a notification
     * every time we change the WatchFacePreset (due to setting new options, etc.)
     * On notification, an object invalidates and redraws itself.
     */
    interface WatchFacePresetListener {
        /**
         * Invalidate this object.
         */
        void onWatchFacePresetChanged();
    }

    /**
     * A object implementing SettingsListener is interested in receiving a notification
     * every time we change the Settings (due to setting new options, etc.)
     * On notification, an object invalidates and redraws itself.
     */
    interface SettingsListener {
        /**
         * Invalidate this object.
         */
        void onSettingsChanged();
    }

    /**
     * A object implementing ComplicationProviderInfoListener is interested in receiving a
     * ComplicationProviderInfo when a new or updated one is available.
     */
    interface ComplicationProviderInfoListener {
        /**
         * Invalidate this object.
         */
        void onComplicationProviderInfo(
                @NonNull ComplicationHolder complication,
                ComplicationProviderInfo complicationProviderInfo);
    }

    /**
     * Displays color options for an item on the watch face and saves value to the
     * SharedPreference associated with it.
     */
    public class WatchFacePresetViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView mImageView;

        private WatchFaceGlobalDrawable mWatchFaceGlobalDrawable;

        WatchFacePresetViewHolder(final View view) {
            super(view);
            mImageView = view.findViewById(R.id.watch_face_preset);
            view.setOnClickListener(this);
            mWatchFaceGlobalDrawable = new WatchFaceGlobalDrawable(view.getContext(),
                    WatchFaceGlobalDrawable.PART_BACKGROUND |
                            WatchFaceGlobalDrawable.PART_TICKS |
                            WatchFaceGlobalDrawable.PART_HANDS);
        }

        void setPreset(String watchFacePresetString, String settingsString) {
            WatchFaceState w = mWatchFaceGlobalDrawable.getWatchFaceState();
            if (watchFacePresetString != null) {
                w.getWatchFacePreset().setString(watchFacePresetString);
            }
            if (settingsString != null) {
                w.getSettings().setString(settingsString);
            }
            w.setNotifications(0, 0);
            w.setAmbient(false);
            mImageView.setImageDrawable(mWatchFaceGlobalDrawable);
        }

        @Override
        public void onClick(View view) {
            String watchFacePresetString =
                    mWatchFaceGlobalDrawable.getWatchFaceState().getWatchFacePreset().getString();

            Activity activity = (Activity) view.getContext();

            SharedPreferences preferences = activity.getSharedPreferences(
                    activity.getString(R.string.analog_complication_preference_file_key),
                    Context.MODE_PRIVATE);

            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(activity.getString(R.string.saved_watch_face_preset), watchFacePresetString);
            editor.apply();

            // Lets Complication Config Activity know there was an update to colors.
            activity.setResult(Activity.RESULT_OK);

            // Show a toast popup with the color we just selected.
//            toastText = toastText.replace('\n', ' ') +
//                    ":\n" + paintBox.getColorName(sixBitColor);
//            Toast.makeText(this, toastText, Toast.LENGTH_LONG).show();

            activity.finish();
        }
    }

    /**
     * TODO: we really have to see if we can generalise it, it's the same code as in
     * WatchFaceSelectionRecyclerViewAdapter
     */
    public class WatchFaceDrawableComplicationViewHolder extends RecyclerView.ViewHolder
            implements ComplicationProviderInfoListener, WatchFacePresetListener, SettingsListener {

        private ImageView mImageView;

        private WatchFaceGlobalDrawable mWatchFaceGlobalDrawable;

        private @DrawableRes
        int mDefaultComplicationDrawableId;

        private ConfigData.WatchFaceDrawableConfigItem mConfigItem;

        private Context mContext;

        private float mLastTouchX = -1f, mLastTouchY = -1f;
        private Activity mCurrentActivity;

        WatchFaceDrawableComplicationViewHolder(final View view) {
            super(view);
            mImageView = view.findViewById(R.id.watch_face_preset);
//            view.setOnClickListener(this);
            mWatchFaceGlobalDrawable = new WatchFaceGlobalDrawable(view.getContext(),
                    WatchFaceGlobalDrawable.PART_BACKGROUND |
                            WatchFaceGlobalDrawable.PART_RINGS_ALL |
                            WatchFaceGlobalDrawable.PART_COMPLICATIONS);
            mContext = view.getContext();
            mCurrentActivity = (Activity) view.getContext();

            mImageView.setOnTouchListener((v, event) -> {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    mLastTouchX = event.getX() - (float) mImageView.getPaddingLeft();
                    mLastTouchY = event.getY() - (float) mImageView.getPaddingTop();
                }
                return false;
            });
        }

        void setDefaultComplicationDrawable(@DrawableRes int resourceId) {
            mDefaultComplicationDrawableId = resourceId;
        }

        public void onWatchFacePresetChanged() {
            itemView.invalidate();
        }

        public void onSettingsChanged() {
            itemView.invalidate();
        }

        @Override
        public void onComplicationProviderInfo(
                @NonNull ComplicationHolder complication,
                ComplicationProviderInfo complicationProviderInfo) {
            Log.d(TAG, "updateComplicationViews(): id: " + complication);
            Log.d(TAG, "\tinfo: " + complicationProviderInfo);

            if (complication.isForeground) {
                // Update complication view.
                if (complicationProviderInfo != null &&
                        complicationProviderInfo.providerIcon != null) {
                    complication.setProviderIconDrawable(
                            complicationProviderInfo.providerIcon.loadDrawable(mContext),
                            true);
                    // TODO: make that async

                    onWatchFacePresetChanged();
                } else {
                    Drawable drawable = mContext.getDrawable(mDefaultComplicationDrawableId);
                    if (drawable != null) {
                        complication.setProviderIconDrawable(drawable, false);
                        onWatchFacePresetChanged();
                    }
                }
            }
        }

        void bind(ConfigData.WatchFaceDrawableConfigItem configItem) {
            mConfigItem = configItem;

            String settingsString = mCurrentSettings.getString();
            String watchFacePresetString = mCurrentWatchFacePreset.getString();

            WatchFaceState w = mWatchFaceGlobalDrawable.getWatchFaceState();
            w.getWatchFacePreset().setString(watchFacePresetString);
            w.getSettings().setString(settingsString);
            w.setNotifications(0, 0);
            w.setAmbient(false);
            int[] complicationIds = w.initializeComplications(mContext, this::onWatchFacePresetChanged);

            mImageView.setOnClickListener(v -> {
                // Find out which thing got clicked!
                if (mLastTouchX != -1f || mLastTouchY != -1f) {
                    Optional<ComplicationHolder> nearest = w.getComplications().stream()
                            .filter(c -> c.isForeground)
                            .min(Comparator.comparing(c -> c.distanceFrom(mLastTouchX, mLastTouchY)));
                    if (nearest.isPresent()) {
                        launchComplicationHelperActivity(nearest.get());
                    }
                }
            });

            mImageView.setImageDrawable(mWatchFaceGlobalDrawable);

            mProviderInfoRetriever.retrieveProviderInfo(
                    new ProviderInfoRetriever.OnProviderInfoReceivedCallback() {
                        @Override
                        public void onProviderInfoReceived(
                                int id,
                                @Nullable ComplicationProviderInfo providerInfo) {
                            w.getComplications().stream()
                                    .filter(c -> c.getId() == id)
                                    .forEach(c -> onComplicationProviderInfo(c, providerInfo));
                        }
                    },
                    mWatchFaceComponentName,
                    complicationIds);
        }

        // Verifies the watch face supports the complication location, then launches the helper
        // class, so user can choose their complication data provider.
        private void launchComplicationHelperActivity(ComplicationHolder complication) {
            Log.d("Complication", "Launching for id " + complication.getId());

            mSelectedComplication = complication;

            if (mSelectedComplication != null) {

                ComponentName watchFace = new ComponentName(
                        mCurrentActivity, AnalogComplicationWatchFaceService.class);

                mCurrentActivity.startActivityForResult(
                        ComplicationHelperActivity.createProviderChooserHelperIntent(
                                mCurrentActivity,
                                watchFace,
                                mSelectedComplication.getId(),
                                mSelectedComplication.getSupportedComplicationTypes()),
                        ConfigActivity.COMPLICATION_CONFIG_REQUEST_CODE);

            } else {
                Log.d(TAG, "Complication not supported by watch face.");
            }
        }
    }
}
