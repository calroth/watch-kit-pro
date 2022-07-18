/*
 * Copyright (C) 2019-2022 Terence Tan
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

// Reference constants "ref_U" and "ref_V" for the CIELUV colorspace conversions.
float ref_U, ref_V;
// Our LUV colors to interpolate between to generate a palette.
float3 luvColorA, luvColorB;

void prepareLuvPalette(float ar, float ag, float ab, float br, float bg, float bb) {
    // Reference constants for the CIELUV colorspace conversions. D65 illuminant, 2 degrees.
    float Reference_X = 95.047f, Reference_Y = 100.000f, Reference_Z = 108.883f;

    // Reference constant "ref_U" for the CIELUV colorspace conversions.
    ref_U = (4.0f * Reference_X) / (Reference_X + (15.0f * Reference_Y) + (3.0f * Reference_Z));

    // Reference constant "ref_V" for the CIELUV colorspace conversions.
    ref_V = (9.0f * Reference_Y) / (Reference_X + (15.0f * Reference_Y) + (3.0f * Reference_Z));

    luvColorA.r = ar;
    luvColorA.g = ag;
    luvColorA.b = ab;
    luvColorB.r = br;
    luvColorB.g = bg;
    luvColorB.b = bb;
}

uchar4 RS_KERNEL generateLuvPalette(uint32_t x, uint32_t y) {
    float3 color = mix(luvColorB, luvColorA, x / 63.0f);

    float lightnessModifier = ((float)y - 15.75f) / 15.75f; // Clamp to [-1,1].
    float3 lightness;
    lightness.x = lightnessModifier < 0.0f ? 0.0001f : 100.0f;
    lightness.y = lightnessModifier < 0.0f ? 0.0001f : 0.0001f;
    lightness.z = lightnessModifier < 0.0f ? 0.0001f : 0.0001f; // Mix towards black or white.
    color = mix(color, lightness, fabs(lightnessModifier) * 0.15f);

    float var_y = (color.x + 16.0f) / 116.0f;
    var_y = var_y > (6.0f / 29.0f) ?
       pown(var_y, 3) : ((var_y - 16.0f / 116.0f) * 3132.0f / 24389.0f);

    float var_U = color.y / (13.0f * color.x) + ref_U;
    float var_V = color.z / (13.0f * color.x) + ref_V;

    float Y = var_y * 100.0f;
    float X = 0.0f - (9.0f * Y * var_U) / ((var_U - 4.0f) * var_V - var_U * var_V);
    float Z = (9.0f * Y - (15.0f * var_V * Y) - (var_V * X)) / (3.0f * var_V);

    // Convert XYZ to sRGB...
    float var_x = X / 100.0f;
    //var_y = Y / 100.0f;
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

rs_allocation luvPalette;

void prepareLuvTransform(rs_allocation palette) {
    luvPalette = palette;
}

uchar4 RS_KERNEL generateLuvTransform(uchar4 gradient, uchar4 texture) {
    return rsGetElementAt_uchar4(luvPalette, gradient.r / 4, texture.r / 8);
}

uchar4 RS_KERNEL generateLuvTransformAndSparkle(uchar4 gradient, uint32_t x, uint32_t y) {
    uchar4 in = rsGetElementAt_uchar4(luvPalette, gradient.r / 4, 128 / 8);

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
