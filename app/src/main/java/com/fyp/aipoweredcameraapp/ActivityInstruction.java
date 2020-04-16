package com.fyp.aipoweredcameraapp;

import android.app.Activity;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class ActivityInstruction extends AppCompatActivity {

    private Activity activityInstruction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityInstruction = this;

    }


    @Override
    protected void onPause() {
        super.onPause();
    }
}
