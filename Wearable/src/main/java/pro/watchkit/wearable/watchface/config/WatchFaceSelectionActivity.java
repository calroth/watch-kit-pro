/*
 * Copyright (C) 2019 Terence Tan
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

/**
 * Allows user to select a WatchFacePreset and
 * saves it to {@link android.content.SharedPreferences} in
 * {@link RecyclerView.Adapter}.
 */
public class WatchFaceSelectionActivity extends Activity {

    static final String INTENT_EXTRA_PRESETS =
            WatchFaceSelectionActivity.class.getSimpleName() + "INTENT_EXTRA_PRESETS";
    static final String INTENT_EXTRA_SETTINGS =
            WatchFaceSelectionActivity.class.getSimpleName() + "INTENT_EXTRA_SETTINGS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watch_face_preset_selection_config);

        String[] watchFacePresetStrings = getIntent().getStringArrayExtra(INTENT_EXTRA_PRESETS);
        String[] settingsStrings = getIntent().getStringArrayExtra(INTENT_EXTRA_SETTINGS);

        WatchFaceSelectionRecyclerViewAdapter recyclerViewAdapter =
                new WatchFaceSelectionRecyclerViewAdapter(watchFacePresetStrings, settingsStrings);

        WearableRecyclerView view = findViewById(R.id.wearable_recycler_view);

        // Aligns the first and last items on the list vertically centered on the screen.
        view.setEdgeItemsCenteringEnabled(true);

        view.setLayoutManager(new LinearLayoutManager(this));

        // Improves performance because we know changes in content do not change the layout size of
        // the RecyclerView.
        view.setHasFixedSize(true);

        view.setAdapter(recyclerViewAdapter);
    }
}