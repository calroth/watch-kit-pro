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
 */

package pro.watchkit.wearable.watchface.watchface;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;

import androidx.annotation.NonNull;

import pro.watchkit.wearable.watchface.model.BytePackable.DigitDisplay;
import pro.watchkit.wearable.watchface.model.BytePackable.DigitFormat;
import pro.watchkit.wearable.watchface.model.BytePackable.DigitRotation;
import pro.watchkit.wearable.watchface.model.BytePackable.Style;

final class WatchPartDigitsDrawable extends WatchPartDrawable {
    @Override
    String getStatsName() {
        return "Dig";
    }

    private final Path mPath = new Path();
    private final Path mTempPath = new Path();
    private final Matrix mTempPathMatrix = new Matrix();
    private final Rect mLabelRect = new Rect();

    @Override
    public void draw2(@NonNull Canvas canvas) {
        if (mWatchFaceState.getDigitDisplay() == DigitDisplay.NONE) {
            return;
        }

        String[] labels = mWatchFaceState.getDigitFormatLabels();
        Style style = mWatchFaceState.getDigitStyle();
        Paint paint = mWatchFaceState.getPaintBox().getPaintFromPreset(style);

        // Save various attributes of the paint before we temporarily overwrite them...
        float originalTextSize = paint.getTextSize();
        paint.setTextSize(10f * pc);
        Paint.Align originalTextAlign = paint.getTextAlign();
        paint.setTextAlign(Paint.Align.CENTER);

        mPath.reset();
        for (int i = 0; i < 12; i++) {
            String label = labels[i];
            if (label == null || label.length() == 0) {
                // Don't worry about empty labels...
                continue;
            }

            // Get the size (bounds) of the label we're trying to draw.
            paint.getTextBounds(label, 0, label.length(), mLabelRect);

            // Calculate the location we want to draw.
            // Our calculations consider the "origin" to be the centre of the bounds.
            float x = 0f;// - (float)(mLabelRect.left + mLabelRect.right) / 2f;
            float y = 0f - (float) (mLabelRect.top + mLabelRect.bottom) / 2f;

            // Offset x and y to be relative to the centre of the canvas.
            x += mCenterX;
            y += mCenterY;

            // Now, draw to a temporary path, and union that to our main path!
            mTempPath.reset();
            paint.getTextPath(label, 0, label.length(), x, y, mTempPath);

            mTempPathMatrix.reset();
            // Trigonometry ahoy!
            float degrees = ((float) i / 12f) * 360f;
            double radians = ((double) i / 12d) * 2d * Math.PI;
            // Rotate the label if necessary.
            if (mWatchFaceState.getDigitRotation() == DigitRotation.CURVED) {
                if ((mWatchFaceState.getDigitFormat() == DigitFormat.NUMERALS_12_4 ||
                        mWatchFaceState.getDigitFormat() == DigitFormat.NUMERALS_12_12) &&
                        i >= 4 && i <= 8) {
                    // For the numeric label types, draw the labels 4-8 "upside down"...
                    // Essentially to disambiguate labels "6" and "9".
                    mTempPathMatrix.setRotate(degrees + 180f, mCenterX, mCenterY);
                } else {
                    // Right way up
                    mTempPathMatrix.setRotate(degrees, mCenterX, mCenterY);
                }
            }
            // Spread the labels out in a circle, 37.5% from the centre!
            mTempPathMatrix.postTranslate((float) Math.sin(radians) * 37.5f * pc,
                    0f - (float) Math.cos(radians) * 37.5f * pc);
            mTempPath.transform(mTempPathMatrix);

            mPath.op(mTempPath, Path.Op.UNION);
        }

        // Restore the paint's attributes.
        paint.setTextSize(originalTextSize);
        paint.setTextAlign(originalTextAlign);

        // Draw it!
        drawPath(canvas, mPath, paint);
    }
}
