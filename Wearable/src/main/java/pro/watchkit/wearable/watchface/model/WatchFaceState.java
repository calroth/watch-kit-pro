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

package pro.watchkit.wearable.watchface.model;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.wearable.complications.ComplicationData;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;
import java.util.Objects;

public class WatchFaceState extends WatchFaceStateBase {
    private final Collection<ComplicationHolder> mComplications = new ArrayList<>();
    private final Map<Integer, ComplicationHolder> mComplicationMap = new Hashtable<>();

    public WatchFaceState(@NonNull Context context) {
        super(context);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                super.hashCode(),
                mComplications);
    }

    public void setBurnInProtection(boolean lowBitAmbient, boolean burnInProtection) {
        super.setBurnInProtection(lowBitAmbient, burnInProtection);

        // Set low-bit ambient and burn-in protection on our complications as required.
        getComplications().forEach(
                c -> c.setLowBitAmbientBurnInProtection(lowBitAmbient, burnInProtection));
    }

    public void onComplicationDataUpdate(
            int complicationId, @NonNull ComplicationData complicationData,
            @Nullable Drawable.Callback cb) {
        // Updates correct ComplicationDrawable with updated data.
        ComplicationHolder c = getComplicationWithId(complicationId);
        if (c != null) {
            switch (complicationData.getType()) {
                case ComplicationData.TYPE_EMPTY:
                case ComplicationData.TYPE_NO_DATA:
                case ComplicationData.TYPE_NOT_CONFIGURED:
                case ComplicationData.TYPE_NO_PERMISSION: {
                    c.isActive = false;
                    break;
                }
                default: {
                    c.isActive = true;
                    break;
                }
            }
            c.setComplicationData(complicationData);
        }

        // Set the callback drawable for all complications.
        if (cb != null) {
            mComplications.forEach(k -> k.setComplicationDrawableCallback(cb));
        }
    }

    public boolean onComplicationTap(int x, int y) {
        // Try all foreground complications first, before background complications.
        for (ComplicationHolder complication : mComplications) {
            if (complication.isForeground) {
                boolean successfulTap = complication.onDrawableTap(x, y);

                if (successfulTap) {
                    return true;
                }
            }
        }
        // Try all background complications.
        for (ComplicationHolder complication : mComplications) {
            if (!complication.isForeground) {
                boolean successfulTap = complication.onDrawableTap(x, y);

                if (successfulTap) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * @return Array of complication IDs
     */
    public int[] getComplicationIds() {
        return mComplications.stream().mapToInt(ComplicationHolder::getId).toArray();
    }

    @Nullable
    public ComplicationHolder getComplicationWithId(int id) {
        return mComplicationMap.get(id);
    }

    /**
     * Initialize our complications. Returns an array of complication IDs
     *
     * @param context                  Current application context
     * @param withComplicationDrawable Also initialise a ComplicationDrawable?
     * @return Array of complication IDs
     */
    public int[] initializeComplications(Context context, boolean withComplicationDrawable) {
        // Creates a ComplicationDrawable for each location where the user can render a
        // complication on the watch face. In this watch face, we create one for left, right,
        // and background, but you could add many more.
        ComplicationHolder.resetBaseId();

        mComplications.clear();
        mComplicationMap.clear();
        {
            final ComplicationHolder b = new ComplicationHolder(context, withComplicationDrawable);
            b.isForeground = false;
            b.isActive = false;
            mComplications.add(b);
            mComplicationMap.put(b.getId(), b);
        }

        for (int i = 0; i < getComplicationCountInt(); i++) {
            final ComplicationHolder f = new ComplicationHolder(context, withComplicationDrawable);
            f.isForeground = true;
            mComplications.add(f);
            mComplicationMap.put(f.getId(), f);
        }

        // Adds new complications to a SparseArray to simplify setting materials and ambient
        // properties for all complications, i.e., iterate over them all.
        setComplicationColors();

        return getComplicationIds();
    }

    private int mPreviousBoundsSerial = -1;

    /**
     * Recalculates the location bounds for our circular complications (both foreground and
     * background). Call this if the size of our drawable changes with our bounds, and we'll put
     * the complications somewhere within these bounds.
     *
     * @param bounds Bounds of drawable
     */
    private void recalculateComplicationBounds(@NonNull Rect bounds) {
        int width = bounds.width(), height = bounds.height();
        int currentBoundsSerial = Objects.hash(width, height, getSettingsHashCode());

        // Count how many complications have null bounds.
        // If any, we definitely want to recalculate them, so no early leaving this function.
        long complicationsWithNullBounds =
                mComplications.stream().filter(c -> c.getBounds() == null).count();
        if (complicationsWithNullBounds == 0) {
            if (width == 0 || height == 0 || mPreviousBoundsSerial == currentBoundsSerial) {
                return;
            }
        }

        // Only take this code path if something has changed.
        mPreviousBoundsSerial = currentBoundsSerial;

        float size = Math.min(width, height) / 4f;

        // Start "i" off with our complication rotation, between 0 and 0.75 of a circle width.
        float i;
        switch (getComplicationRotation()) {
            default:
            case ROTATE_00: {
                i = 0.0f;
                break;
            }
            case ROTATE_25: {
                i = 0.25f;
                break;
            }
            case ROTATE_50: {
                i = 0.50f;
                break;
            }
            case ROTATE_75: {
                i = 0.75f;
                break;
            }
        }

        float radius = size * 2f;
        switch (getComplicationSize()) {
            case SMALL: {
                radius *= 0.4f;
                break;
            }
            case MEDIUM: {
                radius *= 0.6f;
                break;
            }
            case LARGE: {
                radius *= 0.8f;
                break;
            }
            default:
            case X_LARGE: {
                // Don't need to do this: radius *= 1.0f;
                break;
            }
        }

        float complicationScale;
        switch (getComplicationScale()) {
            case SMALL: {
                complicationScale = 0.875f;
                break;
            }
            default:
            case MEDIUM: {
                complicationScale = 1f;
                break;
            }
            case LARGE: {
                complicationScale = 1.125f;
                break;
            }
            case X_LARGE: {
                complicationScale = 1.25f;
                break;
            }
        }

        for (ComplicationHolder complication : mComplications) {
            if (complication.isForeground) {
                // Foreground
                float degreesB = (float) (i * Math.PI * 2 / getComplicationCountInt());
                float degreesC = (float) ((i + 1f) * Math.PI * 2 / getComplicationCountInt());

                // A triangle (a, b, c) with a at the centre,
                // and b and c forming a wedge of the circle.
                // Their co-ordinates:
                float xa = (float) width / 2f;
                float ya = (float) height / 2f;
                float xb = xa + (float) Math.sin(degreesB) * radius;
                float yb = ya + (float) Math.cos(degreesB) * radius;
                float xc = xa + (float) Math.sin(degreesC) * radius;
                float yc = ya + (float) Math.cos(degreesC) * radius;

                // Their side lengths
                float a = (float) Math.sqrt(((xb - xc) * (xb - xc)) + ((yb - yc) * (yb - yc)));
                @SuppressWarnings("UnnecessaryLocalVariable") float b = radius; // By definition.
                @SuppressWarnings("UnnecessaryLocalVariable") float c = radius;
                // (We suppressed the warnings as "b" and "c" mean something maths-wise.)

                // The centre and radius of the incircle.
                float incentreX = (a * xa + b * xb + c * xc) / (a + b + c);
                float incentreY = (a * ya + b * yb + c * yc) / (a + b + c);
                float s = (a + b + c) / 2f;
                float inradius = (float) Math.sqrt(s * (s - a) * (s - b) * (s - c)) / s;
                inradius *= complicationScale;

                Rect b1 = new Rect((int) (incentreX - inradius), (int) (incentreY - inradius),
                        (int) (incentreX + inradius), (int) (incentreY + inradius));
                complication.setBounds(b1);
                i++;
            } else {
                // Background
                Rect b1 = new Rect(0, 0, width, height);
                complication.setBounds(b1);
            }
        }
    }

    /**
     * Sets active/ambient mode colors for all complications to WatchFacePreset.ColorType.HIGHLIGHT
     *
     * Note: With the rest of the watch face, we update the paint colors based on
     * ambient/active mode callbacks, but because the ComplicationDrawable handles
     * the active/ambient colors, we only set the colors twice. Once at initialization and
     * again if the user changes the highlight color via ConfigActivity.
     */
    public void setComplicationColors() {
        @ColorInt int activeColor = getComplicationTextColor();
        @ColorInt int activeColorAlt1 = getComplicationTextColorAlt1();
        @ColorInt int activeColorAlt2 = getComplicationTextColorAlt2();
        @ColorInt int ambientColor = Color.WHITE;
        @Nullable android.graphics.Typeface typeface = getTypefaceObject();

        mComplications.forEach(c -> c.setColors(
                activeColor, activeColorAlt1, activeColorAlt2, ambientColor, typeface));
    }

    /**
     * Get a list of our ComplicationHolder objects. Don't call this one if you intend to draw
     * them, but if you just want to iterate over them for their properties, that's OK!
     *
     * @return List of our ComplicationHolder objects
     */
    @NonNull
    public Collection<ComplicationHolder> getComplications() {
        return mComplications;
    }

    /**
     * Get a list of our ComplicationHolder objects, ready for drawing. This method has some extra
     * logic to re-position each object if our bounds have changed. You can also use the result to
     * iterate over for their properties too, if you want.
     *
     * @param bounds Bounds of the drawable to which we intend to draw
     * @return List of our ComplicationHolder objects
     */
    @NonNull
    public Collection<ComplicationHolder> getComplicationsForDrawing(@NonNull Rect bounds) {
        recalculateComplicationBounds(bounds);
        return mComplications;
    }

    @Override
    public void setAmbient(boolean ambient) {
        super.setAmbient(ambient);

        // Update drawable complications' ambient state.
        // Note: ComplicationDrawable handles switching between active/ambient colors, we just
        // have to inform it to enter ambient mode.
        mComplications.forEach(c -> c.setAmbientMode(ambient));
    }
}
