package pro.watchkit.wearable.watchface.watchface;

import pro.watchkit.wearable.watchface.model.WatchFacePreset.Style;
import pro.watchkit.wearable.watchface.model.WatchFacePreset.TickLength;
import pro.watchkit.wearable.watchface.model.WatchFacePreset.TickRadiusPosition;
import pro.watchkit.wearable.watchface.model.WatchFacePreset.TickShape;
import pro.watchkit.wearable.watchface.model.WatchFacePreset.TickThickness;

final class WatchPartTicksFourDrawable extends WatchPartTicksDrawable {
    @Override
    protected boolean isVisible(int tickIndex) {
        if (tickIndex % 15 == 0)
            return mWatchFaceState.getWatchFacePreset().isFourTicksVisible();
        else
            return false;
    }

    @Override
    protected float getMod() {
        return (float) Math.sqrt(2d);
    }

    @Override
    protected TickShape getTickShape() {
        return mWatchFaceState.getWatchFacePreset().getFourTickShape();
    }

    @Override
    protected TickLength getTickLength() {
        return mWatchFaceState.getWatchFacePreset().getFourTickLength();
    }

    @Override
    protected TickThickness getTickThickness() {
        return mWatchFaceState.getWatchFacePreset().getFourTickThickness();
    }

    @Override
    protected TickRadiusPosition getTickRadiusPosition() {
        return mWatchFaceState.getWatchFacePreset().getFourTickRadiusPosition();
    }

    @Override
    protected Style getTickStyle() {
        return mWatchFaceState.getWatchFacePreset().getFourTickStyle();
    }
}
