package com.lihui.android.liver.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.lihui.android.liver.MyApplication;
import com.lihui.android.liver.R;
import com.lihui.android.liver.filter.EGLUtil;
import com.lihui.android.liversdk.LiverSdkManager;

public class MainActivity extends BaseActivity implements View.OnClickListener {

    private ImageView ivPush,ivPull,ivSetting;

    public static String beautyFragmentCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ivPush = findViewById(R.id.iv_push);
        ivPull = findViewById(R.id.iv_pull);
        ivSetting = findViewById(R.id.iv_setting);
        ivPush.setOnClickListener(this);
        ivPull.setOnClickListener(this);
        ivSetting.setOnClickListener(this);
        LiverSdkManager.getLiverSdkManager().init(this);
        beautyFragmentCode = EGLUtil.readTextFromRawResource(MyApplication.getInstance(),R.raw.fragment_shader_beauty);

        requestPermission();
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this,new String[] {
                Manifest.permission.CAMERA,Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.INTERNET,
                Manifest.permission.RECORD_AUDIO
        },100);
    }

    @Override
    public void onClick(View v) {
        if (!hasP) {
            requestPermission();
            return;
        }
        switch (v.getId()) {
            case R.id.iv_push:
                startActivity(new Intent(this,PushActivity2.class));
                break;
            case R.id.iv_pull:
                startActivity(new Intent(this,PullActivity.class));
                break;
            case R.id.iv_setting:
                startActivity(new Intent(this,SettingActivity.class));
                break;
        }
    }

    private boolean hasP = false;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            for (int i = 0 ;i < grantResults.length;i++) {
                if (grantResults[i] != 0) {
                    hasP = false;
                    return;
                }
            }
            hasP = true;
        }
    }
}