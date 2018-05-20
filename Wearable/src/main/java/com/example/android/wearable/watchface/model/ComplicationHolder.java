/*
 * Copyright (C) 2018 Terence Tan
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

package com.example.android.wearable.watchface.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.wearable.complications.ComplicationData;
import android.support.wearable.complications.rendering.ComplicationDrawable;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.util.Objects;

public final class ComplicationHolder implements Drawable.Callback {

    public interface InvalidateCallback {
        /**
         * Called when the drawable needs to be redrawn.  A view at this point
         * should invalidate itself (or at least the part of itself where the
         * drawable appears).
         */
        void invalidate();
    }

    @Override
    public boolean equals(Object o) {
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
        return Objects.hash(isForeground, isActive, id, mIsInAmbientMode, mBounds);
    }

    @Override
    public String toString() {
        return "Complication " + getId() + (isForeground ? " (foreground)" : " (background)");
    }

    private static int BASE_ID = 99;

    public static void resetBaseId() {
        BASE_ID = 99;
    }

    public ComplicationHolder(Context context) {
        id = BASE_ID;
        BASE_ID++;

        if (context != null) {
            drawable = new ComplicationDrawable(context);
        } else {
            drawable = new ComplicationDrawable();
        }

        drawable.setCallback(this);
    }

    private static final boolean cacheImages = false;

    public boolean isForeground = false;

    public boolean isActive = false;

    private int id;

    public ImageButton imageButton;

    public ImageView background;

    private ComplicationDrawable drawable;

    public boolean onDrawableTap(int x, int y) {
        return drawable.onTap(x, y);
    }

    private boolean mIsInAmbientMode = false;

    public void setAmbientMode(boolean inAmbientMode) {
        drawable.setInAmbientMode(inAmbientMode);
        drawable.setRangedValueProgressHidden(inAmbientMode);
        mIsInAmbientMode = inAmbientMode;
    }

    private Rect mBounds;

    public Rect getBounds() {
        return mBounds;
    }

    public void setBounds(Rect bounds) {
        boolean dimensionsChanged = true;
        if (mBounds != null) {
            dimensionsChanged = mBounds.width() == bounds.width()
                    && mBounds.height() == bounds.height();
        }
        mBounds = bounds;

        if (dimensionsChanged) {
            if (cacheImages) {
                Rect drawableBounds = new Rect(0, 0, mBounds.width(), mBounds.height());
                drawable.setBounds(drawableBounds);
                mAmbientBitmap = Bitmap.createBitmap(mBounds.width(), mBounds.height(),
                        Bitmap.Config.ARGB_8888);
                mActiveBitmap = Bitmap.createBitmap(mBounds.width(), mBounds.height(),
                        Bitmap.Config.ARGB_8888);
            } else {
                drawable.setBounds(mBounds);
            }
            invalidate();
        }
    }

    @Deprecated
    public void setBorderStyleActive(int borderStyle) {
        drawable.setBorderStyleActive(borderStyle);
    }

    public void setAmbientColors(int textColor, int titleColor, int iconColor) {
        drawable.setTextColorAmbient(textColor);
        drawable.setTitleColorAmbient(titleColor);
        drawable.setIconColorAmbient(iconColor);
    }

    public void setColors(int primaryComplicationColor) {
        if (!isForeground) {
            // Set the default to black, in case the user-defined image takes a while to load.
            drawable.setBackgroundColorActive(Color.BLACK);
        } else {
            // Active mode colors
//            drawable.setBorderColorActive(primaryComplicationColor);
//            drawable.setBorderStyleActive(ComplicationDrawable.BORDER_STYLE_NONE);
            drawable.setRangedValuePrimaryColorActive(primaryComplicationColor);

            // Ambient mode colors
            drawable.setBorderStyleAmbient(ComplicationDrawable.BORDER_STYLE_NONE);
        }
    }


    private final Handler mHandler = new Handler();

    private long itWasMe = 0;

    @Override
    public void invalidateDrawable(@NonNull Drawable who) {
//        Log.d("AnalogWatchFace", "invalidateDrawable() (" + getId() //+ "): " + mComplicationDescription);
//                + ")");
        itWasMe = System.currentTimeMillis();
        invalidate();
        if (mInvalidateCallback != null) {
            mInvalidateCallback.invalidate();
        }
    }

    @Override
    public void scheduleDrawable(@NonNull Drawable who, @NonNull Runnable what, long when) {
        Log.d("AnalogWatchFace", "scheduleDrawable() (" + getId() + "): " + when);
        mHandler.postAtTime(what, who, when);
    }

    @Override
    public void unscheduleDrawable(@NonNull Drawable who, @NonNull Runnable what) {
        Log.d("AnalogWatchFace", "unscheduleDrawable() (" + getId() + ")");
        mHandler.removeCallbacks(what, who);
    }

    private InvalidateCallback mInvalidateCallback;

    public void setDrawableCallback(InvalidateCallback invalidateCallback) {
        mInvalidateCallback = invalidateCallback;
//        public void setDrawableCallback(Drawable.Callback cb) {
//        drawable.setCallback(this);
//        drawable.setCallback(cb);
        // Test it out?
//        drawable.invalidateSelf();
    }

//    private String mComplicationDescription;

    public void setComplicationData(ComplicationData complicationData) {
        drawable.setComplicationData(complicationData);
//        mComplicationDescription = complicationData.toString();
        invalidate();
    }

    public void setLowBitAmbientBurnInProtection(boolean lowBitAmbient, boolean burnInProtection) {
        drawable.setLowBitAmbient(lowBitAmbient);
        drawable.setBurnInProtection(burnInProtection);
    }

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

    private boolean mAmbientBitmapInvalidated = true;
    private boolean mActiveBitmapInvalidated = true;

    private void invalidate() {
        mAmbientBitmapInvalidated = true;
        mActiveBitmapInvalidated = true;
    }

    private Bitmap mAmbientBitmap;

    private Bitmap mActiveBitmap;

    private Bitmap getAmbientBitmap(long currentTimeMillis) {
        if (mAmbientBitmapInvalidated) {
            drawable.draw(new Canvas(mAmbientBitmap), currentTimeMillis);
            mAmbientBitmapInvalidated = false;
        }
        return mAmbientBitmap;
    }

    private Bitmap getActiveBitmap(long currentTimeMillis) {
        if (mActiveBitmapInvalidated) {
            drawable.draw(new Canvas(mActiveBitmap), currentTimeMillis);
            mActiveBitmapInvalidated = false;
        }
        return mActiveBitmap;
    }

    final static boolean highlightItWasMe = false;

    public void draw(Canvas canvas, long currentTimeMillis) {
        if (cacheImages) {
            Bitmap bitmap = mIsInAmbientMode ? getAmbientBitmap(currentTimeMillis) : getActiveBitmap(currentTimeMillis);
            canvas.drawBitmap(bitmap, mBounds.left, mBounds.top, null);
        } else {
//            Drawable.Callback obj = drawable.getCallback();
//            Log.d("AnalogWatchFace", "Complication draw (id=" + getId()
//                    + "): callback is " + (obj == null ? "null" : obj.toString()));
            if (highlightItWasMe && itWasMe > currentTimeMillis - 500) {
                // if itWasMe triggered in the last 500 ms
                drawable.setBorderStyleAmbient(ComplicationDrawable.BORDER_STYLE_DASHED);
                drawable.setBorderColorAmbient(Color.WHITE);
                Log.d("AnalogWatchFace", "invalidateDrawable(itWasMe) (" + getId() + ")");
            } else {
                drawable.setBorderStyleAmbient(ComplicationDrawable.BORDER_STYLE_NONE);
            }
            drawable.draw(canvas, currentTimeMillis);
        }
    }
}
