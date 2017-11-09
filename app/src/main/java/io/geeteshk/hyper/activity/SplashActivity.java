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

import android.Manifest;
import android.animation.Animator;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.psdev.licensesdialog.LicenseResolver;
import io.geeteshk.hyper.R;
import io.geeteshk.hyper.helper.Prefs;
import io.geeteshk.hyper.helper.FontsOverride;
import io.geeteshk.hyper.license.EclipseDistributionLicense10;

/**
 * Activity for application splash
 */
public class SplashActivity extends AppCompatActivity {

    private static final int WRITE_PERMISSION_REQUEST = 0;

    /**
     * Layout to handle snackbars
     */
    @BindView(R.id.splash_layout) CoordinatorLayout splashLayout;

    @BindView(R.id.hyper_logo) ImageView logo;
    @BindView(R.id.hyper_logo_text) TextView logoText;

    /**
     * Method called when activity is created
     *
     * @param savedInstanceState previously stored state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LicenseResolver.registerLicense(new EclipseDistributionLicense10());
        FontsOverride.setDefaultFont(getApplicationContext(), "SERIF", "fonts/Roboto-Medium.ttf");
        FontsOverride.setDefaultFont(getApplicationContext(), "SANS_SERIF", "fonts/RobotoCondensed-Regular.ttf");
        FontsOverride.setDefaultFont(getApplicationContext(), "MONOSPACE", "fonts/Consolas.ttf");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        ButterKnife.bind(this);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        logo.animate().alpha(1).setDuration(1000);
        logoText.animate().alpha(1).setDuration(1000).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                setupPermissions();
            }

            @Override
            public void onAnimationCancel(Animator animator) {
                setupPermissions();
            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
    }

    private void startIntro() {
        Class classTo = IntroActivity.class;
        if (Prefs.get(SplashActivity.this, "intro_done", false)) {
            classTo = MainActivity.class;
        }

        Intent intent = new Intent(SplashActivity.this, classTo);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void setupPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                final Snackbar snackbar = Snackbar.make(splashLayout, getString(R.string.permission_storage_rationale), Snackbar.LENGTH_INDEFINITE);
                snackbar.setAction("GRANT", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        snackbar.dismiss();
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivityForResult(intent, WRITE_PERMISSION_REQUEST);
                    }
                });

                snackbar.show();
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        WRITE_PERMISSION_REQUEST);
            }
        } else {
            startIntro();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == WRITE_PERMISSION_REQUEST) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startIntro();
            }
        } else {
            final Snackbar snackbar = Snackbar.make(splashLayout, getString(R.string.permission_storage_rationale), Snackbar.LENGTH_INDEFINITE);
            snackbar.setAction("GRANT", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    snackbar.dismiss();
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivityForResult(intent, WRITE_PERMISSION_REQUEST);
                }
            });

            snackbar.show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == WRITE_PERMISSION_REQUEST) {
            setupPermissions();
        }
    }
}
