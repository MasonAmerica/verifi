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


import static android.os.Build.VERSION.SDK_INT;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.mason.verifi.databinding.ActivityMainBinding;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "verifi.MainActivity";

    final Fragment configureFragment = new ConfigureFragment();
    final Fragment statusFragment = new StatusFragment();
    final FragmentManager fm = getSupportFragmentManager();
    Fragment active = configureFragment;

    private MyReceiver myReceiver = null;

    private ActivityMainBinding binding;
    private StatusUpdater updater;

    private boolean isTestStarted;

    private FileLogger fileLogger;


    public void setTestStarted(boolean testStarted) {
        isTestStarted = testStarted;
    }

    public boolean isTestStarted() {
        return isTestStarted;
    }

    //Display Configure Fragment
    public void showConfigureFragment() {
        fm.beginTransaction().hide(active).show(configureFragment).commit();
        active = configureFragment;
    }

    //Display Status Fragment
    public void showStatusFragment() {
        fm.beginTransaction().hide(active).show(statusFragment).commit();
        active = statusFragment;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        fm.beginTransaction().add(R.id.main_container, configureFragment, "fr1").show(configureFragment).commit();
        fm.beginTransaction().add(R.id.main_container, statusFragment, "fr2").hide(statusFragment).commit();
        updater = (StatusUpdater) statusFragment;

        check_runtime_permissions();

        registerReceiver();
        setTestStarted(false);
    }

    //Check external storage access permission
    private void check_runtime_permissions() {
        if (SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                try {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                    intent.addCategory("android.intent.category.DEFAULT");
                    intent.setData(Uri.parse(String.format("package:%s", getApplicationContext().getPackageName())));
                    startActivityForResult(intent, 2319);
                } catch (Exception e) {
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                    startActivityForResult(intent, 2319);
                }
            } else {
                fileLogger = FileLogger.getInstance(this);
            }

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.BODY_SENSORS) != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.BODY_SENSORS}, 2320);

            }

        } else {
            if ( ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.BODY_SENSORS) != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.BODY_SENSORS}, 2320);
            } else {
                fileLogger = FileLogger.getInstance(this);
            }
        }
    }

    //handler for permission response
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (SDK_INT >= Build.VERSION_CODES.R) {
            if (requestCode == 2320) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[1] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[2] == PackageManager.PERMISSION_GRANTED) {
                    //Done, do nothing
                } else {
                    //exit
                    Toast.makeText(this, "Exiting! Location or Sensors permissions are not granted", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        } else {
            if (requestCode == 2320) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[1] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[2] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[3] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[4] == PackageManager.PERMISSION_GRANTED) {
                    fileLogger = FileLogger.getInstance(this);
                } else {
                    //exit
                    Toast.makeText(this, "Exiting! Storage, Location, or Sensors permissions are not granted", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }
    }

    //handle external storage permission response
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2319) {
            if (SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    fileLogger = FileLogger.getInstance(this);
                } else {
                    //exit
                    Toast.makeText(this, "Exiting! Storage permission is not granted", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }
    }

    //Register status message receiver
    private void registerReceiver(){
        //Register BroadcastReceiver
        //to receive event from our service
        myReceiver = new MyReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MainService.SENDSTATUS);

        //register with the Application context, to receive broadcasts as long as the app is running
        getApplicationContext().registerReceiver(myReceiver, intentFilter);

        Log.d(TAG, "Broadcast Receiver registered...");
    }

    //Receiver to handle status messages to be displayed on the Status Fragment
    public class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context arg0, Intent arg1) {
            Log.d(TAG, "Receive status: " + arg1.getStringExtra("status"));
            if (arg1.getAction().equals(MainService.SENDSTATUS) && arg1.hasExtra("status")) {
                String status = arg1.getStringExtra("status");
                updater.updateStatus(status);
                fileLogger.log(status);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isTestStarted()) {
            //send intent to TestService to stop test
            stopService(new Intent(this, MainService.class));
            fileLogger.close();
        }

        if (myReceiver != null)
            getApplicationContext().unregisterReceiver(myReceiver); //unregister my receiver...
    }
}