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
    private final Path mExclusionPath = new Path();
    private final Path mTempPath = new Path();
    private final Matrix mTempPathMatrix = new Matrix();
    private final Rect mLabelRect = new Rect();

    @Override
    public void draw2(@NonNull Canvas canvas) {
        if (mWatchFaceState.isDeveloperMode() && mWatchFaceState.isHideTicks()) {
            // If we set developer mode "hide ticks", then just return!
            return;
        }

        if (mWatchFaceState.getDigitDisplay() == DigitDisplay.NONE) {
            return;
        }

        final String[] labels = mWatchFaceState.getDigitFormatLabels();
        final Style style = mWatchFaceState.getDigitStyle();
        final Paint paint = mWatchFaceState.getPaintBox().getPaintFromPreset(style);

        final float digitBandStart = mWatchFaceState.getDigitBandStart(pc);
        final float digitBandHeight = 0.5f * (mWatchFaceState.getDigitDisplay() == DigitDisplay.OVER ?
                mWatchFaceState.getTickBandHeight(pc) : mWatchFaceState.getDigitBandHeight(pc));
        final float digitLocation = 50f - (digitBandStart + digitBandHeight);

        if (hasStateChanged(labels, style, paint, digitBandStart, digitBandHeight, digitLocation)) {
            mPath.reset();
            mExclusionPath.reset();
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

                // Trigonometry ahoy!
                mTempPathMatrix.reset();
                float degrees = ((float) i / 12f) * 360f;
                double radians = ((double) i / 12d) * 2d * Math.PI;
                // Rotate the label if necessary.
                if (mWatchFaceState.getDigitRotation() == DigitRotation.CURVED) {
                    if ((mWatchFaceState.getDigitFormat() == DigitFormat.NUMERALS_12_4 ||
                            mWatchFaceState.getDigitFormat() == DigitFormat.NUMERALS_12_12 ||
                            mWatchFaceState.getDigitFormat() == DigitFormat.CIRCLED ||
                            mWatchFaceState.getDigitFormat() == DigitFormat.NEGATIVE_CIRCLED ||
                            mWatchFaceState.getDigitFormat() == DigitFormat.DOUBLE_STRUCK) &&
                            i >= 4 && i <= 8) {
                        // For the numeric label types, draw the labels 4-8 "upside down"...
                        // Essentially to disambiguate labels "6" and "9".
                        mTempPathMatrix.setRotate(degrees + 180f, mCenterX, mCenterY);
                    } else {
                        // Right way up
                        mTempPathMatrix.setRotate(degrees, mCenterX, mCenterY);
                    }
                }

                // Now, draw to a temporary path, and union that to our main path!
                mTempPath.reset();
                paint.getTextPath(label, 0, label.length(), x, y, mTempPath);
                // Spread the labels out in a circle, digitLocation% from the centre!
                mTempPathMatrix.postTranslate((float) Math.sin(radians) * digitLocation * pc,
                        0f - (float) Math.cos(radians) * digitLocation * pc);
                mTempPath.transform(mTempPathMatrix);
                mPath.op(mTempPath, Path.Op.UNION);

                // Now do it again, but this time make it a rectangle with the label's bounds.
                // This will form our exclusion path.
                mTempPath.reset();
                if (mWatchFaceState.getDigitFormat() == DigitFormat.CIRCLED ||
                        mWatchFaceState.getDigitFormat() == DigitFormat.NEGATIVE_CIRCLED) {
                    // The exclusion path is a circle!
                    // Make the radius the height of the glyph plus 1%.
                    float radius = (float) mLabelRect.height() / 2f + (1f * pc);
                    // Adjust y to consider aligned centre.
                    y += (float) (mLabelRect.top + mLabelRect.bottom) / 2f;
                    mTempPath.addCircle(x, y, radius, getDirection());
                } else {
                    // The exclusion path is a (round) rectangle!
                    // Make the size of the round rect,
                    // mLabelRect plus an extra padding of 1% all sides.
                    // Round rect radius of 1%.
                    // Adjust x to consider aligned centre.
                    x -= (float) (mLabelRect.left + mLabelRect.right) / 2f;
                    mTempPath.addRoundRect((float) mLabelRect.left - (1f * pc) + x,
                            (float) mLabelRect.top - (1f * pc) + y,
                            (float) mLabelRect.right + (1f * pc) + x,
                            (float) mLabelRect.bottom + (1f * pc) + y,
                            pc, pc, getDirection());
                }
                mTempPath.transform(mTempPathMatrix); // Transform it with the same matrix!
                mExclusionPath.op(mTempPath, Path.Op.UNION);
            }
        }

        // Draw it!
        drawPath(canvas, mPath, paint);

        // Add to exclusion path so it punches out of everything above it.
        addExclusionPath(mExclusionPath, Path.Op.DIFFERENCE);
    }
}
