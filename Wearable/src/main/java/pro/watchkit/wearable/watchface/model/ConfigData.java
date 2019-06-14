package pro.watchkit.wearable.watchface.model;

import android.content.Context;
import android.text.Html;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import pro.watchkit.wearable.watchface.config.ColorSelectionActivity;
import pro.watchkit.wearable.watchface.config.ConfigActivity;
import pro.watchkit.wearable.watchface.config.ConfigRecyclerViewAdapter;
import pro.watchkit.wearable.watchface.config.WatchFaceSelectionActivity;
import pro.watchkit.wearable.watchface.watchface.AnalogComplicationWatchFaceService;

abstract public class ConfigData {
    /**
     * Returns Watch Face Service class associated with configuration Activity.
     */
    @NonNull
    final public Class getWatchFaceServiceClass() {
        return AnalogComplicationWatchFaceService.class;
    }

    /**
     * Includes all data to populate each of the 5 different custom
     * {@link RecyclerView.ViewHolder} types in {@link ConfigRecyclerViewAdapter}.
     */
    abstract public List<ConfigItemType> getDataToPopulateAdapter(Context context);

    /**
     * Interface all ConfigItems must implement so the {@link RecyclerView}'s Adapter associated
     * with the configuration activity knows what type of ViewHolder to inflate.
     */
    public interface ConfigItemType {
        int getConfigType();
    }

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
        Enum getCurrentValue(WatchFaceState currentPreset);
    }

    /**
     * Data for Watch Face Preview item in RecyclerView.
     */
    public static class WatchFaceDrawableConfigItem implements ConfigItemType {

        private int mFlags;

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

        private int defaultComplicationResourceId;

        ComplicationConfigItem(int defaultComplicationResourceId) {
            this.defaultComplicationResourceId = defaultComplicationResourceId;
        }

        public int getDefaultComplicationResourceId() {
            return defaultComplicationResourceId;
        }

        @Override
        public int getConfigType() {
            return ConfigRecyclerViewAdapter.TYPE_COMPLICATION_CONFIG;
        }
    }

    /**
     * Data for color picker item in RecyclerView.
     */
    public static class ColorPickerConfigItem implements ConfigItemType {

        private String name;
        private int iconResourceId;
        private PaintBox.ColorType mWatchFacePresetColorType;
        private Class<ColorSelectionActivity> activityToChoosePreference;

        ColorPickerConfigItem(
                String name,
                int iconResourceId,
                PaintBox.ColorType colorType,
                Class<ColorSelectionActivity> activity) {
            this.name = name;
            this.iconResourceId = iconResourceId;
            this.mWatchFacePresetColorType = colorType;
            this.activityToChoosePreference = activity;
        }

        public PaintBox.ColorType getType() {
            return mWatchFacePresetColorType;
        }

        public String getName() {
            return name;
        }

        public int getIconResourceId() {
            return iconResourceId;
        }

//        public String getSharedPrefString() {
//            return sharedPrefString;
//        }

        public Class<ColorSelectionActivity> getActivityToChoosePreference() {
            return activityToChoosePreference;
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

        private String name;
        private int iconResourceId;
        //        private WatchFacePreset.ColorType mWatchFacePresetColorType;
        private Class<? extends ConfigData> mConfigDataClass;
        private Class<ConfigActivity> activityToChoosePreference;

        ConfigActivityConfigItem(
                String name,
                int iconResourceId,
                Class<? extends ConfigData> configDataClass,
                Class<ConfigActivity> activity) {
            this.name = name;
            this.iconResourceId = iconResourceId;
            mConfigDataClass = configDataClass;
            this.activityToChoosePreference = activity;
        }

        public Class<? extends ConfigData> getConfigDataClass() {
            return mConfigDataClass;
        }

        public String getName() {
            return name;
        }

        public int getIconResourceId() {
            return iconResourceId;
        }

//        public String getSharedPrefString() {
//            return sharedPrefString;
//        }

        public Class<ConfigActivity> getActivityToChoosePreference() {
            return activityToChoosePreference;
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
        private BiConsumer<WatchFaceState, Boolean> mSetter;

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
        public Enum getCurrentValue(WatchFaceState currentPreset) {
            return null;
        }
    }

    protected static class EnumMutator<E extends Enum> implements Mutator {
        /**
         * All the possible Enum values of E.
         */
        private E[] mValues;

        /**
         * A lambda which sets (or applies) setting E to the given WatchFaceState.
         */
        private BiConsumer<WatchFaceState, E> mSetter;

        /**
         * A lambda which gets and returns setting E from the given WatchFaceState.
         */
        private Function<WatchFaceState, E> mGetter;

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
        private String mName;
        private int mIconResourceId;
        private Class<WatchFaceSelectionActivity> mActivityToChoosePreference;
        private Mutator mWatchFaceStateMutator;
        private Function<WatchFaceState, Boolean> mConfigItemVisibilityCalculator;
        private int mWatchFaceGlobalDrawableFlags;

        PickerConfigItem(
                String name,
                int iconResourceId,
                int watchFaceGlobalDrawableFlags,
                Class<WatchFaceSelectionActivity> activity,
                Mutator mutator) {
            this(name, iconResourceId, watchFaceGlobalDrawableFlags, activity, mutator, null);
        }

        PickerConfigItem(
                String name,
                int iconResourceId,
                int watchFaceGlobalDrawableFlags,
                Class<WatchFaceSelectionActivity> activity,
                Mutator mutator,
                Function<WatchFaceState, Boolean> configItemVisibilityCalculator) {
            this(name, iconResourceId, watchFaceGlobalDrawableFlags, activity, configItemVisibilityCalculator);
            mWatchFaceStateMutator = mutator;
        }

        private PickerConfigItem(
                String name,
                int iconResourceId,
                int watchFaceGlobalDrawableFlags,
                Class<WatchFaceSelectionActivity> activity,
                Function<WatchFaceState, Boolean> configItemVisibilityCalculator) {
            mName = name;
            mIconResourceId = iconResourceId;
            mActivityToChoosePreference = activity;
            mConfigItemVisibilityCalculator = configItemVisibilityCalculator;
            mWatchFaceGlobalDrawableFlags = watchFaceGlobalDrawableFlags;
        }

        public CharSequence getName(
                WatchFaceState watchFaceState, @NonNull Context context) {
            Enum e = null;
            if (mWatchFaceStateMutator != null) {
                e = mWatchFaceStateMutator.getCurrentValue(watchFaceState);
            }

            if (e == null) {
                return mName;
            } else if (e instanceof BytePackable.EnumResourceId) {
                BytePackable.EnumResourceId f = (BytePackable.EnumResourceId) e;
                return Html.fromHtml(mName + "<br/><small>" +
                        context.getResources().getStringArray(f.getNameResourceId())[e.ordinal()] +
                        "</small>", Html.FROM_HTML_MODE_LEGACY);
            } else {
                return Html.fromHtml(mName + "<br/><small>" +
                        e.getClass().getSimpleName() + " ~ " + e.name() +
                        "</small>", Html.FROM_HTML_MODE_LEGACY);
            }
        }

        public int getWatchFaceGlobalDrawableFlags() {
            return mWatchFaceGlobalDrawableFlags;
        }

        public int getIconResourceId() {
            return mIconResourceId;
        }

        public Class<WatchFaceSelectionActivity> getActivityToChoosePreference() {
            return mActivityToChoosePreference;
        }

        @Override
        public int getConfigType() {
            return ConfigRecyclerViewAdapter.TYPE_PICKER_CONFIG;
        }

        @Nullable
        public String[] permute(@NonNull WatchFaceState watchFaceState, Context context) {
            WatchFaceState deepCopy = new WatchFaceState(context);
            deepCopy.setString(watchFaceState.getString());

            if (mWatchFaceStateMutator != null) {
                return mWatchFaceStateMutator.permute(deepCopy);
            } else {
                return null;
            }
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

        private String name;
        private int iconEnabledResourceId;
        private int iconDisabledResourceId;
        private Mutator mMutator;
        private Function<WatchFaceState, Boolean> mConfigItemVisibilityCalculator;

        ToggleConfigItem(
                String name,
                int iconEnabledResourceId,
                int iconDisabledResourceId,
                Mutator mutator) {
            this(name, iconEnabledResourceId, iconDisabledResourceId, mutator, null);
        }

        ToggleConfigItem(
                String name,
                int iconEnabledResourceId,
                int iconDisabledResourceId,
                Mutator mutator,
                Function<WatchFaceState, Boolean> configItemVisibilityCalculator) {
            this.name = name;
            this.iconEnabledResourceId = iconEnabledResourceId;
            this.iconDisabledResourceId = iconDisabledResourceId;
            mMutator = mutator;
            mConfigItemVisibilityCalculator = configItemVisibilityCalculator;
        }

        public String getName() {
            return name;
        }

        public int getIconEnabledResourceId() {
            return iconEnabledResourceId;
        }

        public int getIconDisabledResourceId() {
            return iconDisabledResourceId;
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
