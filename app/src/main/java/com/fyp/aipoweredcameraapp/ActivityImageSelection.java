package com.fyp.aipoweredcameraapp;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.fyp.aipoweredcameraapp.utils.CallbackDialog;
import com.fyp.aipoweredcameraapp.utils.DialogUtils;
import com.fyp.aipoweredcameraapp.utils.NetworkCheck;

import java.io.FileNotFoundException;
import java.io.IOException;

public class ActivityImageSelection extends AppCompatActivity {

    private ActionBar actionBar;
    private Toolbar toolbar;
    private Button reselectBtn;
    private Button uploadImgBtn;
    private Bitmap bitmap;

    private Activity activityImageSelection;
    public static final int GET_FROM_GALLERY = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityImageSelection = this;
        setContentView(R.layout.activity_image_selection);

        initToolbar();
        initReselectImage();
        initImageUpload();
        requestImageFromGallery();
    }

    private void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle(R.string.select_facial_features_from_map);
    }

    private void initReselectImage() {
        reselectBtn = (Button) findViewById(R.id.reselect_image);
        reselectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestImageFromGallery();
            }
        });
    }

    private void initImageUpload() {
        uploadImgBtn = (Button) findViewById(R.id.next);
        uploadImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage();
            }
        });
    }

    private void requestImageFromGallery() {
        startActivityForResult(new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI), GET_FROM_GALLERY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Detects request codes
        if(requestCode==GET_FROM_GALLERY && resultCode == Activity.RESULT_OK) {
            Uri path = data.getData();
            String mimeType = getContentResolver().getType(path);
            if (mimeType != null && mimeType.startsWith("image")) {
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), path);
                    ImageView img = (ImageView) findViewById(R.id.loadImageView);
                    img.setImageBitmap(bitmap);

                } catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block

                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            } else {
                requestImageFromGallery();
            }
        }
    }

    public void dialogNoInternet() {
        Dialog dialog = new DialogUtils(this).buildDialogWarning(R.string.title_no_internet, R.string.msg_no_internet, R.string.TRY_AGAIN, R.string.CLOSE, R.drawable.img_no_internet, new CallbackDialog() {
            @Override
            public void onPositiveClick(Dialog dialog) {
                dialog.dismiss();
                retryUploadImage();
            }
            @Override
            public void onNegativeClick(Dialog dialog) {
                dialog.dismiss();
                onBackPressed();
            }
        });
        dialog.show();
    }

    private void uploadImage() {
        if (!NetworkCheck.isConnect(this)) {
            dialogNoInternet();
        } else {
            //Intent intent = new Intent(ActivityFacialFeatures.this, ServicesDatabase.class);
            //intent.putExtra("Function", "upload_image");
            //intent.putExtra("image", bitmap);
            //startService(intent);
        }
    }

    private void retryUploadImage() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                uploadImage();
            }
        }, 2000);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int item_id = item.getItemId();
        if (item_id == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
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
        finish();
    }

}
