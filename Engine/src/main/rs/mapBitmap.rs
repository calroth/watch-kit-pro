/*
 * Copyright (C) 2019-2024 Terence Tan
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

// Less-precise, marginally faster floating point, since we're only using it for display purposes.
#pragma rs_fp_relaxed

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

// Our Oklab colors to interpolate between to generate a palette.
float3 oklabColorA, oklabColorB, oklabDynMin, oklabDynMax;
float oklabDynRange;
uchar4 black;

void prepareOklabPalette(float ar, float ag, float ab, float br, float bg, float bb, float d) {
    oklabColorA.r = ar;
    oklabColorA.g = ag;
    oklabColorA.b = ab;
    oklabColorB.r = br;
    oklabColorB.g = bg;
    oklabColorB.b = bb;

    // For Oklab, modify our lightness dynamic range from 0..100 down to 0..4.6.
    d = d / 100.0f * 4.641596888f;

    // Parameter "d" is the dynamic range which we clamp to.
    // Between 0.0f and 50.0f. Lower = more range.
    oklabDynMin.r = d;
    oklabDynMin.g = -999.0f;
    oklabDynMin.b = -999.0f;
    oklabDynMax.r = 4.641596888f - d;
    oklabDynMax.g = 999.0f;
    oklabDynMax.b = 999.0f;
    oklabDynRange = d;
    black.r = 0;
    black.g = 0;
    black.b = 0;
    black.a = 255;
}

#define OKLAB_PALETTE_W 63.0f
#define OKLAB_PALETTE_H 31.0f

#define OKLAB_PALETTE_W2 (OKLAB_PALETTE_W / 2.0f)
#define OKLAB_PALETTE_H2 (OKLAB_PALETTE_H / 2.0f)

uchar4 RS_KERNEL generateOklabPalette(uint32_t x, uint32_t y) {
    float3 color = clamp(mix(oklabColorB, oklabColorA, x / OKLAB_PALETTE_W), oklabDynMin, oklabDynMax);
    float lightnessModifier = ((float)y - OKLAB_PALETTE_H2) / OKLAB_PALETTE_H2; // Clamp to [-1,1].
    float3 lightness; // A color that's either pure black or pure white
    lightness.x = lightnessModifier < 0.0f ? 0.0f : 100.0f;
    lightness.y = lightnessModifier < 0.0f ? 0.0f : 0.0001f;
    lightness.z = lightnessModifier < 0.0f ? 0.0f : 0.0001f;
    // Mix towards either pure black or pure white, with magnitude of oklabDynRange.
    color = mix(color, lightness, fabs(lightnessModifier) * oklabDynRange * 0.01f);

    if (color.x <= 0.0f) { return black; } // Avoid dividing by zero...

    float l = pow(0.9999999984f * color.x + 0.3963377922f * color.y + 0.2158037581f * color.z, 3.0f);
    float m = pow(1.0000000089f * color.x - 0.1055613423f * color.y - 0.0638541748f * color.z, 3.0f);
    float s = pow(1.0000000547f * color.x - 0.0894841821f * color.y - 1.2914855379f * color.z, 3.0f);

    float X =  1.2270138511f * l - 0.5577999807f * m + 0.2812561490f * s;
    float Y = -0.0405801784f * l + 1.1122568696f * m - 0.0716766787f * s;
    float Z = -0.0763812845f * l - 0.4214819784f * m + 1.5861632204f * s;

    // Convert XYZ to sRGB...
    float var_x = X / 100.0f;
    float var_y = Y / 100.0f;
    float var_z = Z / 100.0f;

    float4 var_RGB;
    var_RGB.r = var_x * 3.2406f + var_y * -1.5372f + var_z * -0.4986f;
    var_RGB.g = var_x * -0.9689f + var_y * 1.8758f + var_z * 0.0415f;
    var_RGB.b = var_x * 0.0557f + var_y * -0.2040f + var_z * 1.0570f;
    var_RGB.a = 1.0f; // Note to self, this could probably be a matrix multiply.

    var_RGB.r = var_RGB.r > 0.0031308f ?
       1.055f * pow(var_RGB.r, 1.0f / 2.4f) - 0.055f : 12.92f * var_RGB.r;
    var_RGB.g = var_RGB.g > 0.0031308f ?
       1.055f * pow(var_RGB.g, 1.0f / 2.4f) - 0.055f : 12.92f * var_RGB.g;
    var_RGB.b = var_RGB.b > 0.0031308f ?
       1.055f * pow(var_RGB.b, 1.0f / 2.4f) - 0.055f : 12.92f * var_RGB.b;

    // Clamp the final RGB values to [0, 1] and scale to [0, 255].
    var_RGB = clamp(var_RGB, 0.0f, 1.0f) * 255.0f;
    var_RGB.a = 255.0f;

    // And return.
    return convert_uchar4(var_RGB);
}

rs_allocation oklabPalette;

void prepareOklabTransform(rs_allocation palette) {
    oklabPalette = palette;
}

uchar4 RS_KERNEL generateOklabTransform(uchar4 gradient, uchar4 texture) {
    return rsGetElementAt_uchar4(oklabPalette, gradient.r / 4, texture.r / 8);
}

uchar4 RS_KERNEL generateOklabTransformAndSparkle(uchar4 gradient, uint32_t x, uint32_t y) {
    uchar4 in = rsGetElementAt_uchar4(oklabPalette, gradient.r / 4, 128 / 8);

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
