package com.readboy.util;

import android.os.Build;
import android.os.Environment;

import com.readboy.log.LogUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Properties;

public class PhoneTypeUtil {
    public static final String SYS_EMUI = "sys_emui";
    public static final String SYS_MIUI = "sys_miui";
    public static final String SYS_FLYME = "sys_flyme";
    private static final String KEY_MIUI_VERSION_CODE = "ro.miui.ui.version.code";
    private static final String KEY_MIUI_VERSION_NAME = "ro.miui.ui.version.name";
    private static final String KEY_MIUI_INTERNAL_STORAGE = "ro.miui.internal.storage";
    private static final String KEY_EMUI_API_LEVEL = "ro.build.hw_emui_api_level";
    private static final String KEY_EMUI_VERSION = "ro.build.version.emui";
    private static final String KEY_EMUI_CONFIG_HW_SYS_VERSION = "ro.confg.hw_systemversion";

    public static String getSystem(){
        String manufacturer = Build.MANUFACTURER;
        if (manufacturer != null && manufacturer.length()>0){
            String phone_type = manufacturer.toLowerCase();
            LogUtils.i("initView: " + phone_type);
            switch (phone_type){
                case "huawei":
                    return SYS_EMUI;
                case "xiaomi":
                    return SYS_MIUI;
            }
        }
        return manufacturer;
    }
}
