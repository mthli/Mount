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
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

import io.github.mthli.mount.BuildConfig;
import io.github.mthli.mount.R;
import io.github.mthli.mount.util.IntentUtils;
import io.github.mthli.mount.util.PolicyUtils;
import io.github.mthli.mount.util.ToastUtils;
import io.reactivex.functions.Consumer;

public class SettingsActivity extends Activity {
    public static class SettingsFragment extends PreferenceFragment {
        private ProgressDialog mProgressDialog;
        private boolean mIsProgressDialogShowing;

        public boolean isProgressDialogShowing() {
            return mIsProgressDialogShowing;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);
        }

        @Override
        public void onResume() {
            super.onResume();

            // update version
            Preference preference = findPreference(getString(R.string.preference_key_version));
            preference.setSummary(BuildConfig.VERSION_NAME);
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
            switch (preference.getTitleRes()) {
                case R.string.preference_title_ignore_foreground_app:
                    IntentUtils.showUsageStatsAccess(getActivity());
                    break;
                case R.string.preference_title_ignore_foreground_service:
                    IntentUtils.showNotificationAccess(getActivity());
                    break;
                case R.string.preference_title_mount_all_temporary:
                    onClickMountAllTemporary();
                    break;
                case R.string.preference_title_pre_uninstall:
                    onClickPreUninstall();
                    break;
                case R.string.preference_title_version:
                    IntentUtils.showVersion(getActivity());
                    break;
                case R.string.preference_title_tutorial:
                    IntentUtils.showTutorial(getActivity());
                    break;
                default:
                    break;
            }

            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }

        private void onClickMountAllTemporary() {
            showProgressDialog();
            PolicyUtils.mountAll(getActivity(), true, new Consumer<Boolean>() {
                @Override
                public void accept(Boolean aBoolean) throws Exception {
                    hideProgressDialog();
                    ToastUtils.showWithShortTime(getActivity(), R.string.toast_mount_all_temporary_ok);
                }
            });
        }

        private void onClickPreUninstall() {
            showProgressDialog();
            PolicyUtils.mountAll(getActivity(), false, new Consumer<Boolean>() {
                @Override
                public void accept(Boolean aBoolean) throws Exception {
                    hideProgressDialog();
                    PolicyUtils.clearDeviceOwnerApp(getActivity());
                    ToastUtils.showWithShortTime(getActivity(), R.string.toast_pre_uninstall_ok);
                }
            });
        }

        private void showProgressDialog() {
            mIsProgressDialogShowing = true;
            if (mProgressDialog != null) {
                mProgressDialog.dismiss();
            }

            mProgressDialog = new ProgressDialog(getActivity());
            mProgressDialog.setMessage(getString(R.string.dialog_message_wait_a_minute));
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
        }

        private void hideProgressDialog() {
            mIsProgressDialogShowing = false;
            if (mProgressDialog != null) {
                mProgressDialog.dismiss();
            }
        }
    }

    private SettingsFragment mSettingsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        mSettingsFragment = new SettingsFragment();
        transaction.replace(R.id.content, mSettingsFragment);
        transaction.commit();
    }

    @Override
    public void onBackPressed() {
        if (!mSettingsFragment.isProgressDialogShowing()) {
            super.onBackPressed();
        }
    }
}
