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
 */

package pro.watchkit.wearable.watchface.model;

import android.annotation.SuppressLint;
import android.util.Log;

import androidx.annotation.ArrayRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import pro.watchkit.wearable.watchface.R;

public abstract class BytePackable {
    private static final String TAG = "BytePackable";

    @NonNull
    BytePacker mBytePacker = new BytePacker();

    abstract void pack();

    abstract void unpack();

    @NonNull
    public String getString() {
        pack();
        return mBytePacker.getStringFast();
    }

    @NonNull
    String getHash() {
        pack();
        return mBytePacker.getString().substring(0, 8); // First 8 digits.
    }

    public void setString(@Nullable String s) {
        if (s == null || s.length() == 0) return;
        try {
            mBytePacker.setStringFast(s);
            unpack();
        } catch (java.lang.StringIndexOutOfBoundsException e) {
            Log.d(TAG, "setString failed: " + s);
            Log.d(TAG, "setString failed: " + e.toString());
        }
    }

    public enum TicksDisplay implements EnumResourceId {
        NONE, FOUR, FOUR_TWELVE, FOUR_TWELVE_60;

        private static final int bits = 2;

        static TicksDisplay unpack(@NonNull BytePacker bytePacker) {
            return values()[bytePacker.get(bits)];
        }

        void pack(@NonNull BytePacker bytePacker) {
            bytePacker.put(bits, values(), this);
        }

        @Override
        @ArrayRes
        public int getNameResourceId() {
            return R.array.WatchFacePreset_TicksDisplay;
        }
    }

    public enum HandShape implements EnumResourceId {
        STRAIGHT, ROUNDED, DIAMOND, TRIANGLE;

        private static final int bits = 2;

        static HandShape unpack(@NonNull BytePacker bytePacker) {
            return values()[bytePacker.get(bits)];
        }

        void pack(@NonNull BytePacker bytePacker) {
            bytePacker.put(bits, values(), this);
        }

        @Override
        @ArrayRes
        public int getNameResourceId() {
            return R.array.WatchFacePreset_HandShape;
        }
    }

    public enum HandLength implements EnumResourceId {
        SHORT, MEDIUM, LONG, X_LONG;

        private static final int bits = 2;

        static HandLength unpack(@NonNull BytePacker bytePacker) {
            return values()[bytePacker.get(bits)];
        }

        void pack(@NonNull BytePacker bytePacker) {
            bytePacker.put(bits, values(), this);
        }

        @Override
        @ArrayRes
        public int getNameResourceId() {
            return R.array.WatchFacePreset_HandLength;
        }
    }

    public enum HandThickness implements EnumResourceId {
        THIN, REGULAR, THICK, X_THICK;

        private static final int bits = 2;

        static HandThickness unpack(@NonNull BytePacker bytePacker) {
            return values()[bytePacker.get(bits)];
        }

        void pack(@NonNull BytePacker bytePacker) {
            bytePacker.put(bits, values(), this);
        }

        @Override
        @ArrayRes
        public int getNameResourceId() {
            return R.array.WatchFacePreset_HandThickness;
        }
    }

    public enum HandStalk implements EnumResourceId {
        NEGATIVE, NONE, SHORT, MEDIUM;

        private static final int bits = 2;

        static HandStalk unpack(@NonNull BytePacker bytePacker) {
            return values()[bytePacker.get(bits)];
        }

        void pack(@NonNull BytePacker bytePacker) {
            bytePacker.put(bits, values(), this);
        }

        @Override
        @ArrayRes
        public int getNameResourceId() {
            return R.array.WatchFacePreset_HandStalk;
        }
    }

    public enum HandCutoutStyle implements EnumResourceId {
        PLUS_ONE, PLUS_TWO, PLUS_THREE;

        @Override
        @ArrayRes
        public int getNameResourceId() {
            return R.array.WatchFacePreset_HandCutoutStyle;
        }
    }

    public enum HandCutoutShape implements EnumResourceId {
        TIP, TIP_STALK, HAND, STALK, HAND_STALK;

        @Override
        @ArrayRes
        public int getNameResourceId() {
            return R.array.WatchFacePreset_HandCutoutShape;
        }
    }

    public enum HandCutoutCombination implements EnumResourceId {
        TIP_PLUS_ONE, TIP_PLUS_TWO, TIP_PLUS_THREE,
        TIP_STALK_PLUS_ONE, TIP_STALK_PLUS_TWO, TIP_STALK_PLUS_THREE,
        HAND_PLUS_ONE, HAND_PLUS_TWO, HAND_PLUS_THREE,
        STALK_PLUS_ONE, STALK_PLUS_TWO, STALK_PLUS_THREE,
        HAND_STALK_PLUS_ONE, HAND_STALK_PLUS_TWO, HAND_STALK_PLUS_THREE,
        NONE;

        private static final int bits = 4;

        static HandCutoutCombination unpack(@NonNull BytePacker bytePacker) {
            return values()[bytePacker.get(bits)];
        }

        void pack(@NonNull BytePacker bytePacker) {
            bytePacker.put(bits, values(), this);
        }

        @Override
        @ArrayRes
        public int getNameResourceId() {
            return R.array.WatchFacePreset_HandCutoutPermutation;
        }
    }

    public enum TickShape implements EnumResourceId {
        SECTOR, SQUARE_WIDE, SQUARE, SQUARE_CUTOUT, BAR_1_2, BAR_1_4, BAR_1_8,
        DOT_THIN, DOT, DOT_CUTOUT,
        TRIANGLE_THIN, TRIANGLE, TRIANGLE_CUTOUT,
        DIAMOND_THIN, DIAMOND, DIAMOND_CUTOUT;

        @NonNull
        private static TickShape[] orderedValues = new TickShape[]{
                SECTOR, DOT, TRIANGLE, DIAMOND,
                SQUARE, BAR_1_2, BAR_1_4, BAR_1_8,
                SQUARE_WIDE, DOT_THIN, TRIANGLE_THIN, DIAMOND_THIN,
                SQUARE_CUTOUT, DOT_CUTOUT, TRIANGLE_CUTOUT, DIAMOND_CUTOUT};

        private static final int bits = 4;

        static TickShape unpack(@NonNull BytePacker bytePacker) {
//            return values()[bytePacker.get(bits)];
            return orderedValues[bytePacker.get(bits)];
        }

        static TickShape unpack2(@NonNull BytePacker bytePacker) {
//            return values()[bytePacker.get(2)];
            return orderedValues[bytePacker.get(2)];
        }

        void pack(@NonNull BytePacker bytePacker) {
//            bytePacker.put(bits, values(), this);
            bytePacker.put(bits, orderedValues, this);
        }

        @Override
        @ArrayRes
        public int getNameResourceId() {
            return R.array.WatchFacePreset_TickShape;
        }
    }

    public enum TickSize implements EnumResourceId {
        XX_SHORT, X_SHORT, SHORT, MEDIUM, LONG, X_LONG, XX_LONG, XXX_LONG;

        private static final int bits = 3;

        static TickSize unpack(@NonNull BytePacker bytePacker) {
            return values()[bytePacker.get(bits)];
        }

        static TickSize unpack2(@NonNull BytePacker bytePacker) {
            return values()[bytePacker.get(2)];
        }

        void pack(@NonNull BytePacker bytePacker) {
            bytePacker.put(bits, values(), this);
        }

        @Override
        @ArrayRes
        public int getNameResourceId() {
            return R.array.WatchFacePreset_TickSize;
        }
    }

    public enum TickMargin implements EnumResourceId {
        NONE, SMALL, MEDIUM, LARGE;

        private static final int bits = 2;

        static TickMargin unpack(@NonNull BytePacker bytePacker) {
            return values()[bytePacker.get(bits)];
        }

        void pack(@NonNull BytePacker bytePacker) {
            bytePacker.put(bits, values(), this);
        }

        @Override
        @ArrayRes
        public int getNameResourceId() {
            return R.array.WatchFacePreset_TickMargin;
        }
    }

    public enum DigitDisplay implements EnumResourceId {
        NONE, BELOW, OVER, ABOVE;

        private static final int bits = 2;

        static DigitDisplay unpack(@NonNull BytePacker bytePacker) {
            return values()[bytePacker.get(bits)];
        }

        void pack(@NonNull BytePacker bytePacker) {
            bytePacker.put(bits, values(), this);
        }

        @Override
        @ArrayRes
        public int getNameResourceId() {
            return R.array.WatchFacePreset_DigitDisplay;
        }
    }

    public enum DigitSize implements EnumResourceId {
        SMALL, MEDIUM, LARGE, X_LARGE;

        private static final int bits = 2;

        static DigitSize unpack(@NonNull BytePacker bytePacker) {
            return values()[bytePacker.get(bits)];
        }

        void pack(@NonNull BytePacker bytePacker) {
            bytePacker.put(bits, values(), this);
        }

        @Override
        @ArrayRes
        public int getNameResourceId() {
            return R.array.WatchFacePreset_DigitSize;
        }
    }

    public enum DigitRotation implements EnumResourceId {
        UPRIGHT, CURVED;

        private static final int bits = 1;

        static DigitRotation unpack(@NonNull BytePacker bytePacker) {
            return values()[bytePacker.get(bits)];
        }

        void pack(@NonNull BytePacker bytePacker) {
            bytePacker.put(bits, values(), this);
        }

        @Override
        @ArrayRes
        public int getNameResourceId() {
            return R.array.WatchFacePreset_DigitRotation;
        }
    }

    public enum DigitFormat implements EnumResourceId {
        NUMERALS_12_4, NUMERALS_12_12, NUMERALS_60, ROMAN,
        CIRCLED, NEGATIVE_CIRCLED, DOUBLE_STRUCK, CLOCK_FACES;

        private static final int bits = 3;

        static DigitFormat unpack(@NonNull BytePacker bytePacker) {
            return values()[bytePacker.get(bits)];
        }

        static DigitFormat unpack2(@NonNull BytePacker bytePacker) {
            return values()[bytePacker.get(2)];
        }

        void pack(@NonNull BytePacker bytePacker) {
            bytePacker.put(bits, values(), this);
        }

        @Override
        @ArrayRes
        public int getNameResourceId() {
            return R.array.WatchFacePreset_DigitFormat;
        }
    }

    public enum TextStyle implements EnumResourceId {
        FILL, ACCENT, HIGHLIGHT, BASE;

        private static final int bits = 2;

        static TextStyle unpack(@NonNull BytePacker bytePacker) {
            return values()[bytePacker.get(bits)];
        }

        void pack(@NonNull BytePacker bytePacker) {
            bytePacker.put(bits, values(), this);
        }

        @Override
        @ArrayRes
        public int getNameResourceId() {
            return R.array.WatchFacePreset_TextStyle;
        }
    }

    public enum Style implements EnumResourceId {
        FILL_HIGHLIGHT, ACCENT_FILL, ACCENT_HIGHLIGHT, BASE_ACCENT;

        private static final int bits = 2;

        static Style unpack(@NonNull BytePacker bytePacker) {
            return values()[bytePacker.get(bits)];
        }

        static Style unpack3(@NonNull BytePacker bytePacker) {
            return values()[bytePacker.get(3) % values().length];
        }

        void pack(@NonNull BytePacker bytePacker) {
            bytePacker.put(bits, values(), this);
        }

        @Override
        @ArrayRes
        public int getNameResourceId() {
            return R.array.WatchFacePreset_Style;
        }
    }

    public enum StyleGradient implements EnumResourceId {
        FLAT, SWEEP, RADIAL, TRIANGLE;

        private static final int bits = 2;

        static StyleGradient unpack(@NonNull BytePacker bytePacker) {
            return values()[bytePacker.get(bits)];
        }

        void pack(@NonNull BytePacker bytePacker) {
            bytePacker.put(bits, values(), this);
        }

        @Override
        @ArrayRes
        public int getNameResourceId() {
            return R.array.WatchFacePreset_StyleGradient;
        }
    }

    public enum StyleTexture implements EnumResourceId {
        NONE, SPUN, WEAVE, HEX;

        private static final int bits = 2;

        static StyleTexture unpack(@NonNull BytePacker bytePacker) {
            return values()[bytePacker.get(bits)];
        }

        void pack(@NonNull BytePacker bytePacker) {
            bytePacker.put(bits, values(), this);
        }

        @Override
        @ArrayRes
        public int getNameResourceId() {
            return R.array.WatchFacePreset_StyleTexture;
        }
    }

    public enum ComplicationRotation implements EnumResourceId {
        ROTATE_00, ROTATE_25, ROTATE_50, ROTATE_75;

        private static final int bits = 2;

        static ComplicationRotation unpack(@NonNull BytePacker bytePacker) {
            return values()[bytePacker.get(bits)];
        }

        void pack(@NonNull BytePacker bytePacker) {
            bytePacker.put(bits, values(), this);
        }

        @Override
        @ArrayRes
        public int getNameResourceId() {
            return R.array.Settings_ComplicationRotation;
        }
    }

    public enum ComplicationCount implements EnumResourceId {
        COUNT_5, COUNT_6, COUNT_7, COUNT_8;

        private static final int bits = 2;

        static ComplicationCount unpack(@NonNull BytePacker bytePacker) {
            return values()[bytePacker.get(bits)];
        }

        void pack(@NonNull BytePacker bytePacker) {
            bytePacker.put(bits, values(), this);
        }

        @Override
        @ArrayRes
        public int getNameResourceId() {
            return R.array.Settings_ComplicationCount;
        }
    }

    public enum ComplicationSize implements EnumResourceId {
        SMALL, MEDIUM, LARGE, X_LARGE;

        private static final int bits = 2;

        static ComplicationSize unpack(@NonNull BytePacker bytePacker) {
            return values()[bytePacker.get(bits)];
        }

        void pack(@NonNull BytePacker bytePacker) {
            bytePacker.put(bits, values(), this);
        }

        @Override
        @ArrayRes
        public int getNameResourceId() {
            return R.array.Settings_ComplicationSize;
        }
    }

    public enum ComplicationScale implements EnumResourceId {
        SMALL, MEDIUM, LARGE, X_LARGE;

        private static final int bits = 2;

        static ComplicationScale unpack(@NonNull BytePacker bytePacker) {
            return values()[bytePacker.get(bits)];
        }

        void pack(@NonNull BytePacker bytePacker) {
            bytePacker.put(bits, values(), this);
        }

        @Override
        @ArrayRes
        public int getNameResourceId() {
            return R.array.Settings_ComplicationScale;
        }
    }

    public enum Typeface implements EnumResourceId {
        SANS_THIN, SANS_LIGHT, SANS_REGULAR, SANS_MEDIUM, SANS_BOLD, SANS_BLACK,
        CONDENSED_LIGHT, CONDENSED_REGULAR, CONDENSED_MEDIUM, CONDENSED_BOLD,
        SERIF_REGULAR, SERIF_BOLD,
        MONO_REGULAR,
        PRODUCT_SANS_REGULAR, PRODUCT_SANS_MEDIUM, PRODUCT_SANS_BOLD;

        private static final int bits = 4;

        static Typeface unpack(@NonNull BytePacker bytePacker) {
            return values()[bytePacker.get(bits)];
        }

        void pack(@NonNull BytePacker bytePacker) {
            bytePacker.put(bits, values(), this);
        }

        @Override
        @ArrayRes
        public int getNameResourceId() {
            return R.array.Settings_Typeface;
        }
    }

    public interface EnumResourceId {
        @ArrayRes
        int getNameResourceId();
    }

    /**
     * A class that packs variable-length ints into a stream of mBytes.
     * We use this to fit all our config data into a single 128-bit
     * binary array.
     * <p>
     * We represent this data as 16-hex-digit strings that are reversibly
     * hashed for transport and packing.
     */
    final class BytePacker {
        private static final int LENGTH = 16;
        private byte[] mBytes;
        private int mBytePtr;
        private SecretKeySpec mKey;
        private Cipher mCipher;

        BytePacker() {
            setKey(getDefaultKey());
            mBytes = new byte[LENGTH];
            rewind();
        }

        /**
         * We're done writing. Write zeroes to the rest of the array, followed by
         * the version string.
         */
        void finish() {
//            Log.d(TAG, "finish() with " + mBytePtr + " / " + (mBytes.length * 8 - 1) + " bits.");
            if (mBytePtr >= mBytes.length * 8 - 1) {
                // Already finalized?
                return;
            }
            int versionLength = 0;
            int remainingBits = (mBytes.length * 8) - mBytePtr - versionLength - 1; // Off by 1...
            // Write out "remainingBits" zeroes.
            while (remainingBits > 0) {
                int length = remainingBits > 8 ? 8 : remainingBits;
                put(length, 0);
                remainingBits -= length;
            }
//            // Write our version.
//            int version = 0;
//            put(versionLength, version);
        }

        @NonNull
        private byte[] getDefaultKey() {
            try {
                // Default key is the md5sum of John 3:16 (KJV).
                // Not trying to be secure, just enough for a reversible hash.
                MessageDigest messageDigest = MessageDigest.getInstance("MD5");
                String john_3_16_KJV = "For God so loved the world, that he gave his only "
                        + "begotten Son, that whosoever believeth in him should not perish, "
                        + "but have everlasting life.";
                return messageDigest.digest(john_3_16_KJV.getBytes());
            } catch (NoSuchAlgorithmException e) {
                // MD5 isn't available...
                // Just use this pre-calculated key.
                return new byte[]{
                        (byte) 0xbd, (byte) 0xa6, (byte) 0x15, (byte) 0xc4,
                        (byte) 0xbe, (byte) 0x1e, (byte) 0x90, (byte) 0xa9,
                        (byte) 0x85, (byte) 0xec, (byte) 0xe4, (byte) 0xc6,
                        (byte) 0x83, (byte) 0x9a, (byte) 0xd6, (byte) 0x4e};
            }
        }

        @SuppressLint("GetInstance")
        private void setKey(@NonNull byte[] key) {
            byte[] key1 = new byte[16];
            // Copy the first 16 bytes from key into key1.
            System.arraycopy(key, 0, key1, 0, 16);
            // And use that to seed our SecretKeySpec.
            mKey = new SecretKeySpec(key1, "AES");
            try {
                // Whilst we're here, create our cipher.
                // Cipher is AES ECB. Use of ECB is notionally weak...
                // But we don't mind, we're not trying to be crypto-strong or protect anything.
                // We just want a reversible hash that evenly distributes amongst buckets.
                mCipher = Cipher.getInstance("AES/ECB/NoPadding");
            } catch (@NonNull NoSuchAlgorithmException | NoSuchPaddingException ex) {
                Log.d(TAG, "setKey: " + ex.toString());
            }
        }

        @NonNull
        String getStringFast() {
            return byteArrayToString(mBytes);
        }

        void setStringFast(@NonNull String s) {
            if (s.length() < mBytes.length * 2) {
                throw new Error("Invalid length, expected " + (mBytes.length * 2) +
                        " or more hex digits");
            }
            for (int i = 0; i < mBytes.length; i++) {
                // Go through "s", 2 hex digits at a time.
                // Pack those 2 hex digits into a single byte in "mBytes".
                mBytes[i] = (byte) ((Character.digit(s.charAt(i * 2), 16) << 4)
                        + Character.digit(s.charAt(i * 2 + 1), 16));
            }
        }

        @NonNull
        public String getString() {
            //        Log.d(TAG, "Unencrypted: " + byteArrayToString(mBytes));
            try {
                mCipher.init(Cipher.ENCRYPT_MODE, mKey);
                //            Log.d(TAG, "Encrypted: " + result);
                return byteArrayToString(mCipher.doFinal(mBytes));
            } catch (@NonNull IllegalBlockSizeException | BadPaddingException | InvalidKeyException ex) {
                Log.d(TAG, "getString: " + ex.toString());
                return "0000000000000000";
            }
        }

        public void setString(@NonNull String s) {
            // Source: https://stackoverflow.com/questions/140131/convert-a-string-representation-of-a-hex-dump-to-a-byte-array-using-java
            int length = s.length();
            byte[] encrypted = new byte[length / 2];
            for (int i = 0; i < length; i += 2) {
                encrypted[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                        + Character.digit(s.charAt(i + 1), 16));
            }

            // Log.d(TAG, "Encrypted: " + byteArrayToString(encrypted));

            try {
                mCipher.init(Cipher.DECRYPT_MODE, mKey);
                mBytes = mCipher.doFinal(encrypted);
                //            Log.d(TAG, "Decrypted: " + byteArrayToString(mBytes));
            } catch (@NonNull IllegalBlockSizeException
                    | BadPaddingException | InvalidKeyException ex) {
                Log.d(TAG, "setString: " + ex.toString());
            }

            rewind();
        }

        @NonNull
        private String byteArrayToString(@NonNull byte[] a) {
            StringBuilder sb = new StringBuilder(a.length * 2);
            for (byte b : a) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        }

        void rewind() {
            mBytePtr = 0;
        }

        @SuppressWarnings("unused")
        void unitTest() {
            rewind();
            testPut(2, 0); // start=0, length=2
            testPut(2, 2); // start=2, length=2
            testPut(2, 1); // start=4, length=2
            testPut(2, 3); // start=6, length=2
            testPut(5, 0); // start=8, length=5
            testPut(5, 2); // start=13, length=5
            testPut(5, 1); // start=18, length=5
            testPut(5, 3); // start=23, length=5
            testPut(5, 0); // start=28, length=5
            testPut(2, 0); // start=33, length=2
            testPut(2, 2); // start=35, length=2
            testPut(2, 1); // start=37, length=2
            testPut(2, 3); // start=39, length=2

            rewind();
            testGet(2, 0); // start=0, length=2
            testGet(2, 2); // start=2, length=2
            testGet(2, 1); // start=4, length=2
            testGet(2, 3); // start=6, length=2
            testGet(5, 0); // start=8, length=5
            testGet(5, 2); // start=13, length=5
            testGet(5, 1); // start=18, length=5
            testGet(5, 3); // start=23, length=5
            testGet(5, 0); // start=28, length=5
            testGet(2, 0); // start=33, length=2
            testGet(2, 2); // start=35, length=2
            testGet(2, 1); // start=37, length=2
            testGet(2, 3); // start=39, length=2

            rewind();
            testPut(5, 31);
            testPut(2, 0);
            testPut(2, 2);
            testPut(2, 1);
            testPut(2, 3);
            testPut(5, 2);
            testPut(5, 1);
            testPut(5, 3);
            testPut(2, 0);
            testPut(2, 2);
            testPut(2, 1);
            testPut(2, 3);

            rewind();
            testGet(5, 31);
            testGet(2, 0);
            testGet(2, 2);
            testGet(2, 1);
            testGet(2, 3);
            testGet(5, 2);
            testGet(5, 1);
            testGet(5, 3);
            testGet(2, 0);
            testGet(2, 2);
            testGet(2, 1);
            testGet(2, 3);
            // This last one should be a remnant of previous.
            testGet(5, 7);
            // One more test. Should be 0 from here on in.
            testGet(5, 0);
            testGet(2, 0);
            testGet(5, 0);
        }

        private void testPut(int length, int value) {
            put(length, value);
        }

        void put(boolean value) {
            put(1, value ? 1 : 0);
        }

        /**
         * Push the given 6-bit color (between 0 and 63) onto the stack.
         *
         * @param value 6-bit color to push onto the stack
         */
        void putSixBitColor(int value) {
            // Stow our color. It's 6-bit (between 0 and 63).
            put(6, value);
        }

        void put(int length, @NonNull Object[] values, Object value) {
            for (int i = 0; i < values.length; i++) {
                if (values[i] == value) {
                    put(length, i);
                    return;
                }
            }
            // Uh-oh. Nothing matched???
            // Just put 0 and hope for the best.
            put(length, 0);
        }

        void put(int length, int value) {
            put(length, value, mBytePtr);
            mBytePtr += length;
        }

        private void put(int length, int value, int start) {
            // Cap our length to [0, 8].
            if (length < 0) {
                length = 0;
            } else if (length > 8) {
                length = 8;
            }

            // Cap our start to [0, mBytes.length * 8 - length].
            if (start < 0) {
                start = 0;
            } else if (start + length > mBytes.length * 8) {
                start = mBytes.length * 8 - length;
            }

            // Sanitize "value"; zero out any bits that are longer than "length".
            value = value & (0xff >>> (8 - length));

            int startBit = 8 - (start % 8);
            int endBit = 8 - ((start + length) % 8);
            int startByte = (start - (start % 8)) / 8;
            if (startBit > endBit) {
                // length = 5, start = 2
                // startBit = 6, endBit = 1
                // value = 000xxxxx
                // want  = 00xxxxx0
                // The bits we want are in a single byte.
                // Bit-shift "value" into "byte0".
                int byte0 = value << endBit;
                // The mask is to zero out the bits we want overwritten.
                int mask0 = (0xff >> (8 - length)) << endBit;
                // Zero out the bits that we want overwritten and add "value" on.
                mBytes[startByte] = (byte) (mBytes[startByte] & ~mask0 | byte0);
            } else {
                // length = 5, start = 6
                // startBit = 2, endBit = 5
                // value = 000xxxxx
                // want  = 000000xx xxx00000
                // length = 5, start = 4
                // startBit = 4, endBit = 7
                // value = 000xxxxx
                // want  = 0000xxxx x0000000
                // The bits we want span 2 mBytes.
                // Bit-shift "value" into "byte0" and "byte1".
                int byte0 = value >>> (length - startBit);
                int byte1 = value << endBit;
                // The mask is to zero out the bits we want overwritten.
                int mask0 = 0xff >>> (8 - startBit);
                int mask1 = ~(0xff >>> (8 - endBit));
                // Zero out the bits that we want overwritten and add "value" on.
                mBytes[startByte] = (byte) (mBytes[startByte] & ~mask0 | byte0);
                mBytes[startByte + 1] = (byte) (mBytes[startByte + 1] & ~mask1 | byte1);
            }
        }

        private void testGet(int length, int expected) {
            int actual = get(length);
            Log.d(TAG, "get start=" + mBytePtr + " length=" + length +
                    " expected=" + expected + " actual=" + actual +
                    (expected == actual ? " ✔" : ""));
        }

        /**
         * Get the next 6-bit (between 0 and 63) color off the stack.
         *
         * @return Next 6-bit color
         */
        int getSixBitColor() {
            // Return our 6-bit color (between 0 and 63).
            return get(6);
        }

        public boolean getBoolean() {
            return get(1) == 1;
        }

        int get(int length) {
            int result = get(length, mBytePtr);
            mBytePtr += length;
            return result;
        }

        private int get(int length, int start) {
            // Cap our length to [0, 8].
            if (length < 0) {
                length = 0;
            } else if (length > 8) {
                length = 8;
            }

            // Cap our start to [0, mBytes.length * 8 - length].
            if (start < 0) {
                start = 0;
            } else if (start + length > mBytes.length * 8) {
                start = mBytes.length * 8 - length;
            }

            int startBit = 8 - (start % 8);
            int endBit = 8 - ((start + length) % 8);
            int startByte = (start - (start % 8)) / 8;
            if (startBit > endBit) {
                // length = 5, start = 2
                // startBit = 6, endBit = 1
                // value = 00xxxxx0
                // want  = 000xxxxx
                // The bits we want are in a single byte.
                // Bit-shift the unnecessary right-hand bits off the end.
                // Then zero the unnecessary left-hand bits using AND.
                return (mBytes[startByte] >>> endBit) & (0xff >>> (8 - length));
            } else {
                // length = 5, start = 6
                // startBit = 2, endBit = 5
                // value = 000000xx xxx00000
                // want  = 000xx000 00000xxx
                // length = 5, start = 4
                // startBit = 4, endBit = 7
                // value = 0000xxxx x0000000
                // want  = 000xxxx0 0000000x
                int existing0 = mBytes[startByte];
                existing0 += existing0 > 0 ? 0 : 256;
                int existing1 = mBytes[startByte + 1];
                existing1 += existing1 > 0 ? 0 : 256;
                // The bits we want span 2 mBytes.
                // Get the first byte.
                // Zero the unnecessary left-hand bits using AND.
                // Then bit-shift the unnecessary left-hand bits off the end,
                // zero-filling at the right-hand bits. (They'll be added with byte2.)
                int byte0 = (existing0 & (0xff >>> (8 - startBit))) << (length - startBit);
                // Get the second byte.
                // Bit-shift the unnecessary right-hand bits off the end.
                int byte1 = (existing1 >>> endBit) & (0xff >>> endBit);
                return byte0 + byte1;
            }
        }
    }
}