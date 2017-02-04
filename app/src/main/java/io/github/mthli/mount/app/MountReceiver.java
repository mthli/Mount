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

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import java.util.List;

import io.github.mthli.mount.model.PackageRecord;
import io.github.mthli.mount.util.IntentUtils;
import io.github.mthli.mount.util.PolicyUtils;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class MountReceiver extends DeviceAdminReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        if (TextUtils.equals(intent.getAction(), Intent.ACTION_BOOT_COMPLETED)) {
            onBootCompleted(context);
        } else if (TextUtils.equals(intent.getAction(), Intent.ACTION_PACKAGE_FULLY_REMOVED)) {
            onActionPackageFullyRemoved(intent);
        } else if (TextUtils.equals(intent.getAction(), Intent.ACTION_SHUTDOWN)) {
            onActionShutdown(context);
        }
    }

    private void onBootCompleted(final Context context) {
        PolicyUtils.umountAll(context, new Consumer<Boolean>() {
            @Override
            public void accept(Boolean aBoolean) throws Exception {
                IntentUtils.startCocaService(context);
            }
        });
    }

    private void onActionPackageFullyRemoved(final Intent intent) {
        Observable.create(
                new ObservableOnSubscribe<Boolean>() {
                    @Override
                    public void subscribe(ObservableEmitter<Boolean> e) throws Exception {
                        // prefix "package:"
                        String packageName = intent.getData().toString().substring(8);
                        List<PackageRecord> list = PackageRecord.listAll(PackageRecord.class);
                        for (PackageRecord record : list) {
                            if (TextUtils.equals(record.name, packageName)) {
                                record.delete();
                            }
                        }

                        e.onNext(true);
                        e.onComplete();
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .subscribe();
    }

    private void onActionShutdown(Context context) {
        PolicyUtils.umountAll(context, null);
    }
}
