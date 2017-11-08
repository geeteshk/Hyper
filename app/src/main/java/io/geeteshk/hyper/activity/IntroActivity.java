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
import android.widget.Toast;

import io.geeteshk.hyper.R;
import io.geeteshk.hyper.adapter.IntroAdapter;
import io.geeteshk.hyper.helper.Prefs;
import io.geeteshk.hyper.text.HtmlCompat;

public class IntroActivity extends AppCompatActivity {

    ViewPager mPager;
    IntroAdapter mAdapter;
    LinearLayout mDotsLayout;
    TextView[] mDots;
    Button mSkip, mNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }

        setContentView(R.layout.activity_intro);

        mPager = (ViewPager) findViewById(R.id.intro_pager);
        mDotsLayout = (LinearLayout) findViewById(R.id.intro_dots);
        mSkip = (Button) findViewById(R.id.btn_skip);
        mNext = (Button) findViewById(R.id.btn_next);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }

        addBottomDots(0);

        mAdapter = new IntroAdapter(this, getSupportFragmentManager());
        mPager.setAdapter(mAdapter);
        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                addBottomDots(position);
                if (position == 3) {
                    mNext.setText(getString(R.string.start));
                    mSkip.setVisibility(View.GONE);
                } else {
                    mNext.setText(getString(R.string.next));
                    mSkip.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        mSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                endIntro();
            }
        });

        mNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int current = mPager.getCurrentItem() + 1;
                if (current < 4) {
                    mPager.setCurrentItem(current);
                } else {
                    endIntro();
                }
            }
        });
    }

    private void endIntro() {
        if (getIntent().getBooleanExtra("isAppetize", false)) {
            Toast.makeText(IntroActivity.this, "Get the app to try out these features for yourself!", Toast.LENGTH_LONG).show();
        } else {
            Prefs.store(IntroActivity.this, "intro_done", true);
            Intent intent = new Intent(IntroActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }
    }

    private void addBottomDots(int currentPage) {
        mDots = new TextView[4];

        int[] colorsActive = getResources().getIntArray(R.array.array_dot_active);
        int[] colorsInactive = getResources().getIntArray(R.array.array_dot_inactive);

        mDotsLayout.removeAllViews();
        for (int i = 0; i < mDots.length; i++) {
            mDots[i] = new TextView(this);
            mDots[i].setText(HtmlCompat.fromHtml("&#8226;"));
            mDots[i].setTextSize(35);
            mDots[i].setTextColor(colorsInactive[currentPage]);
            mDotsLayout.addView(mDots[i]);
        }

        if (mDots.length > 0) mDots[currentPage].setTextColor(colorsActive[currentPage]);
    }
}
