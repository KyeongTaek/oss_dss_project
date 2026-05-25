package com.example.finalblescanner;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.kakao.vectormap.KakaoMapSdk;
import com.kakao.vectormap.MapView;

import android.util.Log;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    public MapView mapView;

    List<Map<String, Object>> building_loc = List.of(
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
    Map<String, Object> fakeSensorResponse = Map.of(
            "statusCode", 200,
            "message", "request success",
            "data", Map.of(
                    "campus_humidity", 44.5,
                    "campus_aqi", 2,
                    "buildings", List.of(
                            Map.of(
                                    "building_name", "CBNU스포츠센터",
                                    "building_ext_temp", 20.6,
                                    "building_ext_co2", 600,
                                    "operating_status", "POWER_SAVING_REQUIRED",
                                    "recommendation_msg", "현재 냉난방이 필요 없는 최적의 외부 기후입니다. 적극적인 자연환기를 전개하고, 모든 공조 설비를 절전/송풍 모드로 전환하여 에너지를 절약하세요."
                            ),
                            Map.of(
                                    "building_name", "중앙도서관",
                                    "building_ext_temp", 26.5,
                                    "building_ext_co2", 480,
                                    "operating_status", "COOLING_REQUIRED",
                                    "recommendation_msg", "환기하기 좋은 날씨네요. 그러나 외기 온도가 매우 가파르게 상승하고 있어, 창문을 열기보다는 냉방을 유지하는 것이 에너지를 절약하는 길입니다."
                            )
                    )
            )
    );

    public TextView buildingView;
    public TextView tempView;
    public TextView co2View;
    public TextView statusView;
    public TextView ventilationView;
    public TextView tagView;
    public TextView operationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        KakaoMapSdk.init(this, BuildConfig.API_KEY);
        mapView = findViewById(R.id.map_view);

        CampusMapCallback campusMapCallback = new CampusMapCallback(building_loc, fakeSensorResponse);
        mapView.start(campusMapCallback.lifeCycleCallback, campusMapCallback.readyCallback);

        // onCreate 내부 또는 적절한 위치에 추가
        String keyHash = KakaoMapSdk.INSTANCE.getHashKey();
        Log.d("KakaoKeyHash", "내 키 해시값: " + keyHash);

//        Map<String, Object> building_data = (Map)fakeSensorResponse.get("data");
//        buildingView = findViewById(R.id.buildingName_TextView);
//        buildingView.setText(((Map)((List)building_data.get("buildings")).get(0)).get("building_name").toString());
//        tempView = findViewById(R.id.temp_TextView);
//        tempView.setText(((Map)((List)building_data.get("buildings")).get(0)).get("building_ext_temp").toString());
//        co2View = findViewById(R.id.co2_TextView);
//        co2View.setText(((Map)((List)building_data.get("buildings")).get(0)).get("building_ext_co2").toString());
//        statusView = findViewById(R.id.status_TextView);
//        statusView.setText(((Map)((List)building_data.get("buildings")).get(0)).get("operating_status").toString());
//        ventilationView = findViewById(R.id.ventilation_TextView);
//        ventilationView.setText(building_data.get("campus_humidity").toString());
//        operationView = findViewById(R.id.operation_TextView);
//        operationView.setText(((Map)((List)building_data.get("buildings")).get(0)).get("recommendation_msg").toString());


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
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