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

import androidx.annotation.ArrayRes;

import java.util.Objects;

import pro.watchkit.wearable.watchface.R;

/**
 * Check yuor settings.
 */
public class Settings implements Cloneable {
    private BytePacker bytePacker = new BytePacker();
    private boolean mShowUnreadNotifications;
    private boolean mNightVisionModeEnabled;

    private int mComplicationCount = 5;
    private ComplicationRotation mComplicationRotation = ComplicationRotation.ROTATE_25;

    public Settings clone() {
        Settings result;
        try {
            result = (Settings) super.clone();
        } catch (CloneNotSupportedException e) {
            result = new Settings();
            result.setString(getString());
        }
        return result;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                super.hashCode(),
                mShowUnreadNotifications,
                mNightVisionModeEnabled,
                mComplicationCount,
                mComplicationRotation);
    }

    ComplicationRotation getComplicationRotation() {
        return mComplicationRotation;
    }

    private void setComplicationRotation(ComplicationRotation complicationRotation) {
        mComplicationRotation = complicationRotation;
    }

    int getComplicationCount() {
        return mComplicationCount;
    }

    private void setComplicationCount(int complicationCount) {
        mComplicationCount = complicationCount;
        if (mComplicationCount < 5) {
            mComplicationCount = 5;
        } else if (mComplicationCount > 8) {
            mComplicationCount = 8;
        }
    }

    public String getString() {
        packVersion0();
        return bytePacker.getStringFast();
    }

    public void setString(String s) {
        if (s == null || s.length() == 0) return;
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
        bytePacker.put(mNightVisionModeEnabled);
        bytePacker.put(mComplicationCount, 3); // 3-bit complication count
        mComplicationRotation.pack(bytePacker);

        bytePacker.finish();
    }

    private void unpack() {
        bytePacker.rewind();

        /*int version = */
        bytePacker.get(3);
//        switch (version) {
//            case 0:
//            default: {
                mShowUnreadNotifications = bytePacker.getBoolean();
        mNightVisionModeEnabled = bytePacker.getBoolean();
        setComplicationCount(bytePacker.get(3)); // 3-bit complication count
        setComplicationRotation(ComplicationRotation.unpack(bytePacker));
//                break;
//            }
//        }
    }

    public boolean isShowUnreadNotifications() {
        return mShowUnreadNotifications;
    }

    public boolean toggleShowUnreadNotifications() {
        mShowUnreadNotifications = !mShowUnreadNotifications;
        return mShowUnreadNotifications;
    }

    public boolean isNightVisionModeEnabled() {
        return mNightVisionModeEnabled;
    }

    public boolean toggleNightVisionModeEnabled() {
        mNightVisionModeEnabled = !mNightVisionModeEnabled;
        return mNightVisionModeEnabled;
    }

    public enum ComplicationRotation implements WatchFacePreset.EnumResourceId {
        ROTATE_00, ROTATE_25, ROTATE_50, ROTATE_75;

        private static final int bits = 2;

        static ComplicationRotation unpack(BytePacker bytePacker) {
            return values()[bytePacker.get(bits)];
        }

        void pack(BytePacker bytePacker) {
            bytePacker.put(bits, values(), this);
        }

        @Override
        @ArrayRes
        public int getNameResourceId() {
            return R.array.Settings_ComplicationRotation;
        }
    }
}
