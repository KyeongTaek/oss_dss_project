package com.example.finalblescanner;

public class CommonSensorData {
    private String deviceName;
    private String deviceAddress;
    private double temperature;
    private int eco2;

    private String rawHex;
    private int rssi;
    private String uuid;
    //숫자 형식 시간을 받는 변수 추가
    private long unixTimestamp;
    public CommonSensorData(long unixTimestamp, String deviceAddress, String deviceName,
                            double temperature, int eco2, String rawHex, int rssi, String uuid) {
        this.unixTimestamp = unixTimestamp;
        this.deviceAddress = deviceAddress;
        this.deviceName = deviceName;

        this.temperature = temperature;
        this.eco2 = eco2;

        this.rawHex = rawHex;
        this.rssi = rssi;
        this.uuid = uuid;
    }

    public long getUnixTimestamp() { return unixTimestamp;}
    public String getDeviceName() {return deviceName;}
    public String getDeviceAddress() {return deviceAddress;}
    public double getTemperature() {return temperature;}
    public int getEco2(){return eco2;}

    public String getRawHex() {return rawHex;}
    public int getRssi(){return rssi;}
    public String getUuid(){return uuid;}

}
