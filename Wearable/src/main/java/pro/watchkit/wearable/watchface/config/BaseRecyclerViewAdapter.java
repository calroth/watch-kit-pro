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
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.support.wearable.complications.ComplicationHelperActivity;
import android.support.wearable.complications.ComplicationProviderInfo;
import android.support.wearable.complications.ProviderInfoRetriever;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
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

import java.util.Comparator;

import pro.watchkit.wearable.watchface.R;
import pro.watchkit.wearable.watchface.model.ComplicationHolder;
import pro.watchkit.wearable.watchface.model.ConfigData;
import pro.watchkit.wearable.watchface.model.PaintBox;
import pro.watchkit.wearable.watchface.model.WatchFaceState;
import pro.watchkit.wearable.watchface.watchface.AnalogComplicationWatchFaceService;
import pro.watchkit.wearable.watchface.watchface.WatchFaceGlobalDrawable;

import static pro.watchkit.wearable.watchface.config.ColorSelectionActivity.INTENT_EXTRA_COLOR;
import static pro.watchkit.wearable.watchface.config.ConfigActivity.CONFIG_DATA;
import static pro.watchkit.wearable.watchface.config.WatchFaceSelectionActivity.INTENT_EXTRA_FLAGS;
import static pro.watchkit.wearable.watchface.config.WatchFaceSelectionActivity.INTENT_EXTRA_STATES;

abstract class BaseRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    /**
     * The current user-selected WatchFaceState with what's currently stored in preferences
     */
    @NonNull
    final WatchFaceState mCurrentWatchFaceState;
    SharedPreferences mSharedPref;
    private static final String TAG = BaseRecyclerViewAdapter.class.getSimpleName();
    String saved_watch_face_preset_1, saved_settings_1, saved_watch_face_state;

    BaseRecyclerViewAdapter(@NonNull Context context) {
        super();
        mCurrentWatchFaceState = new WatchFaceState(context);

        mSharedPref = context.getSharedPreferences(
                context.getString(R.string.analog_complication_preference_file_key),
                Context.MODE_PRIVATE);

        saved_watch_face_preset_1 = context.getString(R.string.saved_watch_face_preset_1);
        saved_settings_1 = context.getString(R.string.saved_settings_1);
        saved_watch_face_state = context.getString(R.string.saved_watch_face_state);
    }

    void onWatchFaceStateChanged() {
    }

    /**
     * When the user selects a complication to launch the choose-a-complication-provider Activity,
     * this object holds the complication that we'll assign the provider to.
     */
    @Nullable
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
     * A object implementing WatchFaceStateListener is interested in receiving a notification
     * every time we change the WatchFaceState (due to setting new options, etc.)
     * On notification, an object invalidates and redraws itself.
     */
    interface WatchFaceStateListener {
        /**
         * Invalidate this object.
         */
        void onWatchFaceStateChanged();
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
    public class WatchFacePresetSelectionViewHolder extends WatchFaceDrawableViewHolder
            implements View.OnClickListener {

        WatchFacePresetSelectionViewHolder(@NonNull final View view) {
            super(view);
            view.setOnClickListener(this);
            setWatchFaceGlobalDrawableFlags(WatchFaceGlobalDrawable.PART_BACKGROUND |
                    WatchFaceGlobalDrawable.PART_TICKS |
                    WatchFaceGlobalDrawable.PART_HANDS);
        }

        @Override
        public void onClick(@NonNull View view) {
            String watchFaceStateString =
                    mWatchFaceGlobalDrawable.getWatchFaceState().getString();

            Activity activity = (Activity) view.getContext();

            // TODO: the below code is duplicated, just have one SharedPreferences.
            SharedPreferences preferences = activity.getSharedPreferences(
                    activity.getString(R.string.analog_complication_preference_file_key),
                    Context.MODE_PRIVATE);

            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(
                    activity.getString(R.string.saved_watch_face_state), watchFaceStateString);
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

    class WatchFaceDrawableViewHolder extends RecyclerView.ViewHolder
            implements WatchFaceStateListener {

        ImageView mImageView;

        WatchFaceGlobalDrawable mWatchFaceGlobalDrawable;

        private int mWatchFaceGlobalDrawableFlags =
                WatchFaceGlobalDrawable.PART_BACKGROUND; // Default flags.

        WatchFaceDrawableViewHolder(@NonNull View view) {
            super(view);
            mImageView = view.findViewById(R.id.watch_face_preset);
        }

        void setWatchFaceGlobalDrawableFlags(int WatchFaceGlobalDrawableFlags) {
            mWatchFaceGlobalDrawableFlags = WatchFaceGlobalDrawableFlags;
        }

        void setPreset(@Nullable String watchFaceStateString) {
            if (mWatchFaceGlobalDrawable == null) {
                mWatchFaceGlobalDrawable = new WatchFaceGlobalDrawable(
                        mImageView.getContext(), mWatchFaceGlobalDrawableFlags);
                mImageView.setImageDrawable(mWatchFaceGlobalDrawable);
            }
            WatchFaceState w = mWatchFaceGlobalDrawable.getWatchFaceState();
            if (watchFaceStateString != null) {
                w.setString(watchFaceStateString);
            }
            w.setNotifications(0, 0);
            w.setAmbient(false);

            // Initialise complications, just enough to be able to draw rings.
            w.initializeComplications(mImageView.getContext(), this::onWatchFaceStateChanged);
        }

        public void onWatchFaceStateChanged() {
            setPreset(mCurrentWatchFaceState.getString());
            itemView.invalidate();
        }
    }

    public class ComplicationViewHolder extends WatchFaceDrawableViewHolder
            implements ComplicationProviderInfoListener {

        private @DrawableRes
        int mDefaultComplicationDrawableId;

        private ConfigData.ComplicationConfigItem mConfigItem;

        private Context mContext;

        private float mLastTouchX = -1f, mLastTouchY = -1f;
        private Activity mCurrentActivity;

        ComplicationViewHolder(@NonNull final View view) {
            super(view);
            mContext = view.getContext();
            mCurrentActivity = (Activity) view.getContext();

            setWatchFaceGlobalDrawableFlags(WatchFaceGlobalDrawable.PART_BACKGROUND |
                    WatchFaceGlobalDrawable.PART_RINGS_ALL |
                    WatchFaceGlobalDrawable.PART_COMPLICATIONS);

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

        @Override
        public void onComplicationProviderInfo(
                @NonNull ComplicationHolder complication,
                @Nullable ComplicationProviderInfo complicationProviderInfo) {
            Log.d(TAG, "updateComplicationViews(): id: " + complication);
            Log.d(TAG, "\tinfo: " + complicationProviderInfo);

            if (complication.isForeground) {
                // Update complication view.
                if (complicationProviderInfo != null &&
                        complicationProviderInfo.providerIcon != null) {
                    complicationProviderInfo.providerIcon.setTint(
                            mCurrentWatchFaceState.getColor(
                                    mCurrentWatchFaceState.getComplicationTextStyle()));
                    complication.setProviderIconDrawable(
                            complicationProviderInfo.providerIcon.loadDrawable(mContext),
                            true);
                    // TODO: make that async

                    itemView.invalidate();
                } else {
                    Drawable drawable = mContext.getDrawable(mDefaultComplicationDrawableId);
                    drawable.setTint(mCurrentWatchFaceState.getColor(
                            mCurrentWatchFaceState.getComplicationTextStyle()));
                    if (drawable != null) {
                        complication.setProviderIconDrawable(drawable, false);
                        itemView.invalidate();
                    }
                }
            }
        }

        void setPreset(String watchFaceStateString) {
            super.setPreset(watchFaceStateString);

            // Initialise complications, completely.
            WatchFaceState w = mWatchFaceGlobalDrawable.getWatchFaceState();

            mImageView.setOnClickListener(v -> {
                // Find out which thing got clicked!
                if (mLastTouchX != -1f || mLastTouchY != -1f) {
                    w.getComplications().stream()
                            .filter(c -> c.isForeground)
                            .min(Comparator.comparing(c -> c.distanceFrom(mLastTouchX, mLastTouchY)))
                            .ifPresent(this::launchComplicationHelperActivity);
                }
            });

            mProviderInfoRetriever.retrieveProviderInfo(
                    new ProviderInfoRetriever.OnProviderInfoReceivedCallback() {
                        @Override
                        public void onProviderInfoReceived(
                                int id,
                                @Nullable ComplicationProviderInfo providerInfo) {
                            if (w.getComplicationWithId(id) != null) {
                                onComplicationProviderInfo(
                                        w.getComplicationWithId(id), providerInfo);
                            }
                        }
                    },
                    mWatchFaceComponentName,
                    w.getComplicationIds());
        }

        void bind(ConfigData.ComplicationConfigItem configItem) {
            mConfigItem = configItem;

            // Set the preset based on current settings.
            setPreset(mCurrentWatchFaceState.getString());
        }

        // Verifies the watch face supports the complication location, then launches the helper
        // class, so user can choose their complication data provider.
        private void launchComplicationHelperActivity(ComplicationHolder complication) {
            mSelectedComplication = complication;

            ComponentName watchFace =
                    new ComponentName(mCurrentActivity, AnalogComplicationWatchFaceService.class);

            mCurrentActivity.startActivityForResult(
                    ComplicationHelperActivity.createProviderChooserHelperIntent(
                            mCurrentActivity,
                            watchFace,
                            mSelectedComplication.getId(),
                            mSelectedComplication.getSupportedComplicationTypes()),
                    ConfigActivity.COMPLICATION_CONFIG_REQUEST_CODE);
        }
    }

    /**
     * Displays color options for the an item on the watch face. These could include marker color,
     * background color, etc.
     */
    public class ColorPickerViewHolder
            extends RecyclerView.ViewHolder implements View.OnClickListener, WatchFaceStateListener {

        private Button mButton;
        private Class<ColorSelectionActivity> mLaunchActivity;
        private PaintBox.ColorType mColorType;
        @NonNull
        private Drawable mColorSwatchDrawable = new Drawable() {
            private Paint mCirclePaint;

            @Override
            public void draw(@NonNull Canvas canvas) {
                if (mColorType == null) return;

                @ColorInt int color = mCurrentWatchFaceState.getColor(mColorType);

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

        ColorPickerViewHolder(@NonNull View view) {
            super(view);

            mButton = view.findViewById(R.id.color_picker_button);
            view.setOnClickListener(this);
        }

        void bind(@NonNull ConfigData.ColorPickerConfigItem configItem) {
            mButton.setText(configItem.getName());
            mButton.setCompoundDrawablesWithIntrinsicBounds(
                    mButton.getContext().getDrawable(configItem.getIconResourceId()),
                    null, mColorSwatchDrawable, null);
            mColorType = configItem.getType();
            mLaunchActivity = configItem.getActivityToChoosePreference();
        }

        public void onWatchFaceStateChanged() {
            itemView.invalidate();
        }

        @Override
        public void onClick(@NonNull View view) {
            if (mLaunchActivity != null) {
                Intent launchIntent = new Intent(view.getContext(), mLaunchActivity);

                // Pass shared preference name to save color value to.
//                launchIntent.putExtra(INTENT_EXTRA_STATES, mSharedPrefResourceString);
                launchIntent.putExtra(INTENT_EXTRA_COLOR, mColorType.name());

                Activity activity = (Activity) view.getContext();
                activity.startActivityForResult(
                        launchIntent,
                        ConfigActivity.UPDATED_CONFIG_REDRAW_PLEASE_REQUEST_CODE);
            }
        }
    }

    public class ConfigActivityViewHolder
            extends RecyclerView.ViewHolder implements View.OnClickListener {

        private Button mButton;
        private Class<? extends ConfigData> mConfigDataClass;
        private Class<ConfigActivity> mLaunchActivity;

        ConfigActivityViewHolder(@NonNull View view) {
            super(view);

            mButton = view.findViewById(R.id.color_picker_button);
            view.setOnClickListener(this);
        }

        void bind(@NonNull ConfigData.ConfigActivityConfigItem configItem) {
            mButton.setText(configItem.getName());
            mButton.setCompoundDrawablesWithIntrinsicBounds(
                    mButton.getContext().getDrawable(configItem.getIconResourceId()),
                    null, null, null);
            mConfigDataClass = configItem.getConfigDataClass();
            mLaunchActivity = configItem.getActivityToChoosePreference();
        }

        @Override
        public void onClick(@NonNull View view) {
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
    public class PickerViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener, WatchFaceStateListener {

        private Button mButton;

        private Class<WatchFaceSelectionActivity> mLaunchActivity;
        private ConfigData.PickerConfigItem mConfigItem;
        private int mFlags;

        private int mVisibleLayoutHeight, mVisibleLayoutWidth;

        PickerViewHolder(@NonNull View view) {
            super(view);

            mButton = view.findViewById(R.id.watch_face_preset_picker_button);
            view.setOnClickListener(this);

            mVisibleLayoutHeight = itemView.getLayoutParams().height;
            mVisibleLayoutWidth = itemView.getLayoutParams().width;
        }

        void bind(@NonNull ConfigData.PickerConfigItem configItem) {
            mConfigItem = configItem;
            mLaunchActivity = configItem.getActivityToChoosePreference();
            mFlags = configItem.getWatchFaceGlobalDrawableFlags();

            setTextAndVisibility();
        }

        private void setTextAndVisibility() {
            mButton.setText(mConfigItem.getName(
                    mCurrentWatchFaceState, mButton.getContext()));

            ViewGroup.LayoutParams param = itemView.getLayoutParams();
            if (mConfigItem.isVisible(mCurrentWatchFaceState)) {
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

        public void onWatchFaceStateChanged() {
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
        public void onClick(@NonNull View view) {
            int position = getAdapterPosition();
            Log.d(TAG, "Complication onClick() position: " + position);

            if (mLaunchActivity != null) {
                // Regenerate and grab our current permutations. Just in time!
                String[] permutations =
                        mConfigItem.permute(mCurrentWatchFaceState, mButton.getContext());

                Intent launchIntent = new Intent(view.getContext(), mLaunchActivity);

                // Pass shared preference name to save color value to.
                launchIntent.putExtra(INTENT_EXTRA_STATES, permutations);
                launchIntent.putExtra(INTENT_EXTRA_FLAGS, mFlags);

                Activity activity = (Activity) view.getContext();
                activity.startActivityForResult(
                        launchIntent,
                        ConfigActivity.UPDATED_CONFIG_REDRAW_PLEASE_REQUEST_CODE);
            }
        }
    }

    // This code is left over from Night Vision and we need to put it somewhere.
    // It gets location permissions for our Night Vision check.
//            if (newState && context.checkSelfPermission(
//                    android.Manifest.permission.ACCESS_COARSE_LOCATION)
//                    != PackageManager.PERMISSION_GRANTED) {
//                Activity a = (Activity) context;
//                a.requestPermissions(new String[]{
//                                android.Manifest.permission.ACCESS_COARSE_LOCATION},
//                        MY_PERMISSION_ACCESS_COURSE_LOCATION);
//            }

    /**
     * Displays switch to indicate whether or not a boolean value is toggled on/off.
     */
    public class ToggleViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener, WatchFaceStateListener {
        private Switch mSwitch;
        private int mEnabledIconResourceId;
        private int mDisabledIconResourceId;
        private ConfigData.ToggleConfigItem mConfigItem;
        private int mVisibleLayoutHeight, mVisibleLayoutWidth;

        ToggleViewHolder(@NonNull View view) {
            super(view);

            mSwitch = view.findViewById(R.id.config_list_toggle);
            view.setOnClickListener(this);

            mVisibleLayoutHeight = itemView.getLayoutParams().height;
            mVisibleLayoutWidth = itemView.getLayoutParams().width;
        }

        void bind(@NonNull ConfigData.ToggleConfigItem configItem) {
            mConfigItem = configItem;

            setName(configItem.getName());
            setIcons(configItem.getIconEnabledResourceId(),
                    configItem.getIconDisabledResourceId());

            setDefaultSwitchValue();
        }

        public void setName(String name) {
            mSwitch.setText(name);
        }

        void setIcons(int enabledIconResourceId, int disabledIconResourceId) {
            mEnabledIconResourceId = enabledIconResourceId;
            mDisabledIconResourceId = disabledIconResourceId;

            setDefaultSwitchValue();
        }

        void setDefaultSwitchValue() {
            // Regenerate and grab our current permutations. Just in time!
            String[] permutations =
                    mConfigItem.permute(mCurrentWatchFaceState, mSwitch.getContext());
            setChecked(mCurrentWatchFaceState.getString().equals(permutations[1]));

            // Set visibility.
            ViewGroup.LayoutParams param = itemView.getLayoutParams();
            if (mConfigItem.isVisible(mCurrentWatchFaceState)) {
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
        public void onWatchFaceStateChanged() {
            setDefaultSwitchValue();
        }

        @Override
        public void onClick(View view) {
            // Regenerate and grab our current permutations. Just in time!
            String[] permutations =
                    mConfigItem.permute(mCurrentWatchFaceState, mSwitch.getContext());

            SharedPreferences.Editor editor = mSharedPref.edit();
            editor.putString(mSwitch.getContext().getString(R.string.saved_watch_face_state),
                    isChecked() ? permutations[1] : permutations[0]);
            editor.apply();

            BaseRecyclerViewAdapter.this.onWatchFaceStateChanged();
        }
    }
}
