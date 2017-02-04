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

import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.text.TextUtils;

import io.github.mthli.mount.R;
import io.github.mthli.mount.util.IntentUtils;
import io.github.mthli.mount.util.PolicyUtils;
import io.github.mthli.mount.util.PreferenceUtils;

public class MountService extends Service implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final int NOTIFICATION_ID = 0x01;

    private BroadcastReceiver mScreenOffReceiver;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        mScreenOffReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (TextUtils.equals(intent.getAction(), Intent.ACTION_SCREEN_OFF)) {
                    PolicyUtils.umountAll(MountService.this, null);
                }
            }
        };

        registerReceiver(mScreenOffReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
        PreferenceUtils.register(this, this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        updateForeground();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(mScreenOffReceiver);
        PreferenceUtils.unregister(this, this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (TextUtils.equals(key, getString(R.string.preference_key_notification_quick_action))) {
            updateForeground();
        }
    }

    private void updateForeground() {
        if (!PolicyUtils.isDeviceOwnerApp(this) || !PreferenceUtils.isNotificationQuickAction(this)) {
            stopForeground(true);
            return;
        }

        Notification notification = new Notification.Builder(this)
                .setAutoCancel(false)
                .setPriority(Notification.PRIORITY_MIN)
                .setSmallIcon(R.drawable.ic_notification_android)
                .setContentTitle(getString(R.string.mount_service_notification_title))
                .setContentText(getString(R.string.mount_service_notification_text))
                .setContentIntent(IntentUtils.createNotificationIntent(this))
                .build();
        startForeground(NOTIFICATION_ID, notification);
    }
}
