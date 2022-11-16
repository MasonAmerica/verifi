package com.example.verifi;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

public class TestScheduler extends HandlerThread {
    final static int START_GPS = 1;
    final static int STOP_GPS = 2;
    final static int START_SENSOR = 3;
    final static int STOP_SENSOR = 4;


    private static final String TAG = "verifi.TestScheduler";

    private final GpsTest gpsTest;

    CustomHandler mHandler;
    MainService parentService;


    public TestScheduler(String name, MainService service) {
        super(name, android.os.Process.THREAD_PRIORITY_BACKGROUND);
        parentService = service;
        gpsTest = new GpsTest(parentService);
    }

    // Get a reference to worker thread's handler after looper is prepared
    @Override
    protected void onLooperPrepared() {
        super.onLooperPrepared();
        mHandler = new CustomHandler(getLooper());
    }

    @Override
    public boolean quitSafely() {
        //gpsTest.stopGpsTest();

        return super.quitSafely();
    }

    // Used by UI thread to send a message to the worker thread's message queue
    public void addMessage(int message){
        Log.d(TAG, "addMessage: " + message);
        if(mHandler != null) {
            mHandler.sendEmptyMessage(message);
        }
    }

    // Used by UI thread to send a runnable to the worker thread's message queue
    public void postRunnable(Runnable runnable){
        if(mHandler != null) {
            mHandler.post(runnable);
        }
    }

    private class CustomHandler extends Handler {
        public CustomHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case START_GPS:
                        gpsTest.startGpsTest();
                    break;

                case STOP_GPS:
                        gpsTest.stopGpsTest();
                    break;

                case START_SENSOR:
                        gpsTest.startSensorTest();
                    break;

                case STOP_SENSOR:
                        gpsTest.stopSensorTest();
                    break;

                default:
                    break;
            }
        }
    }

}
