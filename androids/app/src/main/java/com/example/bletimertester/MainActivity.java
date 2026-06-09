package com.example.bletimertester;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;

public class MainActivity extends Activity {

    private static final int PERMISSION_REQUEST_CODE = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 💡 UI 팀원이 만든 화면 레이아웃을 나중에 여기 연결하면 됩니다!
        // 예: setContentView(R.layout.activity_main);

        // 🚀 앱 켜지자마자 권한 체크 및 팝업창 띄우기
        checkAndRequestRuntimePermissions();

        // ⏰ 권한과 상관없이 기존처럼 백그라운드 서비스(10초 타이머)는 즉시 시동
        Context context = MainActivity.this;
        BleScanService.initialize(context);
        Intent serviceIntent = new Intent(context, BleScanService.class);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent);
        } else {
            context.startService(serviceIntent);
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
}