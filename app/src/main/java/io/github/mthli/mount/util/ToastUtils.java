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
import android.widget.Toast;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

public class ToastUtils {
    private static final long SHORT_DELAY = 2000L; // ms; Toast.LENGTH_SHORT
    private static final long LONG_DELAY = 3500L; // ms; Toast.LENGTH_LONG

    private static Toast sToast;
    private static Disposable sDisposable;

    public static void showWithShortTime(Context context, int stringResId) {
        showWithShortTime(context, context.getString(stringResId));
    }

    public static void showWithShortTime(Context context, String text) {
        show(context, text, false);
    }

    public static void showWithLongTime(Context context, int stringResId) {
        showWithLongTime(context, context.getString(stringResId));
    }

    public static void showWithLongTime(Context context, String text) {
        show(context, text, true);
    }

    private static void show(Context context, String text, boolean isLong) {
        RxUtils.disposeSafety(sDisposable);

        if (sToast != null) {
            sToast.setText(text);
        } else {
            sToast = Toast.makeText(context, text, !isLong ? Toast.LENGTH_SHORT : Toast.LENGTH_LONG);
        }

        sToast.show();
        sDisposable = Observable.timer(!isLong ? SHORT_DELAY : LONG_DELAY, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        sToast.cancel();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        throwable.printStackTrace();
                    }
                });
    }
}
