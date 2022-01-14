/*
 * Copyright (C) 2018-2022 Terence Tan
 *
 *  This file is free software: you may copy, redistribute and/or modify it
 *  under the terms of the GNU General Public License as published by the
 *  Free Software Foundation, either version 3 of the License, or (at your
 *  option) any later version.
 *
 *  This file is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package pro.watchkit.wearable.watchface.watchface;

import android.graphics.Canvas;
import android.support.wearable.watchface.decomposition.WatchFaceDecomposition;

import androidx.annotation.NonNull;

import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

import pro.watchkit.wearable.watchface.model.ComplicationHolder;

final class WatchPartComplicationsDrawable extends WatchPartDrawable
        implements WatchFaceGlobalDrawable.WatchFaceDecompositionComponent {
    @NonNull
    @Override
    String getStatsName() {
        return "Comps";
    }

    @Override
    public void draw2(@NonNull Canvas canvas) {
        mWatchFaceState.getComplicationsForDrawing(getBounds())
                .forEach(c -> c.draw(canvas, mWatchFaceState.getTimeInMillis()));
    }

    /**
     * Build this watch face decomposition component into "builder".
     *
     * @param builder WatchFaceDecomposition builder to build into.
     * @param idA     AtomicInteger for the component ID, which we will increment
     * @return The time at which this component expires, at which point (or before),
     * call this again
     */
    @Override
    public long buildWatchFaceDecompositionComponents(
            @NonNull WatchFaceDecomposition.Builder builder, @NonNull AtomicInteger idA) {
        long currentTimeMillis = mWatchFaceState.getTimeInMillis();
        long maxTimeMillis;

        // Calculate "maxTimeMillis", the time in the future to which we'll be pre-rendering.

        // Here's how it works:
        // We have a budget, of 15 or 30 or however many frames.
        // It's defined in ComplicationHolder.TIME_DEPENDENT_STRIP_BUDGET.
        // We want to fill this budget with frames from all our different complications.
        // It's ordered by frame time. So we might get 10 frames from complication A,
        // 30 frames from complication B (it updates a lot), and 15 frames from complication C.
        // So 55 total. We take the 30 (or however many) earliest ones.

        // Why do we need a budget? Because memory on the offload processor is scarce!
        // We've only got room to send a certain number of frames.
        // We dynamically calculate how many we can send, that will last longest between updates.

        TreeSet<ComplicationFrame> frames = new TreeSet<>();

        // Fill "frames" with every frame in every time-dependent complication.
        mWatchFaceState.getComplicationsForDrawing(getBounds())
                .stream().filter(c1 -> c1.isForeground && c1.isTimeDependent())
                .forEach(c1 -> {
                    long frameTime0 = c1.getTimeDependentFrameTime0(currentTimeMillis);
                    long msPerIncrement = c1.getTimeDependentMsBetweenIncrements(currentTimeMillis);
                    int maxFrames = c1.getTimeDependentStripSize(currentTimeMillis);
                    for (int i = 0; i < maxFrames; i++) {
                        frames.add(new ComplicationFrame(
                                frameTime0, i, msPerIncrement, c1));
                    }
                });

        if (frames.isEmpty()) {
            // No frames? No worries. Return an update time at the end of time.
            maxTimeMillis = Long.MAX_VALUE;
        } else {
            // Get a "budget" collection -- if we've got more than TIME_DEPENDENT_STRIP_BUDGET
            // frames, use that as the limit; otherwise the entire lot.
            Optional<ComplicationFrame> firstDiscardedFrame = frames.stream()
                    .skip(ComplicationHolder.TIME_DEPENDENT_STRIP_BUDGET)
                    .findFirst();
            SortedSet<ComplicationFrame> budget;
            if (firstDiscardedFrame.isPresent()) {
                budget = frames.headSet(firstDiscardedFrame.get());
                if (firstDiscardedFrame.get().mFrameTime == budget.last().mFrameTime) {
                    // If there's more than one frame straddling TIME_DEPENDENT_STRIP_BUDGET
                    // with the same frame time, it's possible that some frames might get the
                    // update but the others won't. In that case, start walking back the frame
                    // array until we find an earlier frame.
                    ComplicationFrame oldLastFrame, newLastFrame;
                    //noinspection ConstantConditions
                    do {
                        oldLastFrame = budget.last();
                        // Trim "oldLastFrame" off the end of "budget".
                        budget = budget.headSet(oldLastFrame);
                        newLastFrame = budget.last();
                        // Now, does the new last frame have a different frame time?
                        // If so, stop trimming. If not, keep trimming...
                    } while (!budget.isEmpty() && oldLastFrame.mFrameTime == newLastFrame.mFrameTime);
                }
            } else {
                // No first discarded frame! Just use all our frames.
                budget = frames;
            }

            // OK, our update time is the frame time of the last frame in the budget.
            maxTimeMillis = budget.last().mFrameTime;
        }

        mWatchFaceState.getComplicationsForDrawing(getBounds())
                .stream().filter(c -> c.isForeground && c.isTimeDependent())
                .forEach(c -> {
                    // Terence temporary hack
                    c.buildTimeDependentDecomposableComplication(
                            builder, getBounds(), currentTimeMillis, maxTimeMillis,
                            idA, mWatchFaceState.getPaintBox(), mWatchFaceState.getAmbientTint()/*, mWatchFaceState.getContextDebugTerenceDeleteMe()*/);
                });
        return maxTimeMillis;
    }

    /**
     * Does this WatchFaceDecomposition component have an update available? We ask because if
     * there are no updates available, we want to avoid sending updates to the offload
     * processor.
     *
     * @param currentTimeMillis The time when we're asking if there's an update available
     * @return Whether the update is available?
     */
    @Override
    public boolean hasDecompositionUpdateAvailable(long currentTimeMillis) {
        // Are there any updated time-dependent complications?
        return mWatchFaceState.getComplications().stream()
                .filter(c -> c.isForeground && c.isTimeDependent())
                .map(c -> c.checkUpdatedComplicationData(currentTimeMillis))
                .reduce(false, (a, b) -> a || b);
    }

    /**
     * Represents a single frame in a strip in a time-dependent complication.
     * Used for calculating budgets.
     */
    private static class ComplicationFrame implements Comparable<ComplicationFrame> {
        public long mFrameTime;
        public long mFrameTimeNext;
        public ComplicationHolder mComplicationHolder;

        public ComplicationFrame(
                long frameTime0, int frameNumber, long msPerIncrement,
                ComplicationHolder complicationHolder) {
            mFrameTime = frameTime0 + frameNumber * msPerIncrement;
            mFrameTimeNext = frameTime0 + (frameNumber + 1) * msPerIncrement;
            mComplicationHolder = complicationHolder;
        }

        @Override
        public int compareTo(ComplicationFrame o) {
            if (mFrameTime != o.mFrameTime) {
                return Long.compare(mFrameTime, o.mFrameTime);
            } else {
                return Integer.compare(mComplicationHolder.getId(), o.mComplicationHolder.getId());
            }
        }
    }
}
