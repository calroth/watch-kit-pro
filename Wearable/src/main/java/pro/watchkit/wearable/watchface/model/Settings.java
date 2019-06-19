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
final class Settings extends BytePackable {
    boolean mShowUnreadNotifications;
    boolean mNightVisionModeEnabled;
    ComplicationCount mComplicationCount;
    ComplicationRotation mComplicationRotation = ComplicationRotation.ROTATE_25;
    int mAmbientDaySixBitColor, mAmbientNightSixBitColor;
    TextStyle mComplicationTextStyle;
    Style mComplicationRingStyle, mComplicationBackgroundStyle;
    Style mSwatchStyle;
    boolean mDeveloperMode;
    boolean mStats, mStatsDetail;

    @Override
    public int hashCode() {
        return Objects.hash(
                super.hashCode(),
                mShowUnreadNotifications,
                mNightVisionModeEnabled,
                mComplicationCount,
                mComplicationRotation,
                mAmbientDaySixBitColor, mAmbientNightSixBitColor,
                mComplicationTextStyle,
                mComplicationRingStyle, mComplicationBackgroundStyle,
                mSwatchStyle,
                mDeveloperMode,
                mStats, mStatsDetail);
    }

    @Override
    void pack() {
        packVersion1();
    }

    @SuppressWarnings("unused")
    private void packVersion0() {
        mBytePacker.rewind();

        // Version. 3-bits. Current version is v0.
//        mBytePacker.put(3, 0);
//
//        mBytePacker.put(mShowUnreadNotifications);
//        mBytePacker.put(mNightVisionModeEnabled);
//        mBytePacker.put(3, mComplicationCount); // 3-bit complication count
//        mComplicationRotation.pack(mBytePacker);

        mBytePacker.finish();
    }

    private void packVersion1() {
        mBytePacker.rewind();

        // Version. 3-bits. Current version is v1.
        mBytePacker.put(3, 1);

        mBytePacker.put(mShowUnreadNotifications);
        mBytePacker.put(mNightVisionModeEnabled);
        mComplicationCount.pack(mBytePacker);
        mComplicationRotation.pack(mBytePacker);
        mBytePacker.put(6, mAmbientDaySixBitColor);
        mBytePacker.put(6, mAmbientNightSixBitColor);
        mComplicationRingStyle.pack(mBytePacker);
        mComplicationBackgroundStyle.pack(mBytePacker);
        mBytePacker.put(mDeveloperMode);
        mBytePacker.put(mStats);
        mBytePacker.put(mStatsDetail);
        mComplicationTextStyle.pack(mBytePacker);
        mSwatchStyle.pack(mBytePacker);

        mBytePacker.finish();
    }

    @Override
    void unpack() {
        mBytePacker.rewind();

        int version = mBytePacker.get(3);
        switch (version) {
            case 0: {
//                mShowUnreadNotifications = mBytePacker.getBoolean();
//                mNightVisionModeEnabled = mBytePacker.getBoolean();
//                mComplicationCount = mBytePacker.get(3); // 3-bit complication count
//                mComplicationRotation = ComplicationRotation.unpack(mBytePacker);
                break;
            }
            case 1:
            default: {
                mShowUnreadNotifications = mBytePacker.getBoolean();
                mNightVisionModeEnabled = mBytePacker.getBoolean();
                mComplicationCount = ComplicationCount.unpack(mBytePacker);
                mComplicationRotation = ComplicationRotation.unpack(mBytePacker);
                mAmbientDaySixBitColor = mBytePacker.get(6);
                mAmbientNightSixBitColor = mBytePacker.get(6);
                mComplicationRingStyle = Style.unpack(mBytePacker);
                mComplicationBackgroundStyle = Style.unpack(mBytePacker);
                mDeveloperMode = mBytePacker.getBoolean();
                mStats = mBytePacker.getBoolean();
                mStatsDetail = mBytePacker.getBoolean();
                mComplicationTextStyle = TextStyle.unpack(mBytePacker);
                mSwatchStyle = Style.unpack(mBytePacker);
                break;
            }
        }
    }
}
