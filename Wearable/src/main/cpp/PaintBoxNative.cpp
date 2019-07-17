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
 */

#include <jni.h>
#include <android/bitmap.h>

constexpr int CLUT_SIZE = 0xFF;

extern "C"
JNIEXPORT void JNICALL Java_pro_watchkit_wearable_watchface_model_PaintBox_nativeMapBitmap(
        JNIEnv *env, jobject, jobject bitmap, jintArray cLUT) {
    jint *bitmapPtr;
    jint *cLUTPtr;
    AndroidBitmapInfo info;
    int err;

    err = AndroidBitmap_getInfo(env, bitmap, &info);
    if (err < 0) {
        // Can't get info; return.
        return;
    }

    if (info.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
        // Bitmap format isn't right; return.
        return;
    }

    if (env->GetArrayLength(cLUT) <= CLUT_SIZE) {
        // Need a cLUT of at least 256 elements; return.
        return;
    }

    err = AndroidBitmap_lockPixels(env, bitmap, (void **) &bitmapPtr);
    if (err < 0) {
        // Can't lock the bitmap; return.
        return;
    }

    cLUTPtr = env->GetIntArrayElements(cLUT, NULL);

    // For whatever reason, in NDK the red and blue channels are swapped
    // (i.e. it expects colors of form ABGR rather than ARGB).
    // Swap everything in the cLUT...
    for (int i = 0; i <= CLUT_SIZE; i++) {
        int a = cLUTPtr[i] >> 24 & CLUT_SIZE;
        int r = cLUTPtr[i] >> 16 & CLUT_SIZE;
        int g = cLUTPtr[i] >> 8 & CLUT_SIZE;
        int b = cLUTPtr[i] & CLUT_SIZE;
        cLUTPtr[i] = (a << 24) + (b << 16) + (g << 8) + r;
    }

    for (int i = 0; i < info.width * info.height; i++) {
        bitmapPtr[i] = cLUTPtr[(bitmapPtr[i] & CLUT_SIZE)];
    }

    // Release the array. JNI_ABORT means don't copy the changed elements back.
    env->ReleaseIntArrayElements(cLUT, cLUTPtr, JNI_ABORT);

    AndroidBitmap_unlockPixels(env, bitmap);
}
