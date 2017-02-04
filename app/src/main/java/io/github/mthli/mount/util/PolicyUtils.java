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

import android.app.admin.DevicePolicyManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.text.TextUtils;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import io.github.mthli.mount.app.MountApplication;
import io.github.mthli.mount.app.MountReceiver;
import io.github.mthli.mount.model.PackageRecord;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class PolicyUtils {
    public static boolean isDeviceOwnerApp(Context context) {
        DevicePolicyManager manager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        return manager.isDeviceOwnerApp(context.getPackageName());
    }

    public static void clearDeviceOwnerApp(Context context) {
        try {
            DevicePolicyManager manager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
            manager.clearDeviceOwnerApp(context.getPackageName());
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    // http://stackoverflow.com/a/14665381/4696820
    public static boolean isSystemApp(ApplicationInfo info) {
        int mask = ApplicationInfo.FLAG_SYSTEM | ApplicationInfo.FLAG_UPDATED_SYSTEM_APP;
        return (info.flags & mask) != 0;
    }

    public static boolean isApplicationMount(Context context, String packageName) {
        DevicePolicyManager manager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        return !manager.isApplicationHidden(new ComponentName(context, MountReceiver.class), packageName);
    }

    public static void setApplicationMount(Context context, String packageName, boolean mount) {
        DevicePolicyManager manager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName admin = new ComponentName(context, MountReceiver.class);
        manager.setApplicationHidden(admin, packageName, !mount);
        manager.setUninstallBlocked(admin, packageName, !mount);
    }

    public static void mountAll(final Context context, final boolean temporary, Consumer<Boolean> consumer) {
        if (consumer == null) {
            consumer = new Consumer<Boolean>() {
                @Override
                public void accept(Boolean aBoolean) throws Exception {
                    // DO NOTHING
                }
            };
        }

        Observable.create(
                new ObservableOnSubscribe<Boolean>() {
                    @Override
                    public void subscribe(ObservableEmitter<Boolean> e) throws Exception {
                        List<PackageRecord> list = PackageRecord.listAll(PackageRecord.class);
                        for (PackageRecord record : list) {
                            setApplicationMount(context, record.name, true);
                            if (!temporary) {
                                record.delete();
                            }
                        }

                        e.onNext(true);
                        e.onComplete();
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(consumer);
    }

    public static void umountAll(final Context context, Consumer<Boolean> consumer) {
        if (consumer == null) {
            consumer = new Consumer<Boolean>() {
                @Override
                public void accept(Boolean aBoolean) throws Exception {
                    // DO NOTHING
                }
            };
        }

        final String foregroundPackageName = getForegroundAppPackageName(context);
        final List<String> foregroundServiceList = getForegroundServiceList(context);
        Observable.create(
                new ObservableOnSubscribe<Boolean>() {
                    @Override
                    public void subscribe(ObservableEmitter<Boolean> e) throws Exception {
                        List<PackageRecord> list = PackageRecord.listAll(PackageRecord.class);
                        for (PackageRecord record : list) {
                            if (!TextUtils.equals(record.name, foregroundPackageName)
                                    && !foregroundServiceList.contains(record.name)) {
                                setApplicationMount(context, record.name, false);
                            }
                        }

                        e.onNext(true);
                        e.onComplete();
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(consumer);
    }

    @SuppressWarnings("WrongConstant")
    public static String getForegroundAppPackageName(Context context) {
        UsageStatsManager manager = (UsageStatsManager) context.getSystemService("usagestats");
        long time = System.currentTimeMillis();
        List<UsageStats> list = manager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY,
                time - 1000 * 1000, time);
        if (list != null && !list.isEmpty()) {
            SortedMap<Long, UsageStats> map = new TreeMap<>();
            for (UsageStats stats : list) {
                map.put(stats.getLastTimeUsed(), stats);
            }

            if (!map.isEmpty()) {
                return map.get(map.lastKey()).getPackageName();
            }
        }

        return null;
    }

    public static List<String> getForegroundServiceList(Context context) {
        return ((MountApplication) context.getApplicationContext()).getForegroundServiceList();
    }
}
