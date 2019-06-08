package pro.watchkit.wearable.watchface.watchface;

import pro.watchkit.wearable.watchface.model.BytePackable.Style;
import pro.watchkit.wearable.watchface.model.BytePackable.TickLength;
import pro.watchkit.wearable.watchface.model.BytePackable.TickRadiusPosition;
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
            return mWatchFaceState.getWatchFacePreset().isTwelveTicksVisible();
        else
            return false;
    }

    @Override
    protected float getMod() {
        return 1f;
    }

    @Override
    protected TickShape getTickShape() {
        return mWatchFaceState.getWatchFacePreset().getTwelveTickShape();
    }

    @Override
    protected TickLength getTickLength() {
        return mWatchFaceState.getWatchFacePreset().getTwelveTickLength();
    }

    @Override
    protected TickThickness getTickThickness() {
        return mWatchFaceState.getWatchFacePreset().getTwelveTickThickness();
    }

    @Override
    protected TickRadiusPosition getTickRadiusPosition() {
        return mWatchFaceState.getWatchFacePreset().getTwelveTickRadiusPosition();
    }

    @Override
    protected Style getTickStyle() {
        return mWatchFaceState.getWatchFacePreset().getTwelveTickStyle();
    }
}
