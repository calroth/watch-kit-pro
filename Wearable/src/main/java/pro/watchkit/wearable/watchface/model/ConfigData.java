package pro.watchkit.wearable.watchface.model;

import android.content.Context;
import android.text.Html;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import pro.watchkit.wearable.watchface.config.ColorSelectionActivity;
import pro.watchkit.wearable.watchface.config.ConfigActivity;
import pro.watchkit.wearable.watchface.config.ConfigRecyclerViewAdapter;
import pro.watchkit.wearable.watchface.config.WatchFaceSelectionActivity;
import pro.watchkit.wearable.watchface.model.BytePackable.Material;
import pro.watchkit.wearable.watchface.model.BytePackable.TextStyle;
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
     * returning those mutations in an array of WatchFaceState strings. For example, a
     * Mutator that works on hand shape may take a WatchFaceState, and return all related
     * WatchFaceStates that differ only by their hand shape, with everything else the same.
     */
    protected interface Mutator {
        /**
         * For the given WatchFaceState (which must be a clone, since we'll modify it in the
         * process) return a String array with each permutation.
         *
         * @param permutation WatchFaceState, which must be a clone, since we'll modify it
         * @return String array with each permutation
         */
        String[] permute(WatchFaceState permutation);

        /**
         * For the given WatchFaceState (which is our current preference) return the current
         * value.
         *
         * @param currentPreset WatchFaceState of our current preference
         * @return Value that it's currently set to
         */
        @Nullable
        Enum<?> getCurrentValue(WatchFaceState currentPreset);
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
        @StringRes
        final private int mTitleResourceId;
        final private boolean mWithTitle;
        @Nullable
        final private Function<WatchFaceState, Boolean> mConfigItemVisibilityCalculator;

        public LabelConfigItem(@StringRes final int labelResourceId) {
            this(-1, labelResourceId, null, true, null);
        }

        LabelConfigItem(@StringRes final int titleResourceId,
                        @StringRes final int labelResourceId) {
            this(titleResourceId, labelResourceId, null, false, null);
        }

        LabelConfigItem(@StringRes final int titleResourceId,
                        @NonNull Function<WatchFaceState, String> labelGenerator) {
            this(titleResourceId, -1, labelGenerator, false, null);
        }

        LabelConfigItem(@StringRes final int titleResourceId,
                        @StringRes final int labelResourceId,
                        @Nullable Function<WatchFaceState, Boolean>
                                configItemVisibilityCalculator) {
            this(titleResourceId, labelResourceId, null, titleResourceId != -1,
                    configItemVisibilityCalculator);
        }

        private LabelConfigItem(@StringRes final int titleResourceId,
                                @StringRes final int labelResourceId,
                                @Nullable final Function<WatchFaceState, String> labelGenerator,
                                boolean withTitle,
                                @Nullable Function<WatchFaceState, Boolean>
                                        configItemVisibilityCalculator) {
            mTitleResourceId = titleResourceId;
            mLabelResourceId = labelResourceId;
            mLabelGenerator = labelGenerator;
            mWithTitle = withTitle;
            mConfigItemVisibilityCalculator = configItemVisibilityCalculator;
        }

        @StringRes
        public int getTitleResourceId() {
            return mTitleResourceId;
        }

        @StringRes
        public int getLabelResourceId() {
            return mLabelResourceId;
        }

        @Nullable
        public Function<WatchFaceState, String> getLabelGenerator() {
            return mLabelGenerator;
        }

        public boolean getWithTitle() {
            return mWithTitle;
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
         * @param permutation WatchFaceState, which must be a clone, since we'll modify it
         * @return String array with each permutation
         */
        @NonNull
        @Override
        public String[] permute(@NonNull WatchFaceState permutation) {
            String[] result = new String[2];
            mSetter.accept(permutation, false);
            result[0] = permutation.getString();
            mSetter.accept(permutation, true);
            result[1] = permutation.getString();
            return result;
        }

        /**
         * For the given WatchFaceState (which is our current preference) return the current
         * value.
         *
         * @param currentPreset WatchFaceState of our current preference
         * @return Value that it's currently set to (always null for a BooleanMutator)
         */
        @Nullable
        public Enum<?> getCurrentValue(WatchFaceState currentPreset) {
            return null;
        }
    }

    protected static class EnumMutator<E extends Enum<?>> implements Mutator {
        /**
         * All the possible Enum values of E.
         */
        private final E[] mValues;

        /**
         * A lambda which sets (or applies) setting E to the given WatchFaceState.
         */
        private final BiConsumer<WatchFaceState, E> mSetter;

        /**
         * A lambda which gets and returns setting E from the given WatchFaceState.
         */
        private final Function<WatchFaceState, E> mGetter;

        /**
         * Create the given EnumMutator for the E of type Enum.
         *
         * @param values all possible enumeration values of E
         * @param setter a lambda which sets (or applies) setting E to the given WatchFaceState
         * @param getter a lambda which gets and returns setting E from the given WatchFaceState
         */
        EnumMutator(E[] values, BiConsumer<WatchFaceState, E> setter,
                    Function<WatchFaceState, E> getter) {
            mValues = values;
            mSetter = setter;
            mGetter = getter;
        }

        /**
         * For the given WatchFaceState (which must be a clone, since we'll modify it in the
         * process) return a String array with each permutation.
         *
         * @param permutation WatchFaceState, which must be a clone, since we'll modify it
         * @return String array with each permutation
         */
        public String[] permute(@NonNull WatchFaceState permutation) {
            return Arrays.stream(mValues).map(h -> {
                mSetter.accept(permutation, h);
                // if (h instanceof StyleGradient || h instanceof StyleTexture)
                // then we do the "setSwatchMaterial" inline in their setters in WatchFaceState
                // else...
                if (h instanceof Material) {
                    permutation.setSwatchMaterial((Material) h);
                } else if (h instanceof TextStyle) {
                    permutation.setSwatchTextStyle((TextStyle) h);
                }
                return permutation.getString();
            }).toArray(String[]::new);
        }

        /**
         * For the given WatchFaceState (which is our current preference) return the current
         * value.
         *
         * @param currentPreset WatchFaceState of our current preference
         * @return Value that it's currently set to
         */
        public E getCurrentValue(WatchFaceState currentPreset) {
            return mGetter.apply(currentPreset);
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
        @Nullable
        final private Function<WatchFaceState, Boolean> mConfigItemVisibilityCalculator;
        final private int mWatchFaceGlobalDrawableFlags;
        @Nullable
        final private Material mMaterial;

        PickerConfigItem(
                @StringRes int nameResourceId,
                @DrawableRes int iconResourceId,
                int watchFaceGlobalDrawableFlags,
                @NonNull Class<WatchFaceSelectionActivity> activityToChoosePreference,
                @NonNull Mutator watchFaceStateMutator) {
            this(nameResourceId, iconResourceId, watchFaceGlobalDrawableFlags, activityToChoosePreference,
                    null, watchFaceStateMutator, null);
        }

        PickerConfigItem(
                @StringRes int nameResourceId,
                @DrawableRes int iconResourceId,
                int watchFaceGlobalDrawableFlags,
                @NonNull Class<WatchFaceSelectionActivity> activityToChoosePreference,
                @NonNull Material material,
                @NonNull Mutator watchFaceStateMutator) {
            this(nameResourceId, iconResourceId, watchFaceGlobalDrawableFlags, activityToChoosePreference,
                    material, watchFaceStateMutator, null);
        }

        PickerConfigItem(
                @StringRes int nameResourceId,
                @DrawableRes int iconResourceId,
                int watchFaceGlobalDrawableFlags,
                @NonNull Class<WatchFaceSelectionActivity> activityToChoosePreference,
                @NonNull Mutator watchFaceStateMutator,
                @NonNull Function<WatchFaceState, Boolean> configItemVisibilityCalculator) {
            this(nameResourceId, iconResourceId, watchFaceGlobalDrawableFlags, activityToChoosePreference,
                    null, watchFaceStateMutator, configItemVisibilityCalculator);
        }

        private PickerConfigItem(
                @StringRes int nameResourceId,
                @DrawableRes int iconResourceId,
                int watchFaceGlobalDrawableFlags,
                @NonNull Class<WatchFaceSelectionActivity> activityToChoosePreference,
                @Nullable Material material,
                @NonNull Mutator watchFaceStateMutator,
                @Nullable Function<WatchFaceState, Boolean> configItemVisibilityCalculator) {
            mNameResourceId = nameResourceId;
            mIconResourceId = iconResourceId;
            mActivityToChoosePreference = activityToChoosePreference;
            mMaterial = material;
            mConfigItemVisibilityCalculator = configItemVisibilityCalculator;
            mWatchFaceGlobalDrawableFlags = watchFaceGlobalDrawableFlags;
            mWatchFaceStateMutator = watchFaceStateMutator;
        }

        private final StringBuilder mExtra = new StringBuilder();

        @StringRes
        public int getNameResourceId() {
            return mNameResourceId;
        }

        public CharSequence getName(
                @NonNull WatchFaceState watchFaceState, @NonNull Context context) {
            Enum<?> e = mWatchFaceStateMutator.getCurrentValue(watchFaceState);

            String name = context.getString(mNameResourceId);

            if (e == null) {
                return name;
            } else if (e instanceof BytePackable.EnumResourceId) {
                BytePackable.EnumResourceId f = (BytePackable.EnumResourceId) e;
                mExtra.setLength(0);
                // Append mNameResourceId of current setting.
                mExtra.append(name).append("<br/><small>")
                        .append(context.getResources().getStringArray(
                                f.getNameResourceId())[e.ordinal()]).append("</small>");
                // Append any settings whose style are set to this.
                watchFaceState.getConfigItemLabelsSetToMaterial(mMaterial).forEach(
                        s -> mExtra.append("<br/><small> &bull; ").append(s).append("</small>"));
                return Html.fromHtml(mExtra.toString(), Html.FROM_HTML_MODE_LEGACY);
            } else {
                return Html.fromHtml(name + "<br/><small>" +
                        e.getClass().getSimpleName() + " ~ " + e.name() +
                        "</small>", Html.FROM_HTML_MODE_LEGACY);
            }
        }

        public String getExtraName(
                @NonNull WatchFaceState watchFaceState, @NonNull Context context) {
            Enum<?> e = mWatchFaceStateMutator.getCurrentValue(watchFaceState);
            String name = context.getString(mNameResourceId);

            if (e == null) {
                return name;
            } else if (e instanceof BytePackable.EnumResourceId) {
                BytePackable.EnumResourceId f = (BytePackable.EnumResourceId) e;
                return context.getResources().getStringArray(f.getNameResourceId())[e.ordinal()];
            } else {
                return e.name();
            }
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

        @Nullable
        public String[] permute(@NonNull WatchFaceState watchFaceState, Context context,
                                @NonNull SharedPref sharedPref) {
            if (mWatchFaceStateMutator instanceof MutatorWithPrefsAccess) {
                MutatorWithPrefsAccess m = (MutatorWithPrefsAccess) mWatchFaceStateMutator;
                m.setSharedPref(sharedPref);
            }
            WatchFaceState deepCopy = new WatchFaceState(context);
            deepCopy.setString(watchFaceState.getString());
//            return mWatchFaceStateMutator.permute(deepCopy);
            String[] result = mWatchFaceStateMutator.permute(deepCopy);
            if (mWatchFaceStateMutator instanceof MutatorWithPrefsAccess) {
                Arrays.stream(result).forEach(s -> android.util.Log.d("permute", s));
            }
            return result;
        }

        public boolean isVisible(WatchFaceState watchFaceState) {
            return mConfigItemVisibilityCalculator == null ||
                    mConfigItemVisibilityCalculator.apply(watchFaceState);
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

        public String[] permute(@NonNull WatchFaceState watchFaceState, Context context) {
            WatchFaceState deepCopy = new WatchFaceState(context);
            deepCopy.setString(watchFaceState.getString());
            return mMutator.permute(deepCopy);
        }

        public boolean isVisible(WatchFaceState watchFaceState) {
            return mConfigItemVisibilityCalculator == null ||
                    mConfigItemVisibilityCalculator.apply(watchFaceState);
        }
    }
}
