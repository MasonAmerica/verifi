package com.example.verifi;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Log;

import androidx.annotation.NonNull;

//This class provides background thread to execute each test and is a singleton class.
//MainService calls the Start and Stop function of each test to send message to this thread's Handler.

public class TestScheduler extends HandlerThread {
    private static final String TAG = "verifi.TestScheduler";
    private CustomHandler mHandler;

    final static int START_GPS = 1;
    final static int STOP_GPS = 2;
    final static int START_SENSOR = 3;
    final static int STOP_SENSOR = 4;
    final static int START_DATA_CONN = 5;
    final static int STOP_DATA_CONN = 6;
    //Add new test constants here


    private final GpsTest gpsTest;
    private final DataConnTest dataConnTest;
    private final SensorTest sensorTest;
    //Add new test object here



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

    //Add new test start and stop function here


    //Constructor
    public TestScheduler(String name, MainService service) {
        super(name, Process.THREAD_PRIORITY_MORE_FAVORABLE);

        gpsTest = new GpsTest(service);
        dataConnTest = new DataConnTest(service.getApplicationContext());
        sensorTest = new SensorTest(service.getApplicationContext());
        //Add new test object instantiation here

    }

    //Thread Message Handler
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
                    sensorTest.startSensorTest();
                    break;

                case STOP_SENSOR:
                    sensorTest.stopSensorTest();
                    break;

                case START_DATA_CONN:
                    dataConnTest.startDataConnTest();
                    break;

                case STOP_DATA_CONN:
                    dataConnTest.stopDataConnTest();
                    break;

                //Add new test case statement here

                default:
                    break;
            }
        }
    }


    @Override
    public boolean quitSafely() {
        return super.quitSafely();
    }


    // Get a reference to worker thread's handler after looper is prepared
    @Override
    protected void onLooperPrepared() {
        super.onLooperPrepared();
        mHandler = new CustomHandler(getLooper());
    }

    // Used by UI thread to send a message to the worker thread's message queue
    private void addMessage(int message){
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

    public void postDelayedRunnable(Runnable runnable, long delayMs){
        if(mHandler != null) {
            mHandler.postDelayed(runnable, delayMs);
        }
    }
}
