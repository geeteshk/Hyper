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
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;

import io.geeteshk.hyper.helper.GoogleHolder;
import io.geeteshk.hyper.util.PermissionUtil;
import io.geeteshk.hyper.util.PreferenceUtil;
import io.geeteshk.hyper.util.TypefaceUtil;

public class SplashActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = SplashActivity.class.getSimpleName();
    private static final int RC_SIGN_IN = 205;

    GoogleSignInOptions mOptions;
    GoogleApiClient mApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        TypefaceUtil.setDefaultFont(getApplicationContext(), "SERIF", "fonts/Roboto-Medium.ttf");
        TypefaceUtil.setDefaultFont(getApplicationContext(), "MONOSPACE", "fonts/RobotoCondensed-BoldItalic.ttf");
        TypefaceUtil.setDefaultFont(getApplicationContext(), "SANS_SERIF", "fonts/RobotoCondensed-Regular.ttf");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        mOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, mOptions)
                .build();

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setStatusBarColor(0xFFE64A19);
        }

        if (!PreferenceUtil.get(this, "first_sign_in", true)) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    signIn();
                }
            }, 800);
        }

        SignInButton signInButton = (SignInButton) findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_WIDE);
        if (PreferenceUtil.get(this, "dark_theme", false)) {
            signInButton.setColorScheme(SignInButton.COLOR_DARK);
        }

        signInButton.setScopes(mOptions.getScopeArray());
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });

        signInButton.animate().alpha(1);
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @SuppressLint("InlinedApi")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PermissionUtil.WRITE_STORAGE_REQUEST_CODE) {
            if (PermissionUtil.checkPermission(SplashActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                PermissionUtil.getPermission(SplashActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE, R.string.permission_storage_rationale, PermissionUtil.WRITE_STORAGE_REQUEST_CODE);
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }

        if (PermissionUtil.checkAllPermissions(SplashActivity.this)) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "Google sign-in error. " + connectionResult.getErrorCode() + ": " + connectionResult.getErrorMessage(), Toast.LENGTH_LONG).show();
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            GoogleSignInAccount account = result.getSignInAccount();
            GoogleHolder.getInstance().setAccount(account);
            PermissionUtil.getRequiredPermissions(SplashActivity.this);
            PreferenceUtil.store(this, "first_sign_in", false);
        } else {
            Toast.makeText(this, "Sign-in failed. Please sign-in to Google to use Hyper.", Toast.LENGTH_LONG).show();
        }
    }
}
