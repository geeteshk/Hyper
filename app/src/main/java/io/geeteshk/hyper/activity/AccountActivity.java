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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;

import io.geeteshk.hyper.R;
import io.geeteshk.hyper.helper.Firebase;
import io.geeteshk.hyper.helper.Theme;

/**
 * Activity for handling account management functions such as
 * changing e-mail, changing password, removing user and signing out
 */
public class AccountActivity extends AppCompatActivity {

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
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme(Theme.getThemeInt(this));
        Theme.setNavigationColor(this);
        mAuth = FirebaseAuth.getInstance();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        Button changeEmail = (Button) findViewById(R.id.acc_change_email);
        Button resetPassword = (Button) findViewById(R.id.acc_change_password);
        Button removeAcc = (Button) findViewById(R.id.acc_remove_acc);
        final Button signOut = (Button) findViewById(R.id.acc_sign_out);
        final EditText editText = (EditText) findViewById(R.id.email_text);
        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.acc_progress_bar);

        if (mAuth.getCurrentUser() != null) {
            editText.setText(mAuth.getCurrentUser().getEmail());
        }

        changeEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                if (mAuth.getCurrentUser() != null && !editText.getText().toString().trim().equals("")) {
                    mAuth.getCurrentUser().updateEmail(editText.getText().toString().trim())
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(AccountActivity.this, "Email address is updated. Please sign in with new email id!", Toast.LENGTH_LONG).show();
                                        signOut();
                                        progressBar.setVisibility(View.GONE);
                                    } else {
                                        Toast.makeText(AccountActivity.this, "Failed to update email!", Toast.LENGTH_LONG).show();
                                        progressBar.setVisibility(View.GONE);
                                    }
                                }
                            });
                } else if (editText.getText().toString().trim().equals("")) {
                    editText.setError("Enter email");
                    progressBar.setVisibility(View.GONE);
                }
            }
        });

        resetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                assert mAuth.getCurrentUser() != null;
                assert mAuth.getCurrentUser().getEmail() != null;
                String email = mAuth.getCurrentUser().getEmail().trim();

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(getApplication(), "Enter your registered email id", Toast.LENGTH_SHORT).show();
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);
                mAuth.sendPasswordResetEmail(email)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(AccountActivity.this, "We have sent you instructions to reset your password!", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(AccountActivity.this, "Failed to send reset email!", Toast.LENGTH_SHORT).show();
                                }

                                progressBar.setVisibility(View.GONE);
                            }
                        });
            }
        });

        removeAcc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                Firebase.removeUser(mAuth, FirebaseStorage.getInstance());
                if (mAuth.getCurrentUser() != null) {
                    mAuth.getCurrentUser().delete()
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(AccountActivity.this, "Your profile has been removed.", Toast.LENGTH_SHORT).show();
                                        signOut();
                                        progressBar.setVisibility(View.GONE);
                                    } else {
                                        Toast.makeText(AccountActivity.this, "Failed to delete your account!", Toast.LENGTH_SHORT).show();
                                        progressBar.setVisibility(View.GONE);
                                    }
                                }
                            });
                }
            }
        });

        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });
    }

    /**
     * Method to sign out from Firebase and switch back to LoginActivity
     */
    private void signOut() {
        mAuth.signOut();
        Intent intent = new Intent(AccountActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}
