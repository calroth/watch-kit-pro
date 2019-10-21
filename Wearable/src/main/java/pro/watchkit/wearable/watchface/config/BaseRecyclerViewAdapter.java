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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.wearable.complications.ComplicationData;
import android.support.wearable.complications.ComplicationHelperActivity;
import android.support.wearable.complications.ComplicationProviderInfo;
import android.support.wearable.complications.ComplicationText;
import android.support.wearable.complications.ProviderInfoRetriever;
import android.text.Html;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.ArrayRes;
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
import pro.watchkit.wearable.watchface.model.WatchFaceState;
import pro.watchkit.wearable.watchface.util.SharedPref;
import pro.watchkit.wearable.watchface.util.Toaster;
import pro.watchkit.wearable.watchface.watchface.ProWatchFaceService;
import pro.watchkit.wearable.watchface.watchface.WatchFaceGlobalDrawable;

import static android.support.wearable.complications.ComplicationData.TYPE_NOT_CONFIGURED;
import static android.support.wearable.complications.ComplicationData.TYPE_SHORT_TEXT;
import static pro.watchkit.wearable.watchface.config.ColorSelectionActivity.INTENT_EXTRA_COLOR;
import static pro.watchkit.wearable.watchface.config.ColorSelectionActivity.INTENT_EXTRA_COLOR_LABEL;
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
    @StringRes
    private final int mTitleLabel;
    @NonNull
    private final String mSubActivityIntent;

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

    /**
     * Called when the WatchFaceState has changed. By default it does nothing, but override this
     * to be notified if interested.
     */
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
            mSubActivityIntent = "pro.watchkit.wearable.watchface.CONFIG_WATCH_KIT_PRO_B";
        } else if (watchFaceServiceClass.equals(ProWatchFaceService.C.class)) {
            mTitleLabel = R.string.watch_face_service_label_c;
            mSubActivityIntent = "pro.watchkit.wearable.watchface.CONFIG_WATCH_KIT_PRO_C";
        } else if (watchFaceServiceClass.equals(ProWatchFaceService.D.class)) {
            mTitleLabel = R.string.watch_face_service_label_d;
            mSubActivityIntent = "pro.watchkit.wearable.watchface.CONFIG_WATCH_KIT_PRO_D";
        } else {
            mTitleLabel = R.string.watch_face_service_label_a;
            mSubActivityIntent = "pro.watchkit.wearable.watchface.CONFIG_WATCH_KIT_PRO_A";
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
                // Set layer type to hardware. We promise not to update this any more,
                // so now Android can render this to a texture and leave it there.
                mImageView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            }
            WatchFaceState w = mWatchFaceGlobalDrawable.getWatchFaceState();
            if (watchFaceStateString != null) {
                w.setString(watchFaceStateString);
            }
            w.setNotifications(0, 0);
            w.setAmbient(false);
            mComplicationTextColor = w.getColor(w.getComplicationTextStyle());

            // Initialise complications, just enough to be able to draw rings.
            w.initializeComplications(mImageView.getContext());

            mImageView.setOnApplyWindowInsetsListener((v, insets) -> {
                SharedPref.setIsRoundScreen(insets.isRound());
                // cutoutSize = insets.getSystemWindowInsetBottom();
                return insets;
            });
        }

        void setHighlightedCurrentSelection(@Nullable String watchFaceStateString) {
            // Highlight this if it's the current selection.
            if (WatchFaceState.mostlyEquals(
                    mSharedPref.getWatchFaceStateString(), watchFaceStateString)) {
                itemView.setBackground(itemView.getContext().
                        getDrawable(android.R.drawable.screen_background_dark_transparent));
            } else {
                itemView.setBackground(null);
            }
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
                            ComplicationHolder h = w.getComplicationWithId(id);
                            if (h != null) {
                                onComplicationProviderInfo(h, providerInfo);
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
        private float mLastTouchX = -1f, mLastTouchY = -1f;
        private Activity mCurrentActivity;

        @SuppressLint("ClickableViewAccessibility")
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

        void bind() {
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

    class LabelViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener, WatchFaceStateListener {

        private TextView mLabelTextView;
        private int mVisibleLayoutHeight, mVisibleLayoutWidth;
        private ConfigData.LabelConfigItem mConfigItem;
        private final StringBuilder mStringBuilder = new StringBuilder();

        LabelViewHolder(@NonNull final View view) {
            super(view);
            mLabelTextView = view.findViewById(R.id.config_item_textview_widget);
            view.setOnClickListener(this);

            mVisibleLayoutHeight = itemView.getLayoutParams().height;
            mVisibleLayoutWidth = itemView.getLayoutParams().width;
        }

        void bind(@NonNull ConfigData.LabelConfigItem configItem) {
            mConfigItem = configItem;
            Resources r = itemView.getResources();
            mLabelTextView.setTypeface(null); // Set as default typeface by default!
            if (configItem.getWithTitle()) {
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
            if (mConfigItem == null) {
                return;
            }

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

        @Override
        public void onClick(@NonNull View view) {
            // Temporary code to generate an icon?
            if (mConfigItem.getLabelResourceId() == R.string.config_generate_icon_files) {
                boolean wasRound = SharedPref.isRoundScreen();
                SharedPref.setIsRoundScreen(false); // Temporarily go square for the icon.

                Context context = itemView.getContext();
                SharedPref.mWriteLayersToDiskContext = context;

                // Create our foreground drawable.
                {
                    int flags = WatchFaceGlobalDrawable.PART_BACKGROUND_FULL_CANVAS |
                            WatchFaceGlobalDrawable.PART_TICKS |
                            WatchFaceGlobalDrawable.PART_HANDS |
                            WatchFaceGlobalDrawable.PART_RINGS_ACTIVE;

                    WatchFaceGlobalDrawable drawable =
                            new WatchFaceGlobalDrawable(context, flags);

                    WatchFaceState watchFaceState = drawable.getWatchFaceState();
                    watchFaceState.setString(mCurrentWatchFaceState.getString());
                    watchFaceState.setCurrentTime(1570365309000L); // 2019-10-06T23:35:09.000+1100
                    watchFaceState.setNotifications(0, 0);
                    watchFaceState.setAmbient(false);
                    drawable.setBounds(0, 0, 960, 960);

                    // Initialise complications, just enough to be able to draw rings.
                    watchFaceState.initializeComplications(context);

                    // Quick-and-dirty code to get complication IDs.
                    ProWatchFaceService p;
                    if (mTitleLabel == R.string.watch_face_service_label_a) {
                        p = new ProWatchFaceService.A();
                    } else if (mTitleLabel == R.string.watch_face_service_label_b) {
                        p = new ProWatchFaceService.B();
                    } else if (mTitleLabel == R.string.watch_face_service_label_c) {
                        p = new ProWatchFaceService.C();
                    } else {
                        p = new ProWatchFaceService.D();
                    }

                    // A dummy complication that won't be displayed.
                    ComplicationData.Builder cb =
                            new ComplicationData.Builder(TYPE_SHORT_TEXT);
                    cb.setShortText(ComplicationText.plainText("x"));
                    ComplicationData c = cb.build();

                    // Get our complication IDs and default providers.
                    int[] complicationIds = watchFaceState.getComplicationIds();
                    int[][] defaultComplicationProviders =
                            p.getDefaultSystemComplicationProviders();

                    for (int i = 0; i < complicationIds.length; i++) {
                        // For each active complication, check for a corresponding default complication.
                        // If it's there, set the system default complication provider accordingly.
                        if (i < defaultComplicationProviders.length) {
                            int[] complicationProvider = defaultComplicationProviders[i];
                            if (complicationProvider.length >= 2 &&
                                    complicationProvider[1] != TYPE_NOT_CONFIGURED) {
                                // Activate one ring per active complication.
                                // Activate them with a dummy complication that won't be displayed.
                                watchFaceState.onComplicationDataUpdate(complicationIds[i], c);
                            }
                        }
                    }

                    // Create our canvas...
                    Bitmap bitmap =
                            Bitmap.createBitmap(960, 960, Bitmap.Config.ARGB_8888);
                    Canvas canvas = new Canvas(bitmap);

                    // Draw to the canvas!
                    drawable.draw(canvas);
                }

                SharedPref.setIsRoundScreen(wasRound); // Restore our round screen pref.
                SharedPref.mWriteLayersToDiskContext = null;
                return;
            }
            if (mCurrentWatchFaceState.isDeveloperMode()) {
                // Ignore if we're already in developer mode
                return;
            }
            // If we spam clicks on the config git hash or date, enter developer mode.
            if (mConfigItem.getTitleResourceId() == R.string.config_git_hash ||
                    mConfigItem.getTitleResourceId() == R.string.config_git_date) {
                mDeveloperModeEntry--;
                if (mDeveloperModeEntry > 0 && mDeveloperModeEntry < 5) {
                    Toaster.makeText(view.getContext(), "Entering developer mode in " +
                            mDeveloperModeEntry + " clicks", Toaster.LENGTH_LONG);
                } else if (mDeveloperModeEntry == 0) {
                    Toaster.makeText(view.getContext(), "You are now a developer!",
                            Toaster.LENGTH_LONG);
                }
                if (mDeveloperModeEntry <= 0) {
                    // Enter developer mode!
                    mCurrentWatchFaceState.setDeveloperMode(true);

                    // Store the pref.
                    mSharedPref.putWatchFaceStateString(mCurrentWatchFaceState.getString());

                    // Notify everything else so we can show the UI.
                    BaseRecyclerViewAdapter.this.onWatchFaceStateChanged();
                }
            }
        }
    }

    /**
     * The number of clicks required to enter developer mode. Decrements by one on each click.
     * If 0, we enter developer mode!
     */
    private static int mDeveloperModeEntry = 8;

    /**
     * Displays color options for the an item on the watch face. These could include marker color,
     * background color, etc.
     */
    public class ColorPickerViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener, WatchFaceStateListener {

        private ConfigData.ColorPickerConfigItem mConfigItem;
        private Button mButton;
        private Class<ColorSelectionActivity> mLaunchActivity;
        @NonNull
        private Drawable mColorSwatchDrawable = new Drawable() {
            private Paint mCirclePaint;

            @Override
            public void draw(@NonNull Canvas canvas) {
                if (mConfigItem == null || mConfigItem.getType() == null) return;

                @ColorInt int color = mCurrentWatchFaceState.getColor(mConfigItem.getType());

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
            mConfigItem = configItem;
            setTextAndVisibility();
        }

        private void setTextAndVisibility() {
            String name = itemView.getResources().getString(mConfigItem.getNameResourceId());
            if (mConfigItem == null || mConfigItem.getType() == null) {
                mButton.setText(name);
            } else {
                String colorName = mCurrentWatchFaceState.getColorName(mConfigItem.getType());
                CharSequence text = Html.fromHtml(name + "<br/><small>" +
                        colorName + "</small>", Html.FROM_HTML_MODE_LEGACY);
                mButton.setText(text);
            }
            Drawable left = itemView.getContext().getDrawable(mConfigItem.getIconResourceId());
            if (left != null) {
                left.setTint(mButton.getCurrentTextColor());
                mButton.setCompoundDrawablesWithIntrinsicBounds(
                        left, null, mColorSwatchDrawable, null);
            }
            mLaunchActivity = mConfigItem.getActivityToChoosePreference();
        }

        public void onWatchFaceStateChanged() {
            setTextAndVisibility();
            itemView.invalidate();
        }

        @Override
        public void onClick(@NonNull View view) {
            if (mLaunchActivity != null) {
                Intent launchIntent = new Intent(view.getContext(), mLaunchActivity);

                // Pass shared preference name to save color value to.
                if (mConfigItem != null && mConfigItem.getType() != null) {
                    launchIntent.putExtra(INTENT_EXTRA_COLOR, mConfigItem.getType().name());
                }
                // And the name of the label, for the activity's title header.
                if (mConfigItem != null) {
                    launchIntent.putExtra(INTENT_EXTRA_COLOR_LABEL, mConfigItem.getNameResourceId());
                }
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
            Drawable left = itemView.getContext().getDrawable(configItem.getIconResourceId());
            if (left != null) {
                left.setTint(mButton.getCurrentTextColor());
                mButton.setCompoundDrawablesWithIntrinsicBounds(left, null, null, null);
            }
            mConfigDataClass = configItem.getConfigDataClass();
            mLaunchActivity = configItem.getActivityToChoosePreference();
        }

        @Override
        public void onClick(@NonNull View view) {
            if (mLaunchActivity != null) {
                Intent launchIntent = new Intent(view.getContext(), mLaunchActivity);

                // Add an intent to the launch to point it towards our sub-activity.
                launchIntent.putExtra(CONFIG_DATA, mConfigDataClass.getSimpleName());
                launchIntent.setAction(mSubActivityIntent);

                Activity activity = (Activity) view.getContext();
                activity.startActivityForResult(
                        launchIntent,
                        ConfigActivity.UPDATED_CONFIG_REDRAW_NO_MATTER_WHAT_RESULT_CODE);
            }
        }
    }

    public class TypefaceViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        private final Button mButton;

        private ConfigData.TypefaceConfigItem mConfigItem;

        private final int mVisibleLayoutHeight, mVisibleLayoutWidth;

        TypefaceViewHolder(@NonNull View view) {
            super(view);

            mButton = view.findViewById(R.id.config_list_button_widget);
            view.setOnClickListener(this);

            mVisibleLayoutHeight = itemView.getLayoutParams().height;
            mVisibleLayoutWidth = itemView.getLayoutParams().width;
        }

        void bind(@NonNull ConfigData.TypefaceConfigItem configItem) {
            mConfigItem = configItem;

            setTextAndVisibility();
        }

        private void setTextAndVisibility() {
            // Set text to the appropriate resource in the Typeface's resource array.
            @ArrayRes int arrayRes = mConfigItem.getTypeface().getNameResourceId();
            mButton.setText(itemView.getResources().
                    getStringArray(arrayRes)[mConfigItem.getTypeface().ordinal()]);

            // Set typeface to whatever the typeface ought to be!
            Typeface t = mCurrentWatchFaceState.getTypefaceObject(mConfigItem.getTypeface());
            mButton.setTypeface(t);

            // Set visible or invisible based on whether the typeface is installed.
            if (t != null) {
                itemView.getLayoutParams().height = mVisibleLayoutHeight;
                itemView.getLayoutParams().width = mVisibleLayoutWidth;
                itemView.setVisibility(View.VISIBLE);
            } else {
                itemView.getLayoutParams().height = 0;
                itemView.getLayoutParams().width = 0;
                itemView.setVisibility(View.GONE);
            }
        }

        @Override
        public void onClick(@NonNull View view) {
            // We selected this typeface. Write it to shared pref.
            mCurrentWatchFaceState.setTypeface(mConfigItem.getTypeface());
            mSharedPref.putWatchFaceStateString(mCurrentWatchFaceState.getString());

            // Close the activity, it went just great!
            Activity activity = (Activity) view.getContext();
            activity.setResult(Activity.RESULT_OK);
            activity.finish();
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
            if (mConfigItem == null) {
                return;
            }
            mButton.setText(mConfigItem.getName(
                    mCurrentWatchFaceState, itemView.getContext()));
            Drawable left = itemView.getContext().getDrawable(mConfigItem.getIconResourceId());
            if (left != null) {
                left.setTint(mButton.getCurrentTextColor());
                mButton.setCompoundDrawablesWithIntrinsicBounds(left, null, null, null);
            }

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
//            int position = getAdapterPosition();
//            Log.d(TAG, "Complication onClick() position: " + position);

            if (mLaunchActivity != null) {
                // Regenerate and grab our current permutations. Just in time!
                String[] permutations =
                        mConfigItem.permute(mCurrentWatchFaceState, itemView.getContext());

                if (permutations != null) {
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
                    launchIntent.putExtra(INTENT_EXTRA_SLOT,
                            mWatchFaceComponentName.getClassName());
                    launchIntent.putExtra(INTENT_EXTRA_LABEL, mConfigItem.getNameResourceId());
                    launchIntent.putExtra(INTENT_EXTRA_EXTRA_NAMES, extraNames);

                    Activity activity = (Activity) view.getContext();
                    activity.startActivityForResult(
                            launchIntent,
                            ConfigActivity.UPDATED_CONFIG_REDRAW_PLEASE_REQUEST_CODE);
                }
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

            mSwitch = view.findViewById(R.id.config_list_toggle_widget);
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
            if (mConfigItem == null) {
                return;
            }

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
            Drawable left = itemView.getContext().getDrawable(currentIconResourceId);
            if (left != null) {
                left.setTint(mSwitch.getCurrentTextColor());
                mSwitch.setCompoundDrawablesWithIntrinsicBounds(left, null, null, null);
            }
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
