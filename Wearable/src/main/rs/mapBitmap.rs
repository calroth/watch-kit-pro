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
