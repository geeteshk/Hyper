package io.geeteshk.hyper;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import io.geeteshk.hyper.helper.Law;
import io.geeteshk.hyper.helper.Typefacer;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Typefacer.setDefaultFont(getApplicationContext(), "SERIF", "fonts/Roboto-Medium.ttf");
        Typefacer.setDefaultFont(getApplicationContext(), "MONOSPACE", "fonts/RobotoCondensed-BoldItalic.ttf");
        Typefacer.setDefaultFont(getApplicationContext(), "SANS_SERIF", "fonts/RobotoCondensed-Regular.ttf");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setStatusBarColor(0xFFE64A19);
        }

        TextView logo = (TextView) findViewById(R.id.hyper_logo);
        logo.animate().alpha(1).setDuration(800);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Law.getRequiredPermissions(SplashActivity.this);
            }
        }, 1000);
    }

    @SuppressLint("InlinedApi")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == Law.WRITE_STORAGE_REQUEST_CODE) {
            if (Law.checkPermission(SplashActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                Law.getPermission(SplashActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE, R.string.permission_storage_rationale, Law.WRITE_STORAGE_REQUEST_CODE);
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }

        if (Law.checkAllPermissions(SplashActivity.this)) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }
    }
}
