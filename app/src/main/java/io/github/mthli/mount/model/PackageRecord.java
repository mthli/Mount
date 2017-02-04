/*
 * App Mount, mount/umount apps that you don't like on Android, without root.
 * Copyright (C) 2017 Matthew Lee
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.github.mthli.mount.model;

import com.orm.SugarRecord;
import com.orm.dsl.Unique;

public class PackageRecord extends SugarRecord {
    @Unique
    public String name;
    public byte[] icon;
    public String label;
    public String version;
    public boolean umount;

    public PackageRecord() {}

    public PackageRecord(String name, byte[] icon, String label, String version, boolean umount) {
        this.name = name;
        this.icon = icon;
        this.label = label;
        this.version = version;
        this.umount = umount;
    }
}
