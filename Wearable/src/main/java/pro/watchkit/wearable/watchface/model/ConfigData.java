package pro.watchkit.wearable.watchface.model;

import android.content.Context;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import pro.watchkit.wearable.watchface.R;
import pro.watchkit.wearable.watchface.config.ColorSelectionActivity;
import pro.watchkit.wearable.watchface.config.ConfigActivity;
import pro.watchkit.wearable.watchface.config.ConfigRecyclerViewAdapter;
import pro.watchkit.wearable.watchface.config.WatchFaceSelectionActivity;
import pro.watchkit.wearable.watchface.model.BytePackable.Typeface;
import pro.watchkit.wearable.watchface.util.SharedPref;

abstract public class ConfigData {
    /**
     * Includes all data to populate each of the 5 different custom
     * {@link RecyclerView.ViewHolder} types in {@link ConfigRecyclerViewAdapter}.
     */
    @NonNull
    abstract public List<ConfigItemType> getDataToPopulateAdapter();

    /**
     * Interface all ConfigItems must implement so the {@link RecyclerView}'s Adapter associated
     * with the configuration activity knows what type of ViewHolder to inflate.
     */
    public interface ConfigItemType {
        int getConfigType();
    }

    /**
     * A Mutator object, which takes a WatchFaceState and mutates it in a number of ways,
     * returning those mutations in an array of Permutations. For example, a
     * Mutator that works on hand shape may take a WatchFaceState, and return all related
     * WatchFaceStates that differ only by their hand shape, with everything else the same.
     */
    protected interface Mutator {
        /**
         * For the given WatchFaceState (which must be a clone, since we'll modify it in the
         * process) return an array with each permutation and its display name (or resource).
         * <p>
         * As this is the only method in the interface, it can be replaced by a Java lambda
         * expression in code!
         *
         * @param permutation WatchFaceState, which must be a clone, since we'll modify it
         * @return Permutation object array with each permutation
         */
        @NonNull
        Permutation[] getPermutations(WatchFaceState permutation);
    }

    /**
     * A Mutator object that requires access to a SharedPref object, possibly because it
     * does its mutation based on what's currently in preferences.
     */
    protected interface MutatorWithPrefsAccess extends Mutator {
        /**
         * This Mutator is one that needs access to a SharedPref object. Implement this method
         * to pass in such an object.
         *
         * @param sharedPref SharedPref object to access
         */
        void setSharedPref(@NonNull SharedPref sharedPref);
    }

    /**
     * A single permutation of a WatchFaceState. Lots of these are generated by Mutator.
     * <p>
     * Consists of its string value, i.e. its configuration as received
     * from WatchFaceState.getString(), and the name of the permutation.
     */
    public static class Permutation {
        @NonNull
        private final String mValue, mName;
        private final int mSwatch;

        /**
         * Construct the Permutation with the given string value and name.
         *
         * @param value String value as received from WatchFaceState.getString()
         * @param name  Name of this Permutation
         */
        Permutation(@NonNull String value, @NonNull String name) {
            this(value, name, -1);
        }

        /**
         * Construct the Permutation with the given string value and name.
         *
         * @param value  String value as received from WatchFaceState.getString()
         * @param name   Name of this Permutation
         * @param swatch Swatch to display for this Permutation, or -1 if not applicable
         */
        Permutation(@NonNull String value, @NonNull String name, int swatch) {
            mValue = value;
            mName = name;
            mSwatch = swatch;
        }

        /**
         * Gets the string value of this Permutation.
         *
         * @return String value as received from WatchFaceState.getString()
         */
        @NonNull
        public String getValue() {
            return mValue;
        }

        /**
         * Gets the name of this Permutation.
         *
         * @return the name of this Permutation.
         */
        @NonNull
        public String getName() {
            return mName;
        }

        /**
         * Gets the swatch of this Permutation, or -1 if not applicable.
         *
         * @return the swatch of this Permutation.
         */
        public int getSwatch() {
            return mSwatch;
        }
    }

    /**
     * Data for Watch Face Preview item in RecyclerView.
     */
    public static class WatchFaceDrawableConfigItem implements ConfigItemType {
        final private int mFlags;

        WatchFaceDrawableConfigItem(int flags) {
            mFlags = flags;
        }

        @Override
        public int getConfigType() {
            return ConfigRecyclerViewAdapter.TYPE_WATCH_FACE_DRAWABLE_CONFIG;
        }

        public int getFlags() {
            return mFlags;
        }
    }

    /**
     * Data for Watch Face Preview with Complications Preview item in RecyclerView.
     */
    public static class ComplicationConfigItem implements ConfigItemType {
        @DrawableRes
        final private int mDefaultComplicationResourceId;

        ComplicationConfigItem(@DrawableRes int defaultComplicationResourceId) {
            mDefaultComplicationResourceId = defaultComplicationResourceId;
        }

        @DrawableRes
        public int getDefaultComplicationResourceId() {
            return mDefaultComplicationResourceId;
        }

        @Override
        public int getConfigType() {
            return ConfigRecyclerViewAdapter.TYPE_COMPLICATION_CONFIG;
        }
    }

    /**
     * Data for label item in RecyclerView.
     */
    public static class LabelConfigItem implements ConfigItemType {
        @StringRes
        final private int mLabelResourceId;
        @Nullable
        final private Function<WatchFaceState, String> mLabelGenerator;
        @Nullable
        final private Function<WatchFaceState, Boolean> mConfigItemVisibilityCalculator;

        LabelConfigItem(@StringRes final int labelResourceId,
                        @Nullable Function<WatchFaceState, Boolean>
                                configItemVisibilityCalculator) {
            this(labelResourceId, null, configItemVisibilityCalculator);
        }

        private LabelConfigItem(@StringRes final int labelResourceId,
                                @Nullable final Function<WatchFaceState, String> labelGenerator,
                                @Nullable Function<WatchFaceState, Boolean>
                                        configItemVisibilityCalculator) {
            mLabelResourceId = labelResourceId;
            mLabelGenerator = labelGenerator;
            mConfigItemVisibilityCalculator = configItemVisibilityCalculator;
        }

        @StringRes
        public int getLabelResourceId() {
            return mLabelResourceId;
        }

        @Nullable
        public Function<WatchFaceState, String> getLabelGenerator() {
            return mLabelGenerator;
        }

        @Override
        public int getConfigType() {
            return ConfigRecyclerViewAdapter.TYPE_LABEL_CONFIG;
        }

        public boolean isVisible(WatchFaceState watchFaceState) {
            return mConfigItemVisibilityCalculator == null ||
                    mConfigItemVisibilityCalculator.apply(watchFaceState);
        }
    }

    /**
     * A LabelConfigItem that's designed for headers, at the top of the view. It just takes
     * a label resource ID; the "header" part is implementation specific.
     */
    public static class HeadingLabelConfigItem extends LabelConfigItem {
        public HeadingLabelConfigItem(@StringRes final int labelResourceId) {
            super(labelResourceId, null, null);
        }
    }

    /**
     * A LabelConfigItem that's split into two parts: a title (perhaps in bold), followed
     * by a label (perhaps on the same line). Any label that needs a title can use this.
     */
    public static class TitleLabelConfigItem extends LabelConfigItem {
        public TitleLabelConfigItem(@StringRes final int titleResourceId,
                                    @StringRes final int labelResourceId) {
            super(labelResourceId, null, null);
            mTitleResourceId = titleResourceId;
        }

        @StringRes
        final private int mTitleResourceId;

        @StringRes
        public int getTitleResourceId() {
            return mTitleResourceId;
        }
    }

    /**
     * A TitleLabelConfigItem where the title is always the Help title resource.
     */
    public static class HelpLabelConfigItem extends TitleLabelConfigItem {
        public HelpLabelConfigItem(@StringRes final int labelResourceId) {
            super(R.string.config_configure_help, labelResourceId);
        }
    }

    /**
     * Data for typeface item in RecyclerView.
     */
    public static class TypefaceConfigItem implements ConfigItemType {
        @NonNull
        final private Typeface mTypeface;

        TypefaceConfigItem(@NonNull Typeface typeface) {
            mTypeface = typeface;
        }

        @NonNull
        public Typeface getTypeface() {
            return mTypeface;
        }

        @Override
        public int getConfigType() {
            return ConfigRecyclerViewAdapter.TYPE_TYPEFACE_CONFIG;
        }
    }

    /**
     * Data for color picker item in RecyclerView.
     */
    public static class ColorPickerConfigItem implements ConfigItemType {
        @StringRes
        final private int mNameResourceId;
        @DrawableRes
        final private int mIconResourceId;
        final private PaintBox.ColorType mWatchFacePresetColorType;
        final private Class<ColorSelectionActivity> mActivityToChoosePreference;

        ColorPickerConfigItem(
                @StringRes int nameResourceId,
                @DrawableRes int iconResourceId,
                PaintBox.ColorType colorType,
                Class<ColorSelectionActivity> activity) {
            mNameResourceId = nameResourceId;
            mIconResourceId = iconResourceId;
            mWatchFacePresetColorType = colorType;
            mActivityToChoosePreference = activity;
        }

        public PaintBox.ColorType getType() {
            return mWatchFacePresetColorType;
        }

        @StringRes
        public int getNameResourceId() {
            return mNameResourceId;
        }

        @DrawableRes
        public int getIconResourceId() {
            return mIconResourceId;
        }

        public Class<ColorSelectionActivity> getActivityToChoosePreference() {
            return mActivityToChoosePreference;
        }

        @Override
        public int getConfigType() {
            return ConfigRecyclerViewAdapter.TYPE_COLOR_PICKER_CONFIG;
        }
    }

    /**
     * Data for another config (sub-) activity in RecyclerView.
     */
    public static class ConfigActivityConfigItem implements ConfigItemType {
        @StringRes
        final private int mNameResourceId;
        @DrawableRes
        final private int mIconResourceId;
        final private Class<? extends ConfigData> mConfigDataClass;
        final private Class<ConfigActivity> mActivityToChoosePreference;

        ConfigActivityConfigItem(
                @StringRes int nameResourceId,
                @DrawableRes int iconResourceId,
                Class<? extends ConfigData> configDataClass,
                Class<ConfigActivity> activity) {
            mNameResourceId = nameResourceId;
            mIconResourceId = iconResourceId;
            mConfigDataClass = configDataClass;
            mActivityToChoosePreference = activity;
        }

        public Class<? extends ConfigData> getConfigDataClass() {
            return mConfigDataClass;
        }

        @StringRes
        public int getNameResourceId() {
            return mNameResourceId;
        }

        @DrawableRes
        public int getIconResourceId() {
            return mIconResourceId;
        }

        public Class<ConfigActivity> getActivityToChoosePreference() {
            return mActivityToChoosePreference;
        }

        @Override
        public int getConfigType() {
            return ConfigRecyclerViewAdapter.TYPE_CONFIG_ACTIVITY_CONFIG;
        }
    }

    /**
     * A Mutator which generates permutations for a particular setting in WatchFaceState
     * given by "setter". There will be two permutations: when the setting is false, and
     * when it is true.
     */
    protected static class BooleanMutator implements Mutator {
        /**
         * A lambda which sets (or applies) a boolean to the given WatchFaceState.
         */
        private final BiConsumer<WatchFaceState, Boolean> mSetter;

        /**
         * Create the given BooleanMutator.
         *
         * @param setter a lambda which sets (or applies) a boolean to the given Settings
         */
        BooleanMutator(BiConsumer<WatchFaceState, Boolean> setter) {
            mSetter = setter;
        }

        /**
         * For the given WatchFaceState (which must be a clone, since we'll modify it in the
         * process) return a String array with each permutation.
         *
         * @param clone WatchFaceState, which must be a clone, since we'll modify it
         * @return String array with each permutation
         */
        @NonNull
        public Permutation[] getPermutations(@NonNull WatchFaceState clone) {
            mSetter.accept(clone, false);
            String key0 = clone.getString();
            mSetter.accept(clone, true);
            String key1 = clone.getString();

            return new Permutation[]{
                    new Permutation(key0, "false"),
                    new Permutation(key1, "true")
            };
        }
    }

    /**
     * A Mutator which generates permutations for a particular setting in WatchFaceState
     * given by "setter". Each permutation corresponds to a possible value of Enum E.
     *
     * @param <E> The Enum for which permutations are generated.
     */
    protected static class EnumMutator<E extends Enum<?>> implements Mutator {
        /**
         * All the possible Enum values of E.
         */
        @NonNull
        private final E[] mValues;

        /**
         * A lambda which sets (or applies) setting E to the given WatchFaceState.
         */
        @NonNull
        private final BiConsumer<WatchFaceState, E> mSetter;

        /**
         * The Material which will be displayed as a swatch, if non-null.
         */
        @Nullable
        private final BytePackable.Material mSwatchMaterial;

        /**
         * Create the given EnumMutator for the E of type Enum.
         *
         * @param values all possible enumeration values of E
         * @param setter a lambda which sets (or applies) setting E to the given WatchFaceState
         */
        EnumMutator(@NonNull E[] values, @NonNull BiConsumer<WatchFaceState, E> setter) {
            this(values, setter, null);
        }

        /**
         * Create the given EnumMutator for the E of type Enum.
         *
         * @param values         all possible enumeration values of E
         * @param setter         a lambda which sets (or applies) setting E to the given WatchFaceState
         * @param swatchMaterial a Material which will be displayed as a swatch
         */
        EnumMutator(@NonNull E[] values, @NonNull BiConsumer<WatchFaceState, E> setter,
                    @Nullable BytePackable.Material swatchMaterial) {
            mValues = values;
            mSetter = setter;
            mSwatchMaterial = swatchMaterial;
        }

        /**
         * For the given WatchFaceState (which must be a clone, since we'll modify it in the
         * process) return a String array with each permutation.
         *
         * @param clone WatchFaceState, which must be a clone, since we'll modify it
         * @return String array with each permutation
         */
        @NonNull
        public Permutation[] getPermutations(@NonNull WatchFaceState clone) {
            return Arrays.stream(mValues).map(h -> {
                // Call "mSetter" on "clone" with enum value "h".
                mSetter.accept(clone, h);

                // And, if it's a swatch, set the swatch material.
                int swatch = -1;
                if (h instanceof BytePackable.Material) {
                    swatch = h.ordinal();
                } else if (mSwatchMaterial != null) {
                    swatch = mSwatchMaterial.ordinal();
                }

                // Generate the name of this permutation.
                String name;
                if (h == null) {
                    name = "???";
                } else if (h instanceof BytePackable.EnumResourceId) {
                    BytePackable.EnumResourceId f = (BytePackable.EnumResourceId) h;
                    name = clone.getStringArrayResource(f.getNameResourceId())[h.ordinal()];
                } else {
                    name = h.name();
                }

                return new Permutation(clone.getString(), name, swatch);
            }).toArray(Permutation[]::new);
        }
    }

    public static class PickerConfigItem implements ConfigItemType {
        @StringRes
        final private int mNameResourceId;
        @DrawableRes
        final private int mIconResourceId;
        @NonNull
        final private Class<WatchFaceSelectionActivity> mActivityToChoosePreference;
        @NonNull
        final private Mutator mWatchFaceStateMutator;
        @NonNull
        final private Function<WatchFaceState, Boolean> mConfigItemVisibilityCalculator;
        final private int mWatchFaceGlobalDrawableFlags;

        PickerConfigItem(
                @StringRes int nameResourceId,
                @DrawableRes int iconResourceId,
                int watchFaceGlobalDrawableFlags,
                @NonNull Class<WatchFaceSelectionActivity> activityToChoosePreference,
                @NonNull Mutator watchFaceStateMutator) {
            this(nameResourceId, iconResourceId, watchFaceGlobalDrawableFlags, activityToChoosePreference,
                    watchFaceStateMutator, p -> true /* Always visible! */);
        }

        PickerConfigItem(
                @StringRes int nameResourceId,
                @DrawableRes int iconResourceId,
                int watchFaceGlobalDrawableFlags,
                @NonNull Class<WatchFaceSelectionActivity> activityToChoosePreference,
                @NonNull Mutator watchFaceStateMutator,
                @NonNull Function<WatchFaceState, Boolean> configItemVisibilityCalculator) {
            mNameResourceId = nameResourceId;
            mIconResourceId = iconResourceId;
            mActivityToChoosePreference = activityToChoosePreference;
            mConfigItemVisibilityCalculator = configItemVisibilityCalculator;
            mWatchFaceGlobalDrawableFlags = watchFaceGlobalDrawableFlags;
            mWatchFaceStateMutator = watchFaceStateMutator;
        }

        @StringRes
        public int getNameResourceId() {
            return mNameResourceId;
        }

        public int getWatchFaceGlobalDrawableFlags() {
            return mWatchFaceGlobalDrawableFlags;
        }

        @DrawableRes
        public int getIconResourceId() {
            return mIconResourceId;
        }

        @NonNull
        public Class<WatchFaceSelectionActivity> getActivityToChoosePreference() {
            return mActivityToChoosePreference;
        }

        @Override
        public int getConfigType() {
            return ConfigRecyclerViewAdapter.TYPE_PICKER_CONFIG;
        }

        public void setSharedPref(@NonNull SharedPref sharedPref) {
            if (mWatchFaceStateMutator instanceof MutatorWithPrefsAccess) {
                MutatorWithPrefsAccess m = (MutatorWithPrefsAccess) mWatchFaceStateMutator;
                m.setSharedPref(sharedPref);
            }
        }

        @NonNull
        public Permutation[] getPermutations(
                @NonNull WatchFaceState watchFaceState, @NonNull Context context) {
            WatchFaceState clone = new WatchFaceState(context);
            clone.setString(watchFaceState.getString());
            return mWatchFaceStateMutator.getPermutations(clone);
        }

        public boolean isVisible(WatchFaceState watchFaceState) {
            return mConfigItemVisibilityCalculator.apply(watchFaceState);
        }
    }
    /**
     * Data for toggle config item in RecyclerView.
     */
    public static class ToggleConfigItem implements ConfigItemType {

        @StringRes
        final private int mNameResourceId;
        @DrawableRes
        final private int mIconEnabledResourceId;
        @DrawableRes
        final private int mIconDisabledResourceId;
        final private Mutator mMutator;
        final private Function<WatchFaceState, Boolean> mConfigItemVisibilityCalculator;

        ToggleConfigItem(
                @StringRes int nameResourceId,
                @DrawableRes int iconEnabledResourceId,
                @DrawableRes int iconDisabledResourceId,
                Mutator mutator) {
            this(nameResourceId, iconEnabledResourceId, iconDisabledResourceId, mutator, null);
        }

        ToggleConfigItem(
                @StringRes int nameResourceId,
                @DrawableRes int iconEnabledResourceId,
                @DrawableRes int iconDisabledResourceId,
                Mutator mutator,
                Function<WatchFaceState, Boolean> configItemVisibilityCalculator) {
            mNameResourceId = nameResourceId;
            mIconEnabledResourceId = iconEnabledResourceId;
            mIconDisabledResourceId = iconDisabledResourceId;
            mMutator = mutator;
            mConfigItemVisibilityCalculator = configItemVisibilityCalculator;
        }

        @StringRes
        public int getNameResourceId() {
            return mNameResourceId;
        }

        @DrawableRes
        public int getIconEnabledResourceId() {
            return mIconEnabledResourceId;
        }

        @DrawableRes
        public int getIconDisabledResourceId() {
            return mIconDisabledResourceId;
        }

        @Override
        public int getConfigType() {
            return ConfigRecyclerViewAdapter.TYPE_TOGGLE_CONFIG;
        }

        public String[] permute(@NonNull WatchFaceState watchFaceState, @NonNull Context context) {
            WatchFaceState deepCopy = new WatchFaceState(context);
            deepCopy.setString(watchFaceState.getString());
            return Arrays.stream(mMutator.getPermutations(deepCopy))
                    .map(Permutation::getValue).toArray(String[]::new);
        }

        public boolean isVisible(WatchFaceState watchFaceState) {
            return mConfigItemVisibilityCalculator == null ||
                    mConfigItemVisibilityCalculator.apply(watchFaceState);
        }
    }
}
