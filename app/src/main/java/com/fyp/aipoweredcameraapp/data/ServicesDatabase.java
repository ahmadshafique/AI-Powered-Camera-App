package com.fyp.aipoweredcameraapp.data;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;

import androidx.annotation.Nullable;

public class ServicesDatabase extends Service {

    private boolean result = false;
    private boolean isService = false;
    Activity activity;

    String functionPerformed = null;
    Bitmap image = null;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return iBinder;
    }

    public void onDestroy() {
        super.onDestroy();
    }

    public void onCreate() {    }

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


            if (functionPerformed != null && functionPerformed.equals("image_upload"))
                image = (Bitmap) intent.getParcelableExtra("image");
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
            if (functionPerformed != null && functionPerformed.equals("image_upload")) {
                final Intent intent = new Intent(".ActivityFullScreenImage");
                if (image != null) {
                    intent.putExtra("image", image);
                    intent.putExtra("Result", true);

                    //FULL SCREEN INTENT CALL
//                    Intent i = new Intent(ActivityProductDetails.this, ActivityFullScreenImage.class);
//                    i.putExtra(ActivityFullScreenImage.EXTRA_POS, pos);
//                    i.putStringArrayListExtra(ActivityFullScreenImage.EXTRA_IMGS, images_list);
//                    startActivity(i);
                }
                else {
                    //if (task.getException() != null) {
                    //intent.putExtra("Error", task.getException().getMessage());
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
}
