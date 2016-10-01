package io.geeteshk.hyper.helper;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;

import io.geeteshk.hyper.R;
import io.geeteshk.hyper.activity.LoginActivity;
import io.geeteshk.hyper.activity.MainActivity;

/**
 * Helper class for handling permissions
 */
public class Law {

    /**
     * Request code for WRITE_STORAGE permission
     */
    public static final int WRITE_STORAGE_REQUEST_CODE = 3;

    /**
     * Check if permission is granted
     *
     * @param context context to check permission
     * @param permission to check for
     * @return whether permission is granted
     */
    public static int checkPermission(Context context, String permission) {
        return ContextCompat.checkSelfPermission(context, permission);
    }

    /**
     * Request access for a permission
     *
     * @param context context to get permission
     * @param permission permission to request
     * @param explanation why we are requesting the permission
     * @param code to request permission
     * @param layout for snack bar
     */
    public static void getPermission(final Context context, final String permission, final int explanation, final int code, final CoordinatorLayout layout) {
        if (checkPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, permission)) {
                Snackbar.make(layout, explanation,
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.ok, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                getPermission(context, permission, explanation, code, layout);
                            }
                        })
                        .show();
            } else {
                ActivityCompat.requestPermissions((Activity) context, new String[]{permission}, code);
            }
        }
    }

    /**
     * Method to get multiple permissions
     *
     * @param context context to get permissions
     * @param layout for snack bar
     */
    public static void getRequiredPermissions(Context context, CoordinatorLayout layout) {
        getPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE, R.string.permission_storage_rationale, WRITE_STORAGE_REQUEST_CODE, layout);

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

    /**
     * Check multiple permissions
     *
     * @param context context to check permissions
     * @return true if permissions are granted
     */
    public static boolean checkAllPermissions(Context context) {
        return checkPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }
}
