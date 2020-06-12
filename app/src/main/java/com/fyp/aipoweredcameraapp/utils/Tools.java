package com.fyp.aipoweredcameraapp.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.fyp.aipoweredcameraapp.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.fyp.aipoweredcameraapp.data.SharedPref;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

public class Tools {

    public static boolean needRequestPermission() {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void setSystemBarColor(Activity act, int color) {
        if (isLolipopOrHigher()) {
            Window window = act.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(color);
        }
    }

    public static void setSystemBarColor(Activity act, String color) {
        setSystemBarColor(act, Color.parseColor(color));
    }

    public static void setSystemBarColorDarker(Activity act, String color) {
        setSystemBarColor(act, colorDarker(Color.parseColor(color)));
    }

    public static boolean isLolipopOrHigher() {
        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP);
    }

    public static void systemBarLolipop(Activity act) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = act.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(act.getResources().getColor(R.color.colorPrimaryDark));
        }
    }

    public static void rateAction(Activity activity) {
        Uri uri = Uri.parse("market://details?id=" + activity.getPackageName());
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        try {
            activity.startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + activity.getPackageName())));
        }
    }

    public static void showDialogAbout(Activity activity) {
        Dialog dialog = new DialogUtils(activity).buildDialogInfo(R.string.title_about, R.string.content_about, R.string.OK, R.drawable.img_about, new CallbackDialog2Buttons() {
            @Override
            public void onPositiveClick(Dialog dialog) {
                dialog.dismiss();
            }

            @Override
            public void onNegativeClick(Dialog dialog) {
            }
        });
        dialog.show();
    }

    /**
     * For device info parameters
     */
    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return model;
        } else {
            return manufacturer + " " + model;
        }
    }

    public static String getAndroidVersion() {
        return Build.VERSION.RELEASE + "";
    }

    public static int getVersionCode(Context ctx) {
        try {
            PackageManager manager = ctx.getPackageManager();
            PackageInfo info = manager.getPackageInfo(ctx.getPackageName(), 0);
            return info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            return -1;
        }
    }

    public static String getVersionNamePlain(Context ctx) {
        try {
            PackageManager manager = ctx.getPackageManager();
            PackageInfo info = manager.getPackageInfo(ctx.getPackageName(), 0);
            return info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return ctx.getString(R.string.version_unknown);
        }
    }

    public static String getFormattedDate(Long dateTime) {
        SimpleDateFormat newFormat = new SimpleDateFormat("MMMM dd, yyyy hh:mm");
        return newFormat.format(new Date(dateTime));
    }

    public static String getFormattedDateSimple(Long dateTime) {
        SimpleDateFormat newFormat = new SimpleDateFormat("MMM dd, yyyy");
        return newFormat.format(new Date(dateTime));
    }

    public static void displayImageOriginal(Context ctx, ImageView img, String url) {
        try {
            Glide.with(ctx).load(url)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(img);
        } catch (Exception e) {
        }
    }

    public static void displayImageThumbnail(Context ctx, ImageView img, String url, float thumb) {
        try {
            Glide.with(ctx).load(url)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .thumbnail(thumb)
                    .into(img);
        } catch (Exception e) {

        }
    }

    public static int colorDarker(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= 0.9f; // value component
        return Color.HSVToColor(hsv);
    }

    public static int colorBrighter(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] /= 0.8f; // value component
        return Color.HSVToColor(hsv);
    }

    public static Bitmap getBitmap(File file) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        return BitmapFactory.decodeFile(file.getAbsolutePath(), options);
    }

    public static String getVersionName(Context ctx) {
        try {
            PackageManager manager = ctx.getPackageManager();
            PackageInfo info = manager.getPackageInfo(ctx.getPackageName(), 0);
            return ctx.getString(R.string.app_version) + " " + info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return ctx.getString(R.string.version_unknown);
        }
    }

    public static String getDeviceID(Context context) {
        String deviceID = Build.SERIAL;
        if (deviceID == null || deviceID.trim().isEmpty() || deviceID.equals("unknown")) {
            try {
                deviceID = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
            } catch (Exception e) {
            }
        }
        return deviceID;
    }

    public static void saveImage(Context context, Bitmap sampleImgBmp, Bitmap aicamImgBmp, String imageSource) {

        //String root = Environment.getExternalStorageDirectory().getPath();
        File baseFolder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "CamAI");
        if(!baseFolder.exists()){
            baseFolder.mkdirs();
        }

        //writing image files
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String currentDateandTime = sdf.format(new Date());

        if (imageSource.equals("camera")) {
            String sampleFileName = baseFolder.getPath() + "/sample_picture_" + currentDateandTime + ".jpg";
            ByteArrayOutputStream stream1 = new ByteArrayOutputStream();
            sampleImgBmp.compress(Bitmap.CompressFormat.JPEG, 100, stream1);
            byte[] data1 = stream1.toByteArray();
            try {
                FileOutputStream fos1 = new FileOutputStream(sampleFileName);
                fos1.write(data1);
                fos1.close();
                MediaScannerConnection.scanFile(context, new String[] { sampleFileName }, null, null);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        String camAiFileName = baseFolder.getPath() + "/camAI_picture_" + currentDateandTime + ".jpg";
        ByteArrayOutputStream stream2 = new ByteArrayOutputStream();
        aicamImgBmp.compress(Bitmap.CompressFormat.JPEG, 100, stream2);
        byte[] data2 = stream2.toByteArray();
        try {
            FileOutputStream fos2 = new FileOutputStream(camAiFileName);
            fos2.write(data2);
            fos2.close();
            MediaScannerConnection.scanFile(context, new String[] { camAiFileName }, null, null);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        if (imageSource.equals("camera")) {
            String msg = "Both Photos saved at " + baseFolder.getAbsolutePath();
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
        } else {
            String msg = "CamAI Photo saved at " + baseFolder.getAbsolutePath();
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
        }
    }

    public static File BitMapTempFile(Context context, Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] b = baos.toByteArray();
        File f = null;
        try {
            f = File.createTempFile("img_temp", ".jpg", context.getCacheDir());
            FileOutputStream fos = new FileOutputStream(f);
            fos.write(b);
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return f;
    }

    public static String getRealPathFromURI(Context context, Uri contentURI) {
        String result;
        Cursor cursor = context.getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null || !(cursor.moveToFirst()) || cursor.getCount() == 0)    // Source is Dropbox or other similar local file path
            result = contentURI.getPath();
         else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.Media.DATA);//Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }

    public static boolean validateSelfieManipulationParameters (Context context, Dialog dialog, SharedPref sharedPref) {
        EditText sm_edt1 = (EditText) dialog.findViewById(R.id.edt_sm1);
        EditText sm_edt2 = (EditText) dialog.findViewById(R.id.edt_sm2);
        EditText sm_edt3 = (EditText) dialog.findViewById(R.id.edt_sm3);
        if (sm_edt1.getText().toString().isEmpty()) {
            sm_edt1.requestFocus();
            Toast.makeText(context, "Field can not be empty", Toast.LENGTH_SHORT).show();
            return false;
        } else if ( Integer.valueOf(sm_edt1.getText().toString()) < -50 || Integer.valueOf(sm_edt1.getText().toString()) > 50 ) {
            sm_edt1.requestFocus();
            Toast.makeText(context, "Value must be between -50 to 50", Toast.LENGTH_SHORT).show();
            return false;
        } else if (sm_edt2.getText().toString().isEmpty()) {
            sm_edt2.requestFocus();
            Toast.makeText(context, "Field can not be empty", Toast.LENGTH_SHORT).show();
            return false;
        } else if ( Integer.valueOf(sm_edt2.getText().toString()) < -50 || Integer.valueOf(sm_edt2.getText().toString()) > 50 ) {
            sm_edt2.requestFocus();
            Toast.makeText(context, "Value must be between -50 to 50", Toast.LENGTH_SHORT).show();
            return false;
        } else if (sm_edt3.getText().toString().isEmpty()) {
            sm_edt3.requestFocus();
            Toast.makeText(context, "Field can not be empty", Toast.LENGTH_SHORT).show();
            return false;
        } else if ( Integer.valueOf(sm_edt3.getText().toString()) < 200 || Integer.valueOf(sm_edt3.getText().toString()) > 500 ) {
            sm_edt3.requestFocus();
            Toast.makeText(context, "Value must be between 200 to 500", Toast.LENGTH_SHORT).show();
            return false;
        } else if ( Integer.valueOf(sm_edt1.getText().toString()) == 0 & Integer.valueOf(sm_edt2.getText().toString()) == 0 & Integer.valueOf(sm_edt3.getText().toString()) == 0 ) {
            sm_edt1.requestFocus();
            Toast.makeText(context, "All fields can not be zero", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            int x = sharedPref.getIntPref("x");
            int y = sharedPref.getIntPref("y");
            int z = sharedPref.getIntPref("z");
            if (Integer.valueOf(sm_edt1.getText().toString()) == x & Integer.valueOf(sm_edt3.getText().toString()) == y & Integer.valueOf(sm_edt3.getText().toString()) == z) {
                sm_edt1.requestFocus();
                Toast.makeText(context, "None of the parameters have changed", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return true;
    }

    public static ArrayList<String> getServerURLs(Context context) {
        ArrayList<String> urls = new ArrayList<>();
        try {
            File urlsFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getPath() + "/CamAI/urls.txt");
            if (urlsFile.canRead()) {
                Scanner myReader = new Scanner(urlsFile);
                int count = 0;
                while (myReader.hasNextLine() & count < 2) {
                    String data = myReader.nextLine();
                    urls.add(data);
                    count ++;
                }
                myReader.close();
            }
        } catch (Exception e) {
            Toast.makeText(context, "Error reading urls file", Toast.LENGTH_LONG).show();
        }
        return urls;
    }
}
