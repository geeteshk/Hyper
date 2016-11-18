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
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

import io.geeteshk.hyper.R;
import io.geeteshk.hyper.helper.Pref;
import io.geeteshk.hyper.helper.Theme;
import io.geeteshk.hyper.helper.Typefacer;

/**
 * Activity for application splash
 */
public class SplashActivity extends AppCompatActivity {

    /**
     * Layout to handle snackbars
     */
    CoordinatorLayout mLayout;

    /**
     * Method called when activity is created
     *
     * @param savedInstanceState previously stored state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Typefacer.setDefaultFont(getApplicationContext(), "SERIF", "fonts/Roboto-Medium.ttf");
        Typefacer.setDefaultFont(getApplicationContext(), "SANS_SERIF", "fonts/RobotoCondensed-Regular.ttf");
        Typefacer.setDefaultFont(getApplicationContext(), "MONOSPACE", "fonts/Consolas.ttf");
        Theme.setNavigationColor(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        ImageView logo = (ImageView) findViewById(R.id.hyper_logo);
        TextView logoText = (TextView) findViewById(R.id.hyper_logo_text);
        logo.animate().alpha(1).setDuration(800);
        logoText.animate().alpha(1).setDuration(800);

        mLayout = (CoordinatorLayout) findViewById(R.id.splash_layout);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Class classTo = IntroActivity.class;
                if (Pref.get(SplashActivity.this, "intro_done", false)) {
                    classTo = MainActivity.class;
                }

                Intent intent = new Intent(SplashActivity.this, classTo);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        }, 1000);
    }
}
