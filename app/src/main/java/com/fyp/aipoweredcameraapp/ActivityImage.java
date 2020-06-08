package com.fyp.aipoweredcameraapp;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.fyp.aipoweredcameraapp.data.SharedPref;
import com.fyp.aipoweredcameraapp.utils.CallbackDialog;
import com.fyp.aipoweredcameraapp.utils.DialogUtils;
import com.fyp.aipoweredcameraapp.utils.NetworkCheck;
import com.fyp.aipoweredcameraapp.utils.PermissionUtil;
import com.fyp.aipoweredcameraapp.widget.TouchImageView;
import com.google.android.material.snackbar.Snackbar;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.loopj.android.http.*;

import cz.msebera.android.httpclient.Header;

public class ActivityImage extends AppCompatActivity {

    private View rootView;
    private ActionBar actionBar;
    private Toolbar toolbar;
    private Button previousBtn;
    private Button nextBtn;
    private ImageView img;
    private int module_selected;
    private String image_source;
    private SharedPref sharedPref;
    private String filePath;

    public static final int GET_FROM_GALLERY = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rootView = getLayoutInflater().inflate(R.layout.activity_image_selection, null, false);
        setContentView(rootView);
        //setContentView(R.layout.activity_image_selection);

        sharedPref = new SharedPref(this);
        module_selected = sharedPref.getIntPref("module_selected");
        image_source = getIntent().getStringExtra("image_source");
        previousBtn = (Button) findViewById(R.id.previous);
        nextBtn = (Button) findViewById(R.id.next);

        initToolbar();
        initImageView();
        initImageSource();
        initModules();
        System.loadLibrary("native-lib");
    }
    private void initImageSource() {
        if (image_source.equals("camera"))
            initCameraSource();
        else // if (image_source.equals("gallery"))
            initGallerySource();
    }

    private void initModules() {
        if (module_selected == R.id.enhanced_image) {
            nextBtn.setText(R.string.PROCESS);
            nextBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    processImage();
                }
            });
        } else {
            nextBtn.setText(R.string.UPLOAD);
            nextBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    uploadImage();
                }
            });
        }
    }

    private void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle(R.string.image_preview);
    }

    private void initImageView() {
        img = (TouchImageView) findViewById(R.id.loadImageView);
        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = 0;
                ArrayList<String> images_list = new ArrayList<>();
                images_list.add(filePath);

                Intent i = new Intent(ActivityImage.this, ActivityFullScreenImage.class);
                i.putExtra(ActivityFullScreenImage.EXTRA_POS, pos);
                i.putStringArrayListExtra(ActivityFullScreenImage.EXTRA_IMGS, images_list);
                startActivity(i);
            }
        });
        img.setImageDrawable(getDrawable(R.drawable.loading_placeholder));
        img.setEnabled(false);
    }

    private void initCameraSource() {
        filePath = getIntent().getStringExtra("filePath");
        if (filePath.isEmpty())
            Snackbar.make(rootView,"Image file is empty or not valid", Snackbar.LENGTH_INDEFINITE).setAction("RETRY", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    previousBtn.performClick();
                }
            }).show();
        else {
            Glide.with(img.getContext()).load(filePath).into(img);
            img.setEnabled(true);
            //mark delete temp file
            sharedPref.setStringPref("temp_file", filePath);
        }
        previousBtn.setText(R.string.RETAKE);
        previousBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToActivity(ActivityCamera.class);
            }
        });
    }

    private void initGallerySource() {
        nextBtn.setEnabled(false);
        requestImageFromGallery();
        previousBtn.setText(R.string.RESELECT);
        previousBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextBtn.setEnabled(false);
                previousBtn.setText(R.string.RESELECT);
                actionBar.setTitle(R.string.image_preview);
                initModules();
                initImageView();
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
            filePath = data.getData().toString();
            String mimeType = getContentResolver().getType(Uri.parse(filePath));
            if (mimeType != null && mimeType.startsWith("image")) {
                Glide.with(img.getContext()).load(filePath).into(img);
                nextBtn.setEnabled(true);
                img.setEnabled(true);
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

    private void processImage() {
        Bitmap sampleImgBmp = ((BitmapDrawable)img.getDrawable()).getBitmap();
        enhanceImage runner = new enhanceImage();
        runner.execute(sampleImgBmp);
    }

    private class enhanceImage extends AsyncTask<Bitmap, String, Bitmap> {

        ProgressDialog progressDialog;
        Bitmap sampleImgBmp = null;
        Bitmap aicamImgBmp = null;

        @Override
        protected Bitmap doInBackground(Bitmap... params) {
            //publishProgress("Processing..."); // Calls onProgressUpdate()
            try {
                //Sample image
                //Store the picture in mat object
                Mat prev = new Mat();
                sampleImgBmp = params[0];
                Utils.bitmapToMat(sampleImgBmp, prev);
                Mat res = new Mat(prev.cols(), prev.rows(), CvType.CV_8UC3);

                //Pass mat to native C++ function
                synEFFromJNI(prev.getNativeObjAddr(), res.getNativeObjAddr());

                //AiCam image
                aicamImgBmp = Bitmap.createBitmap(res.cols(), res.rows(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(res, aicamImgBmp);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return aicamImgBmp;
        }
        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(ActivityImage.this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER); // Progress Dialog Style Spinner
            progressDialog.setCancelable(false);
            progressDialog = ProgressDialog.show(ActivityImage.this,
                    "Image Enhancement",
                    "Processing...");
        }
        @Override
        protected void onPostExecute(Bitmap result) {
            // execution of result of Long time consuming operation
            progressDialog.dismiss();
            Toast.makeText(rootView.getContext(), "Photo processing completed", Toast.LENGTH_LONG).show();

            actionBar.setTitle("Final Image");
            Glide.with(img.getContext()).load(aicamImgBmp).into(img);
            previousBtn.setText(R.string.DISCARD);
            nextBtn.setText(R.string.SAVE);
            nextBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    saveImage(sampleImgBmp, aicamImgBmp);

                    previousBtn.setText(R.string.BACK);
                    nextBtn.setEnabled(false);
                    previousBtn.performClick();
                }
            });
            img.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = 0;
                    ArrayList<String> images_list = new ArrayList<>();
                    Bitmap tmpBmp = aicamImgBmp.copy(aicamImgBmp.getConfig(), true);
                    images_list.add(BitMapTempFile(tmpBmp));
                    images_list.add(filePath);

                    Intent i = new Intent(ActivityImage.this, ActivityFullScreenImage.class);
                    i.putExtra(ActivityFullScreenImage.EXTRA_POS, pos);
                    i.putStringArrayListExtra(ActivityFullScreenImage.EXTRA_IMGS, images_list);
                    startActivity(i);
                }
            });
        }
        @Override
        protected void onProgressUpdate(String... text) { }

        private String BitMapTempFile(Bitmap bitmap){
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] b = baos.toByteArray();
            File f = null;
            try {
                f = File.createTempFile("img_temp", ".jpg", getCacheDir());
                FileOutputStream fos = new FileOutputStream(f);
                fos.write(b);
                fos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return f.getAbsolutePath();
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
            //TODO
            Toast.makeText(this, "TODO", Toast.LENGTH_LONG).show();
            //if (module_selected == R.id.facial_features)
                //ay module
            //else // if (module_selected == R.id.selfie_manipulation)
                //mr module

            String transform_prnet_url = "camai-transform-prnet.herokuapp.com";
            AsyncHttpClient client = new AsyncHttpClient();
			client.addHeader("Connection", "Keep-Alive");
			client.setTimeout(120 * 1000);
			client.setConnectTimeout(120 * 1000);
			client.setResponseTimeout(120 * 1000);
            RequestParams params = new RequestParams();
            params.put("x", -50);
            params.put("y", 0);
            params.put("z", 250);
            String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/CamAI/";
            File sourceFile = new File(path + "trump.obj");
                File targetFile = new File(path + "trump.jpg");
			try {
         		Toast.makeText(getBaseContext(), path + "trump.obj", Toast.LENGTH_LONG).show();
                params.put("source", sourceFile);
                params.put("target", targetFile);
            } catch (FileNotFoundException e) {
                Toast.makeText(getBaseContext(), "File not found", Toast.LENGTH_LONG).show();
            }
            client.post(transform_prnet_url, new FileAsyncHttpResponseHandler(this) {
                @Override
                public void onSuccess(int statusCode, Header[] headers, File file) {
                    //show img
                    if (statusCode == 200)
                        Glide.with(img.getContext()).load(file).into(img);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, File file) {
                    //failure toast
                    Toast.makeText(getBaseContext(), "Error occured", Toast.LENGTH_LONG).show();
                    ;
                }
            });
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

    private void saveImage(Bitmap sampleImgBmp, Bitmap aicamImgBmp) {
        if (!PermissionUtil.checkPermission(ActivityImage.this, rootView,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                "Write permission required."))
            return;

        //String root = Environment.getExternalStorageDirectory().getPath();
        File baseFolder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "CamAI");
        if(!baseFolder.exists()){
            baseFolder.mkdirs();
        }

        ByteArrayOutputStream stream1 = new ByteArrayOutputStream();
        sampleImgBmp.compress(Bitmap.CompressFormat.JPEG, 100, stream1);
        byte[] data1 = stream1.toByteArray();
        ByteArrayOutputStream stream2 = new ByteArrayOutputStream();
        aicamImgBmp.compress(Bitmap.CompressFormat.JPEG, 100, stream2);
        byte[] data2 = stream2.toByteArray();

        //writing image files
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String currentDateandTime = sdf.format(new Date());
        String sampleFileName = baseFolder.getPath() + "/sample_picture_" + currentDateandTime + ".jpg";
        String camAiFileName = baseFolder.getPath() + "/camAI_picture_" + currentDateandTime + ".jpg";

        try {
            FileOutputStream fos1 = new FileOutputStream(sampleFileName);
            fos1.write(data1);
            fos1.close();
            FileOutputStream fos2 = new FileOutputStream(camAiFileName);
            fos2.write(data2);
            fos2.close();

            String msg = "Photos saved at " + baseFolder.getAbsolutePath();
            Toast.makeText(rootView.getContext(), msg, Toast.LENGTH_LONG).show();

            MediaScannerConnection.scanFile(ActivityImage.this,
                    new String[] { sampleFileName, camAiFileName }, null, null);

        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void goToActivity (Class actClass){
        Intent i = new Intent(ActivityImage.this, actClass);
        startActivity(i);

        //kill current activity
        finish();
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
    protected void onPause() { super.onPause(); }

    @Override
    protected void onResume() { super.onResume(); }

    @Override
    public void onStart() { super.onStart(); }

    @Override
    protected void onDestroy() { super.onDestroy(); }

    @Override
    public void onStop() { super.onStop(); }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        goToActivity(ActivityMain.class);
    }

    public native void synEFFromJNI(long frame, long res);

}
