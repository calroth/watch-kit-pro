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
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

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

    private AnalogComplicationConfigData.ColorConfigItem.Type type;

    public ColorSelectionRecyclerViewAdapter(
            AnalogComplicationConfigData.ColorConfigItem.Type type) {

        this.type = type;
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
//        Log.d(TAG, "Element " + position + " set.");
        ColorViewHolder c = (ColorViewHolder) viewHolder;
        if (c == null) {
            return;
        }

        int width = c.mColorImageView.getMeasuredWidth();

        ViewGroup.LayoutParams l = c.mColorImageView.getLayoutParams();
        Log.d(TAG, "Height of " + position + " was " + l.height + ", width was " + width);
        if (c.getBetterAdapterPosition() == -1) {
            l.height = width / 2;
        } else {
            l.height = width;
        }
        c.mColorImageView.setLayoutParams(l);
    }

    @Override
    public int getItemCount() {
        return PaintBox.colors.length + 12;
    }

    /**
     * Displays color options for an item on the watch face and saves value to the
     * SharedPreference associated with it.
     */
    public class ColorViewHolder extends RecyclerView.ViewHolder {
        private ImageView mColorImageView;

        public ColorViewHolder(final View view) {
            super(view);
            mColorImageView = view.findViewById(R.id.color);
            mColorImageView.setImageDrawable(new Drawable() {
                @Override
                public void draw(@NonNull Canvas canvas) {
                    int position = getBetterAdapterPosition();
                    if (position == -1) {
                        return;
                    }

                    RectF bounds = new RectF(getBounds());

                    Paint o = new Paint();
                    o.setColor(Color.WHITE);
                    o.setStyle(Paint.Style.STROKE);
                    o.setAntiAlias(true);
                    o.setStrokeWidth(4.0f);

                    Paint p = new Paint();
                    p.setStyle(Paint.Style.FILL);
                    p.setAntiAlias(true);
                    p.setColor(PaintBox.colors[position]);
                    canvas.drawOval(bounds, p);
                    canvas.drawOval(bounds, o);
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
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    int position = getBetterAdapterPosition();
                    if (position == -1) {
                        return;
                    }

                    int color = PaintBox.colors[position];

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
            });
        }

        /**
         * Gets the adapter position. Returns -1 if it's a blank space.
         *
         * @return
         */
        int getBetterAdapterPosition() {
            int result = getAdapterPosition();
            if (result >= 0 && result <= 15) {
                return result;
            } else if (result >= 16 && result <= 19) {
                return -1;
            } else if (result >= 20 && result <= 35) {
                return result - 4;
            } else if (result >= 36 && result <= 39) {
                return -1;
            } else if (result >= 40 && result <= 55) {
                return result - 8;
            } else if (result >= 56 && result <= 59) {
                return -1;
            } else if (result >= 60 && result <= 75) {
                return result - 12;
            } else {
                return -1;
            }
        }
    }
}