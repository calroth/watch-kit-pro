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

#pragma version(1)
#pragma rs java_package_name(pro.watchkit.wearable.watchface.model)

int32_t mapping[256];
uchar4 mapping2[256];

//void setMapping(int index, int32_t m) {
//    mapping[index].w = (m >> 24) & 0xff;
//    mapping[index].x = (m >> 16) & 0xff;
//    mapping[index].y = (m >> 8) & 0xff;
//    mapping[index].z = m & 0xff;
//}

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

// For each pixel, we calculate the amount of sparkle by the lesser of
// (pixel's distance to 0) and (pixel's distance to 255).
// Then we lessen it further by bitshifting right by "SPARKLE_MAGNITUDE" bits.
// 0 = full sparkle
// 1 = half sparkle
// 2 = quarter sparkle
#define SPARKLE_MAGNITUDE 0

uchar4 RS_KERNEL sparkle(uchar4 in) {
    uchar4 result;
    // Random sparkle. 0-5: sparkle. 6-35: no sparkle.
    int rand = rsRand(36);
    // Calculate the amount of sparkle for this pixel
    uchar4 min4 = min((255 - in) >> SPARKLE_MAGNITUDE, in >> SPARKLE_MAGNITUDE);
    // Alpha channel unchanged.
    result.a = in.a;
    // Apply the random sparkle to either the r, g, or b channel as needed,
    // and as a highlight (add) or lowlight (subtract) as needed.
    result.r = rand == 0 ? in.r - min4.r : (rand == 1 ? in.r + min4.r : in.r);
    result.g = rand == 2 ? in.g - min4.g : (rand == 3 ? in.g + min4.g : in.g);
    result.b = rand == 4 ? in.b - min4.b : (rand == 5 ? in.b + min4.b : in.b);
    return result;
}
