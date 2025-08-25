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
 */

package pro.watchkit.wearable.watchface.watchface;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.os.SystemClock;

import androidx.annotation.NonNull;

import java.io.FileOutputStream;
import java.io.IOException;

import pro.watchkit.wearable.watchface.model.BytePackable;
import pro.watchkit.wearable.watchface.util.SharedPref;

final class WatchPartBackgroundDrawable extends WatchPartDrawable {
    @NonNull
    @Override
    String getStatsName() {
        return "Bg";
    }

    @Override
    public void draw2(@NonNull Canvas canvas) {
        // As the bottom-most layer of the draw stack, reset our exclusion paths now.
        resetExclusionPath();

        if (mWatchFaceState.isAmbient()) {
            canvas.drawColor(Color.BLACK);
        } else if (mWatchFaceState.isTransparentBackground()) {
            canvas.drawColor(Color.TRANSPARENT); // Probably doesn't do anything?
        } else {
            canvas.drawPaint(mWatchFaceState.getPaintBox().getPaintFromPreset(
                    mWatchFaceState.getBackgroundMaterial()));
        }

        Rect bounds = canvas.getClipBounds();
        if (SharedPref.mWriteLayersToDisk && bounds.width() != 0 && bounds.height() != 0) {
            Bitmap writeBitmap = Bitmap.createBitmap(
                    bounds.height(), bounds.width(), Bitmap.Config.ARGB_8888);
            Canvas writeCanvas = new Canvas(writeBitmap);

            {
                // Clear out "mWriteCanvas".
                writeCanvas.drawColor(Color.BLACK, PorterDuff.Mode.CLEAR);
                writeCanvas.drawPaint(mWatchFaceState.getPaintBox().getPaintFromPreset(BytePackable.Material.FILL_HIGHLIGHT));

                try {
                    FileOutputStream out = mWatchFaceState.openFileOutput(
                            SystemClock.elapsedRealtimeNanos() +
                                    "-Fill-Highlight.png");
                    writeBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            {
                // Clear out "mWriteCanvas".
                writeCanvas.drawColor(Color.BLACK, PorterDuff.Mode.CLEAR);
                writeCanvas.drawPaint(mWatchFaceState.getPaintBox().getPaintFromPreset(BytePackable.Material.BASE_ACCENT));

                try {
                    FileOutputStream out = mWatchFaceState.openFileOutput(
                            SystemClock.elapsedRealtimeNanos() +
                                    "-Base-Accent.png");
                    writeBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            {
                // Clear out "mWriteCanvas".
                writeCanvas.drawColor(Color.BLACK, PorterDuff.Mode.CLEAR);
                writeCanvas.drawPaint(mWatchFaceState.getPaintBox().getPaintFromPreset(BytePackable.Material.ACCENT_HIGHLIGHT));

                try {
                    FileOutputStream out = mWatchFaceState.openFileOutput(
                            SystemClock.elapsedRealtimeNanos() +
                                    "-Accent-Highlight.png");
                    writeBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            {
                // Clear out "mWriteCanvas".
                writeCanvas.drawColor(Color.BLACK, PorterDuff.Mode.CLEAR);
                writeCanvas.drawPaint(mWatchFaceState.getPaintBox().getPaintFromPreset(BytePackable.Material.ACCENT_FILL));

                try {
                    FileOutputStream out = mWatchFaceState.openFileOutput(
                            SystemClock.elapsedRealtimeNanos() +
                                    "-Accent-Fill.png");
                    writeBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            {
                // Clear out "mWriteCanvas".
                writeCanvas.drawColor(Color.BLACK, PorterDuff.Mode.CLEAR);
                writeCanvas.drawPaint(mWatchFaceState.getPaintBox().getBezelPaint2());

                try {
                    FileOutputStream out = mWatchFaceState.openFileOutput(
                            SystemClock.elapsedRealtimeNanos() +
                                    "-Bezel2.png");
                    writeBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            {
                // Clear out "mWriteCanvas".
                writeCanvas.drawColor(Color.BLACK, PorterDuff.Mode.CLEAR);
                writeCanvas.drawPaint(getAmbientPaint());

                try {
                    FileOutputStream out = mWatchFaceState.openFileOutput(
                            SystemClock.elapsedRealtimeNanos() +
                                    "-Ambient.png");
                    writeBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            {
                // Clear out "mWriteCanvas".
                writeCanvas.drawColor(Color.BLACK, PorterDuff.Mode.CLEAR);
                writeCanvas.drawPaint(mWatchFaceState.getPaintBox().getShadowPaint());

                try {
                    FileOutputStream out = mWatchFaceState.openFileOutput(
                            SystemClock.elapsedRealtimeNanos() +
                                    "-Shadow.png");
                    writeBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
