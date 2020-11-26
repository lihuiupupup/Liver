package com.lihui.android.liver.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SPUtil {

    public static final String PUSH_URL = "pushUrl";
    public static final String PULL_URL = "pullUrl";

    public static SharedPreferences getSp(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("liver",Context.MODE_PRIVATE);
        return sharedPreferences;
    }
}
