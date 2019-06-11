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
import android.graphics.Paint;
import android.graphics.Path;

import androidx.annotation.NonNull;

final class WatchPartNotificationsDrawable extends WatchPartDrawable {
    private Path mPath = new Path();
    private Path mExclusion = new Path();

    @Override
    String getStatsName() {
        return "Note";
    }

    @Override
    public void draw2(@NonNull Canvas canvas) {
        int unreadNotifications = mWatchFaceState.getUnreadNotifications();
        int totalNotifications = mWatchFaceState.getTotalNotifications();

        if (totalNotifications == 0) {
            // Don't need to continue if we don't need to!
            return;
        }

        mPath.reset();
        mExclusion.reset();

        float mCenter = Math.min(mCenterX, mCenterY);
        float tickRadiusPositionDimen = 6f * pc; // Draw at 6% from edge
        float centerTickRadius = mCenter - tickRadiusPositionDimen;
        float x = mCenterX;
        float y = mCenterY + centerTickRadius;

        // Draw a circle of size 4%.
        mPath.addCircle(x, y, 4f * pc, getDirection());
        if (!mWatchFaceState.isAmbient()) {
            // Punch a hole in the circle to make it a donut of size 3%.
            Path p2 = new Path();
            p2.addCircle(x, y, 3f * pc, getDirection());
            mPath.op(p2, Path.Op.DIFFERENCE);
        }
        if (unreadNotifications > 0) {
            // Extra circle for unread notifications of size 2%.
            mPath.addCircle(x, y, 2f * pc, getDirection());
        }

        // Draw with our four tick style.
        // Maybe in the future, allow this to be customised.
        Paint paint = mWatchFaceState.getPaintBox().getPaintFromPreset(
                mWatchFaceState.getFourTickStyle());
        drawPath(canvas, mPath, paint);

        // Add an exclusion zone of size 5%.
        mExclusion.addCircle(x, y, 5f * pc, getDirection());
        addExclusionPath(mExclusion, Path.Op.DIFFERENCE);
    }
}
