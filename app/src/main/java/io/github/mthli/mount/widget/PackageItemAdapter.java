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
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.SectionIndexer;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.github.mthli.mount.R;
import io.github.mthli.mount.model.PackageRecord;

public class PackageItemAdapter extends ArrayAdapter<PackageRecord> implements SectionIndexer {
    private Context mContext;
    private List<PackageRecord> mPackageList;
    private Map<String, Integer> mSectionMap;
    private String[] mSectionArray;

    public PackageItemAdapter(Context context, List<PackageRecord> list) {
        super(context, R.layout.layout_package_item, list);

        mContext = context;
        mPackageList = list;
        mSectionMap = new LinkedHashMap<>();
        mSectionArray = new String[0];

        buildSection();
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        if (!(view instanceof PackageItemLayout)) {
            view = LayoutInflater.from(mContext).inflate(R.layout.layout_package_item, parent, false);
        }

        ((PackageItemLayout) view).setPackageRecord(mPackageList.get(position));

        return view;
    }

    // =============================================================================================

    @Override
    public int getPositionForSection(int sectionIndex) {
        return mSectionMap.get(mSectionArray[sectionIndex]);
    }

    @Override
    public int getSectionForPosition(int position) {
        return 0;
    }

    @Override
    public Object[] getSections() {
        return mSectionArray;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        buildSection();
    }

    private void buildSection() {
        mSectionMap.clear();
        for (int index = 0; index < mPackageList.size(); index++) {
            mSectionMap.put(mPackageList.get(index).label.substring(0, 1).toUpperCase(), index);
        }

        List<String> sectionList = new ArrayList<>(mSectionMap.keySet());
        Collections.sort(sectionList, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return Collator.getInstance().compare(o1, o2);
            }
        });

        mSectionArray = new String[sectionList.size()];
        sectionList.toArray(mSectionArray);
    }
}
