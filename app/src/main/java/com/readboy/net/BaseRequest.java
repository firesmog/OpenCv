package com.readboy.net;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class BaseRequest {

    public static final String DOMAIN = "https://api.ebag-test.readboy.com/exam-omr/";
    public static final String PREFIX = "v1/";
    public static final String API_TOKEN = "CFIsGgvkonYEoVURomNZCk1HwshSQhDw";

    public BaseRequest() {
    }

    public static RequestInterface getServer() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(DOMAIN)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
        RequestInterface request = retrofit.create(RequestInterface.class);

        return request;
    }
}
