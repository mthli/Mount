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
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import io.github.mthli.mount.R;
import io.github.mthli.mount.model.PackageRecord;
import io.github.mthli.mount.util.DisplayUtils;
import io.github.mthli.mount.util.ImageUtils;
import io.github.mthli.mount.util.IntentUtils;
import io.github.mthli.mount.util.PolicyUtils;
import io.github.mthli.mount.util.RxUtils;
import io.github.mthli.mount.util.ToastUtils;
import io.github.mthli.mount.widget.PackageSettingLayout;
import io.github.mthli.mount.widget.PolicyHintLayout;
import io.github.mthli.mount.widget.PackageItemAdapter;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class MountActivity extends Activity implements AbsListView.OnScrollListener,
        AdapterView.OnItemClickListener, PackageSettingLayout.PackageSettingLayoutListener {
    private ListView mListView;
    private PolicyHintLayout mHeaderView;
    private PackageItemAdapter mPackageAdapter;
    private List<PackageRecord> mPackageList;

    private AlertDialog mSettingDialog;
    private MenuItem mLoadingItem;
    private MenuItem mSettingsItem;
    private MenuItem mDonateItem;

    private PackageRecord mCurrentRecord;
    private Disposable mFastScrollDisposable;
    private Disposable mFetchPackageRecordDisposable;
    private Disposable mHeaderViewDisposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupListView();
        setupHeaderView();
    }

    private void setupListView() {
        mListView = (ListView) findViewById(R.id.list);

        mPackageList = new ArrayList<>();
        mPackageAdapter = new PackageItemAdapter(this, mPackageList);
        mListView.setAdapter(mPackageAdapter);
        mListView.setHeaderDividersEnabled(false);

        mListView.setOnScrollListener(this); // not show fast scroll always
        mListView.setOnItemClickListener(this);
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        // DO NOTHING
    }

    @Override
    public void onScrollStateChanged(final AbsListView view, int scrollState) {
        RxUtils.disposeSafety(mFastScrollDisposable);
        if (scrollState != SCROLL_STATE_IDLE) {
            view.setFastScrollAlwaysVisible(true);
            return;
        }

        mFastScrollDisposable = Observable.timer(1000L, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        view.setFastScrollAlwaysVisible(false);
                    }
                });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        dismissSettingDialogSafety();
        if (!PolicyUtils.isDeviceOwnerApp(this)) {
            IntentUtils.showTutorial(this);
            return;
        }

        mCurrentRecord = mPackageList.get(position);
        PackageSettingLayout layout = (PackageSettingLayout) getLayoutInflater()
                .inflate(R.layout.layout_package_setting, null, false);
        layout.setPackageRecord(mCurrentRecord);
        layout.setPackageSettingLayoutListener(this);

        mSettingDialog = new AlertDialog.Builder(this)
                .setCancelable(true)
                .setView(layout)
                .create();
        mSettingDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                mSettingDialog.getWindow().setLayout(DisplayUtils.dp2px(MountActivity.this, 322.0F),
                        WindowManager.LayoutParams.WRAP_CONTENT);
            }
        });
        mSettingDialog.show();
    }

    private void dismissSettingDialogSafety() {
        if (mSettingDialog != null && mSettingDialog.isShowing()) {
            mSettingDialog.dismiss();
        }
    }

    @Override
    public void onMountSwitchChanged(boolean umount) {
        if (umount) {
            mCurrentRecord.umount = true;
            mCurrentRecord.update();
        } else {
            mCurrentRecord.umount = false;
            mCurrentRecord.delete();
        }

        mPackageAdapter.notifyDataSetChanged();
        PolicyUtils.setApplicationMount(this, mCurrentRecord.name, !umount);
        ToastUtils.showWithShortTime(this, getString(umount ? R.string.toast_umount
                : R.string.toast_mount, mCurrentRecord.label));
    }

    @Override
    public void onClickRunView() {
        dismissSettingDialogSafety();
        IntentUtils.runApplication(this, mCurrentRecord.name);
    }

    @Override
    public void onClickInfoView() {
        dismissSettingDialogSafety();
        IntentUtils.showApplicationInfo(this, mCurrentRecord.name);
    }

    @Override
    public void onClickAddToLauncher() {
        dismissSettingDialogSafety();

        Bitmap icon = ImageUtils.bytes2Bitmap(mCurrentRecord.icon);
        float width = icon.getWidth();
        float height = icon.getHeight();
        float max = width > height ? width : height;
        if (max > ImageUtils.LAUNCHER_ICON_MAX_SIZE) {
            float ratio = ImageUtils.LAUNCHER_ICON_MAX_SIZE / max;
            icon = ImageUtils.resize(icon, (int) (width * ratio), (int) (height * ratio));
        }

        IntentUtils.createShortcut(this, mCurrentRecord.name, icon, mCurrentRecord.label);
        ToastUtils.showWithShortTime(this, getString(R.string.toast_add_to_launcher, mCurrentRecord.label));
    }

    private void setupHeaderView() {
        mHeaderView = (PolicyHintLayout) getLayoutInflater().inflate(R.layout.layout_device_hint,
                mListView, false);
        if (!PolicyUtils.isDeviceOwnerApp(this)) {
            mListView.addHeaderView(mHeaderView);
            hideOptions();
        }

        RxUtils.disposeSafety(mHeaderViewDisposable);
        mHeaderViewDisposable = Observable.interval(200L, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        if (!PolicyUtils.isDeviceOwnerApp(MountActivity.this)) {
                            hideOptions();
                            if (mListView.getHeaderViewsCount() <= 0){
                                mListView.addHeaderView(mHeaderView);
                            }
                        } else {
                            showOptions();
                            if (mListView.getHeaderViewsCount() > 0) {
                                mListView.removeHeaderView(mHeaderView);
                            }
                        }
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        mLoadingItem = menu.findItem(R.id.loading);
        mSettingsItem = menu.findItem(R.id.settings);
        mDonateItem = menu.findItem(R.id.donate);

        showLoading();
        hideOptions();

        return true;
    }

    private void showLoading() {
        if (mLoadingItem != null) {
            mLoadingItem.setVisible(true);
        }
    }

    public void hideLoading() {
        if (mLoadingItem != null) {
            mLoadingItem.setVisible(false);
        }
    }

    private void showOptions() {
        if (mSettingsItem != null) {
            mSettingsItem.setVisible(true);
        }

        if (mDonateItem != null) {
            mDonateItem.setVisible(true);
        }
    }

    private void hideOptions() {
        if (mSettingsItem != null) {
            mSettingsItem.setVisible(false);
        }

        if (mDonateItem != null) {
            mDonateItem.setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                IntentUtils.startSettingsActivity(this);
                break;
            case R.id.donate:
                showDonateDialog();
                break;
            default:
                break;
        }

        return true;
    }

    private void showDonateDialog() {
        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setCancelable(true)
                .setTitle(R.string.donate_dialog_title)
                .setMessage(R.string.alipay_account)
                .setPositiveButton(R.string.donate_dialog_positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ClipboardManager manager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                        ClipData data = ClipData.newPlainText("", getString(R.string.alipay_account));
                        manager.setPrimaryClip(data);
                        ToastUtils.showWithShortTime(MountActivity.this, R.string.toast_copied);
                    }
                })
                .create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                dialog.getWindow().setLayout(DisplayUtils.dp2px(MountActivity.this, 322.0F),
                        WindowManager.LayoutParams.WRAP_CONTENT);
            }
        });
        dialog.show();
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchPackageRecord();
    }

    private void fetchPackageRecord() {
        RxUtils.disposeSafety(mFetchPackageRecordDisposable);
        showLoading();

        mFetchPackageRecordDisposable = Observable.create(
                new ObservableOnSubscribe<List<PackageRecord>>() {
                    @Override
                    public void subscribe(ObservableEmitter<List<PackageRecord>> e) throws Exception {
                        // list all umount apps first
                        List<PackageRecord> recordList = PackageRecord.listAll(PackageRecord.class);
                        Set<String> nameSet = new HashSet<>();
                        for (PackageRecord record : recordList) {
                            nameSet.add(record.name);
                        }

                        // query all apps can launched from launcher
                        Intent intent = new Intent(Intent.ACTION_MAIN);
                        intent.addCategory(Intent.CATEGORY_LAUNCHER);
                        List<ResolveInfo> resolveInfoList = getPackageManager().queryIntentActivities(intent, 0);

                        // get all apps' application info
                        List<ApplicationInfo> applicationInfoList = new ArrayList<>();
                        for (ResolveInfo info : resolveInfoList) {
                            applicationInfoList.add(info.activityInfo.applicationInfo);
                        }

                        // filter we don't want
                        for (ApplicationInfo info : applicationInfoList) {
                            if (TextUtils.equals(info.packageName, getPackageName())
                                    || nameSet.contains(info.packageName)) {
                                continue;
                            }

                            nameSet.add(info.packageName);
                            recordList.add(new PackageRecord(info.packageName,
                                    ImageUtils.drawable2Bytes(info.loadIcon(getPackageManager())),
                                    info.loadLabel(getPackageManager()).toString(),
                                    getPackageManager().getPackageInfo(info.packageName, 0).versionName,
                                    false));
                        }

                        // sort by language
                        Collections.sort(recordList, new Comparator<PackageRecord>() {
                            @Override
                            public int compare(PackageRecord record1, PackageRecord record2) {
                                return Collator.getInstance().compare(record1.label, record2.label);
                            }
                        });

                        e.onNext(recordList);
                        e.onComplete();
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<PackageRecord>>() {
                    @Override
                    public void accept(List<PackageRecord> list) throws Exception {
                        mPackageList.clear();
                        mPackageList.addAll(list);
                        mPackageAdapter.notifyDataSetChanged();
                        hideLoading();
                    }
                });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        RxUtils.disposeSafety(mFastScrollDisposable);
        RxUtils.disposeSafety(mFetchPackageRecordDisposable);
        RxUtils.disposeSafety(mHeaderViewDisposable);
    }
}
