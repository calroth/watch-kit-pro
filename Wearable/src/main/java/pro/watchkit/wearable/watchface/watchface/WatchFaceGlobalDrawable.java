package pro.watchkit.wearable.watchface.watchface;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;

import java.util.GregorianCalendar;
import java.util.TimeZone;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import pro.watchkit.wearable.watchface.model.PaintBox;
import pro.watchkit.wearable.watchface.model.WatchFacePreset;

/**
 * A very basic Drawable that you feed a WatchFacePreset and a PaintBox and it
 * draws a watch face!
 */
public class WatchFaceGlobalDrawable extends Drawable {
    private WatchFaceDrawable[] mWatchFaceDrawables = new WatchFaceDrawable[]{
            new WatchFaceBackgroundDrawable(),
            new WatchFaceTicksRingsDrawable(),
//            new WatchFaceComplicationsDrawable(),
            new WatchFaceHandsDrawable()/*,
            new WatchFaceStatsDrawable()*/
    };

    private WatchFaceDrawable.StateObject mStateObject;
    private GregorianCalendar mCalendar = new GregorianCalendar();

    public WatchFaceGlobalDrawable(WatchFacePreset watchFacePreset, PaintBox paintBox) {
        mStateObject = mWatchFaceDrawables[0].new StateObject();
        mStateObject.preset = watchFacePreset;
        mStateObject.paintBox = paintBox;
        mStateObject.unreadNotifications = 0;
        mStateObject.totalNotifications = 0;
        mStateObject.ambient = false;

        for (WatchFaceDrawable d : mWatchFaceDrawables) {
            d.setState(mStateObject, mCalendar, null);
        }
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        // Set the current date and time.
        mCalendar.setTimeZone(TimeZone.getDefault());
        mCalendar.setTimeInMillis(System.currentTimeMillis());
        for (WatchFaceDrawable d : mWatchFaceDrawables) {
            // For each of our drawables: draw it!
            d.draw(canvas);
        }
    }

    @Override
    public void setAlpha(int alpha) {
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
    }

    @Override
    public int getOpacity() {
        return PixelFormat.OPAQUE;
    }
}