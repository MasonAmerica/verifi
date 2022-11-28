package com.example.verifi;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class MainService extends Service {
    private static final String TAG = "verifi.MainService";

    public static final String SENDSTATUS = "SENDSTATUS";
    public static final String DATA_CONN_ALARM_ACTION = "data_conn_alarm_action";

    private TestScheduler testScheduler;
    private TestPreference testPref;

    private static PendingIntent pendingIntent;
    private static AlarmManager alarmManager;


    @Override
    public void onCreate() {
        super.onCreate();

        //TODO: enable this if needed
        //PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        //String packageName = getPackageName();
        //if (!pm.isIgnoringBatteryOptimizations(packageName)) {
        //    Intent intent = new Intent();
        //    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //    intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
        //    intent.setData(Uri.parse("package:" + packageName));
        //    startActivity(intent);
        //}

        //Create Test Scheduler object
        testScheduler = TestScheduler.getInstance(this);

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

        //Start Test based on TestPreference settings
        if (testPref.isEnableGPS()) {
            testScheduler.startGPSTest();
        }

        if (testPref.isEnableSensor()) {
            testScheduler.startSensorTest();
        }

        if (testPref.isEnableDataConn()) {
            //start Data connection Alarm. Alarm receiver will start Data Conn Test
            startDataConnAlarm(getApplicationContext());
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
        }

        if (testPref.isEnableDataConn()) {
            testScheduler.stopDataConnTest();
            stopDataConnAlarm();
        }

        if(testScheduler != null){
            testScheduler.quitSafely();
            testScheduler.interrupt();
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


    public static void startDataConnAlarm(Context context)
    {
        Intent intent = new Intent(context, DataConnAlarmReceiver.class).setAction(DATA_CONN_ALARM_ACTION);

        pendingIntent = PendingIntent.getBroadcast(
                context, 51285128, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        long currentTime = System.currentTimeMillis();
        long futureTime =  TestPreference.getInstance().getDataConnInterval() * 1000L;

        long alarmTime =  currentTime + futureTime;

        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);

        Log.d(TAG, "Current time: " + currentTime + " set Alarm to: " + alarmTime);
    }

    public static void stopDataConnAlarm() {
        alarmManager.cancel(pendingIntent);
    }
}