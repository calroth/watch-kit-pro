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
import android.support.wearable.complications.ComplicationHelperActivity;
import android.support.wearable.complications.ComplicationProviderInfo;
import android.support.wearable.complications.ProviderInfoRetriever;
import android.support.wearable.complications.ProviderInfoRetriever.OnProviderInfoReceivedCallback;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;

import pro.watchkit.wearable.watchface.R;
import pro.watchkit.wearable.watchface.model.ComplicationHolder;
import pro.watchkit.wearable.watchface.model.ConfigData;
import pro.watchkit.wearable.watchface.model.ConfigData.ColorPickerConfigItem;
import pro.watchkit.wearable.watchface.model.ConfigData.ConfigActivityConfigItem;
import pro.watchkit.wearable.watchface.model.ConfigData.ConfigItemType;
import pro.watchkit.wearable.watchface.model.ConfigData.NightVisionConfigItem;
import pro.watchkit.wearable.watchface.model.ConfigData.UnreadNotificationConfigItem;
import pro.watchkit.wearable.watchface.model.ConfigData.WatchFaceDrawableConfigItem;
import pro.watchkit.wearable.watchface.model.ConfigData.WatchFacePickerConfigItem;
import pro.watchkit.wearable.watchface.model.ConfigData.WatchFacePresetToggleConfigItem;
import pro.watchkit.wearable.watchface.model.PaintBox;
import pro.watchkit.wearable.watchface.model.Settings;
import pro.watchkit.wearable.watchface.model.WatchFacePreset;
import pro.watchkit.wearable.watchface.model.WatchFaceState;
import pro.watchkit.wearable.watchface.watchface.AnalogComplicationWatchFaceService;
import pro.watchkit.wearable.watchface.watchface.WatchFaceGlobalDrawable;

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

    public static final int TYPE_COLOR_PICKER_CONFIG = 2;
    public static final int TYPE_UNREAD_NOTIFICATION_CONFIG = 3;
    public static final int TYPE_NIGHT_VISION_CONFIG = 5;
    public static final int TYPE_WATCH_FACE_DRAWABLE_CONFIG = 6;
    public static final int TYPE_WATCH_FACE_PRESET_PICKER_CONFIG = 7;
    public static final int TYPE_WATCH_FACE_PRESET_TOGGLE_CONFIG = 8;
    public static final int TYPE_CONFIG_ACTIVITY_CONFIG = 9;
    private static final String TAG = "CompConfigAdapter";
    private SharedPreferences mSharedPref;
    // ComponentName associated with watch face service (service that renders watch face). Used
    // to retrieve complication information.
    private ComponentName mWatchFaceComponentName;
    private ArrayList<ConfigItemType> mSettingsDataSet;
    // Selected complication id by user.
    private ComplicationHolder mSelectedComplication;

    // Required to retrieve complication data from watch face for preview.
    private ProviderInfoRetriever mProviderInfoRetriever;

    private List<Ticklish> mTicklish = new ArrayList<>();
    private List<ComplicationProviderInfoListener> mComplicationProviderInfoListeners =
            new ArrayList<>();

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

            case TYPE_WATCH_FACE_DRAWABLE_CONFIG: {
                viewHolder =
                        new WatchFaceDrawableViewHolder(
                                LayoutInflater.from(parent.getContext())
                                        .inflate(R.layout.watch_face_preset_config_list_item, parent, false));
                mTicklish.add((Ticklish) viewHolder);
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

                int defaultComplicationResourceId =
                        watchFaceDrawableConfigItem.getDefaultComplicationResourceId();
                watchFaceDrawableViewHolder.setDefaultComplicationDrawable(
                        defaultComplicationResourceId);

                watchFaceDrawableViewHolder.bind(watchFaceDrawableConfigItem);
                mComplicationProviderInfoListeners.add(watchFaceDrawableViewHolder);
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

    void updatePreviewColors() {
        // Update our Ticklish objects.
        mTicklish.forEach(Ticklish::tickle);
    }

    /**
     * An object is ticklish if it can be tickled. When tickled, an object invalidates and
     * redraws itself.
     */
    private interface Ticklish {
        /**
         * Invalidate this object.
         */
        void tickle();
    }

    /**
     * A object implementing ComplicationProviderInfoListener is interested in receiving a
     * ComplicationProviderInfo when a new or updated one is available.
     */
    private interface ComplicationProviderInfoListener {
        /**
         * Invalidate this object.
         */
        void onComplicationProviderInfo(
                @NonNull ComplicationHolder complication,
                ComplicationProviderInfo complicationProviderInfo);
    }


    /**
     * TODO: we really have to see if we can generalise it, it's the same code as in
     * WatchFaceSelectionRecyclerViewAdapter
     */
    public class WatchFaceDrawableViewHolder extends RecyclerView.ViewHolder
            implements ComplicationProviderInfoListener, Ticklish {

        private ImageView mImageView;

        private WatchFaceGlobalDrawable mWatchFaceGlobalDrawable;

        private @DrawableRes
        int mDefaultComplicationDrawableId;

        private WatchFaceDrawableConfigItem mConfigItem;

        private Context mContext;

        private float mLastTouchX = -1f, mLastTouchY = -1f;
        private Activity mCurrentActivity;

        void setDefaultComplicationDrawable(@DrawableRes int resourceId) {
            mDefaultComplicationDrawableId = resourceId;
        }

        WatchFaceDrawableViewHolder(final View view) {
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

        public void tickle() {
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

                    tickle();
                } else {
                    Drawable drawable = mContext.getDrawable(mDefaultComplicationDrawableId);
                    if (drawable != null) {
                        complication.setProviderIconDrawable(drawable, false);
                        tickle();
                    }
                }
            }
        }

        void bind(WatchFaceDrawableConfigItem configItem) {
            mConfigItem = configItem;

            // TODO: the below code is duplicated approx. 1 billion times, refactor it already!
            String sharedPreferenceString = mContext.getString(R.string.saved_settings);
            Settings settings = new Settings();
            settings.setString(mSharedPref.getString(sharedPreferenceString, null));
            String settingsString = mSharedPref.getString(sharedPreferenceString, null);
            String watchFacePresetString = regenerateCurrentWatchFacePreset(mContext).getString();

            WatchFaceState w = mWatchFaceGlobalDrawable.getWatchFaceState();
            if (watchFacePresetString != null) {
                w.getWatchFacePreset().setString(watchFacePresetString);
            }
            if (settingsString != null) {
                w.getSettings().setString(settingsString);
            }
            w.setNotifications(0, 0);
            w.setAmbient(false);
            int[] complicationIds = w.initializeComplications(mContext, this::tickle);

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
                    new OnProviderInfoReceivedCallback() {
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
            private Paint mCirclePaint;

            @Override
            public void draw(@NonNull Canvas canvas) {
                if (mColorType == null) return;

                regenerateCurrentWatchFacePreset(mButton.getContext());
                @ColorInt int color = mCurrentPaintBox.getColor(mColorType);

                // Draw a circle that's 20px from right, top and left borders.
                float radius = (canvas.getClipBounds().height() / 2f) - 20f;
                if (mCirclePaint == null) {
                    // Initialise on first use.
                    mCirclePaint = new Paint();
                    mCirclePaint.setColor(color);
                    mCirclePaint.setStyle(Paint.Style.FILL);
                    mCirclePaint.setAntiAlias(true);
                }
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
            boolean currentState = settings.toggleNightVisionModeEnabled();

            updateIcon(context, currentState);
        }

        @Override
        public void onClick(View view) {
            Context context = view.getContext();
            String sharedPreferenceString = context.getString(R.string.saved_settings);

            // Since user clicked on a switch, new state should be opposite of current state.
            Settings settings = new Settings();
            settings.setString(mSharedPref.getString(sharedPreferenceString, null));
            boolean newState = settings.toggleNightVisionModeEnabled();

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
