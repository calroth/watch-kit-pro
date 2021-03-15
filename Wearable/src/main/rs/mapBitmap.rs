/*
 * Copyright (C) 2019-21 Terence Tan
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

#pragma version(1)
#pragma rs java_package_name(pro.watchkit.wearable.watchface.model)

int32_t mapping[256];
uchar4 mapping2[256];

void convertMapping(void) {
    for (int i = 0; i < 256; i++) {
        mapping2[i].w = (mapping[i] >> 24) & 0xff;
        mapping2[i].x = (mapping[i] >> 16) & 0xff;
        mapping2[i].y = (mapping[i] >> 8) & 0xff;
        mapping2[i].z = mapping[i] & 0xff;
    }
}

uchar4 RS_KERNEL mapBitmap(uchar4 in) {
    return mapping2[in.b];
}

// Simple fast PRNG for RenderScript -- https://stackoverflow.com/a/28117959
uint32_t r0 = 0x6635e5ce, r1 = 0x13bf026f, r2 = 0x43225b59, r3 = 0x3b0314d0;

// For each pixel, we calculate the amount of sparkle by the lesser of
// (pixel's distance to 0) and (pixel's distance to 255).
// Then we lessen it further by bitshifting right by "SPARKLE_MAGNITUDE" bits.
// 0 = full sparkle
// 1 = half sparkle
// 2 = quarter sparkle
#define SPARKLE_MAGNITUDE 1

uchar4 sparkleMappingA[256]; // Big negative sparkle
uchar4 sparkleMappingB[256]; // Moderate negative sparkle
uchar4 sparkleMappingC[256]; // Small negative sparkle
uchar4 sparkleMappingD[256]; // Small positive sparkle
uchar4 sparkleMappingE[256]; // Moderate positive sparkle
uchar4 sparkleMappingF[256]; // Big positive sparkle

uchar4 RS_KERNEL sparkle(uchar4 in, uint32_t x, uint32_t y) {
    // Simple fast PRNG for RenderScript -- https://stackoverflow.com/a/28117959
    uint32_t t = r0 ^ (r0 << 11);
    t = t + (x << 23) + (y << 17); // Mix "x" and "y" into the output too, because why not.
    r0 = r1; r1 = r2; r2 = r3;
    r3 = r3 ^ (r3 >> 19) ^ t ^ (t >> 8);

    uchar4 * sparkleMapping;

    // Calculate the amount of sparkle for this pixel
    uchar4 min4 = min((255 - in), in);
    if (r3 % 17 == 0) {
        // 1 in 17 chance we have a big sparkle
        sparkleMapping = r3 % 2 ? sparkleMappingA : sparkleMappingF;
    } else if (r3 % 11 == 0) {
        // 1 in 11 chance (less chance above) we have a moderate sparkle
        sparkleMapping = r3 % 2 ? sparkleMappingB : sparkleMappingE;
    } else if (r3 % 5 == 0) {
        // 1 in 5 chance (less chance above) we have a small sparkle
        sparkleMapping = r3 % 2 ? sparkleMappingC : sparkleMappingD;
    } else {
        // No sparkle, return early.
        return in;
    }

    // Apply the random sparkle to either the r, g, or b channel as needed.
    uchar4 result;
    if (r3 % 13 < 7) {
        // 2 in 13 chance we highlight three channels.
        // Highlight/lowlight three channels!
        // Use element "z" in sparkleMapping, which is pre-divided by 3.
        // Because we're spreading the sparkle amongst 3 channels.
        result.r = sparkleMapping[in.r].z;
        result.g = sparkleMapping[in.g].z;
        result.b = sparkleMapping[in.b].z;
    } else if (r3 % 13 < 11) {
        // 4 in 13 chance we highlight two channels.
        // Highlight/lowlight two channels!
        // Use element "y" in sparkleMapping, which is pre-divided by 2.
        // Because we're spreading the sparkle amongst 2 channels.
        if (r3 % 3 == 0) {
            result.r = sparkleMapping[in.r].y;
            result.g = sparkleMapping[in.g].y;
            result.b = in.b;
        } else if (r3 % 3 == 1) {
            result.r = in.r;
            result.g = sparkleMapping[in.g].y;
            result.b = sparkleMapping[in.b].y;
        } else {
            result.r = sparkleMapping[in.r].y;
            result.g = in.g;
            result.b = sparkleMapping[in.b].y;
        }
    } else {
        // 7 in 13 chance we highlight one channel.
        // Highlight/lowlight an individual r, g, or b channel!
        // Use element "x" in sparkleMapping, which is pre-divided by 1.
        if (r3 % 3 == 0) {
            result.r = sparkleMapping[in.r].x;
            result.g = in.g;
            result.b = in.b;
        } else if (r3 % 3 == 1) {
            result.r = in.r;
            result.g = sparkleMapping[in.g].x;
            result.b = in.b;
        } else {
            result.r = in.r;
            result.g = in.g;
            result.b = sparkleMapping[in.b].x;
        }
    }

    // Alpha channel unchanged.
    result.a = in.a;

    return result;
}
