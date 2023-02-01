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


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.qti.location.sdk.IZatFlpService;
import com.qti.location.sdk.IZatManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

// This class starts GPS test
// It is using either Android Location Manager or Qualcomm iZat Fused Location Provider
public class GpsTest {
    private static final String TAG = "verifi.GpsTest";

    private final TestPreference testPref;

    private final MainService parentService;

    private GPSType gpsTestType;
    private int gpsTestInterval;
    private boolean gpsTestStarted = false;

    //for Izat SDK
    private IZatManager mIzatMgr = null;
    private IZatFlpService mFlpService = null;
    private IZatFlpService.IZatFlpSessionHandle mFlpHandle = null;
    private FlpLocationCallback mFlpCallback = null;

    //for LocationManager
    private LocMgrListener locMgrListener = null;
    private LocationManager locMgr = null;


    public GpsTest(MainService pService) {
        testPref = TestPreference.getInstance();
        parentService = pService;
    }

    public void startGpsTest() {
        if (!gpsTestStarted) {
            gpsTestType = testPref.getGpsType();
            gpsTestInterval = testPref.getGpsInterval() * 1000;  //convert to mSec

            if (gpsTestType == GPSType.IZATSDK) {
                startIzatSDK();
            } else {
                startLocMgr();
            }
            gpsTestStarted = true;
        }
    }

    public void stopGpsTest() {
        if (gpsTestStarted) {
            if (gpsTestType == GPSType.IZATSDK) {
                stopIzatSDK();
            } else {
                stopLocMgr();
            }
            gpsTestStarted = false;
        }
    }

    private void startIzatSDK() {
        if (mIzatMgr == null) { //do this one time
            mIzatMgr = IZatManager.getInstance(parentService.getApplicationContext());

            mFlpCallback = new FlpLocationCallback();
        }

        if (mFlpCallback != null) {
            if (mFlpHandle != null) {
                stopIzatSDK();
            }

            Log.d(TAG, "Requesting FLP updates: timeInterval= " + gpsTestInterval + " ms");

            mFlpService = mIzatMgr.connectFlpService();
            IZatFlpService.IzatFlpRequest req = IZatFlpService.IzatFlpRequest.getBackgroundFlprequest();

            req.setPowerMode(1);
            req.setTimeInterval(gpsTestInterval);
            req.setTbmMillis(gpsTestInterval);

            mFlpHandle = mFlpService.startFlpSession(mFlpCallback, req);

            if (mFlpHandle == null)
                Log.e(TAG, "Failed to start GPS Test - cannot start FLP session");
            else
                Log.i(TAG, "Start GPS Test using QC IZat SDK - Fused Location Provider (FLP)");
        }
        else
            Log.e(TAG, "Failed to start GPS Test - cannot create FLP Location Callback");
    }

    private void stopIzatSDK() {
        Log.d(TAG, "Stop GPS Test using QC IZat SDK");

        if (mFlpHandle != null) {
            mFlpService.stopFlpSession(mFlpHandle);
            mFlpHandle = null;
            Log.i(TAG, "FLP session is stopped");
        }
    }

    private void startLocMgr() {
        locMgrListener = new LocMgrListener();

        Log.d(TAG, "Requesting Android Location Manager updates: timeInterval= " + gpsTestInterval + " ms");
        locMgr = (LocationManager) parentService.getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(parentService, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(parentService, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Log.e(TAG, "Failed to start GPS Test - permission to get location is not granted");
            return;
        }
        locMgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, gpsTestInterval, 0, locMgrListener);
        Log.i(TAG, "Start GPS Test using Android Location Manager");
    }

    private void stopLocMgr() {
        Log.d(TAG, "Stop GPS Test using Android Location Manager");
        if (locMgr != null) {
            locMgr.removeUpdates(locMgrListener);
            Log.i(TAG, "Location Manager Listener is removed");
        }
    }

    public class FlpLocationCallback implements IZatFlpService.IFlpLocationCallback {
        @Override
        public void onLocationAvailable(Location[] locations) {
            if (locations != null && locations.length > 0) {

                Log.i(TAG, locations.length + " FLP locations received");

                for (Location loc : locations) {
                    Log.i("FLP", "Location received from FLP: " + loc.toString());

                    String longitudeStr = String.format(Locale.US,"%.03f", loc.getLongitude());
                    String latitudeStr = String.format(Locale.US,"%.03f", loc.getLatitude());
                    String hAccStr = String.format(Locale.US,"%.01f", loc.getAccuracy());
                    //String vAccStr = String.format(Locale.US, "%.01f", loc.getVerticalAccuracyMeters());

                    Date df = new java.util.Date(loc.getTime());
                    String ts = new SimpleDateFormat("MM-dd HH:mm:ss", Locale.US).format(df);

                    parentService.sendStatus(ts + " - " + latitudeStr + " - " + longitudeStr + " - " + hAccStr);
                }
            }
        }
    }

    public class LocMgrListener implements LocationListener {
        @Override
        public void onLocationChanged(Location location) {
            Log.i(TAG, "Location Mgr Update: " + location.toString());

            String longitudeStr = String.format(Locale.US,"%.03f", location.getLongitude());
            String latitudeStr = String.format(Locale.US,"%.03f", location.getLatitude());
            String hAccStr = String.format(Locale.US,"%.01f", location.getAccuracy());
            //String vAccStr = String.format(Locale.US, "%.01f", location.getVerticalAccuracyMeters());

            Date df = new java.util.Date(location.getTime());
            String ts = new SimpleDateFormat("MM-dd HH:mm:ss", Locale.US).format(df);

            parentService.sendStatus(ts + " - " + latitudeStr + " - " + longitudeStr + " - " + hAccStr);
        }

        @Override
        public void onProviderDisabled(@NonNull String provider) {
            Log.w(TAG, "onProviderDisabled");
            parentService.sendStatus("LocMgr - onProviderDisabled");
        }

        @Override
        public void onProviderEnabled(@NonNull String provider) {
            Log.w("myApp", "[#] GPSApplication.java - onProviderEnabled");
            parentService.sendStatus("LocMgr - onProviderEnabled");
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            // This is called when the GPS status changes
            switch (status) {
                case LocationProvider.OUT_OF_SERVICE:
                    Log.w(TAG, "GPS Out of Service");
                    parentService.sendStatus("LocMgr - GPS Out of Service");
                    break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    Log.w(TAG, "GPS Temporarily Unavailable");
                    parentService.sendStatus("LocMgr - GPS Temporarily Unavailable");
                    break;
                case LocationProvider.AVAILABLE:
                    Log.w(TAG, "GPS Available");
                    parentService.sendStatus("LocMgr - GPS Available");
                    break;
            }
        }
    }

}
