package com.readboy.net.service;

import android.util.Log;

import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class ManagerProxy {

    private Retrofit client;

    private ManagerProxy() {
        client = new Retrofit.Builder()
                .baseUrl("Base_Url_Test")
                .client(initClient())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    private static volatile AppApi INSTANCE;

    public static AppApi getInstance() {
        if (INSTANCE == null) {
            synchronized (ManagerProxy.class) {
                if (INSTANCE == null) {
                    INSTANCE = new ManagerProxy().getAppApi();
                }
            }
        }
        return INSTANCE;
    }

    private AppApi getAppApi() {
        return client.create(AppApi.class);
    }

    private static OkHttpClient initClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        //声明日志类
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
            @Override
            public void log(String message) {
                Log.d("initClient == ",  message);
            }
        });
        //设定日志级别
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        //延时
        builder.addInterceptor(httpLoggingInterceptor)
                //设置参数加密
                //.addInterceptor(new EncryptParamInterceptor())
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS);
        return builder.build();
    }

}
