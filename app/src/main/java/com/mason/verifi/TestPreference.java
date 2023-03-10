package com.mason.verifi;
/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


enum GPSType {IZATSDK, LOCMGR}
enum SensorType {OFFBODY, OFFBODYENHANCED, HEARTRATE, OFFBODYANDHEARTRATE, ECG, SLEEP_MON}
enum DataConnType {WIFI, CELL}
//add new test variation here

//This class keeps the test preference settings
public class TestPreference {

    private static TestPreference single_instance = null;


    private GPSType gpsType;
    private SensorType sensorType;
    private DataConnType dataConnType;

    private boolean enableGPS;
    private boolean enableSensor;
    private boolean enableDataConn;

    private int gpsInterval;
    private int sensorInterval;
    private int dataConnInterval;

    private TestPreference() {
        enableGPS = true;
        gpsType = GPSType.LOCMGR;
        gpsInterval = 180; //3 min (3*60) in sec

        enableSensor = true;
        sensorType = SensorType.OFFBODY;
        sensorInterval = 60; //1 min

        enableDataConn = true;
        dataConnType = DataConnType.CELL;
        dataConnInterval = 900; //15 min (15*60) in sec

        //initialize new test parameters here

    }

    public static TestPreference getInstance() {
        if (single_instance == null)
            single_instance = new TestPreference();

        return single_instance;
    }

    public void setEnableGPS(boolean enableGPS) {
        this.enableGPS = enableGPS;
    }
    public boolean isEnableGPS() {
        return enableGPS;
    }
    public void setGpsType(GPSType gpsType) {
        this.gpsType = gpsType;
    }
    public GPSType getGpsType() {
        return gpsType;
    }
    public void setGpsInterval(int gpsInterval) {
        this.gpsInterval = gpsInterval;
    }
    public int getGpsInterval() {
        return gpsInterval;
    }


    public void setEnableSensor(boolean enableSensor) {
        this.enableSensor = enableSensor;
    }
    public boolean isEnableSensor() {
        return enableSensor;
    }
    public void setSensorType(SensorType sensorType) {
        this.sensorType = sensorType;
    }
    public SensorType getSensorType() {
        return sensorType;
    }
    public void setSensorInterval(int sensorInterval) {
        this.sensorInterval = sensorInterval;
    }
    public int getSensorInterval() {
        return sensorInterval;
    }


    public void setEnableDataConn(boolean enableDataConn) {
        this.enableDataConn = enableDataConn;
    }
    public boolean isEnableDataConn() {
        return enableDataConn;
    }
    public void setDataConnType(DataConnType dataConnType) {
        this.dataConnType = dataConnType;
    }
    public DataConnType getDataConnType() {
        return dataConnType;
    }
    public void setDataConnInterval(int dataConnInterval) { this.dataConnInterval = dataConnInterval; }
    public int getDataConnInterval() {
        return dataConnInterval;
    }

    //Add new test set and get functions here
}
