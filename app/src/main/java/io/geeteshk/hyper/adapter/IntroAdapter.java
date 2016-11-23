/*
 * Copyright 2016 Geetesh Kalakoti <kalakotig@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.geeteshk.hyper.adapter;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import io.geeteshk.hyper.R;
import io.geeteshk.hyper.fragment.IntroFragment;

public class IntroAdapter extends FragmentStatePagerAdapter {

    Context context;
    int[] bgColors;
    int[] images = {R.drawable.ic_intro_logo, R.drawable.ic_intro_editor, R.drawable.ic_intro_git, R.drawable.ic_intro_done};
    String[] titles, desc;

    public IntroAdapter(Context context, FragmentManager fm) {
        super(fm);
        this.context = context;
        bgColors = context.getResources().getIntArray(R.array.bg_screens);
        titles = context.getResources().getStringArray(R.array.slide_titles);
        desc = context.getResources().getStringArray(R.array.slide_desc);
    }

    @Override
    public Fragment getItem(int position) {
        Bundle bundle = new Bundle();
        bundle.putInt("position", position);
        bundle.putInt("bg", bgColors[position]);
        bundle.putInt("image", images[position]);
        bundle.putString("title", titles[position]);
        bundle.putString("desc", desc[position]);
        return Fragment.instantiate(context, IntroFragment.class.getName(), bundle);
    }

    @Override
    public int getCount() {
        return 4;
    }
}
