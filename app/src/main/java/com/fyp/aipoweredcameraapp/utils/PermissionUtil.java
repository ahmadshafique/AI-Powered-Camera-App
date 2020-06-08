package com.fyp.aipoweredcameraapp.utils;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.view.View;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public abstract class PermissionUtil {

    /* Permission required for application */
    public static final String[] PERMISSION_ALL = {
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    public static void goToPermissionSettingScreen(Activity activity) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", activity.getPackageName(), null));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
    }

    public static boolean isAllPermissionGranted(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] permission = PERMISSION_ALL;
            if (permission.length == 0) return false;
            for (String s : permission) {
                if (ActivityCompat.checkSelfPermission(activity, s) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    @TargetApi(Build.VERSION_CODES.M)
    public static String[] getDeniedPermission(Activity act) {
        List<String> permissions = new ArrayList<>();
        for (int i = 0; i < PERMISSION_ALL.length; i++) {
            int status = act.checkSelfPermission(PERMISSION_ALL[i]);
            if (status != PackageManager.PERMISSION_GRANTED) {
                permissions.add(PERMISSION_ALL[i]);
            }
        }

        return permissions.toArray(new String[permissions.size()]);
    }


    @TargetApi(Build.VERSION_CODES.M)
    public static boolean isGranted(Activity act, String permission) {
        if (!Tools.needRequestPermission()) return true;
        return (act.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED);
    }


    public static void showSystemDialogPermission(Fragment fragment, String perm) {
        fragment.requestPermissions(new String[]{perm}, 200);
    }

    @TargetApi(Build.VERSION_CODES.M)
    public static void showSystemDialogPermission(Activity act, String perm) {
        act.requestPermissions(new String[]{perm}, 200);
    }


    @TargetApi(Build.VERSION_CODES.M)
    public static void showSystemDialogPermission(Activity act, String perm, int code) {
        act.requestPermissions(new String[]{perm}, code);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void getPermission(Activity act, String perm) {
        if (act.shouldShowRequestPermissionRationale(perm))
            showSystemDialogPermission(act, perm);
        else
            goToPermissionSettingScreen(act);
    }

    public static boolean checkPermission(Activity act, View v, String perm, String rationale) {
        if (!PermissionUtil.isGranted(act, perm)) {
            Snackbar.make(v, rationale, Snackbar.LENGTH_INDEFINITE).setAction("GRANT", new View.OnClickListener() {
                @Override
                @RequiresApi(api = Build.VERSION_CODES.M)
                public void onClick(View v) {
                    PermissionUtil.getPermission(act, perm);
                }
            }).show();
            return false;
        }
        return true;
    }

}
