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

package pro.watchkit.wearable.watchface.model;

import android.util.Log;

/**
 * Check yuor settings.
 */
public class Settings {
    private BytePacker bytePacker = new BytePacker();
    private boolean mShowUnreadNotifications;
    private boolean mEnableNightVisionMode;

    public String getString() {
        packVersion0();
        return bytePacker.getStringFast();
    }

    public void setString(String s) {
        if (s == null || s == "") return;
        try {
            bytePacker.setStringFast(s);
            unpack();
        } catch (java.lang.StringIndexOutOfBoundsException e) {
            Log.d("AnalogWatchFace", "It failed: " + s);
            Log.d("AnalogWatchFace", "It failed: " + e.toString());
        }
    }

    private void packVersion0() {
        bytePacker.rewind();

        // Version. 3-bits. Current version is v0.
        bytePacker.put(3, 0);

        bytePacker.put(mShowUnreadNotifications);
        bytePacker.put(mEnableNightVisionMode);

        bytePacker.finish();
    }

    private void unpack() {
        bytePacker.rewind();

        int version = bytePacker.get(3);
        switch (version) {
            case 0:
            default: {
                mShowUnreadNotifications = bytePacker.getBoolean();
                mEnableNightVisionMode = bytePacker.getBoolean();
                break;
            }
        }
    }

    public boolean isShowUnreadNotifications() {
        return mShowUnreadNotifications;
    }

    public boolean toggleShowUnreadNotifications() {
        mShowUnreadNotifications = !mShowUnreadNotifications;
        return mShowUnreadNotifications;
    }

    public boolean isEnableNightVisionMode() {
        return mEnableNightVisionMode;
    }

    public boolean toggleEnableNightVisionMode() {
        mEnableNightVisionMode = !mEnableNightVisionMode;
        return mEnableNightVisionMode;
    }
}
