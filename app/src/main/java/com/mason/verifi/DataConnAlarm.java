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
// to start data connection test
public class DataConnAlarm extends BroadcastReceiver {
    private static final String TAG = "verifi.DataConnAlarm";
    public static final String DATA_CONN_ALARM_ACTION = "data_conn_alarm_action";
    private AlarmManager dataConnAlarmManager;
    private PendingIntent dataConnPendingIntent;

    // this constructor is called by the alarm manager
    public DataConnAlarm() {
    }

    public DataConnAlarm(Context context) {
        startDataConnAlarm(context);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        Log.d(TAG, "Data Connection Alarm is triggered at Current time: " + System.currentTimeMillis());

        if (action.equals(DATA_CONN_ALARM_ACTION)) {
            //need to start Data Conn Test on background thread
            if (MainService.getTestScheduler() != null)
                MainService.getTestScheduler().startDataConnTest();
            else
                Log.e(TAG, "Failed to start Data Connection Test. Null TestScheduler reference");

            //set Alarm again for the next interval
            startDataConnAlarm(context);
        }
    }

    public void startDataConnAlarm(Context context) {
        Intent intent = new Intent(context, DataConnAlarm.class).setAction(DATA_CONN_ALARM_ACTION);

        dataConnPendingIntent = PendingIntent.getBroadcast(
                context, 5128, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        dataConnAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        long currentTime = System.currentTimeMillis();
        long futureTime =  TestPreference.getInstance().getDataConnInterval() * 1000L;

        long alarmTime =  currentTime + futureTime;

        dataConnAlarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTime, dataConnPendingIntent);

        Log.d(TAG, "Current time: " + currentTime + " set Data Connection Alarm to: " + alarmTime);
    }

    public void stopDataConnAlarm() {
        dataConnAlarmManager.cancel(dataConnPendingIntent);
    }

}
