package com.example.verifi;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class MainService extends Service {
    final static String SENDSTATUS = "SENDSTATUS";

    private static final String TAG = "verifi.MainService";

    private TestScheduler testScheduler;
    private TestPreference testPref;


    @Override
    public void onCreate() {
        super.onCreate();

        //Create Test Scheduler object
        testScheduler = new TestScheduler("TestScheduler", this);
        testScheduler.start();

        testPref = TestPreference.getInstance();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        try {
            Thread.sleep(200);
        }
        catch (InterruptedException e){
            Log.e(TAG,"HandlerThread interrupted");
        }

        //Start Test based on TestPreference settings
        if (testPref.isEnableGPS()) {
            testScheduler.addMessage(TestScheduler.START_GPS);
        }

        if (testPref.isEnableSensor()) {
            testScheduler.addMessage(TestScheduler.START_SENSOR);
        }

        if (testPref.isEnableDataConn()) {
            //start Data connection
        }

        Log.d(TAG, "MainService started...");

        //If service is killed while starting, it restarts.
        return START_STICKY;
    }

    @Override
    public void onDestroy() {

        //Stop Test based on TestPreference settings
        if (testPref.isEnableGPS()) {
            testScheduler.addMessage(TestScheduler.STOP_GPS);
        }

        if (testPref.isEnableSensor()) {
            testScheduler.addMessage(TestScheduler.STOP_SENSOR);
        }

        if(testScheduler != null){
            testScheduler.quitSafely();
            testScheduler.interrupt();
        }
        //stop all tests using TestScheduler object
        Log.d(TAG, "MainService stopped...");

        super.onDestroy();
    }

    public void sendStatus(String message){
        Intent intent = new Intent();
        intent.setAction(SENDSTATUS);
        intent.putExtra("status",message);
        sendBroadcast(intent);
    }
}