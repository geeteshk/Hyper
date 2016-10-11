package io.geeteshk.hyper.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;

import java.io.File;

import io.geeteshk.hyper.R;
import io.geeteshk.hyper.helper.Constants;
import io.geeteshk.hyper.helper.Decor;
import io.geeteshk.hyper.helper.Firebase;
import io.geeteshk.hyper.helper.Project;
import io.geeteshk.hyper.helper.Theme;

/**
 * Activity used to login to Firebase
 */
public class LoginActivity extends AppCompatActivity {

    /**
     * FirebaseAuth class to get user information
     * and perform specific Firebase functions
     */
    FirebaseAuth mAuth;

    /**
     * Credential input fields
     */
    private EditText inputEmail, inputPassword;

    /**
     * Dummy progress
     */
    private ProgressBar progressBar;

    /**
     * Method called when activity is created
     *
     * @param savedInstanceState previously stored state
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme(Theme.getThemeInt(this));
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        Decor.setStatusBarColor(this, -1);

        if (mAuth.getCurrentUser() != null) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }

        setContentView(R.layout.activity_login);

        inputEmail = (EditText) findViewById(R.id.email);
        inputPassword = (EditText) findViewById(R.id.password);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        Button btnSignup = (Button) findViewById(R.id.btn_signup);
        Button btnLogin = (Button) findViewById(R.id.btn_login);
        Button btnReset = (Button) findViewById(R.id.btn_reset_password);

        mAuth = FirebaseAuth.getInstance();

        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });

        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, ResetPasswordActivity.class));
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = inputEmail.getText().toString();
                final String password = inputPassword.getText().toString();

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(getApplicationContext(), "Enter email address!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(getApplicationContext(), "Enter password!", Toast.LENGTH_SHORT).show();
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);

                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                progressBar.setVisibility(View.GONE);
                                if (!task.isSuccessful()) {
                                    if (password.length() < 6) {
                                        inputPassword.setError(getString(R.string.minimum_password));
                                    } else {
                                        Toast.makeText(LoginActivity.this, getString(R.string.auth_failed), Toast.LENGTH_LONG).show();
                                    }
                                } else {
                                    File projectDir = new File(Constants.HYPER_ROOT);
                                    if (projectDir.exists()) {
                                        Project.deleteDirectory(LoginActivity.this, projectDir);
                                    }

                                    if (projectDir.mkdir()) {
                                        Firebase.syncProjects(LoginActivity.this, mAuth, FirebaseStorage.getInstance(), null);
                                    }

                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(intent);
                                    finish();
                                }
                            }
                        });
            }
        });
    }
}