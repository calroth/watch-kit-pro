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
    Typeface mTypeface;
    boolean mShowUnreadNotifications;
    //    boolean mNightVisionModeEnabled;
    ComplicationCount mComplicationCount;
    ComplicationRotation mComplicationRotation;
    ComplicationSize mComplicationSize;
    ComplicationScale mComplicationScale;
    int mAmbientDaySixBitColor, mAmbientNightSixBitColor;
    TextStyle mComplicationTextStyle;
    Style mComplicationRingStyle, mComplicationBackgroundStyle;
    boolean mDeveloperMode;
    boolean mStats, mStatsDetail;
    boolean mHideTicks, mHideHands;
    boolean mAltDrawing;
    boolean mHardwareAccelerationEnabled = true;
    boolean mInnerGlow = false;
    boolean mDrawShadows = true;
    boolean mTransparentBackground = false;
    HandCutoutCombination mPreviousHourHandCutoutCombination, mPreviousMinuteHandCutoutCombination;

    @Override
    public int hashCode() {
        return Objects.hash(
                super.hashCode(),
                mTypeface,
                mShowUnreadNotifications,
//                mNightVisionModeEnabled,
                mComplicationCount,
                mComplicationRotation,
                mComplicationSize,
                mComplicationScale,
                mAmbientDaySixBitColor, mAmbientNightSixBitColor,
                mComplicationTextStyle,
                mComplicationRingStyle, mComplicationBackgroundStyle,
                mDeveloperMode,
                mStats, mStatsDetail,
                mHideTicks, mHideHands,
                mAltDrawing,
                mHardwareAccelerationEnabled,
                mInnerGlow,
                mDrawShadows,
                mTransparentBackground,
                mPreviousHourHandCutoutCombination, mPreviousMinuteHandCutoutCombination);
    }

    @Override
    void pack() {
        mBytePacker.rewind();

        // Version. 3-bits. Current version is v2.
        mBytePacker.put(3, 2);

        mBytePacker.put(mShowUnreadNotifications);
        mBytePacker.put(true /*mNightVisionModeEnabled*/);
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
        mBytePacker.put(mHideTicks);
        mBytePacker.put(mHideHands);
        mBytePacker.put(mAltDrawing);
        mTypeface.pack(mBytePacker);
        // TODO: rearrange these
        mComplicationSize.pack(mBytePacker);
        mComplicationScale.pack(mBytePacker);
        mPreviousHourHandCutoutCombination.pack(mBytePacker);
        mPreviousMinuteHandCutoutCombination.pack(mBytePacker);

        mBytePacker.finish();
    }

    @Override
    void unpack() {
        mBytePacker.rewind();

        int version = mBytePacker.get(3);
        switch (version) {
            case 0: {
                break;
            }
            case 1: {
                mShowUnreadNotifications = mBytePacker.getBoolean();
                /*mNightVisionModeEnabled =*/
                mBytePacker.getBoolean();
                mComplicationCount = ComplicationCount.unpack(mBytePacker);
                mComplicationRotation = ComplicationRotation.unpack(mBytePacker);
                mAmbientDaySixBitColor = mBytePacker.get(6);
                mAmbientNightSixBitColor = mBytePacker.get(6);
                mComplicationRingStyle = Style.unpack3(mBytePacker);
                mComplicationBackgroundStyle = Style.unpack3(mBytePacker);
                mDeveloperMode = mBytePacker.getBoolean();
                mStats = mBytePacker.getBoolean();
                mStatsDetail = mBytePacker.getBoolean();
                mComplicationTextStyle = TextStyle.unpack(mBytePacker);
                mHideTicks = mBytePacker.getBoolean();
                mHideHands = mBytePacker.getBoolean();
                mAltDrawing = mBytePacker.getBoolean();
                mTypeface = Typeface.SANS_BOLD;
                mComplicationSize = ComplicationSize.X_LARGE;
                mComplicationScale = ComplicationScale.MEDIUM;
                mPreviousHourHandCutoutCombination = HandCutoutCombination.TIP_PLUS_ONE;
                mPreviousMinuteHandCutoutCombination = HandCutoutCombination.TIP_PLUS_ONE;
                break;
            }
            case 2:
            default: {
                mShowUnreadNotifications = mBytePacker.getBoolean();
                /*mNightVisionModeEnabled =*/
                mBytePacker.getBoolean();
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
                mHideTicks = mBytePacker.getBoolean();
                mHideHands = mBytePacker.getBoolean();
                mAltDrawing = mBytePacker.getBoolean();
                mTypeface = Typeface.unpack(mBytePacker);
                mComplicationSize = ComplicationSize.unpack(mBytePacker);
                mComplicationScale = ComplicationScale.unpack(mBytePacker);
                mPreviousHourHandCutoutCombination = HandCutoutCombination.unpack(mBytePacker);
                mPreviousMinuteHandCutoutCombination = HandCutoutCombination.unpack(mBytePacker);
                break;
            }
        }
    }
}
