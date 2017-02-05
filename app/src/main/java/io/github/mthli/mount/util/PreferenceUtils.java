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

package io.github.mthli.mount.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import io.github.mthli.mount.R;

public class PreferenceUtils {
    public static void register(Context context, SharedPreferences.OnSharedPreferenceChangeListener listener) {
        PreferenceManager.getDefaultSharedPreferences(context).registerOnSharedPreferenceChangeListener(listener);
    }

    public static void unregister(Context context, SharedPreferences.OnSharedPreferenceChangeListener listener) {
        PreferenceManager.getDefaultSharedPreferences(context).unregisterOnSharedPreferenceChangeListener(listener);
    }

    public static boolean isNotificationQuickAction(Context context) {
        String key = context.getString(R.string.preference_key_notification_quick_action);
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(key, false);
    }

    public static boolean isShowSystemApps(Context context) {
        String key = context.getString(R.string.preference_key_show_system_apps);
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(key, false);
    }
}
