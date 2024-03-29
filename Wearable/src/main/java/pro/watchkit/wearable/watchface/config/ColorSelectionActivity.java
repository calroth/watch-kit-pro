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

import static pro.watchkit.wearable.watchface.config.WatchFaceSelectionActivity.INTENT_EXTRA_SLOT;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Html;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.recyclerview.widget.RecyclerView;

import pro.watchkit.wearable.watchface.R;
import pro.watchkit.wearable.watchface.model.PaintBox;
import pro.watchkit.wearable.watchface.model.WatchFaceState;
import pro.watchkit.wearable.watchface.util.SharedPref;
import pro.watchkit.wearable.watchface.util.Toaster;
import pro.watchkit.wearable.watchface.watchface.ProWatchFaceService;

/**
 * Allows user to select color for something on the watch face (background, highlight,etc.) and
 * saves it to {@link android.content.SharedPreferences} in
 * {@link RecyclerView.Adapter}.
 */
public class ColorSelectionActivity extends Activity {

    static final String INTENT_EXTRA_COLOR =
            ColorSelectionActivity.class.getSimpleName() + "INTENT_EXTRA_COLOR";
    static final String INTENT_EXTRA_COLOR_LABEL =
            ColorSelectionActivity.class.getSimpleName() + "INTENT_EXTRA_COLOR_LABEL";

    private float mLastTouchX = -1f, mLastTouchY = -1f;
    private WatchFaceState mWatchFaceState;
    private SharedPref mSharedPref;
    @StringRes
    private int mNameLabel;

    private int calc(int a, int b, int c) {
        return (a * 16) + (b * 4) + c;
    }

    private static final StringBuilder mStringBuilder = new StringBuilder();

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_selection);
        mWatchFaceState = new WatchFaceState(this);

        ensureLocationPermissions();

        // Try to get the watch face slot from our activity intent.
        String slot = getIntent().getStringExtra(INTENT_EXTRA_SLOT);
        Class<? extends ProWatchFaceService> watchFaceServiceClass;
        @StringRes final int titleLabel;
        if (slot == null) {
            // Default: A
            watchFaceServiceClass = ProWatchFaceService.A.class;
            titleLabel = R.string.watch_face_service_label_a;
        } else if (slot.equals(ProWatchFaceService.B.class.getName())) {
            watchFaceServiceClass = ProWatchFaceService.B.class;
            titleLabel = R.string.watch_face_service_label_b;
        } else if (slot.equals(ProWatchFaceService.C.class.getName())) {
            watchFaceServiceClass = ProWatchFaceService.C.class;
            titleLabel = R.string.watch_face_service_label_c;
        } else if (slot.equals(ProWatchFaceService.D.class.getName())) {
            watchFaceServiceClass = ProWatchFaceService.D.class;
            titleLabel = R.string.watch_face_service_label_d;
        } else {
            watchFaceServiceClass = ProWatchFaceService.A.class;
            titleLabel = R.string.watch_face_service_label_a;
        }

        mNameLabel = getIntent().getIntExtra(INTENT_EXTRA_COLOR_LABEL, -1);

        final TextView labelTextView = findViewById(R.id.config_item_textview_widget);
        mStringBuilder.setLength(0);
        mStringBuilder.append(getString(titleLabel));
        if (mNameLabel != -1) {
            mStringBuilder.append("<br>");
            mStringBuilder.append(getString(mNameLabel));
        }
        labelTextView.setText(
                Html.fromHtml(mStringBuilder.toString(), Html.FROM_HTML_MODE_LEGACY));

        // Reload our current WatchFacePreset.
        // So we can get our currently selected color.
        mSharedPref = new SharedPref(this, watchFaceServiceClass);

        mWatchFaceState.setString(mSharedPref.getWatchFaceStateString());

        final int[] row1 = new int[]{
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

        final int[] row2 = new int[]{
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

        final int[] row3 = new int[]{
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

        final int[] row4 = new int[]{
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

        final int[] row5 = new int[]{
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

        final int[] row6 = new int[]{
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

        final int[][] mRows = new int[][]{row1, row2, row3, row4, row5, row6};
        final RectF[][] mRectFs = new RectF[][]{
                new RectF[row1.length],
                new RectF[row2.length],
                new RectF[row3.length],
                new RectF[row4.length],
                new RectF[row5.length],
                new RectF[row6.length]
        };

        ImageView colorImageView = findViewById(R.id.color);
        // Set layer type to hardware. We promise not to update this any more,
        // so now Android can render this to a texture and leave it there.
        colorImageView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        colorImageView.setImageDrawable(new Drawable() {
            @Override
            public void draw(@NonNull Canvas canvas) {
                @ColorInt int currentColor;
                {
                    String sharedPrefString = getIntent().getStringExtra(INTENT_EXTRA_COLOR);
                    PaintBox.ColorType colorType =
                            PaintBox.ColorType.valueOf(sharedPrefString);

                    currentColor = mWatchFaceState.getColor(colorType);
                }

                RectF bounds = new RectF(getBounds());
                float pc = 0.01f * Math.min(bounds.height(), bounds.width());

                Paint b = new Paint();
                b.setColor(Color.RED);
                b.setStyle(Paint.Style.STROKE);
                b.setAntiAlias(true);
                b.setStrokeWidth(1.0f);

                Paint o = new Paint();
                o.setStyle(Paint.Style.FILL);
                o.setAntiAlias(true);

                Paint p = new Paint();
                p.setStyle(Paint.Style.FILL);
                p.setAntiAlias(true);

                float spanRoot3 = bounds.width() / (mRows.length + 1f);
                float span = spanRoot3 / 0.86602540378f; // sqrt(3) / 2

                float radius = spanRoot3 * 0.45f;
                float cx = 0f;
//                android.util.Log.d("ColorSelectionActivity", String.format(
//                        "<svg width=\"%f\" height=\"%f\">",
//                        bounds.width(), bounds.height()));

                for (int i = 0; i < mRows.length; i++) {
                    cx += spanRoot3;
                    // 0 for even cols, vertical offset for odd
                    float cy = (i % 2 == 0) ? span * 0.5f : span /* * 1.0f */;
                    for (int j = 0; j < mRows[i].length; j++) {
                        RectF r = new RectF(cx - (1.5f * spanRoot3 / 2f),
                                cy - (span / 2f),
                                cx + (1.5f * spanRoot3 / 2f),
                                cy + (span / 2f));
//                        canvas.drawRect(r, b);
                        if (mRows[i][j] != -1) {
                            mRectFs[i][j] = r;

                            // For our current selection, make our circle slightly larger.
                            float radius2 = radius;
                            if (mWatchFaceState.getPaintBox().getColor(mRows[i][j]) == currentColor) {
                                radius2 *= 1.333333f;
                            }

                            // Draw our bevels as follows:
                            // Draw a white circle offset -0.2%, -0.2%
                            o.setColor(Color.WHITE);
                            canvas.drawCircle(cx - 0.2f * pc, cy - 0.2f * pc, radius2, o);
//                            android.util.Log.d("ColorSelectionActivity", String.format(
//                                    "<circle cx=\"%f\" cy=\"%f\" r=\"%f\" fill=\"#%06X\" />",
//                                    cx - 0.2f * pc, cy - 0.2f * pc, radius,
//                                    (0xFFFFFF & o.getColor())));

                            // Draw a dark gray circle offset +0.2%, +0.2%
                            o.setColor(Color.DKGRAY);
                            canvas.drawCircle(cx + 0.2f * pc, cy + 0.2f * pc, radius2, o);
//                            android.util.Log.d("ColorSelectionActivity", String.format(
//                                    "<circle cx=\"%f\" cy=\"%f\" r=\"%f\" fill=\"#%06X\" />",
//                                    cx + 0.2f * pc, cy + 0.2f, radius,
//                                    (0xFFFFFF & o.getColor())));

                            // Now draw our swatch.
                            p.setColor(mWatchFaceState.getPaintBox().getColor(mRows[i][j]));
                            canvas.drawCircle(cx, cy, radius2, p);
//                            android.util.Log.d("ColorSelectionActivity", String.format(
//                                    "<circle cx=\"%f\" cy=\"%f\" r=\"%f\" fill=\"#%06X\" />",
//                                    cx, cy, radius,
//                                    (0xFFFFFF & p.getColor())));
                        }
                        cy += span;
                    }
                }
//                android.util.Log.d("ColorSelectionActivity", "</svg>");
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

        colorImageView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                mLastTouchX = event.getX();
                mLastTouchY = event.getY();
            }
            return false;
        });

        colorImageView.setOnClickListener(v -> {
            // Just do a linear search through all 64 colors.
            // Not necessarily quick, but probably quick enough.
            boolean foundPrimary = false, foundSecondary = false;
            int iPrimary = -1, jPrimary = -1;
            int iSecondary = -1, jSecondary = -1;

            // Loop through each rect, checking for a hit against each rect.
            // Each click could potentially hit one or two mRectFs, so store
            // hits in primary and secondary co-ordinates.
            outerLoop:
            for (int i = 0; i < mRectFs.length; i++) {
                for (int j = 0; j < mRectFs[i].length; j++) {
                    if (mRectFs[i][j] != null && mRectFs[i][j].contains(mLastTouchX, mLastTouchY)) {
                        if (!foundPrimary) {
                            foundPrimary = true;
                            iPrimary = i;
                            jPrimary = j;
                        } else {
                            iSecondary = i;
                            jSecondary = j;
                            foundSecondary = true;
                            break outerLoop;
                        }
                    }
                }
            }

            if (foundPrimary && foundSecondary) {
                // Find whether the touch is closer to primary or secondary.
                float primaryX = mRectFs[iPrimary][jPrimary].centerX();
                float primaryY = mRectFs[iPrimary][jPrimary].centerY();
                float secondaryX = mRectFs[iSecondary][jSecondary].centerX();
                float secondaryY = mRectFs[iSecondary][jSecondary].centerY();

                float primaryDistance = Math.abs(mLastTouchX - primaryX) +
                        Math.abs(mLastTouchY - primaryY);
                float secondaryDistance = Math.abs(mLastTouchX - secondaryX) +
                        Math.abs(mLastTouchY - secondaryY);

                // So, which is closer? Set that.
                if (primaryDistance < secondaryDistance) {
                    setSixBitColor(mRows[iPrimary][jPrimary]);
                } else {
                    setSixBitColor(mRows[iSecondary][jSecondary]);
                }
            } else if (foundPrimary) {
                // No secondary. Hit right in the middle of the primary. Just set based on that.
                setSixBitColor(mRows[iPrimary][jPrimary]);
            }
        });
    }

    /**
     * Save the given color to preferences. We extract the WatchFacePreset held in preferences,
     * then change the color type pre-stored in INTENT_EXTRA_COLOR, then save the WatchFacePreset
     * back to preferences.
     *
     * @param sixBitColor New 6-bit color to set (between 0 and 63)
     */
    private void setSixBitColor(int sixBitColor) {
        String sharedPrefString = getIntent().getStringExtra(INTENT_EXTRA_COLOR);
        PaintBox.ColorType colorType = PaintBox.ColorType.valueOf(sharedPrefString);

        mWatchFaceState.setSixBitColor(colorType, sixBitColor);

        mSharedPref.putWatchFaceStateString(mWatchFaceState.getString());

        // Lets Complication Config Activity know there was an update to colors.
        setResult(Activity.RESULT_OK);

        // Show a toast popup with the color we just selected.
        String toastText = mNameLabel != -1 ? getString(mNameLabel) : "???\nColor";
        toastText = toastText.replace('\n', ' ') +
                ":\n" + mWatchFaceState.getPaintBox().getColorName(sixBitColor);
        Toaster.makeText(this, toastText, Toaster.LENGTH_LONG);

        finish();
    }

    /**
     * Request/return code for requesting coarse location permissions.
     */
    private final static int PERMISSION_ACCESS_COARSE_LOCATION = 0;

    /**
     * Request array for requesting coarse location permissions.
     */
    private final static String[] mPermissionsRequestArray =
            new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION};

    /**
     * A wedge of code here. If we're looking for AMBIENT_NIGHT, make sure that we have
     * coarse location permissions. If we don't, pop an alert dialog with an explanation of
     * why we need these permissions. If the user accepts the explanation, request the
     * permissions themselves.
     */
    private void ensureLocationPermissions() {
        // Only for AMBIENT_NIGHT. For anything else, return.
        String sharedPrefString = getIntent().getStringExtra(INTENT_EXTRA_COLOR);
        PaintBox.ColorType colorType = PaintBox.ColorType.valueOf(sharedPrefString);
        if (!colorType.equals(PaintBox.ColorType.AMBIENT_NIGHT)) {
            return;
        }

        // Check if we have the permission or not.
        if (checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            // We don't have it. Pop a quick-and-dirty alert dialog with an explanation
            // for why we need it. Dark theme: android.R.style.Theme_DeviceDefault_Dialog_Alert
            new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_Alert)
                    .setIcon(android.R.drawable.ic_dialog_info)
                    .setTitle(R.string.config_ambient_night_color_label)
                    .setMessage(R.string.config_request_location_permissions_message)
                    // User said "OK" to our explanation, so request the permission.
                    .setPositiveButton(android.R.string.yes, (dialog, which) -> requestPermissions(
                            mPermissionsRequestArray, PERMISSION_ACCESS_COARSE_LOCATION))
                    // User said "Cancel" to our explanation, so finish this activity early.
                    .setNegativeButton(android.R.string.no, (dialog, which) -> finish())
                    // User swiped away our explanation, so finish this activity early.
                    .setOnCancelListener(dialog -> finish())
                    .show();
        }
    }

    /**
     * A callback for when the user decides to grant our permission or not.
     *
     * @param requestCode  The request code passed in
     * @param permissions  The requested permissions
     * @param grantResults The grant results for the corresponding permissions
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_ACCESS_COARSE_LOCATION) {
            if (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                // Permission denied. End this activity.
                finish();
            }
            // Permission granted. Keep going!
        }
    }
}
