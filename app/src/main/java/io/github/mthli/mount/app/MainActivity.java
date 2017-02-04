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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import io.github.mthli.mount.R;
import io.github.mthli.mount.util.IntentUtils;
import io.github.mthli.mount.util.PolicyUtils;
import io.github.mthli.mount.util.ToastUtils;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        IntentUtils.startCocaService(this);

        if (getIntent() == null || getIntent().getData() == null
                || TextUtils.isEmpty(getIntent().getData().toString())
                || !PolicyUtils.isDeviceOwnerApp(this)) {
            IntentUtils.startCocaActivity(this);
            finish();
            return;
        }

        if (TextUtils.equals(getIntent().getAction(), Intent.ACTION_MAIN)
                && (getIntent().hasCategory(Intent.CATEGORY_LAUNCHER)
                || getIntent().hasCategory(Intent.CATEGORY_LEANBACK_LAUNCHER))) {
            IntentUtils.startCocaActivity(this);
            finish();
            return;
        }

        // deal with schema
        String data = getIntent().getData().toString();
        if (TextUtils.equals(data, IntentUtils.SCHEMA_COCA_NOTIFICATION)) {
            PolicyUtils.umountAll(this, null);
            ToastUtils.showWithShortTime(this, R.string.toast_umount_all_ok);
        } else if (data.startsWith(IntentUtils.SCHEMA_COCA_SHORTCUT)) {
            String packageName = data.substring(IntentUtils.SCHEMA_COCA_SHORTCUT.length(), data.length());
            IntentUtils.runApplication(this, packageName);
        } else {
            IntentUtils.startCocaActivity(this);
        }

        finish();
    }
}
