package pro.watchkit.wearable.watchface.watchface;

import pro.watchkit.wearable.watchface.model.WatchFacePreset.TickLength;
import pro.watchkit.wearable.watchface.model.WatchFacePreset.TickRadiusPosition;
import pro.watchkit.wearable.watchface.model.WatchFacePreset.TickShape;
import pro.watchkit.wearable.watchface.model.WatchFacePreset.TickThickness;

final class WatchPartTicksSixtyDrawable extends WatchPartTicksRingsDrawable {
    @Override
    protected boolean isVisible(int tickIndex) {
        if (tickIndex % 15 == 0)
            return false;
        else if (tickIndex % 5 == 0)
            return false;
        else
            return mWatchFaceState.getWatchFacePreset().isTwelveTicksVisible();
    }

    @Override
    protected float getMod() {
        return (float) Math.sqrt(0.5d);
    }

    @Override
    protected TickShape getTickShape() {
        return mWatchFaceState.getWatchFacePreset().getSixtyTickShape();
    }

    @Override
    protected TickLength getTickLength() {
        return mWatchFaceState.getWatchFacePreset().getSixtyTickLength();
    }

    @Override
    protected TickThickness getTickThickness() {
        return mWatchFaceState.getWatchFacePreset().getSixtyTickThickness();
    }

    @Override
    protected TickRadiusPosition getTickRadiusPosition() {
        return mWatchFaceState.getWatchFacePreset().getSixtyTickRadiusPosition();
    }
}
