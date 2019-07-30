package pro.watchkit.wearable.watchface.watchface;

import pro.watchkit.wearable.watchface.model.BytePackable.Style;
import pro.watchkit.wearable.watchface.model.BytePackable.TickShape;
import pro.watchkit.wearable.watchface.model.BytePackable.TickSize;

final class WatchPartTicksFourDrawable extends WatchPartTicksDrawable {
    @Override
    String getStatsName() {
        return "Four";
    }

    @Override
    protected boolean isVisible(int tickIndex) {
        if (tickIndex % 15 == 0)
            return mWatchFaceState.isFourTicksVisible();
        else
            return false;
    }

    @Override
    protected float getMod() {
        return (float) Math.sqrt(2d);
    }

    @Override
    protected TickShape getTickShape() {
        return mWatchFaceState.getFourTickShape();
    }

    @Override
    protected TickSize getTickSize() {
        return mWatchFaceState.getFourTickSize();
    }

    @Override
    protected Style getTickStyle() {
        return mWatchFaceState.getFourTickStyle();
    }
}
