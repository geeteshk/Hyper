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

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.geeteshk.hyper.R;
import io.geeteshk.hyper.adapter.AnalyzeAdapter;
import io.geeteshk.hyper.fragment.analyze.AnalyzeFileFragment;
import io.geeteshk.hyper.helper.Styles;

public class AnalyzeActivity extends AppCompatActivity {

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.analyze_tabs) TabLayout analyzeTabs;
    @BindView(R.id.analyze_pager) ViewPager analyzePager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(Styles.getThemeInt(this));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analyze);
        ButterKnife.bind(this);
        toolbar.setTitle(new File(getIntent().getStringExtra("project_file")).getName());
        setSupportActionBar(toolbar);
        setupPager(analyzePager);
        analyzeTabs.setupWithViewPager(analyzePager);
    }

    private void setupPager(ViewPager analyzePager) {
        AnalyzeAdapter adapter = new AnalyzeAdapter(getSupportFragmentManager());
        adapter.addFragment(new AnalyzeFileFragment(), "FILES", getIntent().getExtras());
        analyzePager.setAdapter(adapter);
    }
}
