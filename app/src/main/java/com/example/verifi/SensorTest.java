package com.example.verifi;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SensorTest {
    private static final String TAG = "verifi.SensorTest";

    private final Context mContext;
    private boolean isHRStarted = false;
    private boolean isOffBodyStarted = false;
    private boolean isOffBodyEnhancedStarted = false;

    private final SensorType sensorType;

    private final SensorManager sensorManager;
    private final Sensor heartRateSensor;
    private final Sensor offBodySensor;
    private Sensor offBodyEnhancedSensor;

    private final SensorEventListener offBodySensorTestListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            Date df = new Date();
            String ts = new SimpleDateFormat("MM-dd HH:mm:ss", Locale.US).format(df);

            if (sensorEvent.sensor.getType() == Sensor.TYPE_LOW_LATENCY_OFFBODY_DETECT) {
                    sendStatus(ts + " - OBD value: " + sensorEvent.values[0] + " Acc: " + sensorEvent.accuracy);
                    Log.d(TAG, "OBD Sensor - Value: " + sensorEvent.values[0] + " Accuracy: " + sensorEvent.accuracy);
                    stopOffBodySensorTest();
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    private final SensorEventListener heartRateSensorTestListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            Date df = new Date();
            String ts = new SimpleDateFormat("MM-dd HH:mm:ss", Locale.US).format(df);

            if (sensorEvent.sensor.getType() == Sensor.TYPE_HEART_RATE) {
                    sendStatus(ts + " - Heart Rate value: " + sensorEvent.values[0] + " Acc: " + sensorEvent.accuracy);
                    Log.d(TAG, "HR Sensor - Value: " + sensorEvent.values[0] + " Accuracy: " + sensorEvent.accuracy);

                    //stop HR sensor test in 30 seconds
                    MainService.getTestScheduler().postDelayedRunnable(() -> stopHRSensorTest(), 30000);
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            Log.d(TAG, "Phillips PPG HR accuracy changed to: " + accuracy);
        }
    };

    public SensorTest(Context context) {
        TestPreference testPref = TestPreference.getInstance();
        mContext = context;
        sensorType = testPref.getSensorType();

        sensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
        heartRateSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
        offBodySensor = sensorManager.getDefaultSensor(Sensor.TYPE_LOW_LATENCY_OFFBODY_DETECT);
    }

    public void startSensorTest() {
        switch(sensorType) {
            case OFFBODY:
                startOffBodySensorTest();
                break;

            case OFFBODYENHANCED:
                startOffBodyEnhancedSensorTest();
                break;

            case HEARTRATE:
                startHRSensorTest();
                break;

            case OFFBODYANDHEARTRATE:
                startOffBodySensorTest();
                startHRSensorTest();
                break;
        }
    }

    public void stopSensorTest() {
        switch(sensorType) {
            case OFFBODY:
                stopOffBodySensorTest();
                break;

            case OFFBODYENHANCED:
                stopOffBodyEnhancedSensorTest();
                break;

            case HEARTRATE:
                stopHRSensorTest();
                break;

            case OFFBODYANDHEARTRATE:
                stopOffBodySensorTest();
                stopHRSensorTest();
                break;
        }
    }

    private void startHRSensorTest() {
        if (!isHRStarted && heartRateSensor != null) {
            sensorManager.registerListener(heartRateSensorTestListener, heartRateSensor, SensorManager.SENSOR_DELAY_NORMAL);
            Log.d(TAG, "Start Heart Rate Sensor");
            isHRStarted = true;
        }
    }

    private void stopHRSensorTest() {
        if (isHRStarted) {
            sensorManager.unregisterListener(heartRateSensorTestListener);
            Log.d(TAG, "Stop Heart Rate Sensor");
            isHRStarted = false;
        }
    }

    private void startOffBodySensorTest() {
        if (!isOffBodyStarted && offBodySensor != null) {
            sensorManager.registerListener(offBodySensorTestListener, offBodySensor, SensorManager.SENSOR_DELAY_NORMAL);
            isOffBodyStarted = true;
        }
    }

    private void stopOffBodySensorTest() {
        if (isOffBodyStarted) {
            sensorManager.unregisterListener(offBodySensorTestListener);
            isOffBodyStarted = false;
        }
    }

    private void startOffBodyEnhancedSensorTest() {
        if (!isOffBodyEnhancedStarted) {
            isOffBodyEnhancedStarted = true;
        }
    }

    private void stopOffBodyEnhancedSensorTest() {
        if (isOffBodyEnhancedStarted) {
            isOffBodyEnhancedStarted = false;
        }
    }

    private synchronized void sendStatus(String message){
        Intent intent = new Intent();
        intent.setAction(MainService.SENDSTATUS);
        intent.putExtra("status",message);
        mContext.sendBroadcast(intent);
    }
}