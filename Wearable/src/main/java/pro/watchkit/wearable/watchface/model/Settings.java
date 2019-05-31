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
    private int mAmbientDaySixBitColor;
    private int mAmbientNightSixBitColor;

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
                mComplicationRotation,
                mAmbientDaySixBitColor,
                mAmbientNightSixBitColor);
    }

    ComplicationRotation getComplicationRotation() {
        return mComplicationRotation;
    }

    void setComplicationRotation(ComplicationRotation complicationRotation) {
        mComplicationRotation = complicationRotation;
    }

    ComplicationCount getComplicationCountEnum() {
        switch (getComplicationCount()) {
            case 5: {
                return ComplicationCount.COUNT_5;
            }
            case 6: {
                return ComplicationCount.COUNT_6;
            }
            case 7: {
                return ComplicationCount.COUNT_7;
            }
            case 8: {
                return ComplicationCount.COUNT_8;
            }
            default: {
                // 6 by default?
                return ComplicationCount.COUNT_6;
            }
        }
    }

    void setComplicationCountEnum(ComplicationCount c) {
        switch (c) {
            case COUNT_5: {
                setComplicationCount(5);
                break;
            }
            case COUNT_6: {
                setComplicationCount(6);
                break;
            }
            case COUNT_7: {
                setComplicationCount(7);
                break;
            }
            case COUNT_8: {
                setComplicationCount(8);
                break;
            }
        }
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
        packVersion1();
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
        bytePacker.put(3, mComplicationCount); // 3-bit complication count
        mComplicationRotation.pack(bytePacker);

        bytePacker.finish();
    }

    private void packVersion1() {
        bytePacker.rewind();

        // Version. 3-bits. Current version is v1.
        bytePacker.put(3, 1);

        bytePacker.put(mShowUnreadNotifications);
        bytePacker.put(mNightVisionModeEnabled);
        getComplicationCountEnum().pack(bytePacker);
        mComplicationRotation.pack(bytePacker);
        bytePacker.put(6, getAmbientDaySixBitColor());
        bytePacker.put(6, getAmbientNightSixBitColor());

        bytePacker.finish();
    }

    private void unpack() {
        bytePacker.rewind();

        int version = bytePacker.get(3);
        switch (version) {
            case 0: {
                mShowUnreadNotifications = bytePacker.getBoolean();
                mNightVisionModeEnabled = bytePacker.getBoolean();
                setComplicationCount(bytePacker.get(3)); // 3-bit complication count
                setComplicationRotation(ComplicationRotation.unpack(bytePacker));
                break;
            }
            case 1:
            default: {
                mShowUnreadNotifications = bytePacker.getBoolean();
                mNightVisionModeEnabled = bytePacker.getBoolean();
                setComplicationCountEnum(ComplicationCount.unpack(bytePacker));
                setComplicationRotation(ComplicationRotation.unpack(bytePacker));
                setAmbientDaySixBitColor(bytePacker.get(6));
                setAmbientNightSixBitColor(bytePacker.get(6));
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

    public boolean isNightVisionModeEnabled() {
        return mNightVisionModeEnabled;
    }

    public boolean toggleNightVisionModeEnabled() {
        mNightVisionModeEnabled = !mNightVisionModeEnabled;
        return mNightVisionModeEnabled;
    }

    int getAmbientDaySixBitColor() {
        return mAmbientDaySixBitColor;
    }

    void setAmbientDaySixBitColor(int ambientDaySixBitColor) {
        mAmbientDaySixBitColor = ambientDaySixBitColor;
    }

    int getAmbientNightSixBitColor() {
        return mAmbientNightSixBitColor;
    }

    void setAmbientNightSixBitColor(int ambientNightSixBitColor) {
        mAmbientNightSixBitColor = ambientNightSixBitColor;
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

    public enum ComplicationCount implements WatchFacePreset.EnumResourceId {
        COUNT_5, COUNT_6, COUNT_7, COUNT_8;

        private static final int bits = 2;

        static ComplicationCount unpack(BytePacker bytePacker) {
            return values()[bytePacker.get(bits)];
        }

        void pack(BytePacker bytePacker) {
            bytePacker.put(bits, values(), this);
        }

        @Override
        @ArrayRes
        public int getNameResourceId() {
            return R.array.Settings_ComplicationCount;
        }
    }
}
