package com.example.verifi;

enum GPSType {IZATSDK, LOCMGR}
enum SensorType {OFFBODY, OFFBODYENHANCED, HEARTRATE, OFFBODYANDHEARTRATE}
enum DataConnType {WIFI, CELL}

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
        gpsType = GPSType.IZATSDK;
        gpsInterval = 180; //3 min (3*60) in sec

        enableSensor = true;
        sensorType = SensorType.OFFBODY;
        sensorInterval = 3600; //1 hour (60*60) in sec

        enableDataConn = true;
        dataConnType = DataConnType.CELL;
        dataConnInterval = 14400; //4 hours (4*60*60) in sec

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
