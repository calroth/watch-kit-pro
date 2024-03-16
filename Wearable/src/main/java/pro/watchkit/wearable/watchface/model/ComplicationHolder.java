/*
 * Copyright (C) 2018-2023 Terence Tan
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

package pro.watchkit.wearable.watchface.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.support.wearable.complications.ComplicationData;
import android.support.wearable.complications.rendering.ComplicationDrawable;
import android.support.wearable.watchface.decomposition.FontComponent;
import android.support.wearable.watchface.decomposition.NumberComponent;
import android.support.wearable.watchface.decomposition.WatchFaceDecomposition;
import android.widget.ImageView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Objects;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public final class ComplicationHolder {
    private static int BASE_ID = 99;
    public boolean isForeground = false;
    public boolean isActive = false;
    public ImageView background;
    private final int id;
    @Nullable
    private final ComplicationDrawable mComplicationDrawable;
    private boolean mIsInAmbientMode = false;
    private boolean mLowBitAmbient = false;
    private boolean mInset = false;
    private Rect mBounds, mInsetBounds;
    private Drawable mProviderIconDrawable;

    ComplicationHolder(@Nullable Context context, boolean withComplicationDrawable) {
        id = BASE_ID;
        BASE_ID++;

        if (!withComplicationDrawable) {
            mComplicationDrawable = null;
        } else if (context != null) {
            mComplicationDrawable = new ComplicationDrawable(context);
        } else {
            mComplicationDrawable = new ComplicationDrawable();
        }
    }

    public void setComplicationDrawableCallback(Drawable.Callback cb) {
        if (mComplicationDrawable != null) {
            mComplicationDrawable.setCallback(cb);
        }
    }

    public void setProviderIconDrawable(@NonNull Drawable providerIconDrawable, boolean inset) {
        mProviderIconDrawable = providerIconDrawable;
        mInset = inset;
        if (mInsetBounds != null && inset) {
            mProviderIconDrawable.setBounds(mInsetBounds);
        } else if (mBounds != null) {
            mProviderIconDrawable.setBounds(mBounds);
        }
    }

    public static void resetBaseId() {
        BASE_ID = 99;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ComplicationHolder that = (ComplicationHolder) o;
        return isForeground == that.isForeground &&
                isActive == that.isActive &&
                id == that.id &&
                mIsInAmbientMode == that.mIsInAmbientMode &&
                Objects.equals(mBounds, that.mBounds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(isForeground, isActive, id);
    }

    @NonNull
    @Override
    public String toString() {
        return "Complication " + getId() + (isForeground ? " (foreground)" : " (background)");
    }

    /**
     * We keep our own private copy of our ComplicationData to query its internals.
     */
    @Nullable
    private ComplicationData mComplicationData;

    /**
     * Has the "mComplicationData" object been updated since last we checked?
     * N.B. The object may be updated but its data may be unchanged; we need
     * to check that too.
     */
    private boolean mHasUpdatedComplicationDataObject = false;

    /**
     * Has "mAmbientCacheBitmap2" been updated since last we checked?
     */
    private boolean mIsAmbientCacheBitmap2Dirty = false;

    /**
     * The time of the most recent time-dependent frame we've rendered. Updated
     * when we render a new strip of time-dependent frames.
     */
    private long mLastTimeDependentFrameTime0 = Long.MIN_VALUE;

    /**
     * If we're rendering a time-dependent complication, it's rendered here.
     */
    @Nullable
    private Bitmap mTimeDependentStripBitmap;

    /**
     * If we're rendering a time-dependent complication, it's rendered into
     * "mTimeDependentStripBitmap", then mapped to 16 colors here.
     */
    @Nullable
    private Bitmap mTimeDependentStripDestBitmap;

    /**
     * If we're rendering a time-dependent complication, we draw here, which
     * renders into "mTimeDependentStripBitmap".
     */
    @Nullable
    private Canvas mTimeDependentStripCanvas;

    /**
     * The maximum number of time-dependent complication frames we hold globally across all
     * time-dependent strips. This is a global budget, as memory on the offload co-processor
     * is shared and scarce.
     */
    public static final int TIME_DEPENDENT_STRIP_BUDGET = 17;

    /**
     * The update rate of time-dependent complication frames. Here, it's 1 minute.
     */
    private static final long TIME_DEPENDENT_UPDATE_RATE_MS = TimeUnit.MINUTES.toMillis(1);

    /**
     * Render and construct a time-dependent complication, using a FontComponent and a
     * NumberComponent. Will render the complication multiple times in a strip, one frame
     * for each time interval. Like a film strip.
     *
     * @param builder           WatchFaceDecomposition builder to build into
     * @param bounds            the draw bounds of this complication into the watch face
     * @param currentTimeMillis the current time; the first frame will be relative to this
     * @param maxTimeMillis     the ending time; the last frame will be relative to this
     * @param idA               AtomicInteger for the component ID, which we will increment
     * @param paintBox          PaintBox to use for color selection
     * @param ambientTint       tint to use for dawn and dusk
     */
    public void buildTimeDependentDecomposableComplication(
            @NonNull WatchFaceDecomposition.Builder builder, @NonNull Rect bounds,
            long currentTimeMillis, long maxTimeMillis, @NonNull AtomicInteger idA,
            @NonNull PaintBox paintBox, @ColorInt int ambientTint) {
        if (mComplicationDrawable == null) {
            return;
        }
        // All frame updates take place on TIME_DEPENDENT_UPDATE_RATE_MS boundaries.
        long initialOffset = currentTimeMillis % TIME_DEPENDENT_UPDATE_RATE_MS;
        // Frame time 0 is the time when frame 0 started being relevant. It may be in the past.
        long frameTime0 = currentTimeMillis - initialOffset;
        // The time between frame updates. Defined as the time between frames 0 and 1.
        long msPerIncrement = getTimeDependentMsBetweenIncrements(currentTimeMillis);
        // The number of frames in our strip, +1 to include the final one!
        int stripSize = (int) ((maxTimeMillis - frameTime0) / msPerIncrement) + 1;

        // Initialise our bitmaps on first use or if they're not the right size.
        if (mTimeDependentStripBitmap == null ||
                mTimeDependentStripDestBitmap == null ||
                mTimeDependentStripCanvas == null ||
                mTimeDependentStripBitmap.getWidth() != mBounds.width() ||
                mTimeDependentStripBitmap.getHeight() != mBounds.height() * stripSize) {
            if (mTimeDependentStripBitmap != null) {
                mTimeDependentStripBitmap.recycle(); // If there's an old bitmap, recycle it.
            }
            if (mTimeDependentStripDestBitmap != null) {
                mTimeDependentStripDestBitmap.recycle(); // Ditto.
            }
            // Create a new bitmap of the required size.
            mTimeDependentStripBitmap = Bitmap.createBitmap(
                    mBounds.width(), mBounds.height() * stripSize,
                    Bitmap.Config.ARGB_8888);
            mTimeDependentStripCanvas = new Canvas(mTimeDependentStripBitmap);
            mTimeDependentStripDestBitmap = Bitmap.createBitmap(
                    mBounds.width(), mBounds.height() * stripSize,
                    Bitmap.Config.ARGB_8888);
        }
        assert mTimeDependentStripBitmap != null;
        assert mTimeDependentStripDestBitmap != null;
        assert mTimeDependentStripCanvas != null;

        // Clear out our bitmap before first draw...
        mTimeDependentStripCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        // Keep a note of what our ambient mode was.
        boolean previousAmbientMode = mIsInAmbientMode;
        boolean previousLowBitAmbient = mLowBitAmbient;
        // Temporarily switch ambient mode to true for decomposition drawing.
        setAmbientMode(true);
        mComplicationDrawable.setLowBitAmbient(false);

        // And draw.
        for (int i = 0; i < stripSize; i++) {
            mTimeDependentStripCanvas.save();
            // Translate so we're drawing to (0,0) of the canvas.
            mTimeDependentStripCanvas.translate(-mBounds.left, -mBounds.top);
            // Translate so we're drawing to the right place in the strip!
            // Make sure we only draw within our bounds
            mTimeDependentStripCanvas.translate(0, mBounds.height() * i);
//            mTimeDependentStripCanvas.clipRect(
//                    0, mBounds.height() * i, mBounds.width(), mBounds.height() * (i + 1));
            // Draw it.
            mComplicationDrawable.draw(mTimeDependentStripCanvas,
                    frameTime0 + (i * msPerIncrement));
            mTimeDependentStripCanvas.restore();
        }

        // Map to 16 colors.
        paintBox.mapBitmapWith8LevelsFromTransparent(
                ambientTint, mTimeDependentStripBitmap, mTimeDependentStripDestBitmap);

        // Restore ambient mode.
        setAmbientMode(previousAmbientMode);
        mComplicationDrawable.setLowBitAmbient(previousLowBitAmbient);

        int componentId = idA.getAndAdd(2);

        builder.addFontComponents(new FontComponent.Builder()
                .setComponentId(componentId)
                .setImage(Icon.createWithBitmap(mTimeDependentStripDestBitmap))
                .setDigitCount(stripSize)
                .build()
        ).addNumberComponents(new NumberComponent.Builder()
                .setComponentId(componentId + 1)
                .setZOrder(componentId + 1)
                .setFontComponentId(componentId)
                .setLowestValue(0).setHighestValue(stripSize - 1)
                .setPosition(new PointF(mBounds.left / (float) bounds.width(),
                        mBounds.top / (float) bounds.height()))
                .setTimeOffsetMs(-frameTime0 - TimeZone.getDefault().getOffset(frameTime0))
                .setMsPerIncrement(msPerIncrement)
                .setMinDigitsShown(0)
                .build()
        );

        // Set the next refresh time to the start of the last frame.
        mLastTimeDependentFrameTime0 = frameTime0 + (stripSize - 1) * msPerIncrement;

        // OK, we've drawn our complication, set the ComplicationData as not new.
        mHasUpdatedComplicationDataObject = false;
    }

    /**
     * Is this complication time-dependent? All sorts of logic are different if so.
     *
     * @return if this complication is time-dependent
     */
    public boolean isTimeDependent() {
        return mComplicationData != null && mComplicationData.isTimeDependent();
    }

    /**
     * For a time-dependent complication, the first frame will be relevant at currentTime,
     * but it will have started being relevant at some point in the past. Returns that time.
     *
     * @param currentTimeMillis the current time
     * @return the time in the past when the first frame (frame 0) became relevant
     */
    public long getTimeDependentFrameTime0(long currentTimeMillis) {
        // Frame time 0 is the time when frame 0 started being relevant. It may be in the past.
        long initialOffset = currentTimeMillis % TIME_DEPENDENT_UPDATE_RATE_MS;
        return currentTimeMillis - initialOffset;
    }

    /**
     * For a time-dependent complication, get the time interval between frames.
     *
     * @param currentTimeMillis the current time
     * @return the time interval between frame refreshes
     */
    public long getTimeDependentMsBetweenIncrements(long currentTimeMillis) {
        // Frame time 0 is the time when frame 0 started being relevant. It may be in the past.
        long initialOffset = currentTimeMillis % TIME_DEPENDENT_UPDATE_RATE_MS;
        long frameTime0 = currentTimeMillis - initialOffset;

        // Frame time 1 is the time when frame 1 will start being relevant.
        long frameTime1 = getNextFrameTime(frameTime0);
        // And we calculate the time difference between the two.
        return frameTime1 - frameTime0;
    }

    /**
     * For a time-dependent complication, get the number of frames we can pre-render into a
     * time-dependent strip, up until a maximum of TIME_DEPENDENT_STRIP_BUDGET. The first
     * frame will render at getFrameTime0 and subsequent frames will render at a steady
     * getTimeDependentMsBetweenIncrements milliseconds after that.
     *
     * @param currentTimeMillis the current time
     * @return the maximum number of frames we can render
     */
    public int getTimeDependentStripSize(long currentTimeMillis) {
        long frameTime0 = getTimeDependentFrameTime0(currentTimeMillis);
        long msPerIncrement = getTimeDependentMsBetweenIncrements(currentTimeMillis);
        // We keep calculating frame time "n" until the frame time differences between it and
        // the frame before != the difference between frames 0 and 1.
        int actualStripSize = 2;
        for (long frameTimePrev = frameTime0;
             actualStripSize < TIME_DEPENDENT_STRIP_BUDGET; actualStripSize++) {
            // Iterate frames 2..end
            // Check that the time difference between (2 and 1) is the same as between (1 and 0).
            long frameTimeNext = getNextFrameTime(frameTimePrev);
            // Calculate the time difference between this frame time and the one before.
            long msPerIncrementNext = frameTimeNext - frameTimePrev;

            // If this time difference is different to what came before, stop calculating here.
            if (msPerIncrement != msPerIncrementNext) {
                break;
            } else {
                frameTimePrev = frameTimeNext;
            }
        }

        return actualStripSize;
    }

    /**
     * For the given frame time, get the next frame time. Might be Long.MAX_VALUE.
     *
     * @param currentFrameTime current frame time
     * @return next frame time, or Long.MAX_VALUE
     */
    private long getNextFrameTime(long currentFrameTime) {
        // Figure out when the next frame change time is:
        // the minimum of the next frame times of the long text/title or short text/title.
        long nextFrameTime = Long.MAX_VALUE;
        if (mComplicationData == null)
            return nextFrameTime;
        if (mComplicationData.getLongText() != null)
            nextFrameTime = Math.min(nextFrameTime,
                    mComplicationData.getLongText().getNextChangeTime(currentFrameTime));
        if (mComplicationData.getLongTitle() != null)
            nextFrameTime = Math.min(nextFrameTime,
                    mComplicationData.getLongTitle().getNextChangeTime(currentFrameTime));
        if (mComplicationData.getShortText() != null)
            nextFrameTime = Math.min(nextFrameTime,
                    mComplicationData.getShortText().getNextChangeTime(currentFrameTime));
        if (mComplicationData.getShortTitle() != null)
            nextFrameTime = Math.min(nextFrameTime,
                    mComplicationData.getShortTitle().getNextChangeTime(currentFrameTime));

        // Round to the next minute.
        nextFrameTime--; // Just in case it's already on a minute boundary...
        nextFrameTime +=
                TIME_DEPENDENT_UPDATE_RATE_MS - nextFrameTime % TIME_DEPENDENT_UPDATE_RATE_MS;

        return nextFrameTime;
    }

    boolean onDrawableTap(int x, int y) {
        if (mComplicationDrawable != null) {
            return mComplicationDrawable.onTap(x, y);
        } else {
            return false;
        }
    }

    void setAmbientMode(boolean inAmbientMode) {
        if (mComplicationDrawable != null) {
            mComplicationDrawable.setInAmbientMode(inAmbientMode);
            mComplicationDrawable.setRangedValueProgressHidden(false);
        }
        mIsInAmbientMode = inAmbientMode;
    }

    public double distanceFrom(float x, float y) {
        // Return Pythagoras distance (x² + y²).
        // Don't bother with sqrt as we're just comparing relative values.
        return Math.pow(x - mBounds.exactCenterX(), 2d) +
                Math.pow(y - mBounds.exactCenterY(), 2d);
    }

    public Rect getBounds() {
        return mBounds;
    }

    void setBounds(@NonNull Rect bounds) {
        boolean dimensionsChanged = true;
        if (mBounds != null) {
            dimensionsChanged = !mBounds.equals(bounds);
        }
        mBounds = bounds;
        if (mInsetBounds == null) {
            mInsetBounds = new Rect();
        }
        // Inset the bounds by 1 - (root 2 / 2).
        // This effectively gives us a square inside the circle that we draw our icon into.
        mInsetBounds.set(mBounds);
        int insetX = (int) ((double) mBounds.width() * (1d - Math.sqrt(0.5d)) / 2d);
        int insetY = (int) ((double) mBounds.height() * (1d - Math.sqrt(0.5d)) / 2d);
        mInsetBounds.inset(insetX, insetY);

        if (dimensionsChanged && mComplicationDrawable != null) {
            mComplicationDrawable.setBounds(mBounds);
            if (mProviderIconDrawable != null) {
                mProviderIconDrawable.setBounds(mInset ? mInsetBounds : mBounds);
            }
        }
    }

    @ColorInt
    int mActiveColor, mActiveColorAlt1, mActiveColorAlt2, mAmbientColor;
    @Nullable
    Typeface mTypeface;

    void setColors(@ColorInt int activeColor,
                   @ColorInt int activeColorAlt1, @ColorInt int activeColorAlt2,
                   @ColorInt int ambientColor, @Nullable Typeface typeface) {
        mActiveColor = activeColor;
        mActiveColorAlt1 = activeColorAlt1;
        mActiveColorAlt2 = activeColorAlt2;
        mAmbientColor = ambientColor;
        mTypeface = typeface;
        setColorsBeforeDraw(mActiveColor);
    }

    private void setColorsBeforeDraw(@ColorInt int activeColor) {
        if (mComplicationDrawable == null) {
            return;
        }
        if (!isForeground) {
            // Set the default to black, in case the user-defined image takes a while to load.
            mComplicationDrawable.setBackgroundColorActive(Color.BLACK);
        } else {
            // Generate a faded active color that is exactly the same as "activeColor"
            // only the alpha is 2/3 the value.
            @ColorInt int fadedActiveColor = Color.argb(
                    (int) ((double) Color.alpha(activeColor) * 2d / 3d),
                    Color.red(activeColor),
                    Color.green(activeColor),
                    Color.blue(activeColor));

            // And a super faded active color that's 1/3 the value.
            @ColorInt int superFadedActiveColor = Color.argb(
                    (int) ((double) Color.alpha(activeColor) / 3d),
                    Color.red(activeColor),
                    Color.green(activeColor),
                    Color.blue(activeColor));

            // Active mode colors
            mComplicationDrawable.setBorderStyleActive(ComplicationDrawable.BORDER_STYLE_NONE);
            mComplicationDrawable.setTextColorActive(activeColor);
            mComplicationDrawable.setTitleColorActive(fadedActiveColor);
            mComplicationDrawable.setIconColorActive(fadedActiveColor);
            mComplicationDrawable.setRangedValuePrimaryColorActive(activeColor);
            mComplicationDrawable.setRangedValueSecondaryColorActive(superFadedActiveColor);
            mComplicationDrawable.setTextTypefaceActive(mTypeface);
            mComplicationDrawable.setTitleTypefaceActive(mTypeface);

            // Generate a faded ambient color that is exactly the same as "mAmbientColor"
            // only the alpha is 2/3 the value.
            @ColorInt int fadedAmbientColor = Color.argb(
                    (int) ((double) Color.alpha(mAmbientColor) * 2d / 3d),
                    Color.red(mAmbientColor),
                    Color.green(mAmbientColor),
                    Color.blue(mAmbientColor));

            // Ambient mode colors
            mComplicationDrawable.setBorderStyleAmbient(ComplicationDrawable.BORDER_STYLE_NONE);
            mComplicationDrawable.setTextColorAmbient(mAmbientColor);
            mComplicationDrawable.setTitleColorAmbient(fadedAmbientColor);
            mComplicationDrawable.setIconColorAmbient(fadedAmbientColor);
            mComplicationDrawable.setRangedValuePrimaryColorAmbient(Color.BLACK);
            mComplicationDrawable.setRangedValueSecondaryColorAmbient(Color.BLACK);
            mComplicationDrawable.setRangedValueProgressHidden(false);
            mComplicationDrawable.setTextTypefaceAmbient(mTypeface);
            mComplicationDrawable.setTitleTypefaceAmbient(mTypeface);
        }
    }

    /**
     * If we're rendering a decomposition, it's rendered here.
     */
    @Nullable
    private Bitmap mAmbientCacheBitmap;

    /**
     * Just like "mDecompositionIntermediateBitmap". Some complications have an annoying
     * habit of "updating" but sending us the exact same data, obliging us to spend time
     * drawing the exact same thing as last time, only to send the exact same graphic up
     * to the offload processor. Not efficient. At the cost of another 600 KiB of runtime
     * memory, we draw into this Bitmap rather than "mDecompositionIntermediateBitmap"
     * every second time, then compare the two (via "Bitmap.sameAs()") to see whether
     * anything has changed... and if it hasn't, we stop work.
     */
    @Nullable
    private Bitmap mAmbientCacheBitmap2;

    /**
     * If we're rendering a decomposition, we draw here, which
     * renders into "mDecompositionDestBitmap".
     */
    @Nullable
    private Canvas mAmbientCacheCanvas;

    /**
     * If we're rendering a decomposition, we draw here...
     * same comments as for "mDecompositionIntermediateBitmap2".
     */
    @Nullable
    private Canvas mAmbientCacheCanvas2;

    void setComplicationData(ComplicationData complicationData) {
        if (mComplicationDrawable != null) {
            mComplicationDrawable.setComplicationData(complicationData);
        }
        mComplicationData = complicationData;
        mHasUpdatedComplicationDataObject = true;
    }

    /**
     * Draw our ambient cached complication to the canvas given.
     *
     * @param canvas Canvas to draw to
     */
    public void drawAmbientCache(@NonNull Canvas canvas) {
        if (mAmbientCacheBitmap2 != null) {
            canvas.drawBitmap(mAmbientCacheBitmap2, mBounds.left, mBounds.top, null);
        }
        mIsAmbientCacheBitmap2Dirty = false;
    }

    /**
     * Has this complication got updated data?
     *
     * @return Whether this complication has updated data
     */
    public boolean checkUpdatedComplicationData(long currentTimeMillis) {
        if (isTimeDependent()) {
            // Also if we're time-dependent, compare the current frame time 0 to the last one.
            return mHasUpdatedComplicationDataObject ||
                    getTimeDependentFrameTime0(currentTimeMillis) >= mLastTimeDependentFrameTime0;
        } else if (mBounds == null) {
            // Not sure what code path would call this before we've set our bounds for the first
            // time, but someone in the world got a NullPointerException this way, so check...
            return false;
        } else if (mHasUpdatedComplicationDataObject && mComplicationDrawable != null) {
            // For non-time-dependent complications, some more logic follows...

            // Wear OS can give us "updated" ComplicationData that hasn't actually changed.
            // If this is the case, we don't want to send it to the offload processor.
            // But there's no actual way to checking if an update is genuinely different,
            // except to render it and compare it to the previous one. So that's what we do...

            // Initialise our bitmaps on first use or if they're not the right size.
            if (mAmbientCacheBitmap == null ||
                    mAmbientCacheBitmap2 == null ||
                    mAmbientCacheCanvas == null ||
                    mAmbientCacheCanvas2 == null ||
                    mAmbientCacheBitmap.getWidth() != mBounds.width() ||
                    mAmbientCacheBitmap.getHeight() != mBounds.height() ||
                    mAmbientCacheBitmap2.getWidth() != mBounds.width() ||
                    mAmbientCacheBitmap2.getHeight() != mBounds.height()) {
                if (mAmbientCacheBitmap != null) {
                    mAmbientCacheBitmap.recycle(); // If there's an old bitmap, recycle it.
                }
                if (mAmbientCacheBitmap2 != null) {
                    mAmbientCacheBitmap2.recycle(); // Ditto.
                }
                // Create a new bitmap of the required size.
                mAmbientCacheBitmap = Bitmap.createBitmap(
                        mBounds.width(), mBounds.height(),
                        Bitmap.Config.ARGB_8888);
                mAmbientCacheCanvas = new Canvas(mAmbientCacheBitmap);
                mAmbientCacheBitmap2 = Bitmap.createBitmap(
                        mBounds.width(), mBounds.height(),
                        Bitmap.Config.ARGB_8888);
                mAmbientCacheCanvas2 = new Canvas(mAmbientCacheBitmap2);
            }
            assert mAmbientCacheBitmap != null;
            assert mAmbientCacheBitmap2 != null;
            assert mAmbientCacheCanvas != null;
            assert mAmbientCacheCanvas2 != null;

            // Keep a note of what our ambient mode was.
            boolean previousAmbientMode = mIsInAmbientMode;
            boolean previousLowBitAmbient = mLowBitAmbient;
            // Temporarily switch ambient mode to true for decomposition drawing.
            setAmbientMode(true);
            mComplicationDrawable.setLowBitAmbient(false);

            // Clear out our ambient cache.
            mAmbientCacheCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

            mAmbientCacheCanvas.save();
            // Translate so we're drawing to (0,0) of the canvas.
            mAmbientCacheCanvas.translate(-mBounds.left, -mBounds.top);
            // Draw it.
            mComplicationDrawable.draw(mAmbientCacheCanvas, currentTimeMillis);
            mAmbientCacheCanvas.restore();

            // Restore ambient mode.
            setAmbientMode(previousAmbientMode);
            mComplicationDrawable.setLowBitAmbient(previousLowBitAmbient);

            // OK, we've gone to the trouble of pre-drawing our complication into a cache.
            // Now, let's see if it's the same as our old one...
            boolean same = mAmbientCacheBitmap.sameAs(mAmbientCacheBitmap2);
            if (!same) {
                // Wear OS sent us an "updated" ComplicationData which really did change.
                // Swap intermediate bitmaps.
                Bitmap b = mAmbientCacheBitmap;
                mAmbientCacheBitmap = mAmbientCacheBitmap2;
                mAmbientCacheBitmap2 = b; // <-- This is now the most recent ambient cache bitmap.
                // And canvases...
                Canvas c = mAmbientCacheCanvas;
                mAmbientCacheCanvas = mAmbientCacheCanvas2;
                mAmbientCacheCanvas2 = c;
            }

            // Don't take this code path again; "mAmbientCacheBitmap2" is now accurate.
            mHasUpdatedComplicationDataObject = false;
            mIsAmbientCacheBitmap2Dirty = true;
        }
        return mIsAmbientCacheBitmap2Dirty;
    }

    public void setLowBitAmbientBurnInProtection(boolean lowBitAmbient, boolean burnInProtection) {
        mLowBitAmbient = lowBitAmbient;
        if (mComplicationDrawable != null) {
            mComplicationDrawable.setLowBitAmbient(lowBitAmbient);
            mComplicationDrawable.setBurnInProtection(burnInProtection);
        }
    }

    @NonNull
    public int[] getSupportedComplicationTypes() {
        if (isForeground) {
            return new int[]{
                    ComplicationData.TYPE_RANGED_VALUE,
                    ComplicationData.TYPE_ICON,
                    ComplicationData.TYPE_SHORT_TEXT,
                    ComplicationData.TYPE_SMALL_IMAGE
            };
        } else {
            return new int[]{
                    ComplicationData.TYPE_LARGE_IMAGE
            };
        }
    }

    public int getId() {
        return id;
    }

    public void draw(@NonNull Canvas canvas, long currentTimeMillis) {
        if (mProviderIconDrawable != null) {
            mProviderIconDrawable.draw(canvas);
        } else if (mComplicationDrawable != null && isActive) {
            mComplicationDrawable.setBorderStyleAmbient(ComplicationDrawable.BORDER_STYLE_NONE);
            if (!mIsInAmbientMode) {
                canvas.save();
                // Draw Alt1.
                canvas.translate(-1, -1);
                setColorsBeforeDraw(mActiveColorAlt1);
                mComplicationDrawable.draw(canvas, currentTimeMillis);
                // Draw Alt2.
                canvas.translate(2, 2);
                setColorsBeforeDraw(mActiveColorAlt2);
                mComplicationDrawable.draw(canvas, currentTimeMillis);
                // And draw the original over the top of it all.
                canvas.restore();
            }
            setColorsBeforeDraw(mActiveColor);
            mComplicationDrawable.draw(canvas, currentTimeMillis);
        }
    }
}
