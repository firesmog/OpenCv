package com.readboy.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.Log;

import androidx.exifinterface.media.ExifInterface;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PhotoUtil {

    public static Bitmap correctAndSetImage(ExifInterface exifInterface, Bitmap bitmap1) {
        int degree = 0;
        Bitmap bitmap = bitmap1;
        if (exifInterface != null) {
            degree = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0);
            Log.d("TAG", "degree before 11== " + degree);


            switch (degree) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
                default:
                    degree = 0;
                    break;
            }


            Log.d("TAG", "degree after == " + degree );

            if (degree != 0) {
                Matrix matrix = new Matrix();
                matrix.postRotate(degree);
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            }
        }
        return bitmap;

    }

    public static boolean isContainChinese(String str) {
        Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
        Matcher m = p.matcher(str);
        if (m.find()) {
            return true;
        }
        return false;
    }

    /**
     * 该函数判断一个字符串是否包含标点符号（中文英文标点符号）。
     * 原理是原字符串做一次清洗，清洗掉所有标点符号。
     * 此时，如果原字符串包含标点符号，那么清洗后的长度和原字符串长度不同。返回true。
     * 如果原字符串未包含标点符号，则清洗后长度不变。返回false。
     * @param s
     * @return
     */
    public static boolean check(String s) {
        boolean b = false;

        String tmp = s;
        tmp = tmp.replaceAll("\\p{P}", "");
        if (s.length() != tmp.length()) {
            b = true;
        }

        return b;
    }

    /** 判断字符串中是否包含数字 **/
    public static boolean isContainsNum(String content) {
        boolean flag = false;
        Pattern p = Pattern.compile(".*\\d+.*");
        Matcher m = p.matcher(content);
        if (m.matches()) {
            flag = true;
        }
        return flag;
    }

    public static Bitmap getBitmapFromUrl(String url, float scaleWidth, float scaleHeight ) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true; // 设置了此属性一定要记得将值设置为false
        Bitmap bitmap = BitmapFactory.decodeFile(url);
        // 防止OOM发生
        options.inJustDecodeBounds = false;
        int mWidth = bitmap.getWidth();
        int mHeight = bitmap.getHeight();
        Matrix matrix = new Matrix();
        // 按照固定大小对图片进行缩放
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap newBitmap = Bitmap.createBitmap(bitmap, 0, 0, mWidth, mHeight, matrix, true);
        // 用完了记得回收
        bitmap.recycle();
        return newBitmap;
    }

    /**
     * 判断一个字符串是否为字母
     * @param fstrData
     * @return
     */
    public static boolean checkEnglish(String fstrData) {
        char c = fstrData.charAt(0);
        if (((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z'))) {
            return true;
        } else {
            return false;
        }
    }
}
