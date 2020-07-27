package com.readboy;

import android.app.Application;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;

import com.readboy.log.LogConfig;

import java.util.Objects;

public class DemoApplication  extends Application {

    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        initLog();
    }

    public static Context getInstance() {
        return mContext;
    }

    private void initLog() {
        new LogConfig.Builder()
                //设置日志打印开关 默认为true
                .setLog(true)
                //统一设置日志TAG 默认为LOG_TAG
                .setTAG("GUARD_TAG")
                //设置日志是否保存到文件 默认为false
                .setSaveFile(true)
                //设置日志保存路径，在设置setSaveFile为true的情况下必须设置该路径
                .setLogPath(Objects.requireNonNull(getExternalFilesDir(null)).getPath())
                //设置日志名称 默认为当天的日期如2017-11-13，设置后变为log_2017-11-13
                .setLogFileName("log")
                //设置日志在客户端最大保存天数 默认为7天
                .setMaxSaveDay(7)
                .build();
    }
}
