/*
 * Copyright (C) 2018-2020 Terence Tan
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
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.wearable.complications.ComplicationData;
import android.support.wearable.complications.rendering.ComplicationDrawable;
import android.widget.ImageView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Objects;

public final class ComplicationHolder {
    private static int BASE_ID = 99;
    public boolean isForeground = false;
    public boolean isActive = false;
    public ImageView background;
    private int id;
    @NonNull
    private ComplicationDrawable mComplicationDrawable;
    private boolean mIsInAmbientMode = false;
    private boolean mInset = false;
    private Rect mBounds, mInsetBounds;
    private Drawable mProviderIconDrawable;

    ComplicationHolder(@Nullable Context context) {
        id = BASE_ID;
        BASE_ID++;

        if (context != null) {
            mComplicationDrawable = new ComplicationDrawable(context);
        } else {
            mComplicationDrawable = new ComplicationDrawable();
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
        return Objects.hash(isForeground, isActive, id, mBounds);
    }

    @NonNull
    @Override
    public String toString() {
        return "Complication " + getId() + (isForeground ? " (foreground)" : " (background)");
    }

    boolean onDrawableTap(int x, int y) {
        return mComplicationDrawable.onTap(x, y);
    }

    void setAmbientMode(boolean inAmbientMode) {
        mComplicationDrawable.setInAmbientMode(inAmbientMode);
        mComplicationDrawable.setRangedValueProgressHidden(false);
        mIsInAmbientMode = inAmbientMode;
    }

    public double distanceFrom(float x, float y) {
        // Return Pythagoras distance (x² + y²).
        // Don't bother with sqrt as we're just comparing relative values.
        return Math.pow((double) (x - mBounds.exactCenterX()), 2d) +
                Math.pow((double) (y - mBounds.exactCenterY()), 2d);
    }

    public Rect getBounds() {
        return mBounds;
    }

    void setBounds(Rect bounds) {
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

        if (dimensionsChanged) {
            mComplicationDrawable.setBounds(mBounds);
            if (mProviderIconDrawable != null) {
                mProviderIconDrawable.setBounds(mInset ? mInsetBounds : mBounds);
            }
        }
    }

    void setColors(@ColorInt int activeColor, @ColorInt int ambientColor,
                   @Nullable Typeface typeface) {
        if (!isForeground) {
            // Set the default to black, in case the user-defined image takes a while to load.
            mComplicationDrawable.setBackgroundColorActive(Color.BLACK);
        } else {
            // Generate a faded ambient color that is exactly the same as "ambientColor"
            // only the alpha is 2/3 the value.
            @ColorInt int fadedActiveColor = Color.argb(
                    (int) (Color.alpha(activeColor) * 0.66666666666667f),
                    Color.red(activeColor),
                    Color.green(activeColor),
                    Color.blue(activeColor));

            // Active mode colors
            mComplicationDrawable.setBorderStyleActive(ComplicationDrawable.BORDER_STYLE_NONE);
            mComplicationDrawable.setTextColorActive(activeColor);
            mComplicationDrawable.setTitleColorActive(fadedActiveColor);
            mComplicationDrawable.setIconColorActive(fadedActiveColor);
            mComplicationDrawable.setRangedValuePrimaryColorActive(activeColor);
            mComplicationDrawable.setTextTypefaceActive(typeface);
            mComplicationDrawable.setTitleTypefaceActive(typeface);

            // Generate a faded ambient color that is exactly the same as "ambientColor"
            // only the alpha is 2/3 the value.
            @ColorInt int fadedAmbientColor = Color.argb(
                    (int) (Color.alpha(ambientColor) * 0.66666666666667f),
                    Color.red(ambientColor),
                    Color.green(ambientColor),
                    Color.blue(ambientColor));

            // Ambient mode colors
            mComplicationDrawable.setBorderStyleAmbient(ComplicationDrawable.BORDER_STYLE_NONE);
            mComplicationDrawable.setTextColorAmbient(ambientColor);
            mComplicationDrawable.setTitleColorAmbient(fadedAmbientColor);
            mComplicationDrawable.setIconColorAmbient(fadedAmbientColor);
            mComplicationDrawable.setRangedValuePrimaryColorAmbient(Color.BLACK);
            mComplicationDrawable.setRangedValueSecondaryColorAmbient(Color.BLACK);
            mComplicationDrawable.setTextTypefaceAmbient(typeface);
            mComplicationDrawable.setTitleTypefaceAmbient(typeface);
        }
    }

    void setComplicationData(ComplicationData complicationData) {
        mComplicationDrawable.setComplicationData(complicationData);
    }

    public void setLowBitAmbientBurnInProtection(boolean lowBitAmbient, boolean burnInProtection) {
        mComplicationDrawable.setLowBitAmbient(lowBitAmbient);
        mComplicationDrawable.setBurnInProtection(burnInProtection);
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
        } else {
            mComplicationDrawable.setBorderStyleAmbient(ComplicationDrawable.BORDER_STYLE_NONE);
            mComplicationDrawable.draw(canvas, currentTimeMillis);
        }
    }
}
