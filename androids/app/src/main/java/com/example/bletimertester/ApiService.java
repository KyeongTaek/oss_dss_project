package com.example.bletimertester;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {
    // A파트 외부 수업용 API 서버 데이터 업로드 엔드포인트
    @POST("api/sensor/upload")
    Call<ResponseBody> sendSensorData(@Body SensorData data);

    // B파트 캠퍼스 자체 분석 서버 리프레시 트리거 엔드포인트
    @POST("api/campus/refresh")
    Call<ResponseBody> triggerCampusRefresh();
}