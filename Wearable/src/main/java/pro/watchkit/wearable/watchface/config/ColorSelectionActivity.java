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
import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.wear.widget.WearableRecyclerView;
import pro.watchkit.wearable.watchface.R;
import pro.watchkit.wearable.watchface.model.AnalogComplicationConfigData;

/**
 * Allows user to select color for something on the watch face (background, highlight,etc.) and
 * saves it to {@link android.content.SharedPreferences} in
 * {@link RecyclerView.Adapter}.
 */
public class ColorSelectionActivity extends Activity {

    static final String EXTRA_SHARED_PREF =
            "pro.watchkit.wearable.watchface.config.extra.EXTRA_SHARED_PREF";
    private static final String TAG = ColorSelectionActivity.class.getSimpleName();
    private WearableRecyclerView mConfigAppearanceWearableRecyclerView;

    private ColorSelectionRecyclerViewAdapter mColorSelectionRecyclerViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_selection_config);

        // Assigns SharedPreference String used to save color selected.
        String sharedPrefString = getIntent().getStringExtra(EXTRA_SHARED_PREF);

        mColorSelectionRecyclerViewAdapter = new ColorSelectionRecyclerViewAdapter(
                sharedPrefString,
                AnalogComplicationConfigData.getColorOptionsDataSet());

        mConfigAppearanceWearableRecyclerView =
                findViewById(R.id.wearable_recycler_view);

        // Aligns the first and last items on the list vertically centered on the screen.
        mConfigAppearanceWearableRecyclerView.setEdgeItemsCenteringEnabled(true);

        mConfigAppearanceWearableRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Improves performance because we know changes in content do not change the layout size of
        // the RecyclerView.
        mConfigAppearanceWearableRecyclerView.setHasFixedSize(true);

        mConfigAppearanceWearableRecyclerView.setAdapter(mColorSelectionRecyclerViewAdapter);
    }
}