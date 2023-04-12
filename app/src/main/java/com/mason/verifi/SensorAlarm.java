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

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

// This class creates periodic timer using Alarm service
// to start sensor test
public class SensorAlarm extends BroadcastReceiver {
    private static final String TAG = "verifi.SensorAlarm";
    public static final String SENSOR_ALARM_ACTION = "sensor_alarm_action";
    private AlarmManager sensorAlarmManager;
    private PendingIntent sensorPendingIntent;

    // this constructor is called by the alarm manager
    public SensorAlarm() {
    }

    public SensorAlarm(Context context) {
        startSensorAlarm(context);
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        Log.d(TAG, "Sensor Alarm is triggered at Current time: " + System.currentTimeMillis());

        if (action.equals(SENSOR_ALARM_ACTION)) {
            //need to start Sensor Test on background thread
            if (MainService.getTestScheduler() != null)
                MainService.getTestScheduler().startSensorTest(this);
            else
                Log.e(TAG, "Failed to start Sensor Test. Null TestScheduler reference");

            //For SLEEP_MON, don't restart sensor alarm here because it will be restarted in the
            //SLEEP_MON state machine in SensorTest.java
            if (TestPreference.getInstance().getSensorType() != SensorType.SLEEP_MON) {
                //set Alarm again for the next interval
                startSensorAlarm(context);
            }
        }
    }

    public void startSensorAlarm(Context context)
    {
        Intent intent = new Intent(context, SensorAlarm.class).setAction(SENSOR_ALARM_ACTION);

        sensorPendingIntent = PendingIntent.getBroadcast(
                context, 5129, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        sensorAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        long currentTime = System.currentTimeMillis();
        long futureTime =  TestPreference.getInstance().getSensorInterval() * 1000L;

        long alarmTime =  currentTime + futureTime;

        sensorAlarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTime, sensorPendingIntent);

        Log.d(TAG, "Current time: " + currentTime + " set Sensor Alarm to: " + alarmTime);
    }

    public void stopSensorAlarm() {
        sensorAlarmManager.cancel(sensorPendingIntent);
    }
}
