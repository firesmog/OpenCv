package com.readboy.log;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Environment;
import android.util.Log;


import com.readboy.myopencvcamera.BuildConfig;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 *
 * 日志封装
 */

public class LogUtils {
    static String className;//类名
    static String methodName;//方法名
    static int lineNumber;//行数
    private static String tag;
    private static LogConfig mLogConfig;


    @SuppressLint("SdCardPath")
    private static String MYLOG_PATH_SDCARD_DIR = "/sdcard/childguard/log";// 日志文件在sdcard中的路径
    private static int SDCARD_LOG_FILE_SAVE_DAYS = 0;// sd卡中日志文件的最多保存天数
    private static String MYLOGFILEName = "Log.txt";// 本类输出的日志文件名称
    private static SimpleDateFormat myLogSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// 日志的输出格式
    private static SimpleDateFormat logfile = new SimpleDateFormat("yyyy-MM-dd");// 日志文件格式
    public Context context;

    /**
     * 判断是否可以调试
     * @return
     */
    public static boolean isDebuggable() {
        return BuildConfig.DEBUG;
    }

    private static String createLog(String log ) {
        return methodName +
                "(" + className + ":" + lineNumber + "):" +
                log;
    }

    private static String getFileMessage(String type,String msg){
        return DateUtil.getDate(DateUtil.SIMPLE_FORMAT_1)
                +" "
                +DateUtil.getDate(DateUtil.SIMPLE_FORMAT_2)
                +" "
                +type
                +"："
                +msg
                +"\t\n"
                ;
    }

    /**
     * 设置配置文件
     */
    public static void setLogConfig(LogConfig logConfig){
        if(logConfig==null){
            return;
        }
        mLogConfig = logConfig;
        if(logConfig.isSaveFile() && isExistsSdcard()){
            LogFile.getInstance(mLogConfig).deleteLogFile();
        }
    }

    private static boolean isExistsSdcard(){
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    /**
     * 获取文件名、方法名、所在行数
     * @param sElements
     */
    private static void getMethodNames(StackTraceElement[] sElements){
        tag = " TAG_" + sElements[1].getFileName();
        className = sElements[1].getFileName();
        methodName = sElements[1].getMethodName();
        lineNumber = sElements[1].getLineNumber();
    }

    public static void e(String message){
        getMethodNames(new Throwable().getStackTrace());
        Log.e(tag, createLog(message));
    }

    public static void i(String message){
        getMethodNames(new Throwable().getStackTrace());
        Log.i(tag, createLog(message));
    }

    public static void d(String message){
        getMethodNames(new Throwable().getStackTrace());
        String messageReal = createLog(message);
        Log.d(tag, messageReal);
        LogFile.getInstance(mLogConfig).writeToFile(getFileMessage(tag,messageReal));
    }

    public static void v(String message){
        getMethodNames(new Throwable().getStackTrace());
        Log.v(tag, createLog(message));
    }

    public static void w(String message){
        getMethodNames(new Throwable().getStackTrace());
        Log.w(tag, createLog(message));
    }




    /**
     * 打开日志文件并写入日志
     * @param mylogtype
     * @param tag
     * @param text
     */
    private static void writeLogtoFile(String mylogtype, String tag, String text) {// 新建或打开日志文件
        Date nowtime = new Date();
        String needWriteFiel = logfile.format(nowtime);
        String needWriteMessage = myLogSdf.format(nowtime) + "    " + mylogtype + "    " + tag + "    " + text;
        File dirPath = Environment.getExternalStorageDirectory();

        File dirsFile = new File(MYLOG_PATH_SDCARD_DIR);
        if (!dirsFile.exists()){
            dirsFile.mkdirs();
        }
        //Log.i("创建文件","创建文件");
        File file = new File(dirsFile.toString(), needWriteFiel + MYLOGFILEName);// MYLOG_PATH_SDCARD_DIR
        if (!file.exists()) {
            try {
                //在指定的文件夹中创建文件
                file.createNewFile();
            } catch (Exception e) {
                LogUtils.d(e.getMessage());
            }
        }

        try {
            FileWriter filerWriter = new FileWriter(file, true);// 后面这个参数代表是不是要接上文件中原来的数据，不进行覆盖
            BufferedWriter bufWriter = new BufferedWriter(filerWriter);
            bufWriter.write(needWriteMessage);
            bufWriter.newLine();
            bufWriter.close();
            filerWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除制定的日志文件
     */
    public static void delFile() {// 删除日志文件
        String needDelFiel = logfile.format(getDateBefore());
        File dirPath = Environment.getExternalStorageDirectory();
        File file = new File(dirPath, needDelFiel + MYLOGFILEName);// MYLOG_PATH_SDCARD_DIR
        if (file.exists()) {
            file.delete();
        }
    }

    /**
     * 得到现在时间前的几天日期，用来得到需要删除的日志文件名
     */
    private static Date getDateBefore() {
        Date nowtime = new Date();
        Calendar now = Calendar.getInstance();
        now.setTime(nowtime);
        now.set(Calendar.DATE, now.get(Calendar.DATE) - SDCARD_LOG_FILE_SAVE_DAYS);
        return now.getTime();
    }
}