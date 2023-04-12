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
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import mason.hardware.platform.ECGSensorManager;
import mason.hardware.platform.MasonHardwareFramework;
import mason.hardware.platform.ecg.ECGEnergy;
import mason.hardware.platform.ecg.ECGEventListener;
import mason.hardware.platform.ecg.ECGExecuteResponse;
import mason.hardware.platform.ecg.ECGFwVersion;
import mason.hardware.platform.ecg.ECGHeartRate;
import mason.hardware.platform.ecg.ECGHeartRateVariability;
import mason.hardware.platform.ecg.ECGLibConfig;
import mason.hardware.platform.ecg.ECGSamples;
import mason.hardware.platform.ecg.ECGStress;
import mason.hardware.platform.ecg.ECGUserID;
import mason.hardware.platform.ecg.ECGUserMetadata;
import mason.hardware.platform.ecg.ECGUserPresence;

// This class starts sensor (HR, Offbody, or ECG) test
public class SensorTest {
    private static final String TAG = "verifi.SensorTest";
    private static final String MODEL_A4100A1 = "Mason A4100A1";
    private static final int HEART_RATE_SAMPLE_COUNT = 20;
    private static final int ECG_DURATION_SEC = 20;

    private final Context mContext;
    private SensorAlarm mSensorAlarm;
    private boolean isHRStarted = false;
    private boolean isOffBodyStarted = false;
    private boolean isOffBodyEnhancedStarted = false;
    private boolean isEcgStarted = false;
    private boolean isSpo2Started = false;
    private boolean isSleepMonitorEnabled = false;
    private int sleepMonitorState = 0;
    private float sleepHeartRate = 0;

    private final SensorType sensorType;

    private final SensorManager sensorManager;
    private final Sensor heartRateSensor;
    private final Sensor offBodySensor;
    //private final Sensor offBodyEnhancedSensor; //Not supported, yet
    private final Sensor spo2Sensor;

    private ECGSensorManager ecgManager;
    private Sensor ecgSensorData;
    //Enable this to get ECG raw sample data
    //private Sensor ecgSampleData;

    private static final int qSensorTypeBase = 33171000;
    //private static final int sensorTypeRespiratory = qSensorTypeBase + 5;
    private static final int sensorTypeSpo2 = qSensorTypeBase + 6;

    private int heartRateCounter = 0;
    private int ecgCounter = 0;
    private int spo2Counter = 0;

    public SensorTest(Context context) {
        TestPreference testPref = TestPreference.getInstance();
        mContext = context;
        sensorType = testPref.getSensorType();

        sensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
        heartRateSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
        offBodySensor = sensorManager.getDefaultSensor(Sensor.TYPE_LOW_LATENCY_OFFBODY_DETECT);
        spo2Sensor = sensorManager.getDefaultSensor(sensorTypeSpo2);

        if (getDeviceModelName().equals(MODEL_A4100A1)) {
            //ECG sensor to be registered with ecgSensorDataTestListener in startEcgSensorTest()
            ecgSensorData = sensorManager.getDefaultSensor(ECGSensorManager.SensorType.ECG_SENSORS_DATA);

            //Enable this to get ECG raw sample data
            //ecgSampleData = sensorManager.getDefaultSensor(ECGSensorManager.SensorType.ECG_SAMPLES_DATA);

            //ECG sensor manager which is dispatched from ecgSensorDataTestListener's onSensorChanged()
            ecgManager = MasonHardwareFramework.get(mContext, ECGSensorManager.class);

            //mHeartKeySensorEventListener is is actual listener for handling sensor event
            ecgManager.registerEventListener(mHeartKeySensorEventListener);
        }
    }

    private final SensorEventListener offBodySensorTestListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            Date df = new Date();
            String ts = new SimpleDateFormat("MM-dd HH:mm:ss", Locale.US).format(df);

            if (sensorEvent.sensor.getType() == Sensor.TYPE_LOW_LATENCY_OFFBODY_DETECT) {
                    //Log.d(TAG, "OBD Sensor - Value: " + sensorEvent.values[0] + " Accuracy: " + sensorEvent.accuracy);
                    sendStatus(ts + " - OBD value: " + sensorEvent.values[0] + " Acc: " + sensorEvent.accuracy);
                    stopOffBodySensorTest();
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    /*
    State machine for SLEEP_MON:
    State 0: Initial state
    State 1: if state is 0 and Heart rate accuracy is 3, capture HR value.
    State 2: if state is 1 and SPO2 value is not zero:
               capture SPO2 value,
               report both HR and SPO2 value,
               unregister HR amd SPO2 listener,
               and restart sensor alarm
     */

    private final SensorEventListener heartRateSensorTestListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {

            if (sensorEvent.sensor.getType() == Sensor.TYPE_HEART_RATE) {
                if (!isSleepMonitorEnabled) {
                    if (heartRateCounter > 0) {
                        heartRateCounter--;
                        //Log.d(TAG, "HR Sensor - Value: " + sensorEvent.values[0] + " Accuracy: " + sensorEvent.accuracy);

                        //only display the last 5 readings to the status fragment to prevent flooding the status screen
                        //if (heartRateCounter <= 4) {
                        Date df = new Date();
                        String ts = new SimpleDateFormat("MM-dd HH:mm:ss", Locale.US).format(df);
                        sendStatus(ts + " - Heart Rate: " + sensorEvent.values[0] + " Acc: " + sensorEvent.accuracy);

                        if (heartRateCounter == 0)
                            stopHRSensorTest();
                        //}
                    }
                } else {
                    if (sensorEvent.accuracy == 3 && sleepMonitorState == 0) {
                        sleepMonitorState = 1;
                        //save as sleep HR and report together with blood oxygen
                        sleepHeartRate = sensorEvent.values[0];
                    }
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            Log.d(TAG, "Phillips PPG HR accuracy changed to: " + accuracy);
        }
    };

    private final SensorEventListener spo2SensorTestListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {

            if (sensorEvent.sensor.getType() == sensorTypeSpo2) {
                if (!isSleepMonitorEnabled) {
                    if (spo2Counter > 0) {
                        spo2Counter--;
                        //Log.d(TAG, "SPO2 Sensor - Value: " + sensorEvent.values[1] + " Accuracy: " + sensorEvent.accuracy);

                        //only display the last 11 readings to the status fragment to prevent flooding the status screen
                        //if (spo2Counter <= 10) {
                        Date df = new Date();
                        String ts = new SimpleDateFormat("MM-dd HH:mm:ss", Locale.US).format(df);
                        sendStatus(ts + " - Blood Oxygen: " + sensorEvent.values[1]);

                        if (spo2Counter == 0)
                            stopSPO2SensorTest();
                        //}
                    }
                } else {
                    if (sensorEvent.values[1] != 0.0 && sleepMonitorState == 1) {
                        sleepMonitorState = 2;
                        Date df = new Date();
                        String ts = new SimpleDateFormat("HH:mm:ss", Locale.US).format(df);

                        //send both heart rate and blood oxygen level
                        sendStatus(ts + ", " + sleepHeartRate + ", " + sensorEvent.values[1]);

                        //stop HR and SPO2 test listener
                        stopSleepMonitorTest();

                        //restart sensor alarm
                        mSensorAlarm.startSensorAlarm(mContext);
                    }
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            Log.d(TAG, "Phillips PPG SPO2 accuracy changed to: " + accuracy);
        }
    };

    //Sensor event listener for ECG sensor data
    private final SensorEventListener ecgSensorDataTestListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (getDeviceModelName().equals(mContext.getString(R.string.A4100A1))) {
                ecgManager.dispatch(event);
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    /*
    //Sensor event listener for ECG sensor raw sample data
    private final SensorEventListener ecgSampleDataTestListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            ecgManager.dispatch(event);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };
    */

    //ECG sensor event handler which is invoked by ECG sensor manager
    private final ECGEventListener mHeartKeySensorEventListener = new ECGEventListener () {
        @Override
        public void HandleHeartRate(ECGHeartRate ecgHeartRate) {
            if (ecgCounter > 0) {
                ecgCounter--;

                Date df = new Date();
                String ts = new SimpleDateFormat("MM-dd HH:mm:ss", Locale.US).format(df);
                sendStatus(ts + " - ECG HR: " + ecgHeartRate.getBpm());

                //stop ECG reading after ECG_DURATION_SEC
                if (ecgCounter == 0)
                    stopEcgSensorTest();
            }
        }

        @Override
        public void HandleStress(ECGStress ecgStress) {
            //String score = Integer.toString(ecgStress.getScore());
            //String state = Integer.toString(ecgStress.getState());
            //Date df = new Date();
            //String ts = new SimpleDateFormat("MM-dd HH:mm:ss", Locale.US).format(df);
            //sendStatus(ts + " - Stress: " + score + " State: " + state);
        }

        @Override
        public void HandleEnergy(ECGEnergy ecgEnergy) {
            //String totalEnergy = Float.toString(ecgEnergy.getTotalEnergy());
            //String progress = Integer.toString(ecgEnergy.getProgress());
            //Date df = new Date();
            //String ts = new SimpleDateFormat("MM-dd HH:mm:ss", Locale.US).format(df);
            //sendStatus(ts + " - Total Energy: " + totalEnergy + " Progress: " + progress);
        }

        @Override
        public void HandleUserId(ECGUserID ecgUserID) {
            //not supported
        }

        @Override
        public void HandleHeartRateVariability(ECGHeartRateVariability ecgHeartRateVariability) {
            //String hrv = Integer.toString(ecgHeartRateVariability.getHeartRateVariability());
            //String progress = Integer.toString(ecgHeartRateVariability.getProgress());
            //Date df = new Date();
            //String ts = new SimpleDateFormat("MM-dd HH:mm:ss", Locale.US).format(df);
            //sendStatus(ts + " - HR Variability: " + hrv + " Progress " + progress);
        }

        @Override
        public void HandleUserPresence(ECGUserPresence ecgUserPresence) {
            //String presence = Integer.toString(ecgUserPresence.getUserPresence());
            //String alive = Integer.toString(ecgUserPresence.getUserAlive());
            //String sQuality = Integer.toString(ecgUserPresence.getSignalQuality());
            //Date df = new Date();
            //String ts = new SimpleDateFormat("MM-dd HH:mm:ss", Locale.US).format(df);
            //sendStatus(ts + " - Presence: " + presence  + " Alive: " + alive  +  " Sig Qual: " + sQuality);
        }

        @Override
        public void HandleECGSamples(ECGSamples ecgSamples) {
            //Enable this to get ECG raw sample data
            //Date df = new Date();
            //String ts = new SimpleDateFormat("MM-dd HH:mm:ss", Locale.US).format(df);
            //float[] samples = ecgSamples.getSamples();
            //int subType = ecgSamples.getSubtype();
            //String str = ts + " Raw Type: " + subType + " Samples: ";
            //for (int i = 0; i < samples.length; ++i) {
            //    str += samples[i] + " ";
            //}
            //sendStatus(str);
        }

        @Override
        public void HandleExecuteResponse(ECGExecuteResponse ecgExecuteResponse) {
            //not supported
        }

        @Override
        public void HandleGetVersion(ECGFwVersion ecgFwVersion) {
            sendStatus("ECG Version " + ecgFwVersion.getVersion());
        }

        @Override
        public void HandleGetUserMetadata(ECGUserMetadata ecgUserMetadata) {
            String age = Integer.toString(ecgUserMetadata.getAge());
            String h = Integer.toString(ecgUserMetadata.getHeight());
            String w = Integer.toString(ecgUserMetadata.getWeight());
            String g = Integer.toString(ecgUserMetadata.getGender());

            sendStatus("User Age: " + age + " Height: " + h + " Weight: " + w + " Gender: " + g);
        }

        @Override
        public void HandleGetLibConfig(ECGLibConfig ecgLibConfig) {
            int useCase = ecgLibConfig.getUseCase();
            int authMode = ecgLibConfig.getContinuousAuthEnable();
            String msg = "Use case:" + useCase + "Auth enabled:" + authMode;
            sendStatus(msg);
        }
    };


    public void startSensorTest(SensorAlarm sensorAlarm) {
        mSensorAlarm = sensorAlarm;
        switch(sensorType) {
            case OFFBODY:
                startOffBodySensorTest();
                break;

            case OFFBODYENHANCED:
                startOffBodyEnhancedSensorTest();
                break;

            case HEARTRATE:
                startHRSensorTest();
                startSPO2SensorTest();
                break;

            case OFFBODYANDHEARTRATE:
                startOffBodySensorTest();
                startHRSensorTest();
                break;
            case ECG:
                startEcgSensorTest();
                break;
            case SLEEP_MON:
                startSleepMonitorTest();
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
                stopSPO2SensorTest();
                break;

            case OFFBODYANDHEARTRATE:
                stopOffBodySensorTest();
                stopHRSensorTest();
                break;
            case ECG:
                stopEcgSensorTest();
                break;
            case SLEEP_MON:
                stopSleepMonitorTest();
                break;
        }
    }

    private void startHRSensorTest() {
        if (!isHRStarted && heartRateSensor != null) {
            heartRateCounter = HEART_RATE_SAMPLE_COUNT;
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

    private void startSPO2SensorTest() {
        if (!isSpo2Started && spo2Sensor != null) {
            spo2Counter = HEART_RATE_SAMPLE_COUNT;
            sensorManager.registerListener(spo2SensorTestListener, spo2Sensor, SensorManager.SENSOR_DELAY_NORMAL);
            Log.d(TAG, "Start SPO2 Sensor");
            isSpo2Started = true;
        }
    }

    private void stopSPO2SensorTest() {
        if (isSpo2Started) {
            sensorManager.unregisterListener(spo2SensorTestListener);
            Log.d(TAG, "Stop SPO2 Sensor");
            isSpo2Started = false;
        }
    }

    private void startSleepMonitorTest() {
        isSleepMonitorEnabled = true;
        sleepMonitorState = 0;
        startHRSensorTest();
        startSPO2SensorTest();
    }

    private void stopSleepMonitorTest() {
        stopSPO2SensorTest();
        stopHRSensorTest();
        sleepMonitorState = 0;
        isSleepMonitorEnabled = false;
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

    private void startEcgSensorTest() {
        if (getDeviceModelName().equals(mContext.getString(R.string.A4100A1))) {
            if (!isEcgStarted && ecgSensorData != null) {
                ecgCounter = ECG_DURATION_SEC;  //initialize the ECG data reading counter
                sensorManager.registerListener(ecgSensorDataTestListener, ecgSensorData, SensorManager.SENSOR_DELAY_NORMAL);

                //Reading raw ECG sample data is disabled. Uncomment the code below to enable it
                //sensorManager.registerListener(ecgSampleDataTestListener, ecgSampleData, SensorManager.SENSOR_DELAY_NORMAL);

                isEcgStarted = true;
            }
        }
    }

    private void stopEcgSensorTest() {
        if (getDeviceModelName().equals(mContext.getString(R.string.A4100A1))) {
            if (isEcgStarted) {
                sensorManager.unregisterListener(ecgSensorDataTestListener);

                //Reading raw ECG sample data is disabled. Uncomment the code below to enable it
                //sensorManager.unregisterListener(ecgSampleDataTestListener);

                isEcgStarted = false;
            }
        }
    }

    private synchronized void sendStatus(String message){
        Intent intent = new Intent();
        intent.setAction(MainService.SENDSTATUS);
        intent.putExtra("status",message);
        mContext.sendBroadcast(intent);
    }

    public String getDeviceModelName() {
        return Build.MODEL;
    }

}