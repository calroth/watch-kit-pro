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
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import pro.watchkit.wearable.watchface.R;
import pro.watchkit.wearable.watchface.model.AnalogComplicationConfigData;
import pro.watchkit.wearable.watchface.model.PaintBox;

/**
 * Allows user to select color for something on the watch face (background, highlight,etc.) and
 * saves it to {@link android.content.SharedPreferences} in
 * {@link RecyclerView.Adapter}.
 */
public class ColorSelectionActivity extends Activity {

    static final String EXTRA_SHARED_PREF =
            "pro.watchkit.wearable.watchface.config.extra.EXTRA_SHARED_PREF";
    private static final String TAG = ColorSelectionActivity.class.getSimpleName();

    private int[][] rows;

    private int calc(int a, int b, int c) {
        return (a * 16) + (b * 4) + c;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_selection_config);

        // Assigns SharedPreference String used to save color selected.
        String sharedPrefString = getIntent().getStringExtra(EXTRA_SHARED_PREF);
        AnalogComplicationConfigData.ColorConfigItem.Type type =
                AnalogComplicationConfigData.ColorConfigItem.Type.valueOf(sharedPrefString);

        int[] row1 = new int[]{
                -1,
                calc(3, 2, 3),
                calc(3, 1, 3),
                calc(2, 2, 3),
                calc(2, 3, 3),
                calc(1, 3, 3),
                calc(2, 3, 2),
                calc(3, 3, 2),
                calc(3, 3, 1),
                calc(3, 2, 2),
                calc(3, 3, 3),
                -1
        };

        int[] row2 = new int[]{
                -1,
                calc(3, 1, 2),
                calc(3, 0, 3),
                calc(2, 1, 3),
                calc(1, 2, 3),
                calc(0, 3, 3),
                calc(1, 3, 2),
                calc(2, 3, 1),
                calc(3, 3, 0),
                calc(3, 2, 1),
                calc(2, 2, 2),
                -1
        };

        int[] row3 = new int[]{
                calc(3, 0, 2),
                calc(2, 1, 2),
                calc(2, 0, 3),
                calc(1, 1, 3),
                calc(0, 2, 3),
                calc(1, 2, 2),
                calc(0, 3, 2),
                calc(1, 3, 1),
                calc(2, 3, 0),
                calc(2, 2, 1),
                calc(3, 2, 0),
                calc(3, 1, 1),
        };

        int[] row4 = new int[]{
                calc(3, 0, 1),
                calc(2, 0, 2),
                calc(1, 0, 3),
                calc(1, 1, 2),
                calc(0, 1, 3),
                calc(0, 2, 2),
                calc(0, 3, 1),
                calc(1, 2, 1),
                calc(1, 3, 0),
                calc(2, 2, 0),
                calc(3, 1, 0),
                calc(2, 1, 1),
        };

        int[] row5 = new int[]{
                -1,
                calc(3, 0, 0),
                calc(2, 0, 1),
                calc(1, 0, 2),
                calc(0, 0, 3),
                calc(0, 1, 2),
                calc(0, 2, 1),
                calc(0, 3, 0),
                calc(1, 2, 0),
                calc(2, 1, 0),
                calc(1, 1, 1),
                -1
        };

        int[] row6 = new int[]{
                -1,
                calc(2, 0, 0),
                calc(1, 0, 1),
                calc(0, 0, 1),
                calc(0, 0, 2),
                calc(0, 1, 1),
                calc(0, 1, 0),
                calc(0, 2, 0),
                calc(1, 1, 0),
                calc(1, 0, 0),
                calc(0, 0, 0),
                -1
        };

        rows = new int[][]{row1, row2, row3, row4, row5, row6};

        ImageView mColorImageView = findViewById(R.id.color);
        mColorImageView.setImageDrawable(new Drawable() {
            @Override
            public void draw(@NonNull Canvas canvas) {
                RectF bounds = new RectF(getBounds());

                Paint b = new Paint();
                b.setColor(Color.RED);
                b.setStyle(Paint.Style.STROKE);
                b.setAntiAlias(true);
                b.setStrokeWidth(1.0f);

                Paint o = new Paint();
                o.setColor(Color.WHITE);
                o.setStyle(Paint.Style.STROKE);
                o.setAntiAlias(true);
                o.setStrokeWidth(4.0f);

                Paint p = new Paint();
                p.setStyle(Paint.Style.FILL);
                p.setAntiAlias(true);

                float spanRoot3 = bounds.width() / (rows.length + 1f);
                float span = spanRoot3 / 0.86602540378f; // sqrt(3) / 2

                float radius = spanRoot3 * 0.45f;
                float cx = 0f;

                for (int i = 0; i < rows.length; i++) {
                    cx += spanRoot3;
                    // 0 for even cols, vertical offset for odd
                    float cy = (i % 2 == 0) ? span * 0.5f : span * 1.0f;
                    cy += span; // Temporary
                    for (int j = 0; j < rows[i].length; j++) {
                        canvas.drawRect(cx - (spanRoot3 / 2f),
                                cy - (span / 2f),
                                cx + (spanRoot3 / 2f),
                                cy + (span / 2f), b);
                        if (rows[i][j] != -1) {
                            p.setColor(PaintBox.colors[rows[i][j]]);
                            canvas.drawCircle(cx, cy, radius, o);
                            canvas.drawCircle(cx, cy, radius, p);
                        }
                        cy += span;
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
    }
}