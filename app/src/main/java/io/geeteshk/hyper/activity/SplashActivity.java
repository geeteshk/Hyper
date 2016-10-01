package io.geeteshk.hyper.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import io.geeteshk.hyper.R;
import io.geeteshk.hyper.helper.Decor;
import io.geeteshk.hyper.helper.Law;
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
     * Firebase class(es) to get user information
     * and perform specific Firebase functions
     */
    FirebaseAuth mAuth;

    /**
     * Method called when activity is created
     *
     * @param savedInstanceState previously stored state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Typefacer.setDefaultFont(getApplicationContext(), "SERIF", "fonts/Roboto-Medium.ttf");
        Typefacer.setDefaultFont(getApplicationContext(), "MONOSPACE", "fonts/RobotoCondensed-BoldItalic.ttf");
        Typefacer.setDefaultFont(getApplicationContext(), "SANS_SERIF", "fonts/RobotoCondensed-Regular.ttf");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        mAuth = FirebaseAuth.getInstance();

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        Decor.setStatusBarColor(this, -1);

        ImageView logo = (ImageView) findViewById(R.id.hyper_logo);
        TextView logoText = (TextView) findViewById(R.id.hyper_logo_text);
        logo.animate().alpha(1).setDuration(800);
        logoText.animate().alpha(1).setDuration(800);

        mLayout = (CoordinatorLayout) findViewById(R.id.splash_layout);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Law.getRequiredPermissions(SplashActivity.this, mLayout);
            }
        }, 1000);
    }

    /**
     * Called when a permission request is handled
     *
     * @param requestCode code to request permision
     * @param permissions array of permissions that were requested
     * @param grantResults what the results are
     */
    @SuppressLint("InlinedApi")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == Law.WRITE_STORAGE_REQUEST_CODE) {
            if (Law.checkPermission(SplashActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                Law.getPermission(SplashActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE, R.string.permission_storage_rationale, Law.WRITE_STORAGE_REQUEST_CODE, mLayout);
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }

        if (Law.checkAllPermissions(SplashActivity.this)) {
            Class classTo = SignupActivity.class;
            if (mAuth.getCurrentUser() != null) {
                classTo = MainActivity.class;
            }

            Intent intent = new Intent(getApplicationContext(), classTo);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }
    }
}
