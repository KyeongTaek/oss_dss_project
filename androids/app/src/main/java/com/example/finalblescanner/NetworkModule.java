package com.example.finalblescanner;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

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

    public static void showStatusDialog(Context cont, String title, String message) { // 통신 결과를 보여주는, 확인버튼만 있는 dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(cont); // activity 컨텍스트를 받아, UI를 띄워주는 틀.
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss(); // 확인 버튼 누르면 창 닫음
                    }
                })
                .create()
                .show();
    }

    public static SpecialTypeDataRequest fromSpecialSensorData(SpecialSensorData data, String deviceId, double lat, double lon) {
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

    public static CommonTypeDataRequest fromCommonSensorData(CommonSensorData data, String deviceId) {
        return new CommonTypeDataRequest(
                data.getDeviceAddress(),
                data.getDeviceName(),
                deviceId,
                "mobile",
                data.getTemperature(),
                data.getEco2(),
                data.getUnixTimestamp(),
                data.getRssi()
        );
    }
}

// 특수 라즈베리파이 센서 포맷
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
// 일반 라즈베리파이 센서 포맷
class CommonTypeDataRequest {
    @SerializedName("mac")          String mac;
    @SerializedName("sensor")
                                    String sensor;
    @SerializedName("receiver")     String receiver;
    @SerializedName("mode")         String mode;
    @SerializedName("temperature")  double temp;
    @SerializedName("co2")          int eco2;
    @SerializedName("sensing_time") long timestamp;
    @SerializedName("rssi")         int rssi;

    public CommonTypeDataRequest(String mac, String sensor, String receiver,
                                 String mode, double temp, int eco2,
                                 long timestamp, int rssi) {
        this.mac = mac; this.sensor = sensor; this.receiver = receiver;
        this.mode = mode; this.temp = temp; this.eco2 = eco2;
        this.timestamp = timestamp; this.rssi = rssi;
    }
}

// 특수 라즈베리파이 센서 데이터에 대한 응답
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
// refresh나 status에 대한 응답
class ServerDataResponse {
    @SerializedName("statusCode")   private int statusCode;
    @SerializedName("message")      private String message;
    @SerializedName("data")         private CampusData data;

    public int getStatusCode()          {   return statusCode; }
    public String getMessage()          {   return message; }
    public CampusData getCampusData()   {   return data; }

    class CampusData {
        @SerializedName("campus_humidity")  private double humid;
        @SerializedName("campus_aqi")       private double aqi;
        @SerializedName("buildings")        private List<Building> buildings;

        public double getCampusHumidity()   {   return humid; }
        public double getCampusAqi()        {   return aqi; }
        public List<Building> getBuildings()  {   return buildings; }

        class Building {
            @SerializedName("building_name")        private String name;
            @SerializedName("building_ext_temp")    private double temp;
            @SerializedName("building_ext_co2")     private double co2;
            @SerializedName("operating_status")     private String status;
            @SerializedName("recommendation_msg")   private String msg;

            public String getName()     {   return name; }
            public double getTemp()     {   return temp; }
            public double getCo2()      {   return co2; }
            public String getStatus()   {   return status; }
            public String getMsg()      {   return msg; }
        }
    }
}

// 수업 서버로의 요청
interface ClassApiService {
    // sensor/sensing에 대한 GET
    @GET("sensor/sensing/")
    Call<String> get(
            @Query("mac") String mac,
            @Query("sensor") String sensor,
            @Query("receiver") String receiver,
            @Query("mode") String mode,
            @Query("temperature") double temperature,
            @Query("co2") int co2,
            @Query("sensing_time") long sensing_time,
            @Query("rssi") int rssi
    );

    // 엔드포인트에 POST로 SpecialTypeDataRequest를 보낼 것임을 명시
    @POST("sensor/opensrc/upload/")
    Call<SpecialTypeDataResponse> sendSensorData(@Body SpecialTypeDataRequest data);
}
// 분석 서버로의 요청
interface AnalysisApiService {
    // api/campus/refresh에 대한 POST
    @POST("api/campus/refresh/")
    Call<ServerDataResponse> triggerRefresh();

    // api/campus/status에 대한 GET
    @GET("api/campus/status/")
    Call<ServerDataResponse> get();
}