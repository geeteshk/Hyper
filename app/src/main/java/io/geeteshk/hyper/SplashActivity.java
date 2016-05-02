package io.geeteshk.hyper;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import io.geeteshk.hyper.util.PermissionUtil;
import io.geeteshk.hyper.util.TypefaceUtil;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        TypefaceUtil.setDefaultFont(getApplicationContext(), "SERIF", "fonts/Roboto-Medium.ttf");
        TypefaceUtil.setDefaultFont(getApplicationContext(), "MONOSPACE", "fonts/RobotoCondensed-BoldItalic.ttf");
        TypefaceUtil.setDefaultFont(getApplicationContext(), "SANS_SERIF", "fonts/RobotoCondensed-Regular.ttf");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                PermissionUtil.getRequiredPermissions(SplashActivity.this);
            }
        }, 1000);
    }

    @SuppressLint("InlinedApi")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PermissionUtil.NETWORK_ACCESS_REQUEST_CODE) {
            if (PermissionUtil.checkPermission(SplashActivity.this, Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED) {
                PermissionUtil.getPermission(SplashActivity.this, Manifest.permission.ACCESS_NETWORK_STATE, R.string.permission_network_rationale, PermissionUtil.NETWORK_ACCESS_REQUEST_CODE);
            }
        } else if (requestCode == PermissionUtil.INTERNET_REQUEST_CODE) {
            if (PermissionUtil.checkPermission(SplashActivity.this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
                PermissionUtil.getPermission(SplashActivity.this, Manifest.permission.INTERNET, R.string.permission_internet_rationale, PermissionUtil.INTERNET_REQUEST_CODE);
            }
        } else if (requestCode == PermissionUtil.WRITE_STORAGE_REQUEST_CODE) {
            if (PermissionUtil.checkPermission(SplashActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                PermissionUtil.getPermission(SplashActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE, R.string.permission_storage_rationale, PermissionUtil.WRITE_STORAGE_REQUEST_CODE);
            }
        } else if (requestCode == PermissionUtil.READ_STORAGE_REQUEST_CODE) {
            if (PermissionUtil.checkPermission(SplashActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                PermissionUtil.getPermission(SplashActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE, R.string.permission_storage_rationale, PermissionUtil.READ_STORAGE_REQUEST_CODE);
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }

        if (PermissionUtil.checkAllPermissions(SplashActivity.this)) {
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            finish();
        }
    }
}
