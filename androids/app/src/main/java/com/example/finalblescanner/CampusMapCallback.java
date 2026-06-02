package com.example.finalblescanner;

import static com.example.finalblescanner.MainActivity.fillBottomSheet;

import android.graphics.Color;
import android.util.Log;
import android.widget.TextView;

import com.kakao.vectormap.KakaoMap;
import com.kakao.vectormap.LatLng;
import com.kakao.vectormap.KakaoMapReadyCallback;
import com.kakao.vectormap.MapLifeCycleCallback;
import com.kakao.vectormap.camera.CameraUpdateFactory;
import com.kakao.vectormap.label.Label;
import com.kakao.vectormap.label.LabelLayer;
import com.kakao.vectormap.label.LabelOptions;
import com.kakao.vectormap.label.LabelStyle;
import com.kakao.vectormap.label.LabelStyles;
import com.kakao.vectormap.label.LabelTextBuilder;

import java.util.List;
import java.util.Map;

public class CampusMapCallback {
    List<Map<String, Object>> locations;
    ServerDataResponse.CampusData responses;
    private KakaoMap kakaoMap;
    private ServerDataResponse.CampusData pendingData = null;

    public CampusMapCallback(List<Map<String, Object>> loc, ServerDataResponse.CampusData sensorResponse) {
        locations = loc;
        responses = sensorResponse;
    }
    public final MapLifeCycleCallback lifeCycleCallback = new MapLifeCycleCallback() {
        @Override
        public void onMapDestroy() {
            // 지도 API 가 정상적으로 종료될 때 호출됨
            kakaoMap = null;
        }

        @Override
        public void onMapError(Exception error) {
            // 인증 실패 및 지도 사용 중 에러가 발생할 때 호출됨
        }
    };

    public final KakaoMapReadyCallback readyCallback = new KakaoMapReadyCallback() {
        @Override
        public void onMapReady(KakaoMap mapInstance) {
            kakaoMap = mapInstance;
            if (pendingData != null) {
                Log.d("CampusMapCallback", "대기 중이던 최신 서버 데이터로 마커 그림");
                responses = pendingData;
                drawMarkers(pendingData);
                pendingData = null;
            }
            else if (responses != null) {
                drawMarkers(responses);
            }

            kakaoMap.setOnLabelClickListener(new KakaoMap.OnLabelClickListener() {
                @Override
                public boolean onLabelClicked(KakaoMap kakaoMap, LabelLayer layer, Label label) {
                    // 마커가 클릭되었을 때 실행할 코드 작성
                    LatLng labelPosition= label.getPosition();
                    double lat = labelPosition.getLatitude();
                    double lon = labelPosition.getLongitude();

                    int key = 0;
                    for (Map<String, Object> location : locations) {
                        if((double)location.get("lat") == lat && (double)location.get("lon") == lon) {
                            break;
                        }
                        else {
                            key = key + 1;
                        }
                    }

                    String building_name = locations.get(key).get("building").toString();

                    key = 0;
                    for (ServerDataResponse.CampusData.Building b : responses.getBuildings()) {
                        if (building_name.equals(b.getName())) {
                            break;
                        }
                        else {
                            key = key + 1;
                        }
                    }
                    ServerDataResponse.CampusData.Building building = responses.getBuildings().get(key);
                    fillBottomSheet(building, responses.getCampusHumidity());

                    return true; // 이벤트를 소비했으므로 true 리턴
                }
            });
        }

        @Override
        public LatLng getPosition() {
            // 지도 시작 시 위치 좌표를 설정
            return LatLng.from(36.629000, 127.457000);
        }
    };

    public void drawMarkers(ServerDataResponse.CampusData data) {
        // 인증 후 API 가 정상적으로 실행될 때 호출됨

        if (kakaoMap == null) return;

        LabelStyles styles = LabelStyles.from("myStyleId",
                LabelStyle.from(R.drawable.red_marker).setZoomLevel(8),
                LabelStyle.from(R.drawable.blue_marker).setZoomLevel(11),
                LabelStyle.from(R.drawable.blue_marker).setTextStyles(32, Color.BLACK, 1, Color.GRAY).setZoomLevel(15));

        styles = kakaoMap.getLabelManager().addLabelStyles(styles);
        LabelStyles orange_style = LabelStyles.from("orange_style_id",
                LabelStyle.from(R.drawable.orange_marker).setTextStyles(32, Color.BLACK, 1, Color.GRAY));
        LabelStyles green_style = LabelStyles.from("green_style_id",
                LabelStyle.from(R.drawable.green_marker).setTextStyles(32, Color.BLACK, 1, Color.GRAY));
        LabelStyles blue_style = LabelStyles.from("blue_style_id",
                LabelStyle.from(R.drawable.blue_marker).setTextStyles(32, Color.BLACK, 1, Color.GRAY));
        LabelStyles gray_style = LabelStyles.from("gray_style_id",
                LabelStyle.from(R.drawable.gray_marker).setTextStyles(32, Color.BLACK, 1, Color.GRAY));

        for (Map<String, Object> location : locations) {
            int key = 0;
            int max = responses.getBuildings().size();
            for (ServerDataResponse.CampusData.Building b : responses.getBuildings()) {
                if ((location.get("building")).toString().equals(b.getName())) {
                    break;
                }
                else {
                    key = key + 1;
                }
            }

            if(key == max) {
                styles = kakaoMap.getLabelManager().addLabelStyles(gray_style);
            }
            else {
                String operating_status = responses.getBuildings().get(key).getStatus();
                if (operating_status != null) {
                    switch (operating_status) {
                        case "COOLING_REQUIRED":
                            styles = kakaoMap.getLabelManager().addLabelStyles(orange_style);
                            break;
                        case "POWER_SAVING":
                            styles = kakaoMap.getLabelManager().addLabelStyles(green_style);
                            break;
                        case "HEATING_REQUIRED":
                            styles = kakaoMap.getLabelManager().addLabelStyles(blue_style);
                            break;
                        default:
                            styles = kakaoMap.getLabelManager().addLabelStyles(gray_style);
                            break;
                    }
                }
                else {
                    styles = kakaoMap.getLabelManager().addLabelStyles(gray_style);
                }
            }


            kakaoMap.getLabelManager().getLayer().addLabel(LabelOptions.from(setPosition((Double)location.get("lat"), (Double)location.get("lon")))
                    .setStyles(styles));
        }
    }


    public void updateMarkers(ServerDataResponse.CampusData data) {
        if (this.kakaoMap == null) {
            Log.w("CampusmapCallback", "지도 아직 로딩 중. 도착 데이터는 임시 보관");
            this.pendingData = data;

            return; // 아직 지도가 안 켜졌으면 패스
        }

        this.responses = data;

        // 1. 기존에 지도에 그려져 있던 마커(라벨)를 싹 지웁니다.
        LabelLayer layer = kakaoMap.getLabelManager().getLayer();
        if (layer != null) {
            layer.removeAll();
        }

        // 2. 서버에서 받아온 새 데이터로 마커를 다시 그립니다.
        drawMarkers(data);
    }

    public LatLng setPosition(double lat, double lon) {
        return LatLng.from(lat, lon);
    }
}
