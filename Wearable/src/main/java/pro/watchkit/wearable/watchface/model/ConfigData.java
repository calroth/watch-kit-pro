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

    protected interface WatchFacePresetMutator {
        /**
         * For the given WatchFacePreset (which must be a clone, since we'll modify it in the
         * process) return a String array with each permutation.
         *
         * @param permutation WatchFacePreset, which must be a clone, since we'll modify it
         * @return String array with each permutation
         */
        String[] permute(WatchFacePreset permutation);

        /**
         * For the given WatchFacePreset (which is our current preference) return the current
         * value.
         *
         * @param currentPreset WatchFacePreset of our current preference
         * @return Value that it's currently set to
         */
        Enum getCurrentValue(WatchFacePreset currentPreset);
    }

    /**
     * Objects inherit this interface to determine the visibility of a ConfigItem.
     * That is, implement this interface and put in some custom logic that determines
     * whether an item is visible or not.
     */
    protected interface ConfigItemVisibilityCalculator {
        boolean isVisible(WatchFacePreset currentPreset);
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
        private WatchFacePreset.ColorType mColorType;
        private Class<ColorSelectionActivity> activityToChoosePreference;

        ColorPickerConfigItem(
                String name,
                int iconResourceId,
                WatchFacePreset.ColorType colorType,
                Class<ColorSelectionActivity> activity) {
            this.name = name;
            this.iconResourceId = iconResourceId;
            this.mColorType = colorType;
            this.activityToChoosePreference = activity;
        }

        public WatchFacePreset.ColorType getType() {
            return mColorType;
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
        //        private WatchFacePreset.ColorType mColorType;
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

    protected static class WatchFacePresetMutatorImpl<E extends Enum>
            implements WatchFacePresetMutator {
        /**
         * All the possible Enum values of E.
         */
        private E[] mValues;

        /**
         * A lambda which sets (or applies) setting E to the given WatchFacePreset.
         */
        private BiConsumer<WatchFacePreset, E> mSetter;

        /**
         * A lambda which gets and returns setting E from the given WatchFacePreset.
         */
        private Function<WatchFacePreset, E> mGetter;

        /**
         * Create the given WatchFacePresetMutatorImpl for the E of type Enum.
         *
         * @param values all possible enumeration values of E
         * @param setter a lambda which sets (or applies) setting E to the given WatchFacePreset
         * @param getter a lambda which gets and returns setting E from the given WatchFacePreset
         */
        WatchFacePresetMutatorImpl(E[] values,
                                   BiConsumer<WatchFacePreset, E> setter,
                                   Function<WatchFacePreset, E> getter) {
            mValues = values;
            mSetter = setter;
            mGetter = getter;
        }

        /**
         * For the given WatchFacePreset (which must be a clone, since we'll modify it in the
         * process) return a String array with each permutation.
         *
         * @param permutation WatchFacePreset, which must be a clone, since we'll modify it
         * @return String array with each permutation
         */
        public String[] permute(WatchFacePreset permutation) {
            return Arrays.stream(mValues).map(h -> {
                mSetter.accept(permutation, h);
                return permutation.getString();
            }).toArray(String[]::new);
        }

        /**
         * For the given WatchFacePreset (which is our current preference) return the current
         * value.
         *
         * @param currentPreset WatchFacePreset of our current preference
         * @return Value that it's currently set to
         */
        public E getCurrentValue(WatchFacePreset currentPreset) {
            return mGetter.apply(currentPreset);
        }
    }

    public static class WatchFacePickerConfigItem implements ConfigItemType {
        private String mName;
        private int mIconResourceId;
        private Class<WatchFaceSelectionActivity> mActivityToChoosePreference;
        private WatchFacePresetMutator mMutator;
        private ConfigItemVisibilityCalculator mConfigItemVisibilityCalculator;

        WatchFacePickerConfigItem(
                String name,
                int iconResourceId,
                Class<WatchFaceSelectionActivity> activity,
                WatchFacePresetMutator mutator) {
            this(name, iconResourceId, activity, mutator, null);
        }

        WatchFacePickerConfigItem(
                String name,
                int iconResourceId,
                Class<WatchFaceSelectionActivity> activity,
                WatchFacePresetMutator mutator,
                ConfigItemVisibilityCalculator configItemVisibilityCalculator) {
            mMutator = mutator;
            mName = name;
            mIconResourceId = iconResourceId;
            mActivityToChoosePreference = activity;
            mConfigItemVisibilityCalculator = configItemVisibilityCalculator;
        }

        public CharSequence getName(WatchFacePreset watchFacePreset, Context context) {
            Enum e = mMutator.getCurrentValue(watchFacePreset);

            if (e == null) {
                return mName;
            } else if (e instanceof WatchFacePreset.EnumResourceId) {
                WatchFacePreset.EnumResourceId f = (WatchFacePreset.EnumResourceId) e;
                return Html.fromHtml(mName + "<br/><small>" +
                        context.getResources().getStringArray(f.getNameResourceId())[e.ordinal()] +
                        "</small>", Html.FROM_HTML_MODE_LEGACY);
            } else {
                return Html.fromHtml(mName + "<br/><small>" +
                        e.getClass().getSimpleName() + " ~ " + e.name() +
                        "</small>", Html.FROM_HTML_MODE_LEGACY);
            }
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

        public String[] permute(WatchFacePreset watchFacePreset) {
            return mMutator.permute(watchFacePreset.clone());
        }

        public boolean isVisible(WatchFacePreset watchFacePreset) {
            return mConfigItemVisibilityCalculator == null ||
                    mConfigItemVisibilityCalculator.isVisible(watchFacePreset);
        }
    }

    /**
     * Data for Night Vision preference picker item in RecyclerView.
     */
    public static class WatchFacePresetToggleConfigItem implements ConfigItemType {

        private String name;
        private int iconEnabledResourceId;
        private int iconDisabledResourceId;
        private WatchFacePresetMutator mMutator;

        WatchFacePresetToggleConfigItem(
                String name,
                int iconEnabledResourceId,
                int iconDisabledResourceId,
                WatchFacePresetMutator mutator) {
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

        public String[] permute(WatchFacePreset watchFacePreset) {
            return mMutator.permute(watchFacePreset.clone());
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
