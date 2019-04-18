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
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.wearable.complications.ProviderInfoRetriever;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import pro.watchkit.wearable.watchface.R;
import pro.watchkit.wearable.watchface.model.ComplicationHolder;
import pro.watchkit.wearable.watchface.model.PaintBox;
import pro.watchkit.wearable.watchface.model.WatchFacePreset;
import pro.watchkit.wearable.watchface.model.WatchPartHandsConfigData;
import pro.watchkit.wearable.watchface.model.WatchPartHandsConfigData.ConfigItemType;
import pro.watchkit.wearable.watchface.model.WatchPartHandsConfigData.WatchFacePresetPickerConfigItem;
import pro.watchkit.wearable.watchface.model.WatchPartHandsConfigData.WatchFacePresetToggleConfigItem;

import static pro.watchkit.wearable.watchface.config.ColorSelectionActivity.EXTRA_SHARED_PREF;

/**
 * Displays different layouts for configuring watch face's complications and appearance settings
 * (highlight color [second arm], background color, unread notifications, etc.).
 *
 * <p>All appearance settings are saved via {@link SharedPreferences}.
 *
 * <p>Layouts provided by this adapter are split into 5 main view types.
 *
 * <p>A watch face preview including complications. Allows user to tap on the complications to
 * change the complication data and see a live preview of the watch face.
 *
 * <p>Simple arrow to indicate there are more options below the fold.
 *
 * <p>Color configuration options for both highlight (seconds hand) and background color.
 *
 * <p>Toggle for unread notifications.
 *
 * <p>Background image complication configuration for changing background image of watch face.
 */
public class WatchPartHandsConfigRecyclerViewAdapter
        extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int TYPE_WATCH_FACE_PRESET_PICKER_CONFIG = 6;
    public static final int TYPE_WATCH_FACE_PRESET_TOGGLE_CONFIG = 7;
    private static final String TAG = "CompConfigAdapter";
    private SharedPreferences mSharedPref;
    private ArrayList<ConfigItemType> mSettingsDataSet;
    private Context mContext;

    // Required to retrieve complication data from watch face for preview.
    private ProviderInfoRetriever mProviderInfoRetriever;

    private List<Ticklish> mTicklish = new ArrayList<>();

    /**
     * The current user-selected WatchFacePreset with what's currently stored in preferences.
     */
    private WatchFacePreset mCurrentWatchFacePreset = new WatchFacePreset();
    /**
     * A PaintBox with the current user-selected WatchFacePreset.
     */
    private PaintBox mCurrentPaintBox;

    WatchPartHandsConfigRecyclerViewAdapter(
            Context context,
            ArrayList<ConfigItemType> settingsDataSet) {
        mContext = context;
        mSettingsDataSet = settingsDataSet;

        mSharedPref =
                context.getSharedPreferences(
                        context.getString(R.string.analog_complication_preference_file_key),
                        Context.MODE_PRIVATE);

        // Initialization of code to retrieve active complication data for the watch face.
        mProviderInfoRetriever =
                new ProviderInfoRetriever(mContext, Executors.newCachedThreadPool());
        mProviderInfoRetriever.init();

        mCurrentPaintBox = new PaintBox(context, mCurrentWatchFacePreset);
    }

    /**
     * Regenerates the current WatchFacePreset with what's currently stored in preferences.
     * Call this if you suspect that preferences are changed, before accessing
     * mCurrentWatchFacePreset.
     *
     * @param context Current application context, to get preferences from
     * @return mCurrentWatchFacePreset, for convenience
     */
    private WatchFacePreset regenerateCurrentWatchFacePreset(Context context) {
        SharedPreferences preferences =
                context.getSharedPreferences(
                        context.getString(R.string.analog_complication_preference_file_key),
                        Context.MODE_PRIVATE);
        mCurrentWatchFacePreset.setString(preferences.getString(
                context.getString(R.string.saved_watch_face_preset), null));

        return mCurrentWatchFacePreset;
    }

    @Override
    @NonNull
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ComplicationHolder.resetBaseId();

        RecyclerView.ViewHolder viewHolder;

        switch (viewType) {
            case TYPE_WATCH_FACE_PRESET_PICKER_CONFIG: {
                viewHolder = new WatchFacePresetPickerViewHolder(
                        LayoutInflater.from(parent.getContext()).inflate(
                                R.layout.config_list_watch_face_preset_item, parent,
                                false));
                mTicklish.add((Ticklish) viewHolder);
                break;
            }

            case TYPE_WATCH_FACE_PRESET_TOGGLE_CONFIG:
            default: {
                viewHolder = new WatchFacePresetToggleViewHolder(
                        LayoutInflater.from(parent.getContext()).inflate(
                                R.layout.config_list_watch_face_preset_toggle, parent,
                                false));
                mTicklish.add((Ticklish) viewHolder);
                break;
            }
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        // Pulls all data required for creating the UX for the specific setting option.
        ConfigItemType configItemType = mSettingsDataSet.get(position);

        switch (viewHolder.getItemViewType()) {
            case TYPE_WATCH_FACE_PRESET_PICKER_CONFIG: {
                WatchFacePresetPickerViewHolder watchFacePresetPickerViewHolder =
                        (WatchFacePresetPickerViewHolder) viewHolder;
                WatchFacePresetPickerConfigItem watchFacePresetPickerConfigItem =
                        (WatchPartHandsConfigData.WatchFacePresetPickerConfigItem) configItemType;
                watchFacePresetPickerViewHolder.bind(watchFacePresetPickerConfigItem);
                break;
            }

            case TYPE_WATCH_FACE_PRESET_TOGGLE_CONFIG: {
                WatchFacePresetToggleViewHolder watchFacePresetToggleViewHolder =
                        (WatchFacePresetToggleViewHolder) viewHolder;
                WatchFacePresetToggleConfigItem watchFacePresetToggleConfigItem =
                        (WatchFacePresetToggleConfigItem) configItemType;
                watchFacePresetToggleViewHolder.bind(watchFacePresetToggleConfigItem);
                break;
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        ConfigItemType configItemType = mSettingsDataSet.get(position);
        return configItemType.getConfigType();
    }

    @Override
    public int getItemCount() {
        return mSettingsDataSet.size();
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        // Required to release retriever for active complication data on detach.
        mProviderInfoRetriever.release();
    }

    void updatePreviewColors() {
        // Update our Ticklish objects.
        for (Ticklish t : mTicklish) {
            t.tickle();
        }
    }

    /**
     * An object is ticklish if it can be tickled. When ticked, an object invalidates and
     * redraws itself.
     */
    private interface Ticklish {
        /**
         * Invalidate this object.
         */
        void tickle();
    }

    /**
     * Displays color options for the an item on the watch face. These could include marker color,
     * background color, etc.
     */
    public class WatchFacePresetPickerViewHolder
            extends RecyclerView.ViewHolder implements OnClickListener, Ticklish {

        private Button mButton;

        private Class<WatchFacePresetSelectionActivity> mLaunchActivity;
        private WatchFacePresetPickerConfigItem mConfigItem;

        private int mVisibleLayoutHeight, mVisibleLayoutWidth;

        WatchFacePresetPickerViewHolder(View view) {
            super(view);

            mButton = view.findViewById(R.id.watch_face_preset_picker_button);
            view.setOnClickListener(this);

            mVisibleLayoutHeight = itemView.getLayoutParams().height;
            mVisibleLayoutWidth = itemView.getLayoutParams().width;
        }

        void bind(WatchFacePresetPickerConfigItem configItem) {
            mConfigItem = configItem;
            mLaunchActivity = configItem.getActivityToChoosePreference();

            setTextAndVisibility();
        }

        private void setTextAndVisibility() {
            mButton.setText(mConfigItem.getName(mCurrentWatchFacePreset, mButton.getContext()));

            ViewGroup.LayoutParams param = itemView.getLayoutParams();
            if (mConfigItem.isVisible(mCurrentWatchFacePreset)) {
                param.height = mVisibleLayoutHeight;
                param.width = mVisibleLayoutWidth;
                itemView.setVisibility(View.VISIBLE);
            } else {
                param.height = 0;
                param.width = 0;
                itemView.setVisibility(View.GONE);
            }
            itemView.setLayoutParams(param);
        }

        public void tickle() {
            String oldText = mButton.getText().toString();
            int oldVisibility = itemView.getVisibility();

            setTextAndVisibility();

            String newText = mButton.getText().toString();
            int newVisibility = itemView.getVisibility();
            if (!oldText.equals(newText) &&
                    oldVisibility == View.VISIBLE && newVisibility == View.VISIBLE) {
                // Show a toast if our text has changed, which assumes this was the setting
                // that changed.
                // Don't show if we were previously invisible.
                // Only show if we are visible.
                Toast.makeText(mButton.getContext(), newText, Toast.LENGTH_LONG).show();
            }
            itemView.invalidate();
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            Log.d(TAG, "Complication onClick() position: " + position);

            if (mLaunchActivity != null) {
                // Regenerate and grab our current permutations. Just in time!
                String[] permutations =
                        mConfigItem.permute(regenerateCurrentWatchFacePreset(mButton.getContext()));

                Intent launchIntent = new Intent(view.getContext(), mLaunchActivity);

                // Pass shared preference name to save color value to.
                launchIntent.putExtra(EXTRA_SHARED_PREF, permutations);

                Activity activity = (Activity) view.getContext();
                activity.startActivityForResult(
                        launchIntent,
                        WatchPartHandsConfigActivity.UPDATED_CONFIG_REDRAW_PLEASE_REQUEST_CODE);
            }
        }
    }

    /**
     * Displays switch to indicate whether or not the given WatchFacePreset flag is toggled on/off.
     */
    public class WatchFacePresetToggleViewHolder
            extends RecyclerView.ViewHolder implements OnClickListener, Ticklish {
        private Switch mToggleSwitch;
        private int mEnabledIconResourceId;
        private int mDisabledIconResourceId;
        private WatchFacePresetToggleConfigItem mConfigItem;

        WatchFacePresetToggleViewHolder(View view) {
            super(view);

            mToggleSwitch = view.findViewById(R.id.watch_face_preset_toggle_switch);
            view.setOnClickListener(this);
        }

        void bind(WatchFacePresetToggleConfigItem configItem) {
            mConfigItem = configItem;

            mToggleSwitch.setText(configItem.getName());
            mEnabledIconResourceId = configItem.getIconEnabledResourceId();
            mDisabledIconResourceId = configItem.getIconDisabledResourceId();

            Context context = mToggleSwitch.getContext();

            // Set default to enabled.
            mToggleSwitch.setCompoundDrawablesWithIntrinsicBounds(
                    context.getDrawable(mEnabledIconResourceId), null, null, null);

            tickle();
        }

        public void tickle() {
            // Regenerate and grab our current permutations. Just in time!
            String[] permutations =
                    mConfigItem.permute(regenerateCurrentWatchFacePreset(mToggleSwitch.getContext()));
            mToggleSwitch.setChecked(mCurrentWatchFacePreset.getString().equals(permutations[1]));
            itemView.invalidate();
        }

        private void updateIcon(Context context, Boolean currentState) {
            int currentIconResourceId;

            if (currentState) {
                currentIconResourceId = mEnabledIconResourceId;
            } else {
                currentIconResourceId = mDisabledIconResourceId;
            }

            mToggleSwitch.setChecked(currentState);
            mToggleSwitch.setCompoundDrawablesWithIntrinsicBounds(
                    context.getDrawable(currentIconResourceId), null, null, null);
        }

        @Override
        public void onClick(View view) {
            // Regenerate and grab our current permutations. Just in time!
            String[] permutations =
                    mConfigItem.permute(regenerateCurrentWatchFacePreset(mToggleSwitch.getContext()));

            Boolean newState = mToggleSwitch.isChecked();
            Context context = view.getContext();
            SharedPreferences.Editor editor = mSharedPref.edit();
            editor.putString(context.getString(R.string.saved_watch_face_preset),
                    newState ? permutations[1] : permutations[0]);
            editor.apply();

            updateIcon(context, newState);

            updatePreviewColors();
        }
    }
}
