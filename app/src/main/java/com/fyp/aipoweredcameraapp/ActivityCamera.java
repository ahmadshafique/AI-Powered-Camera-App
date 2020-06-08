package com.fyp.aipoweredcameraapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.fyp.aipoweredcameraapp.data.SharedPref;

import java.io.File;

public class ActivityCamera extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        //delete temp file
        String filePath = new SharedPref(this).getStringPref("temp_file");
        if (filePath != null) {
            File imgFile = new File(filePath);
            imgFile.delete();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Intent i = new Intent(ActivityCamera.this, ActivityMain.class);
        startActivity(i);

        finish();
    }

}
