package com.example.verifi;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

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
                MainService.getTestScheduler().startSensorTest();
            else
                Log.e(TAG, "Failed to start Sensor Test. Null TestScheduler reference");

            //set Alarm again for the next interval
            startSensorAlarm(context);
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
