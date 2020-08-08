package com.readboy.util;

import android.content.Context;
import android.text.TextUtils;
import android.util.DisplayMetrics;

import com.readboy.bean.newexam.ExamBean;
import com.readboy.log.LogUtils;
import com.readboy.myopencvcamera.R;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.netty.util.internal.StringUtil;

public class DeviceUtil {
    public static ExamBean getExamData(Context context){
        String question = context.getResources().getString(R.string.json_new_data_c);
        question = replaceBlank(question);
        question = question.replace("\\", "");
        question = question.replace("\n", "");
        ExamBean data = GsonUtil.gsonToBean(question, ExamBean.class);
        return data;
    }

    private static String replaceBlank(String str) {
        String dest = "";
        if (str!=null) {
            Pattern p = Pattern.compile("\\s*|\t|\r|\n");
            Matcher m = p.matcher(str);
            dest = m.replaceAll("");
        }
        return dest;
    }

    public static String getResultFromContent(String gSQL){
        String quStr=gSQL.substring(gSQL.indexOf("(")+1,gSQL.indexOf("(")+2);
        if(TextUtils.isEmpty(quStr)){
            quStr = gSQL.substring(gSQL.indexOf(")")-1,gSQL.indexOf(")"));
        }
        return quStr;
    }
}
