package safetybread.hanium.sangeun.safetybread;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;


import java.util.ArrayList;
import java.util.List;

/**
 * Created by sangeun on 2018-08-12.
 */

public class PermissionActivity extends AppCompatActivity {
    private final static int ALL_PERMISSIONS_RESULT = 101;

    private boolean mustGrantAllPermission = true;

    private List<String> permissions = new ArrayList();
    private List<String> permissionsNotGranted = new ArrayList<>();

    protected void addPermission(String permission) {
        permissions.add(permission);
    }

    protected void setMustGrant(boolean mustGrantAllPermission) {
        this.mustGrantAllPermission = mustGrantAllPermission;
    }

    protected void checkAndRequestPermissions() {
        permissionsNotGranted.clear();

        for (String permission : permissions) {
            if (!checkPermission(permission)) {
                permissionsNotGranted.add(permission);
            }
        }
        requestAllPermissions();
    }

    protected boolean checkPermission(String permission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
        }
        return false;
    }

    private void requestAllPermissions() {
        if (permissionsNotGranted.size() == 0) {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permissionsNotGranted.toArray(new String[permissionsNotGranted.size()]), ALL_PERMISSIONS_RESULT);
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .create()
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case ALL_PERMISSIONS_RESULT:
                boolean allGranted = true;
                for (String permission : permissions) {
                    if (!checkPermission(permission)) {
                        allGranted = false;
                        break;
                    }
                }
                if (mustGrantAllPermission && !allGranted) {
                    showMessageOKCancel("This permissions are mandatory for the application. Please allow access.", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            checkAndRequestPermissions();
                        }
                    });
                }
                break;
        }
    }
}
