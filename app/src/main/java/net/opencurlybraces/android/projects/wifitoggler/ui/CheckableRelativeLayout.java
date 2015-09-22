/*
 * Copyright 2014 DogmaLabs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.opencurlybraces.android.projects.wifitoggler.ui;


import android.content.Context;
import android.util.AttributeSet;
import android.widget.Checkable;
import android.widget.RelativeLayout;

import net.opencurlybraces.android.projects.wifitoggler.R;

public class CheckableRelativeLayout extends RelativeLayout implements Checkable {

    private boolean mIsChecked = false;

    public CheckableRelativeLayout(Context context) {
        super(context);
    }

    public CheckableRelativeLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CheckableRelativeLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setChecked(boolean checked) {
        this.mIsChecked = checked;
        changeColor(mIsChecked);
    }

    @Override
    public boolean isChecked() {
        return mIsChecked;
    }

    @Override
    public void toggle() {
        this.mIsChecked = !this.mIsChecked;
        changeColor(this.mIsChecked);
    }


    private void changeColor(boolean isChecked) {
        if (isChecked) {
            setBackgroundColor(getResources().getColor(R.color.material_blue_400));
        } else {
            setBackgroundColor(getResources().getColor(android.R.color.transparent));
        }

    }
}






