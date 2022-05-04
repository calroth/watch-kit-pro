# In pro.watchkit.wearable.watchface.config.ConfigActivity$ConfigSubActivity.getNewInstance()
# We do a "mClass.newInstance()".
# Make sure all those ConfigData classes don't get pruned out.
-keepclassmembers class * extends pro.watchkit.wearable.watchface.model.ConfigData
