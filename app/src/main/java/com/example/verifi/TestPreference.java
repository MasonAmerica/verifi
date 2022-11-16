package com.example.verifi;

enum GPSType {IZATSDK, LOCMGR}
enum SensorType {PROXIMITY, ANTIOBJECT, HEARTRATE}
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
        gpsInterval = 180; //in sec

        enableSensor = true;
        sensorType = SensorType.PROXIMITY;
        sensorInterval = 300; //in sec

        enableDataConn = false;
        dataConnType = DataConnType.WIFI;
        dataConnInterval = 3600; //in sec

    }

    public static TestPreference getInstance() {
        if (single_instance == null)
            single_instance = new TestPreference();

        return single_instance;
    }

    public void setEnableGPS(boolean enableGPS) {
        this.enableGPS = enableGPS;
    }

    public void setEnableSensor(boolean enableSensor) {
        this.enableSensor = enableSensor;
    }

    public void setEnableDataConn(boolean enableDataConn) {
        this.enableDataConn = enableDataConn;
    }

    public boolean isEnableGPS() {
        return enableGPS;
    }

    public boolean isEnableSensor() {
        return enableSensor;
    }

    public boolean isEnableDataConn() {
        return enableDataConn;
    }

    public void setGpsType(GPSType gpsType) {
        this.gpsType = gpsType;
    }

    public GPSType getGpsType() {
        return gpsType;
    }

    public void setSensorType(SensorType sensorType) {
        this.sensorType = sensorType;
    }

    public SensorType getSensorType() {
        return sensorType;
    }

    public void setDataConnType(DataConnType dataConnType) {
        this.dataConnType = dataConnType;
    }

    public DataConnType getDataConnType() {
        return dataConnType;
    }

    public void setGpsInterval(int gpsInterval) {
        this.gpsInterval = gpsInterval;
    }

    public int getGpsInterval() {
        return gpsInterval;
    }

    public void setSensorInterval(int sensorInterval) {
        this.sensorInterval = sensorInterval;
    }

    public int getSensorInterval() {
        return sensorInterval;
    }

    public void setDataConnInterval(int dataConnInterval) {
        this.dataConnInterval = dataConnInterval;
    }

    public int getDataConnInterval() {
        return dataConnInterval;
    }

}
