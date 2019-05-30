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

package pro.watchkit.wearable.watchface.model;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.support.wearable.complications.ComplicationData;
import android.util.Log;

import androidx.annotation.ColorInt;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;

/**
 * The state class for our watch face.
 * <p>
 * This class attempts to capture all the state for our watch face, leaving everything else (in
 * particular the display classes) stateless. By "state" we mean preferences like the following:
 * <ul>
 * <li>The user's selected watch face preset</li>
 * <li>The number of complication slots to display</li>
 * <li>The user's selected complications (i.e. what to fill the slots with)</li>
 * <li>Any other user preferences</li>
 * </ul>
 * <p>
 * And by "state" we also mean anything else relevant to the display of a watch face, like:
 * <ul>
 * <li>The current (or selected) time, date and time zone</li>
 * <li>The user's current location</li>
 * <li>The current data for any selected complications</li>
 * <li>The current number of notifications, of which, how many are unread</li>
 * <li>Whether the watch is currently switched on, or in "ambient" (or "always-on") state</li>
 * </ul>
 * <p>
 * Again, it's all held in this model class so that our display/UI classes are stateless. With a
 * fully populated WatchFaceState object, you have enough information to render your watch face
 * from scratch.
 * <p>
 * When creating a WatchFaceState object, you'll need to pass a Context. This is so we can get
 * the relevant resources out of the application (e.g. localised UI strings).
 */
public class WatchFaceState {
    private WatchFacePreset mWatchFacePreset = new WatchFacePreset();
    private Settings mSettings = new Settings();
    private PaintBox mPaintBox;
    private Collection<ComplicationHolder> mComplications = new ArrayList<>();
    private Map<Integer, ComplicationHolder> mComplicationMap = new Hashtable<>();
    private int mUnreadNotifications = 0;
    private int mTotalNotifications = 0;
    private boolean mAmbient = false;
    private GregorianCalendar mCalendar = new GregorianCalendar();
    private LocationCalculator mLocationCalculator = new LocationCalculator(mCalendar);

    @Override
    public int hashCode() {
        return Objects.hash(
                mWatchFacePreset,
                mSettings,
                mComplications,
                mUnreadNotifications,
                mTotalNotifications);
    }

    public WatchFaceState(Context context) {
        mPaintBox = new PaintBox(context, mWatchFacePreset, mSettings);
    }

    /**
     * Get the current time as milliseconds from the calendar's time.
     *
     * @return Current number of milliseconds
     */
    public long getTimeInMillis() {
        return mCalendar.getTimeInMillis();
    }

    /**
     * Get the current number of seconds from the calendar's time, including the fractional part.
     *
     * @return Current number of seconds including fractional
     */
    public float getSecondsDecimal() {
        return (mCalendar.get(Calendar.SECOND) + mCalendar.get(Calendar.MILLISECOND) / 1000f);
    }

    /**
     * Get the current number of minutes from the calendar's time.
     *
     * @return Current number of minutes
     */
    public int getMinutes() {
        return mCalendar.get(Calendar.MINUTE);
    }

    /**
     * Get the current number of hours from the calendar's time.
     *
     * @return Current number of hours
     */
    public int getHours() {
        return mCalendar.get(Calendar.HOUR);
    }

    /**
     * Set the calendar's time zone to default. Call this if you suspect that the time zone may
     * have changed, daylight savings time has started or ended, etc.
     */
    public void setDefaultTimeZone() {
        mCalendar.setTimeZone(TimeZone.getDefault());
    }

    /**
     * Set the calendar's current time to now. An excellent thing to do before showing or
     * calculating the time.
     */
    public void setCurrentTimeToNow() {
        mCalendar.setTimeInMillis(System.currentTimeMillis());
    }

    public void onAmbientModeChanged(boolean inAmbientMode) {
        mAmbient = inAmbientMode;

        // Update drawable complications' ambient state.
        // Note: ComplicationDrawable handles switching between active/ambient colors, we just
        // have to inform it to enter ambient mode.
        mComplications.forEach(c -> c.setAmbientMode(inAmbientMode));
    }

    public void onComplicationDataUpdate(
            int complicationId, ComplicationData complicationData) {
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
     * Initialize our complications. Returns an array of complication IDs
     *
     * @param context Current application context
     * @return Array of complication IDs
     */
    public int[] initializeComplications(Context context, ComplicationHolder.InvalidateCallback invalidateCallback) {
        // Creates a ComplicationDrawable for each location where the user can render a
        // complication on the watch face. In this watch face, we create one for left, right,
        // and background, but you could add many more.
        ComplicationHolder.resetBaseId();

        mComplications.clear();
        mComplicationMap.clear();
        {
            final ComplicationHolder b = new ComplicationHolder(context);
            b.isForeground = false;
            b.isActive = false;
            b.setDrawableCallback(invalidateCallback);
            mComplications.add(b);
            mComplicationMap.put(b.getId(), b);
        }

        for (int i = 0; i < mSettings.getComplicationCount(); i++) {
            final ComplicationHolder f = new ComplicationHolder(context);
            f.isForeground = true;
            f.setDrawableCallback(invalidateCallback);
            mComplications.add(f);
            mComplicationMap.put(f.getId(), f);
        }

        // Adds new complications to a SparseArray to simplify setting styles and ambient
        // properties for all complications, i.e., iterate over them all.
        setComplicationColors();

        return getComplicationIds();
    }

    /**
     * @return Array of complication IDs
     */
    public int[] getComplicationIds() {
        return mComplications.stream().mapToInt(ComplicationHolder::getId).toArray();
    }

    public ComplicationHolder getComplicationWithId(int id) {
        return mComplicationMap.get(id);
    }

    private int mPreviousBoundsSerial = -1;

    /**
     * Recalculates the location bounds for our circular complications (both foreground and
     * background). Call this if the size of our drawable changes with our bounds, and we'll put
     * the complications somewhere within these bounds.
     *
     * @param bounds Bounds of drawable
     */
    private void recalculateComplicationBounds(Rect bounds) {
        int width = bounds.width(), height = bounds.height();
        int currentBoundsSerial = Objects.hash(width, height, mSettings);
        if (width == 0 || height == 0 || mPreviousBoundsSerial == currentBoundsSerial) {
            return;
        }
        Log.d(WatchFaceState.class.getSimpleName(), "recalculateComplicationBounds ("
                + width + "," + height + ")");
        // Only take this code path if something has changed.
        mPreviousBoundsSerial = currentBoundsSerial;

        float size = Math.min(width, height) / 4f;

        // Start "i" off with our complication rotation, between 0 and 0.75 of a circle width.
        float i;
        switch (mSettings.getComplicationRotation()) {
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

        for (ComplicationHolder complication : mComplications) {
            if (complication.isForeground) {
                // Foreground
                float degrees = (float) ((i + 0.5f) * Math.PI * 2 / mSettings.getComplicationCount());

                float halfSize = size / 2f;

                float innerX = (width / 2f) + (float) Math.sin(degrees) * size;
                float innerY = (height / 2f) - (float) Math.cos(degrees) * size;

                Rect b1 = new Rect((int) (innerX - halfSize), (int) (innerY - halfSize),
                        (int) (innerX + halfSize), (int) (innerY + halfSize));

                complication.setBounds(b1);
                i++;
            } else {
                // Background
                Rect b1 = new Rect(0, 0, width, height);
                complication.setBounds(b1);
            }
        }
    }

    /* Sets active/ambient mode colors for all complications to WatchFacePreset.ColorType.HIGHLIGHT
     *
     * Note: With the rest of the watch face, we update the paint colors based on
     * ambient/active mode callbacks, but because the ComplicationDrawable handles
     * the active/ambient colors, we only set the colors twice. Once at initialization and
     * again if the user changes the highlight color via ConfigActivity.
     */
    public void setComplicationColors() {
        @ColorInt int activeColor = mPaintBox.getColor(PaintBox.ColorType.HIGHLIGHT);
        @ColorInt int ambientColor = Color.WHITE;
        // TODO: hook that up to the night vision tint when after dark

        mComplications.forEach(c -> c.setColors(activeColor, ambientColor));
    }

    public LocationCalculator getLocationCalculator() {
        return mLocationCalculator;
    }

    /**
     * Get the current ambient tint color. This depends on what the user has set, plus the time of
     * day.
     * <p>
     * The way we draw ambient is: all ambient drawing is done in Color.WHITE, then we tint it
     * using a transfer mode paint to whatever this color is.
     *
     * @return Current ambient tint color
     */
    @ColorInt
    public int getAmbientTint() {
        return PaintBox.getIntermediateColor(
                getPaintBox().getColor(PaintBox.ColorType.AMBIENT_NIGHT),
                getPaintBox().getColor(PaintBox.ColorType.AMBIENT_DAY),
                getLocationCalculator().getDuskDawnMultiplier());
    }

    public Settings getSettings() {
        return mSettings;
    }

    public WatchFacePreset getWatchFacePreset() {
        return mWatchFacePreset;
    }

    public PaintBox getPaintBox() {
        return mPaintBox;
    }

    /**
     * Get a list of our ComplicationHolder objects. Don't call this one if you intend to draw
     * them, but if you just want to iterate over them for their properties, that's OK!
     *
     * @return List of our ComplicationHolder objects
     */
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
    public Collection<ComplicationHolder> getComplicationsForDrawing(Rect bounds) {
        recalculateComplicationBounds(bounds);
        return mComplications;
    }

    public boolean isAmbient() {
        return mAmbient;
    }

    public void setAmbient(boolean ambient) {
        mAmbient = ambient;
    }

    public int getUnreadNotifications() {
        return mUnreadNotifications;
    }

    public int getTotalNotifications() {
        return mTotalNotifications;
    }

    public void setNotifications(int unread, int total) {
        mUnreadNotifications = unread;
        mTotalNotifications = total;
    }
}
