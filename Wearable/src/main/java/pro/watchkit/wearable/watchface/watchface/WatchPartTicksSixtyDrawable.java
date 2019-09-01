package pro.watchkit.wearable.watchface.watchface;

import androidx.annotation.NonNull;

import pro.watchkit.wearable.watchface.model.BytePackable.Style;
import pro.watchkit.wearable.watchface.model.BytePackable.TickShape;
import pro.watchkit.wearable.watchface.model.BytePackable.TickSize;

final class WatchPartTicksSixtyDrawable extends WatchPartTicksDrawable {
    @NonNull
    @Override
    String getStatsName() {
        return "Sixty";
    }

    @Override
    protected boolean isVisible(int tickIndex) {
        if (tickIndex % 15 == 0)
            return false;
        else if (tickIndex % 5 == 0)
            return false;
        else
            return mWatchFaceState.isSixtyTicksVisible();
    }

    @Override
    protected float getMod() {
        return (float) Math.sqrt(0.5d);
    }

    @Override
    protected TickShape getTickShape() {
        return mWatchFaceState.getSixtyTickShape();
    }

    @Override
    protected TickSize getTickSize() {
        return mWatchFaceState.getSixtyTickSize();
    }

    @Override
    protected Style getTickStyle() {
        return mWatchFaceState.getSixtyTickStyle();
    }
}
