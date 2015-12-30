package com.ssiot.donghai;

import android.app.Application;
import android.util.Log;

//http://blog.csdn.net/hyx1990/article/details/7584789  在任意位置获取应用程序Context
public class ContextUtilApp extends Application {
    private static final String tag = "ContextUtilApp";
    private static ContextUtilApp instance;

    public static ContextUtilApp getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        Log.v(tag, "----app create----");
        super.onCreate();
        instance = this;
    }
}