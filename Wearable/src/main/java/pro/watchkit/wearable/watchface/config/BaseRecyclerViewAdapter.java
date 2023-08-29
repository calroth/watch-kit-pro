/*
 * Copyright (C) 2018-2022 Terence Tan
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

import static android.support.wearable.complications.ComplicationData.TYPE_NOT_CONFIGURED;
import static android.support.wearable.complications.ComplicationData.TYPE_SHORT_TEXT;
import static pro.watchkit.wearable.watchface.config.ColorSelectionActivity.INTENT_EXTRA_COLOR;
import static pro.watchkit.wearable.watchface.config.ColorSelectionActivity.INTENT_EXTRA_COLOR_LABEL;
import static pro.watchkit.wearable.watchface.config.ConfigActivity.CONFIG_DATA;
import static pro.watchkit.wearable.watchface.config.WatchFaceSelectionActivity.INTENT_EXTRA_EXTRA_NAMES;
import static pro.watchkit.wearable.watchface.config.WatchFaceSelectionActivity.INTENT_EXTRA_EXTRA_SWATCHES;
import static pro.watchkit.wearable.watchface.config.WatchFaceSelectionActivity.INTENT_EXTRA_FLAGS;
import static pro.watchkit.wearable.watchface.config.WatchFaceSelectionActivity.INTENT_EXTRA_LABEL;
import static pro.watchkit.wearable.watchface.config.WatchFaceSelectionActivity.INTENT_EXTRA_SLOT;
import static pro.watchkit.wearable.watchface.config.WatchFaceSelectionActivity.INTENT_EXTRA_STATES;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
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

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.Executors;

import pro.watchkit.wearable.watchface.BuildConfig;
import pro.watchkit.wearable.watchface.R;
import pro.watchkit.wearable.watchface.model.BytePackable;
import pro.watchkit.wearable.watchface.model.ComplicationHolder;
import pro.watchkit.wearable.watchface.model.ConfigData;
import pro.watchkit.wearable.watchface.model.PaintBox;
import pro.watchkit.wearable.watchface.model.WatchFaceState;
import pro.watchkit.wearable.watchface.util.SharedPref;
import pro.watchkit.wearable.watchface.util.Toaster;
import pro.watchkit.wearable.watchface.watchface.ProWatchFaceService;
import pro.watchkit.wearable.watchface.watchface.WatchFaceGlobalDeferredDrawable;
import pro.watchkit.wearable.watchface.watchface.WatchFaceGlobalDrawable;

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
    @NonNull
    private final ProviderInfoRetriever mProviderInfoRetriever;

    /**
     * The ComponentName of our WatchFaceService. We use this to find what complications have been
     * set for this WatchFaceService. It's different for slot A, B, C etc. as we allow the user to
     * have different complication setups per slot!
     */
    @NonNull
    private final ComponentName mWatchFaceComponentName;

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

    BaseRecyclerViewAdapter(@NonNull Context context,
                            @NonNull Class<? extends ProWatchFaceService> watchFaceServiceClass) {
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

        if (mCurrentWatchFaceState.isDeveloperMode()) {
            android.util.Log.d("BaseRecyclerViewAdapter",
                    mSubActivityIntent + " ~ " + mCurrentWatchFaceState.getString());
        }
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
                    WatchFaceGlobalDrawable.PART_PIPS |
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

        final ImageView mImageView;

        WatchFaceGlobalDeferredDrawable mWatchFaceGlobalDrawable;

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

        void setPreset(@Nullable String watchFaceStateString, int swatch) {
            if (mWatchFaceGlobalDrawable == null) {
                mWatchFaceGlobalDrawable = new WatchFaceGlobalDeferredDrawable(
                        mImageView.getContext(),
                        // Always set PART_CLIP for this ImageView.
                        mWatchFaceGlobalDrawableFlags | WatchFaceGlobalDrawable.PART_CLIP,
                        mImageView);
                mImageView.setImageDrawable(mWatchFaceGlobalDrawable);
                // Set layer type to hardware. We promise not to update this any more,
                // so now Android can render this to a texture and leave it there.
                mImageView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            } else {
                // We've recycled this ViewHolder. Cancel its background tasks.
                mWatchFaceGlobalDrawable.cancelBackgroundTasks();
            }
            WatchFaceState w = mWatchFaceGlobalDrawable.getWatchFaceState();
            if (watchFaceStateString != null) {
                w.setString(watchFaceStateString);
                // Sanity check on the value of swatch.
                if (swatch >= BytePackable.Material.finalValues.length || swatch < -1) {
                    swatch = -1;
                }
                w.setSwatchMaterial(
                        swatch == -1 ? null : BytePackable.Material.finalValues[swatch]);
            }
            w.setNotifications(0, 0);
            w.setAmbient(false);
            mComplicationTextColor = w.getComplicationTextColor();

            // Initialise complications, just enough to be able to draw rings.
            w.initializeComplications(mImageView.getContext(), false);
        }

        void setPreset(@Nullable String watchFaceStateString) {
            setPreset(watchFaceStateString, -1);
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
                    Drawable icon = complicationProviderInfo.providerIcon.loadDrawable(
                            itemView.getContext());
                    if (icon != null) {
                        complication.setProviderIconDrawable(icon, true);
                    }
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
        @NonNull
        private final Activity mCurrentActivity;

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

        private final TextView mLabelTextView;
        private final int mVisibleLayoutHeight, mVisibleLayoutWidth;
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

            onWatchFaceStateChanged();
        }

        @Override
        public void onWatchFaceStateChanged() {
            if (mConfigItem == null) {
                return;
            }

            Resources r = itemView.getResources();
            mStringBuilder.setLength(0);

            // Generate the heading if any.
            if (mConfigItem instanceof ConfigData.HeadingLabelConfigItem) {
                mLabelTextView.setTextAppearance(
                        androidx.appcompat.R.style.TextAppearance_AppCompat_Large_Inverse);
                mStringBuilder.append(r.getString(mTitleLabel)).append("<br>");
            } else if (mConfigItem instanceof ConfigData.TitleLabelConfigItem) {
                mLabelTextView.setTextAppearance(
                        androidx.appcompat.R.style.TextAppearance_AppCompat_Medium_Inverse);
                int t = ((ConfigData.TitleLabelConfigItem) mConfigItem).getTitleResourceId();
                mStringBuilder.append("<b>")
                        .append(r.getString(t))
                        .append("</b> ");
            }

            // Generate the label if any.
            if (mConfigItem.getLabelResourceId() != -1) {
                mStringBuilder.append(r.getString(mConfigItem.getLabelResourceId()));
            } else if (mConfigItem.getLabelGenerator() != null) {
                mStringBuilder.append(
                        mConfigItem.getLabelGenerator().apply(mCurrentWatchFaceState));
            }

            // Set the text to the title and label!
            mLabelTextView.setText(Html.fromHtml(mStringBuilder.toString(),
                    Html.FROM_HTML_MODE_LEGACY));

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
            if (mConfigItem.getLabelResourceId() == R.string.config_generate_icon_files &&
                    BuildConfig.DEBUG) {
                Context context = itemView.getContext();
                SharedPref.mWriteLayersToDisk = false;

                // Create our foreground drawable.
                int count = 0;
                for (Map.Entry<String, String> g :
                        mCurrentWatchFaceState.getGalleryEntries().entrySet()) {
                    for (Map.Entry<String, Integer> cw :
                            mCurrentWatchFaceState.getPaintBox().getOriginalColorways().entrySet()) {
                        int flags = WatchFaceGlobalDrawable.PART_BACKGROUND |
                                WatchFaceGlobalDrawable.PART_PIPS |
                                WatchFaceGlobalDrawable.PART_HANDS |
                                WatchFaceGlobalDrawable.PART_RINGS_ACTIVE;

                        WatchFaceGlobalDrawable drawable =
                                new WatchFaceGlobalDrawable(context, flags);

                        WatchFaceState watchFaceState = drawable.getWatchFaceState();

                        // Quick-and-dirty code to get complication IDs.
                        ProWatchFaceService p;
                        if (count % 4 == 0) {
                            watchFaceState.setString("04941b40ef006610846065dcc6f81411~" +
                                    "16e1cf096cc000000000000000000001~0");
                            p = new ProWatchFaceService.A();
                        } else if (count % 4 == 1) {
                            watchFaceState.setString("0cd71a20ef007c2008e2212a68384c01~" +
                                    "1b434f0963c000000000000000000001~0");
                            p = new ProWatchFaceService.B();
                        } else if (count % 4 == 2) {
                            watchFaceState.setString("155b4120e0004274fb8601d8cfb81001~" +
                                    "1fe4ef0eba0000000000000000000001~0");
                            p = new ProWatchFaceService.C();
                        } else {
                            watchFaceState.setString("1d404b6a6f0066f6b4aae8c459bf4ca0~" +
                                    "13308f05084000000000000000000000~0");
                            p = new ProWatchFaceService.D();
                        }
                        watchFaceState.setWatchFacePresetString(g.getValue());
                        watchFaceState.setColorway(cw.getValue());
                        watchFaceState.setCurrentTime(1570365309000L); // 2019-10-06T23:35:09.000+1100
                        watchFaceState.setNotifications(0, 0);
                        watchFaceState.setAmbient(false);
                        drawable.setBounds(0, 0, 960, 960);

                        // Initialise complications, just enough to be able to draw rings.
                        watchFaceState.initializeComplications(context, false);

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

                        // Export as PNG
                        try {
                            String n = g.getKey()
                                    .replace("🅰", "AA")
                                    .replace("🅱", "AB")
                                    .replace("🅲", "AC")
                                    .replace("🅳", "AD");
                            FileOutputStream out = watchFaceState.openFileOutput(
                                    n + " x " + cw.getKey() + ".png");
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                            out.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    count++;
                }

                SharedPref.mWriteLayersToDisk = false;
                return;
            }
            if (mCurrentWatchFaceState.isDeveloperMode()) {
                // Ignore if we're already in developer mode
                return;
            }
            // If we spam clicks on the config git hash or date, enter developer mode.
            if ((mConfigItem instanceof ConfigData.TitleLabelConfigItem) &&
                    (((ConfigData.TitleLabelConfigItem) mConfigItem).getTitleResourceId() ==
                            R.string.config_git_hash ||
                            ((ConfigData.TitleLabelConfigItem) mConfigItem).getTitleResourceId() ==
                                    R.string.config_git_date)) {
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
        private final Button mButton;
        private Class<ColorSelectionActivity> mLaunchActivity;
        @NonNull
        private final Drawable mColorSwatchDrawable = new Drawable() {
            private Paint mCirclePaint;

            @Override
            public void draw(@NonNull Canvas canvas) {
                if (mConfigItem == null || mConfigItem.getType() == null) return;

                @ColorInt int color = mCurrentWatchFaceState.getColor(mConfigItem.getType());

                // Draw a circle that's 20px from right, top and left borders.
                Rect r = canvas.getClipBounds();
                float radius = (r.height() / 2f) - 20f;
                if (mCirclePaint == null) {
                    // Initialise on first use.
                    mCirclePaint = new Paint();
                    mCirclePaint.setStyle(Paint.Style.FILL);
                    mCirclePaint.setAntiAlias(true);
                }
                float offset = 1.0f;
                // Draw our bevels as follows:
                // Draw a white circle offset northwest
                mCirclePaint.setColor(Color.WHITE);
                canvas.drawCircle(r.right - 20f - radius - offset,
                        (r.top + r.bottom) / 2f - offset, radius, mCirclePaint);
                // Draw a black circle offset southeast
                mCirclePaint.setColor(Color.BLACK);
                canvas.drawCircle(r.right - 20f - radius + offset,
                        (r.top + r.bottom) / 2f + offset, radius, mCirclePaint);
                // Now draw our swatch.
                mCirclePaint.setColor(color);
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
            if (mConfigItem == null || mConfigItem.getType() == null || needPermissions()) {
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
            }
            Drawable right = needPermissions() ?
                    itemView.getContext().getDrawable(android.R.drawable.ic_dialog_info) :
                    mColorSwatchDrawable;
            mButton.setCompoundDrawablesWithIntrinsicBounds(left, null, right, null);
            mLaunchActivity = mConfigItem.getActivityToChoosePreference();
        }

        private boolean needPermissions() {
            Activity a = (Activity) itemView.getContext();
            return a.checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED &&
                    mConfigItem.getType().equals(PaintBox.ColorType.AMBIENT_NIGHT);
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
                        needPermissions() ?
                                ConfigActivity.UPDATED_CONFIG_REDRAW_NO_MATTER_WHAT_RESULT_CODE :
                                ConfigActivity.UPDATED_CONFIG_REDRAW_PLEASE_REQUEST_CODE);
            }
        }
    }

    public class ConfigActivityViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener, WatchFaceStateListener {

        private final Button mButton;
        private ConfigData.ConfigActivityConfigItem mConfigItem;
        private Class<? extends ConfigData> mConfigDataClass;
        private Class<ConfigActivity> mLaunchActivity;

        ConfigActivityViewHolder(@NonNull View view) {
            super(view);

            mButton = view.findViewById(R.id.config_list_button_widget);
            view.setOnClickListener(this);
        }

        void bind(@NonNull ConfigData.ConfigActivityConfigItem configItem) {
            mConfigItem = configItem;

            onWatchFaceStateChanged();

            Drawable left = itemView.getContext().getDrawable(configItem.getIconResourceId());
            if (left != null) {
                left.setTint(mButton.getCurrentTextColor());
            }
            Drawable right = itemView.getContext().getDrawable(R.drawable.ic_keyboard_arrow_right);
            if (right != null) {
                right.setTint(mButton.getCurrentTextColor());
            }
            mButton.setCompoundDrawablesWithIntrinsicBounds(left, null, right, null);
            mConfigDataClass = configItem.getConfigDataClass();
            mLaunchActivity = configItem.getActivityToChoosePreference();
        }

        public void onWatchFaceStateChanged() {
            if (mConfigItem == null) {
                return;
            }

            mButton.setText(itemView.getResources().getString(mConfigItem.getNameResourceId()));

            // This is a bit of a hack to display the current typeface name.
            // In future, we can generalise this to display any subtitle.
            if (mConfigItem.getNameResourceId() != R.string.config_configure_typeface) {
                mButton.setText(itemView.getResources().getString(mConfigItem.getNameResourceId()));
            } else {
                String title = itemView.getResources().getString(mConfigItem.getNameResourceId());
                String subtitle;
                if (mCurrentWatchFaceState.getTypefaceObject() != null) {
                    BytePackable.Typeface t = mCurrentWatchFaceState.getTypeface();
                    String[] typefaces =
                            itemView.getResources().getStringArray(t.getNameResourceId());
                    subtitle = "<br/><small>" + typefaces[t.ordinal()] + "</small>";
                } else {
                    subtitle = "";
                }
                CharSequence text = Html.fromHtml(title + subtitle, Html.FROM_HTML_MODE_LEGACY);
                mButton.setText(text);
            }
            itemView.invalidate();
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
            Drawable left = itemView.getContext().getDrawable(R.drawable.ic_font);
            if (left != null) {
                left.setTint(mButton.getCurrentTextColor());
            }
            mButton.setCompoundDrawablesWithIntrinsicBounds(left, null, null, null);

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

            // Set highlighted or not based on whether this is our current selection!
            if (t != null && t == mCurrentWatchFaceState.getTypefaceObject()) {
                itemView.setBackground(itemView.getContext().
                        getDrawable(android.R.drawable.screen_background_dark_transparent));
            } else {
                itemView.setBackground(null);
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
            mConfigItem.setSharedPref(mSharedPref);
            mLaunchActivity = configItem.getActivityToChoosePreference();
            mFlags = configItem.getWatchFaceGlobalDrawableFlags();

            setTextAndVisibility();
        }

        private final StringBuilder mExtra = new StringBuilder();

        private CharSequence getButtonText() {
            String name = itemView.getContext().getString(mConfigItem.getNameResourceId());
            mExtra.setLength(0);
            // Append mNameResourceId of current setting.
            mExtra.append(name);

            ConfigData.Permutation activePermutation = Arrays.stream(
                    mConfigItem.getPermutations(mCurrentWatchFaceState, itemView.getContext()))
                    .filter(s -> mCurrentWatchFaceState.mostlyEquals(s.getValue()))
                    .findFirst().orElse(null);
            if (activePermutation != null) {
                mExtra.append("<br/><small>")
                        .append(activePermutation.getName()).append("</small>");
            } else {
                mExtra.append("<br/><small>???</small>");
            }

            return Html.fromHtml(mExtra.toString(), Html.FROM_HTML_MODE_LEGACY);
        }

        private void setTextAndVisibility() {
            if (mConfigItem == null) {
                return;
            }
            mButton.setText(getButtonText());
            Drawable left = itemView.getContext().getDrawable(mConfigItem.getIconResourceId());
            if (left != null) {
                left.setTint(mButton.getCurrentTextColor());
            }
            Drawable right;
            if (mConfigItem instanceof ConfigData.PickerFourColorConfigItem) {
                right = mColorSwatchDrawable;
            } else {
                right = itemView.getContext().getDrawable(R.drawable.ic_keyboard_arrow_right);
                if (right != null) {
                    right.setTint(mButton.getCurrentTextColor());
                }
            }
            mButton.setCompoundDrawablesWithIntrinsicBounds(left, null, right, null);

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

        @NonNull
        private final Drawable mColorSwatchDrawable = new Drawable() {
            private Paint mCirclePaint;

            @Override
            public void draw(@NonNull Canvas canvas) {
                ConfigData.PickerFourColorConfigItem ci =
                        (ConfigData.PickerFourColorConfigItem) mConfigItem;
                if (ci == null) return;

                @ColorInt int colorA = ci.getColorACalculator().applyAsInt(mCurrentWatchFaceState);
                @ColorInt int colorB = ci.getColorBCalculator().applyAsInt(mCurrentWatchFaceState);
                @ColorInt int colorC = ci.getColorCCalculator().applyAsInt(mCurrentWatchFaceState);
                @ColorInt int colorD = ci.getColorDCalculator().applyAsInt(mCurrentWatchFaceState);

                // Draw a circle that's 20px from right, top and left borders.
                Rect r = canvas.getClipBounds();
                float radius = (r.height() / 2f) - 20f;
                if (mCirclePaint == null) {
                    // Initialise on first use.
                    mCirclePaint = new Paint();
                    mCirclePaint.setStyle(Paint.Style.FILL);
                    mCirclePaint.setAntiAlias(true);
                }
                float offset = 1.0f;
                // Draw our bevels as follows:
                // Draw a white circle offset northwest
                mCirclePaint.setColor(Color.WHITE);
                canvas.drawCircle(r.right - 20f - radius - offset,
                        (r.top + r.bottom) / 2f - offset, radius, mCirclePaint);
                // Draw a black circle offset southeast
                mCirclePaint.setColor(Color.BLACK);
                canvas.drawCircle(r.right - 20f - radius + offset,
                        (r.top + r.bottom) / 2f + offset, radius, mCirclePaint);
                // Now draw our swatch.
                RectF oval = new RectF(
                        r.right - 20f - (2 * radius), (r.top + r.bottom) / 2f - radius,
                        r.right - 20f, (r.top + r.bottom) / 2f + radius);
                mCirclePaint.setColor(colorA);
                canvas.drawArc(oval, 180f, 90f, true, mCirclePaint);
                mCirclePaint.setColor(colorB);
                canvas.drawArc(oval, 270f, 90f, true, mCirclePaint);
                mCirclePaint.setColor(colorD);
                canvas.drawArc(oval, 0f, 90f, true, mCirclePaint);
                mCirclePaint.setColor(colorC);
                canvas.drawArc(oval, 90f, 90f, true, mCirclePaint);
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
            if (mLaunchActivity != null) {
                // Regenerate and grab our current permutations. Just in time!
                ConfigData.Permutation[] permutations = mConfigItem.getPermutations(
                        mCurrentWatchFaceState, itemView.getContext());

                String[] permutationValues = Arrays.stream(permutations)
                        .map(ConfigData.Permutation::getValue).toArray(String[]::new);
                String[] permutationNames = Arrays.stream(permutations)
                        .map(ConfigData.Permutation::getName).toArray(String[]::new);
                int[] permutationSwatches = Arrays.stream(permutations)
                        .mapToInt(ConfigData.Permutation::getSwatch).toArray();

                Intent launchIntent = new Intent(view.getContext(), mLaunchActivity);

                // Pass shared preference name to save color value to.
                launchIntent.putExtra(INTENT_EXTRA_STATES, permutationValues);
                launchIntent.putExtra(INTENT_EXTRA_FLAGS, mFlags);
                launchIntent.putExtra(INTENT_EXTRA_SLOT,
                        mWatchFaceComponentName.getClassName());
                launchIntent.putExtra(INTENT_EXTRA_LABEL, mConfigItem.getNameResourceId());
                launchIntent.putExtra(INTENT_EXTRA_EXTRA_NAMES, permutationNames);
                launchIntent.putExtra(INTENT_EXTRA_EXTRA_SWATCHES, permutationSwatches);

                Activity activity = (Activity) view.getContext();
                activity.startActivityForResult(
                        launchIntent,
                        ConfigActivity.UPDATED_CONFIG_REDRAW_PLEASE_REQUEST_CODE);
            }
        }
    }

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
            }
            mSwitch.setCompoundDrawablesWithIntrinsicBounds(left, null, null, null);
        }

        @Override
        public void onWatchFaceStateChanged() {
            setDefaultSwitchValue();
        }

        @Override
        public void onClick(View view) {
            if (mConfigItem.getAlertMessageResourceId() != -1 && isChecked()) {
                // Regenerate and grab our current permutations. Just in time!
                String[] permutations =
                        mConfigItem.permute(mCurrentWatchFaceState, mSwitch.getContext());
                String yesSetting = permutations[1];
                String noSetting = permutations[0];

                BaseRecyclerViewAdapter.this.onWatchFaceStateChanged();
                // Pop an alert dialog with the given alert message.
                new AlertDialog.Builder(view.getContext(), android.R.style.Theme_DeviceDefault_Dialog_Alert)
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .setTitle(mConfigItem.getNameResourceId())
                        .setMessage(mConfigItem.getAlertMessageResourceId())
                        // User said "OK" to our explanation, so use the "yes" option.
                        .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                            mSharedPref.putWatchFaceStateString(yesSetting);
                            BaseRecyclerViewAdapter.this.onWatchFaceStateChanged();
                        })
                        // User said "Cancel" to our explanation, so use the "no" option.
                        .setNegativeButton(android.R.string.cancel, (dialog, which) -> {
                            mSharedPref.putWatchFaceStateString(noSetting);
                            BaseRecyclerViewAdapter.this.onWatchFaceStateChanged();
                        })
                        // User swiped away our explanation, so use the "no" option.
                        .setOnCancelListener(dialog -> {
                            mSharedPref.putWatchFaceStateString(noSetting);
                            BaseRecyclerViewAdapter.this.onWatchFaceStateChanged();
                        })
                        .show();
                return;
            }

            // Regenerate and grab our current permutations. Just in time!
            String[] permutations =
                    mConfigItem.permute(mCurrentWatchFaceState, mSwitch.getContext());

            mSharedPref.putWatchFaceStateString(isChecked() ? permutations[1] : permutations[0]);

            BaseRecyclerViewAdapter.this.onWatchFaceStateChanged();
        }
    }
}
