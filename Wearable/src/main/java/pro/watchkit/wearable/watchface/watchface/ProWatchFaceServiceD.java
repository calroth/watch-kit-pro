package pro.watchkit.wearable.watchface.watchface;

import androidx.annotation.StringRes;

import pro.watchkit.wearable.watchface.R;

public final class ProWatchFaceServiceD extends ProWatchFaceService {
    @Override
    String getDefaultWatchFaceStateString() {
        return "f4d81c000c0100030c04c1c260034801~2cda1ddb200000000000000000000001";
    }

    @Override
    @StringRes
    int getPrefStringResId() {
        return R.string.watch_kit_pro_d_preference_file_key;
    }
}
