package com.example.finalblescanner;

import com.kakao.vectormap.KakaoMap;
import com.kakao.vectormap.LatLng;
import com.kakao.vectormap.KakaoMapReadyCallback;
import com.kakao.vectormap.MapLifeCycleCallback;
public class CampusMapCallback {
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
        }

        @Override
        public LatLng getPosition() {
            // 지도 시작 시 위치 좌표를 설정
            return LatLng.from(36.629000, 127.457000);
        }
    };
}
