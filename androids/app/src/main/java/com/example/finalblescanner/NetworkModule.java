package com.example.finalblescanner;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.POST;

public class NetworkModule {
    private static final String ANALYSIS_BASE_URL = BuildConfig.ANALYSIS_SERVER_URL; // 직접 url을 적지 않고 local.properties에 적음
    private static final String CLASS_BASE_URL = BuildConfig.CLASS_SERVER_URL;

    // 싱글톤(전체에 analysis_conn, class_conn 객체 각각 한 개) 위한 conn 변수 선언
    private static Retrofit analysis_conn = null;
    private static Retrofit class_conn = null;

    // retrofit 인스턴스 반환 함수
    public static Retrofit getAnalysisConn() {
        if (analysis_conn == null) {
            Gson gson = new GsonBuilder().setLenient().create(); // json을 java object로 바꿔주고, 반대로도 변환해주는 gson. json을 파싱할 때 느슨한 규칙(lenient) 적용

            analysis_conn = new Retrofit.Builder()
                    .baseUrl(ANALYSIS_BASE_URL)
                    .addConverterFactory((ScalarsConverterFactory.create()))
                    .addConverterFactory((GsonConverterFactory.create()))
                    .build();
        }

        return analysis_conn;
    }
    public static Retrofit getClassConn() {
        if (class_conn == null) {
            Gson gson = new GsonBuilder().setLenient().create(); // json을 java object로 바꿔주고, 반대로도 변환해주는 gson. json을 파싱할 때 느슨한 규칙(lenient) 적용

            class_conn = new Retrofit.Builder()
                    .baseUrl(CLASS_BASE_URL)
                    .addConverterFactory((ScalarsConverterFactory.create()))
                    .addConverterFactory((GsonConverterFactory.create()))
                    .build();
        }

        return class_conn;
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {
            NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
            if (capabilities != null) {
                return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR);
            }
        }

        return false;
    }

    public static SpecialTypeDataRequest fromSpecialSensorData(SensorData data, String deviceId, double lat, double lon) {
        return new SpecialTypeDataRequest(
                "opensrc2026",
                "team 5",
                data.getDeviceName(),
                data.getDeviceAddress(),
                data.getTemperature(),
                data.getHumidity(),
                data.getAqi(),
                data.getTvoc(),
                data.getEco2(),
                data.getUnixTimestamp(),
                lat,
                lon,
                deviceId,
                data.getRssi()
        );
    }
}

class SpecialTypeDataRequest {
    @SerializedName("key")       private String key;
    @SerializedName("team")      private String team;
    @SerializedName("sensor")    private String sensor;
    @SerializedName("mac")       private String mac;
    @SerializedName("temp")      private double temp;
    @SerializedName("humidity")  private double humidity;
    @SerializedName("AQI")       private int aqi;
    @SerializedName("TVOC")      private int tvoc;
    @SerializedName("eCO2")      private int eco2;
    @SerializedName("timestamp") private long timestamp;
    @SerializedName("lat")       private double lat;
    @SerializedName("lon")       private double lon;
    @SerializedName("sender")    private String sender;
    @SerializedName("rssi")      private int rssi;

    public SpecialTypeDataRequest(String key, String team, String sensor, String mac,
                                 double temp, double humidity, int aqi, int tvoc, int eco2,
                                 long timestamp, double lat, double lon, String sender, int rssi) {
        this.key = key; this.team = team; this.sensor = sensor; this.mac = mac;
        this.temp = temp; this.humidity = humidity; this.aqi = aqi;
        this.tvoc = tvoc; this.eco2 = eco2; this.timestamp = timestamp;
        this.lat = lat; this.lon = lon; this.sender = sender; this.rssi = rssi;
    }
}

class SpecialTypeDataResponse {
    @SerializedName("result")        private String result;
    @SerializedName("message")       private String message;
    @SerializedName("received_data") private ReceivedData receivedData;

    public String getResult()  { return result; }
    public String getMessage() { return message; }

    static class ReceivedData {
        @SerializedName("team")   private String team;
        @SerializedName("sensor") private String sensor;
    }
}

interface SpecialApiService{ // 엔드포인트에 POST로 SpecialTypeDataRequest를 보낼 것임을 명시
    @POST("sensor/opensrc/upload/")
    Call<SpecialTypeDataResponse> sendSensorData(@Body SpecialTypeDataRequest data);
}