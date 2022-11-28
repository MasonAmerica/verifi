package com.example.verifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class DataConnAlarmReceiver extends BroadcastReceiver {
    private static final String TAG = "verifi.DataConnAlarmReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        Log.d(TAG, "Alarm is triggered at Current time: " + System.currentTimeMillis());

        if (action.equals(MainService.DATA_CONN_ALARM_ACTION)) {

            //need to start Data Conn Test on background thread
            TestScheduler testScheduler = TestScheduler.getInstance();
            testScheduler.startDataConnTest();

            //set Alarm again for the next interval
            MainService.startDataConnAlarm(context);
        }
    }
}
