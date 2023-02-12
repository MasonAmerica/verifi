package com.mason.verifi;

import static android.os.Build.VERSION.SDK_INT;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import androidx.core.content.ContextCompat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class FileLogger{
    private static final String TAG = "verifi.FileLogger";
    private static FileLogger single_instance = null;
    static String state = Environment.getExternalStorageState();

    private BufferedWriter fileBuf;
    private boolean loggerOn = false;

    public static FileLogger getInstance(Context context) {
        if (single_instance == null)
            single_instance = new FileLogger(context);

        return single_instance;
    }

    private FileLogger(Context context) {
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            File sdcard = Environment.getExternalStorageDirectory();
            String filename = "verifi_status.log";
            File file = new File(sdcard, filename);

            if(!file.exists()) {
                try  {
                    if (file.createNewFile()) {
                        Log.d(TAG, "Log file created");
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Failed to create log file!");
                    e.printStackTrace();
                }
            }

            if (file.exists()) {
                try {
                    //BufferedWriter for performance, true to set append to file flag
                    fileBuf = new BufferedWriter(new FileWriter(file, true));
                    loggerOn = true;
                    Log.d(TAG, "Log file buffer is created");
                } catch (IOException e) {
                    Log.e(TAG, "Failed to create log file buffer!");
                    e.printStackTrace();
                }
            }
        }
    }

    public void log(String status) {
        if (loggerOn) {
            //Log.d(TAG, status);
            try {
                fileBuf.write(status);
                //buf.append(message);
                fileBuf.newLine();
                fileBuf.flush();
            } catch (IOException e) {
                Log.e(TAG, "Failed to write to log file!");
                e.printStackTrace();
            }
        }
    }

    public void close() {
        if (loggerOn) {
            try {
                fileBuf.close();
            } catch (IOException e) {
                Log.e(TAG, "Failed to close log file!");
                e.printStackTrace();
            }
        }
    }
}
