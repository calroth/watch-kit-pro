/*
 * Copyright (C) 2019-2024 Terence Tan
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
import android.graphics.RectF;
import android.graphics.drawable.Icon;
import android.support.wearable.watchface.decomposition.ImageComponent;
import android.support.wearable.watchface.decomposition.WatchFaceDecomposition;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.concurrent.atomic.AtomicInteger;

import pro.watchkit.wearable.watchface.model.WatchFaceState;

public class LegacyWatchFaceGlobalCacheDrawable extends WatchFaceGlobalCacheDrawable
        implements LegacyWatchFaceGlobalDrawable.WatchFaceDecompositionComponent {
    LegacyWatchFaceGlobalCacheDrawable(int flags) {
        super(LegacyWatchFaceGlobalDrawable.buildDrawables(null, flags));
    }

    /**
     * If we're rendering a decomposition, it's rendered here.
     */
    @Nullable
    private Bitmap mDecompositionIntermediateBitmap;

    /**
     * If we're rendering a decomposition, it's rendered into
     * "mDecompositionIntermediateBitmap", then mapped to 16 colors here.
     */
    @Nullable
    private Bitmap mDecompositionDestBitmap;

    /**
     * If we're rendering a decomposition, we draw here, which
     * renders into "mDecompositionDestBitmap".
     */
    @Nullable
    private Canvas mDecompositionIntermediateCanvas;

    /**
     * A private copy of our current ambient tint. Useful for caching.
     */
    @ColorInt
    private int mCurrentAmbientTint;

    /**
     * Build the watch face decomposition into "builder". In this case, it'll be our cache
     * drawables and (also) our non-time-dependent complications.
     *
     * @param builder WatchFaceDecomposition builder to build into.
     * @param idA     AtomicInteger for the component ID, which we will increment
     * @return The time at which this decomposition expires, at which point, call this again
     */
    @Override
    public long buildWatchFaceDecompositionComponents(
            @NonNull WatchFaceDecomposition.Builder builder, @NonNull AtomicInteger idA) {
        // Our generated decomposition image goes into mDecompositionDestBitmap.
        // Regenerate it if there are updates available? Skip if unnecessary.
        if (hasDecompositionUpdateAvailable(mWatchFaceState.getTimeInMillis())) {
            // Copy "mAmbientCacheBitmap" into an intermediate Bitmap we can draw all over.
            if (mDecompositionIntermediateBitmap == null ||
                    mDecompositionDestBitmap == null ||
                    mDecompositionIntermediateCanvas == null ||
                    mDecompositionDestBitmap.getWidth() != mAmbientCacheBitmap.getWidth() ||
                    mDecompositionDestBitmap.getHeight() != mAmbientCacheBitmap.getHeight()) {
                if (mDecompositionIntermediateBitmap != null) {
                    mDecompositionIntermediateBitmap.recycle(); // If there's an old bitmap, recycle it.
                }
                if (mDecompositionDestBitmap != null) {
                    mDecompositionDestBitmap.recycle(); // Ditto.
                }
                mDecompositionDestBitmap = Bitmap.createBitmap(
                        mAmbientCacheBitmap.getWidth(), mAmbientCacheBitmap.getHeight(),
                        Bitmap.Config.ARGB_8888);
                mDecompositionIntermediateBitmap = Bitmap.createBitmap(
                        mAmbientCacheBitmap.getWidth(), mAmbientCacheBitmap.getHeight(),
                        Bitmap.Config.ARGB_8888);
                mDecompositionIntermediateCanvas = new Canvas(mDecompositionIntermediateBitmap);
            }
            assert mDecompositionIntermediateBitmap != null;
            assert mDecompositionDestBitmap != null;
            assert mDecompositionIntermediateCanvas != null;

            // Copy the existing ambient bitmap into "mDecompositionDestBitmap2".
            mDecompositionIntermediateCanvas.drawBitmap(mAmbientCacheBitmap, 0, 0, null);

            // Hack all our existing (non-time-dependent) complications in.
            boolean wasAmbient = mWatchFaceState.isAmbient();
            mWatchFaceState.setAmbient(true);
            // Just draw each (non-time-dependent) complication straight over the top.
            ((WatchFaceState)mWatchFaceState).getComplicationsForDrawing(getBounds())
                    .stream().filter(c -> c.isForeground && !c.isTimeDependent())
                    .forEach(c -> c.drawAmbientCache(mDecompositionIntermediateCanvas));
            mWatchFaceState.setAmbient(wasAmbient);

            // Fast map from "mDecompositionIntermediateBitmap" to "mDecompositionDestBitmap".
            // Using RenderScript!
            mWatchFaceState.getPaintBox().mapBitmapWith8LevelsFromBlack(
                    mWatchFaceState.getAmbientTint(), mDecompositionIntermediateBitmap,
                    mDecompositionDestBitmap);

            // Save a copy of the ambient tint we used to draw this,
            // so next time we won't (necessarily) run this path.
            mCurrentAmbientTint = mWatchFaceState.getAmbientTint();

            // Note that we've drawn the updated "mAmbientCacheBitmap",
            // so next time we won't (necessarily) run this path.
            mIsAmbientCacheBitmapDirty = false;
        }

        int baseId = idA.getAndIncrement();

        // OK, build and add our ImageComponent to the decomposition.
        builder.addImageComponents(new ImageComponent.Builder()
                .setComponentId(baseId)
                .setZOrder(baseId)
                .setImage(Icon.createWithBitmap(mDecompositionDestBitmap))
                .setBounds(new RectF(0f, 0f, 1f, 1f)) // Entire screen
                .build());

        // This doesn't need updating on a schedule.
        return Long.MAX_VALUE;
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
        // Are there any updated non-time-dependent complications?
        boolean hasUpdatedComplicationData = ((WatchFaceState)mWatchFaceState).getComplications().stream()
                .filter(c -> c.isForeground && !c.isTimeDependent())
                .map(c -> c.checkUpdatedComplicationData(currentTimeMillis))
                .reduce(false, (a, b) -> a || b);

        // Regenerate our cache bitmaps; if there's nothing to do, this returns quickly.
        regenerateCacheBitmaps();

        // Regenerate the decomposition if our (non-time-dependent) complications have changed.
        // Regenerate the decomposition if the ambient cache bitmap (ticks & digits) has changed.
        // Regenerate the decomposition if our ambient tint color has changed (this happens
        // regularly during dusk and dawn).
        // Regenerate the decomposition if we've never drawn it before!
        return hasUpdatedComplicationData || mIsAmbientCacheBitmapDirty ||
                mCurrentAmbientTint != mWatchFaceState.getAmbientTint() ||
                mDecompositionDestBitmap == null;
    }
}
