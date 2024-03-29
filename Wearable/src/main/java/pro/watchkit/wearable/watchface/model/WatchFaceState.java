/*
 * Copyright (C) 2019-2022 Terence Tan
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
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.wearable.complications.ComplicationData;

import androidx.annotation.ArrayRes;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;
import java.util.TreeMap;

import pro.watchkit.wearable.watchface.R;
import pro.watchkit.wearable.watchface.model.BytePackable.ComplicationCount;
import pro.watchkit.wearable.watchface.model.BytePackable.ComplicationRotation;
import pro.watchkit.wearable.watchface.model.BytePackable.ComplicationScale;
import pro.watchkit.wearable.watchface.model.BytePackable.ComplicationSize;
import pro.watchkit.wearable.watchface.model.BytePackable.DigitDisplay;
import pro.watchkit.wearable.watchface.model.BytePackable.DigitFormat;
import pro.watchkit.wearable.watchface.model.BytePackable.DigitRotation;
import pro.watchkit.wearable.watchface.model.BytePackable.DigitSize;
import pro.watchkit.wearable.watchface.model.BytePackable.HandCutoutCombination;
import pro.watchkit.wearable.watchface.model.BytePackable.HandCutoutMaterial;
import pro.watchkit.wearable.watchface.model.BytePackable.HandCutoutShape;
import pro.watchkit.wearable.watchface.model.BytePackable.HandLength;
import pro.watchkit.wearable.watchface.model.BytePackable.HandShape;
import pro.watchkit.wearable.watchface.model.BytePackable.HandStalk;
import pro.watchkit.wearable.watchface.model.BytePackable.HandThickness;
import pro.watchkit.wearable.watchface.model.BytePackable.Material;
import pro.watchkit.wearable.watchface.model.BytePackable.MaterialGradient;
import pro.watchkit.wearable.watchface.model.BytePackable.MaterialTexture;
import pro.watchkit.wearable.watchface.model.BytePackable.PipMargin;
import pro.watchkit.wearable.watchface.model.BytePackable.PipShape;
import pro.watchkit.wearable.watchface.model.BytePackable.PipSize;
import pro.watchkit.wearable.watchface.model.BytePackable.PipsDisplay;
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
    private boolean mBurnInProtection = false;
    private final GregorianCalendar mCalendar = new GregorianCalendar();
    private final LocationCalculator mLocationCalculator = new LocationCalculator(mCalendar);
    @NonNull
    private final Context mContext;
    private static final double GOLDEN_RATIO = (1d + Math.sqrt(5d)) / 2d;

    @Override
    public int hashCode() {
        return Objects.hash(
                mWatchFacePreset,
                mSettings,
                mPaintBox,
                mComplications,
                mUnreadNotifications,
                mTotalNotifications,
                mBurnInProtection);
    }

    @NonNull
    private final StringBuilder mStringBuilder = new StringBuilder();

    public WatchFaceState(@NonNull Context context) {
        mPaintBox = new PaintBox(context);
        mContext = context;
        mTypefaceCache = new android.graphics.Typeface[Typeface.finalValues.length];
        // Hmm. Strictly temporary: how about a default setting?
        // setString("fcd81c000c0100000006c06a60000001~3cda1cc0000000000000000000000001");
    }

    /**
     * Returns the string from the application's resources.
     * Convenience method for "mContext.getString(resId)".
     *
     * @param resId Resource id for the string
     * @return The string from the application's resources..
     */
    @NonNull
    String getStringResource(@StringRes int resId) {
        return mContext.getString(resId);
    }

    /**
     * Returns the string from the application's resources.
     * Convenience method for "mContext.getString(resId)".
     *
     * @param resId Resource id for the string
     * @return The string from the application's resources..
     */
    @NonNull
    String[] getStringArrayResource(@ArrayRes int resId) {
        return mContext.getResources().getStringArray(resId);
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
     * <p>
     * We make this one static reference time, rather than just using the system realtime
     * clock, as it takes time to run our code... we don't want to be calculating our
     * hour hand at 00:01, our minute hand at 00:02, our second hand at 00:03 etc. and
     * having different elements out of sync with each other.
     */
    public void setCurrentTimeToNow() {
        mCalendar.setTimeInMillis(System.currentTimeMillis());
        // mCalendar.setTimeInMillis(1570365309000L); // For preview generation.
    }

    /**
     * Set the calendar's current time to the given time in milliseconds
     *
     * @param millis The time in UTC milliseconds since epoch
     */
    public void setCurrentTime(long millis) {
        mCalendar.setTimeInMillis(millis);
    }

    public void setBurnInProtection(boolean lowBitAmbient, boolean burnInProtection) {
        mBurnInProtection = burnInProtection;

        // Set low-bit ambient on our ambient watch face paint as required.
        getPaintBox().getAmbientPaint().setAntiAlias(!lowBitAmbient);

        // Set low-bit ambient and burn-in protection on our complications as required.
        getComplications().forEach(
                c -> c.setLowBitAmbientBurnInProtection(lowBitAmbient, burnInProtection));
    }

    public boolean getBurnInProtection() {
        return mBurnInProtection;
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

    private int mPreviousBoundsSerial = -1;

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

    /**
     * Recalculates the location bounds for our circular complications (both foreground and
     * background). Call this if the size of our drawable changes with our bounds, and we'll put
     * the complications somewhere within these bounds.
     *
     * @param bounds Bounds of drawable
     */
    private void recalculateComplicationBounds(@NonNull Rect bounds) {
        int width = bounds.width(), height = bounds.height();
        int currentBoundsSerial = Objects.hash(width, height, mSettings);

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
        @ColorInt int activeColor = getComplicationTextColor();
        @ColorInt int activeColorAlt1 = getComplicationTextColorAlt1();
        @ColorInt int activeColorAlt2 = getComplicationTextColorAlt2();
        @ColorInt int ambientColor = Color.WHITE;
        @Nullable android.graphics.Typeface typeface = getTypefaceObject();

        mComplications.forEach(c -> c.setColors(
                activeColor, activeColorAlt1, activeColorAlt2, ambientColor, typeface));
    }

    @NonNull
    private final static Map<String, String> mGalleryEntries = new TreeMap<>((o1, o2) -> {
        // A comparator which sorts the characters 🅰, 🅱, 🅲, 🅳 etc. to the very top.
        int c1 = (int) (o1.codePointAt(0));
        int c2 = (int) (o2.codePointAt(0));
        if (c1 > 0x1f000 && c2 > 0x1f000) {
            return o1.compareTo(o2);
        } else if (c1 > 0x1F000) {
            return -1;
        } else if (c2 > 0x1F000) {
            return 1;
        } else {
            return o1.compareTo(o2);
        }
    });
    @NonNull
    private final static Map<String, String> mGalleryEntriesFlipped = new TreeMap<>();
    @NonNull
    private final static Map<String, String> mGalleryEntriesFlippedZeroed = new TreeMap<>();

    public Map<String, String> getGalleryEntries() {
        if (mGalleryEntries.size() == 0) {
            String currentString = getWatchFacePresetString(); // Preserve current preset string.
            // Initialise on first use
            final String[] galleryNames = getStringArrayResource(R.array.gallery_names);
            final String[] galleryPresets = getStringArrayResource(R.array.gallery_presets);

            // Loop through each default colorway name and value.
            // Add original named colorways first, before we add any variants.
            for (int i = 0; i < galleryNames.length; i++) {
                mGalleryEntries.putIfAbsent(galleryNames[i], galleryPresets[i]);
                mGalleryEntriesFlipped.putIfAbsent(galleryPresets[i], galleryNames[i]);
                setWatchFacePresetString(galleryPresets[i]);
                setColorway(0);
                mGalleryEntriesFlippedZeroed.putIfAbsent(getWatchFacePresetString(), galleryNames[i]);
            }

            setWatchFacePresetString(currentString); // Restore current preset string.
        }
        return mGalleryEntries;
    }

    private Map<String, String> getGalleryEntriesFlipped() {
        getGalleryEntries();
        return mGalleryEntriesFlipped;
    }

    private Map<String, String> getGalleryEntriesFlippedZeroed() {
        getGalleryEntries();
        return mGalleryEntriesFlippedZeroed;
    }

    public String getWatchFaceName() {
        // See if our watch face string is directly in the gallery.
        String currentString = getWatchFacePresetString();
        if (getGalleryEntriesFlipped().containsKey(currentString)) {
            return getGalleryEntriesFlipped().get(currentString);
        }

        // It might be a different colorway. Try zeroing out the colorway and checking...
        setColorway(0);
        String zeroedString = getWatchFacePresetString();
        setWatchFacePresetString(currentString); // Restore current preset string.
        if (getGalleryEntriesFlippedZeroed().containsKey(zeroedString)) {
            return getGalleryEntriesFlippedZeroed().get(zeroedString) + " × " +
                    getPaintBox().getColorwayName();
        }

        // It's truly custom.
        return "Current Watch Face (#" + getHash() + ")";
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

        // Update drawable complications' ambient state.
        // Note: ComplicationDrawable handles switching between active/ambient colors, we just
        // have to inform it to enter ambient mode.
        mComplications.forEach(c -> c.setAmbientMode(ambient));
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

    @NonNull
    public String getColorwayName() {
        return mPaintBox.getColorwayName();
    }

    // region Ephemeral
    @Nullable
    private Material mSwatchMaterial;

    /**
     * Determines whether the two given WatchFaceState strings are mostly equal.
     * That is, whether they're equal in all things such as WatchFacePreset and Settings,
     * but don't compare ephemeral settings that don't matter.
     *
     * @param a First string to compare
     * @param b Second string to compare
     * @return Whether the objects are mostly equal
     */
    public static boolean mostlyEquals(@Nullable String a, @Nullable String b) {
        if (a == null || b == null) {
            // Don't even try comparing nulls, even to each other.
            return false;
        }

        String[] aSplit = a.split("~");
        String[] bSplit = b.split("~");
        if (aSplit.length < 2 || bSplit.length < 2) {
            return false;
        }

        // Essentially this works by unpacking "a" and "b", repacking them
        // and comparing the output. By doing this, we sidestep issues such
        // as old versions of the packed string formats; everything is up-to-date.

        WatchFacePreset aPreset = new WatchFacePreset(), bPreset = new WatchFacePreset();
        Settings aSettings = new Settings(), bSettings = new Settings();

        aPreset.setString(aSplit[0]);
        aSettings.setString(aSplit[1]);
        bPreset.setString(bSplit[0]);
        bSettings.setString(bSplit[1]);

        return aPreset.getString().equals(bPreset.getString()) &&
                aSettings.getString().equals(bSettings.getString());
    }

    /**
     * Determines whether the given WatchFaceState is mostly equal to this object.
     * That is, whether they're equal in all things such as WatchFacePreset and Settings,
     * but don't compare ephemeral settings that don't matter.
     *
     * @param b The other string to compare
     * @return Whether the objects are mostly equal
     */
    public boolean mostlyEquals(@Nullable String b) {
        if (b == null) {
            // "b" is null; this object isn't null... so false!
            return false;
        }

        String[] bSplit = b.split("~");
        if (bSplit.length < 2) {
            return false;
        }

        // Essentially this works by unpacking "b", repacking "this" and "b"
        // and comparing the output. By doing this, we sidestep issues such
        // as old versions of the packed string formats; everything is up-to-date.

        WatchFacePreset bPreset = new WatchFacePreset();
        Settings bSettings = new Settings();

        bPreset.setString(bSplit[0]);
        bSettings.setString(bSplit[1]);

        return mWatchFacePreset.getString().equals(bPreset.getString()) &&
                mSettings.getString().equals(bSettings.getString());
    }

//    /**
//     * Determines whether the given WatchFaceState is mostly equal to this object.
//     * That is, whether they're equal in all things such as WatchFacePreset and Settings,
//     * but don't compare ephemeral settings that don't matter.
//     *
//     * @param b The other WatchFaceState to compare
//     * @return Whether the objects are mostly equal
//     */
//    public boolean mostlyEquals(@Nullable WatchFaceState b) {
//        if (b == null) {
//            // "b" is null; this object isn't null... so false!
//            return false;
//        }
//
//        // Essentially this works by repacking "this" and "b"
//        // and comparing the output. By doing this, we sidestep issues such
//        // as old versions of the packed string formats; everything is up-to-date.
//
//        return mWatchFacePreset.getString().equals(b.mWatchFacePreset.getString()) &&
//                mSettings.getString().equals(b.mSettings.getString());
//    }
    // endregion

    public FileOutputStream openFileOutput(String s) throws FileNotFoundException {
        return mContext.openFileOutput(s, Context.MODE_PRIVATE);
    }

    @NonNull
    public String getString() {
        mStringBuilder.setLength(0);
        mStringBuilder.append(mWatchFacePreset.getString())
                .append("~")
                .append(mSettings.getString());
        return mStringBuilder.toString();
    }

    @NonNull
    public String getHash() {
        return mWatchFacePreset.getHash();
    }

    // region Settings
    public Typeface getTypeface() {
        return mSettings.mTypeface;
    }

    public void setTypeface(Typeface typeface) {
        mSettings.mTypeface = typeface;
        regeneratePaints();
    }

    @NonNull
    final private android.graphics.Typeface[] mTypefaceCache;

    @Nullable
    public android.graphics.Typeface getTypefaceObject() {
        return getTypefaceObject(getTypeface());
    }

    @Nullable
    public android.graphics.Typeface getTypefaceObject(@Nullable Typeface typeface) {
        if (typeface == null) {
            return android.graphics.Typeface.DEFAULT;
        }
        if (Build.VERSION.SDK_INT >= 26) {
            // For API 26 and above, we can attempt to get most cool fonts.
            int i = typeface.ordinal();
            switch (typeface) {
                case SANS_THIN: {
                    if (mTypefaceCache[i] == null) {
                        Builder b = new Builder("/system/fonts/Roboto-Thin.ttf");
                        mTypefaceCache[i] = b.build();
                    }
                    return mTypefaceCache[i];
                }
                case SANS_LIGHT: {
                    if (mTypefaceCache[i] == null) {
                        Builder b = new Builder("/system/fonts/Roboto-Light.ttf");
                        mTypefaceCache[i] = b.build();
                    }
                    return mTypefaceCache[i];
                }
                case SANS_REGULAR: {
                    if (mTypefaceCache[i] == null) {
                        Builder b = new Builder("/system/fonts/DroidSans.ttf");
                        mTypefaceCache[i] = b.build();
                    }
                    if (mTypefaceCache[i] == null) {
                        mTypefaceCache[i] = android.graphics.Typeface.DEFAULT;
                    }
                    return mTypefaceCache[i];
                }
                case SANS_MEDIUM: {
                    if (mTypefaceCache[i] == null) {
                        Builder b = new Builder("/system/fonts/Roboto-Medium.ttf");
                        mTypefaceCache[i] = b.build();
                    }
                    return mTypefaceCache[i];
                }
                case SANS_BOLD: {
                    if (mTypefaceCache[i] == null) {
                        Builder b = new Builder("/system/fonts/DroidSans-Bold.ttf");
                        mTypefaceCache[i] = b.build();
                    }
                    if (mTypefaceCache[i] == null) {
                        mTypefaceCache[i] = android.graphics.Typeface.DEFAULT_BOLD;
                    }
                    return mTypefaceCache[i];
                }
                case SANS_BLACK: {
                    if (mTypefaceCache[i] == null) {
                        Builder b = new Builder("/system/fonts/Roboto-Black.ttf");
                        mTypefaceCache[i] = b.build();
                    }
                    return mTypefaceCache[i];
                }
                case SERIF_REGULAR: {
                    if (mTypefaceCache[i] == null) {
                        Builder b = new Builder("/system/fonts/NotoSerif-Regular.ttf");
                        mTypefaceCache[i] = b.build();
                    }
                    if (mTypefaceCache[i] == null) {
                        mTypefaceCache[i] = android.graphics.Typeface.SERIF;
                    }
                    return mTypefaceCache[i];
                }
                case SERIF_BOLD: {
                    if (mTypefaceCache[i] == null) {
                        Builder b = new Builder("/system/fonts/NotoSerif-Bold.ttf");
                        mTypefaceCache[i] = b.build();
                    }
                    return mTypefaceCache[i];
                }
                case MONO_REGULAR: {
                    if (mTypefaceCache[i] == null) {
                        Builder b = new Builder("/system/fonts/DroidSansMono.ttf");
                        mTypefaceCache[i] = b.build();
                    }
                    if (mTypefaceCache[i] == null) {
                        mTypefaceCache[i] = android.graphics.Typeface.MONOSPACE;
                    }
                    return mTypefaceCache[i];
                }
                case CONDENSED_LIGHT: {
                    if (mTypefaceCache[i] == null) {
                        Builder b = new Builder("/system/fonts/RobotoCondensed-Light.ttf");
                        mTypefaceCache[i] = b.build();
                    }
                    return mTypefaceCache[i];
                }
                case CONDENSED_REGULAR: {
                    if (mTypefaceCache[i] == null) {
                        Builder b = new Builder("/system/fonts/RobotoCondensed-Regular.ttf");
                        mTypefaceCache[i] = b.build();
                    }
                    return mTypefaceCache[i];
                }
                case CONDENSED_MEDIUM: {
                    if (mTypefaceCache[i] == null) {
                        Builder b = new Builder("/system/fonts/RobotoCondensed-Medium.ttf");
                        mTypefaceCache[i] = b.build();
                    }
                    return mTypefaceCache[i];
                }
                case CONDENSED_BOLD: {
                    if (mTypefaceCache[i] == null) {
                        Builder b = new Builder("/system/fonts/RobotoCondensed-Bold.ttf");
                        mTypefaceCache[i] = b.build();
                    }
                    return mTypefaceCache[i];
                }
                case PRODUCT_SANS_REGULAR: {
                    if (mTypefaceCache[i] == null) {
                        Builder b = new Builder("/system/fonts/GoogleSans-Regular.ttf");
                        mTypefaceCache[i] = b.build();
                    }
                    return mTypefaceCache[i];
                }
                case PRODUCT_SANS_MEDIUM: {
                    if (mTypefaceCache[i] == null) {
                        Builder b = new Builder("/system/fonts/GoogleSans-Medium.ttf");
                        mTypefaceCache[i] = b.build();
                    }
                    return mTypefaceCache[i];
                }
                case PRODUCT_SANS_BOLD: {
                    if (mTypefaceCache[i] == null) {
                        Builder b = new Builder("/system/fonts/GoogleSans-Bold.ttf");
                        mTypefaceCache[i] = b.build();
                    }
                    return mTypefaceCache[i];
                }
                default: {
                    return null;
                }
            }
        } else {
            // For API 25 and older, just get the regular uncool fonts.
            switch (getTypeface()) {
                case SANS_REGULAR: {
                    return android.graphics.Typeface.DEFAULT;
                }
                case SANS_BOLD: {
                    return android.graphics.Typeface.DEFAULT_BOLD;
                }
                case SERIF_REGULAR: {
                    return android.graphics.Typeface.SERIF;
                }
                case MONO_REGULAR: {
                    return android.graphics.Typeface.MONOSPACE;
                }
                default: {
                    return null;
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

    ComplicationScale getComplicationScale() {
        return mSettings.mComplicationScale;
    }

    void setComplicationScale(ComplicationScale complicationScale) {
        mSettings.mComplicationScale = complicationScale;
    }

    public boolean isShowUnreadNotifications() {
        return mSettings.mShowUnreadNotifications;
    }

    void setShowUnreadNotifications(boolean showUnreadNotifications) {
        mSettings.mShowUnreadNotifications = showUnreadNotifications;
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

    @ColorInt
    public int getComplicationTextColor() {
        Material complicationBackgroundMaterial = getComplicationBackgroundMaterial();
        switch (complicationBackgroundMaterial) {
            case FILL_HIGHLIGHT:
                return mPaintBox.getContrastingColor(complicationBackgroundMaterial,
                        getFillHighlightMaterialGradient());
            case ACCENT_FILL:
                return mPaintBox.getContrastingColor(complicationBackgroundMaterial,
                        getAccentFillMaterialGradient());
            case ACCENT_HIGHLIGHT:
                return mPaintBox.getContrastingColor(complicationBackgroundMaterial,
                        getAccentHighlightMaterialGradient());
            case BASE_ACCENT:
            default:
                return mPaintBox.getContrastingColor(complicationBackgroundMaterial,
                        getBaseAccentMaterialGradient());
        }
    }

    @ColorInt
    public int getComplicationTextColorAlt1() {
        Material complicationBackgroundMaterial = getComplicationBackgroundMaterial();
        switch (complicationBackgroundMaterial) {
            case FILL_HIGHLIGHT:
                return mPaintBox.getColor(getSixBitColor(ColorType.FILL));
            case ACCENT_FILL:
            case ACCENT_HIGHLIGHT:
                return mPaintBox.getColor(getSixBitColor(ColorType.ACCENT));
            case BASE_ACCENT:
            default:
                return mPaintBox.getColor(getSixBitColor(ColorType.BASE));
        }
    }

    @ColorInt
    public int getComplicationTextColorAlt2() {
        Material complicationBackgroundMaterial = getComplicationBackgroundMaterial();
        switch (complicationBackgroundMaterial) {
            case FILL_HIGHLIGHT:
            case ACCENT_HIGHLIGHT:
                return mPaintBox.getColor(getSixBitColor(ColorType.HIGHLIGHT));
            case ACCENT_FILL:
                return mPaintBox.getColor(getSixBitColor(ColorType.FILL));
            case BASE_ACCENT:
            default:
                return mPaintBox.getColor(getSixBitColor(ColorType.ACCENT));
        }
    }

    public Material getComplicationRingMaterial() {
        return mSettings.mComplicationRingMaterial;
    }

    void setComplicationRingMaterial(Material complicationRingMaterial) {
        mSettings.mComplicationRingMaterial = complicationRingMaterial;
    }

    public Material getComplicationBackgroundMaterial() {
        return mSettings.mComplicationBackgroundMaterial;
    }

    void setComplicationBackgroundMaterial(Material complicationBackgroundMaterial) {
        mSettings.mComplicationBackgroundMaterial = complicationBackgroundMaterial;
    }

    public boolean getUseDecomposition() {
        return mSettings.mUseDecomposition;
    }

    public void setUseDecomposition(boolean useDecomposition) {
        mSettings.mUseDecomposition = useDecomposition;
    }

    public boolean isDeveloperMode() {
        return mSettings.mDeveloperMode;
    }

//    void setHardwareAccelerationEnabled(boolean hardwareAccelerationEnabled) {
//        mSettings.mHardwareAccelerationEnabled = hardwareAccelerationEnabled;
//    }

    public boolean isHardwareAccelerationEnabled() {
        return mSettings.mHardwareAccelerationEnabled;
    }

//    void setInnerGlow(boolean innerGlow) {
//        mSettings.mInnerGlow = innerGlow;
//    }

    public boolean isInnerGlow() {
        return mSettings.mInnerGlow;
    }

//    void setDrawShadows(boolean drawShadows) {
//        mSettings.mDrawShadows = drawShadows;
//    }

    public boolean isDrawShadows() {
        return mSettings.mDrawShadows;
    }

//    public void setTransparentBackground(boolean transparentBackground) {
//        mSettings.mTransparentBackground = transparentBackground;
//    }

    public boolean isTransparentBackground() {
        return mSettings.mTransparentBackground;
    }

    public void setDeveloperMode(boolean developerMode) {
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

    public boolean isHidePips() {
        return mSettings.mHidePips;
    }

    void setHidePips(boolean hidePips) {
        mSettings.mHidePips = hidePips;
    }

    public boolean isHideHands() {
        return mSettings.mHideHands;
    }

    void setHideHands(boolean hideHands) {
        mSettings.mHideHands = hideHands;
    }

    private boolean isUseLegacyEffects() {
        return mSettings.mUseLegacyEffects;
    }

    void setUseLegacyEffects(boolean useLegacyEffects) {
        mSettings.mUseLegacyEffects = useLegacyEffects;
    }
    // endregion

    // region WatchFacePreset
    void setMinuteHandOverride(boolean minuteHandOverride) {
        mWatchFacePreset.mMinuteHandOverride = minuteHandOverride;
    }

    boolean isMinuteHandOverridden() {
        return mWatchFacePreset.mMinuteHandOverride;
    }

    boolean isMinuteHandOverriddenAndCutout() {
        return mWatchFacePreset.mMinuteHandOverride && isMinuteHandCutout();
    }

    void setSecondHandOverride(boolean secondHandOverride) {
        mWatchFacePreset.mSecondHandOverride = secondHandOverride;
    }

    boolean isSecondHandOverridden() {
        return mWatchFacePreset.mSecondHandOverride;
    }

    void setHourPipOverride(boolean hourPipOverride) {
        mWatchFacePreset.mHourPipOverride = hourPipOverride;
    }

    boolean isHourPipsOverridden() {
        return isHourPipsVisible() && mWatchFacePreset.mHourPipOverride;
    }

    void setMinutePipOverride(boolean minutePipOverride) {
        mWatchFacePreset.mMinutePipOverride = minutePipOverride;
    }

    boolean isMinutePipsOverridden() {
        return isMinutePipsVisible() && mWatchFacePreset.mMinutePipOverride;
    }

    public String getWatchFacePresetString() {
        return mWatchFacePreset.getString();
    }

    public void setWatchFacePresetString(@NonNull String s) {
        mWatchFacePreset.setString(s);
    }

    public void setString(@Nullable String s) {
        if (s == null || s.length() == 0) return;
        String[] split = s.split("~");
        if (split.length >= 2) {
            mWatchFacePreset.setString(split[0]);
            mSettings.setString(split[1]);
        }
        regeneratePaints();
    }

    @SuppressWarnings("SameReturnValue")
    @NonNull
    public Material getBackgroundMaterial() {
        return Material.BASE_ACCENT; // Hard-coded!
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

    public Material getHourHandMaterial() {
        return mWatchFacePreset.mHourHandMaterial;
    }

    void setHourHandMaterial(Material hourHandMaterial) {
        mWatchFacePreset.mHourHandMaterial = hourHandMaterial;
    }

    @NonNull
    private Material getMaterial(
            @NonNull Material handMaterial, @NonNull HandCutoutCombination handCutoutCombination) {
        switch (handCutoutCombination) {
            default:
            case NONE: {
                // No effect.
                return handMaterial;
            }
            case TIP_PLUS_ONE:
            case TIP_STALK_PLUS_ONE:
            case HAND_PLUS_ONE:
            case STALK_PLUS_ONE:
            case HAND_STALK_PLUS_ONE: {
                if (handMaterial.equals(Material.FILL_HIGHLIGHT)) {
                    return Material.ACCENT_FILL;
                } else {
                    return Material.FILL_HIGHLIGHT;
                }
            }
            case TIP_PLUS_TWO:
            case TIP_STALK_PLUS_TWO:
            case HAND_PLUS_TWO:
            case STALK_PLUS_TWO:
            case HAND_STALK_PLUS_TWO: {
                if (handMaterial.equals(Material.FILL_HIGHLIGHT) ||
                        handMaterial.equals(Material.ACCENT_FILL)) {
                    return Material.ACCENT_HIGHLIGHT;
                } else {
                    return Material.ACCENT_FILL;
                }
            }
            case TIP_PLUS_THREE:
            case TIP_STALK_PLUS_THREE:
            case HAND_PLUS_THREE:
            case STALK_PLUS_THREE:
            case HAND_STALK_PLUS_THREE: {
                if (handMaterial.equals(Material.BASE_ACCENT)) {
                    return Material.ACCENT_HIGHLIGHT;
                } else {
                    return Material.BASE_ACCENT;
                }
            }
        }
    }

    @NonNull
    private HandCutoutCombination setHandCutoutCombination(
            @NonNull HandCutoutShape handCutoutShape,
            @NonNull HandCutoutMaterial handCutoutMaterial) {
        switch (handCutoutMaterial) {
            default:
            case PLUS_ONE: {
                switch (handCutoutShape) {
                    default:
                    case TIP: {
                        return HandCutoutCombination.TIP_PLUS_ONE;
                    }
                    case TIP_STALK: {
                        return HandCutoutCombination.TIP_STALK_PLUS_ONE;
                    }
                    case HAND: {
                        return HandCutoutCombination.HAND_PLUS_ONE;
                    }
                    case STALK: {
                        return HandCutoutCombination.STALK_PLUS_ONE;
                    }
                    case HAND_STALK: {
                        return HandCutoutCombination.HAND_STALK_PLUS_ONE;
                    }
                }
            }
            case PLUS_TWO: {
                switch (handCutoutShape) {
                    default:
                    case TIP: {
                        return HandCutoutCombination.TIP_PLUS_TWO;
                    }
                    case TIP_STALK: {
                        return HandCutoutCombination.TIP_STALK_PLUS_TWO;
                    }
                    case HAND: {
                        return HandCutoutCombination.HAND_PLUS_TWO;
                    }
                    case STALK: {
                        return HandCutoutCombination.STALK_PLUS_TWO;
                    }
                    case HAND_STALK: {
                        return HandCutoutCombination.HAND_STALK_PLUS_TWO;
                    }
                }
            }
            case PLUS_THREE: {
                switch (handCutoutShape) {
                    default:
                    case TIP: {
                        return HandCutoutCombination.TIP_PLUS_THREE;
                    }
                    case TIP_STALK: {
                        return HandCutoutCombination.TIP_STALK_PLUS_THREE;
                    }
                    case HAND: {
                        return HandCutoutCombination.HAND_PLUS_THREE;
                    }
                    case STALK: {
                        return HandCutoutCombination.STALK_PLUS_THREE;
                    }
                    case HAND_STALK: {
                        return HandCutoutCombination.HAND_STALK_PLUS_THREE;
                    }
                }
            }
        }
    }

    @NonNull
    private HandCutoutMaterial getHandCutoutMaterial(
            @NonNull HandCutoutCombination handCutoutCombination) {
        switch (handCutoutCombination) {
            default:
            case TIP_PLUS_ONE:
            case TIP_STALK_PLUS_ONE:
            case HAND_PLUS_ONE:
            case STALK_PLUS_ONE:
            case HAND_STALK_PLUS_ONE:
            case NONE: {
                return HandCutoutMaterial.PLUS_ONE;
            }
            case TIP_PLUS_TWO:
            case TIP_STALK_PLUS_TWO:
            case HAND_PLUS_TWO:
            case STALK_PLUS_TWO:
            case HAND_STALK_PLUS_TWO: {
                return HandCutoutMaterial.PLUS_TWO;
            }
            case TIP_PLUS_THREE:
            case TIP_STALK_PLUS_THREE:
            case HAND_PLUS_THREE:
            case STALK_PLUS_THREE:
            case HAND_STALK_PLUS_THREE: {
                return HandCutoutMaterial.PLUS_THREE;
            }
        }
    }

    @NonNull
    private HandCutoutShape getHandCutoutShape(
            @NonNull HandCutoutCombination handCutoutCombination) {
        switch (handCutoutCombination) {
            default:
            case TIP_PLUS_ONE:
            case TIP_PLUS_TWO:
            case TIP_PLUS_THREE:
            case NONE: {
                return HandCutoutShape.TIP;
            }
            case TIP_STALK_PLUS_ONE:
            case TIP_STALK_PLUS_TWO:
            case TIP_STALK_PLUS_THREE: {
                return HandCutoutShape.TIP_STALK;
            }
            case HAND_PLUS_ONE:
            case HAND_PLUS_TWO:
            case HAND_PLUS_THREE: {
                return HandCutoutShape.HAND;
            }
            case STALK_PLUS_ONE:
            case STALK_PLUS_TWO:
            case STALK_PLUS_THREE: {
                return HandCutoutShape.STALK;
            }
            case HAND_STALK_PLUS_ONE:
            case HAND_STALK_PLUS_TWO:
            case HAND_STALK_PLUS_THREE: {
                return HandCutoutShape.HAND_STALK;
            }
        }
    }

    void setHourHandCutout(boolean hourHandCutout) {
        if (hourHandCutout) {
            // OK, we want a cutout.
            // Restore it from previous value in settings.
            mWatchFacePreset.mHourHandCutoutCombination =
                    mSettings.mPreviousHourHandCutoutCombination;
            if (mWatchFacePreset.mHourHandCutoutCombination.equals(
                    HandCutoutCombination.NONE)) {
                // Sanity check so we didn't restore HandCutoutCombination.NONE.
                mWatchFacePreset.mHourHandCutoutCombination =
                        HandCutoutCombination.TIP_PLUS_ONE;
            }
            // Clean out previous value in settings.
            mSettings.mPreviousHourHandCutoutCombination = HandCutoutCombination.NONE;
        } else {
            // OK, we want no cutout.
            // Save the previous value (if any).
            mSettings.mPreviousHourHandCutoutCombination =
                    mWatchFacePreset.mHourHandCutoutCombination;
            if (mSettings.mPreviousHourHandCutoutCombination.equals(
                    HandCutoutCombination.NONE)) {
                // Sanity check so we didn't preserve HandCutoutCombination.NONE.
                mSettings.mPreviousHourHandCutoutCombination =
                        HandCutoutCombination.TIP_PLUS_ONE;
            }
            // Now clear the value so it draws none.
            mWatchFacePreset.mHourHandCutoutCombination = HandCutoutCombination.NONE;
        }
    }

    boolean isHourHandCutout() {
        return !mWatchFacePreset.mHourHandCutoutCombination.equals(HandCutoutCombination.NONE);
    }

    @NonNull
    public Material getHourHandCutoutMaterialAsMaterial() {
        return getMaterial(mWatchFacePreset.mHourHandMaterial, mWatchFacePreset.mHourHandCutoutCombination);
    }

//    @NonNull
//    HandCutoutMaterial getHourHandCutoutMaterial() {
//        return getHandCutoutMaterial(mWatchFacePreset.mHourHandCutoutCombination);
//    }

    void setHourHandCutoutMaterial(@NonNull HandCutoutMaterial hourHandCutoutMaterial) {
        mWatchFacePreset.mHourHandCutoutCombination = setHandCutoutCombination(
                getHandCutoutShape(mWatchFacePreset.mHourHandCutoutCombination),
                hourHandCutoutMaterial);
    }

    public Material getMinuteHandMaterial() {
        return mWatchFacePreset.mMinuteHandOverride ?
                mWatchFacePreset.mMinuteHandMaterial : mWatchFacePreset.mHourHandMaterial;
    }

    void setMinuteHandMaterial(Material minuteHandMaterial) {
        mWatchFacePreset.mMinuteHandMaterial = minuteHandMaterial;
    }

    void setMinuteHandCutout(boolean minuteHandCutout) {
        if (minuteHandCutout) {
            // OK, we want a cutout.
            // Restore it from previous value in settings.
            mWatchFacePreset.mMinuteHandCutoutCombination =
                    mSettings.mPreviousMinuteHandCutoutCombination;
            if (mWatchFacePreset.mMinuteHandCutoutCombination.equals(
                    HandCutoutCombination.NONE)) {
                // Sanity check so we didn't restore HandCutoutCombination.NONE.
                mWatchFacePreset.mMinuteHandCutoutCombination =
                        HandCutoutCombination.TIP_PLUS_ONE;
            }
            // Clean out previous value in settings.
            mSettings.mPreviousMinuteHandCutoutCombination = HandCutoutCombination.NONE;
        } else {
            // OK, we want no cutout.
            // Save the previous value (if any).
            mSettings.mPreviousMinuteHandCutoutCombination =
                    mWatchFacePreset.mMinuteHandCutoutCombination;
            if (mSettings.mPreviousMinuteHandCutoutCombination.equals(
                    HandCutoutCombination.NONE)) {
                // Sanity check so we didn't preserve HandCutoutCombination.NONE.
                mSettings.mPreviousMinuteHandCutoutCombination =
                        HandCutoutCombination.TIP_PLUS_ONE;
            }
            // Now clear the value so it draws none.
            mWatchFacePreset.mMinuteHandCutoutCombination = HandCutoutCombination.NONE;
        }
    }

    boolean isMinuteHandCutout() {
        return !mWatchFacePreset.mMinuteHandCutoutCombination.equals(HandCutoutCombination.NONE);
    }

    @NonNull
    public Material getMinuteHandCutoutMaterialAsMaterial() {
        return mWatchFacePreset.mMinuteHandOverride ?
                getMaterial(mWatchFacePreset.mMinuteHandMaterial,
                        mWatchFacePreset.mMinuteHandCutoutCombination) :
                getHourHandCutoutMaterialAsMaterial();
    }

//    @NonNull
//    HandCutoutMaterial getMinuteHandCutoutMaterial() {
//        return mWatchFacePreset.mMinuteHandOverride ?
//                getHandCutoutMaterial(mWatchFacePreset.mMinuteHandCutoutCombination) :
//                getHourHandCutoutMaterial();
//    }

    void setMinuteHandCutoutMaterial(@NonNull HandCutoutMaterial minuteHandCutoutMaterial) {
        mWatchFacePreset.mMinuteHandCutoutCombination = setHandCutoutCombination(
                getHandCutoutShape(mWatchFacePreset.mMinuteHandCutoutCombination),
                minuteHandCutoutMaterial);
    }

//    void setBackgroundMaterial(Material backgroundMaterial) {
//        mWatchFacePreset.mBackgroundMaterial = backgroundMaterial;
//    }

    void setSecondHandMaterial(Material secondHandMaterial) {
        mWatchFacePreset.mSecondHandMaterial = secondHandMaterial;
    }

//    PipsDisplay getPipsDisplay() {
//        return mWatchFacePreset.mPipsDisplay;
//    }

    void setPipsDisplay(PipsDisplay pipsDisplay) {
        mWatchFacePreset.mPipsDisplay = pipsDisplay;
    }

    public boolean isQuarterPipsVisible() {
        return mWatchFacePreset.mPipsDisplay != PipsDisplay.NONE;
    }

    public boolean isHourPipsVisible() {
        return mWatchFacePreset.mPipsDisplay == PipsDisplay.FOUR_TWELVE ||
                mWatchFacePreset.mPipsDisplay == PipsDisplay.FOUR_TWELVE_60;
    }

    public boolean isMinutePipsVisible() {
        return mWatchFacePreset.mPipsDisplay == PipsDisplay.FOUR_TWELVE_60;
    }

    public PipShape getQuarterPipShape() {
        return mWatchFacePreset.mQuarterPipShape;
    }

    void setQuarterPipShape(PipShape quarterPipShape) {
        mWatchFacePreset.mQuarterPipShape = quarterPipShape;
    }

    public PipShape getHourPipShape() {
        return mWatchFacePreset.mHourPipOverride ?
                mWatchFacePreset.mHourPipShape : mWatchFacePreset.mQuarterPipShape;
    }

    void setHourPipShape(PipShape hourPipShape) {
        mWatchFacePreset.mHourPipShape = hourPipShape;
    }

    public PipShape getMinutePipShape() {
        return mWatchFacePreset.mMinutePipOverride ?
                mWatchFacePreset.mMinutePipShape : mWatchFacePreset.mQuarterPipShape;
    }

    void setMinutePipShape(PipShape minutePipShape) {
        mWatchFacePreset.mMinutePipShape = minutePipShape;
    }

    public PipSize getQuarterPipSize() {
        return mWatchFacePreset.mQuarterPipSize;
    }

    void setQuarterPipSize(PipSize quarterPipSize) {
        mWatchFacePreset.mQuarterPipSize = quarterPipSize;
    }

    public PipSize getHourPipSize() {
        return mWatchFacePreset.mHourPipOverride ?
                mWatchFacePreset.mHourPipSize : mWatchFacePreset.mQuarterPipSize;
    }

    void setHourPipSize(PipSize hourPipSize) {
        mWatchFacePreset.mHourPipSize = hourPipSize;
    }

    public PipSize getMinutePipSize() {
        return mWatchFacePreset.mMinutePipOverride ?
                mWatchFacePreset.mMinutePipSize : mWatchFacePreset.mQuarterPipSize;
    }

    void setMinutePipSize(PipSize minutePipSize) {
        mWatchFacePreset.mMinutePipSize = minutePipSize;
    }

    PipMargin getPipMargin() {
        return mWatchFacePreset.mPipMargin;
    }

    void setPipMargin(PipMargin pipMargin) {
        mWatchFacePreset.mPipMargin = pipMargin;
    }

    public Material getQuarterPipMaterial() {
        return mWatchFacePreset.mQuarterPipMaterial;
    }

    void setQuarterPipMaterial(Material quarterPipMaterial) {
        mWatchFacePreset.mQuarterPipMaterial = quarterPipMaterial;
    }

    public Material getHourPipMaterial() {
        return mWatchFacePreset.mHourPipOverride ?
                mWatchFacePreset.mHourPipMaterial : mWatchFacePreset.mQuarterPipMaterial;
    }

    void setHourPipMaterial(Material hourPipMaterial) {
        mWatchFacePreset.mHourPipMaterial = hourPipMaterial;
    }

    public Material getMinutePipMaterial() {
        return mWatchFacePreset.mMinutePipOverride ?
                mWatchFacePreset.mMinutePipMaterial : mWatchFacePreset.mQuarterPipMaterial;
    }

    void setMinutePipMaterial(Material minutePipMaterial) {
        mWatchFacePreset.mMinutePipMaterial = minutePipMaterial;
    }

    public Material getPipBackgroundMaterial() {
        return mWatchFacePreset.mPipBackgroundMaterial;
    }

    void setPipBackgroundMaterial(Material pipBackgroundMaterial) {
        mWatchFacePreset.mPipBackgroundMaterial = pipBackgroundMaterial;
    }

    boolean isDigitVisible() {
        return getDigitDisplay() != DigitDisplay.NONE;
    }

    public Material getDigitMaterial() {
        return mWatchFacePreset.mDigitMaterial;
    }

    void setDigitMaterial(Material digitMaterial) {
        mWatchFacePreset.mDigitMaterial = digitMaterial;
    }

    public DigitDisplay getDigitDisplay() {
        return mWatchFacePreset.mDigitDisplay;
    }

    void setDigitDisplay(DigitDisplay digitDisplay) {
        mWatchFacePreset.mDigitDisplay = digitDisplay;
    }

    DigitSize getDigitSize() {
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
            case ROMAN: {
                labelRes = R.array.WatchFacePreset_DigitFormat_ROMAN;
                break;
            }
            case CIRCLED: {
                labelRes = R.array.WatchFacePreset_DigitFormat_CIRCLED;
                break;
            }
            case NEGATIVE_CIRCLED: {
                labelRes = R.array.WatchFacePreset_DigitFormat_NEGATIVE_CIRCLED;
                break;
            }
            case DOUBLE_STRUCK: {
                labelRes = R.array.WatchFacePreset_DigitFormat_DOUBLE_STRUCK;
                break;
            }
            default:
            case LOCALISED: {
                labelRes = R.array.WatchFacePreset_DigitFormat_LOCALISED;
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

    MaterialGradient getFillHighlightMaterialGradient() {
        return mWatchFacePreset.mFillHighlightMaterialGradient;
    }

    void setFillHighlightMaterialGradient(MaterialGradient fillHighlightMaterialGradient) {
        mWatchFacePreset.mFillHighlightMaterialGradient = fillHighlightMaterialGradient;
        regeneratePaints();
    }

    MaterialGradient getAccentFillMaterialGradient() {
        return mWatchFacePreset.mAccentFillMaterialGradient;
    }

    void setAccentFillMaterialGradient(MaterialGradient accentFillMaterialGradient) {
        mWatchFacePreset.mAccentFillMaterialGradient = accentFillMaterialGradient;
        regeneratePaints();
    }

    MaterialGradient getAccentHighlightMaterialGradient() {
        return mWatchFacePreset.mAccentHighlightMaterialGradient;
    }

    void setAccentHighlightMaterialGradient(MaterialGradient accentHighlightMaterialGradient) {
        mWatchFacePreset.mAccentHighlightMaterialGradient = accentHighlightMaterialGradient;
        regeneratePaints();
    }

    public MaterialGradient getBaseAccentMaterialGradient() {
        return mWatchFacePreset.mBaseAccentMaterialGradient;
    }

    void setBaseAccentMaterialGradient(MaterialGradient baseAccentMaterialGradient) {
        mWatchFacePreset.mBaseAccentMaterialGradient = baseAccentMaterialGradient;
        regeneratePaints();
    }

    MaterialTexture getFillHighlightMaterialTexture() {
        return mWatchFacePreset.mFillHighlightMaterialTexture;
    }

    void setFillHighlightMaterialTexture(MaterialTexture fillHighlightMaterialTexture) {
        mWatchFacePreset.mFillHighlightMaterialTexture = fillHighlightMaterialTexture;
        regeneratePaints();
    }

    MaterialTexture getAccentFillMaterialTexture() {
        return mWatchFacePreset.mAccentFillMaterialTexture;
    }

    void setAccentFillMaterialTexture(MaterialTexture accentFillMaterialTexture) {
        mWatchFacePreset.mAccentFillMaterialTexture = accentFillMaterialTexture;
        regeneratePaints();
    }

    MaterialTexture getAccentHighlightMaterialTexture() {
        return mWatchFacePreset.mAccentHighlightMaterialTexture;
    }

    void setAccentHighlightMaterialTexture(MaterialTexture accentHighlightMaterialTexture) {
        mWatchFacePreset.mAccentHighlightMaterialTexture = accentHighlightMaterialTexture;
        regeneratePaints();
    }

    MaterialTexture getBaseAccentMaterialTexture() {
        return mWatchFacePreset.mBaseAccentMaterialTexture;
    }

    void setBaseAccentMaterialTexture(MaterialTexture baseAccentMaterialTexture) {
        mWatchFacePreset.mBaseAccentMaterialTexture = baseAccentMaterialTexture;
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

    @NonNull
    public HandCutoutShape getHourHandCutoutShape() {
        return getHandCutoutShape(mWatchFacePreset.mHourHandCutoutCombination);
    }

    void setHourHandCutoutShape(@NonNull HandCutoutShape hourHandCutoutShape) {
        mWatchFacePreset.mHourHandCutoutCombination = setHandCutoutCombination(
                hourHandCutoutShape,
                getHandCutoutMaterial(mWatchFacePreset.mHourHandCutoutCombination));
    }

    @NonNull
    public HandCutoutShape getMinuteHandCutoutShape() {
        return mWatchFacePreset.mMinuteHandOverride ?
                getHandCutoutShape(mWatchFacePreset.mMinuteHandCutoutCombination) :
                getHourHandCutoutShape();
    }

    void setMinuteHandCutoutShape(@NonNull HandCutoutShape minuteHandCutoutShape) {
        mWatchFacePreset.mMinuteHandCutoutCombination = setHandCutoutCombination(
                minuteHandCutoutShape,
                getHandCutoutMaterial(mWatchFacePreset.mMinuteHandCutoutCombination));
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
        mPaintBox.setUseLegacyEffects(isDeveloperMode() && isUseLegacyEffects());
        mPaintBox.regeneratePaints(
                getFillSixBitColor(), getAccentSixBitColor(),
                getHighlightSixBitColor(), getBaseSixBitColor(),
                getAmbientDaySixBitColor(), getAmbientNightSixBitColor(),
                getFillHighlightMaterialGradient(), getAccentFillMaterialGradient(),
                getAccentHighlightMaterialGradient(), getBaseAccentMaterialGradient(),
                getFillHighlightMaterialTexture(), getAccentFillMaterialTexture(),
                getAccentHighlightMaterialTexture(), getBaseAccentMaterialTexture(),
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
        return mPaintBox.getColor(getSixBitColor(colorType));
    }

    /**
     * Get the given color name from our 6-bit (64-color) palette. Returns a String.
     *
     * @param colorType ColorType to get from our current WatchFacePreset.
     * @return Name of color from our palette as a String
     */
    public String getColorName(@NonNull ColorType colorType) {
        return mPaintBox.getColorName(getSixBitColor(colorType));
    }

    /**
     * Get the given color from our 6-bit (64-color) palette. Returns a 6-bit color.
     *
     * @param colorType ColorType to get from our current WatchFacePreset.
     * @return Color from our palette as a 6-bit color
     */
    public int getSixBitColor(@NonNull ColorType colorType) {
        switch (colorType) {
            case FILL: {
                return getFillSixBitColor();
            }
            case ACCENT: {
                return getAccentSixBitColor();
            }
            case HIGHLIGHT: {
                return getHighlightSixBitColor();
            }
            case BASE: {
                return getBaseSixBitColor();
            }
            case AMBIENT_DAY: {
                return getAmbientDaySixBitColor();
            }
            default:
            case AMBIENT_NIGHT: {
                return getAmbientNightSixBitColor();
            }
        }
    }

    /**
     * Set the given color from our 6-bit (64-color) palette.
     *
     * @param colorType   ColorType to set: fill, accent, highlight etc.
     * @param sixBitColor Color to set
     */
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

    /**
     * Set the current colorway of fill, accent, highlight, and base six-bit colors. Requires a
     * packed colorway 24-bit int.
     *
     * @param colorway Colorway of colors, packed into a 24-bit int.
     */
    public void setColorway(int colorway) {
        // Separate "colorway" into our 4 six-bit colours.
        setFillSixBitColor((colorway >> 18) & 63);
        setAccentSixBitColor((colorway >> 12) & 63);
        setHighlightSixBitColor((colorway >> 6) & 63);
        setBaseSixBitColor(colorway & 63);
    }
    // endregion

    @NonNull
    public Material getSecondHandMaterial() {
        // If not overridden, the default is just a plain and regular second hand.
        return mWatchFacePreset.mSecondHandOverride ?
                mWatchFacePreset.mSecondHandMaterial : Material.ACCENT_HIGHLIGHT;
    }

    @Nullable
    public Paint getSwatchPaint() {
        return mSwatchMaterial != null ? getPaintBox().getPaintFromPreset(mSwatchMaterial) : null;
    }

    public void setSwatchMaterial(@Nullable Material swatchMaterial) {
        mSwatchMaterial = swatchMaterial;
    }
    // endregion

    // region Dimensions

    private float getBandStart() {
        switch (getPipMargin()) {
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

    public float getPipBandStart(float pc) {
        if (getDigitDisplay() == DigitDisplay.ABOVE) {
            return getBandStart() + getDigitBandHeight(pc);
        } else {
            return getBandStart();
        }
    }

    public float getDigitBandStart(float pc) {
        if (getDigitDisplay() == DigitDisplay.BELOW) {
            return getBandStart() + getPipBandHeight(pc);
        } else {
            return getBandStart();
        }
    }

    /**
     * Get the pip band height, which is the longest of the quarter, hour and minute pips (and the
     * digits too if we're drawing them here).
     *
     * @param pc Current length of one percent
     * @return Height of pip band
     */
    public float getPipBandHeight(float pc) {
        float quarterPipHeight = getPipHalfLength(getQuarterPipShape(), getQuarterPipSize());
        float hourPipHeight = getPipHalfLength(getHourPipShape(), getHourPipSize());
        float minutePipHeight = getPipHalfLength(getMinutePipShape(), getMinutePipSize());

        float result = 2f + // Add 2f padding, which is 1f on top and bottom
                2f * Math.max(quarterPipHeight, Math.max(hourPipHeight, minutePipHeight));

        if (getDigitDisplay() == DigitDisplay.OVER) {
            return Math.max(result, getDigitBandHeight(pc));
        } else {
            return result;
        }
    }

    private final Rect mLabelRect = new Rect();

    public float getDigitBandHeight(float pc) {
        String[] labels = getDigitFormatLabels();
        Material material = getDigitMaterial();
        Paint paint = getPaintBox().getPaintFromPreset(material);
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
                        mLabelRect.height() * mLabelRect.height() +
                                mLabelRect.width() * mLabelRect.width()));
            }
        }

        return (result / pc) + 2f; // Convert back from pixels to percentage; add 2% padding
    }

    public static float getPipHalfLength(@NonNull PipShape pipShape, @NonNull PipSize pipSize) {
        final float barLengthScale = 1.5f;
        // Height of an equilateral triangle.
        final float triangleFactor = (float) (Math.sqrt(3d) / 2d);

        // Scaling factors for dot, triangle and diamond.
        // Relative to a square of side 1. So all greater than 1.
        final float dotScale = 2f / (float) Math.sqrt(Math.PI);
        final float triangleScale = 2f / (float) Math.sqrt(Math.sqrt(3d));
        final float diamondScale = (float) Math.sqrt(2d);

        float result;
        switch (pipShape) {
            case SQUARE_WIDE: {
                result = (float) Math.pow(GOLDEN_RATIO, -2d / 3d);
                break;
            }
            default:
            case SQUARE:
            case SQUARE_CUTOUT: {
                result = (float) Math.pow(GOLDEN_RATIO, 0.0d);
                break;
            }
            case BAR_1_2: {
                result = (float) Math.pow(GOLDEN_RATIO, 2d / 3d);
                break;
            }
            case BAR_1_4: {
                result = (float) Math.pow(GOLDEN_RATIO, 4d / 3d);
                break;
            }
            case BAR_1_8: {
                result = (float) Math.pow(GOLDEN_RATIO, 6d / 3d);
                break;
            }
            case SECTOR: {
                result = barLengthScale;
                break;
            }
            case DOT:
            case DOT_THIN:
            case DOT_CUTOUT: {
                result = dotScale;
                break;
            }
            case TRIANGLE:
            case TRIANGLE_THIN:
            case TRIANGLE_CUTOUT: {
                result = triangleFactor * triangleScale;
                break;
            }
            case DIAMOND:
            case DIAMOND_THIN:
            case DIAMOND_CUTOUT: {
                result = diamondScale;
                break;
            }
        }

        // For thin types, make their length more (and their thickness less)!
        switch (pipShape) {
            case DOT_THIN:
            case TRIANGLE_THIN:
            case DIAMOND_THIN: {
                result *= (float) Math.pow(GOLDEN_RATIO, 0.5d);
                break;
            }
        }

        switch (pipSize) {
            case XX_SHORT: {
                result *= (float) Math.pow(GOLDEN_RATIO, -1.5d);
                break;
            }
            case X_SHORT: {
                result *= (float) Math.pow(GOLDEN_RATIO, -1.0d);
                break;
            }
            default:
            case SHORT: {
                result *= (float) Math.pow(GOLDEN_RATIO, -0.5d);
                break;
            }
            case MEDIUM: {
                result *= (float) Math.pow(GOLDEN_RATIO, 0.0d);
                break;
            }
            case LONG: {
                result *= (float) Math.pow(GOLDEN_RATIO, 0.5d);
                break;
            }
            case X_LONG: {
                result *= (float) Math.pow(GOLDEN_RATIO, 1.0d);
                break;
            }
            case XX_LONG: {
                result *= (float) Math.pow(GOLDEN_RATIO, 1.5d);
                break;
            }
            case XXX_LONG: {
                result *= (float) Math.pow(GOLDEN_RATIO, 2.0d);
                break;
            }
        }

        return result;
    }

    public static float getPipThickness(@NonNull PipShape pipShape, @NonNull PipSize pipSize) {
        final float barThicknessScale = (float) (Math.PI / 120d);

        // Scaling factors for dot, triangle and diamond.
        // Relative to a square of side 1. So all greater than 1.
        final float dotScale = 2f / (float) Math.sqrt(Math.PI);
        final float triangleScale = 2f / (float) Math.sqrt(Math.sqrt(3d));
        final float diamondScale = (float) Math.sqrt(2d);

        float result;
        switch (pipShape) {
            case SQUARE_WIDE: {
                result = (float) Math.pow(GOLDEN_RATIO, 2d / 3d);
                break;
            }
            default:
            case SQUARE:
            case SQUARE_CUTOUT: {
                result = (float) Math.pow(GOLDEN_RATIO, 0.0d);
                break;
            }
            case BAR_1_2: {
                result = (float) Math.pow(GOLDEN_RATIO, -2d / 3d);
                break;
            }
            case BAR_1_4: {
                result = (float) Math.pow(GOLDEN_RATIO, -4d / 3d);
                break;
            }
            case BAR_1_8: {
                result = (float) Math.pow(GOLDEN_RATIO, -6d / 3d);
                break;
            }
            case SECTOR: {
                result = barThicknessScale * 2f;
                break;
            }
            case DOT:
            case DOT_THIN:
            case DOT_CUTOUT: {
                result = dotScale;
                break;
            }
            case TRIANGLE:
            case TRIANGLE_THIN:
            case TRIANGLE_CUTOUT: {
                result = triangleScale;
                break;
            }
            case DIAMOND:
            case DIAMOND_THIN:
            case DIAMOND_CUTOUT: {
                result = diamondScale;
                break;
            }
        }

        // For thin types, make their thickness less (and their length more)!
        switch (pipShape) {
            case DOT_THIN:
            case TRIANGLE_THIN:
            case DIAMOND_THIN: {
                result *= (float) Math.pow(GOLDEN_RATIO, -0.5d);
                break;
            }
        }

        if (pipShape != PipShape.SECTOR) {
            switch (pipSize) {
                case XX_SHORT: {
                    result *= (float) Math.pow(GOLDEN_RATIO, -1.5d);
                    break;
                }
                case X_SHORT: {
                    result *= (float) Math.pow(GOLDEN_RATIO, -1.0d);
                    break;
                }
                default:
                case SHORT: {
                    result *= (float) Math.pow(GOLDEN_RATIO, -0.5d);
                    break;
                }
                case MEDIUM: {
                    result *= (float) Math.pow(GOLDEN_RATIO, 0.0d);
                    break;
                }
                case LONG: {
                    result *= (float) Math.pow(GOLDEN_RATIO, 0.5d);
                    break;
                }
                case X_LONG: {
                    result *= (float) Math.pow(GOLDEN_RATIO, 1.0d);
                    break;
                }
                case XX_LONG: {
                    result *= (float) Math.pow(GOLDEN_RATIO, 1.5d);
                    break;
                }
                case XXX_LONG: {
                    result *= (float) Math.pow(GOLDEN_RATIO, 2.0d);
                    break;
                }
            }
        }

        return result;
    }

    public static float getHandLength(@NonNull HandLength handLength) {
        // f0, f1, f2, f3 are a geometric series!
        final float f0 = (float) Math.pow(GOLDEN_RATIO, -1.0d);
        final float f1 = (float) Math.pow(GOLDEN_RATIO, 0.0d);
        final float f2 = (float) Math.pow(GOLDEN_RATIO, 1.0d);
        final float f3 = (float) Math.pow(GOLDEN_RATIO, 2.0d);

        float result;

        switch (handLength) {
            case SHORT: {
                result = 2f + f0;
                break;
            }
            default:
            case MEDIUM: {
                result = 2f + f1;
                break;
            }
            case LONG: {
                result = 2f + f2;
                break;
            }
            case X_LONG: {
                result = 2f + f3;
                break;
            }
        }

        return result;
    }

    public static float getHandThickness(@NonNull HandShape handShape, @NonNull HandThickness handThickness) {
        final float globalScale = 1.0f;

        // f0, f1, f2, f3 are a geometric series!
        final float f0 = (float) Math.pow(GOLDEN_RATIO, -1.0d);
        final float f1 = (float) Math.pow(GOLDEN_RATIO, 0.0d);
        final float f2 = (float) Math.pow(GOLDEN_RATIO, 1.0d);
        final float f3 = (float) Math.pow(GOLDEN_RATIO, 2.0d);

        float result = globalScale;

        switch (handShape) {
            default:
            case STRAIGHT:
            case ROUNDED: {
                result *= 1.0f;
                break;
            }
            case DIAMOND:
            case TRIANGLE: {
                // Diamonds are drawn slightly thicker to account for the fact they taper at the ends.
                result *= f2;
            }
        }

        switch (handThickness) {
            case THIN: {
                result *= f0;
                break;
            }
            default:
            case REGULAR: {
                result *= f1;
                break;
            }
            case THICK: {
                result *= f2;
                break;
            }
            case X_THICK: {
                result *= f3;
                break;
            }
        }

        return result;
    }

    public boolean isScreenRound() {
        return mContext.getResources().getConfiguration().isScreenRound();
    }
    // endregion
}
