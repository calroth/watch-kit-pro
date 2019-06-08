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

import java.util.Objects;

/**
 * Check yuor settings.
 */
public class Settings extends BytePackable implements Cloneable {
    private boolean mShowUnreadNotifications;
    private boolean mNightVisionModeEnabled;

    private int mComplicationCount = 5;
    private ComplicationRotation mComplicationRotation = ComplicationRotation.ROTATE_25;
    private int mAmbientDaySixBitColor;
    private int mAmbientNightSixBitColor;
    private Style mComplicationRingStyle;
    private Style mComplicationBackgroundStyle;

    @Override
    public int hashCode() {
        return Objects.hash(
                super.hashCode(),
                mShowUnreadNotifications,
                mNightVisionModeEnabled,
                mComplicationCount,
                mComplicationRotation,
                mAmbientDaySixBitColor,
                mAmbientNightSixBitColor,
                mComplicationRingStyle,
                mComplicationBackgroundStyle);
    }

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
    void pack() {
        packVersion1();
    }

    @SuppressWarnings("unused")
    private void packVersion0() {
        mBytePacker.rewind();

        // Version. 3-bits. Current version is v0.
        mBytePacker.put(3, 0);

        mBytePacker.put(mShowUnreadNotifications);
        mBytePacker.put(mNightVisionModeEnabled);
        mBytePacker.put(3, mComplicationCount); // 3-bit complication count
        mComplicationRotation.pack(mBytePacker);

        mBytePacker.finish();
    }

    private void packVersion1() {
        mBytePacker.rewind();

        // Version. 3-bits. Current version is v1.
        mBytePacker.put(3, 1);

        mBytePacker.put(mShowUnreadNotifications);
        mBytePacker.put(mNightVisionModeEnabled);
        getComplicationCountEnum().pack(mBytePacker);
        mComplicationRotation.pack(mBytePacker);
        mBytePacker.put(6, getAmbientDaySixBitColor());
        mBytePacker.put(6, getAmbientNightSixBitColor());
        mComplicationRingStyle.pack(mBytePacker);
        mComplicationBackgroundStyle.pack(mBytePacker);

        mBytePacker.finish();
    }

    @Override
    void unpack() {
        mBytePacker.rewind();

        int version = mBytePacker.get(3);
        switch (version) {
            case 0: {
                mShowUnreadNotifications = mBytePacker.getBoolean();
                mNightVisionModeEnabled = mBytePacker.getBoolean();
                setComplicationCount(mBytePacker.get(3)); // 3-bit complication count
                setComplicationRotation(ComplicationRotation.unpack(mBytePacker));
                break;
            }
            case 1:
            default: {
                mShowUnreadNotifications = mBytePacker.getBoolean();
                mNightVisionModeEnabled = mBytePacker.getBoolean();
                setComplicationCountEnum(ComplicationCount.unpack(mBytePacker));
                setComplicationRotation(ComplicationRotation.unpack(mBytePacker));
                setAmbientDaySixBitColor(mBytePacker.get(6));
                setAmbientNightSixBitColor(mBytePacker.get(6));
                setComplicationRingStyle(Style.unpack(mBytePacker));
                setComplicationBackgroundStyle(Style.unpack(mBytePacker));
                break;
            }
        }
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

    public boolean isShowUnreadNotifications() {
        return mShowUnreadNotifications;
    }

    public void toggleShowUnreadNotifications() {
        mShowUnreadNotifications = !mShowUnreadNotifications;
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

    public Style getComplicationRingStyle() {
        return mComplicationRingStyle;
    }

    void setComplicationRingStyle(Style complicationRingStyle) {
        this.mComplicationRingStyle = complicationRingStyle;
    }

    public Style getComplicationBackgroundStyle() {
        return mComplicationBackgroundStyle;
    }

    void setComplicationBackgroundStyle(Style complicationBackgroundStyle) {
        this.mComplicationBackgroundStyle = complicationBackgroundStyle;
    }

}
