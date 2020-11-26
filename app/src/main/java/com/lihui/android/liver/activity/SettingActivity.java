package com.lihui.android.liver.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.lihui.android.liver.R;

public class SettingActivity extends BaseActivity {

    private RadioGroup rg;
    private RadioButton rb0,rb1,rb2;
    private RadioButton lastId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        rg = findViewById(R.id.rg);
        rb0 = findViewById(R.id.rb0);
        rb1= findViewById(R.id.rb1);
        rb2= findViewById(R.id.rb2);
        if (PushActivity2.PUSH_WIDTH == 480) {
            rg.check(rb0.getId());

        } else if (PushActivity2.PUSH_WIDTH == 720) {
            rg.check(rb1.getId());
        } else {
            rg.check(rb2.getId());
        }

        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                //lastId.setSelected(false);
                switchPreviewSize(checkedId);

            }
        });
    }

    private void switchPreviewSize(int checkedId) {
        if (checkedId == R.id.rb0) {
            PushActivity2.PUSH_HEIGHT = 640;
            PushActivity2.PUSH_WIDTH = 480;
            PushActivity2.BIT_RATE = 600000;
        } else if (checkedId == R.id.rb1) {
            PushActivity2.PUSH_HEIGHT = 1280;
            PushActivity2.PUSH_WIDTH = 720;
            PushActivity2.BIT_RATE = 1000000;
        } else {
            PushActivity2.PUSH_HEIGHT = 1920;
            PushActivity2.PUSH_WIDTH = 1080;
            PushActivity2.BIT_RATE = 1500000;
        }
    }
}