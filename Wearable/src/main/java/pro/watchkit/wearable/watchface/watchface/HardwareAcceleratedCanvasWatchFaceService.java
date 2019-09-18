/*
 * Copyright (C) 2018-2019 Terence Tan
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
 * This file incorporates work covered by the following copyright and
 * permission notice:
 *
 *     Copyright (C) 2017 The Android Open Source Project
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */

package pro.watchkit.wearable.watchface.watchface;

import android.graphics.Canvas;
import android.os.Build;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.view.Choreographer;
import android.view.SurfaceHolder;

import androidx.annotation.NonNull;

/**
 * A hardware-accelerated implementation of CanvasWatchFaceService. Extend this class instead
 * of CanvasWatchFaceService, if you want hardware acceleration. Can be toggled at compile-time.
 */
abstract class HardwareAcceleratedCanvasWatchFaceService extends CanvasWatchFaceService {
    public class Engine extends CanvasWatchFaceService.Engine
            implements Choreographer.FrameCallback {
//        Engine(boolean hardwareAccelerationEnabled) {
//            super();
//            mHardwareAccelerationEnabled = hardwareAccelerationEnabled;
//        }

//        Engine() {
//            super();
//        }

        private boolean mHardwareAccelerationEnabled = true;

        void setHardwareAccelerationEnabled(boolean hardwareAccelerationEnabled) {
            mHardwareAccelerationEnabled = hardwareAccelerationEnabled;
        }

        // Our own private Choreographer which we can use to call hardware render.
        private Choreographer mChoreographer = Choreographer.getInstance();
        // Track whether we've been invalidated; don't need to draw if we haven't.
        private int mInvalidated = 0;

//        private final Choreographer.FrameCallback mChoreographerFrameCallback =
//                new Choreographer.FrameCallback() {
//                    // We create our own Choreographer to intercept calls to "draw" in
//                    // CanvasWatchFaceService.Engine. If we don't (i.e. by default)
//                    // calls to CanvasWatchFaceService.Engine perform a software render.
//                    // If we want to hardware render, we wedge ourselves in here...
//                    @Override
//                    public void doFrame(long frameTimeNanos) {
//                        while (mInvalidated > 0) {
//                            beforeDoFrame(mInvalidated);
//                            drawHardwareAccelerated(getSurfaceHolder());
//                            mInvalidated--;
//                        }
//                        afterDoFrame(mInvalidated);
//                    }
//                };

        /**
         * Implement Choreographer.FrameCallback.
         * We create our own Choreographer to intercept calls to "draw" in
         * CanvasWatchFaceService.Engine. If we don't (i.e. by default)
         * calls to CanvasWatchFaceService.Engine perform a software render.
         * If we want to hardware render, we wedge ourselves in here...
         *
         * @param frameTimeNanos The time in nanoseconds when the frame started being rendered.
         */
        @Override
        public void doFrame(long frameTimeNanos) {
//            while (mInvalidated > 0) {
            beforeDoFrame(mInvalidated);
            drawHardwareAccelerated(getSurfaceHolder());
            mInvalidated--;
//            }
            afterDoFrame(mInvalidated);
        }

        protected void beforeDoFrame(int invalidated) {
        }

        protected void afterDoFrame(int invalidated) {
        }

        @Override
        public void onSurfaceRedrawNeeded(@NonNull SurfaceHolder holder) {
            // We override this method to intercept calls to "draw" in
            // CanvasWatchFaceService.Engine. Those calls do a software render.
            // If we want to do hardware render, we wedge ourselves in here...
            if (!mHardwareAccelerationEnabled) {
                // Software render path
                super.onSurfaceRedrawNeeded(holder);
            } else {
                // Hardware render path
                drawHardwareAccelerated(holder);
            }
        }

        @Override
        public void invalidate() {
            // We override this method to intercept calls to "draw" in
            // CanvasWatchFaceService.Engine. Those calls do a software render.

            // If we want to do hardware render, we override use of the Choreographer
            // in CanvasWatchFaceService.Engine (which calls the software render),
            // and instead call our own Choreographer (which calls the hardware render).
            if (!mHardwareAccelerationEnabled) {
                // Software render path
                super.invalidate();
            } else if (mInvalidated < 5) {
                // Hardware render path
                // Only if already hasn't been invalidated--ignore multiple calls.
                // Allows 1 extra call, to cater for calling this during an existing
                // long-running call, to get one more redraw after it's done.
                mInvalidated++;
//                mChoreographer.postFrameCallback(mChoreographerFrameCallback);
                mChoreographer.postFrameCallback(this);
            }
        }

        private void drawHardwareAccelerated(@NonNull SurfaceHolder holder) {
            Canvas canvas = null;
            if (holder.getSurface() != null && holder.getSurface().isValid()) {
                canvas = Build.VERSION.SDK_INT >= 26
                        ? holder.lockHardwareCanvas() : holder.lockCanvas();
            }
            if (canvas != null) {
                try {
                    onDraw(canvas, holder.getSurfaceFrame());
                } finally {
                    holder.unlockCanvasAndPost(canvas);
                }
            }
        }
    }
}
