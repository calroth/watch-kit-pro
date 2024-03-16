/*
 * Copyright (C) 2018-2024 Terence Tan
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
import android.graphics.RectF;
import android.graphics.drawable.Icon;
import android.support.wearable.watchface.decomposition.ImageComponent;
import android.support.wearable.watchface.decomposition.WatchFaceDecomposition;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

class LegacyWatchPartHandsDrawable extends WatchPartHandsDrawable
        implements LegacyWatchFaceGlobalDrawable.WatchFaceDecompositionComponent {
    public LegacyWatchPartHandsDrawable(HandType handType) {
        super(handType);
    }

    /**
     * If we're rendering a decomposition, it's rendered here.
     */
    @Nullable
    private Bitmap mDecompositionSourceBitmap;

    /**
     * If we're rendering a decomposition, it's rendered into
     * "mDecompositionSourceBitmap", then mapped to 16 colors here.
     */
    @Nullable
    private Bitmap mDecompositionDestBitmap;

    /**
     * If we're rendering a decomposition, we draw here, which
     * renders into "mDecompositionDestBitmap".
     */
    @Nullable
    private Canvas mDecompositionSourceCanvas;

    /**
     * The bounds of the path of this hand.
     */
    private final RectF mHandAmbientPathBounds = new RectF();

    /**
     * The bounds of the path of this hand, as a proportion of the full watchface.
     */
    private final RectF mHandAmbientPathProportion = new RectF();

    /**
     * Build this watch face decomposition component into "builder".
     *
     * @param builder WatchFaceDecomposition builder to build into.
     * @param idA     AtomicInteger for the component ID, which we will increment
     * @return The time at which this component expires, at which point (or before),
     * call this again
     */
    @Override
    public long buildWatchFaceDecompositionComponents(
            @NonNull WatchFaceDecomposition.Builder builder, @NonNull AtomicInteger idA) {
        int baseId = idA.getAndIncrement();
        // Regenerate the hand path if required.
        getHandPath();

        // Regenerate the decomposition if our ambient tint color has changed.
        // This happens regularly during dusk and dawn.
        if (mCurrentAmbientTint != mWatchFaceState.getAmbientTint()) {
            regenerateDecomposition();
        }

        ImageComponent.Builder iBuilder = new ImageComponent.Builder();
        iBuilder.setComponentId(baseId).setZOrder(baseId);

        // OK, set the builder accordingly. Start by setting our bitmap...
        iBuilder.setImage(Icon.createWithBitmap(mDecompositionDestBitmap));

        // The bounds is the proportion of the entire watch face that this hand takes up.
        float entireWidth = (float) (getBounds().width());
        float entireHeight = (float) (getBounds().height());
        mHandAmbientPathProportion.set(mHandAmbientPathBounds.left / entireWidth,
                mHandAmbientPathBounds.top / entireHeight,
                mHandAmbientPathBounds.right / entireWidth,
                mHandAmbientPathBounds.bottom / entireHeight);
        iBuilder.setBounds(mHandAmbientPathProportion);

        // Set rotation for the hand.
        iBuilder.setDegreesPerDay(getDegreesPerDay());
        if (getDegreesPerDay() > 360f * 2f * 12f) {
            // For seconds hands, set degrees per step.
            iBuilder.setDegreesPerStep(6f); // 1fps
        }

        builder.addImageComponents(iBuilder.build());

        // This doesn't need updating on a schedule.
        return Long.MAX_VALUE;
    }

    /**
     * Regenerate the decomposition bitmap "mDecompositionDestBitmap" if anything has
     * changed with the hands, such as its shape, size, or dusk/dawn tint.
     */
    @Override
    void regenerateDecomposition() {
        // Compute the bounds for the path, make sure we don't render more than we need.
        mHandAmbientPath.computeBounds(mHandAmbientPathBounds, true);
        // Round "mHandAmbientPathBounds" height and width up to an alignment of 4.
        float outsetX = mHandAmbientPathBounds.width() -
                (float) Math.ceil(mHandAmbientPathBounds.width() / 4f) * 4f;
        float outsetY = mHandAmbientPathBounds.height() -
                (float) Math.ceil(mHandAmbientPathBounds.height() / 4f) * 4f;

        mHandAmbientPathBounds.inset(outsetX / 2f, outsetY / 2f); // Round fractions up; adds padding.

        int bitmapWidth = (int) mHandAmbientPathBounds.width();
        int bitmapHeight = (int) mHandAmbientPathBounds.height();

        // Render into a new bitmap. Use a Canvas to draw the Path.
        if (mDecompositionSourceBitmap == null ||
                mDecompositionSourceCanvas == null ||
                mDecompositionDestBitmap == null ||
                mDecompositionSourceBitmap.getWidth() != bitmapWidth ||
                mDecompositionSourceBitmap.getHeight() != bitmapHeight) {
            // Initialise our bitmaps and canvas on first use.
            mDecompositionSourceBitmap =
                    Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888);
            mDecompositionSourceCanvas = new Canvas(mDecompositionSourceBitmap);
            // Translate, to ensure our painting happens within bounds.
            mDecompositionSourceCanvas.translate(
                    -mHandAmbientPathBounds.left,
                    -mHandAmbientPathBounds.top);
            mDecompositionDestBitmap =
                    Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888);
        } else {
            // Re-use the canvas, but clear it first.
            mDecompositionSourceCanvas.drawColor(Color.BLACK);
        }
        assert mDecompositionSourceBitmap != null;
        assert mDecompositionDestBitmap != null;
        assert mDecompositionSourceCanvas != null;

        // Draw our hand!

        mDecompositionSourceCanvas.drawPath(mHandAmbientPath, getAmbientPaint());

        // Reduce our bit depth to 16 levels, which is what decomposable bitmaps require.
        mWatchFaceState.getPaintBox().mapBitmapWith8LevelsFromTransparent(
                mWatchFaceState.getAmbientTint(),
                mDecompositionSourceBitmap, mDecompositionDestBitmap);

        // Save a copy of the ambient tint we used to draw this.
        mCurrentAmbientTint = mWatchFaceState.getAmbientTint();
    }

    /**
     * Does this WatchFaceDecomposition component have an update available? We ask because if
     * there are no updates available, we want to avoid sending updates to the offload
     * processor.
     *
     * @param currentTimeMillis The time when we're asking if there's an update available
     * @return Whether the update is available?
     */
    @Override
    public boolean hasDecompositionUpdateAvailable(long currentTimeMillis) {
        int currentSerial = Objects.hash(mWatchFaceState);
        return mPreviousSerial != currentSerial;
    }
}
