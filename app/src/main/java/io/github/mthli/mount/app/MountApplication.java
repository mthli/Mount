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

package io.github.mthli.mount.app;

import android.app.Application;
import android.app.Notification;
import android.service.notification.StatusBarNotification;

import com.orm.SugarContext;

import java.util.ArrayList;
import java.util.List;

public class MountApplication extends Application {
    private NotificationService mNotificationService;

    @Override
    public void onCreate() {
        super.onCreate();
        SugarContext.init(this);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        SugarContext.terminate();
    }

    public void registerNotificationService(NotificationService service) {
        mNotificationService = service;
    }

    public void unregisterNotificationService() {
        mNotificationService = null;
    }

    public List<String> getForegroundServiceList() {
        List<String> list = new ArrayList<>();

        if (mNotificationService != null) {
            StatusBarNotification[] notifications = mNotificationService.getActiveNotifications();
            for (StatusBarNotification notification : notifications) {
                int mask = Notification.FLAG_FOREGROUND_SERVICE;
                if ((notification.getNotification().flags & mask) != 0) {
                    list.add(notification.getPackageName());
                }
            }
        }

        return list;
    }
}
