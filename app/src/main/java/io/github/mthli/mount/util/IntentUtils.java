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

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.Settings;

import io.github.mthli.mount.app.MountActivity;
import io.github.mthli.mount.app.MountService;
import io.github.mthli.mount.app.MainActivity;
import io.github.mthli.mount.app.SettingsActivity;

public class IntentUtils {
    public static final String SCHEMA_COCA = "coca://";
    public static final String SCHEMA_COCA_NOTIFICATION = SCHEMA_COCA + "notification/";
    public static final String SCHEMA_COCA_SHORTCUT = SCHEMA_COCA + "shortcut/";

    public static PendingIntent createNotificationIntent(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setData(Uri.parse(IntentUtils.SCHEMA_COCA_NOTIFICATION));
        return PendingIntent.getActivity(context, 0, intent, 0);
    }

    public static void createShortcut(Context context, String packageName, Bitmap icon, String label) {
        Intent shortcutIntent = new Intent(context, MainActivity.class);
        shortcutIntent.setData(Uri.parse(SCHEMA_COCA_SHORTCUT + packageName));

        Intent addIntent = new Intent();
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON, icon);
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, label);
        addIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
        context.sendBroadcast(addIntent);
    }

    public static void runApplication(Context context, String packageName) {
        if (!PolicyUtils.isApplicationMount(context, packageName)) {
            PolicyUtils.setApplicationMount(context, packageName, true);
        }

        try {
            Intent intent = context.getPackageManager().getLaunchIntentForPackage(packageName);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void showApplicationInfo(Context context, String packageName) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + packageName));
        context.startActivity(intent);
    }

    public static void showUsageStatsAccess(Context context) {
        context.startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
    }

    public static void showNotificationAccess(Context context) {
        context.startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
    }

    public static void showVersion(Context context) {
        context.startActivity(new Intent(Intent.ACTION_VIEW,
                Uri.parse("https://github.com/mthli/Mount/releases")));
    }

    public static void showTutorial(Context context) {
        context.startActivity(new Intent(Intent.ACTION_VIEW,
                Uri.parse("https://github.com/mthli/Mount/blob/master/README.md")));
    }

    public static void startCocaActivity(Context context) {
        context.startActivity(new Intent(context, MountActivity.class));
    }

    public static void startCocaService(Context context) {
        context.startService(new Intent(context, MountService.class));
    }

    public static void startSettingsActivity(Context context) {
        context.startActivity(new Intent(context, SettingsActivity.class));
    }
}
