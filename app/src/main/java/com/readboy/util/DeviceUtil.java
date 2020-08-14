package com.readboy.util;

import android.content.Context;
import android.text.TextUtils;
import android.util.DisplayMetrics;

import com.readboy.bean.newexam.ExamBean;
import com.readboy.log.LogUtils;
import com.readboy.myopencvcamera.R;

import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.netty.util.internal.StringUtil;

public class DeviceUtil {
    public static ExamBean getExamData(Context context){
        ExamBean data = null;
        try {
            String question = context.getResources().getString(R.string.json_new_data_e);
            question = replaceBlank(question);
            question = question.replace("\\", "");
            question = question.replace("\n", "");
            question = question.replace("/", "");
            data = GsonUtil.gsonToBean(question, ExamBean.class);
        }catch (Exception e){
            LogUtils.e("error == " + e.getMessage());
        }
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

    public static String getResultFromContent(String content){
        if(TextUtils.isEmpty(content)){
            return null;
        }
        Stack<Character> stack = new Stack<Character>();
        StringBuilder result = new StringBuilder();
        for(int i=0; i<content.length(); i++){
            char c = content.charAt(i);
            if('(' == c){
                stack.push(c);
            }else if(')' == c && !stack.empty()){
                stack.pop();
                result.append("￥&#&#@");
            }else if(!stack.isEmpty()){
                result.append(c);
            }
        }
        return result.toString();
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

}
