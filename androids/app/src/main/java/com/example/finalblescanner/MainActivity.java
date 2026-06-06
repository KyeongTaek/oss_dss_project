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
    private static MainActivity instance;
    public static MapView mapView;

    static List<Map<String, Object>> building_loc = List.of(
            Map.of("building", "정문", "lat",	36.63245042, "lon", 	127.4529926),
            Map.of("building", "법학전문대학원", "lat",	36.63218943, "lon", 	127.453852),
            Map.of("building", "테니스장", "lat",	36.63067437, "lon", 	127.4555739),
            Map.of("building", "씨비엔유스타", "lat",	36.63045836, "lon", 	127.4562121),
            Map.of("building", "쿱스켓 대학본부점", "lat",	36.63046548, "lon", 	127.4559997),
            Map.of("building", "산학협력관", "lat",	36.63239667, "lon", 	127.4552732),
            Map.of("building", "국제교류본부 2호관", "lat",	36.63215137, "lon", 	127.4557972),
            Map.of("building", "고시원", "lat",	36.63242647, "lon", 	127.4554947),
            Map.of("building", "형설관", "lat",	36.63283197, "lon", 	127.4559667),
            Map.of("building", "보육교사교육원", "lat",	36.63307883, "lon", 	127.4564512),
            Map.of("building", "국제교류본부 3호관", "lat",	36.63326399, "lon", 	127.4570449),
            Map.of("building", "대학본부", "lat",	36.62992867, "lon", 	127.454514),
            Map.of("building", "공동실험실습관", "lat",	36.629306, "lon", 	127.4552215),
            Map.of("building", "중앙도서관", "lat",	36.62848293, "lon", 	127.4574258),
            Map.of("building", "경영학관", "lat",	36.63006758, "lon", 	127.4568359),
            Map.of("building", "인문사회관", "lat",	36.63099512, "lon", 	127.4565283),
            Map.of("building", "사회과학대학 본관", "lat",	36.62939334, "lon", 	127.4578248),
            Map.of("building", "인문대학 본관", "lat",	36.63012198, "lon", 	127.4586251),
            Map.of("building", "미술관", "lat",	36.63073274, "lon", 	127.4572737),
            Map.of("building", "미술과", "lat",	36.63076411, "lon", 	127.4584903),
            Map.of("building", "개성재 관리동", "lat",	36.63153193, "lon", 	127.4575378),
            Map.of("building", "개성재 진리관", "lat",	36.63104797, "lon", 	127.4577853),
            Map.of("building", "개성재 정의관", "lat",	36.6312015, "lon", 	127.4581664),
            Map.of("building", "개성재 개척관", "lat",	36.63148193, "lon", 	127.4583537),
            Map.of("building", "계영원", "lat",	36.63187029, "lon", 	127.4585941),
            Map.of("building", "법학관", "lat",	36.63094825, "lon", 	127.4593456),
            Map.of("building", "역사관", "lat",	36.63054701, "lon", 	127.4598743),
            Map.of("building", "생활과학대학", "lat",	36.63041306, "lon", 	127.4607243),
            Map.of("building", "어린이집", "lat",	36.63072814, "lon", 	127.4603393),
            Map.of("building", "직장어린이집", "lat",	36.6303422, "lon", 	127.4608715),
            Map.of("building", "은하수식당", "lat",	36.6299154, "lon", 	127.4602179),
            Map.of("building", "사범대학 실험동", "lat",	36.62905034, "lon", 	127.4607788),
            Map.of("building", "사범대학 강의동", "lat",	36.62855848, "lon", 	127.4602705),
            Map.of("building", "개신문화관", "lat",	36.62819423, "lon", 	127.4593874),
            Map.of("building", "제1학생회관", "lat",	36.62758723, "lon", 	127.4588248),
            Map.of("building", "NH관", "lat",	36.62727179, "lon", 	127.4593059),
            Map.of("building", "CBNU스포츠센터", "lat",	36.62731552, "lon", 	127.4608165),
            Map.of("building", "보조체육관", "lat",	36.62687031, "lon", 	127.4624562),
            Map.of("building", "123 학군단", "lat",	36.62705336, "lon", 	127.4617284),
            Map.of("building", "의과대학", "lat",	36.62507738, "lon",	127.4609787),
            Map.of("building", "의과대학 2호관", "lat",	36.62564266, "lon",	127.4602085),
            Map.of("building", "공과대학 1호관", "lat",	36.6266955, "lon",	127.4582894),
            Map.of("building", "합동강의동", "lat",	36.62634063, "lon",	127.4577419),
            Map.of("building", "공과대학 2호관", "lat",	36.62604791, "lon",	127.4588738),
            Map.of("building", "제1공장동", "lat",	36.62539862, "lon",	127.4589996),
            Map.of("building", "제2공장동", "lat",	36.62503874, "lon",	127.4588499),
            Map.of("building", "공과대학 3호관", "lat",	36.6244608, "lon",	127.4584819),
            Map.of("building", "전자정보 1관", "lat",	36.62545492, "lon",	127.457949),
            Map.of("building", "공과대학 4호관", "lat",	36.62470165, "lon",	127.4593342),
            Map.of("building", "공과대학 5호관", "lat",	36.6240766, "lon",	127.4580802),
            Map.of("building", "양진재", "lat",	36.62420315, "lon",	127.4596141),
            Map.of("building", "학연산", "lat",	36.62512455, "lon",	127.4571556),
            Map.of("building", "전자정보 2관", "lat",	36.62484449, "lon",	127.457809),
            Map.of("building", "수의과학대학 및 동물의료센터", "lat",	36.62326526, "lon",	127.4560468),
            Map.of("building", "수의과학대학 2호관", "lat",	36.62348279, "lon",	127.4568949),
            Map.of("building", "실험동물연구지원센터", "lat",	36.62382037, "lon",	127.4560433),
            Map.of("building", "자연대 1호관", "lat",	36.63004547, "lon",	127.4550782),
            Map.of("building", "자연대 2호관", "lat",	36.62711026, "lon",	127.4567849),
            Map.of("building", "자연대 3호관", "lat",	36.62664674, "lon",	127.4568739),
            Map.of("building", "자연대 4호관", "lat",	36.62625482, "lon",	127.4566211),
            Map.of("building", "자연대 5호관", "lat",	36.62560153, "lon",	127.4559062),
            Map.of("building", "자연대 6호관", "lat",	36.62505036, "lon",	127.4558225),
            Map.of("building", "충북pro메이커센터", "lat",	36.626884, "lon",	127.4570407),
            Map.of("building", "정보화본부", "lat",	36.62632238, "lon",	127.4554499),
            Map.of("building", "전자정보3관", "lat",	36.62562154, "lon",	127.454444),
            Map.of("building", "나이팅게일관", "lat",	36.62519482, "lon",	127.4548071),
            Map.of("building", "목공실", "lat",	36.65304309, "lon",	127.4929162),
            Map.of("building", "박물관", "lat",	36.6276437, "lon",	127.4553816),
            Map.of("building", "제2학생회관", "lat",	36.62799025, "lon",	127.4543014),
            Map.of("building", "종양연구소", "lat",	36.62874226, "lon",	127.4518104),
            Map.of("building", "첨단바이오연구센터", "lat",	36.62895295, "lon",	127.4523304),
            Map.of("building", "의과대학 1호관", "lat",	36.6250178, "lon",	127.4610219),
            Map.of("building", "농업과학 기술센터", "lat",	36.62959438, "lon",	127.4514263),
            Map.of("building", "농업생명환경대학", "lat", 	36.62959438, "lon", 	127.4514263)
    );

    private static final int PERMISSION_REQUEST_CODE = 1000;

    public static TextView buildingView;
    public static TextView tempView;
    public static TextView co2View;
    public static TextView statusView;
    public static TextView humidityView;
    public static TextView operationView;

    private CampusMapCallback campusMapCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        instance = this;

        KakaoMapSdk.init(this, BuildConfig.API_KEY);
        mapView = findViewById(R.id.map_view);

        campusMapCallback = new CampusMapCallback(building_loc, null);
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

        BleScanService.getCampusStatus();

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
        instance.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // pause/resume 하지 말고, 기존 콜백 객체의 마커 업데이트 함수만 호출!
                if (instance.campusMapCallback != null) {
                    instance.campusMapCallback.updateMarkers(data);
                }
            }
        });
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
            buildingView.setText("선택한 건물: " + ((represent.getName() != null) ? represent.getName() : "-"));
            tempView.setText(String.valueOf(represent.getTemp()));
            co2View.setText(String.valueOf(represent.getCo2()));
            statusView.setText((represent.getStatus() != null) ? represent.getStatus() : "-");
            humidityView.setText(String.valueOf(data.getCampusHumidity()));
            operationView.setText((represent.getMsg() != null) ? represent.getMsg() : "-");
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
            buildingView.setText("선택한 건물: " + ((represent.getName() != null) ? represent.getName() : "-"));
            tempView.setText(String.valueOf(represent.getTemp()));
            co2View.setText(String.valueOf(represent.getCo2()));
            statusView.setText((represent.getStatus() != null) ? represent.getStatus() : "-");
            humidityView.setText(String.valueOf(humidity));
            operationView.setText((represent.getMsg() != null) ? represent.getMsg() : "-");
        }
    }

    /**
     * 🔐 안드로이드 버전별 필수 권한 팝업 요청
     */
    private void checkAndRequestRuntimePermissions() {
        if (!hasAllPermissions()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.BLUETOOTH_SCAN,
                        Manifest.permission.BLUETOOTH_CONNECT,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.POST_NOTIFICATIONS
                }, PERMISSION_REQUEST_CODE);
            }
            else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            return checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
        }
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
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