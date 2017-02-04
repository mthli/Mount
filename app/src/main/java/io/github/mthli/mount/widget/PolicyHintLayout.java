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
import android.text.Html;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import io.github.mthli.mount.R;
import io.github.mthli.mount.util.IntentUtils;

public class PolicyHintLayout extends RelativeLayout {
    public PolicyHintLayout(Context context) {
        super(context);
    }

    public PolicyHintLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PolicyHintLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        ((TextView) findViewById(R.id.hint)).setText(Html.fromHtml(
                getContext().getString(R.string.device_policy_hint)));

        findViewById(R.id.button).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentUtils.showTutorial(getContext());
            }
        });
    }
}
