/*
 * Copyright (C) 2019-2021 Terence Tan
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
    ComplicationCount mComplicationCount;
    ComplicationRotation mComplicationRotation;
    ComplicationSize mComplicationSize;
    ComplicationScale mComplicationScale;
    int mAmbientDaySixBitColor, mAmbientNightSixBitColor;
    Material mComplicationRingMaterial, mComplicationBackgroundMaterial;
    boolean mDeveloperMode;
    boolean mStats, mStatsDetail;
    boolean mHidePips, mHideHands;
    boolean mUseLegacyColorDrawing, mUseLegacyEffects;
    boolean mUseDecomposition;
    final boolean mHardwareAccelerationEnabled = true;
    final boolean mInnerGlow = false;
    final boolean mDrawShadows = true;
    boolean mTransparentBackground = false;
    HandCutoutCombination mPreviousHourHandCutoutCombination, mPreviousMinuteHandCutoutCombination;

    @Override
    public int hashCode() {
        return Objects.hash(
                super.hashCode(),
                mTypeface,
                mShowUnreadNotifications,
                mComplicationCount,
                mComplicationRotation,
                mComplicationSize,
                mComplicationScale,
                mAmbientDaySixBitColor, mAmbientNightSixBitColor,
                mComplicationRingMaterial, mComplicationBackgroundMaterial,
                mDeveloperMode,
                mStats, mStatsDetail,
                mHidePips, mHideHands,
                mUseLegacyColorDrawing, mUseLegacyEffects,
                mHardwareAccelerationEnabled,
                mInnerGlow,
                mDrawShadows,
                mTransparentBackground,
                mPreviousHourHandCutoutCombination, mPreviousMinuteHandCutoutCombination);
    }

    @Override
    void pack() {
        mBytePacker.rewind();

        // Version. 3-bits. Current version is v0.
        mBytePacker.put(3, 0);

        mBytePacker.put(mShowUnreadNotifications);
        mBytePacker.put(6, mAmbientDaySixBitColor);
        mBytePacker.put(6, mAmbientNightSixBitColor);
        mTypeface.pack(mBytePacker);
        mPreviousHourHandCutoutCombination.pack(mBytePacker);
        mPreviousMinuteHandCutoutCombination.pack(mBytePacker);
        mComplicationCount.pack(mBytePacker);
        mComplicationRotation.pack(mBytePacker);
        mComplicationRingMaterial.pack(mBytePacker);
        mComplicationBackgroundMaterial.pack(mBytePacker);
        mComplicationSize.pack(mBytePacker);
        mComplicationScale.pack(mBytePacker);
        mBytePacker.put(2, 0); // mComplicationTextStyle.pack
        mBytePacker.put(mDeveloperMode);
        mBytePacker.put(mStats);
        mBytePacker.put(mStatsDetail);
        mBytePacker.put(mHidePips);
        mBytePacker.put(mHideHands);
        mBytePacker.put(mUseLegacyColorDrawing);
        mBytePacker.put(mUseLegacyEffects);
        mBytePacker.put(mUseDecomposition);

        mBytePacker.finish();
    }

    @Override
    void unpack() {
        mBytePacker.rewind();

        int version = mBytePacker.get(3);
        switch (version) {
            case 0:
            default: {
                mShowUnreadNotifications = mBytePacker.getBoolean();
                mAmbientDaySixBitColor = mBytePacker.get(6);
                mAmbientNightSixBitColor = mBytePacker.get(6);
                mTypeface = Typeface.unpack(mBytePacker);
                mPreviousHourHandCutoutCombination = HandCutoutCombination.unpack(mBytePacker);
                mPreviousMinuteHandCutoutCombination = HandCutoutCombination.unpack(mBytePacker);
                mComplicationCount = ComplicationCount.unpack(mBytePacker);
                mComplicationRotation = ComplicationRotation.unpack(mBytePacker);
                mComplicationRingMaterial = Material.unpack(mBytePacker);
                mComplicationBackgroundMaterial = Material.unpack(mBytePacker);
                mComplicationSize = ComplicationSize.unpack(mBytePacker);
                mComplicationScale = ComplicationScale.unpack(mBytePacker);
                mBytePacker.get(2); // mComplicationTextStyle = TextStyle.unpack
                mDeveloperMode = mBytePacker.getBoolean();
                mStats = mBytePacker.getBoolean();
                mStatsDetail = mBytePacker.getBoolean();
                mHidePips = mBytePacker.getBoolean();
                mHideHands = mBytePacker.getBoolean();
                mUseLegacyColorDrawing = mBytePacker.getBoolean();
                mUseLegacyEffects = mBytePacker.getBoolean();
                mUseDecomposition = mBytePacker.getBoolean();
                break;
            }
            case 1: {
                break;
            }
        }
    }
}
