package pro.watchkit.wearable.watchface.watchface;

import pro.watchkit.wearable.watchface.model.BytePackable.Style;
import pro.watchkit.wearable.watchface.model.BytePackable.TickLength;
import pro.watchkit.wearable.watchface.model.BytePackable.TickShape;
import pro.watchkit.wearable.watchface.model.BytePackable.TickThickness;

final class WatchPartTicksTwelveDrawable extends WatchPartTicksDrawable {
    @Override
    String getStatsName() {
        return "Twelve";
    }

    @Override
    protected boolean isVisible(int tickIndex) {
        if (tickIndex % 15 == 0)
            return false;
        else if (tickIndex % 5 == 0)
            return mWatchFaceState.isTwelveTicksVisible();
        else
            return false;
    }

    @Override
    protected float getMod() {
        return 1f;
    }

    @Override
    protected TickShape getTickShape() {
        return mWatchFaceState.getTwelveTickShape();
    }

    @Override
    protected TickLength getTickLength() {
        return mWatchFaceState.getTwelveTickLength();
    }

    @Override
    protected TickThickness getTickThickness() {
        return mWatchFaceState.getTwelveTickThickness();
    }

    @Override
    protected Style getTickStyle() {
        return mWatchFaceState.getTwelveTickStyle();
    }
}
