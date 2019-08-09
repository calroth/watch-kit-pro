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
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.wearable.complications.ComplicationHelperActivity;
import android.support.wearable.complications.ComplicationProviderInfo;
import android.support.wearable.complications.ProviderInfoRetriever;
import android.text.Html;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Comparator;
import java.util.concurrent.Executors;

import pro.watchkit.wearable.watchface.R;
import pro.watchkit.wearable.watchface.model.ComplicationHolder;
import pro.watchkit.wearable.watchface.model.ConfigData;
import pro.watchkit.wearable.watchface.model.PaintBox;
import pro.watchkit.wearable.watchface.model.WatchFaceState;
import pro.watchkit.wearable.watchface.util.SharedPref;
import pro.watchkit.wearable.watchface.util.Toaster;
import pro.watchkit.wearable.watchface.watchface.ProWatchFaceService;
import pro.watchkit.wearable.watchface.watchface.WatchFaceGlobalDrawable;

import static pro.watchkit.wearable.watchface.config.ColorSelectionActivity.INTENT_EXTRA_COLOR;
import static pro.watchkit.wearable.watchface.config.ConfigActivity.CONFIG_DATA;
import static pro.watchkit.wearable.watchface.config.WatchFaceSelectionActivity.INTENT_EXTRA_EXTRA_NAMES;
import static pro.watchkit.wearable.watchface.config.WatchFaceSelectionActivity.INTENT_EXTRA_FLAGS;
import static pro.watchkit.wearable.watchface.config.WatchFaceSelectionActivity.INTENT_EXTRA_LABEL;
import static pro.watchkit.wearable.watchface.config.WatchFaceSelectionActivity.INTENT_EXTRA_SLOT;
import static pro.watchkit.wearable.watchface.config.WatchFaceSelectionActivity.INTENT_EXTRA_STATES;

abstract class BaseRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    /**
     * The current user-selected WatchFaceState with what's currently stored in preferences
     */
    @NonNull
    private final WatchFaceState mCurrentWatchFaceState;
    @NonNull
    private final SharedPref mSharedPref;
    private static final String TAG = BaseRecyclerViewAdapter.class.getSimpleName();
    @StringRes
    private final int mTitleLabel;

    /**
     * The object that retrieves complication data for us to preview our complications with.
     */
    private ProviderInfoRetriever mProviderInfoRetriever;
    /**
     * The ComponentName of our WatchFaceService. We use this to find what complications have been
     * set for this WatchFaceService. It's different for slot A, B, C etc. as we allow the user to
     * have different complication setups per slot!
     */
    private ComponentName mWatchFaceComponentName;

    void onWatchFaceStateChanged() {
    }

    /**
     * When the user selects a complication to launch the choose-a-complication-provider Activity,
     * this object holds the complication that we'll assign the provider to.
     */
    @Nullable
    ComplicationHolder mSelectedComplication;

    BaseRecyclerViewAdapter(@NonNull Context context, @NonNull Class watchFaceServiceClass) {
        super();
        mCurrentWatchFaceState = new WatchFaceState(context);

        mSharedPref = new SharedPref(context, watchFaceServiceClass);

        // Initialization of code to retrieve active complication data for the watch face.
        mWatchFaceComponentName = new ComponentName(context, watchFaceServiceClass);
        mProviderInfoRetriever =
                new ProviderInfoRetriever(context, Executors.newCachedThreadPool());
        mProviderInfoRetriever.init();

        // Grab our title label based on the class (i.e. the watch face slot).
        if (watchFaceServiceClass.equals(ProWatchFaceService.B.class)) {
            mTitleLabel = R.string.watch_face_service_label_b;
        } else if (watchFaceServiceClass.equals(ProWatchFaceService.C.class)) {
            mTitleLabel = R.string.watch_face_service_label_c;
        } else if (watchFaceServiceClass.equals(ProWatchFaceService.D.class)) {
            mTitleLabel = R.string.watch_face_service_label_d;
        } else {
            mTitleLabel = R.string.watch_face_service_label_a;
        }
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        // Release our mProviderInfoRetriever to clean up and prevent object leaks.
        mProviderInfoRetriever.release();
        super.onDetachedFromRecyclerView(recyclerView);
    }

    /**
     * Regenerates the current WatchFaceState with what's currently stored in preferences.
     * Call this if you suspect that preferences are changed, before accessing
     * mCurrentWatchFaceState.
     */
    void regenerateCurrentWatchFaceState() {
        mCurrentWatchFaceState.setString(mSharedPref.getWatchFaceStateString());
    }

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

            mSharedPref.putWatchFaceStateString(watchFaceStateString);

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
            implements WatchFaceStateListener, ComplicationProviderInfoListener {

        ImageView mImageView;

        WatchFaceGlobalDrawable mWatchFaceGlobalDrawable;

        @DrawableRes
        private int mDefaultComplicationDrawableId = -1;

        private int mWatchFaceGlobalDrawableFlags =
                WatchFaceGlobalDrawable.PART_BACKGROUND; // Default flags.

        /**
         * The color of our complication text. We keep our own private copy as
         * "mWatchFaceState" can change, but this should stay the same from when we
         * called "setPreset".
         */
        @ColorInt
        private int mComplicationTextColor;

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
            mComplicationTextColor = w.getColor(w.getComplicationTextStyle());

            // Initialise complications, just enough to be able to draw rings.
            w.initializeComplications(mImageView.getContext(), this::onWatchFaceStateChanged);
        }

        public void onWatchFaceStateChanged() {
            setPreset(mCurrentWatchFaceState.getString());
            itemView.invalidate();
        }

        void setDefaultComplicationDrawable(@DrawableRes int resourceId) {
            mDefaultComplicationDrawableId = resourceId;
        }

        void retrieveProviderInfo() {
            WatchFaceState w = mWatchFaceGlobalDrawable.getWatchFaceState();
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

        @Override
        public void onComplicationProviderInfo(
                @NonNull ComplicationHolder complication,
                @Nullable ComplicationProviderInfo complicationProviderInfo) {
            if (complication.isForeground) {
                // Update complication view.
                if (complicationProviderInfo != null &&
                        complicationProviderInfo.providerIcon != null) {
                    complicationProviderInfo.providerIcon.setTint(mComplicationTextColor);
                    complication.setProviderIconDrawable(
                            complicationProviderInfo.providerIcon.loadDrawable(
                                    itemView.getContext()),
                            true);
                    // TODO: make that async

                    itemView.invalidate();
                } else if (mDefaultComplicationDrawableId != -1) {
                    Drawable drawable =
                            itemView.getContext().getDrawable(mDefaultComplicationDrawableId);
                    if (drawable != null) {
                        drawable.setTint(mComplicationTextColor);
                        complication.setProviderIconDrawable(drawable, false);
                        itemView.invalidate();
                    }
                }
            }
        }
    }

    class ComplicationViewHolder extends WatchFaceDrawableViewHolder {

        private ConfigData.ComplicationConfigItem mConfigItem;

        private float mLastTouchX = -1f, mLastTouchY = -1f;
        private Activity mCurrentActivity;

        ComplicationViewHolder(@NonNull final View view) {
            super(view);
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

            retrieveProviderInfo();
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

            mCurrentActivity.startActivityForResult(
                    ComplicationHelperActivity.createProviderChooserHelperIntent(
                            mCurrentActivity,
                            mWatchFaceComponentName,
                            mSelectedComplication.getId(),
                            mSelectedComplication.getSupportedComplicationTypes()),
                    ConfigActivity.COMPLICATION_CONFIG_REQUEST_CODE);
        }
    }

    class LabelViewHolder extends RecyclerView.ViewHolder implements WatchFaceStateListener {

        private TextView mLabelTextView;
        private int mVisibleLayoutHeight, mVisibleLayoutWidth;
        private ConfigData.LabelConfigItem mConfigItem;
        private final StringBuilder mStringBuilder = new StringBuilder();

        LabelViewHolder(@NonNull final View view) {
            super(view);
            mLabelTextView = view.findViewById(R.id.label_textView);

            mVisibleLayoutHeight = itemView.getLayoutParams().height;
            mVisibleLayoutWidth = itemView.getLayoutParams().width;
        }

        void bind(ConfigData.LabelConfigItem configItem) {
            mConfigItem = configItem;
            Resources r = itemView.getResources();
            mLabelTextView.setTypeface(null); // Set as default typeface by default!
            if (configItem.getTypeface() != null) {
                // TODO: that code has a warning. A legitimate warning. Address it.
                // Maybe have two TextViews and hide the unused one (or just don't use it).
                Typeface t = mCurrentWatchFaceState.getTypefaceObject(configItem.getTypeface());
                mLabelTextView.setTypeface(t);
                mStringBuilder.setLength(0);
                mStringBuilder.append(r.getString(configItem.getTitleResourceId()));
                mStringBuilder.append("<br>");
                mStringBuilder.append(r.getString(configItem.getLabelResourceId()));
                mLabelTextView.setText(Html.fromHtml(mStringBuilder.toString(),
                        Html.FROM_HTML_MODE_LEGACY));
            } else if (configItem.getWithTitle()) {
                // TODO: that code has a warning. A legitimate warning. Address it.
                // Maybe have two TextViews and hide the unused one (or just don't use it).
                mStringBuilder.setLength(0);
                mStringBuilder.append(r.getString(mTitleLabel));
                mStringBuilder.append("<br>");
                mStringBuilder.append(r.getString(configItem.getLabelResourceId()));
                mLabelTextView.setText(Html.fromHtml(mStringBuilder.toString(),
                        Html.FROM_HTML_MODE_LEGACY));
            } else if (configItem.getTitleResourceId() != -1) {
                mStringBuilder.setLength(0);
                mStringBuilder.append("<b>");
                mStringBuilder.append(r.getString(configItem.getTitleResourceId()));
                mStringBuilder.append("</b> ");
                mStringBuilder.append(r.getString(configItem.getLabelResourceId()));
                mLabelTextView.setText(Html.fromHtml(mStringBuilder.toString(),
                        Html.FROM_HTML_MODE_LEGACY));
            } else {
                mLabelTextView.setText(configItem.getLabelResourceId());
            }

            onWatchFaceStateChanged();
        }

        @Override
        public void onWatchFaceStateChanged() {
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

            mButton = view.findViewById(R.id.config_list_button_widget);
            view.setOnClickListener(this);
        }

        void bind(@NonNull ConfigData.ColorPickerConfigItem configItem) {
            mButton.setText(itemView.getResources().getString(configItem.getNameResourceId()));
            mButton.setCompoundDrawablesWithIntrinsicBounds(
                    itemView.getResources().getDrawable(configItem.getIconResourceId()),
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
                launchIntent.putExtra(INTENT_EXTRA_COLOR, mColorType.name());
                launchIntent.putExtra(INTENT_EXTRA_SLOT, mWatchFaceComponentName.getClassName());

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

            mButton = view.findViewById(R.id.config_list_button_widget);
            view.setOnClickListener(this);
        }

        void bind(@NonNull ConfigData.ConfigActivityConfigItem configItem) {
            mButton.setText(itemView.getResources().getString(configItem.getNameResourceId()));
            mButton.setCompoundDrawablesWithIntrinsicBounds(
                    itemView.getContext().getDrawable(configItem.getIconResourceId()),
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

        private final Button mButton;

        private Class<WatchFaceSelectionActivity> mLaunchActivity;
        private ConfigData.PickerConfigItem mConfigItem;
        private int mFlags;

        private final int mVisibleLayoutHeight, mVisibleLayoutWidth;

        PickerViewHolder(@NonNull View view) {
            super(view);

            mButton = view.findViewById(R.id.config_list_button_widget);
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
                    mCurrentWatchFaceState, itemView.getContext()));
            mButton.setCompoundDrawablesWithIntrinsicBounds(
                    itemView.getContext().getDrawable(mConfigItem.getIconResourceId()),
                    null, null, null);

            if (mConfigItem.isVisible(mCurrentWatchFaceState)) {
                itemView.getLayoutParams().height = mVisibleLayoutHeight;
                itemView.getLayoutParams().width = mVisibleLayoutWidth;
                itemView.setVisibility(View.VISIBLE);
            } else {
                itemView.getLayoutParams().height = 0;
                itemView.getLayoutParams().width = 0;
                itemView.setVisibility(View.GONE);
            }
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
                Toaster.makeText(itemView.getContext(), newText, Toaster.LENGTH_LONG);
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
                        mConfigItem.permute(mCurrentWatchFaceState, itemView.getContext());

                String[] extraNames = new String[permutations.length];
                String currentWatchFaceStateString = mCurrentWatchFaceState.getString();
                for (int i = 0; i < permutations.length; i++) {
                    mCurrentWatchFaceState.setString(permutations[i]);
                    extraNames[i] = mConfigItem.getExtraName(
                            mCurrentWatchFaceState, itemView.getContext());
                }
                mCurrentWatchFaceState.setString(currentWatchFaceStateString);

                Intent launchIntent = new Intent(view.getContext(), mLaunchActivity);

                // Pass shared preference name to save color value to.
                launchIntent.putExtra(INTENT_EXTRA_STATES, permutations);
                launchIntent.putExtra(INTENT_EXTRA_FLAGS, mFlags);
                launchIntent.putExtra(INTENT_EXTRA_SLOT, mWatchFaceComponentName.getClassName());
                launchIntent.putExtra(INTENT_EXTRA_LABEL, mConfigItem.getNameResourceId());
                launchIntent.putExtra(INTENT_EXTRA_EXTRA_NAMES, extraNames);

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
        private final Switch mSwitch;
        private ConfigData.ToggleConfigItem mConfigItem;
        private final int mVisibleLayoutHeight, mVisibleLayoutWidth;
        private final Drawable mSwitchTrackDrawable, mSwitchThumbDrawable;

        ToggleViewHolder(@NonNull View view) {
            super(view);

            mSwitch = view.findViewById(R.id.config_list_toggle);
            mSwitch.setSplitTrack(true);
            view.setOnClickListener(this);

            mSwitchTrackDrawable = mSwitch.getTrackDrawable();
            mSwitchThumbDrawable = mSwitch.getThumbDrawable();

            mVisibleLayoutHeight = itemView.getLayoutParams().height;
            mVisibleLayoutWidth = itemView.getLayoutParams().width;
        }

        void bind(@NonNull ConfigData.ToggleConfigItem configItem) {
            mConfigItem = configItem;

            mSwitch.setText(mSwitch.getResources().getString(configItem.getNameResourceId()));

            setDefaultSwitchValue();
        }

        void setDefaultSwitchValue() {
            // Regenerate and grab our current permutations. Just in time!
            String[] permutations =
                    mConfigItem.permute(mCurrentWatchFaceState, mSwitch.getContext());
            setChecked(mCurrentWatchFaceState.getString().equals(permutations[1]));

            // Set visibility.
            if (mConfigItem.isVisible(mCurrentWatchFaceState)) {
                mSwitch.setTrackDrawable(mSwitchTrackDrawable);
                mSwitch.setThumbDrawable(mSwitchThumbDrawable);
                itemView.getLayoutParams().height = mVisibleLayoutHeight;
                itemView.getLayoutParams().width = mVisibleLayoutWidth;
                itemView.setVisibility(View.VISIBLE);
            } else {
                // For whatever reason, the toggle switch won't truly disappear until we also null
                // out its track and thumb drawables. Sure, OK...
                mSwitch.setTrackDrawable(null);
                mSwitch.setThumbDrawable(null);
                itemView.getLayoutParams().height = 0;
                itemView.getLayoutParams().width = 0;
                itemView.setVisibility(View.GONE);
            }
        }

        boolean isChecked() {
            return mSwitch.isChecked();
        }

        void setChecked(Boolean checked) {
            int currentIconResourceId;

            if (checked) {
                currentIconResourceId = mConfigItem.getIconEnabledResourceId();
            } else {
                currentIconResourceId = mConfigItem.getIconDisabledResourceId();
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

            mSharedPref.putWatchFaceStateString(isChecked() ? permutations[1] : permutations[0]);

            BaseRecyclerViewAdapter.this.onWatchFaceStateChanged();
        }
    }
}
