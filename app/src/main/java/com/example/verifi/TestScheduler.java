package com.example.verifi;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Log;

import androidx.annotation.NonNull;

public class TestScheduler extends HandlerThread {
    final static int START_GPS = 1;
    final static int STOP_GPS = 2;
    final static int START_SENSOR = 3;
    final static int STOP_SENSOR = 4;
    final static int START_DATA_CONN = 5;
    final static int STOP_DATA_CONN = 6;

    private static final String TAG = "verifi.TestScheduler";

    private static TestScheduler single_instance = null;

    private final GpsTest gpsTest;
    private final DataConnTest dataConnTest;

    public CustomHandler mHandler;
    MainService parentService;


    private TestScheduler(String name, MainService service) {
        super(name, Process.THREAD_PRIORITY_MORE_FAVORABLE);
        parentService = service;

        gpsTest = new GpsTest(parentService);
        dataConnTest = new DataConnTest(parentService.getApplicationContext());

    }

    public static TestScheduler getInstance(MainService service) {
        if (single_instance == null) {
            single_instance = new TestScheduler("TestScheduler", service);
            single_instance.start();
        }

        return single_instance;
    }

    //IMPORTANT: Only call this function after calling the above function first
    public static TestScheduler getInstance() {
        return single_instance;
    }

    // Get a reference to worker thread's handler after looper is prepared
    @Override
    protected void onLooperPrepared() {
        super.onLooperPrepared();
        mHandler = new CustomHandler(getLooper());
    }

    @Override
    public boolean quitSafely() {
        //TODO: may need to stop each test here

        return super.quitSafely();
    }

    public void startGPSTest() {
        addMessage(TestScheduler.START_GPS);
    }

    public void stopGPSTest() {
        addMessage(TestScheduler.STOP_GPS);
    }

    public void startSensorTest() {
        addMessage(TestScheduler.START_SENSOR);
    }

    public void stopSensorTest() {
        addMessage(TestScheduler.STOP_SENSOR);
    }

    public void startDataConnTest() {
        addMessage(TestScheduler.START_DATA_CONN);
    }

    public void stopDataConnTest() {
        addMessage(TestScheduler.STOP_DATA_CONN);
    }

    // Used by UI thread to send a message to the worker thread's message queue
    private void addMessage(int message){
        Log.d(TAG, "addMessage: " + message);
        if(mHandler != null) {
            mHandler.sendEmptyMessage(message);
        }
    }

    //TODO:enable this when needed
    // Used by UI thread to send a runnable to the worker thread's message queue
    //public void postRunnable(Runnable runnable){
    //    if(mHandler != null) {
    //        mHandler.post(runnable);
    //    }
    //}

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
                    //TODO: start sensor test
                    break;

                case STOP_SENSOR:
                    //TODO: stop sensor test
                    break;

                case START_DATA_CONN:
                    dataConnTest.startDataConnTest();
                    break;

                case STOP_DATA_CONN:
                    dataConnTest.stopDataConnTest();
                    break;

                default:
                    break;
            }
        }
    }

}
