package com.fyp.aipoweredcameraapp;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.fyp.aipoweredcameraapp.data.SharedPref;
import com.fyp.aipoweredcameraapp.utils.CallbackDialog2Buttons;
import com.fyp.aipoweredcameraapp.utils.CallbackDialog4Buttons;
import com.fyp.aipoweredcameraapp.utils.DialogUtils;
import com.fyp.aipoweredcameraapp.utils.NetworkCheck;
import com.fyp.aipoweredcameraapp.utils.PermissionUtil;
import com.fyp.aipoweredcameraapp.utils.Tools;
import com.fyp.aipoweredcameraapp.widget.TouchImageView;
import com.google.android.material.snackbar.Snackbar;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

import com.loopj.android.http.*;
import cz.msebera.android.httpclient.Header;

public class ActivityImage extends AppCompatActivity {

    private View rootView;
    private ActionBar actionBar;
    private Toolbar toolbar;
    private Button previousBtn;
    private Button nextBtn;
    private ImageView img;
    private ProgressDialog progressDialog;
    private int module_selected;
    private String image_source;
    private SharedPref sharedPref;
    private String filePath;
	private Uri fileUri;
    private ArrayList<String> urls;
    private boolean readFileURLs;

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
        progressDialog = new ProgressDialog(ActivityImage.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER); // Progress Dialog Style Spinner
        progressDialog.setCancelable(false);
        readFileURLs = true;

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
        nextBtn.setText(R.string.PROCESS);
        if (module_selected == R.id.enhanced_image) {
            nextBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    processImage();
                }
            });
        } else {
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
                images_list.add(fileUri.toString());

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
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setDataAndType(MediaStore.Images.Media.INTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(intent, GET_FROM_GALLERY);
        //startActivityForResult(Intent.createChooser(intent, "Select picture"), GET_FROM_GALLERY );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Detects request codes
        if(requestCode==GET_FROM_GALLERY && resultCode == Activity.RESULT_OK) {
            filePath = Tools.getRealPathFromURI(this, data.getData());
			fileUri = data.getData();
            //Toast.makeText(getBaseContext(), filePath, Toast.LENGTH_LONG).show();
            Glide.with(img.getContext()).load(fileUri).into(img);
            nextBtn.setEnabled(true);
            img.setEnabled(true);
        }
    }

    private void processImage() {
        Bitmap sampleImgBmp = ((BitmapDrawable)img.getDrawable()).getBitmap();
        enhanceImage runner = new enhanceImage();
        runner.execute(sampleImgBmp);
    }

    private class enhanceImage extends AsyncTask<Bitmap, String, Bitmap> {

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
            progressDialog = ProgressDialog.show(ActivityImage.this,
                    "Image Enhancement",
                    "Processing...please wait.");
        }
        @Override
        protected void onPostExecute(Bitmap result) {
            // execution of result of Long time consuming operation
            progressDialog.dismiss();
            Toast.makeText(getBaseContext(), "Photo processing completed", Toast.LENGTH_LONG).show();

            Glide.with(img.getContext()).load(aicamImgBmp).into(img);
            onSuccessUpdates(sampleImgBmp, aicamImgBmp);
        }
        @Override
        protected void onProgressUpdate(String... text) { }

    }

    public void dialogNoInternet() {
        Dialog dialog = new DialogUtils(this).buildDialogWarning(R.string.title_no_internet, R.string.msg_no_internet, R.string.TRY_AGAIN, R.string.CLOSE, R.drawable.img_no_internet, new CallbackDialog2Buttons() {
            @Override
            public void onPositiveClick(Dialog dialog) {
                dialog.dismiss();
                retryUploadImage();
            }
            @Override
            public void onNegativeClick(Dialog dialog) {
                dialog.dismiss();
                //onBackPressed();
            }
        });
        dialog.show();
    }

    private void uploadImage() {
        if (!NetworkCheck.isConnect(this)) {
            dialogNoInternet();
        } else {
            if (readFileURLs) {
                if (!PermissionUtil.checkPermission(ActivityImage.this, rootView,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        "Read permission required to read urls file."))
                    return;
                urls = Tools.getServerURLs(this);
                if (urls.size() < 2) {
                    Toast.makeText(this, "Server URLs not valid", Toast.LENGTH_LONG).show();
                    return;
                }
            }
            if (module_selected == R.id.facial_features)
                editFacialFeatures();
            else {// if (module_selected == R.id.selfie_manipulation)
                manipulateSelfie();
            }
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

    private void editFacialFeatures() {
        Dialog dialog = new DialogUtils(this).buildDialogFacialFeatures(getString(R.string.facial_features), getString(R.string.ff_options), getString(R.string.hair_ff), getString(R.string.bald_ff), getString(R.string.young_ff), getString(R.string.old_ff), getString(R.string.CLOSE), new CallbackDialog4Buttons() {
            @Override
            public void onButton1Click(Dialog dialog) {
                //hair selection case
                dialog.dismiss();
                editFacialFeaturesSelection(getString(R.string.hair_ff).toLowerCase());
            }
            @Override
            public void onButton2Click(Dialog dialog) {
                //bald selection case
                dialog.dismiss();
                editFacialFeaturesSelection(getString(R.string.bald_ff).toLowerCase());
            }
            @Override
            public void onButton3Click(Dialog dialog) {
                //young selection case
                dialog.dismiss();
                editFacialFeaturesSelection(getString(R.string.young_ff).toLowerCase());
            }
            @Override
            public void onButton4Click(Dialog dialog) {
                //old selection case
                editFacialFeaturesSelection(getString(R.string.old_ff).toLowerCase());
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void editFacialFeaturesSelection(String selection) {
        progressDialog = ProgressDialog.show(ActivityImage.this,
                "Facial Feature Editing",
                "Processing...please wait.");
        String url = "";
        if (readFileURLs) {
            url = urls.get(0) + "?todo=" + selection;
        } else {
            url = getString(R.string.camai_edit_facial_features_url)+"?todo="+selection;
        }
        //Toast.makeText(getBaseContext(), url, Toast.LENGTH_LONG).show();

        RequestParams params = new RequestParams();
        try {
            params.put("source", new File(filePath));
            //Toast.makeText(getBaseContext(), "File found " + filePath, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(getBaseContext(), "File not found " + filePath, Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

        sendHttpPostRequest(url, params);
    }

    private void manipulateSelfie() {
        Dialog dialog = new DialogUtils(this).buildDialogSelfieManipulation(getString(R.string.selfie_manipulation), getString(R.string.sm_parameters), getString(R.string.UPLOAD), getString(R.string.CLOSE), new CallbackDialog2Buttons() {
            @Override
            public void onPositiveClick(Dialog dialog) {
                if (!Tools.validateSelfieManipulationParameters(getBaseContext(), dialog, sharedPref))
                    return;
                String x = ((EditText) dialog.findViewById(R.id.edt_sm1)).getText().toString();     //eye left right
                String y = ((EditText) dialog.findViewById(R.id.edt_sm2)).getText().toString();     //head up down
                String z = ((EditText) dialog.findViewById(R.id.edt_sm3)).getText().toString();     //distance from camera
                dialog.dismiss();
                progressDialog = ProgressDialog.show(ActivityImage.this,
                        "Selfie Manipulation",
                        "Processing...please wait.");

                String url = "";
                if (readFileURLs) {
                    url = urls.get(0)+"/both?x="+x+"&y="+y+"&z="+z;
                } else {
                    url = getString(R.string.camai_transform_selfie_url)+"/both?x="+x+"&y="+y+"&z="+z;
                }
                //Toast.makeText(getBaseContext(), url, Toast.LENGTH_LONG).show();

                RequestParams params = new RequestParams();
                try {
                    params.put("target", new File(filePath));
                    //Toast.makeText(getBaseContext(), "File found " + filePath, Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    Toast.makeText(getBaseContext(), "File not found " + filePath, Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }

                sendHttpPostRequest(url, params);
            }
            @Override
            public void onNegativeClick(Dialog dialog) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void sendHttpPostRequest(String url, RequestParams params) {
        AsyncHttpClient client = new AsyncHttpClient();
        client.setTimeout(180 * 1000);
        client.setConnectTimeout(180 * 1000);
        client.setResponseTimeout(180 * 1000);
        String[] allowedTypes = new String[] { "image/png", "image/jpg", "text/html; charset=utf-8", "text/plain" };
        client.post(this, url, params,  new BinaryHttpResponseHandler(allowedTypes) {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] binaryData) {
                if (statusCode==200) {
                    progressDialog.dismiss();
                    try {
                        //File f = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getPath()+"/CamAI/response");
                        File f = File.createTempFile("img_temp", ".jpg", getCacheDir());
                        FileOutputStream fos = new FileOutputStream(f);
                        fos.write(binaryData);
                        fos.close();
                        Toast.makeText(getBaseContext(), "Photo processing completed", Toast.LENGTH_LONG).show();
                        Glide.with(img.getContext()).load(f).into(img);

                        Bitmap sampleImgBmp = Tools.getBitmap(new File(filePath));
                        Bitmap aicamImgBmp = Tools.getBitmap(f);

                        onSuccessUpdates(sampleImgBmp, aicamImgBmp);
                        if (module_selected == R.id.selfie_manipulation) {
                            onSuccessAdditionalUpdates(url);
                        }
                    } catch (Exception e) {
                        Toast.makeText(getBaseContext(), "response temp file write error", Toast.LENGTH_LONG).show();
                    }
                }
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] binaryData, Throwable error) {
                progressDialog.dismiss();
                //toast server error here
                String http_request_error = String.valueOf(statusCode) + "  " + error.toString();
                String server_error = "";
                try {
                    server_error = binaryData.toString();
                    Toast.makeText(getBaseContext(),  server_error + " " + http_request_error , Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    Toast.makeText(getBaseContext(),  "Server is down " + http_request_error, Toast.LENGTH_LONG).show();
                }
                Log.d("http request", error.toString());
            }
        });
    }

    private void onSuccessUpdates(Bitmap sampleImgBmp, Bitmap aicamImgBmp) {
        actionBar.setTitle(R.string.final_output_image);
        previousBtn.setText(R.string.DISCARD);
        nextBtn.setText(R.string.SAVE);
        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!PermissionUtil.checkPermission(ActivityImage.this, rootView,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        "Write permission required."))
                    return;

                Tools.saveImage(getBaseContext(), sampleImgBmp, aicamImgBmp, image_source);

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
                images_list.add(Tools.BitMapTempFile(getBaseContext(), tmpBmp).getAbsolutePath());
                images_list.add(filePath);

                Intent i = new Intent(ActivityImage.this, ActivityFullScreenImage.class);
                i.putExtra(ActivityFullScreenImage.EXTRA_POS, pos);
                i.putStringArrayListExtra(ActivityFullScreenImage.EXTRA_IMGS, images_list);
                startActivity(i);
            }
        });
    }

    private void onSuccessAdditionalUpdates(String url) {
        nextBtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                manipulateSelfie();
                return true;
            }
        });

        //set old values
        String[] tokens = url.split("[=&]+");
        sharedPref.setIntPref("x", Integer.parseInt(tokens[1]));
        sharedPref.setIntPref("y", Integer.parseInt(tokens[3]));
        sharedPref.setIntPref("z", Integer.parseInt(tokens[5]));
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
