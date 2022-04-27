/*
 * Copyright (C) 2022 Terence Tan
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

package pro.watchkit.wearable.watchface.util;

import android.util.Log;

import pro.watchkit.wearable.watchface.BuildConfig;

/**
 * Utility class for timing.
 */
public final class DebugTiming {

    /**
     * Our StringBuilder for logging things to.
     */
    private static StringBuilder sb;

    /**
     * The tag to log to.
     */
    private static String mTag = null;

    /**
     * The first timing point in nanoseconds.
     */
    private static long mStartTime;

    /**
     * The most recent timing point in nanoseconds.
     */
    private static long mTime;

    /**
     * start logging to the given tag. Starts the timer.
     *
     * @param tag Used to identify the source of a log message.
     */
    public static void start(String tag) {
        if (BuildConfig.DEBUG) {
            mTime = System.nanoTime();
            mStartTime = mTime;
            mTag = tag;
            if (sb == null) {
                sb = new StringBuilder();
            }
            sb.setLength(0);
        }
    }

    /**
     * Continue logging to the given tag. Resets the timer.
     *
     * @param label Name of the checkpoint we've reached.
     */
    public static void checkpoint(String label) {
        if (BuildConfig.DEBUG) {
            if (mTag == null) {
                start("DebugTiming");
            }

            long nowTime = System.nanoTime();
            sb.append(" ~ ").append(label).append(": ").append((nowTime - mTime) / 1000000f);
            mTime = nowTime;
        }
    }

    /**
     * Finish logging. Write out what we have.
     */
    public static void endAndWrite() {
        if (BuildConfig.DEBUG) {
            sb.insert(0, (mTime - mStartTime) / 1000000f).insert(0, "TOTAL: ");
            Log.d(mTag, sb.toString());
            mTag = null;
        }
    }
}
