package com.example.bletimertester;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // [수정] XML 파일 에러를 방지하기 위해 화면 설정 코드를 주석 처리합니다.
        // 어차피 백그라운드 서비스 구동 테스트이므로 화면이 없어도 타이머는 완벽히 돕니다!
        // setContentView(R.layout.activity_main);

        // 10초 타이머 독립 서비스 엔진 시동!
        Context context = MainActivity.this;
        Intent serviceIntent = new Intent(context, BleScanService.class);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent);
        } else {
            context.startService(serviceIntent);
        }
    }
}