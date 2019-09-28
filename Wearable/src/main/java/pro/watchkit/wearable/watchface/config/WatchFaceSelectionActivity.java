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
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;
import androidx.wear.widget.WearableRecyclerView;

import pro.watchkit.wearable.watchface.R;
import pro.watchkit.wearable.watchface.model.WatchFaceState;
import pro.watchkit.wearable.watchface.util.SharedPref;
import pro.watchkit.wearable.watchface.util.Toaster;
import pro.watchkit.wearable.watchface.watchface.ProWatchFaceService;
import pro.watchkit.wearable.watchface.watchface.WatchFaceGlobalDrawable;

/**
 * Allows user to select a WatchFacePreset and
 * saves it to {@link android.content.SharedPreferences} in
 * {@link RecyclerView.Adapter}.
 */
public class WatchFaceSelectionActivity extends Activity {

    static final String INTENT_EXTRA_STATES =
            WatchFaceSelectionActivity.class.getSimpleName() + "INTENT_EXTRA_STATES";
    static final String INTENT_EXTRA_FLAGS =
            WatchFaceSelectionActivity.class.getSimpleName() + "INTENT_EXTRA_FLAGS";
    static final String INTENT_EXTRA_SLOT =
            WatchFaceSelectionActivity.class.getSimpleName() + "INTENT_EXTRA_SLOT";
    static final String INTENT_EXTRA_LABEL =
            WatchFaceSelectionActivity.class.getSimpleName() + "INTENT_EXTRA_LABEL";
    static final String INTENT_EXTRA_EXTRA_NAMES =
            WatchFaceSelectionActivity.class.getSimpleName() + "INTENT_EXTRA_EXTRA_NAMES";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watch_face_preset_selection_config);

        String[] watchFaceStateStrings = getIntent().getStringArrayExtra(INTENT_EXTRA_STATES);
        int flags = getIntent().getIntExtra(
                INTENT_EXTRA_FLAGS, WatchFaceGlobalDrawable.PART_BACKGROUND);
        int nameResourceId = getIntent().getIntExtra(
                INTENT_EXTRA_LABEL, 0);

        // Try to get the watch face slot from our activity intent.
        String slot = getIntent().getStringExtra(INTENT_EXTRA_SLOT);
        Class watchFaceServiceClass;
        if (slot == null) {
            // Default: A
            watchFaceServiceClass = ProWatchFaceService.A.class;
        } else if (slot.equals(ProWatchFaceService.B.class.getName())) {
            watchFaceServiceClass = ProWatchFaceService.B.class;
        } else if (slot.equals(ProWatchFaceService.C.class.getName())) {
            watchFaceServiceClass = ProWatchFaceService.C.class;
        } else if (slot.equals(ProWatchFaceService.D.class.getName())) {
            watchFaceServiceClass = ProWatchFaceService.D.class;
        } else {
            watchFaceServiceClass = ProWatchFaceService.A.class;
        }

        String[] extraNames = getIntent().getStringArrayExtra(INTENT_EXTRA_EXTRA_NAMES);

        WatchFaceSelectionRecyclerViewAdapter recyclerViewAdapter =
                new WatchFaceSelectionRecyclerViewAdapter(this, watchFaceServiceClass,
                        watchFaceStateStrings, flags, nameResourceId);

        WearableRecyclerView view = findViewById(R.id.wearable_recycler_view);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        view.setLayoutManager(layoutManager);

        // Improves performance because we know changes in content do not change the layout size of
        // the RecyclerView.
        view.setHasFixedSize(true);

        view.setAdapter(recyclerViewAdapter);

        // Attach a LinearSnapHelper to make it snap to each watch face!
        // This LinearSnapHelper is custom: it allows us to "snap" to the first item too.
        SnapHelper snapHelper = new LinearSnapHelper() {
            /**
             * Custom findSnapView.
             * <p>
             * If we're scrolled to the top, within half a height of the first item (the header),
             * snap to the header instead! Otherwise use the default behaviour.
             *
             * @param layoutManager the RecyclerView.LayoutManager attached to the RecyclerView
             * @return the target view which we want to snap to
             */
            @Override
            public View findSnapView(RecyclerView.LayoutManager layoutManager) {
                final View header = layoutManager.getChildAt(0);
                if (header != null &&
                        view.computeVerticalScrollOffset() < header.getMeasuredHeight() / 2) {
                    return header;
                } else {
                    return super.findSnapView(layoutManager);
                }
            }
        };
        snapHelper.attachToRecyclerView(view);

        // Add an OnScrollListener to the view.
        // Every time we snap to a new view with the SnapHelper, check its position.
        // Show a toast depending on what we snapped.
        view.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    View snapView = snapHelper.findSnapView(layoutManager);
                    if (snapView != null) {
                        int i = layoutManager.getPosition(snapView);
                        if (/* i != RecyclerView.NO_POSITION && */ i > 0) {
                            Toaster.makeText(getApplicationContext(), extraNames[i - 1],
                                    Toaster.LENGTH_SHORT);
                        }
                    }
                }
            }
        });

        // Attempt to scroll to the current selection in preferences.
        // Get our current preference.
        SharedPref sharedPref = new SharedPref(this, watchFaceServiceClass);
        String currentWatchFaceState = sharedPref.getWatchFaceStateString();

        // Go through our state strings and find the one that's equal to our current selection.
        if (currentWatchFaceState != null) {
            for (int i = 0; i < watchFaceStateStrings.length; i++) {
                if (WatchFaceState.mostlyEquals(watchFaceStateStrings[i], currentWatchFaceState)) {
                    // We found it! Scroll to this item.
                    // This doesn't work, why? // layoutManager.scrollToPosition(i);
                    layoutManager.smoothScrollToPosition(view, null, i + 1);
                    break;
                }
            }
        }
    }

    @Override
    protected void onStop() {
        // Unset the adapter. This cleans it up.
        WearableRecyclerView view = findViewById(R.id.wearable_recycler_view);
        view.setAdapter(null);
        super.onStop();
    }
}
