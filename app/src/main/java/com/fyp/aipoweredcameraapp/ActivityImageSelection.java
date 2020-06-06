package com.fyp.aipoweredcameraapp;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.fyp.aipoweredcameraapp.data.ServicesDatabase;
import com.fyp.aipoweredcameraapp.data.SharedPref;
import com.fyp.aipoweredcameraapp.utils.CallbackDialog;
import com.fyp.aipoweredcameraapp.utils.DialogUtils;
import com.fyp.aipoweredcameraapp.utils.NetworkCheck;
import com.fyp.aipoweredcameraapp.utils.PermissionUtil;
import com.fyp.aipoweredcameraapp.utils.Tools;
import com.google.android.material.snackbar.Snackbar;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.io.File;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

//import com.sun.imageio.plugins.jpeg.JPEG;
//import java.util.*;


public class ActivityImageSelection extends AppCompatActivity {

    private View rootView;
    private ActionBar actionBar;
    private Toolbar toolbar;
    private Button previousBtn;
    private Button nextBtn;
    private ImageView img;
    private ProgressDialog progressDialog;
    private int module_selected;
    private String image_source;
    private Uri imgPath;
    private SharedPref sharedPref;
    BroadcastReceiver broadcastReceiver;
    ServicesDatabase servicesDatabase;
    ServiceConnection serviceConnection;

    public static final int GET_FROM_GALLERY = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rootView = getLayoutInflater().inflate(R.layout.activity_image_selection, null, false);
        setContentView(rootView);
        //setContentView(R.layout.activity_image_selection);

        sharedPref = new SharedPref(this);
        module_selected = sharedPref.getPref("module_selected");
        image_source = getIntent().getStringExtra("image_source");
        img = (ImageView) findViewById(R.id.loadImageView);
        previousBtn = (Button) findViewById(R.id.previous);
        nextBtn = (Button) findViewById(R.id.next);
        progressDialog = new ProgressDialog(ActivityImageSelection.this);

        checkService();
        initToolbar();
        if (image_source.equals("camera"))
            initCameraSource();
        else // if (image_source.equals("gallery"))
            initGallerySource();

        if (module_selected == R.id.enhanced_image)
            initEnhanceImage();
        else if (module_selected == R.id.facial_features)
            initEditFacialFeatures();
        else // if (module_selected == R.id.selfie_manipulation)
            initSelfieManipulation();

        System.loadLibrary("native-lib");
    }

    public void checkService()
    {
        if(serviceConnection==null)
        {
            serviceConnection=new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    ServicesDatabase.MyServiceBinder myServiceBinder=(ServicesDatabase.MyServiceBinder)service;
                    servicesDatabase=myServiceBinder.getService();
                }
                @Override
                public void onServiceDisconnected(ComponentName name) {

                }
            };
        }
        Intent serviceIntent=new Intent(ActivityImageSelection.this, ServicesDatabase.class);
        bindService(serviceIntent,serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void initEnhanceImage() {
        nextBtn.setText(R.string.PROCESS);
        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                processImage();
            }
        });
    }

    private void initEditFacialFeatures() {
        nextBtn.setText(R.string.UPLOAD);
        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage();
            }
        });
    }

    private void initSelfieManipulation() {
        nextBtn.setText(R.string.UPLOAD);
        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage();
            }
        });
    }

    private void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle(R.string.image_preview);
    }

    private void initCameraSource() {
        String filePath = getIntent().getStringExtra("filePath");
        if (filePath.isEmpty())
            Snackbar.make(rootView,"Image file is empty or not valid", Snackbar.LENGTH_INDEFINITE).setAction("RETRY", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    previousBtn.performClick();
                }
            }).show();
        else {
            imgPath = Uri.parse(filePath);
            //img.setImageURI(imgPath);
            Glide.with(img.getContext())
                    .load(imgPath.getPath())
                    .into(img);
        }
        previousBtn.setText(R.string.RETAKE);
        previousBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //delete previous temp file
                File imgFile = new File(imgPath.getPath());
                imgFile.delete();

                Intent i = new Intent(ActivityImageSelection.this, ActivityCamera.class);
                startActivity(i);

                //kill current activity
                finish();
            }
        });
    }

    private void initGallerySource() {
        requestImageFromGallery();
        previousBtn.setText(R.string.RESELECT);
        previousBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestImageFromGallery();
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
            imgPath = data.getData();
            String mimeType = getContentResolver().getType(imgPath);
            if (mimeType != null && mimeType.startsWith("image")) {
                img.setImageURI(imgPath);
            } else {
                Snackbar.make(rootView,"Select an image from gallery", Snackbar.LENGTH_INDEFINITE).setAction("RETRY", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        requestImageFromGallery();
                    }
                }).show();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void getFileSystemPermission() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE))
            PermissionUtil.showSystemDialogPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        else
            PermissionUtil.goToPermissionSettingScreen(this);
    }

    private void processImage() {

        //check file system permission
        if (!PermissionUtil.isGranted(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Snackbar.make(rootView,"File write permission required", Snackbar.LENGTH_INDEFINITE).setAction("GRANT PERMISSION", new View.OnClickListener() {
                @Override
                @RequiresApi(api = Build.VERSION_CODES.M)
                public void onClick(View v) {
                    getFileSystemPermission();
                }
            }).show();
            return;
        }

        //String root = Environment.getExternalStorageDirectory().getPath();
        File folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "CamAI");
        if(!folder.exists()){
            folder.mkdirs();
        }


        progressDialog.setTitle("Image Enhancement"); // Setting Title
        progressDialog.setMessage("Processing..."); // Setting Message
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER); // Progress Dialog Style Spinner
        progressDialog.show(); // Display Progress Dialog
        progressDialog.setCancelable(false);


        Intent intent = new Intent(ActivityImageSelection.this, ServicesDatabase.class);
        Bitmap smplImgBmp = ((BitmapDrawable)img.getDrawable()).getBitmap();

        try {
            //writing image files
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
            String currentDateandTime = sdf.format(new Date());
            String sampleFileName = folder.getPath() + "/sample_picture_" + currentDateandTime + ".jpg";
            String camAiFileName = folder.getPath() + "/CamAI_sample_picture_" + currentDateandTime + ".jpg";

            ByteArrayOutputStream stream1 = new ByteArrayOutputStream();
            smplImgBmp.compress(Bitmap.CompressFormat.JPEG, 100, stream1);
            byte[] data1 = stream1.toByteArray();
            FileOutputStream fos1 = new FileOutputStream(sampleFileName);
            fos1.write(data1);
            fos1.close();

            intent.putExtra("Function", "enhance_image");
            intent.putExtra("image", sampleFileName);
            intent.putExtra("save_path", camAiFileName);
            startService(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }

        /*CountDownLatch latch = new CountDownLatch(1);
        Runnable taskIE = () -> {
            try {
                //Sample image
                //Store the picture in mat object
                Mat prev = new Mat();
                Bitmap sampleImgBmp = ((BitmapDrawable)img.getDrawable()).getBitmap();
                Utils.bitmapToMat(sampleImgBmp, prev);
                ByteArrayOutputStream stream1 = new ByteArrayOutputStream();
                sampleImgBmp.compress(Bitmap.CompressFormat.JPEG, 100, stream1);
                byte[] data1 = stream1.toByteArray();
                Mat res = new Mat(prev.cols(), prev.rows(), CvType.CV_8UC3);

                //Pass mat to native C++ function
                synEFFromJNI(prev.getNativeObjAddr(), res.getNativeObjAddr());

                //AiCam image
                Bitmap aicamImgBmp = Bitmap.createBitmap(res.cols(), res.rows(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(res, aicamImgBmp);
                ByteArrayOutputStream stream2 = new ByteArrayOutputStream();
                aicamImgBmp.compress(Bitmap.CompressFormat.JPEG, 100, stream2);
                byte[] data2 = stream2.toByteArray();


                //writing image files
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
                String currentDateandTime = sdf.format(new Date());
                String sampleFileName = folder.getPath() + "/sample_picture_" + currentDateandTime + ".jpg";
                String camAiFileName = folder.getPath() + "/CamAI_sample_picture_" + currentDateandTime + ".jpg";

                FileOutputStream fos1 = new FileOutputStream(sampleFileName);
                fos1.write(data1);
                fos1.close();
                FileOutputStream fos2 = new FileOutputStream(camAiFileName);
                fos2.write(data2);
                fos2.close();

            } catch (Exception e) {
                e.printStackTrace();
            }

            //latch.countDown();
            //previousBtn.performClick();
        };
        progressDialog.dismiss();

        //Executor ext = Executors.newSingleThreadExecutor();
        ExecutorService extServoce = Executors.newSingleThreadExecutor();
        extServoce.execute(taskIE);
        try {
            extServoce.awaitTermination(30, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String msg = "Photo capture succeeded at" ;//+ folder.getAbsolutePath();
        Toast.makeText(rootView.getContext(), msg, Toast.LENGTH_LONG).show();
        ext.execute(taskIE);
        try {
            latch.await();
            String msg = "Photo capture succeeded at" ;//+ folder.getAbsolutePath();
            Toast.makeText(rootView.getContext(), msg, Toast.LENGTH_LONG).show();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
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
            /*int pos = 0;
            ArrayList<String> images_list = new ArrayList<>();
            images_list.add(imgPath.getPath());

            Intent i = new Intent(ActivityImageSelection.this, ActivityFullScreenImage.class);
            i.putExtra(ActivityFullScreenImage.EXTRA_POS, pos);
            i.putStringArrayListExtra(ActivityFullScreenImage.EXTRA_IMGS, images_list);
            startActivity(i);*/
            //Intent intent = new Intent(ActivityImageSelection.this, ServicesDatabase.class);
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

        IntentFilter i=new IntentFilter(".ActivityImageSection");
        broadcastReceiver=new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String function=intent.getStringExtra("Function");
                Toast.makeText(rootView.getContext(), function, Toast.LENGTH_LONG).show();
                boolean result=intent.getBooleanExtra("Result",false);
                if(function.equals("functionPerformed")) {
                    if (result) {
                        unbindService(serviceConnection);
                        servicesDatabase.StopService();

                        //Bitmap aicamImgBmp =  (Bitmap) intent.getParcelableExtra("image");
                        String aicamImg = intent.getStringExtra("image");
                        //img.setImageURI(imgPath);
                        Glide.with(img.getContext())
                                .load(aicamImg)
                                .into(img);

                        progressDialog.dismiss();
                        String msg = "Photo capture succeeded at";//+ folder.getAbsolutePath();
                        Toast.makeText(rootView.getContext(), msg, Toast.LENGTH_LONG).show();

                        //dialogSuccess();
                    } else {
                        Toast.makeText(ActivityImageSelection.this, "Error Occured in enhance image. Please try again", Toast.LENGTH_LONG).show();
                    }
                }
            }
        };
        this.registerReceiver(broadcastReceiver,i);
    }

    /*public void dialogSuccess(String code) {
        progressDialog.dismiss();
        Dialog dialog = new DialogUtils(this).buildDialogInfo(
                getString(R.string.success_checkout),
                String.format(getString(R.string.msg_success_checkout), code),
                getString(R.string.OK),
                R.drawable.img_checkout_success,
                new CallbackDialog() {
                    @Override
                    public void onPositiveClick(Dialog dialog) {
                        finish();
                        dialog.dismiss();
                    }

                    @Override
                    public void onNegativeClick(Dialog dialog) {
                    }
                });
        dialog.show();
    }*/

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        /*if(!isChangingConfigurations()) {
            deleteTempFiles(getCacheDir());
        }*/
    }

    @Override
    public void onStop() {
        super.onStop();
        if(!isChangingConfigurations()) {
            deleteTempFiles(getCacheDir());
        }
    }

    private boolean deleteTempFiles(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.isDirectory()) {
                        deleteTempFiles(f);
                    } else {
                        f.delete();
                    }
                }
            }
        }
        return file.delete();
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();

        if (image_source.equals("camera") && imgPath != null) {
            File imgFile = new File(imgPath.getPath());
            imgFile.delete();
        }
        Intent i = new Intent(ActivityImageSelection.this, ActivityMain.class);
        startActivity(i);

        finish();
    }

    public native void synEFFromJNI(long frame, long res);

}
