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
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.wear.widget.WearableRecyclerView;

import pro.watchkit.wearable.watchface.R;
import pro.watchkit.wearable.watchface.model.PaintBox;
import pro.watchkit.wearable.watchface.model.WatchFacePreset;

/**
 * Allows user to select a WatchFacePreset and
 * saves it to {@link android.content.SharedPreferences} in
 * {@link RecyclerView.Adapter}.
 */
public class WatchFaceSelectionActivity extends Activity {

    static final String INTENT_EXTRA_PRESETS =
            "pro.watchkit.wearable.watchface.config.extra.INTENT_EXTRA_PRESETS";
    static final String INTENT_EXTRA_SETTINGS =
            "pro.watchkit.wearable.watchface.config.extra.INTENT_EXTRA_SETTINGS";
    private static final String TAG = WatchFaceSelectionActivity.class.getSimpleName();

    private WearableRecyclerView mWearableRecyclerView;

    private WatchFacePresetSelectionRecyclerViewAdapter mWatchFacePresetSelectionRecyclerViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watch_face_preset_selection_config);

        String[] watchFacePresetStrings = getIntent().getStringArrayExtra(INTENT_EXTRA_PRESETS);
        String[] settingsStrings = getIntent().getStringArrayExtra(INTENT_EXTRA_SETTINGS);

        mWatchFacePresetSelectionRecyclerViewAdapter =
                new WatchFacePresetSelectionRecyclerViewAdapter(watchFacePresetStrings, settingsStrings);

        mWearableRecyclerView =
                findViewById(R.id.wearable_recycler_view);

        // Aligns the first and last items on the list vertically centered on the screen.
        mWearableRecyclerView.setEdgeItemsCenteringEnabled(true);

        mWearableRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Improves performance because we know changes in content do not change the layout size of
        // the RecyclerView.
        mWearableRecyclerView.setHasFixedSize(true);

        mWearableRecyclerView.setAdapter(mWatchFacePresetSelectionRecyclerViewAdapter);
    }

    /**
     * Save the given color to preferences. We extract the WatchFacePreset held in preferences,
     * then change the color type pre-stored in INTENT_EXTRA_PRESETS, then save the WatchFacePreset
     * back to preferences.
     *
     * @param sixBitColor New 6-bit color to set (between 0 and 63)
     * @param preset      WatchFacePreset to modify and write
     * @param paintBox    PaintBox to get the color names from, for display purposes
     */
    private void setSixBitColor(int sixBitColor, WatchFacePreset preset, PaintBox paintBox) {
        SharedPreferences preferences = getSharedPreferences(
                getString(R.string.analog_complication_preference_file_key),
                Context.MODE_PRIVATE);

        String sharedPrefString = getIntent().getStringExtra(INTENT_EXTRA_PRESETS);
        WatchFacePreset.ColorType colorType = WatchFacePreset.ColorType.valueOf(sharedPrefString);
        String toastText;

        preset.setSixBitColor(colorType, sixBitColor);

        switch (colorType) {
            case FILL:
                toastText = getString(R.string.config_fill_color_label);
                break;
            case ACCENT:
                toastText = getString(R.string.config_accent_color_label);
                break;
            case HIGHLIGHT:
                toastText = getString(R.string.config_marker_color_label);
                break;
            case BASE:
                toastText = getString(R.string.config_base_color_label);
                break;
            default:
                // Should never happen...
                toastText = "???\nColor";
                break;
        }
        Log.d("AnalogWatchFace", "Write: " + preset.getString());

        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(getString(R.string.saved_watch_face_preset), preset.getString());
        editor.commit();

        // Lets Complication Config Activity know there was an update to colors.
        setResult(Activity.RESULT_OK);

        // Show a toast popup with the color we just selected.
        toastText = toastText.replace('\n', ' ') +
                ":\n" + paintBox.getColorName(sixBitColor);
        Toast.makeText(this, toastText, Toast.LENGTH_LONG).show();

        finish();
    }
}