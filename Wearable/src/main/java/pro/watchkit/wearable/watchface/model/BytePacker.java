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

import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import androidx.annotation.NonNull;

/**
 * A class that packs variable-length ints into a stream of mBytes.
 * We use this to fit all our config data into a single 128-bit
 * binary array.
 * <p>
 * We represent this data as 16-hex-digit strings that are reversibly
 * hashed for transport and packing.
 */
final class BytePacker {
    private byte[] mBytes;
    private int mBytePtr;

    private SecretKeySpec mKey;
    private Cipher mCipher;

//    BytePacker(@NonNull String s, @NonNull byte[] key) {
//        setKey(key);
//        setString(s);
//        rewind();
//    }
//
//    BytePacker(String s) {
//        setKey(getDefaultKey());
//        setString(s);
//        rewind();
//    }
//
//    BytePacker(int lengthInBytes, @NonNull byte[] key) {
//        setKey(key);
//        mBytes = new byte[lengthInBytes];
//        rewind();
//    }

    BytePacker(int lengthInBytes) {
        setKey(getDefaultKey());
        mBytes = new byte[lengthInBytes];
        rewind();
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
        } catch (NoSuchAlgorithmException | NoSuchPaddingException ex) {
            String TAG = "AnalogWatchFace";
            Log.d(TAG, "setKey: " + ex.toString());
        }
    }

    @NonNull
    String getStringFast() {
        return byteArrayToString(mBytes);
    }

    void setStringFast(String s) {
        int length = s.length();
        mBytes = new byte[length / 2];
        for (int i = 0; i < length; i += 2) {
            mBytes[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
    }

    @NonNull
    public String getString() {
        String TAG = "AnalogWatchFace";
//        Log.d(TAG, "Unencrypted: " + byteArrayToString(mBytes));
        try {
            mCipher.init(Cipher.ENCRYPT_MODE, mKey);
            //            Log.d(TAG, "Encrypted: " + result);
            return byteArrayToString(mCipher.doFinal(mBytes));
        } catch (IllegalBlockSizeException | BadPaddingException | InvalidKeyException ex) {
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

        String TAG = "AnalogWatchFace";
//        Log.d(TAG, "Encrypted: " + byteArrayToString(encrypted));

        try {
            mCipher.init(Cipher.DECRYPT_MODE, mKey);
            mBytes = mCipher.doFinal(encrypted);
//            Log.d(TAG, "Decrypted: " + byteArrayToString(mBytes));
        } catch (IllegalBlockSizeException | BadPaddingException | InvalidKeyException ex) {
            Log.d(TAG, "setString: " + ex.toString());
        }

        rewind();
    }

    @NonNull
    private String byteArrayToString(byte[] a) {
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

    void put(int length, Object[] values, Object value) {
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

    private void put(int length, int value) {
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
        String TAG = "AnalogWatchFaceZ";
        Log.d(TAG, "get start=" + mBytePtr + " length=" + length +
                " expected=" + expected + " actual=" + actual +
                (expected == actual ? " ✔" : ""));
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
