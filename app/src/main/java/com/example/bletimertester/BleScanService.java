package com.example.bletimertester;

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
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class BleScanService extends Service {

    private static final String TAG = "BleScanService";
    private static final String CHANNEL_ID = "ble_test_channel";
    private static final int NOTIFICATION_ID = 999;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean isRunning = false;

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    private ApiService apiService;

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
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://jsonplaceholder.typicode.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ApiService.class);
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

            if (deviceName == null) deviceName = "RPi_Sensor";

            double mockTemp = 24.5;
            double mockHumidity = 50.2;
            int mockCo2 = 450;
            int mockAqi = 1;

            Log.d(TAG, "==================================================");
            Log.d(TAG, "🎯 [라즈베리파이 센서 장치 감지!!]");
            Log.d(TAG, "  ▶ 장치 이름 : " + deviceName);
            Log.d(TAG, "  ▶ MAC 주소  : " + deviceAddress);
            Log.d(TAG, "  ▶ 신호 세기 : " + rssi + " dBm");
            Log.d(TAG, "--------------------------------------------------");
            Log.d(TAG, "  [📊 실시간 수집된 센서 정보]");
            Log.d(TAG, "  🌡️ 현지 온도 : " + mockTemp + " °C");
            Log.d(TAG, "  💧 현지 습도 : " + mockHumidity + " %");
            Log.d(TAG, "  💨 이산화탄소: " + mockCo2 + " ppm");
            Log.d(TAG, "  ✨ 공기질(AQI): " + mockAqi + " 등급");
            Log.d(TAG, "==================================================");

            SensorData readyData = new SensorData(deviceAddress, mockTemp, mockHumidity, mockCo2, mockAqi, rssi);
            postSensorDataToServer(readyData);

            Intent broadcastIntent = new Intent("com.example.bletimertester.SENSOR_DATA_UPDATE");
            broadcastIntent.putExtra("macAddress", deviceAddress);
            broadcastIntent.putExtra("temp", mockTemp);
            broadcastIntent.putExtra("humidity", mockHumidity);
            broadcastIntent.putExtra("co2", mockCo2);
            broadcastIntent.putExtra("aqi", mockAqi);
            sendBroadcast(broadcastIntent);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Log.e(TAG, "⚠ 타겟 스캔 실패 코드: " + errorCode);
        }
    };

    private void postSensorDataToServer(SensorData data) {
        if (apiService == null) return;

        Call<ResponseBody> call = apiService.sendSensorData(data);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.i(TAG, "🚀 [외부 전송 콜백 확인 완료] 성공으로 간주하고 B파트 자체 분석 서버 트리거를 연동합니다. (코드: " + response.code() + ")");
                triggerBPartServerRefresh();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "❌ [외부 전송 실패] 원인: " + t.getMessage());
            }
        });
    }

    private void triggerBPartServerRefresh() {
        if (apiService == null) return;

        Call<ResponseBody> refreshCall = apiService.triggerCampusRefresh();
        refreshCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> refreshCall, Response<ResponseBody> response) {
                Log.i(TAG, "🔥 [B파트 서버 리프레시 연동 성공] POST /api/campus/refresh 완료! (응답코드: " + response.code() + ")");
            }

            @Override
            public void onFailure(Call<ResponseBody> refreshCall, Throwable t) {
                Log.e(TAG, "❌ [B파트 서버 연결 실패] 리프레시 트리거 전송 실패. 원인: " + t.getMessage());
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