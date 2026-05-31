package com.example.finalblescanner;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.kakao.vectormap.KakaoMap;
import com.kakao.vectormap.KakaoMapSdk;
import com.kakao.vectormap.MapView;

import android.util.Log;
import android.widget.TextView;

import java.util.List;
import java.util.Map;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;

public class MainActivity extends AppCompatActivity {
    public static MapView mapView;

    static List<Map<String, Object>> building_loc = List.of(
            Map.of(
                    "building", "법학전문대학원",
                    "lat", 36.63218943,
                    "lon", 127.453852
            ),
            Map.of(
                    "building", "CBNU스포츠센터",
                    "lat", 36.62731552,
                    "lon", 127.4608165
            ),
            Map.of(
                    "building", "중앙도서관",
                    "lat", 36.62848293,
                    "lon", 127.4574258
            )
    );

    private static final int PERMISSION_REQUEST_CODE = 1000;

    public static TextView buildingView;
    public static TextView tempView;
    public static TextView co2View;
    public static TextView statusView;
    public static TextView humidityView;
    public static TextView operationView;

    private TransferManager transferManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        KakaoMapSdk.init(this, BuildConfig.API_KEY);
        mapView = findViewById(R.id.map_view);

        CampusMapCallback campusMapCallback = new CampusMapCallback(building_loc, null);
        mapView.start(campusMapCallback.lifeCycleCallback, campusMapCallback.readyCallback);

        // onCreate 내부 또는 적절한 위치에 추가
        String keyHash = KakaoMapSdk.INSTANCE.getHashKey();
        Log.d("KakaoKeyHash", "내 키 해시값: " + keyHash);

        initBottomSheet();

        // 🚀 앱 켜지자마자 권한 체크 및 팝업창 띄우기
        checkAndRequestRuntimePermissions();

        // ⏰ 권한과 상관없이 기존처럼 백그라운드 서비스(10초 타이머)는 즉시 시동
        Context context = MainActivity.this;
        BleScanService.initialize(context);
        Intent serviceIntent = new Intent(context, BleScanService.class);

        context.startForegroundService(serviceIntent);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    public void initBottomSheet() {
        buildingView = findViewById(R.id.buildingName_TextView);
        buildingView.setText("선택한 건물: " + "-");
        tempView = findViewById(R.id.temp_TextView);
        tempView.setText("-");
        co2View = findViewById(R.id.co2_TextView);
        co2View.setText("-");
        statusView = findViewById(R.id.status_TextView);
        statusView.setText("-");
        humidityView = findViewById(R.id.humidity_TextView);
        humidityView.setText("-");
        operationView = findViewById(R.id.operation_TextView);
        operationView.setText("-");
    }
    public static void fillMap(ServerDataResponse.CampusData data) {
        CampusMapCallback campusMapCallback = new CampusMapCallback(building_loc, data);

        mapView.pause();
        mapView.resume();
    }
    public static void fillBottomSheet(ServerDataResponse.CampusData data) {
        if (data == null) {
            buildingView.setText("선택한 건물: " + "-");
            tempView.setText("-");
            co2View.setText("-");
            statusView.setText("-");
            humidityView.setText("-");
            operationView.setText("-");
        }
        else {
            ServerDataResponse.CampusData.Building represent = data.getBuildings().get(0);
            buildingView.setText("선택한 건물: " + represent.getName());
            tempView.setText(String.valueOf(represent.getTemp()));
            co2View.setText(String.valueOf(represent.getCo2()));
            statusView.setText(represent.getStatus());
            humidityView.setText(String.valueOf(data.getCampusHumidity()));
            operationView.setText(represent.getMsg());
        }
    }
    public static void fillBottomSheet(ServerDataResponse.CampusData.Building data, double humidity) {
        if (data == null) {
            buildingView.setText("선택한 건물: " + "-");
            tempView.setText("-");
            co2View.setText("-");
            statusView.setText("-");
            humidityView.setText("-");
            operationView.setText("-");
        }
        else {
            ServerDataResponse.CampusData.Building represent = data;
            buildingView.setText("선택한 건물: " + represent.getName());
            tempView.setText(String.valueOf(represent.getTemp()));
            co2View.setText(String.valueOf(represent.getCo2()));
            statusView.setText(represent.getStatus());
            humidityView.setText(String.valueOf(humidity));
            operationView.setText(represent.getMsg());
        }
    }

    /**
     * 🔐 안드로이드 버전별 필수 권한 팝업 요청
     */
    private void checkAndRequestRuntimePermissions() {
        if (!hasAllPermissions()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // Android 12 이상: 블루투스 스캔, 커넥트, 위치 권한 필수 요청
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.BLUETOOTH_SCAN,
                        Manifest.permission.BLUETOOTH_CONNECT,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                }, PERMISSION_REQUEST_CODE);
            } else {
                // Android 11 이하: 위치 권한만 요청
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                }, PERMISSION_REQUEST_CODE);
            }
        }
    }

    /**
     * 현재 권한이 부여되어 있는지 확인하는 함수
     */
    private boolean hasAllPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        } else {
            return checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        }
    }

    /**
     * 사용자가 팝업창에서 허용/거부를 눌렀을 때 결과 콜백
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "블루투스 스캔 권한 승인 완료!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "권한이 거부되었습니다. 스캔이 작동하지 않을 수 있습니다.", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.resume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.pause();
    }
}