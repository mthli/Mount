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

package io.github.mthli.mount.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import io.github.mthli.mount.R;
import io.github.mthli.mount.model.PackageRecord;
import io.github.mthli.mount.util.ImageUtils;

public class PackageSettingLayout extends LinearLayout implements CompoundButton.OnCheckedChangeListener,
        View.OnClickListener {
    public interface PackageSettingLayoutListener {
        void onMountSwitchChanged(boolean umount);
        void onClickRunView();
        void onClickInfoView();
        void onClickAddToLauncher();
    }

    private ImageView mIconView;
    private Switch mMountSwitch;
    private TextView mLabelView;
    private TextView mVersionView;
    private TextView mRunView;
    private TextView mInfoView;
    private TextView mAddToLauncherView;

    private PackageSettingLayoutListener mListener;

    public PackageSettingLayout(Context context) {
        super(context);
    }

    public PackageSettingLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PackageSettingLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setPackageSettingLayoutListener(PackageSettingLayoutListener listener) {
        mListener = listener;
    }

    public void setPackageRecord(PackageRecord record) {
        mIconView.setImageBitmap(ImageUtils.bytes2Bitmap(record.icon));
        mMountSwitch.setOnCheckedChangeListener(null);
        mMountSwitch.setChecked(record.umount);
        mMountSwitch.setOnCheckedChangeListener(this);
        mLabelView.setText(record.label);
        mVersionView.setText(record.version);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mIconView = (ImageView) findViewById(R.id.icon);
        mMountSwitch = (Switch) findViewById(R.id.mount_switch);
        mLabelView = (TextView) findViewById(R.id.label);
        mVersionView = (TextView) findViewById(R.id.version);
        mRunView = (TextView) findViewById(R.id.run);
        mInfoView = (TextView) findViewById(R.id.info);
        mAddToLauncherView = (TextView) findViewById(R.id.add_to_launcher);

        mMountSwitch.setOnCheckedChangeListener(this);
        mRunView.setOnClickListener(this);
        mInfoView.setOnClickListener(this);
        mAddToLauncherView.setOnClickListener(this);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView == mMountSwitch && mListener != null) {
            mListener.onMountSwitchChanged(isChecked);
        }
    }

    @Override
    public void onClick(View view) {
        if (view == mRunView && mListener != null) {
            mListener.onClickRunView();
        } else if (view == mInfoView && mListener != null) {
            mListener.onClickInfoView();
        } else if (view == mAddToLauncherView && mListener != null) {
            mListener.onClickAddToLauncher();
        }
    }
}
