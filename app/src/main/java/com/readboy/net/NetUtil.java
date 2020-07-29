package com.readboy.net;

import android.net.ParseException;
import android.util.Log;


import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public class NetUtil {
    private static final String TEST_APPID = "5eec82a0";
    // 接口密钥(webapi类型应用开通手写文字识别后，控制台--我的应用---手写文字识别---相应服务的apikey)
    private static final String TEST_API_KEY = "d99ddabfb1b58da0b92a2acca93f67fa";
    public static final String WEBOCR_URL = "http://webapi.xfyun.cn/v1/service/v1/ocr/handwriting";

    /**
     * 组装http请求头
     *
     * @param
     * @return
     * @throws UnsupportedEncodingException
     * @throws ParseException
     */
    public static Map<String, String> constructHeader(String language, String location) throws UnsupportedEncodingException, ParseException {
        // 系统当前时间戳
        String X_CurTime = System.currentTimeMillis() / 1000L + "";
        // 业务参数
        String param = "{\"language\":\""+language+"\""+",\"location\":\"" + location + "\"}";
        String X_Param = new String(Base64.encodeBase64(param.getBytes("UTF-8")));
        // 接口密钥
        String apiKey = TEST_API_KEY;
        // 讯飞开放平台应用ID
        String X_Appid = TEST_APPID;
        // 生成令牌
        String X_CheckSum = getMd5(apiKey + X_CurTime + X_Param);
        // 组装请求头
        Map<String, String> header = new HashMap<String, String>();
        header.put("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
        header.put("X-Param", X_Param);
        header.put("X-CurTime", X_CurTime);
        header.put("X-CheckSum", X_CheckSum);
        header.put("X-Appid", X_Appid);
        return header;
    }

    private static String getMd5(String code){
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            Log.d("TAG","get md5 instance error");
            return null;
        }

        byte[] signature = md5.digest((code).getBytes());
        return new String(Hex.encodeHex( signature) );
    }
}
