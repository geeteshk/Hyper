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
        addSlide(AppIntroFragment.newInstance("Hyper", "An intuitive web development IDE.", R.drawable.ic_launcher, primaryColor));
        addSlide(AppIntroFragment.newInstance("Code Editor", "Containing features such as line numbers, code refactoring, color formatting, auto indentation, auto completion and more.", R.drawable.ic_intro_editor, primaryColor));
        addSlide(AppIntroFragment.newInstance("Git Integration", "Support for many different Git functions for version control within your projects.", R.drawable.ic_intro_git, primaryColor));
        addSlide(AppIntroFragment.newInstance("Polymer Support", "Provides support for Polymer packages in your projects.", R.drawable.ic_intro_polymer, primaryColor));
        addSlide(AppIntroFragment.newInstance("Cloud Services", "Using the Firebase API all your work is stored securely on the cloud and can be accessed from any device when signed-in.", R.drawable.ic_intro_cloud, primaryColor));
        addSlide(AppIntroFragment.newInstance("Get Started", "You're ready to get going. Get started by registering for Hyper.", R.drawable.ic_intro_done, primaryColor));

        showSkipButton(false);
        askForPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 5);
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
            Intent intent = new Intent(IntroActivity.this, SignupActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }
    }
}
