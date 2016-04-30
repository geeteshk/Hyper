package io.geeteshk.hyper.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;

import io.geeteshk.hyper.MainActivity;
import io.geeteshk.hyper.R;

public class PermissionUtil {

    public static final int NETWORK_ACCESS_REQUEST_CODE = 0;
    public static final int INTERNET_REQUEST_CODE = 1;
    public static final int READ_STORAGE_REQUEST_CODE = 2;
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
        getPermission(context, Manifest.permission.ACCESS_NETWORK_STATE, R.string.permission_network_rationale, NETWORK_ACCESS_REQUEST_CODE);
        getPermission(context, Manifest.permission.INTERNET, R.string.permission_internet_rationale, INTERNET_REQUEST_CODE);
        getPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE, R.string.permission_storage_rationale, WRITE_STORAGE_REQUEST_CODE);

        if (Build.VERSION.SDK_INT >= 16) {
            getPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE, R.string.permission_storage_rationale, READ_STORAGE_REQUEST_CODE);
        }

        if (PermissionUtil.checkAllPermissions(context)) {
            context.startActivity(new Intent(context, MainActivity.class));
        }
    }

    public static boolean checkAllPermissions(Context context) {
        if (checkPermission(context, Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED ||
                checkPermission(context, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED ||
                checkPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }

        if (Build.VERSION.SDK_INT >= 16) {
            if (checkPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }

        return true;
    }
}
