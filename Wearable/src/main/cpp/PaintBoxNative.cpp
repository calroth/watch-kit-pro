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
 */

#include <jni.h>
#include <android/bitmap.h>

extern "C" {

JNIEXPORT jint JNICALL
Java_pro_watchkit_wearable_watchface_model_PaintBox_nativeMapBitmap(
        JNIEnv *env, jobject, jobject bitmap, jintArray cLUT) {
    jint *bitmapPtr;
//    jint cLUTLength;
    jint *cLUTPtr;
    AndroidBitmapInfo info;
    int err;

    err = AndroidBitmap_getInfo(env, bitmap, &info);
    if (err < 0) {
        // Can't get info; return.
        return NULL;
    }

    if (info.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
        // Bitmap format isn't right; return.
        return NULL;
    }

    err = AndroidBitmap_lockPixels(env, bitmap, (void **) &bitmapPtr);
    if (err < 0) {
        // Can't lock the bitmap; return.
        return NULL;
    }

//    cLUTLength = env->GetArrayLength(cLUT);
    cLUTPtr = env->GetIntArrayElements(cLUT, NULL);

    for (int i = 0; i < info.width * info.height; i++) {
        bitmapPtr[i] = cLUTPtr[bitmapPtr[i] & 0xFF];
    }

    AndroidBitmap_unlockPixels(env, bitmap);

    return NULL;
}

}
