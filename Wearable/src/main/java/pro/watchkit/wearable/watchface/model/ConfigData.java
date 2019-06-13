package pro.watchkit.wearable.watchface.model;

import android.content.Context;
import android.text.Html;

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

    protected interface Mutator<B> {
        /**
         * For the given Settings (which must be a clone, since we'll modify it in the
         * process) return a String array with each permutation.
         *
         * @param permutation Settings, which must be a clone, since we'll modify it
         * @return String array with each permutation
         */
        String[] permute(B permutation);

        /**
         * For the given Settings (which is our current preference) return the current
         * value.
         *
         * @param currentPreset Settings of our current preference
         * @return Value that it's currently set to
         */
        Enum getCurrentValue(B currentPreset);
    }

    /**
     * Objects inherit this interface to determine the visibility of a ConfigItem.
     * That is, implement this interface and put in some custom logic that determines
     * whether an item is visible or not.
     */
    protected interface ConfigItemVisibilityCalculator {
        boolean isVisible(WatchFaceState watchFaceState);
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

    protected static class MutatorImpl<E extends Enum, B extends WatchFaceState>
            implements Mutator<B> {
        /**
         * All the possible Enum values of E.
         */
        private E[] mValues;

        /**
         * A lambda which sets (or applies) setting E to the given BytePackable.
         */
        private BiConsumer<B, E> mSetter;

        /**
         * A lambda which gets and returns setting E from the given BytePackable.
         */
        private Function<B, E> mGetter;

        /**
         * Create the given SettingsMutatorImpl for the E of type Enum.
         *
         * @param values all possible enumeration values of E
         * @param setter a lambda which sets (or applies) setting E to the given Settings
         * @param getter a lambda which gets and returns setting E from the given Settings
         */
        MutatorImpl(E[] values, BiConsumer<B, E> setter, Function<B, E> getter) {
            mValues = values;
            mSetter = setter;
            mGetter = getter;
        }

        /**
         * For the given BytePackable (which must be a clone, since we'll modify it in the
         * process) return a String array with each permutation.
         *
         * @param permutation BytePackable, which must be a clone, since we'll modify it
         * @return String array with each permutation
         */
        public String[] permute(B permutation) {
            return Arrays.stream(mValues).map(h -> {
                mSetter.accept(permutation, h);
                return permutation.getString();
            }).toArray(String[]::new);
        }

        /**
         * For the given BytePackable (which is our current preference) return the current
         * value.
         *
         * @param currentPreset BytePackable of our current preference
         * @return Value that it's currently set to
         */
        public E getCurrentValue(B currentPreset) {
            return mGetter.apply(currentPreset);
        }
    }

    public static class WatchFacePickerConfigItem implements ConfigItemType {
        private String mName;
        private int mIconResourceId;
        private Class<WatchFaceSelectionActivity> mActivityToChoosePreference;
        private Mutator<WatchFaceState> mWatchFaceStateMutator;
        private ConfigItemVisibilityCalculator mConfigItemVisibilityCalculator;
        private int mWatchFaceGlobalDrawableFlags;

        WatchFacePickerConfigItem(
                String name,
                int iconResourceId,
                int watchFaceGlobalDrawableFlags,
                Class<WatchFaceSelectionActivity> activity,
                Mutator<WatchFaceState> mutator) {
            this(name, iconResourceId, watchFaceGlobalDrawableFlags, activity, mutator, null);
        }

        WatchFacePickerConfigItem(
                String name,
                int iconResourceId,
                int watchFaceGlobalDrawableFlags,
                Class<WatchFaceSelectionActivity> activity,
                Mutator<WatchFaceState> mutator,
                ConfigItemVisibilityCalculator configItemVisibilityCalculator) {
            this(name, iconResourceId, watchFaceGlobalDrawableFlags, activity, configItemVisibilityCalculator);
            mWatchFaceStateMutator = mutator;
        }

        private WatchFacePickerConfigItem(
                String name,
                int iconResourceId,
                int watchFaceGlobalDrawableFlags,
                Class<WatchFaceSelectionActivity> activity,
                ConfigItemVisibilityCalculator configItemVisibilityCalculator) {
            mName = name;
            mIconResourceId = iconResourceId;
            mActivityToChoosePreference = activity;
            mConfigItemVisibilityCalculator = configItemVisibilityCalculator;
            mWatchFaceGlobalDrawableFlags = watchFaceGlobalDrawableFlags;
        }

        public CharSequence getName(
                WatchFaceState watchFaceState, Context context) {
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
            return ConfigRecyclerViewAdapter.TYPE_WATCH_FACE_PRESET_PICKER_CONFIG;
        }

        public String[] permute(WatchFaceState watchFaceState, Context context) {
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
                    mConfigItemVisibilityCalculator.isVisible(watchFaceState);
        }
    }

    /**
     * Data for Night Vision preference picker item in RecyclerView.
     */
    public static class WatchFacePresetToggleConfigItem implements ConfigItemType {

        private String name;
        private int iconEnabledResourceId;
        private int iconDisabledResourceId;
        private Mutator<WatchFaceState> mMutator;

        WatchFacePresetToggleConfigItem(
                String name,
                int iconEnabledResourceId,
                int iconDisabledResourceId,
                Mutator<WatchFaceState> mutator) {
            this.name = name;
            this.iconEnabledResourceId = iconEnabledResourceId;
            this.iconDisabledResourceId = iconDisabledResourceId;
            this.mMutator = mutator;
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
            return ConfigRecyclerViewAdapter.TYPE_WATCH_FACE_PRESET_TOGGLE_CONFIG;
        }

        public String[] permute(WatchFaceState watchFaceState, Context context) {
            WatchFaceState deepCopy = new WatchFaceState(context);
            deepCopy.setString(watchFaceState.getString());
            return mMutator.permute(deepCopy);
        }
    }

    /**
     * Data for Unread Notification preference picker item in RecyclerView.
     */
    public static class UnreadNotificationConfigItem implements ConfigItemType {

        private String name;
        private int iconEnabledResourceId;
        private int iconDisabledResourceId;
//        private int sharedPrefId;

        UnreadNotificationConfigItem(
                String name,
                int iconEnabledResourceId,
                int iconDisabledResourceId/*,
                int sharedPrefId*/) {
            this.name = name;
            this.iconEnabledResourceId = iconEnabledResourceId;
            this.iconDisabledResourceId = iconDisabledResourceId;
//            this.sharedPrefId = sharedPrefId;
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

//        public int getSharedPrefId() {
//            return sharedPrefId;
//        }

        @Override
        public int getConfigType() {
            return ConfigRecyclerViewAdapter.TYPE_UNREAD_NOTIFICATION_CONFIG;
        }
    }

    /**
     * Data for Night Vision preference picker item in RecyclerView.
     */
    public static class NightVisionConfigItem implements ConfigItemType {

        private String name;
        private int iconEnabledResourceId;
        private int iconDisabledResourceId;
//        private int sharedPrefId;

        NightVisionConfigItem(
                String name,
                int iconEnabledResourceId,
                int iconDisabledResourceId/*,
                int sharedPrefId*/) {
            this.name = name;
            this.iconEnabledResourceId = iconEnabledResourceId;
            this.iconDisabledResourceId = iconDisabledResourceId;
//            this.sharedPrefId = sharedPrefId;
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

//        public int getSharedPrefId() {
//            return sharedPrefId;
//        }

        @Override
        public int getConfigType() {
            return ConfigRecyclerViewAdapter.TYPE_NIGHT_VISION_CONFIG;
        }
    }
}
