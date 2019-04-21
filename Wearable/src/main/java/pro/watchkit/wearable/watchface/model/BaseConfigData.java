package pro.watchkit.wearable.watchface.model;

import android.content.Context;
import android.text.Html;

import java.util.ArrayList;

import androidx.recyclerview.widget.RecyclerView;
import pro.watchkit.wearable.watchface.config.AnalogComplicationConfigActivity;
import pro.watchkit.wearable.watchface.config.AnalogComplicationConfigRecyclerViewAdapter;
import pro.watchkit.wearable.watchface.config.ColorSelectionActivity;
import pro.watchkit.wearable.watchface.config.WatchFacePresetSelectionActivity;

abstract public class BaseConfigData {

    abstract public Class getWatchFaceServiceClass();

    /**
     * Includes all data to populate each of the 5 different custom
     * {@link RecyclerView.ViewHolder} types in {@link AnalogComplicationConfigRecyclerViewAdapter}.
     */
    abstract public ArrayList<ConfigItemType> getDataToPopulateAdapter(Context context);

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
     * Data for Watch Face Preview with Complications Preview item in RecyclerView.
     */
    public static class PreviewAndComplicationsConfigItem implements ConfigItemType {

        private int defaultComplicationResourceId;

        PreviewAndComplicationsConfigItem(int defaultComplicationResourceId) {
            this.defaultComplicationResourceId = defaultComplicationResourceId;
        }

        public int getDefaultComplicationResourceId() {
            return defaultComplicationResourceId;
        }

        @Override
        public int getConfigType() {
            return AnalogComplicationConfigRecyclerViewAdapter.TYPE_PREVIEW_AND_COMPLICATIONS_CONFIG;
        }
    }

    /**
     * Data for "more options" item in RecyclerView.
     */
    public static class MoreOptionsConfigItem implements ConfigItemType {

        private int iconResourceId;

        MoreOptionsConfigItem(int iconResourceId) {
            this.iconResourceId = iconResourceId;
        }

        public int getIconResourceId() {
            return iconResourceId;
        }

        @Override
        public int getConfigType() {
            return AnalogComplicationConfigRecyclerViewAdapter.TYPE_MORE_OPTIONS;
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
            return AnalogComplicationConfigRecyclerViewAdapter.TYPE_COLOR_PICKER_CONFIG;
        }
    }

    /**
     * Data for another config (sub-) activity in RecyclerView.
     */
    public static class ConfigActivityConfigItem implements ConfigItemType {

        private String name;
        private int iconResourceId;
        //        private WatchFacePreset.ColorType mColorType;
        private Class<?> mConfigDataClass;
        private Class<AnalogComplicationConfigActivity> activityToChoosePreference;

        ConfigActivityConfigItem(
                String name,
                int iconResourceId,
                Class<?> configDataClass,
                Class<AnalogComplicationConfigActivity> activity) {
            this.name = name;
            this.iconResourceId = iconResourceId;
            mConfigDataClass = configDataClass;
            this.activityToChoosePreference = activity;
        }

        public Class<?> getConfigDataClass() {
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

        public Class<AnalogComplicationConfigActivity> getActivityToChoosePreference() {
            return activityToChoosePreference;
        }

        @Override
        public int getConfigType() {
            return AnalogComplicationConfigRecyclerViewAdapter.TYPE_CONFIG_ACTIVITY_CONFIG;
        }
    }

    protected abstract static class WatchFacePresetMutatorGeneric<E extends Enum>
            implements WatchFacePresetMutator {
        private E[] mValues;

        WatchFacePresetMutatorGeneric(E[] values) {
            mValues = values;
        }

        /**
         * For the given WatchFacePreset (which must be a clone, since we'll modify it in the
         * process) return a String array with each permutation.
         *
         * @param permutation WatchFacePreset, which must be a clone, since we'll modify it
         * @return String array with each permutation
         */
        public String[] permute(WatchFacePreset permutation) {
            String[] result = new String[mValues.length];
            int i = 0;
            for (E h : mValues) {
                permuteOne(permutation, h);
                result[i++] = permutation.getString();
            }
            return result;
        }

        abstract void permuteOne(WatchFacePreset permutation, E h);

        /**
         * For the given WatchFacePreset (which is our current preference) return the current
         * value.
         *
         * @param currentPreset WatchFacePreset of our current preference
         * @return Value that it's currently set to
         */
        public abstract E getCurrentValue(WatchFacePreset currentPreset);
    }

    public static class WatchFacePresetPickerConfigItem implements ConfigItemType {
        private String mName;
        private int mIconResourceId;
        private Class<WatchFacePresetSelectionActivity> mActivityToChoosePreference;
        private WatchFacePresetMutator mMutator;
        private ConfigItemVisibilityCalculator mConfigItemVisibilityCalculator;

        WatchFacePresetPickerConfigItem(
                String name,
                int iconResourceId,
                Class<WatchFacePresetSelectionActivity> activity,
                WatchFacePresetMutator mutator) {
            this(name, iconResourceId, activity, mutator, null);
        }

        WatchFacePresetPickerConfigItem(
                String name,
                int iconResourceId,
                Class<WatchFacePresetSelectionActivity> activity,
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

        public Class<WatchFacePresetSelectionActivity> getActivityToChoosePreference() {
            return mActivityToChoosePreference;
        }

        @Override
        public int getConfigType() {
            return AnalogComplicationConfigRecyclerViewAdapter.TYPE_WATCH_FACE_PRESET_PICKER_CONFIG;
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
            return AnalogComplicationConfigRecyclerViewAdapter.TYPE_WATCH_FACE_PRESET_TOGGLE_CONFIG;
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
        private int sharedPrefId;

        UnreadNotificationConfigItem(
                String name,
                int iconEnabledResourceId,
                int iconDisabledResourceId,
                int sharedPrefId) {
            this.name = name;
            this.iconEnabledResourceId = iconEnabledResourceId;
            this.iconDisabledResourceId = iconDisabledResourceId;
            this.sharedPrefId = sharedPrefId;
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

        public int getSharedPrefId() {
            return sharedPrefId;
        }

        @Override
        public int getConfigType() {
            return AnalogComplicationConfigRecyclerViewAdapter.TYPE_UNREAD_NOTIFICATION_CONFIG;
        }
    }

    /**
     * Data for background image complication picker item in RecyclerView.
     */
    public static class BackgroundComplicationConfigItem implements ConfigItemType {

        private String name;
        private int iconResourceId;

        BackgroundComplicationConfigItem(
                String name,
                int iconResourceId) {

            this.name = name;
            this.iconResourceId = iconResourceId;
        }

        public String getName() {
            return name;
        }

        public int getIconResourceId() {
            return iconResourceId;
        }

        @Override
        public int getConfigType() {
            return AnalogComplicationConfigRecyclerViewAdapter.TYPE_BACKGROUND_COMPLICATION_IMAGE_CONFIG;
        }
    }

    /**
     * Data for Night Vision preference picker item in RecyclerView.
     */
    public static class NightVisionConfigItem implements ConfigItemType {

        private String name;
        private int iconEnabledResourceId;
        private int iconDisabledResourceId;
        private int sharedPrefId;

        NightVisionConfigItem(
                String name,
                int iconEnabledResourceId,
                int iconDisabledResourceId,
                int sharedPrefId) {
            this.name = name;
            this.iconEnabledResourceId = iconEnabledResourceId;
            this.iconDisabledResourceId = iconDisabledResourceId;
            this.sharedPrefId = sharedPrefId;
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

        public int getSharedPrefId() {
            return sharedPrefId;
        }

        @Override
        public int getConfigType() {
            return AnalogComplicationConfigRecyclerViewAdapter.TYPE_NIGHT_VISION_CONFIG;
        }
    }
}
