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
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface.Builder;
import android.os.Build;
import android.support.wearable.complications.ComplicationData;
import android.util.Log;

import androidx.annotation.ArrayRes;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;

import pro.watchkit.wearable.watchface.R;
import pro.watchkit.wearable.watchface.model.BytePackable.ComplicationCount;
import pro.watchkit.wearable.watchface.model.BytePackable.ComplicationOverlap;
import pro.watchkit.wearable.watchface.model.BytePackable.ComplicationRotation;
import pro.watchkit.wearable.watchface.model.BytePackable.ComplicationSize;
import pro.watchkit.wearable.watchface.model.BytePackable.DigitDisplay;
import pro.watchkit.wearable.watchface.model.BytePackable.DigitFormat;
import pro.watchkit.wearable.watchface.model.BytePackable.DigitRotation;
import pro.watchkit.wearable.watchface.model.BytePackable.DigitSize;
import pro.watchkit.wearable.watchface.model.BytePackable.HandCutout;
import pro.watchkit.wearable.watchface.model.BytePackable.HandLength;
import pro.watchkit.wearable.watchface.model.BytePackable.HandShape;
import pro.watchkit.wearable.watchface.model.BytePackable.HandStalk;
import pro.watchkit.wearable.watchface.model.BytePackable.HandThickness;
import pro.watchkit.wearable.watchface.model.BytePackable.Style;
import pro.watchkit.wearable.watchface.model.BytePackable.StyleGradient;
import pro.watchkit.wearable.watchface.model.BytePackable.StyleTexture;
import pro.watchkit.wearable.watchface.model.BytePackable.TextStyle;
import pro.watchkit.wearable.watchface.model.BytePackable.TickLength;
import pro.watchkit.wearable.watchface.model.BytePackable.TickMargin;
import pro.watchkit.wearable.watchface.model.BytePackable.TickShape;
import pro.watchkit.wearable.watchface.model.BytePackable.TickThickness;
import pro.watchkit.wearable.watchface.model.BytePackable.TicksDisplay;
import pro.watchkit.wearable.watchface.model.BytePackable.Typeface;
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
    @NonNull
    private final PaintBox mPaintBox;
    private final Collection<ComplicationHolder> mComplications = new ArrayList<>();
    private final Map<Integer, ComplicationHolder> mComplicationMap = new Hashtable<>();
    private int mUnreadNotifications = 0;
    private int mTotalNotifications = 0;
    private boolean mAmbient = false;
    private final GregorianCalendar mCalendar = new GregorianCalendar();
    private final LocationCalculator mLocationCalculator = new LocationCalculator(mCalendar);
    private final Context mContext;

    @Override
    public int hashCode() {
        return Objects.hash(
                mWatchFacePreset,
                mSettings,
                mPaintBox,
                mComplications,
                mUnreadNotifications,
                mTotalNotifications);
    }

    @NonNull
    private StringBuilder mStringBuilder = new StringBuilder();

    public WatchFaceState(Context context) {
        mPaintBox = new PaintBox(context);
        mContext = context;
        // Hmm. Strictly temporary: how about a default setting?
        // setString("fcd81c000c0100000006c06a60000001~3cda1cc0000000000000000000000001");
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
            int complicationId, @NonNull ComplicationData complicationData) {
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

    @Nullable
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
                float a = (float) Math.sqrt((double) (
                        ((xb - xc) * (xb - xc)) + ((yb - yc) * (yb - yc))));
                float b = radius; // By definition.
                float c = radius;

                // The centre and radius of the incircle.
                float incentreX = (a * xa + b * xb + c * xc) / (a + b + c);
                float incentreY = (a * ya + b * yb + c * yc) / (a + b + c);
                float s = (a + b + c) / 2f;
                float inradius = (float) Math.sqrt((double) (s * (s - a) * (s - b) * (s - c))) / s;

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

    @NonNull
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
        @ColorInt int activeColor = getColor(getComplicationTextStyle());
        @ColorInt int ambientColor = Color.WHITE;
        @Nullable android.graphics.Typeface typeface = getTypefaceObject();

        mComplications.forEach(c -> c.setColors(activeColor, ambientColor, typeface));
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

    @NonNull
    public PaintBox getPaintBox() {
        regeneratePaints();
        return mPaintBox;
    }

    // region Ephemeral
    @Nullable
    private Style mSwatchStyle;
    @Nullable
    private TextStyle mSwatchTextStyle;

    /**
     * Determines whether the two given WatchFaceState strings are mostly equal. That is, whether
     * they're equal in all things such as WatchFacePreset and Settings, but don't compare
     * ephemeral settings that don't matter.
     *
     * @param a First string to compare
     * @param b Second string to compare
     * @return Whether the strings are mostly equal
     */
    public static boolean mostlyEquals(@NonNull String a, @NonNull String b) {
        String[] aSplit = a.split("~");
        String[] bSplit = b.split("~");

        if (aSplit.length < 2 || bSplit.length < 2) return false;

        return aSplit[0].equals(bSplit[0]) && aSplit[1].equals(bSplit[1]);
    }
    // endregion

    @NonNull
    public String getString() {
        // Turn "mSwatchTextStyle" or "mSwatchStyle" into an index.
        // I'm not proud of this code, by the way.
        int swatchStyleIndex;
        if (mSwatchTextStyle == TextStyle.FILL) {
            swatchStyleIndex = 0;
        } else if (mSwatchTextStyle == TextStyle.ACCENT) {
            swatchStyleIndex = 1;
        } else if (mSwatchTextStyle == TextStyle.HIGHLIGHT) {
            swatchStyleIndex = 2;
        } else if (mSwatchTextStyle == TextStyle.BASE) {
            swatchStyleIndex = 3;
        } else if (mSwatchStyle == Style.FILL_HIGHLIGHT) {
            swatchStyleIndex = 4;
        } else if (mSwatchStyle == Style.ACCENT_FILL) {
            swatchStyleIndex = 5;
        } else if (mSwatchStyle == Style.ACCENT_HIGHLIGHT) {
            swatchStyleIndex = 6;
        } else {
            swatchStyleIndex = 7;
        }

        mStringBuilder.setLength(0);
        mStringBuilder.append(mWatchFacePreset.getString())
                .append("~")
                .append(mSettings.getString())
                .append("~")
                .append(swatchStyleIndex);
        return mStringBuilder.toString();
    }

    // region Settings
    Typeface getTypeface() {
        return mSettings.mTypeface;
    }

    void setTypeface(Typeface typeface) {
        mSettings.mTypeface = typeface;
        regeneratePaints();
    }

    /**
     * Is the currently-set typeface installed and available for use?
     *
     * @return Whether the typeface is available
     */
    boolean isTypefaceAvailable() {
        return getTypefaceObject() != null;
    }

    private android.graphics.Typeface mTypeface_DROID_SANS;
    private android.graphics.Typeface mTypeface_DROID_SANS_BOLD;
    private android.graphics.Typeface mTypeface_NOTO_SERIF;
    private android.graphics.Typeface mTypeface_NOTO_SERIF_BOLD;
    private android.graphics.Typeface mTypeface_DROID_SANS_MONO;
    private android.graphics.Typeface mTypeface_ROBOTO;
    private android.graphics.Typeface mTypeface_ROBOTO_BOLD;
    private android.graphics.Typeface mTypeface_ROBOTO_BLACK;
    private android.graphics.Typeface mTypeface_PRODUCT_SANS;
    private android.graphics.Typeface mTypeface_PRODUCT_SANS_BOLD;

    @Nullable
    private android.graphics.Typeface getTypefaceObject() {
        Typeface temp = Typeface.NOTO_SERIF_BOLD;
        if (Build.VERSION.SDK_INT >= 26) {
            // For API 26 and above, we can attempt to get most cool fonts.
            switch (temp) {
//            switch (getTypeface()) {
                case DROID_SANS: {
                    if (mTypeface_DROID_SANS == null) {
                        Builder b = new Builder("/system/fonts/DroidSans.ttf");
                        mTypeface_DROID_SANS = b.build();
                    }
                    if (mTypeface_DROID_SANS == null) {
                        mTypeface_DROID_SANS = android.graphics.Typeface.DEFAULT;
                    }
                    return mTypeface_DROID_SANS;
                }
                case DROID_SANS_BOLD: {
                    if (mTypeface_DROID_SANS_BOLD == null) {
                        Builder b = new Builder("/system/fonts/DroidSans-Bold.ttf");
                        mTypeface_DROID_SANS_BOLD = b.build();
                    }
                    if (mTypeface_DROID_SANS_BOLD == null) {
                        mTypeface_DROID_SANS_BOLD = android.graphics.Typeface.DEFAULT_BOLD;
                    }
                    return mTypeface_DROID_SANS;
                }
                case NOTO_SERIF: {
                    if (mTypeface_NOTO_SERIF == null) {
                        Builder b = new Builder("/system/fonts/NotoSerif-Regular.ttf");
                        mTypeface_NOTO_SERIF = b.build();
                    }
                    if (mTypeface_NOTO_SERIF == null) {
                        mTypeface_NOTO_SERIF = android.graphics.Typeface.SERIF;
                    }
                    return mTypeface_NOTO_SERIF;
                }
                case NOTO_SERIF_BOLD: {
                    if (mTypeface_NOTO_SERIF_BOLD == null) {
                        Builder b = new Builder("/system/fonts/NotoSerif-Bold.ttf");
                        mTypeface_NOTO_SERIF_BOLD = b.build();
                    }
                    return mTypeface_NOTO_SERIF_BOLD;
                }
                case DROID_SANS_MONO: {
                    if (mTypeface_NOTO_SERIF_BOLD == null) {
                        Builder b = new Builder("/system/fonts/DroidSans-Mono.ttf");
                        mTypeface_NOTO_SERIF_BOLD = b.build();
                    }
                    if (mTypeface_DROID_SANS_MONO == null) {
                        mTypeface_DROID_SANS_MONO = android.graphics.Typeface.MONOSPACE;
                    }
                    return mTypeface_DROID_SANS_MONO;
                }
                case ROBOTO: {
                    if (mTypeface_ROBOTO == null) {
                        Builder b = new Builder("/system/fonts/Roboto-Regular.ttf");
                        mTypeface_ROBOTO = b.build();
                    }
                    return mTypeface_ROBOTO;
                }
                case ROBOTO_BOLD: {
                    if (mTypeface_ROBOTO_BOLD == null) {
                        Builder b = new Builder("/system/fonts/Roboto-Bold.ttf");
                        mTypeface_ROBOTO_BOLD = b.build();
                    }
                    return mTypeface_ROBOTO_BOLD;
                }
                case ROBOTO_BLACK: {
                    if (mTypeface_ROBOTO_BLACK == null) {
                        Builder b = new Builder("/system/fonts/Roboto-Black.ttf");
                        mTypeface_ROBOTO_BLACK = b.build();
                    }
                    return mTypeface_ROBOTO_BLACK;
                }
                case PRODUCT_SANS: {
                    if (mTypeface_PRODUCT_SANS == null) {
                        Builder b = new Builder("/system/fonts/GoogleSans-Regular.ttf");
                        mTypeface_PRODUCT_SANS = b.build();
                    }
                    return mTypeface_PRODUCT_SANS;
                }
                case PRODUCT_SANS_BOLD: {
                    if (mTypeface_PRODUCT_SANS_BOLD == null) {
                        Builder b = new Builder("/system/fonts/GoogleSans-Bold.ttf");
                        mTypeface_PRODUCT_SANS_BOLD = b.build();
                    }
                    return mTypeface_PRODUCT_SANS_BOLD;
                }
                default: {
                    return android.graphics.Typeface.DEFAULT;
                }
            }
        } else {
            // For API 25 and older, just get the regular uncool fonts.
            switch (getTypeface()) {
                case DROID_SANS: {
                    return android.graphics.Typeface.DEFAULT;
                }
                case DROID_SANS_BOLD: {
                    return android.graphics.Typeface.DEFAULT_BOLD;
                }
                case NOTO_SERIF: {
                    return android.graphics.Typeface.SERIF;
                }
                case NOTO_SERIF_BOLD: {
                    return null;
                }
                case DROID_SANS_MONO: {
                    return android.graphics.Typeface.MONOSPACE;
                }
                case ROBOTO: {
                    return null;
                }
                case ROBOTO_BOLD: {
                    return null;
                }
                case ROBOTO_BLACK: {
                    return null;
                }
                case PRODUCT_SANS: {
                    return null;
                }
                case PRODUCT_SANS_BOLD: {
                    return null;
                }
                default: {
                    return android.graphics.Typeface.DEFAULT;
                }
            }
        }
    }

    ComplicationRotation getComplicationRotation() {
        return mSettings.mComplicationRotation;
    }

    void setComplicationRotation(ComplicationRotation complicationRotation) {
        mSettings.mComplicationRotation = complicationRotation;
    }

    private int getComplicationCountInt() {
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

    ComplicationSize getComplicationSize() {
        return mSettings.mComplicationSize;
    }

    void setComplicationSize(ComplicationSize complicationSize) {
        mSettings.mComplicationSize = complicationSize;
    }

    public ComplicationOverlap getComplicationOverlap() {
        return mSettings.mComplicationOverlap;
    }

    void setComplicationOverlap(ComplicationOverlap complicationOverlap) {
        mSettings.mComplicationOverlap = complicationOverlap;
    }

    public boolean isShowUnreadNotifications() {
        return mSettings.mShowUnreadNotifications;
    }

    void setShowUnreadNotifications(boolean showUnreadNotifications) {
        mSettings.mShowUnreadNotifications = showUnreadNotifications;
    }

    @SuppressWarnings("unused")
    public boolean isNightVisionModeEnabled() {
        // I mean, technically this is unused. If we don't want night vision, just set the night
        // vision color to the same as day. But we need to find a place to request the location
        // permission. TODO...
        return mSettings.mNightVisionModeEnabled;
    }

    void setNightVisionModeEnabled(boolean nightVisionModeEnabled) {
        mSettings.mNightVisionModeEnabled = nightVisionModeEnabled;
    }

    private int getAmbientDaySixBitColor() {
        return mSettings.mAmbientDaySixBitColor;
    }

    private void setAmbientDaySixBitColor(int ambientDaySixBitColor) {
        mSettings.mAmbientDaySixBitColor = ambientDaySixBitColor;
        regeneratePaints();
    }

    private int getAmbientNightSixBitColor() {
        return mSettings.mAmbientNightSixBitColor;
    }

    private void setAmbientNightSixBitColor(int ambientNightSixBitColor) {
        mSettings.mAmbientNightSixBitColor = ambientNightSixBitColor;
        regeneratePaints();
    }

    public TextStyle getComplicationTextStyle() {
        return mSettings.mComplicationTextStyle;
    }

    void setComplicationTextStyle(TextStyle complicationTextStyle) {
        mSettings.mComplicationTextStyle = complicationTextStyle;
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

    void setDeveloperMode(boolean developerMode) {
        mSettings.mDeveloperMode = developerMode;
    }

    public boolean isStats() {
        return mSettings.mStats;
    }

    void setStats(boolean stats) {
        mSettings.mStats = stats;
    }

    public boolean isStatsDetail() {
        return mSettings.mStatsDetail;
    }

    void setStatsDetail(boolean statsDetail) {
        mSettings.mStatsDetail = statsDetail;
    }

    public boolean isHideTicks() {
        return mSettings.mHideTicks;
    }

    void setHideTicks(boolean hideTicks) {
        mSettings.mHideTicks = hideTicks;
    }

    public boolean isHideHands() {
        return mSettings.mHideHands;
    }

    void setHideHands(boolean hideHands) {
        mSettings.mHideHands = hideHands;
    }

    public boolean isAltDrawing() {
        return mSettings.mAltDrawing;
    }

    void setAltDrawing(boolean altDrawing) {
        mSettings.mAltDrawing = altDrawing;
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

    public void setString(@Nullable String s) {
        if (s == null || s.length() == 0) return;
        String[] split = s.split("~");
        if (split.length >= 2) {
            mWatchFacePreset.setString(split[0]);
            mSettings.setString(split[1]);
        }
        if (split.length >= 3) {
            // There's probably a cleaner way of doing this. We'll come back to it...
            switch (split[2]) {
                case "0": {
                    setSwatchStyle(TextStyle.FILL);
                    break;
                }
                case "1": {
                    setSwatchStyle(TextStyle.ACCENT);
                    break;
                }
                case "2": {
                    setSwatchStyle(TextStyle.HIGHLIGHT);
                    break;
                }
                case "3": {
                    setSwatchStyle(TextStyle.BASE);
                    break;
                }
                case "4": {
                    setSwatchStyle(Style.FILL_HIGHLIGHT);
                    break;
                }
                case "5": {
                    setSwatchStyle(Style.ACCENT_FILL);
                    break;
                }
                case "6": {
                    setSwatchStyle(Style.ACCENT_HIGHLIGHT);
                    break;
                }
                default:
                case "7": {
                    setSwatchStyle(Style.BASE_ACCENT);
                    break;
                }
            }
        }
        regeneratePaints();
    }

    @NonNull
    public Style getBackgroundStyle() {
        return Style.BASE_ACCENT; // Hard-coded!
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

    @NonNull
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

    @NonNull
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

    @NonNull
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

    void setBackgroundStyle(Style backgroundStyle) {
//        mWatchFacePreset.mBackgroundStyle = backgroundStyle;
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

    public TickMargin getTickMargin() {
        return mWatchFacePreset.mTickMargin;
    }

    void setTickMargin(TickMargin tickMargin) {
        mWatchFacePreset.mTickMargin = tickMargin;
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

    public Style getTickBackgroundStyle() {
        return mWatchFacePreset.mTickBackgroundStyle;
    }

    void setTickBackgroundStyle(Style tickBackgroundStyle) {
        mWatchFacePreset.mTickBackgroundStyle = tickBackgroundStyle;
    }

    boolean isDigitVisible() {
        return getDigitDisplay() != DigitDisplay.NONE;
    }

    public Style getDigitStyle() {
        return mWatchFacePreset.mDigitStyle;
    }

    void setDigitStyle(Style digitStyle) {
        mWatchFacePreset.mDigitStyle = digitStyle;
    }

    public DigitDisplay getDigitDisplay() {
        return mWatchFacePreset.mDigitDisplay;
    }

    void setDigitDisplay(DigitDisplay digitDisplay) {
        mWatchFacePreset.mDigitDisplay = digitDisplay;
    }

    public DigitSize getDigitSize() {
        return mWatchFacePreset.mDigitSize;
    }

    void setDigitSize(DigitSize digitSize) {
        mWatchFacePreset.mDigitSize = digitSize;
        regeneratePaints();
    }

    public DigitRotation getDigitRotation() {
        return mWatchFacePreset.mDigitRotation;
    }

    void setDigitRotation(DigitRotation digitRotation) {
        mWatchFacePreset.mDigitRotation = digitRotation;
    }

    @NonNull
    public String[] getDigitFormatLabels() {
        @ArrayRes int labelRes;
        switch (getDigitFormat()) {
            case NUMERALS_12_4: {
                labelRes = R.array.WatchFacePreset_DigitFormat_NUMERALS_12_4;
                break;
            }
            case NUMERALS_12_12: {
                labelRes = R.array.WatchFacePreset_DigitFormat_NUMERALS_12_12;
                break;
            }
            case NUMERALS_60: {
                labelRes = R.array.WatchFacePreset_DigitFormat_NUMERALS_60;
                break;
            }
            default:
            case ROMAN: {
                labelRes = R.array.WatchFacePreset_DigitFormat_ROMAN;
                break;
            }
        }
        return mContext.getResources().getStringArray(labelRes);
    }

    public DigitFormat getDigitFormat() {
        return mWatchFacePreset.mDigitFormat;
    }

    void setDigitFormat(DigitFormat digitFormat) {
        mWatchFacePreset.mDigitFormat = digitFormat;
    }

    StyleGradient getFillHighlightStyleGradient() {
        return mWatchFacePreset.mFillHighlightStyleGradient;
    }

    void setFillHighlightStyleGradient(StyleGradient fillHighlightStyleGradient) {
        mWatchFacePreset.mFillHighlightStyleGradient = fillHighlightStyleGradient;
        setSwatchStyle(Style.FILL_HIGHLIGHT);
        regeneratePaints();
    }

    StyleGradient getAccentFillStyleGradient() {
        return mWatchFacePreset.mAccentFillStyleGradient;
    }

    void setAccentFillStyleGradient(StyleGradient accentFillStyleGradient) {
        mWatchFacePreset.mAccentFillStyleGradient = accentFillStyleGradient;
        setSwatchStyle(Style.ACCENT_FILL);
        regeneratePaints();
    }

    StyleGradient getAccentHighlightStyleGradient() {
        return mWatchFacePreset.mAccentHighlightStyleGradient;
    }

    void setAccentHighlightStyleGradient(StyleGradient accentHighlightStyleGradient) {
        mWatchFacePreset.mAccentHighlightStyleGradient = accentHighlightStyleGradient;
        setSwatchStyle(Style.ACCENT_HIGHLIGHT);
        regeneratePaints();
    }

    StyleGradient getBaseAccentStyleGradient() {
        return mWatchFacePreset.mBaseAccentStyleGradient;
    }

    void setBaseAccentStyleGradient(StyleGradient baseAccentStyleGradient) {
        mWatchFacePreset.mBaseAccentStyleGradient = baseAccentStyleGradient;
        setSwatchStyle(Style.BASE_ACCENT);
        regeneratePaints();
    }

    StyleTexture getFillHighlightStyleTexture() {
        return mWatchFacePreset.mFillHighlightStyleTexture;
    }

    void setFillHighlightStyleTexture(StyleTexture fillHighlightStyleTexture) {
        mWatchFacePreset.mFillHighlightStyleTexture = fillHighlightStyleTexture;
        setSwatchStyle(Style.FILL_HIGHLIGHT);
        regeneratePaints();
    }

    StyleTexture getAccentFillStyleTexture() {
        return mWatchFacePreset.mAccentFillStyleTexture;
    }

    void setAccentFillStyleTexture(StyleTexture accentFillStyleTexture) {
        mWatchFacePreset.mAccentFillStyleTexture = accentFillStyleTexture;
        setSwatchStyle(Style.ACCENT_FILL);
        regeneratePaints();
    }

    StyleTexture getAccentHighlightStyleTexture() {
        return mWatchFacePreset.mAccentHighlightStyleTexture;
    }

    void setAccentHighlightStyleTexture(StyleTexture accentHighlightStyleTexture) {
        mWatchFacePreset.mAccentHighlightStyleTexture = accentHighlightStyleTexture;
        setSwatchStyle(Style.ACCENT_HIGHLIGHT);
        regeneratePaints();
    }

    StyleTexture getBaseAccentStyleTexture() {
        return mWatchFacePreset.mBaseAccentStyleTexture;
    }

    void setBaseAccentStyleTexture(StyleTexture baseAccentStyleTexture) {
        mWatchFacePreset.mBaseAccentStyleTexture = baseAccentStyleTexture;
        setSwatchStyle(Style.BASE_ACCENT);
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

    private int getFillSixBitColor() {
        return mWatchFacePreset.mFillSixBitColor;
    }

    private void setFillSixBitColor(int fillSixBitColor) {
        mWatchFacePreset.mFillSixBitColor = fillSixBitColor;
        regeneratePaints();
    }

    private int getAccentSixBitColor() {
        return mWatchFacePreset.mAccentSixBitColor;
    }

    private void setAccentSixBitColor(int accentSixBitColor) {
        mWatchFacePreset.mAccentSixBitColor = accentSixBitColor;
        regeneratePaints();
    }

    private int getHighlightSixBitColor() {
        return mWatchFacePreset.mHighlightSixBitColor;
    }

    private void setHighlightSixBitColor(int highlightSixBitColor) {
        mWatchFacePreset.mHighlightSixBitColor = highlightSixBitColor;
        regeneratePaints();
    }

    private int getBaseSixBitColor() {
        return mWatchFacePreset.mBaseSixBitColor;
    }

    private void setBaseSixBitColor(int baseSixBitColor) {
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
                getFillHighlightStyleGradient(), getAccentFillStyleGradient(),
                getAccentHighlightStyleGradient(), getBaseAccentStyleGradient(),
                getFillHighlightStyleTexture(), getAccentFillStyleTexture(),
                getAccentHighlightStyleTexture(), getBaseAccentStyleTexture(),
                getDigitSize(),
                getTypefaceObject());
    }

    /**
     * Get the given color from our 6-bit (64-color) palette. Returns a ColorInt.
     *
     * @param colorType ColorType to get from our current WatchFacePreset.
     * @return Color from our palette as a ColorInt
     */
    @ColorInt
    public int getColor(@NonNull ColorType colorType) {
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

    /**
     * Get the given color from our 6-bit (64-color) palette. Returns a ColorInt.
     *
     * @param textStyle ColorType to get from our current WatchFacePreset.
     * @return Color from our palette as a ColorInt
     */
    @ColorInt
    public int getColor(@NonNull TextStyle textStyle) {
        switch (textStyle) {
            case FILL: {
                return getColor(ColorType.FILL);
            }
            case ACCENT: {
                return getColor(ColorType.ACCENT);
            }
            case HIGHLIGHT: {
                return getColor(ColorType.HIGHLIGHT);
            }
            default:
            case BASE: {
                return getColor(ColorType.BASE);
            }
        }
    }

    public void setSixBitColor(@NonNull ColorType colorType, @ColorInt int sixBitColor) {
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

    @NonNull
    public Style getSecondHandStyle() {
        // If not overridden, the default is just a plain and regular second hand.
        return mWatchFacePreset.mSecondHandOverride ?
                mWatchFacePreset.mSecondHandStyle : Style.ACCENT_HIGHLIGHT;
    }

    @NonNull
    public Paint getSwatchPaint() {
        if (mSwatchStyle != null) {
            return getPaintBox().getPaintFromPreset(mSwatchStyle);
        } else if (mSwatchTextStyle != null) {
            return getPaintBox().getPaintFromPreset(mSwatchTextStyle);
        } else {
            // Default option...
            return getPaintBox().getPaintFromPreset(Style.BASE_ACCENT);
        }
    }

    void setSwatchStyle(@NonNull TextStyle swatchTextStyle) {
        mSwatchStyle = null;
        mSwatchTextStyle = swatchTextStyle;
    }

    void setSwatchStyle(@NonNull Style swatchStyle) {
        mSwatchStyle = swatchStyle;
        mSwatchTextStyle = null;
    }
    // endregion

    // region Dimensions

    private float getBandStart() {
        switch (getTickMargin()) {
            case NONE: {
                return 0f;
            }
            case SMALL: {
                return 5f;
            }
            case MEDIUM: {
                return 10f;
            }
            default:
            case LARGE: {
                return 15f;
            }
        }
    }

    public float getTickBandStart(float pc) {
        if (getDigitDisplay() == DigitDisplay.ABOVE) {
            return getBandStart() + getDigitBandHeight(pc);
        } else {
            return getBandStart();
        }
    }

    public float getDigitBandStart(float pc) {
        if (getDigitDisplay() == DigitDisplay.BELOW) {
            return getBandStart() + getTickBandHeight(pc);
        } else {
            return getBandStart();
        }
    }

    /**
     * Get the tick band height, which is the longest of the four, twelve and sixty ticks (and the
     * digits too if we're drawing them here).
     *
     * @param pc
     * @return
     */
    public float getTickBandHeight(float pc) {
        float fourTickHeight = getTickHalfLength(getFourTickShape(), getFourTickLength());
        float twelveTickHeight = getTickHalfLength(getTwelveTickShape(), getTwelveTickLength());
        float sixtyTickHeight = getTickHalfLength(getSixtyTickShape(), getSixtyTickLength());

        float result = 2f + // Add 2f padding, which is 1f on top and bottom
                2f * Math.max(fourTickHeight, Math.max(twelveTickHeight, sixtyTickHeight));

        if (getDigitDisplay() == DigitDisplay.OVER) {
            return Math.max(result, getDigitBandHeight(pc));
        } else {
            return result;
        }
    }

    private final Rect mLabelRect = new Rect();

    public float getDigitBandHeight(float pc) {
        String[] labels = getDigitFormatLabels();
        Style style = getDigitStyle();
        Paint paint = getPaintBox().getPaintFromPreset(style);
        float result = 0f;

        for (int i = 0; i < 12; i++) {
            String label = labels[i];
            if (label == null || label.length() == 0) {
                // Don't worry about empty labels...
                continue;
            }

            // Get the size (bounds) of the label we're trying to draw.
            paint.getTextBounds(label, 0, label.length(), mLabelRect);

            if (getDigitRotation() == DigitRotation.CURVED) {
                // For curved, take the band size as the height.
                result = Math.max(result, (float) mLabelRect.height());
            } else {
                // For straight, take the band size as the diagonal length (worst-case scenario)
                // = √(width² + height²)
                result = Math.max(result, (float) Math.sqrt(
                        (double) (mLabelRect.height() * mLabelRect.height() +
                                mLabelRect.width() * mLabelRect.width())));
            }
        }

        return result / pc; // Convert back from pixels to percentage
    }

    public float getTickHalfLength(TickShape tickShape, TickLength tickLength) {
        float barLengthScale = 3f;
        float triangleFactor = (float) (Math.sqrt(3d) / 2d); // Height of an equilateral triangle.

        float globalScale = 1.0f;

        // f0, f1, f2, f3 are a geometric series!
        float f0 = globalScale * (float) (1d / Math.sqrt(2d));
        float f1 = globalScale * 1f;
        float f2 = globalScale * (float) Math.sqrt(2d);
        float f3 = globalScale * 2f;

        // Scaling factors for dot, triangle and diamond.
        // Relative to a square of side 1. So all greater than 1.
        float dotScale = 2f / (float) Math.sqrt(Math.PI);
        float triangleScale = 2f / (float) Math.sqrt(Math.sqrt(3d));
        float diamondScale = (float) Math.sqrt(2d);

        float result;
        switch (tickShape) {
            case BAR: {
                result = barLengthScale;
                break;
            }
            case DOT: {
                result = dotScale;
                break;
            }
            case TRIANGLE: {
                result = triangleFactor * triangleScale;
                break;
            }
            default:
            case DIAMOND: {
                result = diamondScale;
                break;
            }
        }
        switch (tickLength) {
            case SHORT: {
                result *= f0;
                break;
            }
            case MEDIUM: {
                result *= f1;
                break;
            }
            case LONG: {
                result *= f2;
                break;
            }
            default:
            case X_LONG: {
                result *= f3;
                break;
            }
        }

        return result;
    }

    public float getTickThickness(TickShape tickShape, TickThickness tickThickness) {
        float barThicknessScale = (float) (Math.PI / 120d);

        float globalScale = 1.0f;

        // f0, f1, f2, f3 are a geometric series!
        float f0 = globalScale * (float) (1d / Math.sqrt(2d));
        float f1 = globalScale * 1f;
        float f2 = globalScale * (float) Math.sqrt(2d);
        float f3 = globalScale * 2f;

        // Scaling factors for dot, triangle and diamond.
        // Relative to a square of side 1. So all greater than 1.
        float dotScale = 2f / (float) Math.sqrt(Math.PI);
        float triangleScale = 2f / (float) Math.sqrt(Math.sqrt(3d));
        float diamondScale = (float) Math.sqrt(2d);

        float result;
        switch (tickShape) {
            case BAR: {
                result = barThicknessScale;
                break;
            }
            case DOT: {
                result = dotScale;
                break;
            }
            case TRIANGLE: {
                result = triangleScale;
                break;
            }
            default:
            case DIAMOND: {
                result = diamondScale;
                break;
            }
        }
        switch (tickThickness) {
            case THIN: {
                result *= f0;
                break;
            }
            case REGULAR: {
                result *= f1;
                break;
            }
            case THICK: {
                result *= f2;
                break;
            }
            default:
            case X_THICK: {
                result *= f3;
                break;
            }
        }

        return result;
    }
    // endregion

    @NonNull
    List<String> getConfigItemLabelsSetToStyle(@Nullable Style style) {
        List<String> result = new ArrayList<>();
        if (getComplicationRingStyle() == style) {
            result.add(mContext.getString(R.string.config_complication_ring_style));
        }
        if (getComplicationBackgroundStyle() == style) {
            result.add(mContext.getString(R.string.config_complication_background_style));
        }
        if (Style.ACCENT_FILL == style) {
            result.add(mContext.getString(R.string.config_preset_bezel_style));
        }
        if (Style.BASE_ACCENT == style) {
            result.add(mContext.getString(R.string.config_preset_background_style));
        }
        if (getHourHandStyle() == style) {
            result.add(mContext.getString(R.string.config_preset_hour_hand_style));
        }
        if (getMinuteHandStyle() == style) {
            result.add(mContext.getString(R.string.config_preset_minute_hand_style));
        }
        if (getComplicationBackgroundStyle() == style) {
            result.add(mContext.getString(R.string.config_preset_second_hand_style));
        }
        if (getTickBackgroundStyle() == style) {
            result.add(mContext.getString(R.string.config_preset_tick_background_style));
        }
        if (getDigitStyle() == style) {
            result.add(mContext.getString(R.string.config_preset_digit_style));
        }
        if (getFourTickStyle() == style) {
            result.add(mContext.getString(R.string.config_preset_four_tick_style));
        }
        if (getTwelveTickStyle() == style) {
            result.add(mContext.getString(R.string.config_preset_twelve_tick_style));
        }
        if (getSixtyTickStyle() == style) {
            result.add(mContext.getString(R.string.config_preset_sixty_tick_style));
        }
        return result;
    }
}
