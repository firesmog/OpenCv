package com.readboy.net;


import com.readboy.net.bean.ExamResponse;

import io.reactivex.Observable;
import okhttp3.MultipartBody;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface RequestInterface {
    // 获取课程

    @Multipart
    @POST("pad/answer_card")
    Observable<ExamResponse> getScore(@Part MultipartBody.Part imgs);


}
