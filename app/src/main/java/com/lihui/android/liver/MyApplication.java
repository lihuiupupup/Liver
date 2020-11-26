package com.lihui.android.liver;

import android.app.Application;

import com.umeng.analytics.MobclickAgent;
import com.umeng.commonsdk.UMConfigure;

public class MyApplication extends Application {

    public static volatile long timeStamp;

    public static String UM_KEY ="5fbf93acd2a26c6a571fbd2a";

    private static MyApplication myApplication;
    @Override
    public void onCreate() {
        super.onCreate();
        myApplication = this;
        UMConfigure.init(this, UM_KEY, "default", UMConfigure.DEVICE_TYPE_PHONE, "");
        MobclickAgent.setPageCollectionMode(MobclickAgent.PageMode.AUTO);
    }

    public static MyApplication getInstance() {
        return myApplication;
    }
}
