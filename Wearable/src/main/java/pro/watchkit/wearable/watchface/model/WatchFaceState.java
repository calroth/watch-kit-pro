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

import pro.watchkit.wearable.watchface.model.BytePackable.ComplicationCount;
import pro.watchkit.wearable.watchface.model.BytePackable.ComplicationRotation;
import pro.watchkit.wearable.watchface.model.BytePackable.GradientStyle;
import pro.watchkit.wearable.watchface.model.BytePackable.HandCutout;
import pro.watchkit.wearable.watchface.model.BytePackable.HandLength;
import pro.watchkit.wearable.watchface.model.BytePackable.HandShape;
import pro.watchkit.wearable.watchface.model.BytePackable.HandStalk;
import pro.watchkit.wearable.watchface.model.BytePackable.HandThickness;
import pro.watchkit.wearable.watchface.model.BytePackable.Style;
import pro.watchkit.wearable.watchface.model.BytePackable.TickLength;
import pro.watchkit.wearable.watchface.model.BytePackable.TickRadiusPosition;
import pro.watchkit.wearable.watchface.model.BytePackable.TickShape;
import pro.watchkit.wearable.watchface.model.BytePackable.TickThickness;
import pro.watchkit.wearable.watchface.model.BytePackable.TicksDisplay;
import pro.watchkit.wearable.watchface.model.PaintBox.ColorType;

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
    private final WatchFacePreset mWatchFacePreset = new WatchFacePreset();
    private final Settings mSettings = new Settings();
    private final Context mContext;
    private final PaintBox mPaintBox;
    private final Collection<ComplicationHolder> mComplications = new ArrayList<>();
    private final Map<Integer, ComplicationHolder> mComplicationMap = new Hashtable<>();
    private int mUnreadNotifications = 0;
    private int mTotalNotifications = 0;
    private boolean mAmbient = false;
    private final GregorianCalendar mCalendar = new GregorianCalendar();
    private final LocationCalculator mLocationCalculator = new LocationCalculator(mCalendar);

    @Override
    public int hashCode() {
        return Objects.hash(
                mWatchFacePreset,
                mSettings,
                mComplications,
                mUnreadNotifications,
                mTotalNotifications);
    }

    private StringBuilder mStringBuilder = new StringBuilder();

    public WatchFaceState(Context context) {
        mContext = context;
        mPaintBox = new PaintBox(context);
        // Hmm. Strictly temporary: how about a default setting?
        setString("fcd81c000c0100000006c06a60000001~3cda1cc0000000000000000000000001");
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

        for (int i = 0; i < getComplicationCountInt(); i++) {
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

        for (ComplicationHolder complication : mComplications) {
            if (complication.isForeground) {
                // Foreground
                float degrees = (float) ((i + 0.5f) * Math.PI * 2 / getComplicationCountInt());

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

    public LocationCalculator getLocationCalculator() {
        return mLocationCalculator;
    }

    /* Sets active/ambient mode colors for all complications to WatchFacePreset.ColorType.HIGHLIGHT
     *
     * Note: With the rest of the watch face, we update the paint colors based on
     * ambient/active mode callbacks, but because the ComplicationDrawable handles
     * the active/ambient colors, we only set the colors twice. Once at initialization and
     * again if the user changes the highlight color via ConfigActivity.
     */
    public void setComplicationColors() {
        @ColorInt int activeColor = getColor(ColorType.HIGHLIGHT);
        @ColorInt int ambientColor = Color.WHITE;
        // TODO: hook that up to the night vision tint when after dark

        mComplications.forEach(c -> c.setColors(activeColor, ambientColor));
    }

//    public Settings getSettings() {
//        return mSettings;
//    }
//
//    public WatchFacePreset getWatchFacePreset() {
//        return mWatchFacePreset;
//    }

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
                getColor(ColorType.AMBIENT_NIGHT),
                getColor(ColorType.AMBIENT_DAY),
                getLocationCalculator().getDuskDawnMultiplier());
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

    public PaintBox getPaintBox() {
        regeneratePaints();
        return mPaintBox;
    }

    public String getString() {
        mStringBuilder.setLength(0);
        mStringBuilder.append(mWatchFacePreset.getString())
                .append("~")
                .append(mSettings.getString());
        return mStringBuilder.toString();
    }

    public void setString(String s) {
        if (s == null || s.length() == 0) return;
        String[] split = s.split("~");
        if (split.length == 2) {
            mWatchFacePreset.setString(split[0]);
            mSettings.setString(split[1]);
        }
        regeneratePaints();
    }

    // region Settings
    ComplicationRotation getComplicationRotation() {
        return mSettings.mComplicationRotation;
    }

    void setComplicationRotation(BytePackable.ComplicationRotation complicationRotation) {
        mSettings.mComplicationRotation = complicationRotation;
    }

    int getComplicationCountInt() {
        switch (getComplicationCount()) {
            case COUNT_5: {
                return 5;
            }
            case COUNT_6: {
                return 6;
            }
            case COUNT_7: {
                return 7;
            }
            default:
            case COUNT_8: {
                return 8;
            }
        }
    }

    ComplicationCount getComplicationCount() {
        return mSettings.mComplicationCount;
    }

    void setComplicationCount(ComplicationCount complicationCount) {
        mSettings.mComplicationCount = complicationCount;
    }

    public boolean isShowUnreadNotifications() {
        return mSettings.mShowUnreadNotifications;
    }

    public void toggleShowUnreadNotifications() {
        mSettings.mShowUnreadNotifications = !mSettings.mShowUnreadNotifications;
    }

    public boolean isNightVisionModeEnabled() {
        return mSettings.mNightVisionModeEnabled;
    }

    public boolean toggleNightVisionModeEnabled() {
        mSettings.mNightVisionModeEnabled = !mSettings.mNightVisionModeEnabled;
        return mSettings.mNightVisionModeEnabled;
    }

    int getAmbientDaySixBitColor() {
        return mSettings.mAmbientDaySixBitColor;
    }

    void setAmbientDaySixBitColor(int ambientDaySixBitColor) {
        mSettings.mAmbientDaySixBitColor = ambientDaySixBitColor;
        regeneratePaints();
    }

    int getAmbientNightSixBitColor() {
        return mSettings.mAmbientNightSixBitColor;
    }

    void setAmbientNightSixBitColor(int ambientNightSixBitColor) {
        mSettings.mAmbientNightSixBitColor = ambientNightSixBitColor;
        regeneratePaints();
    }

    public Style getComplicationRingStyle() {
        return mSettings.mComplicationRingStyle;
    }

    void setComplicationRingStyle(Style complicationRingStyle) {
        mSettings.mComplicationRingStyle = complicationRingStyle;
    }

    public Style getComplicationBackgroundStyle() {
        return mSettings.mComplicationBackgroundStyle;
    }

    void setComplicationBackgroundStyle(Style complicationBackgroundStyle) {
        mSettings.mComplicationBackgroundStyle = complicationBackgroundStyle;
    }

    public boolean isDeveloperMode() {
        return mSettings.mDeveloperMode;
    }

    public void setDeveloperMode(boolean developerMode) {
        mSettings.mDeveloperMode = developerMode;
    }

    public boolean isStats() {
        return mSettings.mStats;
    }

    public void setStats(boolean stats) {
        mSettings.mStats = stats;
    }

    public boolean isStatsDetail() {
        return mSettings.mStatsDetail;
    }

    public void setStatsDetail(boolean statsDetail) {
        mSettings.mStatsDetail = statsDetail;
    }
    // endregion

    // region WatchFacePreset

    void setMinuteHandOverride(boolean minuteHandOverride) {
        mWatchFacePreset.mMinuteHandOverride = minuteHandOverride;
    }

    boolean isMinuteHandOverridden() {
        return mWatchFacePreset.mMinuteHandOverride;
    }

    void setSecondHandOverride(boolean secondHandOverride) {
        mWatchFacePreset.mSecondHandOverride = secondHandOverride;
    }

    boolean isSecondHandOverridden() {
        return mWatchFacePreset.mSecondHandOverride;
    }

    void setTwelveTickOverride(boolean twelveTickOverride) {
        mWatchFacePreset.mTwelveTickOverride = twelveTickOverride;
    }

    boolean isTwelveTicksOverridden() {
        return isTwelveTicksVisible() && mWatchFacePreset.mTwelveTickOverride;
    }

    void setSixtyTickOverride(boolean sixtyTickOverride) {
        mWatchFacePreset.mSixtyTickOverride = sixtyTickOverride;
    }

    boolean isSixtyTicksOverridden() {
        return isSixtyTicksVisible() && mWatchFacePreset.mSixtyTickOverride;
    }

    public Style getBackgroundStyle() {
        return mWatchFacePreset.mBackgroundStyle;
    }

    void setBackgroundStyle(Style backgroundStyle) {
        mWatchFacePreset.mBackgroundStyle = backgroundStyle;
    }

    public HandShape getHourHandShape() {
        return mWatchFacePreset.mHourHandShape;
    }

    void setHourHandShape(HandShape hourHandShape) {
        mWatchFacePreset.mHourHandShape = hourHandShape;
    }

    public HandShape getMinuteHandShape() {
        return mWatchFacePreset.mMinuteHandOverride ?
                mWatchFacePreset.mMinuteHandShape : mWatchFacePreset.mHourHandShape;
    }

    void setMinuteHandShape(HandShape minuteHandShape) {
        mWatchFacePreset.mMinuteHandShape = minuteHandShape;
    }

    public HandShape getSecondHandShape() {
        // If not overridden, the default is just a plain and regular second hand.
        return mWatchFacePreset.mSecondHandOverride ?
                mWatchFacePreset.mSecondHandShape : HandShape.STRAIGHT;
    }

    void setSecondHandShape(HandShape secondHandShape) {
        mWatchFacePreset.mSecondHandShape = secondHandShape;
    }

    public HandLength getHourHandLength() {
        return mWatchFacePreset.mHourHandLength;
    }

    void setHourHandLength(HandLength hourHandLength) {
        mWatchFacePreset.mHourHandLength = hourHandLength;
    }

    public HandLength getMinuteHandLength() {
        return mWatchFacePreset.mMinuteHandOverride ?
                mWatchFacePreset.mMinuteHandLength : mWatchFacePreset.mHourHandLength;
    }

    void setMinuteHandLength(HandLength minuteHandLength) {
        mWatchFacePreset.mMinuteHandLength = minuteHandLength;
    }

    public HandLength getSecondHandLength() {
        // If not overridden, the default is just a plain and regular second hand.
        return mWatchFacePreset.mSecondHandOverride ?
                mWatchFacePreset.mSecondHandLength : HandLength.LONG;
    }

    void setSecondHandLength(HandLength secondHandLength) {
        mWatchFacePreset.mSecondHandLength = secondHandLength;
    }

    public HandThickness getHourHandThickness() {
        return mWatchFacePreset.mHourHandThickness;
    }

    void setHourHandThickness(HandThickness hourHandThickness) {
        mWatchFacePreset.mHourHandThickness = hourHandThickness;
    }

    public HandThickness getMinuteHandThickness() {
        return mWatchFacePreset.mMinuteHandOverride ?
                mWatchFacePreset.mMinuteHandThickness : mWatchFacePreset.mHourHandThickness;
    }

    void setMinuteHandThickness(HandThickness minuteHandThickness) {
        mWatchFacePreset.mMinuteHandThickness = minuteHandThickness;
    }

    public HandThickness getSecondHandThickness() {
        // If not overridden, the default is just a plain and regular second hand.
        return mWatchFacePreset.mSecondHandOverride ?
                mWatchFacePreset.mSecondHandThickness : HandThickness.THIN;
    }

    void setSecondHandThickness(HandThickness secondHandThickness) {
        mWatchFacePreset.mSecondHandThickness = secondHandThickness;
    }

    public Style getHourHandStyle() {
        return mWatchFacePreset.mHourHandStyle;
    }

    void setHourHandStyle(Style hourHandStyle) {
        mWatchFacePreset.mHourHandStyle = hourHandStyle;
    }

    public Style getMinuteHandStyle() {
        return mWatchFacePreset.mMinuteHandOverride ?
                mWatchFacePreset.mMinuteHandStyle : mWatchFacePreset.mHourHandStyle;
    }

    void setMinuteHandStyle(Style minuteHandStyle) {
        mWatchFacePreset.mMinuteHandStyle = minuteHandStyle;
    }

    public Style getSecondHandStyle() {
        // If not overridden, the default is just a plain and regular second hand.
        return mWatchFacePreset.mSecondHandOverride ?
                mWatchFacePreset.mSecondHandStyle : Style.HIGHLIGHT;
    }

    void setSecondHandStyle(Style secondHandStyle) {
        mWatchFacePreset.mSecondHandStyle = secondHandStyle;
    }

    TicksDisplay getTicksDisplay() {
        return mWatchFacePreset.mTicksDisplay;
    }

    void setTicksDisplay(TicksDisplay ticksDisplay) {
        mWatchFacePreset.mTicksDisplay = ticksDisplay;
    }

    public boolean isFourTicksVisible() {
        return mWatchFacePreset.mTicksDisplay != TicksDisplay.NONE;
    }

    public boolean isTwelveTicksVisible() {
        return mWatchFacePreset.mTicksDisplay == TicksDisplay.FOUR_TWELVE ||
                mWatchFacePreset.mTicksDisplay == TicksDisplay.FOUR_TWELVE_60;
    }

    public boolean isSixtyTicksVisible() {
        return mWatchFacePreset.mTicksDisplay == TicksDisplay.FOUR_TWELVE_60;
    }

    public TickShape getFourTickShape() {
        return mWatchFacePreset.mFourTickShape;
    }

    void setFourTickShape(TickShape fourTickShape) {
        mWatchFacePreset.mFourTickShape = fourTickShape;
    }

    public TickShape getTwelveTickShape() {
        return mWatchFacePreset.mTwelveTickOverride ?
                mWatchFacePreset.mTwelveTickShape : mWatchFacePreset.mFourTickShape;
    }

    void setTwelveTickShape(TickShape twelveTickShape) {
        mWatchFacePreset.mTwelveTickShape = twelveTickShape;
    }

    public TickShape getSixtyTickShape() {
        return mWatchFacePreset.mSixtyTickOverride ?
                mWatchFacePreset.mSixtyTickShape : mWatchFacePreset.mFourTickShape;
    }

    void setSixtyTickShape(TickShape sixtyTickShape) {
        mWatchFacePreset.mSixtyTickShape = sixtyTickShape;
    }

    public TickLength getFourTickLength() {
        return mWatchFacePreset.mFourTickLength;
    }

    void setFourTickLength(TickLength fourTickLength) {
        mWatchFacePreset.mFourTickLength = fourTickLength;
    }

    public TickLength getTwelveTickLength() {
        return mWatchFacePreset.mTwelveTickOverride ?
                mWatchFacePreset.mTwelveTickLength : mWatchFacePreset.mFourTickLength;
    }

    void setTwelveTickLength(TickLength twelveTickLength) {
        mWatchFacePreset.mTwelveTickLength = twelveTickLength;
    }

    public TickLength getSixtyTickLength() {
        return mWatchFacePreset.mSixtyTickOverride ?
                mWatchFacePreset.mSixtyTickLength : mWatchFacePreset.mFourTickLength;
    }

    void setSixtyTickLength(TickLength sixtyTickLength) {
        mWatchFacePreset.mSixtyTickLength = sixtyTickLength;
    }

    public TickThickness getFourTickThickness() {
        return mWatchFacePreset.mFourTickThickness;
    }

    void setFourTickThickness(TickThickness fourTickThickness) {
        mWatchFacePreset.mFourTickThickness = fourTickThickness;
    }

    public TickThickness getTwelveTickThickness() {
        return mWatchFacePreset.mTwelveTickOverride ?
                mWatchFacePreset.mTwelveTickThickness : mWatchFacePreset.mFourTickThickness;
    }

    void setTwelveTickThickness(TickThickness twelveTickThickness) {
        mWatchFacePreset.mTwelveTickThickness = twelveTickThickness;
    }

    public TickThickness getSixtyTickThickness() {
        return mWatchFacePreset.mSixtyTickOverride ?
                mWatchFacePreset.mSixtyTickThickness : mWatchFacePreset.mFourTickThickness;
    }

    void setSixtyTickThickness(TickThickness sixtyTickThickness) {
        mWatchFacePreset.mSixtyTickThickness = sixtyTickThickness;
    }

    public TickRadiusPosition getFourTickRadiusPosition() {
        return mWatchFacePreset.mFourTickRadiusPosition;
    }

    void setFourTickRadiusPosition(TickRadiusPosition fourTickRadiusPosition) {
        mWatchFacePreset.mFourTickRadiusPosition = fourTickRadiusPosition;
    }

    public TickRadiusPosition getTwelveTickRadiusPosition() {
        return mWatchFacePreset.mTwelveTickOverride ?
                mWatchFacePreset.mTwelveTickRadiusPosition :
                mWatchFacePreset.mFourTickRadiusPosition;
    }

    void setTwelveTickRadiusPosition(TickRadiusPosition twelveTickRadiusPosition) {
        mWatchFacePreset.mTwelveTickRadiusPosition = twelveTickRadiusPosition;
    }

    public TickRadiusPosition getSixtyTickRadiusPosition() {
        return mWatchFacePreset.mSixtyTickOverride ?
                mWatchFacePreset.mSixtyTickRadiusPosition : mWatchFacePreset.mFourTickRadiusPosition;
    }

    void setSixtyTickRadiusPosition(TickRadiusPosition sixtyTickRadiusPosition) {
        mWatchFacePreset.mSixtyTickRadiusPosition = sixtyTickRadiusPosition;
    }

    public Style getFourTickStyle() {
        return mWatchFacePreset.mFourTickStyle;
    }

    void setFourTickStyle(Style fourTickStyle) {
        mWatchFacePreset.mFourTickStyle = fourTickStyle;
    }

    public Style getTwelveTickStyle() {
        return mWatchFacePreset.mTwelveTickOverride ?
                mWatchFacePreset.mTwelveTickStyle : mWatchFacePreset.mFourTickStyle;
    }

    void setTwelveTickStyle(Style twelveTickStyle) {
        mWatchFacePreset.mTwelveTickStyle = twelveTickStyle;
    }

    public Style getSixtyTickStyle() {
        return mWatchFacePreset.mSixtyTickOverride ?
                mWatchFacePreset.mSixtyTickStyle : mWatchFacePreset.mFourTickStyle;
    }

    void setSixtyTickStyle(Style sixtyTickStyle) {
        mWatchFacePreset.mSixtyTickStyle = sixtyTickStyle;
    }

    GradientStyle getFillHighlightStyle() {
        return mWatchFacePreset.mFillHighlightStyle;
    }

    void setFillHighlightStyle(GradientStyle fillHighlightStyle) {
        mWatchFacePreset.mFillHighlightStyle = fillHighlightStyle;
        regeneratePaints();
    }

    GradientStyle getAccentFillStyle() {
        return mWatchFacePreset.mAccentFillStyle;
    }

    void setAccentFillStyle(GradientStyle accentFillStyle) {
        mWatchFacePreset.mAccentFillStyle = accentFillStyle;
        regeneratePaints();
    }

    GradientStyle getAccentHighlightStyle() {
        return mWatchFacePreset.mAccentHighlightStyle;
    }

    void setAccentHighlightStyle(GradientStyle accentHighlightStyle) {
        mWatchFacePreset.mAccentHighlightStyle = accentHighlightStyle;
        regeneratePaints();
    }

    GradientStyle getBaseAccentStyle() {
        return mWatchFacePreset.mBaseAccentStyle;
    }

    void setBaseAccentStyle(GradientStyle baseAccentStyle) {
        mWatchFacePreset.mBaseAccentStyle = baseAccentStyle;
        regeneratePaints();
    }

    public HandStalk getHourHandStalk() {
        return mWatchFacePreset.mHourHandStalk;
    }

    void setHourHandStalk(HandStalk hourHandStalk) {
        mWatchFacePreset.mHourHandStalk = hourHandStalk;
    }

    public HandStalk getMinuteHandStalk() {
        return mWatchFacePreset.mMinuteHandOverride ?
                mWatchFacePreset.mMinuteHandStalk : mWatchFacePreset.mHourHandStalk;
    }

    void setMinuteHandStalk(HandStalk minuteHandStalk) {
        mWatchFacePreset.mMinuteHandStalk = minuteHandStalk;
    }

    public HandCutout getHourHandCutout() {
        return mWatchFacePreset.mHourHandCutout;
    }

    void setHourHandCutout(HandCutout hourHandCutout) {
        mWatchFacePreset.mHourHandCutout = hourHandCutout;
    }

    public HandCutout getMinuteHandCutout() {
        return mWatchFacePreset.mMinuteHandOverride ?
                mWatchFacePreset.mMinuteHandCutout : mWatchFacePreset.mHourHandCutout;
    }

    void setMinuteHandCutout(HandCutout minuteHandCutout) {
        mWatchFacePreset.mMinuteHandCutout = minuteHandCutout;
    }

    int getFillSixBitColor() {
        return mWatchFacePreset.mFillSixBitColor;
    }

    void setFillSixBitColor(int fillSixBitColor) {
        mWatchFacePreset.mFillSixBitColor = fillSixBitColor;
        regeneratePaints();
    }

    int getAccentSixBitColor() {
        return mWatchFacePreset.mAccentSixBitColor;
    }

    void setAccentSixBitColor(int accentSixBitColor) {
        mWatchFacePreset.mAccentSixBitColor = accentSixBitColor;
        regeneratePaints();
    }

    int getHighlightSixBitColor() {
        return mWatchFacePreset.mHighlightSixBitColor;
    }

    void setHighlightSixBitColor(int highlightSixBitColor) {
        mWatchFacePreset.mHighlightSixBitColor = highlightSixBitColor;
        regeneratePaints();
    }

    int getBaseSixBitColor() {
        return mWatchFacePreset.mBaseSixBitColor;
    }

    void setBaseSixBitColor(int baseSixBitColor) {
        mWatchFacePreset.mBaseSixBitColor = baseSixBitColor;
        regeneratePaints();
    }
    // endregion

    // region PaintBox
    private void regeneratePaints() {
        mPaintBox.regeneratePaints(
                getFillSixBitColor(), getAccentSixBitColor(),
                getHighlightSixBitColor(), getBaseSixBitColor(),
                getAmbientDaySixBitColor(), getAmbientNightSixBitColor(),
                getFillHighlightStyle(), getAccentFillStyle(),
                getAccentHighlightStyle(), getBaseAccentStyle());
    }

    /**
     * Get the given color from our 6-bit (64-color) palette. Returns a ColorInt.
     *
     * @param colorType ColorType to get from our current WatchFacePreset.
     * @return Color from our palette as a ColorInt
     */
    @ColorInt
    public int getColor(ColorType colorType) {
        switch (colorType) {
            case FILL: {
                return mPaintBox.getColor(getFillSixBitColor());
            }
            case ACCENT: {
                return mPaintBox.getColor(getAccentSixBitColor());
            }
            case HIGHLIGHT: {
                return mPaintBox.getColor(getHighlightSixBitColor());
            }
            case BASE: {
                return mPaintBox.getColor(getBaseSixBitColor());
            }
            case AMBIENT_DAY: {
                return mPaintBox.getColor(getAmbientDaySixBitColor());
            }
            default:
            case AMBIENT_NIGHT: {
                return mPaintBox.getColor(getAmbientNightSixBitColor());
            }
        }
    }

    public void setSixBitColor(ColorType colorType, @ColorInt int sixBitColor) {
        switch (colorType) {
            case FILL: {
                setFillSixBitColor(sixBitColor);
                break;
            }
            case ACCENT: {
                setAccentSixBitColor(sixBitColor);
                break;
            }
            case HIGHLIGHT: {
                setHighlightSixBitColor(sixBitColor);
                break;
            }
            case BASE: {
                setBaseSixBitColor(sixBitColor);
                break;
            }
            case AMBIENT_DAY: {
                setAmbientDaySixBitColor(sixBitColor);
                break;
            }
            case AMBIENT_NIGHT: {
                setAmbientNightSixBitColor(sixBitColor);
                break;
            }
            default: {
                break;
            }
        }
    }
    // endregion
}
