package pro.watchkit.wearable.watchface.watchface;

import androidx.annotation.StringRes;

import pro.watchkit.wearable.watchface.R;

public final class ProWatchFaceServiceA extends ProWatchFaceService {
    @Override
    String getDefaultWatchFaceStateString() {
        return "fcd81c000c0100000006c06a60000001~3cda1cc0000000000000000000000001";
    }

    @Override
    @StringRes
    int getPrefStringResId() {
        return R.string.watch_kit_pro_a_preference_file_key;
    }
}
