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
 *
 */

package pro.watchkit.wearable.watchface.model;

import java.util.Collection;
import java.util.GregorianCalendar;

public class WatchFaceState {
    public WatchFacePreset preset;
    public PaintBox paintBox;
    public Collection<ComplicationHolder> complications;
    public int unreadNotifications;
    public int totalNotifications;
    public boolean ambient;
    public GregorianCalendar mCalendar = new GregorianCalendar();
    public LocationCalculator mLocationCalculator = new LocationCalculator(mCalendar);
}
