package com.mason.verifi;
/*
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

          http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 */


import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.PowerManager;
import android.util.Log;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
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

//This class starts data connection test either via Cellular or Wifi
public class DataConnTest {
    private static final String TAG = "verifi.DataConnTest";

    //This address must be updated every 30 days since it's updated by webhook
    private static final String URL_TEST_ADDRESS = "https://webhook.site/ce16c084-c276-4ab8-9843-a18dd79b0a31";

    private static final String DC_TEST_FILENAME = "verifi_dc_test.png";

    private final Context mContext;
    private boolean isConnected = false;
    private final DataConnType dataConnType;


    public DataConnTest(Context context) {
        TestPreference testPref = TestPreference.getInstance();
        mContext = context;
        dataConnType = testPref.getDataConnType();

        File sdcard = Environment.getExternalStorageDirectory();
        File file = new File(sdcard, DC_TEST_FILENAME);

        //check if test file is in /sdcard
        //if not then copy it from APK's /assets to /sdcard
        if (!file.exists()) {
            AssetManager assetMgr = null;
            AssetFileDescriptor assetFd;

            try {
                assetMgr = mContext.getAssets();
                assetFd = assetMgr.openFd(DC_TEST_FILENAME);

                // Create new file to copy into.
                if (file.createNewFile() && assetFd != null) {
                    copyFileDescriptorToFile(assetFd.getFileDescriptor(), assetFd.getStartOffset(), assetFd.getLength(), file);
                    sendStatus("Copied Data Connection test file to sdcard folder");
                }
            } catch (IOException e) {
                e.printStackTrace();
                sendStatus("Got IOException. Failed to copy from assets to sdcard folder");
            }
        }

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

    private void copyFileDescriptorToFile(FileDescriptor src, long start, long length, File dst) throws IOException {
        FileChannel srcChannel = new FileInputStream(src).getChannel();
        FileChannel dstChannel = new FileOutputStream(dst).getChannel();
        try {
            srcChannel.transferTo(start, length, dstChannel);
        } finally {
            if (srcChannel != null)
                srcChannel.close();
            if (dstChannel != null)
                dstChannel.close();
        }
    }

    //This function establish HTTP connection and send request to upload a file
    private void UploadFileToServer() {
        PowerManager.WakeLock wakeLock;

        PowerManager powerManager = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"Verifi::MyTag");

        //stay awake for at most 5 min
        wakeLock.acquire(5*60*1000L /*5 minutes*/);

        File sdcard = Environment.getExternalStorageDirectory();
        File file = new File(sdcard, DC_TEST_FILENAME);

        //check if test file is in /sdcard
        //if not then copy it from APK's /assets to /sdcard
        if (file.exists()) {
            //build post request using OkHttpClient
            OkHttpClient client = new OkHttpClient();

            RequestBody requestBody = new MultipartBody.Builder()
                    .addFormDataPart("new", "This is a test image")
                    .addFormDataPart("image", DC_TEST_FILENAME,
                            RequestBody.create(file, MediaType.parse("image/png")))
                    .setType(MultipartBody.FORM)
                    .build();

            Request postRequest = new Request.Builder()
                    .url(URL_TEST_ADDRESS)
                    .post(requestBody)
                    .build();

            //send Post request
            try {
                Log.d(TAG, "Post HTTPS Request");
                Response response = client.newCall(postRequest).execute();

                Log.d(TAG, "HTTPS Response: " + Objects.requireNonNull(response.body()));

                Date df = new Date();
                String ts = new SimpleDateFormat("MM-dd HH:mm:ss", Locale.US).format(df);

                if (response.isSuccessful()) {
                    sendStatus(ts + " - HTTPS file upload test pass");
                } else {
                    sendStatus(ts + " - HTTPS file upload test fail");
                }

                response.close();
            } catch (IOException e) {
                e.printStackTrace();
                sendStatus("Got IOException. Cannot find test file in sdcard folder");
            } catch (IllegalStateException e) {
                e.printStackTrace();
                sendStatus("Got IllegalStateException");
            }
        } else {
            sendStatus("File upload failed. Cannot find test file in sdcard folder");
        }


        wakeLock.release();
    }

    // Check if cellular connection is enabled and active
    boolean hasCellularConnection() {
        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo mobileNetwork = cm.getNetworkInfo(cm.getActiveNetwork());

        Date df = new Date();
        String ts = new SimpleDateFormat("MM-dd HH:mm:ss", Locale.US).format(df.getTime());

        if (mobileNetwork != null && mobileNetwork.isConnected() &&
                mobileNetwork.getTypeName().equalsIgnoreCase("MOBILE")) {

            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            if (activeNetwork != null && activeNetwork.isConnected()) {
                sendStatus(ts + " - Connected to Cellular Network");
                return true;
            }
        }

        sendStatus(ts + " - NOT connected to Cellular Network");
        return false;
    }

    // Check if Wifi connection is enabled
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

        sendStatus(ts + " - NOT connected to Wifi Network");
        return false;
    }

    private synchronized void sendStatus(String message){
        Intent intent = new Intent();
        intent.setAction(MainService.SENDSTATUS);
        intent.putExtra("status",message);
        mContext.sendBroadcast(intent);
    }
}
