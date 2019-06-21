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

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;

import androidx.annotation.NonNull;

import pro.watchkit.wearable.watchface.model.BytePackable.Style;

final class WatchPartSwatchDrawable extends WatchPartDrawable {
    @NonNull
    private Path mPath = new Path();

    @Override
    String getStatsName() {
        return "Swt";
    }

    @Override
    public void draw2(@NonNull Canvas canvas) {
        // Reset our exclusion path totally. We'll draw indiscriminately over everything!
        resetExclusionPathTotally();

        mPath.reset();
        mPath.addCircle(mCenterX * 1.6666f, mCenterY * 0.4166f, mCenterX * 0.3333f,
                getDirection());

        Style swatchStyle = mWatchFaceState.getSwatchStyle();
        Paint paint = mWatchFaceState.getPaintBox().getPaintFromPreset(swatchStyle);

        Shader shader = paint.getShader();
        Matrix originalMatrix = new Matrix();
        if (shader != null) {
            // Save the original matrix.
            shader.getLocalMatrix(originalMatrix);

            // Create a new matrix which is a translation of the original.
            Matrix translateMatrix = new Matrix(originalMatrix);
            // Shift it a distance so the swatch's representation is closer to the centre.
            translateMatrix.postTranslate(mCenterX * 0.3333f, -mCenterY * 0.3333f);
            shader.setLocalMatrix(translateMatrix);
        }

        // Draw our swatch!
        drawPath(canvas, mPath, paint);

        // Reset the shader matrix to leave things in their original state.
        if (shader != null) {
            shader.setLocalMatrix(originalMatrix);
        }
    }
}