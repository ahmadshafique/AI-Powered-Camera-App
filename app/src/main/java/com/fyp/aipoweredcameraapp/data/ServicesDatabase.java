package com.fyp.aipoweredcameraapp.data;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.fyp.aipoweredcameraapp.utils.Tools;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ServicesDatabase extends Service {

    private boolean result = false;
    private boolean isService = false;
    Activity activity;

    String functionPerformed = null;
    String image = null;
    //Bitmap image = null;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return iBinder;
    }

    public void onDestroy() {
        super.onDestroy();
    }

    public void onCreate() {
        System.loadLibrary("native-lib");
    }

    public class MyServiceBinder extends Binder {
        public ServicesDatabase getService() {
            return ServicesDatabase.this;
        }
    }

    private IBinder iBinder = new MyServiceBinder();

    public int onStartCommand(Intent intent, int flags, int startId) {
        isService = false;
        try {
            functionPerformed = intent.getStringExtra("Function");

            //if (functionPerformed != null && functionPerformed.equals("image_upload"))
                //image = intent.getStringExtra("image");

        } catch (Exception e) {
            e.printStackTrace();
        }
        new backgroundTask().execute();
        //stopSelf();
        return START_NOT_STICKY;
    }

    public void StopService() {
        stopSelf();
    }

    public class backgroundTask extends AsyncTask<Void, Void, String> {
        protected void onPreExecute() {
        }

        @Override
        protected String doInBackground(Void... voids) {
            if (functionPerformed != null && functionPerformed.equals("enhance_image")) {
                final Intent intent = new Intent(".ActivityImageSection");
                //image = (Bitmap) intent.getParcelableExtra("image");
                image = (String) intent.getStringExtra("image");
                if (image != null) {
                    //Sample image
                    //Store the picture in mat object
                    Mat prev = new Mat();
                    Bitmap sampleImgBmp = Tools.getBitmap(new File((image)));
                    Utils.bitmapToMat(sampleImgBmp, prev);
                    Mat res = new Mat(prev.cols(), prev.rows(), CvType.CV_8UC3);

                    //Pass mat to native C++ function
                    synEFFromJNI(prev.getNativeObjAddr(), res.getNativeObjAddr());

                    //AiCam image
                    Bitmap aicamImgBmp = Bitmap.createBitmap(res.cols(), res.rows(), Bitmap.Config.ARGB_8888);
                    Utils.matToBitmap(res, aicamImgBmp);

                    String savePath=intent.getStringExtra("save_path");
                    ByteArrayOutputStream stream2 = new ByteArrayOutputStream();
                    aicamImgBmp.compress(Bitmap.CompressFormat.JPEG, 100, stream2);
                    byte[] data2 = stream2.toByteArray();
                    try {
                        FileOutputStream fos2 = new FileOutputStream(savePath);
                        fos2.write(data2);
                        fos2.close();

                        intent.putExtra("Function", "functionPerformed");
                        intent.putExtra("Result", true);
                        intent.putExtra("image", savePath);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                else {
                    intent.putExtra("Function", functionPerformed);
                    intent.putExtra("Result", false);
                }
                sendBroadcast(intent);
            }
            return null;
        }

        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        protected void onPostExecute(String values) {
            super.onPostExecute(values);
        }
    }

    public native void synEFFromJNI(long frame, long res);

}
