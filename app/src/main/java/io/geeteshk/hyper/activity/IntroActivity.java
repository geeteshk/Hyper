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

package io.geeteshk.hyper.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.geeteshk.hyper.R;
import io.geeteshk.hyper.adapter.IntroAdapter;
import io.geeteshk.hyper.helper.Prefs;
import io.geeteshk.hyper.text.HtmlCompat;

public class IntroActivity extends AppCompatActivity {

    @BindView(R.id.intro_pager) ViewPager viewPager;
    @BindView(R.id.intro_dots) LinearLayout dotsLayout;
    @BindView(R.id.btn_skip) Button skip;
    @BindView(R.id.btn_next) Button next;

    IntroAdapter introAdapter;
    TextView[] dots;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }

        setContentView(R.layout.activity_intro);
        ButterKnife.bind(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }

        addBottomDots(0);

        introAdapter = new IntroAdapter(this, getSupportFragmentManager());
        viewPager.setAdapter(introAdapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                addBottomDots(position);
                if (position == 3) {
                    next.setText(getString(R.string.start));
                    skip.setVisibility(View.GONE);
                } else {
                    next.setText(getString(R.string.next));
                    skip.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                endIntro();
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int current = viewPager.getCurrentItem() + 1;
                if (current < 4) {
                    viewPager.setCurrentItem(current);
                } else {
                    endIntro();
                }
            }
        });
    }

    private void endIntro() {
        Prefs.store(IntroActivity.this, "intro_done", true);
        Intent intent = new Intent(IntroActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void addBottomDots(int currentPage) {
        dots = new TextView[4];

        int[] colorsActive = getResources().getIntArray(R.array.array_dot_active);
        int[] colorsInactive = getResources().getIntArray(R.array.array_dot_inactive);

        dotsLayout.removeAllViews();
        for (int i = 0; i < dots.length; i++) {
            dots[i] = new TextView(this);
            dots[i].setText(HtmlCompat.fromHtml("&#8226;"));
            dots[i].setTextSize(35);
            dots[i].setTextColor(colorsInactive[currentPage]);
            dotsLayout.addView(dots[i]);
        }

        if (dots.length > 0) dots[currentPage].setTextColor(colorsActive[currentPage]);
    }
}
