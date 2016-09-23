package io.geeteshk.hyper.helper;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;

import io.geeteshk.hyper.activity.LoginActivity;
import io.geeteshk.hyper.activity.MainActivity;
import io.geeteshk.hyper.R;
import io.geeteshk.hyper.activity.SignupActivity;

public class Law {

    public static final int WRITE_STORAGE_REQUEST_CODE = 3;

    public static int checkPermission(Context context, String permission) {
        return ContextCompat.checkSelfPermission(context, permission);
    }

    public static void getPermission(final Context context, final String permission, final int explanation, final int code) {
        if (checkPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, permission)) {
                Snackbar.make(((Activity) context).getWindow().getDecorView(), explanation,
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.ok, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                getPermission(context, permission, explanation, code);
                            }
                        })
                        .show();
            } else {
                ActivityCompat.requestPermissions((Activity) context, new String[]{permission}, code);
            }
        }
    }

    public static void getRequiredPermissions(Context context) {
        getPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE, R.string.permission_storage_rationale, WRITE_STORAGE_REQUEST_CODE);

        if (Law.checkAllPermissions(context)) {
            Class classTo = LoginActivity.class;
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                classTo = MainActivity.class;
            }

            Intent intent = new Intent(context, classTo);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(intent);
            ((Activity) context).finish();
        }
    }

    public static boolean checkAllPermissions(Context context) {
        return checkPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }
}
