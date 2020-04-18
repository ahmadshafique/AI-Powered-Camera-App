package com.fyp.aipoweredcameraapp;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.fyp.aipoweredcameraapp.data.SharedPref;
import com.fyp.aipoweredcameraapp.utils.PermissionUtil;
import com.fyp.aipoweredcameraapp.utils.Tools;

import java.util.Timer;
import java.util.TimerTask;

public class ActivitySplash extends AppCompatActivity {

    private SharedPref sharedPref;
    private boolean on_permission_result = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        sharedPref = new SharedPref(this);
        Tools.setSystemBarColor(this, "white");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onResume() {
        super.onResume();

        // permission checker for android M or higher
        if (Tools.needRequestPermission() && !on_permission_result) {
            String[] permission = PermissionUtil.getDeniedPermission(this);
            if (permission.length != 0) {
                requestPermissions(permission, 200);
            } else {
                startActivityMainDelay();
            }
        } else {
            startActivityMainDelay();
        }
    }

    private void startActivityMainDelay() {
        // Show splash screen for 2 seconds
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                Intent i = new Intent(ActivitySplash.this, ActivityMain.class);
                startActivity(i);
                finish(); // kill current activity
            }
        };
        new Timer().schedule(task, 2000);
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 200) {
            for (String perm : permissions) {
                boolean rationale = shouldShowRequestPermissionRationale(perm);
                sharedPref.setNeverAskAgain(perm, !rationale);
            }
            on_permission_result = true;
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
