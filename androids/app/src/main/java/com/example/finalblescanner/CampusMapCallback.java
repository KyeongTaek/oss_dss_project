package com.example.finalblescanner;

import android.graphics.Color;
import android.util.Log;
import android.widget.TextView;

import com.kakao.vectormap.KakaoMap;
import com.kakao.vectormap.LatLng;
import com.kakao.vectormap.KakaoMapReadyCallback;
import com.kakao.vectormap.MapLifeCycleCallback;
import com.kakao.vectormap.label.Label;
import com.kakao.vectormap.label.LabelOptions;
import com.kakao.vectormap.label.LabelStyle;
import com.kakao.vectormap.label.LabelStyles;
import com.kakao.vectormap.label.LabelTextBuilder;

import java.util.List;
import java.util.Map;

public class CampusMapCallback {
    List<Map<String, Object>> locations;
    Map<String, Object> responses;

    public CampusMapCallback(List<Map<String, Object>> loc, Map<String, Object> fakeSensorResponse) {
        locations = loc;
        responses = fakeSensorResponse;
    }
    public final MapLifeCycleCallback lifeCycleCallback = new MapLifeCycleCallback() {
        @Override
        public void onMapDestroy() {
            // 지도 API 가 정상적으로 종료될 때 호출됨
        }

        @Override
        public void onMapError(Exception error) {
            // 인증 실패 및 지도 사용 중 에러가 발생할 때 호출됨
        }
    };

    public final KakaoMapReadyCallback readyCallback = new KakaoMapReadyCallback() {
        @Override
        public void onMapReady(KakaoMap kakaoMap) {
            // 인증 후 API 가 정상적으로 실행될 때 호출됨
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
                Map<String, Object> building_data = (Map)responses.get("data");
                int key = 0;
                for (Map<String, Object> m : (List<Map<String, Object>>)building_data.get("buildings")) {
                    if ((location.get("building")).toString().equals(m.get("building_name"))) {
                        break;
                    }
                    else {
                        key = key + 1;
                    }
                }

                String operating_status = ((Map)((List)building_data.get("buildings")).get(key)).get("operating_status").toString();
                switch (operating_status) {
                    case "COOLING_REQUIRED":
                        styles = kakaoMap.getLabelManager().addLabelStyles(orange_style);
                        break;
                    case "POWER_SAVING_REQUIRED":
                        styles = kakaoMap.getLabelManager().addLabelStyles(green_style);
                        break;
                    case "HEATING_REQUIRED":
                        styles = kakaoMap.getLabelManager().addLabelStyles(blue_style);
                        break;
                    default:
                        styles = kakaoMap.getLabelManager().addLabelStyles(gray_style);
                        break;
                }

                Label label = kakaoMap.getLabelManager().getLayer().addLabel(LabelOptions.from(setPosition((Double)location.get("lat"), (Double)location.get("lon")))
                        .setStyles(styles));
            }
        }

        @Override
        public LatLng getPosition() {
            // 지도 시작 시 위치 좌표를 설정
            return LatLng.from(36.629000, 127.457000);
        }
        public LatLng setPosition(double lat, double lon) {
            return LatLng.from(lat, lon);
        }
    };
}
