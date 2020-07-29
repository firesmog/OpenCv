package com.readboy.net.service;


/**
 *
 */

public interface AppApi {

    /**
     * 获取验证码
     *//*
    @FormUrlEncoded
    @POST("user/getCode")
    Observable<MessageResponse> getMessageConfirmCode(@Field("mobile") String number);

    *//**
     * 设置守护等级
     *//*
    @FormUrlEncoded
    @POST("child/setGuardLevel")
    Observable<BaseResponse<Object>> setChildGuardLevel(@Field("childId") String childId, @Field("level") String level);

    *//**
     * 设置儿童锁
     *//*
    @FormUrlEncoded
    @POST("child/setChildLock")
    Observable<BaseResponse<Object>> setChildGuardLock(@Field("childId") String childId, @Field("childLock") String childLock);

    *//**
     * 设置设备唯一标识
     *//*
    @FormUrlEncoded
    @POST("child/updateDeviceInfo")
    Observable<MessageResponse> setDeviceInfo(@Field("childId") String childId, @Field("deviceInfo") String deviceInfo);*/



}
