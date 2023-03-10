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


import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

// This class will instantiate TestScheduler and start/stop each test feature (GPS, Sensor, Data connection)
// It also provides sendStatus function to send status messages to Status Fragment
// It is started in ConfigureFragment and stopped in StatusFragment
public class MainService extends Service {
    private static final String TAG = "verifi.MainService";
    public static TestScheduler testScheduler;
    private TestPreference testPref;
    public static final String SENDSTATUS = "SENDSTATUS";

    private DataConnAlarm dataConnAlarm;
    private SensorAlarm sensorAlarm;

    //add new test alarm class object here

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

    //GPS test is started here
    //Sensor and Data Connection test are started by its corresponding Alarm class
    //that manages the test time interval
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

        //add new test start or alarm creation here

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

        //add new test termination here

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