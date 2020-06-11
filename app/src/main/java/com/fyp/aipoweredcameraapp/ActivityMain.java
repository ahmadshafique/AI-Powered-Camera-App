package com.fyp.aipoweredcameraapp;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.fyp.aipoweredcameraapp.data.SharedPref;
import com.fyp.aipoweredcameraapp.utils.CallbackDialog2Buttons;
import com.fyp.aipoweredcameraapp.utils.DialogUtils;
import com.fyp.aipoweredcameraapp.utils.Tools;

import java.io.File;

public class ActivityMain extends AppCompatActivity {

    private ActionBar actionBar;
    private Toolbar toolbar;
    ImageButton btn_about;
    CardView enhanced_image, facial_features, selfie_manipulation;
    private SharedPref sharedPref;

    private ActivityMain activityMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        activityMain = this;
        sharedPref = new SharedPref(this);
        initToolbar();
        initComponent();

        //this.getSharedPreferences("module_selected", Context.MODE_PRIVATE).edit().clear().apply();
        //clear previously set selfie manipulation parameters
        this.getSharedPreferences("x", Context.MODE_PRIVATE).edit().clear().apply();
        this.getSharedPreferences("y", Context.MODE_PRIVATE).edit().clear().apply();
        this.getSharedPreferences("z", Context.MODE_PRIVATE).edit().clear().apply();
        //delete temp file
        String filePath = new SharedPref(this).getStringPref("temp_file");
        if (filePath != null) {
            File imgFile = new File(filePath);
            imgFile.delete();
        }
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

    private View.OnClickListener cardOnClickListener() {
        return v -> {
            sharedPref.setIntPref("module_selected", v.getId());
            dialogGetImage();
        };
    }

    private void initComponent() {
        enhanced_image = (CardView) findViewById(R.id.enhanced_image);
        enhanced_image.setOnClickListener(cardOnClickListener());
        facial_features = (CardView) findViewById(R.id.facial_features);
        facial_features.setOnClickListener(cardOnClickListener());
        selfie_manipulation = (CardView) findViewById(R.id.selfie_manipulation);
        selfie_manipulation.setOnClickListener(cardOnClickListener());
    }

    public void dialogGetImage() {
            Dialog dialog = new DialogUtils(this).buildDialogSelection(R.string.title_get_image, R.string.msg_get_image, R.string.CAMERA, R.string.GALLERY, R.string.CLOSE, R.drawable.img_select_source, new CallbackDialog2Buttons() {
                @Override
            public void onPositiveClick(Dialog dialog) {
                //camera source
                dialog.dismiss();
                Intent i = new Intent(ActivityMain.this, ActivityCamera.class);
                startActivity(i);

                //kill current activity
                finish();
            }
            @Override
            public void onNegativeClick(Dialog dialog) {
                //gallery source
                dialog.dismiss();
                Intent i = new Intent(ActivityMain.this, ActivityImage.class);
                i.putExtra("image_source", "gallery");
                startActivity(i);

                //kill current activity
                finish();
            }
        });
        dialog.show();
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

