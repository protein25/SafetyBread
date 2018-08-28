package safetybread.hanium.sangeun.safetybread;

import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sangeun on 2018-08-18.
 */

public class PermissionActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_RESULT = 100;

    private List<String> permissions = new ArrayList<>();
    private List<String> permissionNotGranted = new ArrayList<>();

    private boolean mustGrantedPermission = true;

    //필수 권한 메서드
    public void setMustGrantedPermission(boolean mustGrantedPermission) {
        this.mustGrantedPermission = mustGrantedPermission;
    }

    //필요한 권한 추가 메서드
    public void addPermissions(String permission) {
        permissions.add(permission);
    }

    //권한 검사 메서드
    private boolean checkPermisson(String permission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
        }
        return false;
    }

    //권한 요청 메서드
    private void requestAllPermission() {
        if (permissionNotGranted.size() == 0) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permissionNotGranted.toArray(new String[permissionNotGranted.size()]), PERMISSION_REQUEST_RESULT);
        }
    }

    //권한 검사 및 요청하는 메서드
    public void checkAndRequestPermission() {
        permissionNotGranted.clear();
        for (String permission : permissions) {
            if (!checkPermisson(permission)) {
                permissionNotGranted.add(permission);
            }
        }
        requestAllPermission();
    }

    //권한 승인 요청 메서드
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_RESULT:
                boolean allGranted = true;
                for (String permission : permissions) {
                    if (!checkPermisson(permission)) {
                        allGranted = false;
                        break;
                    }
                }
                if (mustGrantedPermission && !allGranted) {
                    new AlertDialog.Builder(getApplicationContext())
                            .setMessage("This permissions are mandatory for the application. Please allow access.")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    checkAndRequestPermission();
                                }
                            })
                            .create()
                            .show();
                }
        }
    }
}
