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
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import pro.watchkit.wearable.watchface.R;
import pro.watchkit.wearable.watchface.model.AnalogComplicationConfigData;
import pro.watchkit.wearable.watchface.model.PaintBox;
import pro.watchkit.wearable.watchface.model.WatchFacePreset;

/**
 * Provides a binding from color selection data set to views that are displayed within
 * {@link ColorSelectionActivity}.
 * Color options change appearance for the item specified on the watch face. Value is saved to a
 * {@link SharedPreferences} value passed to the class.
 */

public class ColorSelectionRecyclerViewAdapter extends
        RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = ColorSelectionRecyclerViewAdapter.class.getSimpleName();

    //    private ArrayList<Integer> mColorOptionsDataSet;
    //    private String mSharedPrefString;
    private AnalogComplicationConfigData.ColorConfigItem.Type type;

    public ColorSelectionRecyclerViewAdapter(
            AnalogComplicationConfigData.ColorConfigItem.Type type//,
//            String sharedPrefString,
//            ArrayList<Integer> colorSettingsDataSet
    ) {

//        mSharedPrefString = sharedPrefString;
        this.type = type;
//        mColorOptionsDataSet = colorSettingsDataSet;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder(): viewType: " + viewType);

        RecyclerView.ViewHolder viewHolder =
                new ColorViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.color_config_list_item, parent, false));
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        Log.d(TAG, "Element " + position + " set.");

//        Integer color = mColorOptionsDataSet.get(position);
//        ColorViewHolder colorViewHolder = (ColorViewHolder) viewHolder;
//        colorViewHolder.setColor(color);
    }

    @Override
    public int getItemCount() {
//        return mColorOptionsDataSet.size();
        return 4;
    }

    /**
     * Displays color options for an item on the watch face and saves value to the
     * SharedPreference associated with it.
     */
    public class ColorViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

//        private CircledImageView mColorCircleImageView;

        private ImageView mFourColorImageView;
        @ColorInt
        private int mColor = Color.WHITE;

        private float touchX, touchY;

//        public void setColor(int color) {
//            mColor = color;
////            mColorCircleImageView.setCircleColor(color);
//        }

        public ColorViewHolder(final View view) {
            super(view);
//            mColorCircleImageView = view.findViewById(R.id.color);
            mFourColorImageView = view.findViewById(R.id.color);
            mFourColorImageView.setImageDrawable(new Drawable() {
                @Override
                public void draw(@NonNull Canvas canvas) {
                    int position = getAdapterPosition() * 16;

//                    Paint q = new Paint();
//                    q.setColor(Color.RED);
//                    q.setStyle(Paint.Style.FILL_AND_STROKE);
//                    q.setAntiAlias(true);
//
//                    canvas.drawRect(getBounds(), q);

                    Paint o = new Paint();
                    o.setColor(Color.WHITE);
                    o.setStyle(Paint.Style.STROKE);
                    o.setAntiAlias(true);
                    o.setStrokeWidth(4.0f);

                    float x = getBounds().width() / 4;
                    float y = getBounds().height() / 4;
                    float radius = Math.min(x * 0.4f, y * 0.4f);

                    Paint p = new Paint();
                    p.setStyle(Paint.Style.FILL);
                    p.setAntiAlias(true);

                    // Draw 16 circles
                    for (int i = 0; i < 4; i++) {
                        for (int j = 0; j < 4; j++) {
                            float cx = (i * x) + (0.5f * x);
                            float cy = (j * y) + (0.5f * y);
                            p.setColor(PaintBox.colors[position]);
                            position++;
                            canvas.drawCircle(cx, cy, radius, p);
                            canvas.drawCircle(cx, cy, radius, o);
                        }
                    }
                }

                @Override
                public void setAlpha(int alpha) {

                }

                @Override
                public void setColorFilter(@Nullable ColorFilter colorFilter) {

                }

                @Override
                public int getOpacity() {
                    return PixelFormat.OPAQUE;
                }
            });
            view.setOnClickListener(this);

            mFourColorImageView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    // save the X,Y coordinates
                    if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                        Rect bounds = mFourColorImageView.getDrawable().getBounds();

                        int[] t = new int[2];
                        mFourColorImageView.getLocationOnScreen(t);

                        float x = mFourColorImageView.getDrawable().getBounds().width();
                        float y = mFourColorImageView.getDrawable().getBounds().height();

                        Log.d("Wot3", String.format("%f %f (%d %d %d %d) (%d %d)", event.getX(), event.getY(), bounds.left, bounds.right, bounds.top, bounds.bottom, t[0], t[1]));
                        touchX = event.getX();
                        touchY = event.getY();
                    }

                    // let the touch event pass on to whoever needs it
                    return false;
                }
            });
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
//            Integer color = mColorOptionsDataSet.get(position);

            float x = mFourColorImageView.getDrawable().getBounds().width() / 4f;
            float y = mFourColorImageView.getDrawable().getBounds().height() / 4f;

            int i = (int) Math.floor(touchX / x);
            int j = (int) Math.floor(touchY / y);

            Log.d("Wot", String.format("%f %f / %f %f / %d %d", touchX, touchY, x, y, i, j));

            int color = PaintBox.colors[(position * 16) + (i * 4) + j];

            Activity activity = (Activity) view.getContext();
            {
                SharedPreferences preferences = activity.getSharedPreferences(
                        activity.getString(R.string.analog_complication_preference_file_key),
                        Context.MODE_PRIVATE);

                WatchFacePreset preset = new WatchFacePreset();

                preset.setString(preferences.getString(
                        activity.getString(R.string.saved_watch_face_preset), null));
                switch (type) {
                    case FILL:
                        preset.setFillColor(color);
                        break;
                    case ACCENT:
                        preset.setAccentColor(color);
                        break;
                    case HIGHLIGHT:
                        preset.setHighlightColor(color);
                        break;
                    case BASE:
                        preset.setBaseColor(color);
                        break;
                    default:
                        // Should never happen...
                        break;
                }
                Log.d("AnalogWatchFace", "Write: " + preset.getString());

                SharedPreferences.Editor editor = preferences.edit();
                editor.putString(
                        activity.getString(R.string.saved_watch_face_preset), preset.getString());
                editor.commit();

                // Lets Complication Config Activity know there was an update to colors.
                activity.setResult(Activity.RESULT_OK);
            }

//            if (mSharedPrefString != null && !mSharedPrefString.isEmpty()) {
//                SharedPreferences sharedPref = activity.getSharedPreferences(
//                        activity.getString(R.string.analog_complication_preference_file_key),
//                        Context.MODE_PRIVATE);
//
//                SharedPreferences.Editor editor = sharedPref.edit();
//                editor.putInt(mSharedPrefString, color);
//                editor.commit();
//
//                // Let's Complication Config Activity know there was an update to colors.
//                activity.setResult(Activity.RESULT_OK);
//            }
            activity.finish();
        }
    }
}