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
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.support.wearable.complications.ComplicationHelperActivity;
import android.support.wearable.complications.ComplicationProviderInfo;
import android.support.wearable.complications.ProviderInfoRetriever;
import android.support.wearable.complications.ProviderInfoRetriever.OnProviderInfoReceivedCallback;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executors;

import pro.watchkit.wearable.watchface.R;
import pro.watchkit.wearable.watchface.model.ComplicationHolder;
import pro.watchkit.wearable.watchface.model.ConfigData;
import pro.watchkit.wearable.watchface.model.ConfigData.BackgroundComplicationConfigItem;
import pro.watchkit.wearable.watchface.model.ConfigData.ColorPickerConfigItem;
import pro.watchkit.wearable.watchface.model.ConfigData.ConfigActivityConfigItem;
import pro.watchkit.wearable.watchface.model.ConfigData.ConfigItemType;
import pro.watchkit.wearable.watchface.model.ConfigData.MoreOptionsConfigItem;
import pro.watchkit.wearable.watchface.model.ConfigData.NightVisionConfigItem;
import pro.watchkit.wearable.watchface.model.ConfigData.PreviewAndComplicationsConfigItem;
import pro.watchkit.wearable.watchface.model.ConfigData.UnreadNotificationConfigItem;
import pro.watchkit.wearable.watchface.model.ConfigData.WatchFacePickerConfigItem;
import pro.watchkit.wearable.watchface.model.ConfigData.WatchFacePresetToggleConfigItem;
import pro.watchkit.wearable.watchface.model.PaintBox;
import pro.watchkit.wearable.watchface.model.Settings;
import pro.watchkit.wearable.watchface.model.WatchFacePreset;
import pro.watchkit.wearable.watchface.watchface.AnalogComplicationWatchFaceService;

import static pro.watchkit.wearable.watchface.config.ColorSelectionActivity.INTENT_EXTRA_COLOR;
import static pro.watchkit.wearable.watchface.config.ConfigActivity.CONFIG_DATA;
import static pro.watchkit.wearable.watchface.config.WatchFaceSelectionActivity.INTENT_EXTRA_PRESETS;

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
public class ConfigRecyclerViewAdapter
        extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    /**
     * Used by associated watch face ({@link AnalogComplicationWatchFaceService}) to let this
     * adapter know which complication locations are supported, their ids, and supported
     * complication data types.
     */
//    public enum ComplicationLocation {
//        BACKGROUND,
//        LEFT,
//        RIGHT,
//        TOP,
//        BOTTOM
//    }

    public static final int TYPE_PREVIEW_AND_COMPLICATIONS_CONFIG = 0;
    public static final int TYPE_MORE_OPTIONS = 1;
    public static final int TYPE_COLOR_PICKER_CONFIG = 2;
    public static final int TYPE_UNREAD_NOTIFICATION_CONFIG = 3;
    public static final int TYPE_BACKGROUND_COMPLICATION_IMAGE_CONFIG = 4;
    public static final int TYPE_NIGHT_VISION_CONFIG = 5;
    public static final int TYPE_WATCH_FACE_PRESET_PICKER_CONFIG = 6;
    public static final int TYPE_WATCH_FACE_PRESET_TOGGLE_CONFIG = 7;
    public static final int TYPE_CONFIG_ACTIVITY_CONFIG = 8;
    private static final String TAG = "CompConfigAdapter";
    private SharedPreferences mSharedPref;
    private Collection<ComplicationHolder> complications;
    private ComplicationHolder backgroundComplication;
    // ComponentName associated with watch face service (service that renders watch face). Used
    // to retrieve complication information.
    private ComponentName mWatchFaceComponentName;
    private ArrayList<ConfigItemType> mSettingsDataSet;
    private Context mContext;
    // Selected complication id by user.
    //private int mSelectedComplicationId;
    private ComplicationHolder mSelectedComplication;

    //private int mBackgroundComplicationId;
    //private int mLeftComplicationId;
    //private int mBottomComplicationId;
    //private int mRightComplicationId;

    // Required to retrieve complication data from watch face for preview.
    private ProviderInfoRetriever mProviderInfoRetriever;

    // Maintains reference view holder to dynamically update watch face preview. Used instead of
    // notifyItemChanged(int position) to avoid flicker and re-inflating the view.
    private PreviewAndComplicationsViewHolder mPreviewAndComplicationsViewHolder;
    private List<Ticklish> mTicklish = new ArrayList<>();

    /**
     * The current user-selected WatchFacePreset with what's currently stored in preferences.
     */
    private WatchFacePreset mCurrentWatchFacePreset = new WatchFacePreset();
    /**
     * A PaintBox with the current user-selected WatchFacePreset.
     */
    private PaintBox mCurrentPaintBox;

    ConfigRecyclerViewAdapter(
            Context context,
            Class watchFaceServiceClass,
            ArrayList<ConfigItemType> settingsDataSet) {
        complications = new ArrayList<>();

        mContext = context;
        mWatchFaceComponentName = new ComponentName(mContext, watchFaceServiceClass);
        mSettingsDataSet = settingsDataSet;

        // Default value is invalid (only changed when user taps to change complication).
        mSelectedComplication = null;
        //mSelectedComplicationId = -1;

//        mBackgroundComplicationId =
//                AnalogComplicationWatchFaceService.getComplicationId(
//                        ComplicationLocation.BACKGROUND);
//
//        mLeftComplicationId =
//                AnalogComplicationWatchFaceService.getComplicationId(ComplicationLocation.LEFT);
//        mBottomComplicationId =
//                AnalogComplicationWatchFaceService.getComplicationId(ComplicationLocation.BOTTOM);
//        mRightComplicationId =
//                AnalogComplicationWatchFaceService.getComplicationId(ComplicationLocation.RIGHT);

        mSharedPref =
                context.getSharedPreferences(
                        context.getString(R.string.analog_complication_preference_file_key),
                        Context.MODE_PRIVATE);

        // Initialization of code to retrieve active complication data for the watch face.
        mProviderInfoRetriever =
                new ProviderInfoRetriever(mContext, Executors.newCachedThreadPool());
        mProviderInfoRetriever.init();

        mCurrentPaintBox = new PaintBox(context, regenerateCurrentWatchFacePreset(context));
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
            case TYPE_PREVIEW_AND_COMPLICATIONS_CONFIG: {
                // Need direct reference to watch face preview view holder to update watch face
                // preview based on selections from the user.
                mPreviewAndComplicationsViewHolder =
                        new PreviewAndComplicationsViewHolder(
                                LayoutInflater.from(parent.getContext())
                                        .inflate(
                                                R.layout.config_list_preview_and_complications_item,
                                                parent,
                                                false));
                viewHolder = mPreviewAndComplicationsViewHolder;
                break;
            }

            case TYPE_MORE_OPTIONS: {
                viewHolder =
                        new MoreOptionsViewHolder(
                                LayoutInflater.from(parent.getContext())
                                        .inflate(
                                                R.layout.config_list_more_options_item,
                                                parent,
                                                false));
                break;
            }

            case TYPE_COLOR_PICKER_CONFIG: {
                viewHolder =
                        new ColorPickerViewHolder(
                                LayoutInflater.from(parent.getContext())
                                        .inflate(R.layout.config_list_color_item, parent, false));
                mTicklish.add((Ticklish) viewHolder);
                break;
            }

            case TYPE_CONFIG_ACTIVITY_CONFIG: {
                viewHolder =
                        new ConfigActivityViewHolder(
                                LayoutInflater.from(parent.getContext())
                                        .inflate(R.layout.config_list_color_item, parent, false));
                break;
            }

            case TYPE_WATCH_FACE_PRESET_PICKER_CONFIG: {
                viewHolder =
                        new WatchFacePresetPickerViewHolder(
                                LayoutInflater.from(parent.getContext())
                                        .inflate(R.layout.config_list_watch_face_preset_item, parent, false));
                mTicklish.add((Ticklish) viewHolder);
                break;
            }

            case TYPE_WATCH_FACE_PRESET_TOGGLE_CONFIG: {
                viewHolder =
                        new WatchFacePresetToggleViewHolder(
                                LayoutInflater.from(parent.getContext())
                                        .inflate(R.layout.config_list_toggle, parent, false));
                mTicklish.add((Ticklish) viewHolder);
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

            case TYPE_BACKGROUND_COMPLICATION_IMAGE_CONFIG: {
                viewHolder =
                        new BackgroundComplicationViewHolder(
                                LayoutInflater.from(parent.getContext())
                                        .inflate(
                                                R.layout.config_list_background_complication_item,
                                                parent,
                                                false));
                break;
            }

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

            default: {
                // Default case. Probably shouldn't happen.
                viewHolder =
                        new MoreOptionsViewHolder(
                                LayoutInflater.from(parent.getContext())
                                        .inflate(
                                                R.layout.config_list_more_options_item,
                                                parent,
                                                false));
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
            case TYPE_PREVIEW_AND_COMPLICATIONS_CONFIG: {
                PreviewAndComplicationsViewHolder previewAndComplicationsViewHolder =
                        (PreviewAndComplicationsViewHolder) viewHolder;

                PreviewAndComplicationsConfigItem previewAndComplicationsConfigItem =
                        (PreviewAndComplicationsConfigItem) configItemType;

                int defaultComplicationResourceId =
                        previewAndComplicationsConfigItem.getDefaultComplicationResourceId();
                previewAndComplicationsViewHolder.setDefaultComplicationDrawable(
                        defaultComplicationResourceId);

                previewAndComplicationsViewHolder.initializesColorsAndComplications();
                break;
            }

            case TYPE_MORE_OPTIONS: {
                MoreOptionsViewHolder moreOptionsViewHolder = (MoreOptionsViewHolder) viewHolder;
                MoreOptionsConfigItem moreOptionsConfigItem =
                        (MoreOptionsConfigItem) configItemType;

                moreOptionsViewHolder.setIcon(moreOptionsConfigItem.getIconResourceId());
                break;
            }

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
//                int unreadSharedPrefId = unreadConfigItem.getSharedPrefId();

                unreadViewHolder.setIcons(
                        unreadEnabledIconResourceId, unreadDisabledIconResourceId);
                unreadViewHolder.setName(unreadName);
//                unreadViewHolder.setSharedPrefId(unreadSharedPrefId);
                break;
            }

            case TYPE_BACKGROUND_COMPLICATION_IMAGE_CONFIG: {
                BackgroundComplicationViewHolder backgroundComplicationViewHolder =
                        (BackgroundComplicationViewHolder) viewHolder;

                BackgroundComplicationConfigItem backgroundComplicationConfigItem =
                        (BackgroundComplicationConfigItem) configItemType;

                int backgroundIconResourceId = backgroundComplicationConfigItem.getIconResourceId();
                String backgroundName = backgroundComplicationConfigItem.getName();

                backgroundComplicationViewHolder.setIcon(backgroundIconResourceId);
                backgroundComplicationViewHolder.setName(backgroundName);
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
//                int nightVisionSharedPrefId = nightVisionConfigItem.getSharedPrefId();

                nightVisionViewHolder.setIcons(
                        nightVisionEnabledIconResourceId, nightVisionDisabledIconResourceId);
                nightVisionViewHolder.setName(nightVisionName);
//                nightVisionViewHolder.setSharedPrefId(nightVisionSharedPrefId);
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

    /**
     * Updates the selected complication id saved earlier with the new information.
     */
    void updateSelectedComplication(ComplicationProviderInfo complicationProviderInfo) {

        Log.d(TAG, "updateSelectedComplication: " + mPreviewAndComplicationsViewHolder);

        // Checks if view is inflated and complication id is valid.
        if (mPreviewAndComplicationsViewHolder != null && mSelectedComplication != null) {
//        if (mPreviewAndComplicationsViewHolder != null && mSelectedComplicationId >= 0) {
            mPreviewAndComplicationsViewHolder.updateComplicationViews(
                    mSelectedComplication, complicationProviderInfo);
//                    mSelectedComplicationId, complicationProviderInfo);
        }
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        // Required to release retriever for active complication data on detach.
        mProviderInfoRetriever.release();
    }

    void updatePreviewColors() {
        Log.d(TAG, "updatePreviewColors(): " + mPreviewAndComplicationsViewHolder);

        regenerateCurrentWatchFacePreset(mContext);

        if (mPreviewAndComplicationsViewHolder != null) {
            mPreviewAndComplicationsViewHolder.updateWatchFaceColors();
        }

        // Update our Ticklish objects.
        for (Ticklish t : mTicklish) {
            t.tickle();
        }
    }

    /**
     * Displays watch face preview along with complication locations. Allows user to tap on the
     * complication they want to change and preview updates dynamically.
     */
    public class PreviewAndComplicationsViewHolder extends RecyclerView.ViewHolder
            implements OnClickListener {

        private View mWatchFaceArmsAndTicksView;
        private View mWatchFaceHighlightPreviewView;
//        private ImageView mWatchFaceBackgroundPreviewImageView;
//
//        private ImageView mLeftComplicationBackground;
//        private ImageView mBottomComplicationBackground;
//        private ImageView mRightComplicationBackground;
//
//        private ImageButton mLeftComplication;
//        private ImageButton mBottomComplication;
//        private ImageButton mRightComplication;

        private Drawable mDefaultComplicationDrawable;

        private boolean mBackgroundComplicationEnabled;

        PreviewAndComplicationsViewHolder(final View view) {
            super(view);

            backgroundComplication = new ComplicationHolder(null);
            backgroundComplication.background = view.findViewById(R.id.watch_face_background);
            complications.add(backgroundComplication);
//            mWatchFaceBackgroundPreviewImageView =
//                    (ImageView) view.findViewById(R.id.watch_face_background);
            mWatchFaceArmsAndTicksView = view.findViewById(R.id.watch_face_arms_and_ticks);

            // In our case, just the second arm.
            mWatchFaceHighlightPreviewView = view.findViewById(R.id.watch_face_highlight);

            // Sets up left complication preview.
            {
                ComplicationHolder f = new ComplicationHolder(null);
                f.isForeground = true;
                f.background =
                        view.findViewById(R.id.left_complication_background);
                f.imageButton = view.findViewById(R.id.left_complication);
                f.imageButton.setOnClickListener(this);
                complications.add(f);
            }
            // Sets up bottom complication preview.
            {
                ComplicationHolder f = new ComplicationHolder(null);
                f.isForeground = true;
                f.background =
                        view.findViewById(R.id.bottom_complication_background);
                f.imageButton = view.findViewById(R.id.bottom_complication);
                f.imageButton.setOnClickListener(this);
                complications.add(f);
            }
            // Sets up right complication preview.
            {
                ComplicationHolder f = new ComplicationHolder(null);
                f.isForeground = true;
                f.background =
                        view.findViewById(R.id.right_complication_background);
                f.imageButton = view.findViewById(R.id.right_complication);
                f.imageButton.setOnClickListener(this);
                complications.add(f);
            }

//            // Sets up left complication preview.
//            mLeftComplicationBackground =
//                    (ImageView) view.findViewById(R.id.left_complication_background);
//            mLeftComplication = (ImageButton) view.findViewById(R.id.left_complication);
//            mLeftComplication.setOnClickListener(this);
//
//            // Sets up bottom complication preview.
//            mBottomComplicationBackground =
//                    (ImageView) view.findViewById(R.id.bottom_complication_background);
//            mBottomComplication = (ImageButton) view.findViewById(R.id.bottom_complication);
//            mBottomComplication.setOnClickListener(this);
//
//            // Sets up right complication preview.
//            mRightComplicationBackground =
//                    (ImageView) view.findViewById(R.id.right_complication_background);
//            mRightComplication = (ImageButton) view.findViewById(R.id.right_complication);
//            mRightComplication.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            for (ComplicationHolder complication : complications) {
                if (view.equals(complication.imageButton)) {
                    Activity currentActivity = (Activity) view.getContext();
                    launchComplicationHelperActivity(currentActivity, complication);
                }
            }
//            if (view.equals(mLeftComplication)) {
//                Log.d(TAG, "Left Complication click()");
//
//                Activity currentActivity = (Activity) view.getContext();
//                launchComplicationHelperActivity(currentActivity, ComplicationLocation.LEFT);
//
//            } else if (view.equals(mBottomComplication)) {
//                Log.d(TAG, "Bottom Complication click()");
//
//                Activity currentActivity = (Activity) view.getContext();
//                launchComplicationHelperActivity(currentActivity, ComplicationLocation.BOTTOM);
//
//            } else if (view.equals(mRightComplication)) {
//                Log.d(TAG, "Right Complication click()");
//
//                Activity currentActivity = (Activity) view.getContext();
//                launchComplicationHelperActivity(currentActivity, ComplicationLocation.RIGHT);
//            }
        }

        void updateWatchFaceColors() {

            // Only update background colors for preview if background complications are disabled.
            if (!mBackgroundComplicationEnabled) {
                // Updates background color.
                String backgroundSharedPrefString =
                        mContext.getString(R.string.saved_background_color);
                int currentBackgroundColor =
                        mSharedPref.getInt(backgroundSharedPrefString, Color.BLACK);

                PorterDuffColorFilter backgroundColorFilter =
                        new PorterDuffColorFilter(currentBackgroundColor, PorterDuff.Mode.SRC_ATOP);

                backgroundComplication.background.getBackground()
                        .setColorFilter(backgroundColorFilter);
//                mWatchFaceBackgroundPreviewImageView
//                        .getBackground()
//                        .setColorFilter(backgroundColorFilter);

            } else {
                // Inform user that they need to disable background image for color to work.
                CharSequence text = "Selected image overrides background color.";
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(mContext, text, duration);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }

            // Updates highlight color (just second arm).
//            String highlightSharedPrefString = mContext.getString(R.string.saved_marker_color);
            int currentHighlightColor = Color.RED;
//            int currentHighlightColor = mSharedPref.getInt(highlightSharedPrefString, Color.RED);

            PorterDuffColorFilter highlightColorFilter =
                    new PorterDuffColorFilter(currentHighlightColor, PorterDuff.Mode.SRC_ATOP);

            mWatchFaceHighlightPreviewView.getBackground().setColorFilter(highlightColorFilter);
        }

        // Verifies the watch face supports the complication location, then launches the helper
        // class, so user can choose their complication data provider.
        private void launchComplicationHelperActivity(
                Activity currentActivity, ComplicationHolder complication) {

            mSelectedComplication = complication;
//                    AnalogComplicationWatchFaceService.getComplicationId(complicationLocation);

            mBackgroundComplicationEnabled = false;

            if (mSelectedComplication != null) {
//            if (mSelectedComplicationId >= 0) {

//                int[] supportedTypes = complication.getSupportedComplicationTypes();
//                        AnalogComplicationWatchFaceService.getSupportedComplicationTypes(
//                                complicationLocation);

                ComponentName watchFace =
                        new ComponentName(
                                currentActivity, AnalogComplicationWatchFaceService.class);

                currentActivity.startActivityForResult(
                        ComplicationHelperActivity.createProviderChooserHelperIntent(
                                currentActivity,
                                watchFace,
                                mSelectedComplication.getId(),
                                mSelectedComplication.getSupportedComplicationTypes()),
                        ConfigActivity.COMPLICATION_CONFIG_REQUEST_CODE);

            } else {
                Log.d(TAG, "Complication not supported by watch face.");
            }
        }

        void setDefaultComplicationDrawable(int resourceId) {
            Context context = mWatchFaceArmsAndTicksView.getContext();
            mDefaultComplicationDrawable = context.getDrawable(resourceId);

            for (ComplicationHolder complication : complications) {
                if (complication.isForeground) {
                    complication.imageButton.setImageDrawable(mDefaultComplicationDrawable);
                    complication.background.setVisibility(View.INVISIBLE);
                }
            }

//            mLeftComplication.setImageDrawable(mDefaultComplicationDrawable);
//            mLeftComplicationBackground.setVisibility(View.INVISIBLE);
//
//            mBottomComplication.setImageDrawable(mDefaultComplicationDrawable);
//            mBottomComplicationBackground.setVisibility(View.INVISIBLE);
//
//            mRightComplication.setImageDrawable(mDefaultComplicationDrawable);
//            mRightComplicationBackground.setVisibility(View.INVISIBLE);
        }

        void updateComplicationViews(
                ComplicationHolder complication, ComplicationProviderInfo complicationProviderInfo) {
//                int watchFaceComplicationId, ComplicationProviderInfo complicationProviderInfo) {
            Log.d(TAG, "updateComplicationViews(): id: " + complication);
            Log.d(TAG, "\tinfo: " + complicationProviderInfo);

            if (!complication.isForeground) {
//            if (watchFaceComplicationId == mBackgroundComplicationId) {
                if (complicationProviderInfo != null) {
                    mBackgroundComplicationEnabled = true;

                    // Since we can't get the background complication image outside of the
                    // watch face, we set the icon for that provider instead with a gray background.
                    PorterDuffColorFilter backgroundColorFilter =
                            new PorterDuffColorFilter(Color.GRAY, PorterDuff.Mode.SRC_ATOP);

                    complication.background.getBackground()
                            .setColorFilter(backgroundColorFilter);
//                    mWatchFaceBackgroundPreviewImageView
//                            .getBackground()
//                            .setColorFilter(backgroundColorFilter);
                    complication.background.setImageIcon(
                            complicationProviderInfo.providerIcon);
//                    mWatchFaceBackgroundPreviewImageView.setImageIcon(
//                            complicationProviderInfo.providerIcon);

                } else {
                    mBackgroundComplicationEnabled = false;

                    // Clears icon for background if it was present before.
                    backgroundComplication.background.setImageResource(
//                    mWatchFaceBackgroundPreviewImageView.setImageResource(
                            android.R.color.transparent);
                    String backgroundSharedPrefString =
                            mContext.getString(R.string.saved_background_color);
                    int currentBackgroundColor =
                            mSharedPref.getInt(backgroundSharedPrefString, Color.BLACK);

                    PorterDuffColorFilter backgroundColorFilter =
                            new PorterDuffColorFilter(
                                    currentBackgroundColor, PorterDuff.Mode.SRC_ATOP);

                    backgroundComplication.background
//                    mWatchFaceBackgroundPreviewImageView
                            .getBackground()
                            .setColorFilter(backgroundColorFilter);
                }
            } else {
                updateComplicationView(complicationProviderInfo, complication.imageButton,
                        complication.background);
            }
//            } else if (watchFaceComplicationId == mLeftComplicationId) {
//                updateComplicationView(complicationProviderInfo, mLeftComplication,
//                    mLeftComplicationBackground);
//            } else if (watchFaceComplicationId == mBottomComplicationId) {
//                updateComplicationView(complicationProviderInfo, mBottomComplication,
//                    mBottomComplicationBackground);
//            } else if (watchFaceComplicationId == mRightComplicationId) {
//                updateComplicationView(complicationProviderInfo, mRightComplication,
//                    mRightComplicationBackground);
//            }
        }

        private void updateComplicationView(ComplicationProviderInfo complicationProviderInfo,
                                            ImageButton button, ImageView background) {
            if (complicationProviderInfo != null) {
                button.setImageIcon(complicationProviderInfo.providerIcon);
                button.setContentDescription(
                        mContext.getString(R.string.edit_complication,
                                complicationProviderInfo.appName + " " +
                                        complicationProviderInfo.providerName));
                background.setVisibility(View.VISIBLE);
            } else {
                button.setImageDrawable(mDefaultComplicationDrawable);
                button.setContentDescription(mContext.getString(R.string.add_complication));
                background.setVisibility(View.INVISIBLE);
            }
        }

        void initializesColorsAndComplications() {

            // Initializes highlight color (just second arm and part of complications).
//            String highlightSharedPrefString = mContext.getString(R.string.saved_marker_color);
            int currentHighlightColor = Color.RED;
//            int currentHighlightColor = mSharedPref.getInt(highlightSharedPrefString, Color.RED);

            PorterDuffColorFilter highlightColorFilter =
                    new PorterDuffColorFilter(currentHighlightColor, PorterDuff.Mode.SRC_ATOP);

            mWatchFaceHighlightPreviewView.getBackground().setColorFilter(highlightColorFilter);

            // Initializes background color to gray (updates to color or complication icon based
            // on whether the background complication is live or not.
            PorterDuffColorFilter backgroundColorFilter =
                    new PorterDuffColorFilter(Color.GRAY, PorterDuff.Mode.SRC_ATOP);

            backgroundComplication.background
                    //mWatchFaceBackgroundPreviewImageView
                    .getBackground()
                    .setColorFilter(backgroundColorFilter);

            //final int[] complicationIds = AnalogComplicationWatchFaceService.getComplicationIds();
            int[] complicationIds = new int[complications.size()];
            int i = 0;
            for (ComplicationHolder complication : complications) {
                complicationIds[i] = complication.getId();
                i++;
            }

            mProviderInfoRetriever.retrieveProviderInfo(
                    new OnProviderInfoReceivedCallback() {
                        @Override
                        public void onProviderInfoReceived(
                                int watchFaceComplicationId,
                                @Nullable ComplicationProviderInfo complicationProviderInfo) {

                            Log.d(TAG, "onProviderInfoReceived: " + complicationProviderInfo);

                            for (ComplicationHolder complication : complications) {
                                if (watchFaceComplicationId == complication.getId()) {
                                    updateComplicationViews(
                                            complication, complicationProviderInfo);
                                }
                            }
                        }
                    },
                    mWatchFaceComponentName,
                    complicationIds);
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
     * Displays icon to indicate there are more options below the fold.
     */
    public class MoreOptionsViewHolder extends RecyclerView.ViewHolder {

        private ImageView mMoreOptionsImageView;

        MoreOptionsViewHolder(View view) {
            super(view);
            mMoreOptionsImageView = view.findViewById(R.id.more_options_image_view);
        }

        void setIcon(int resourceId) {
            Context context = mMoreOptionsImageView.getContext();
            mMoreOptionsImageView.setImageDrawable(context.getDrawable(resourceId));
        }
    }

    /**
     * Displays color options for the an item on the watch face. These could include marker color,
     * background color, etc.
     */
    public class ColorPickerViewHolder
            extends RecyclerView.ViewHolder implements OnClickListener, Ticklish {

        private Button mButton;
        private Class<ColorSelectionActivity> mLaunchActivity;
        private WatchFacePreset.ColorType mColorType;
        private Drawable mColorSwatchDrawable = new Drawable() {
            @Override
            public void draw(@NonNull Canvas canvas) {
                if (mColorType == null) return;

                regenerateCurrentWatchFacePreset(mButton.getContext());
                @ColorInt int color = mCurrentPaintBox.getColor(mColorType);

                // Draw a circle that's 20px from right, top and left borders.
                float radius = (canvas.getClipBounds().height() / 2f) - 20f;
                Paint p = new Paint();
                p.setColor(color);
                p.setStyle(Paint.Style.FILL);
                p.setAntiAlias(true);
                android.graphics.Rect r = canvas.getClipBounds();
                canvas.drawCircle(r.right - 20f - radius,
                        (r.top + r.bottom) / 2f, radius, p);
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

        public void tickle() {
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
    public class WatchFacePresetPickerViewHolder
            extends RecyclerView.ViewHolder implements OnClickListener, Ticklish {

        private Button mButton;

        private Class<WatchFaceSelectionActivity> mLaunchActivity;
        private WatchFacePickerConfigItem mConfigItem;

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
                launchIntent.putExtra(INTENT_EXTRA_PRESETS, permutations);

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
    public class WatchFacePresetToggleViewHolder extends ToggleViewHolder implements Ticklish {

        WatchFacePresetToggleViewHolder(View view) {
            super(view);
        }

        private WatchFacePresetToggleConfigItem mConfigItem;

        void bind(WatchFacePresetToggleConfigItem configItem) {
            mConfigItem = configItem;

            setName(configItem.getName());
            setIcons(configItem.getIconEnabledResourceId(),
                    configItem.getIconDisabledResourceId());

            tickle();
        }

        @Override
        void setDefaultSwitchValue(Context context) {
            tickle();
        }

        public void tickle() {
            // Regenerate and grab our current permutations. Just in time!
            String[] permutations =
                    mConfigItem.permute(regenerateCurrentWatchFacePreset(mSwitch.getContext()));
            mSwitch.setChecked(mCurrentWatchFacePreset.getString().equals(permutations[1]));
            itemView.invalidate();
        }

        @Override
        public void onClick(View view) {
            // Regenerate and grab our current permutations. Just in time!
            String[] permutations =
                    mConfigItem.permute(regenerateCurrentWatchFacePreset(mSwitch.getContext()));

            Boolean newState = mSwitch.isChecked();
            Context context = view.getContext();
            SharedPreferences.Editor editor = mSharedPref.edit();
            editor.putString(context.getString(R.string.saved_watch_face_preset),
                    newState ? permutations[1] : permutations[0]);
            editor.apply();

            updateIcon(context, newState);

            updatePreviewColors();
        }
    }

    /**
     * Displays switch to indicate whether or not icon appears for unread notifications. User can
     * toggle on/off.
     */
    public class UnreadNotificationViewHolder extends ToggleViewHolder {
        UnreadNotificationViewHolder(View view) {
            super(view);
        }

        @Override
        void setDefaultSwitchValue(Context context) {
            String sharedPreferenceString = context.getString(R.string.saved_settings);

            Settings settings = new Settings();
            settings.setString(mSharedPref.getString(sharedPreferenceString, null));
            boolean currentState = settings.isShowUnreadNotifications();

            updateIcon(context, currentState);
        }

        @Override
        public void onClick(View view) {
            Context context = view.getContext();
            String sharedPreferenceString = context.getString(R.string.saved_settings);

            // Since user clicked on a switch, new state should be opposite of current state.
            Settings settings = new Settings();
            settings.setString(mSharedPref.getString(sharedPreferenceString, null));
            boolean newState = settings.toggleShowUnreadNotifications();

            SharedPreferences.Editor editor = mSharedPref.edit();
            editor.putString(sharedPreferenceString, settings.getString());
            editor.apply();

            updateIcon(context, newState);
        }
    }

    /**
     * Displays button to trigger background image complication selector.
     */
    public class BackgroundComplicationViewHolder extends RecyclerView.ViewHolder
            implements OnClickListener {

        private Button mBackgroundComplicationButton;

        BackgroundComplicationViewHolder(View view) {
            super(view);

            mBackgroundComplicationButton =
                    view.findViewById(R.id.background_complication_button);
            view.setOnClickListener(this);
        }

        public void setName(String name) {
            mBackgroundComplicationButton.setText(name);
        }

        void setIcon(int resourceId) {
            Context context = mBackgroundComplicationButton.getContext();
            mBackgroundComplicationButton.setCompoundDrawablesWithIntrinsicBounds(
                    context.getDrawable(resourceId), null, null, null);
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            Log.d(TAG, "Background Complication onClick() position: " + position);

            Activity currentActivity = (Activity) view.getContext();

            mSelectedComplication = backgroundComplication;
//            mSelectedComplicationId =
//                    AnalogComplicationWatchFaceService.getComplicationId(
//                            ComplicationLocation.BACKGROUND);

            if (mSelectedComplication != null) {
//            if (mSelectedComplicationId >= 0) {

//                int[] supportedTypes =
//                        AnalogComplicationWatchFaceService.getSupportedComplicationTypes(
//                                ComplicationLocation.BACKGROUND);

                ComponentName watchFace =
                        new ComponentName(
                                currentActivity, AnalogComplicationWatchFaceService.class);

                currentActivity.startActivityForResult(
                        ComplicationHelperActivity.createProviderChooserHelperIntent(
                                currentActivity,
                                watchFace,
                                mSelectedComplication.getId(),
                                mSelectedComplication.getSupportedComplicationTypes()),
                        ConfigActivity.COMPLICATION_CONFIG_REQUEST_CODE);

            } else {
                Log.d(TAG, "Complication not supported by watch face.");
            }
        }
    }

    /**
     * Displays switch to indicate whether or not night vision is toggled on/off.
     */
    public class NightVisionViewHolder extends ToggleViewHolder {
        final private int MY_PERMISSION_ACCESS_COURSE_LOCATION = 1;

        NightVisionViewHolder(View view) {
            super(view);
        }

        @Override
        void setDefaultSwitchValue(Context context) {
            String sharedPreferenceString = context.getString(R.string.saved_settings);
            Settings settings = new Settings();
            settings.setString(mSharedPref.getString(sharedPreferenceString, null));
            boolean currentState = settings.toggleEnableNightVisionMode();

            updateIcon(context, currentState);
        }

        @Override
        public void onClick(View view) {
            Context context = view.getContext();
            String sharedPreferenceString = context.getString(R.string.saved_settings);

            // Since user clicked on a switch, new state should be opposite of current state.
            Settings settings = new Settings();
            settings.setString(mSharedPref.getString(sharedPreferenceString, null));
            boolean newState = settings.toggleEnableNightVisionMode();

            if (newState && context.checkSelfPermission(
                    android.Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                Activity a = (Activity) context;
                a.requestPermissions(new String[]{
                                android.Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_PERMISSION_ACCESS_COURSE_LOCATION);
            }

            SharedPreferences.Editor editor = mSharedPref.edit();
            editor.putString(sharedPreferenceString, settings.toString());
            editor.apply();

            updateIcon(context, newState);
        }
    }

    /**
     * Displays switch to indicate whether or not night vision is toggled on/off.
     */
    public abstract class ToggleViewHolder extends RecyclerView.ViewHolder
            implements OnClickListener {
        Switch mSwitch;
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

            Context context = mSwitch.getContext();

            // Set default to enabled.
            mSwitch.setCompoundDrawablesWithIntrinsicBounds(
                    context.getDrawable(mEnabledIconResourceId), null, null, null);

            if (mSwitch != null) {
                setDefaultSwitchValue(context);
            }
        }

        abstract void setDefaultSwitchValue(Context context);

        void updateIcon(Context context, Boolean currentState) {
            int currentIconResourceId;

            if (currentState) {
                currentIconResourceId = mEnabledIconResourceId;
            } else {
                currentIconResourceId = mDisabledIconResourceId;
            }

            mSwitch.setChecked(currentState);
            mSwitch.setCompoundDrawablesWithIntrinsicBounds(
                    context.getDrawable(currentIconResourceId), null, null, null);
        }

        @Override
        abstract public void onClick(View view);
    }
}
