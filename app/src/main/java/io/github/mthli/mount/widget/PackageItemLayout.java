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
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import io.github.mthli.mount.R;
import io.github.mthli.mount.model.PackageRecord;
import io.github.mthli.mount.util.ImageUtils;

public class PackageItemLayout extends RelativeLayout {
    private ImageView mIconView;
    private ImageView mUmountView;
    private TextView mLabelView;
    private TextView mVersionView;

    public PackageItemLayout(Context context) {
        super(context);
    }

    public PackageItemLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PackageItemLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mIconView = (ImageView) findViewById(R.id.icon);
        mUmountView = (ImageView) findViewById(R.id.umount);
        mLabelView = (TextView) findViewById(R.id.label);
        mVersionView = (TextView) findViewById(R.id.version);

        setupUmountView();
    }

    private void setupUmountView() {
        TypedValue value = new TypedValue();
        getContext().getTheme().resolveAttribute(android.R.attr.colorControlActivated, value, true);
        mUmountView.getDrawable().mutate().setColorFilter(value.data, PorterDuff.Mode.SRC_IN);
    }

    public void setPackageRecord(PackageRecord record) {
        mIconView.setImageBitmap(ImageUtils.bytes2Bitmap(record.icon));
        mUmountView.setVisibility(record.umount ? VISIBLE : GONE);
        mLabelView.setText(record.label);
        mVersionView.setText(record.version);
    }
}
