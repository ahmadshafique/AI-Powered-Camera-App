package com.fyp.aipoweredcameraapp;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.fyp.aipoweredcameraapp.utils.Tools;

public class ActivityMain extends AppCompatActivity {

    private ActionBar actionBar;
    private Toolbar toolbar;
    ImageButton btn_about;
    CardView enhanced_image, facial_features, selfie_manipulation;

    private ActivityMain activityMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        activityMain = this;
        initToolbar();
        initComponent();
    }

    private void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setHomeButtonEnabled(false);
        actionBar.setTitle(R.string.app_name);

        btn_about = (ImageButton) findViewById(R.id.about);
        btn_about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Tools.showDialogAbout(activityMain);
            }
        });
    }

    private void initComponent() {
        enhanced_image = (CardView) findViewById(R.id.enhanced_image);
        enhanced_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               onClickLaunchActivity(v.getId());
            }
        });
        facial_features = (CardView) findViewById(R.id.facial_features);
        facial_features.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickLaunchActivity((v.getId()));
            }
        });
        selfie_manipulation = (CardView) findViewById(R.id.selfie_manipulation);
        selfie_manipulation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickLaunchActivity(v.getId());
            }
        });
    }

    protected void onClickLaunchActivity(int id) {
        Intent i;
        if (id == R.id.enhanced_image)
            i = new Intent(ActivityMain.this, ActivityCamera.class);
        else if (id == R.id.facial_features)
            i = new Intent(ActivityMain.this, ActivityImageSelection.class);
        else //if (id == R.id.selfie_manipulation)
            i = new Intent(ActivityMain.this, ActivityImageSelection.class);

        startActivity(i);
        //finish();
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
        doExitApp();
    }

    private long exitTime = 0;
    public void doExitApp() {
        if ((System.currentTimeMillis() - exitTime) > 2000) {
            Toast.makeText(this, R.string.press_again_exit_app, Toast.LENGTH_SHORT).show();
            exitTime = System.currentTimeMillis();
        } else {
            finish();
        }
    }

}

