package com.example.finalblescanner;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.provider.Settings;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BleScanService extends Service {

    private static final String TAG = "BleScanService";
    private static final String CHANNEL_ID = "ble_test_channel";
    private static final int NOTIFICATION_ID = 999;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean isRunning = false;

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    private ClassApiService classApiService;
    private AnalysisApiService analysisApiService;

    private double currentLat = 36.6287; // 기본값
    private double currentLon = 127.4606;

    private static Context context;

    public static void initialize(Context ctx) {
        context = ctx.getApplicationContext(); // 메모리 누수 방지
    }

    private final Runnable scanRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isRunning) return;
            startTargetedPiScan();
            handler.postDelayed(this, 10000);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        initBluetoothScanner();
        initNetworkClient();
    }

    private void initBluetoothScanner() {
        try {
            BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (bluetoothManager != null) {
                bluetoothAdapter = bluetoothManager.getAdapter();
                if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
                    bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
                } else {
                    Log.e(TAG, "⚠ [경고] 스마트폰의 블루투스 기능이 꺼져 있습니다!");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "⚠ 블루투스 초기화 오류: " + e.getMessage());
        }
    }

    private void initNetworkClient() {
//        Retrofit retrofit = new Retrofit.Builder()
//                .baseUrl("http://168.107.29.35/")
//                .addConverterFactory(GsonConverterFactory.create())
//                .build();
//
//        apiService = retrofit.create(ApiService.class);



        this.classApiService = NetworkModule
                .getClassConn()
                .create(ClassApiService.class);

        this.analysisApiService = NetworkModule
                .getAnalysisConn()
                .create(AnalysisApiService.class);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!isRunning) {
            isRunning = true;
            startForegroundWithNotification();
            handler.post(scanRunnable);
        }
        return START_STICKY;
    }

    @SuppressLint("MissingPermission")
    private void startTargetedPiScan() {
        if (bluetoothLeScanner == null) initBluetoothScanner();
        if (bluetoothLeScanner == null) {
            Log.e(TAG, "🛑 [스캔 건너뜀] 블루투스가 켜지지 않아 스캔을 대기합니다.");
            return;
        }

        Log.d(TAG, "⏰ [10초 주기 완료] 라즈베리파이 스캔 및 서버 전송 루프 가동...");

        List<String> macList = new ArrayList<>();
        macList.add("D8:3A:DD:79:8E:BF");
        macList.add("B8:27:EB:D3:40:06");
        macList.add("88:A2:9E:9B:5E:6A");
        macList.add("D8:3A:DD:79:8F:80");
        macList.add("D8:3A:DD:C1:88:BD");
        macList.add("DC:A6:32:C5:DD:57");

        UUID serviceUUID = UUID.fromString("0000181A-0000-1000-8000-00805F9B34FB");

        List<ScanFilter> filters = new ArrayList<>();
        for (String address : macList) {
            ScanFilter filter = new ScanFilter.Builder()
                    .setDeviceAddress(address)
                    .setServiceUuid(new ParcelUuid(serviceUUID))
                    .build();
            filters.add(filter);
        }

        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build();

        try {
            bluetoothLeScanner.startScan(filters, settings, scanCallback);

            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() { stopBleScan(); }
            }, 3000);
        } catch (Exception e) {
            Log.e(TAG, "⚠ 스캔 오류: " + e.getMessage());
        }
    }

    @SuppressLint("MissingPermission")
    private void stopBleScan() {
        if (bluetoothLeScanner != null && isRunning) {
            try {
                bluetoothLeScanner.stopScan(scanCallback);
                Log.d(TAG, "🛑 스캔 세션 종료. 다음 주기를 대기합니다.\n------------------------");
            } catch (Exception e) {
                Log.e(TAG, "🛑 스캔 정지 중 에러 발생");
            }
        }
    }

    private final ScanCallback scanCallback = new ScanCallback() {
        @Override
        @SuppressLint("MissingPermission")
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);

            String deviceAddress = result.getDevice().getAddress();
            String deviceName = result.getDevice().getName();
            int rssi = result.getRssi();
            String uuid = "0000181A-0000-1000-8000-00805F9B34FB";

            if (deviceName == null) deviceName = "RPi_Sensor";

            byte[] rawData = null;
            if (result.getScanRecord() != null) {
                rawData = result.getScanRecord().getServiceData(ParcelUuid.fromString(uuid));
            }

            double temp;
            int co2;

            String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID); // 기기 id 가져옴
            if(deviceName.contains("sensor")) { // 일반 센서
                CommonSensorData data = SensorParser.parse(rawData, deviceAddress, deviceName, rssi, uuid);

                temp = data.getTemperature();
                co2 = data.getEco2();

                CommonTypeDataRequest request = NetworkModule.fromCommonSensorData(data, deviceId);
//                uploadCommonSensorData(request);
            }
            else {
                SpecialSensorData data = SensorParser.parse(rawData, deviceAddress, deviceName, rssi, uuid, currentLat, currentLon);

                temp = data.getTemperature();
                co2 = data.getEco2();

                SpecialTypeDataRequest request = NetworkModule.fromSpecialSensorData(data, deviceId, currentLat, currentLon);
//                uploadSensorData(request);
            }

            Log.d(TAG, "==================================================");
            Log.d(TAG, "🎯 [라즈베리파이 센서 장치 감지!!]");
            Log.d(TAG, "  ▶ 장치 이름 : " + deviceName);
            Log.d(TAG, "  ▶ MAC 주소  : " + deviceAddress);
            Log.d(TAG, "  ▶ 신호 세기 : " + rssi + " dBm");
            Log.d(TAG, "--------------------------------------------------");
            Log.d(TAG, "  [📊 실시간 수집된 센서 정보]");
            Log.d(TAG, "  🌡️ 현지 온도 : " + temp + " °C");
            Log.d(TAG, "  💨 이산화탄소: " + co2 + " ppm");
            Log.d(TAG, "==================================================");

            Intent broadcastIntent = new Intent("com.example.bletimertester.SENSOR_DATA_UPDATE");
            broadcastIntent.putExtra("macAddress", deviceAddress);
            broadcastIntent.putExtra("temp", temp);
            broadcastIntent.putExtra("co2", co2);
            sendBroadcast(broadcastIntent);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Log.e(TAG, "⚠ 타겟 스캔 실패 코드: " + errorCode);
        }
    };


//    private void postSensorDataToServer(SensorData data) {
//        if (apiService == null) return;
//
//        Call<ResponseBody> call = apiService.sendSensorData(data);
//        call.enqueue(new Callback<ResponseBody>() {
//            @Override
//            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
//                Log.i(TAG, "🚀 [외부 전송 콜백 확인 완료] 성공으로 간주하고 B파트 자체 분석 서버 트리거를 연동합니다. (코드: " + response.code() + ")");
//                triggerBPartServerRefresh();
//            }
//
//            @Override
//            public void onFailure(Call<ResponseBody> call, Throwable t) {
//                Log.e(TAG, "❌ [외부 전송 실패] 원인: " + t.getMessage());
//            }
//        });
//    }
//
//    private void triggerBPartServerRefresh() {
//        if (apiService == null) return;
//
//        Call<ResponseBody> refreshCall = apiService.triggerCampusRefresh();
//        refreshCall.enqueue(new Callback<ResponseBody>() {
//            @Override
//            public void onResponse(Call<ResponseBody> refreshCall, Response<ResponseBody> response) {
//                Log.i(TAG, "🔥 [B파트 서버 리프레시 연동 성공] POST /api/campus/refresh 완료! (응답코드: " + response.code() + ")");
//            }
//
//            @Override
//            public void onFailure(Call<ResponseBody> refreshCall, Throwable t) {
//                Log.e(TAG, "❌ [B파트 서버 연결 실패] 리프레시 트리거 전송 실패. 원인: " + t.getMessage());
//            }
//        });
//    }


    public void uploadSensorData(SpecialTypeDataRequest data) {
        if (!NetworkModule.isNetworkAvailable(context)) { //현재 인터넷 연결 여부를 확인
            NetworkModule.showStatusDialog(
                    context,
                    "네트워크 오류",
                    "인터넷 연결 상태를 확인해주세요."
            );
            return;
        }

        // data를 POST로 서버에 전송하고 응답은 SpecialTypeDataResponse 형태로 받을 것이라 명시.
        Call<SpecialTypeDataResponse> call = classApiService.sendSensorData(data); // 통신 시작, 상태 관리 등을 할 수 있는 Call<DataResponse> 객체를 call에 담음.

        // enqueue()를 이용한 비동기 통신 시작(한 번만 보냄!)
        // 앱 화면을 멈추지 않고 서버 요청을 백그라운드에서 처리
        call.enqueue(new Callback<SpecialTypeDataResponse>() {

            //서버가 응답 돌려줬을 때 실행
            @Override
            public void onResponse(Call<SpecialTypeDataResponse> call, Response<SpecialTypeDataResponse> response) {
                //응답 코드별 처리(성공, 실패 분기 / 다이얼로그 표시)
                if (response.isSuccessful() && response.body() != null) { // 응답코드가 200~300 사이이고(성공) 응답내용이 비어있지 않다면
                    SpecialTypeDataResponse dataResponse = response.body();
                    if (!"Success".equals(dataResponse.getResult())) { // result가 success라면
                        NetworkModule.showStatusDialog(
                                context,
                                dataResponse.getResult(),
                                dataResponse.getMessage()
                        );
                    }
                }
                else { // 성공하지 못한 경우
                    String errorMsg = "";
                    switch (response.code()) { // 응답코드에 따라
                        case 400: errorMsg = "잘못된 요청 (데이터 형식을 확인하세요)"; break;
                        case 404: errorMsg = "서버 경로를 찾을 수 없습니다 (404)"; break;
                        case 500: errorMsg = "서버 내부 오류 발생 (500)"; break;
                        default: errorMsg = "통신 에러 (Code: " + response.code() + ")"; break;
                    }

                    NetworkModule.showStatusDialog(
                            context,
                            "에러",
                            errorMsg
                    );
                }
            }

            //서버와 통신 자체가 실패했을 때 실행함
            //타임아웃, 인터넷 끊김, 서버 다운 등의 이유
            @Override
            public void onFailure(Call<SpecialTypeDataResponse> call, Throwable t) {
                //개발자 확인용
                Log.e(TAG, "요청 실패", t);

                //사용자 내용 다이얼로그
                NetworkModule.showStatusDialog(
                        context,
                        "통신 오류",
                        "요청 실패: " + t.getMessage()
                );
            }
        });
    }

    public void uploadCommonSensorData(CommonTypeDataRequest data) {
        if (!NetworkModule.isNetworkAvailable(context)) { //현재 인터넷 연결 여부를 확인
            NetworkModule.showStatusDialog(
                    context,
                    "네트워크 오류",
                    "인터넷 연결 상태를 확인해주세요."
            );
            return;
        }

        Call<String> call = classApiService.get(data.mac, data.sensor, data.receiver, data.mode, data.temp, data.eco2, data.timestamp, data.rssi);

        // enqueue()를 이용한 비동기 통신 시작(한 번만 보냄!)
        // 앱 화면을 멈추지 않고 서버 요청을 백그라운드에서 처리
        call.enqueue(new Callback<String>() {

            //서버가 응답 돌려줬을 때 실행
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                //응답 코드별 처리(성공, 실패 분기 / 다이얼로그 표시)
                if (response.isSuccessful()) { // 응답코드가 200~300 사이(성공)
                    NetworkModule.showStatusDialog(
                            context,
                            "성공",
                            "등록에 성공했습니다"
                    );
                }
                else { // 성공하지 못한 경우
                    String errorMsg = "";
                    switch (response.code()) { // 응답코드에 따라
                        case 400: errorMsg = "잘못된 요청 (데이터 형식을 확인하세요)"; break;
                        case 404: errorMsg = "서버 경로를 찾을 수 없습니다 (404)"; break;
                        case 500: errorMsg = "서버 내부 오류 발생 (500)"; break;
                        default: errorMsg = "통신 에러 (Code: " + response.code() + ")"; break;
                    }

                    NetworkModule.showStatusDialog(
                            context,
                            "에러",
                            errorMsg
                    );
                }
            }

            //서버와 통신 자체가 실패했을 때 실행함
            //타임아웃, 인터넷 끊김, 서버 다운 등의 이유
            @Override
            public void onFailure(Call<String> call, Throwable t) {
                //개발자 확인용
                Log.e(TAG, "요청 실패", t);

                //사용자 내용 다이얼로그
                NetworkModule.showStatusDialog(
                        context,
                        "통신 오류",
                        "요청 실패: " + t.getMessage()
                );
            }
        });
    }

    public void triggerRefresh() {
        if (!NetworkModule.isNetworkAvailable(context)) { //현재 인터넷 연결 여부를 확인
            NetworkModule.showStatusDialog(
                    context,
                    "네트워크 오류",
                    "인터넷 연결 상태를 확인해주세요."
            );
            return;
        }

        // data를 POST로 서버에 전송하고 응답은 SpecialTypeDataResponse 형태로 받을 것이라 명시.
        Call<ServerDataResponse> call = analysisApiService.triggerRefresh();

        // enqueue()를 이용한 비동기 통신 시작(한 번만 보냄!)
        // 앱 화면을 멈추지 않고 서버 요청을 백그라운드에서 처리
        call.enqueue(new Callback<ServerDataResponse>() {

            //서버가 응답 돌려줬을 때 실행
            @Override
            public void onResponse(Call<ServerDataResponse> call, Response<ServerDataResponse> response) {
                //응답 코드별 처리(성공, 실패 분기 / 다이얼로그 표시)
                if (response.isSuccessful() && response.body() != null) { // 응답코드가 200~300 사이이고(성공) 응답내용이 비어있지 않다면
                    ServerDataResponse dataResponse = response.body();
                    if (dataResponse.getStatusCode() != 200) { // result가 success라면
                        NetworkModule.showStatusDialog(
                                context,
                                String.valueOf(dataResponse.getStatusCode()),
                                dataResponse.getMessage()
                        );
                    }
                }
                else { // 성공하지 못한 경우
                    String errorMsg = "";
                    switch (response.code()) { // 응답코드에 따라
                        case 400: errorMsg = "잘못된 요청 (데이터 형식을 확인하세요)"; break;
                        case 404: errorMsg = "서버 경로를 찾을 수 없습니다 (404)"; break;
                        case 500: errorMsg = "서버 내부 오류 발생 (500)"; break;
                        default: errorMsg = "통신 에러 (Code: " + response.code() + ")"; break;
                    }

                    NetworkModule.showStatusDialog(
                            context,
                            "에러",
                            errorMsg
                    );
                }
            }

            //서버와 통신 자체가 실패했을 때 실행함
            //타임아웃, 인터넷 끊김, 서버 다운 등의 이유
            @Override
            public void onFailure(Call<ServerDataResponse> call, Throwable t) {
                //개발자 확인용
                Log.e(TAG, "요청 실패", t);

                //사용자 내용 다이얼로그
                NetworkModule.showStatusDialog(
                        context,
                        "통신 오류",
                        "요청 실패: " + t.getMessage()
                );
            }
        });
    }

    public void getCampusStatus() {
        if (!NetworkModule.isNetworkAvailable(context)) { //현재 인터넷 연결 여부를 확인
            NetworkModule.showStatusDialog(
                    context,
                    "네트워크 오류",
                    "인터넷 연결 상태를 확인해주세요."
            );
            return;
        }

        Call<ServerDataResponse> call = analysisApiService.get();

        // enqueue()를 이용한 비동기 통신 시작(한 번만 보냄!)
        // 앱 화면을 멈추지 않고 서버 요청을 백그라운드에서 처리
        call.enqueue(new Callback<ServerDataResponse>() {

            //서버가 응답 돌려줬을 때 실행
            @Override
            public void onResponse(Call<ServerDataResponse> call, Response<ServerDataResponse> response) {
                //응답 코드별 처리(성공, 실패 분기 / 다이얼로그 표시)
                if (response.isSuccessful()) { // 응답코드가 200~300 사이(성공)
                    ServerDataResponse dataResponse = response.body();

                    MainActivity.fillMap(dataResponse.getCampusData());
                    MainActivity.fillBottomSheet(dataResponse.getCampusData());
                }
                else { // 성공하지 못한 경우
                    String errorMsg = "";
                    switch (response.code()) { // 응답코드에 따라
                        case 400: errorMsg = "잘못된 요청 (데이터 형식을 확인하세요)"; break;
                        case 404: errorMsg = "서버 경로를 찾을 수 없습니다 (404)"; break;
                        case 500: errorMsg = "서버 내부 오류 발생 (500)"; break;
                        default: errorMsg = "통신 에러 (Code: " + response.code() + ")"; break;
                    }

                    MainActivity.fillBottomSheet(null);

                    NetworkModule.showStatusDialog(
                            context,
                            "에러",
                            errorMsg
                    );
                }
            }

            //서버와 통신 자체가 실패했을 때 실행함
            //타임아웃, 인터넷 끊김, 서버 다운 등의 이유
            @Override
            public void onFailure(Call<ServerDataResponse> call, Throwable t) {
                //개발자 확인용
                Log.e(TAG, "요청 실패", t);

                MainActivity.fillBottomSheet(null);

                //사용자 내용 다이얼로그
                NetworkModule.showStatusDialog(
                        context,
                        "통신 오류",
                        "요청 실패: " + t.getMessage()
                );
            }
        });
    }


    private void startForegroundWithNotification() {
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "통신 연동 서비스", NotificationManager.IMPORTANCE_LOW
            );
            if (manager != null) manager.createNotificationChannel(channel);
        }
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("BLE + Retrofit 통합 테스트")
                .setContentText("라즈베리파이 센서 수집 및 가상 서버 전송 가동 중")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setOngoing(true)
                .build();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION);
        } else {
            startForeground(NOTIFICATION_ID, notification);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
        stopBleScan();
        handler.removeCallbacks(scanRunnable);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) { return null; }
}