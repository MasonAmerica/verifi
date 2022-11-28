package com.example.verifi;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.PowerManager;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DataConnTest {
    private static final String TAG = "verifi.DataConnTest";

    private final Context mContext;
    private boolean isConnected = false;
    private final DataConnType dataConnType;


    public DataConnTest(Context context) {
        TestPreference testPref = TestPreference.getInstance();
        mContext = context;
        dataConnType = testPref.getDataConnType();
    }

    public void startDataConnTest() {
        isConnected = false;

        //check if Wifi or Cellular Connectivity is enabled
        if (dataConnType == DataConnType.CELL) {
            if (hasCellularConnection())
                isConnected = true;
        } else {
            if (hasWifiConnection())
                isConnected = true;
        }

        if (isConnected) {
            UploadFileToServer();
        }
    }

    public void stopDataConnTest() {
        if (isConnected) {
            isConnected = false;
        }
    }

    private void UploadFileToServer() {
        PowerManager.WakeLock wakeLock;

        Log.d(TAG, "send HTTPS Request");

        PowerManager powerManager = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"Verifi::MyTag");
        wakeLock.acquire(5*60*1000L /*5 minutes*/);


        OkHttpClient client = new OkHttpClient();

        File sdcard = Environment.getExternalStorageDirectory();
        File file = new File(sdcard,"dog_300kb.png");

        RequestBody requestBody = new MultipartBody.Builder()
                .addFormDataPart("new", "This is a test image")
                .addFormDataPart("image", "dog_300kb.png",
                                  RequestBody.create(file, MediaType.parse("image/png")))
                .setType(MultipartBody.FORM)
                .build();

        Request postRequest = new Request.Builder()
                .url("https://webhook.site/470b12f5-3954-42c3-b986-1c0957f438c7")
                .post(requestBody)
                .build();

        try {
            Response response = client.newCall(postRequest).execute();

            Log.d(TAG, "HTTPS Response: " + Objects.requireNonNull(response.body()));

            Date df = new Date();
            String ts = new SimpleDateFormat("MM-dd HH:mm:ss", Locale.US).format(df);

            if (response.isSuccessful()) {
                sendStatus(ts + " - HTTPS file upload pass");
            } else {
                sendStatus(ts + " - HTTPS file upload fail");
            }

            response.close();
        } catch (IOException e) {
            e.printStackTrace();
            sendStatus("Got IOException");
        } catch (IllegalStateException e) {
            e.printStackTrace();
            sendStatus("Got IllegalStateException");
        }

        wakeLock.release();
    }

    boolean hasCellularConnection() {
        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo mobileNetwork = cm.getNetworkInfo(cm.getActiveNetwork());

        Date df = new Date();
        String ts = new SimpleDateFormat("MM-dd HH:mm:ss", Locale.US).format(df.getTime());

        if (mobileNetwork != null && mobileNetwork.isConnected() &&
                mobileNetwork.getTypeName().equalsIgnoreCase("MOBILE")) {

            //Log.d(TAG, "Connected to Cellular Network");
            sendStatus(ts + " - Connected to Cellular Network");

            return true;
        }

        //TODO: check if this is necessary
        //NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        //if (activeNetwork != null && activeNetwork.isConnected()) {
        //    return true;
        //}

        //Log.d(TAG, "NOT Connected to Cellular Network");
        sendStatus(ts + " - NOT connected to Cellular Network");
        return false;
    }

    boolean hasWifiConnection() {
        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo wifiNetwork = cm.getNetworkInfo(cm.getActiveNetwork());

        Date df = new Date();
        String ts = new SimpleDateFormat("MM-dd HH:mm:ss", Locale.US).format(df.getTime());

        if (wifiNetwork != null && wifiNetwork.isConnected() &&
                wifiNetwork.getTypeName().equalsIgnoreCase("WIFI")) {
            //Log.d(TAG, "Connected to Wifi Network");
            sendStatus(ts + " - Connected to Wifi Network");
            return true;
        }
        //Log.d(TAG, "NOT Connected to Wifi Network");
        sendStatus(ts + " - NOT connected to Wifi Network");
        return false;
    }

    public synchronized void sendStatus(String message){
        Intent intent = new Intent();
        intent.setAction(MainService.SENDSTATUS);
        intent.putExtra("status",message);
        mContext.sendBroadcast(intent);
    }
}
