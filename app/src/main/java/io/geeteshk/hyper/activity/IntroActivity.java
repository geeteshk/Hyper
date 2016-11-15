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
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.github.paolorotolo.appintro.AppIntro;
import com.github.paolorotolo.appintro.AppIntroFragment;

import io.geeteshk.hyper.R;
import io.geeteshk.hyper.helper.Pref;

public class IntroActivity extends AppIntro {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int primaryColor = ContextCompat.getColor(this, R.color.colorPrimary);
        addSlide(AppIntroFragment.newInstance("Hyper", "An intuitive web development IDE.", R.drawable.ic_intro_logo, primaryColor));
        addSlide(AppIntroFragment.newInstance("Code Editor", "Containing features such as line numbers, code refactoring, color formatting, auto indentation, auto completion and more.", R.drawable.ic_intro_editor, primaryColor));
        addSlide(AppIntroFragment.newInstance("Git Integration", "Support for many different Git functions for version control within your projects.", R.drawable.ic_intro_git, primaryColor));
        addSlide(AppIntroFragment.newInstance("Get Started", "Hope those features have got you excited. Many more await you. Get started by creating a project.", R.drawable.ic_intro_done, primaryColor));

        showSkipButton(false);
        askForPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        setIndicatorColor(R.color.whiteButNotAndroidWhite, R.color.colorAccentDark);
        setBarColor(ContextCompat.getColor(this, R.color.colorAccent));
        setNavBarColor(R.color.colorAccentDark);
        setColorDoneText(ContextCompat.getColor(this, R.color.colorAccentDark));
        setNextArrowColor(ContextCompat.getColor(this, R.color.colorAccentDark));
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        if (ContextCompat.checkSelfPermission(IntroActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(IntroActivity.this, R.string.permission_storage_rationale, Toast.LENGTH_LONG).show();
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", getPackageName(), null);
            intent.setData(uri);
            startActivity(intent);
        } else {
            Pref.store(IntroActivity.this, "intro_done", true);
            Intent intent = new Intent(IntroActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }
    }
}
