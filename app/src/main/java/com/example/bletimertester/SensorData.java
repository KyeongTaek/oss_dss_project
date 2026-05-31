package com.example.bletimertester;

public class SensorData {
    private String macAddress;
    private double temperature;
    private double humidity;
    private int co2;
    private int aqi;
    private int rssi;

    public SensorData(String macAddress, double temperature, double humidity, int co2, int aqi, int rssi) {
        this.macAddress = macAddress;
        this.temperature = temperature;
        this.humidity = humidity;
        this.co2 = co2;
        this.aqi = aqi;
        this.rssi = rssi;
    }
}