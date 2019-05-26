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
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.support.wearable.complications.ComplicationProviderInfo;
import android.support.wearable.complications.ProviderInfoRetriever;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import pro.watchkit.wearable.watchface.R;
import pro.watchkit.wearable.watchface.model.ComplicationHolder;
import pro.watchkit.wearable.watchface.model.ConfigData;
import pro.watchkit.wearable.watchface.model.ConfigData.ColorPickerConfigItem;
import pro.watchkit.wearable.watchface.model.ConfigData.ComplicationConfigItem;
import pro.watchkit.wearable.watchface.model.ConfigData.ConfigActivityConfigItem;
import pro.watchkit.wearable.watchface.model.ConfigData.ConfigItemType;
import pro.watchkit.wearable.watchface.model.ConfigData.NightVisionConfigItem;
import pro.watchkit.wearable.watchface.model.ConfigData.UnreadNotificationConfigItem;
import pro.watchkit.wearable.watchface.model.ConfigData.WatchFaceDrawableConfigItem;
import pro.watchkit.wearable.watchface.model.ConfigData.WatchFacePickerConfigItem;
import pro.watchkit.wearable.watchface.model.ConfigData.WatchFacePresetToggleConfigItem;
import pro.watchkit.wearable.watchface.model.PaintBox;
import pro.watchkit.wearable.watchface.model.WatchFacePreset;

import static pro.watchkit.wearable.watchface.config.ColorSelectionActivity.INTENT_EXTRA_COLOR;
import static pro.watchkit.wearable.watchface.config.ConfigActivity.CONFIG_DATA;
import static pro.watchkit.wearable.watchface.config.WatchFaceSelectionActivity.INTENT_EXTRA_FLAGS;
import static pro.watchkit.wearable.watchface.config.WatchFaceSelectionActivity.INTENT_EXTRA_PRESETS;
import static pro.watchkit.wearable.watchface.config.WatchFaceSelectionActivity.INTENT_EXTRA_SETTINGS;

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
public class ConfigRecyclerViewAdapter extends BaseRecyclerViewAdapter {

    public static final int TYPE_COLOR_PICKER_CONFIG = 0;
    public static final int TYPE_UNREAD_NOTIFICATION_CONFIG = 1;
    public static final int TYPE_NIGHT_VISION_CONFIG = 2;
    public static final int TYPE_WATCH_FACE_DRAWABLE_CONFIG = 3;
    public static final int TYPE_COMPLICATION_CONFIG = 4;
    public static final int TYPE_WATCH_FACE_PRESET_PICKER_CONFIG = 5;
    public static final int TYPE_WATCH_FACE_PRESET_TOGGLE_CONFIG = 6;
    public static final int TYPE_CONFIG_ACTIVITY_CONFIG = 7;
    private static final String TAG = "CompConfigAdapter";
    private SharedPreferences mSharedPref;
    private List<ConfigItemType> mSettingsDataSet;

    private List<WatchFacePresetListener> mWatchFacePresetListeners = new ArrayList<>();
    private List<SettingsListener> mSettingsListeners = new ArrayList<>();
    private List<ComplicationProviderInfoListener> mComplicationProviderInfoListeners =
            new ArrayList<>();
    /**
     * A PaintBox with the current user-selected WatchFacePreset.
     */
    private PaintBox mCurrentPaintBox;

    private Context mContext;

    ConfigRecyclerViewAdapter(
            @NonNull Context context,
            @NonNull Class watchFaceServiceClass,
            @NonNull List<ConfigItemType> settingsDataSet) {
        mContext = context;
        mWatchFaceComponentName = new ComponentName(context, watchFaceServiceClass);
        mSettingsDataSet = settingsDataSet;

        // Default value is invalid (only changed when user taps to change complication).
        mSelectedComplication = null;

        mSharedPref =
                context.getSharedPreferences(
                        context.getString(R.string.analog_complication_preference_file_key),
                        Context.MODE_PRIVATE);

        // Initialization of code to retrieve active complication data for the watch face.
        mProviderInfoRetriever =
                new ProviderInfoRetriever(context, Executors.newCachedThreadPool());
        mProviderInfoRetriever.init();

        regenerateCurrentWatchFacePreset();
        regenerateCurrentSettings();
        mCurrentPaintBox = new PaintBox(context, mCurrentWatchFacePreset);
    }

    /**
     * Regenerates the current WatchFacePreset with what's currently stored in preferences.
     * Call this if you suspect that preferences are changed, before accessing
     * mCurrentWatchFacePreset.
     */
    private void regenerateCurrentWatchFacePreset() {
        mCurrentWatchFacePreset.setString(mSharedPref.getString(
                mContext.getString(R.string.saved_watch_face_preset), null));
    }

    /**
     * Regenerates the current Settings with what's currently stored in preferences.
     * Call this if you suspect that preferences are changed, before accessing
     * mCurrentSettings.
     */
    private void regenerateCurrentSettings() {
        mCurrentSettings.setString(mSharedPref.getString(
                mContext.getString(R.string.saved_settings), null));
    }

    @Override
    @NonNull
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ComplicationHolder.resetBaseId();

        RecyclerView.ViewHolder viewHolder;

        switch (viewType) {
            case TYPE_COLOR_PICKER_CONFIG: {
                viewHolder =
                        new ColorPickerViewHolder(
                                LayoutInflater.from(parent.getContext())
                                        .inflate(R.layout.config_list_color_item, parent, false));
                break;
            }

            case TYPE_CONFIG_ACTIVITY_CONFIG: {
                viewHolder =
                        new ConfigActivityViewHolder(
                                LayoutInflater.from(parent.getContext())
                                        .inflate(R.layout.config_list_color_item, parent, false));
                break;
            }

            case TYPE_WATCH_FACE_DRAWABLE_CONFIG: {
                viewHolder =
                        new WatchFaceDrawableViewHolder(
                                LayoutInflater.from(parent.getContext())
                                        .inflate(R.layout.watch_face_preset_config_list_item, parent, false));
                break;
            }

            case TYPE_COMPLICATION_CONFIG: {
                viewHolder =
                        new ComplicationViewHolder(
                                LayoutInflater.from(parent.getContext())
                                        .inflate(R.layout.watch_face_preset_config_list_item, parent, false));
                break;
            }

            case TYPE_WATCH_FACE_PRESET_PICKER_CONFIG: {
                viewHolder =
                        new WatchFacePresetPickerViewHolder(
                                LayoutInflater.from(parent.getContext())
                                        .inflate(R.layout.config_list_watch_face_preset_item, parent, false));
                break;
            }

            case TYPE_WATCH_FACE_PRESET_TOGGLE_CONFIG: {
                viewHolder =
                        new WatchFacePresetToggleViewHolder(
                                LayoutInflater.from(parent.getContext())
                                        .inflate(R.layout.config_list_toggle, parent, false));
                break;
            }

            case TYPE_UNREAD_NOTIFICATION_CONFIG: {
                viewHolder =
                        new UnreadNotificationViewHolder(
                                LayoutInflater.from(parent.getContext())
                                        .inflate(
                                                R.layout.config_list_toggle,
                                                parent,
                                                false));
                break;
            }

            default: // Default case. Probably shouldn't happen.
            case TYPE_NIGHT_VISION_CONFIG: {
                viewHolder =
                        new NightVisionViewHolder(
                                LayoutInflater.from(parent.getContext())
                                        .inflate(
                                                R.layout.config_list_toggle,
                                                parent,
                                                false));
                break;
            }
        }

        if (viewHolder instanceof WatchFacePresetListener) {
            mWatchFacePresetListeners.add((WatchFacePresetListener) viewHolder);
        }

        if (viewHolder instanceof SettingsListener) {
            mSettingsListeners.add((SettingsListener) viewHolder);
        }

        if (viewHolder instanceof ComplicationProviderInfoListener) {
            mComplicationProviderInfoListeners.add((ComplicationProviderInfoListener) viewHolder);
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        // Pulls all data required for creating the UX for the specific setting option.
        ConfigItemType configItemType = mSettingsDataSet.get(position);

        switch (viewHolder.getItemViewType()) {
            case TYPE_COLOR_PICKER_CONFIG: {
                ColorPickerViewHolder colorPickerViewHolder = (ColorPickerViewHolder) viewHolder;
                ColorPickerConfigItem colorPickerConfigItem = (ColorPickerConfigItem) configItemType;
                colorPickerViewHolder.bind(colorPickerConfigItem);
                break;
            }

            case TYPE_CONFIG_ACTIVITY_CONFIG: {
                ConfigActivityViewHolder configActivityViewHolder = (ConfigActivityViewHolder) viewHolder;
                ConfigActivityConfigItem configActivityConfigItem = (ConfigActivityConfigItem) configItemType;
                configActivityViewHolder.bind(configActivityConfigItem);
                break;
            }

            case TYPE_WATCH_FACE_DRAWABLE_CONFIG: {
                WatchFaceDrawableViewHolder watchFaceDrawableViewHolder =
                        (WatchFaceDrawableViewHolder) viewHolder;
                WatchFaceDrawableConfigItem watchFaceDrawableConfigItem =
                        (WatchFaceDrawableConfigItem) configItemType;
                watchFaceDrawableViewHolder.setWatchFaceGlobalDrawableFlags(watchFaceDrawableConfigItem.getFlags());
                watchFaceDrawableViewHolder.onSettingsChanged();
                break;
            }

            case TYPE_COMPLICATION_CONFIG: {
                ComplicationViewHolder complicationViewHolder =
                        (ComplicationViewHolder) viewHolder;
                ComplicationConfigItem complicationConfigItem =
                        (ComplicationConfigItem) configItemType;

                int defaultComplicationResourceId =
                        complicationConfigItem.getDefaultComplicationResourceId();
                complicationViewHolder.setDefaultComplicationDrawable(
                        defaultComplicationResourceId);
                complicationViewHolder.bind(complicationConfigItem);
                break;
            }

            case TYPE_WATCH_FACE_PRESET_PICKER_CONFIG: {
                WatchFacePresetPickerViewHolder watchFacePresetPickerViewHolder =
                        (WatchFacePresetPickerViewHolder) viewHolder;
                WatchFacePickerConfigItem watchFacePickerConfigItem =
                        (WatchFacePickerConfigItem) configItemType;
                watchFacePresetPickerViewHolder.bind(watchFacePickerConfigItem);
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

            case TYPE_UNREAD_NOTIFICATION_CONFIG: {
                UnreadNotificationViewHolder unreadViewHolder =
                        (UnreadNotificationViewHolder) viewHolder;

                UnreadNotificationConfigItem unreadConfigItem =
                        (UnreadNotificationConfigItem) configItemType;

                int unreadEnabledIconResourceId = unreadConfigItem.getIconEnabledResourceId();
                int unreadDisabledIconResourceId = unreadConfigItem.getIconDisabledResourceId();

                String unreadName = unreadConfigItem.getName();

                unreadViewHolder.setIcons(
                        unreadEnabledIconResourceId, unreadDisabledIconResourceId);
                unreadViewHolder.setName(unreadName);
                break;
            }

            case TYPE_NIGHT_VISION_CONFIG: {
                NightVisionViewHolder nightVisionViewHolder =
                        (NightVisionViewHolder) viewHolder;

                NightVisionConfigItem nightVisionConfigItem =
                        (NightVisionConfigItem) configItemType;

                int nightVisionEnabledIconResourceId = nightVisionConfigItem.getIconEnabledResourceId();
                int nightVisionDisabledIconResourceId = nightVisionConfigItem.getIconDisabledResourceId();

                String nightVisionName = nightVisionConfigItem.getName();

                nightVisionViewHolder.setIcons(
                        nightVisionEnabledIconResourceId, nightVisionDisabledIconResourceId);
                nightVisionViewHolder.setName(nightVisionName);
                break;
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        return mSettingsDataSet.get(position).getConfigType();
    }

    @Override
    public int getItemCount() {
        return mSettingsDataSet.size();
    }

    /**
     * Updates the selected complication id saved earlier with the new information.
     */
    void updateSelectedComplication(ComplicationProviderInfo complicationProviderInfo) {
        mComplicationProviderInfoListeners.forEach(
                c -> c.onComplicationProviderInfo(mSelectedComplication, complicationProviderInfo));
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        // Required to release retriever for active complication data on detach.
        mProviderInfoRetriever.release();
    }

    void onWatchFacePresetChanged() {
        regenerateCurrentWatchFacePreset();
        // Update our WatchFacePresetListener objects.
        mWatchFacePresetListeners.forEach(WatchFacePresetListener::onWatchFacePresetChanged);
    }

    void onSettingsChanged() {
        regenerateCurrentSettings();
        // Update our SettingsListener objects.
        mSettingsListeners.forEach(SettingsListener::onSettingsChanged);
    }

    /**
     * Displays color options for the an item on the watch face. These could include marker color,
     * background color, etc.
     */
    public class ColorPickerViewHolder
            extends RecyclerView.ViewHolder implements OnClickListener, WatchFacePresetListener {

        private Button mButton;
        private Class<ColorSelectionActivity> mLaunchActivity;
        private WatchFacePreset.ColorType mColorType;
        private Drawable mColorSwatchDrawable = new Drawable() {
            private Paint mCirclePaint;

            @Override
            public void draw(@NonNull Canvas canvas) {
                if (mColorType == null) return;

                @ColorInt int color = mCurrentPaintBox.getColor(mColorType);

                // Draw a circle that's 20px from right, top and left borders.
                float radius = (canvas.getClipBounds().height() / 2f) - 20f;
                if (mCirclePaint == null) {
                    // Initialise on first use.
                    mCirclePaint = new Paint();
                    mCirclePaint.setStyle(Paint.Style.FILL);
                    mCirclePaint.setAntiAlias(true);
                }
                mCirclePaint.setColor(color);
                android.graphics.Rect r = canvas.getClipBounds();
                canvas.drawCircle(r.right - 20f - radius,
                        (r.top + r.bottom) / 2f, radius, mCirclePaint);
            }

            @Override
            public void setAlpha(int alpha) {
                // Unused
            }

            @Override
            public void setColorFilter(@Nullable ColorFilter colorFilter) {
                // Unused
            }

            @Override
            public int getOpacity() {
                return PixelFormat.OPAQUE;
            }
        };

        ColorPickerViewHolder(View view) {
            super(view);

            mButton = view.findViewById(R.id.color_picker_button);
            view.setOnClickListener(this);
        }

        void bind(ColorPickerConfigItem configItem) {
            mButton.setText(configItem.getName());
            mButton.setCompoundDrawablesWithIntrinsicBounds(
                    mButton.getContext().getDrawable(configItem.getIconResourceId()),
                    null, mColorSwatchDrawable, null);
            mColorType = configItem.getType();
            mLaunchActivity = configItem.getActivityToChoosePreference();
        }

        public void onWatchFacePresetChanged() {
            itemView.invalidate();
        }

        @Override
        public void onClick(View view) {
            if (mLaunchActivity != null) {
                Intent launchIntent = new Intent(view.getContext(), mLaunchActivity);

                // Pass shared preference name to save color value to.
//                launchIntent.putExtra(INTENT_EXTRA_PRESETS, mSharedPrefResourceString);
                launchIntent.putExtra(INTENT_EXTRA_COLOR, mColorType.name());

                Activity activity = (Activity) view.getContext();
                activity.startActivityForResult(
                        launchIntent,
                        ConfigActivity.UPDATED_CONFIG_REDRAW_PLEASE_REQUEST_CODE);
            }
        }
    }

    public class ConfigActivityViewHolder
            extends RecyclerView.ViewHolder implements OnClickListener {

        private Button mButton;
        private Class<? extends ConfigData> mConfigDataClass;
        private Class<ConfigActivity> mLaunchActivity;

        ConfigActivityViewHolder(View view) {
            super(view);

            mButton = view.findViewById(R.id.color_picker_button);
            view.setOnClickListener(this);
        }

        void bind(ConfigActivityConfigItem configItem) {
            mButton.setText(configItem.getName());
            mButton.setCompoundDrawablesWithIntrinsicBounds(
                    mButton.getContext().getDrawable(configItem.getIconResourceId()),
                    null, null, null);
            mConfigDataClass = configItem.getConfigDataClass();
            mLaunchActivity = configItem.getActivityToChoosePreference();
        }

        @Override
        public void onClick(View view) {
            if (mLaunchActivity != null) {
                Intent launchIntent = new Intent(view.getContext(), mLaunchActivity);

                // Add an intent to the launch to point it towards our sub-activity.
                launchIntent.putExtra(CONFIG_DATA, mConfigDataClass.getSimpleName());

                Activity activity = (Activity) view.getContext();
                activity.startActivityForResult(
                        launchIntent,
                        ConfigActivity.UPDATED_CONFIG_REDRAW_PLEASE_REQUEST_CODE);
            }
        }
    }

    /**
     * Displays color options for the an item on the watch face. These could include marker color,
     * background color, etc.
     */
    public class WatchFacePresetPickerViewHolder extends RecyclerView.ViewHolder
            implements OnClickListener, WatchFacePresetListener, SettingsListener {

        private Button mButton;

        private Class<WatchFaceSelectionActivity> mLaunchActivity;
        private WatchFacePickerConfigItem mConfigItem;
        private int mFlags;

        private int mVisibleLayoutHeight, mVisibleLayoutWidth;

        WatchFacePresetPickerViewHolder(View view) {
            super(view);

            mButton = view.findViewById(R.id.watch_face_preset_picker_button);
            view.setOnClickListener(this);

            mVisibleLayoutHeight = itemView.getLayoutParams().height;
            mVisibleLayoutWidth = itemView.getLayoutParams().width;
        }

        void bind(WatchFacePickerConfigItem configItem) {
            mConfigItem = configItem;
            mLaunchActivity = configItem.getActivityToChoosePreference();
            mFlags = configItem.getWatchFaceGlobalDrawableFlags();

            setTextAndVisibility();
        }

        private void setTextAndVisibility() {
            mButton.setText(mConfigItem.getName(
                    mCurrentWatchFacePreset, mCurrentSettings, mButton.getContext()));

            ViewGroup.LayoutParams param = itemView.getLayoutParams();
            if (mConfigItem.isVisible(mCurrentWatchFacePreset, mCurrentSettings)) {
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

        public void onSettingsChanged() {
            onWatchFacePresetChanged();
        }

        public void onWatchFacePresetChanged() {
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
                String[] permutations0 = mConfigItem.permute(mCurrentWatchFacePreset);
                String[] permutations1 = mConfigItem.permute(mCurrentSettings);

                // If either permutations0 or 1 is null, fill with the current settings.
                if (permutations0 == null && permutations1 != null) {
                    permutations0 = new String[permutations1.length];
                    for (int i = 0; i < permutations1.length; i++) {
                        permutations0[i] = mCurrentWatchFacePreset.getString();
                    }
                } else if (permutations1 == null && permutations0 != null) {
                    permutations1 = new String[permutations0.length];
                    for (int i = 0; i < permutations0.length; i++) {
                        permutations1[i] = mCurrentSettings.getString();
                    }
                }

                Intent launchIntent = new Intent(view.getContext(), mLaunchActivity);

                // Pass shared preference name to save color value to.
                launchIntent.putExtra(INTENT_EXTRA_PRESETS, permutations0);
                launchIntent.putExtra(INTENT_EXTRA_SETTINGS, permutations1);
                launchIntent.putExtra(INTENT_EXTRA_FLAGS, mFlags);

                Activity activity = (Activity) view.getContext();
                activity.startActivityForResult(
                        launchIntent,
                        ConfigActivity.UPDATED_CONFIG_REDRAW_PLEASE_REQUEST_CODE);
            }
        }
    }

    /**
     * Displays switch to indicate whether or not the given WatchFacePreset flag is toggled on/off.
     */
    public class WatchFacePresetToggleViewHolder extends ToggleViewHolder
            implements WatchFacePresetListener {

        WatchFacePresetToggleViewHolder(View view) {
            super(view);
        }

        private WatchFacePresetToggleConfigItem mConfigItem;

        void bind(WatchFacePresetToggleConfigItem configItem) {
            mConfigItem = configItem;

            setName(configItem.getName());
            setIcons(configItem.getIconEnabledResourceId(),
                    configItem.getIconDisabledResourceId());

            setDefaultSwitchValue();
        }

        @Override
        void setDefaultSwitchValue() {
            // Regenerate and grab our current permutations. Just in time!
            String[] permutations = mConfigItem.permute(mCurrentWatchFacePreset);
            setChecked(mCurrentWatchFacePreset.getString().equals(permutations[1]));
        }

        public void onWatchFacePresetChanged() {
            setDefaultSwitchValue();
        }

        @Override
        public void onClick(View view) {
            // Regenerate and grab our current permutations. Just in time!
            String[] permutations = mConfigItem.permute(mCurrentWatchFacePreset);

            Boolean newState = isChecked();
            Context context = view.getContext();
            SharedPreferences.Editor editor = mSharedPref.edit();
            editor.putString(context.getString(R.string.saved_watch_face_preset),
                    newState ? permutations[1] : permutations[0]);
            editor.apply();

            setChecked(newState);

            ConfigRecyclerViewAdapter.this.onWatchFacePresetChanged();
        }
    }

    /**
     * Displays switch to indicate whether or not icon appears for unread notifications. User can
     * toggle on/off.
     */
    public class UnreadNotificationViewHolder extends ToggleViewHolder
            implements SettingsListener {
        UnreadNotificationViewHolder(View view) {
            super(view);
        }

        @Override
        void setDefaultSwitchValue() {
            setChecked(mCurrentSettings.isShowUnreadNotifications());
        }

        @Override
        public void onClick(View view) {
            Context context = view.getContext();
            String sharedPreferenceString = context.getString(R.string.saved_settings);

            // Since user clicked on a switch, new state should be opposite of current state.
            mCurrentSettings.toggleShowUnreadNotifications();

            SharedPreferences.Editor editor = mSharedPref.edit();
            editor.putString(sharedPreferenceString, mCurrentSettings.getString());
            editor.apply();

            ConfigRecyclerViewAdapter.this.onSettingsChanged();
        }

        @Override
        public void onSettingsChanged() {
            setDefaultSwitchValue();
        }
    }

    /**
     * Displays switch to indicate whether or not night vision is toggled on/off.
     */
    public class NightVisionViewHolder extends ToggleViewHolder
            implements SettingsListener {
        final private int MY_PERMISSION_ACCESS_COURSE_LOCATION = 1;

        NightVisionViewHolder(View view) {
            super(view);
        }

        @Override
        void setDefaultSwitchValue() {
            setChecked(mCurrentSettings.isNightVisionModeEnabled());
        }

        @Override
        public void onClick(View view) {
            Context context = view.getContext();
            String sharedPreferenceString = context.getString(R.string.saved_settings);

            // Since user clicked on a switch, new state should be opposite of current state.
            boolean newState = mCurrentSettings.toggleNightVisionModeEnabled();

            if (newState && context.checkSelfPermission(
                    android.Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                Activity a = (Activity) context;
                a.requestPermissions(new String[]{
                                android.Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_PERMISSION_ACCESS_COURSE_LOCATION);
            }

            SharedPreferences.Editor editor = mSharedPref.edit();
            editor.putString(sharedPreferenceString, mCurrentSettings.getString());
            editor.apply();

            ConfigRecyclerViewAdapter.this.onSettingsChanged();
        }

        @Override
        public void onSettingsChanged() {
            setDefaultSwitchValue();
        }
    }

    /**
     * Displays switch to indicate whether or not night vision is toggled on/off.
     */
    public abstract class ToggleViewHolder extends RecyclerView.ViewHolder
            implements OnClickListener {
        private Switch mSwitch;
        private int mEnabledIconResourceId;
        private int mDisabledIconResourceId;

        ToggleViewHolder(View view) {
            super(view);

            mSwitch = view.findViewById(R.id.config_list_toggle);
            view.setOnClickListener(this);
        }

        public void setName(String name) {
            mSwitch.setText(name);
        }

        void setIcons(int enabledIconResourceId, int disabledIconResourceId) {

            mEnabledIconResourceId = enabledIconResourceId;
            mDisabledIconResourceId = disabledIconResourceId;

            setDefaultSwitchValue();
        }

        abstract void setDefaultSwitchValue();

        boolean isChecked() {
            return mSwitch.isChecked();
        }

        void setChecked(Boolean checked) {
            int currentIconResourceId;

            if (checked) {
                currentIconResourceId = mEnabledIconResourceId;
            } else {
                currentIconResourceId = mDisabledIconResourceId;
            }

            mSwitch.setChecked(checked);
            mSwitch.setCompoundDrawablesWithIntrinsicBounds(
                    mSwitch.getContext().getDrawable(currentIconResourceId), null, null, null);
        }

        @Override
        abstract public void onClick(View view);
    }
}
