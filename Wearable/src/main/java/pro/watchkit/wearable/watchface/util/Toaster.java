/*
 * Copyright (C) 2019 Terence Tan
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

import android.content.Context;
import android.widget.Toast;

/**
 * A toaster is a household appliance that makes toast.
 * <p>
 * In the same way, this class makes Toast too.
 * <p>
 * Toaster is a singleton class factory for Toast objects so we only have one global Toast showing
 * at a time.
 */
public final class Toaster {
    /**
     * Show the Toast for a short period of time. Defined to be Toast.LENGTH_SHORT.
     */
    public static final int LENGTH_SHORT = Toast.LENGTH_SHORT;

    /**
     * Show the Toast for a long period of time. Defined to be Toast.LENGTH_LONG.
     */
    public static final int LENGTH_LONG = Toast.LENGTH_LONG;

    /**
     * Our current toast if available.
     */
    private static Toast mCurrentToast;

    /**
     * Make a standard Toast with the given text value, and pop it up. Replaces any existing Toast.
     */
    public static void makeText(Context context, CharSequence text, int duration) {
        cancelCurrent();

        mCurrentToast = Toast.makeText(context, text, duration);
        mCurrentToast.show();
    }

    /**
     * Cancel the current toast message if one is showing.
     */
    public static void cancelCurrent() {
        if (mCurrentToast != null) {
            // Cancel the current toast if showing.
            mCurrentToast.cancel();
        }
    }
}
