package pro.watchkit.wearable.watchface.watchface;

import pro.watchkit.wearable.watchface.model.BytePackable.Style;
import pro.watchkit.wearable.watchface.model.BytePackable.TickLength;
import pro.watchkit.wearable.watchface.model.BytePackable.TickRadiusPosition;
import pro.watchkit.wearable.watchface.model.BytePackable.TickShape;
import pro.watchkit.wearable.watchface.model.BytePackable.TickThickness;

final class WatchPartTicksSixtyDrawable extends WatchPartTicksDrawable {
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
    protected TickLength getTickLength() {
        return mWatchFaceState.getSixtyTickLength();
    }

    @Override
    protected TickThickness getTickThickness() {
        return mWatchFaceState.getSixtyTickThickness();
    }

    @Override
    protected TickRadiusPosition getTickRadiusPosition() {
        return mWatchFaceState.getSixtyTickRadiusPosition();
    }

    @Override
    protected Style getTickStyle() {
        return mWatchFaceState.getSixtyTickStyle();
    }
}
