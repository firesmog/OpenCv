package com.readboy.util;

import android.content.Context;
import android.os.Build;
import android.widget.Toast;

public class ShowToastUtils {

    private static Toast mToast;

    public static void showToast(Context mContext, String text, int duration) {
        //Android9.0系统已处理，没有该问题，Android10.0又改回9.0以前的实现
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.P || mToast == null) {
            mToast = Toast.makeText(mContext, text, duration);
        } else {
            mToast.setText(text);
            mToast.setDuration(duration);
        }
        mToast.show();
    }

    public static void showToast(Context mContext, int resId, int duration) {
        showToast(mContext, mContext.getResources().getString(resId), duration);
    }
}