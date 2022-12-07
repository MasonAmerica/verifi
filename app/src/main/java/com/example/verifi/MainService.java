package com.example.verifi;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainService extends Service {
    private static final String TAG = "verifi.MainService";
    public static TestScheduler testScheduler;
    private TestPreference testPref;
    public static final String SENDSTATUS = "SENDSTATUS";

    private DataConnAlarm dataConnAlarm;
    private SensorAlarm sensorAlarm;

    @Override
    public void onCreate() {
        super.onCreate();

        //Create Test Scheduler object
        testScheduler = new TestScheduler("VerifiTestScheduler", this);
        testScheduler.start();

        //Get TestPreference instance
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
            //sleep to allow TestScheduler thread to be created first
            Thread.sleep(200);
        }
        catch (InterruptedException e){
            Log.e(TAG,"HandlerThread interrupted");
        }
        Date df = new Date();
        String ts = new SimpleDateFormat("MM-dd HH:mm:ss", Locale.US).format(df);
        sendStatus(ts + " - Start Test");

        //Start Test based on TestPreference settings
        if (testPref.isEnableGPS()) {
            testScheduler.startGPSTest();
            sendStatus("Type: " + testPref.getGpsType() + " Interval: " + testPref.getGpsInterval() + " sec");
        }

        if (testPref.isEnableSensor()) {
            //Create Sensor Alarm. Its alarm receiver will start Sensor Test
            sensorAlarm = new SensorAlarm(getApplicationContext());
            sendStatus("Type: " + testPref.getSensorType() + " Interval: " + testPref.getSensorInterval() + " sec");
        }

        if (testPref.isEnableDataConn()) {
            //Create Data connection Alarm. Its alarm receiver will start Data Conn Test
            dataConnAlarm = new DataConnAlarm(getApplicationContext());
            sendStatus("Type: " + testPref.getDataConnType() + " Interval: " + testPref.getDataConnInterval() + " sec");
        }

        Log.d(TAG, "MainService started...");

        //If service is killed while starting, it restarts.
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //Stop Test based on TestPreference settings
        if (testPref.isEnableGPS()) {
            testScheduler.stopGPSTest();
        }

        if (testPref.isEnableSensor()) {
            testScheduler.stopSensorTest();
            sensorAlarm.stopSensorAlarm();
        }

        if (testPref.isEnableDataConn()) {
            testScheduler.stopDataConnTest();
            dataConnAlarm.stopDataConnAlarm();
        }

        if(testScheduler != null){
            testScheduler.quitSafely();
            //testScheduler.interrupt();
        }

        Log.d(TAG, "MainService stopped...");
    }

    //make this thread-safe using synchronized
    public synchronized void sendStatus(String message){
        Intent intent = new Intent();
        intent.setAction(SENDSTATUS);
        intent.putExtra("status",message);
        sendBroadcast(intent);
    }

    public static TestScheduler getTestScheduler() {
        return testScheduler;
    }

}