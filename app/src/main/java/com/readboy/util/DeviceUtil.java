package com.readboy.util;

import android.content.Context;
import android.graphics.RectF;
import android.text.TextUtils;
import android.util.DisplayMetrics;

import com.readboy.bean.newexam.Children;
import com.readboy.bean.newexam.ChildrenQuestion;
import com.readboy.bean.newexam.ExamBean;
import com.readboy.bean.newexam.QuestionInfo;
import com.readboy.log.LogUtils;
import com.readboy.myopencvcamera.R;

import org.opencv.core.Point;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.netty.util.internal.StringUtil;

public class DeviceUtil {
    public static ExamBean getExamData(Context context){
        ExamBean data = null;
        try {
            String question = context.getResources().getString(R.string.json_answer_a);
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

    public static List<QuestionInfo> getQuestionInfoList(ExamBean data,double ratioWidth , double ratioHeight){
        List<QuestionInfo> list = new ArrayList<>();
        List<Children> bigQuestion = data.getChildren();
        int queNum = 1;
        for (Children children : bigQuestion) {
            LogUtils.d("Children children = " + children.getType());
            List<ChildrenQuestion> childQuestion = children.getChildren();
            for(int i = 1 ; i < childQuestion.size();i++){
                QuestionInfo info = new QuestionInfo();
                ChildrenQuestion realQuestion = childQuestion.get(i);
                info.setType(children.getType());
                LogUtils.d("Children realQuestion = " + realQuestion.toString());

                int marginMore = 10;
                RectF rectF = new RectF((float) ((children.getLeftTopX() + realQuestion.getLeftTopX())*ratioWidth- 2.2* marginMore),   (float) ((children.getLeftTopY() + realQuestion.getLeftTopY())*ratioHeight- 0.8*marginMore),
                        (float) ((children.getLeftTopX() + realQuestion.getRightBottomX()) * ratioWidth+ 2.2* marginMore),(float)( (children.getLeftTopY() + realQuestion.getRightBottomY())*ratioHeight+ 1.2*marginMore));
                info.setQueLocation(rectF);
                if(children.getType() == 10023){
                    info.setQueNum(queNum ++);
                    info.setSmallQue(1);
                    info.setQueLocation(rectF);
                    list.add(info);
                    break;
                }


                info.setQueNum(queNum ++);
                info.setSmallQue(i);
                list.add(info);

            }

            for (QuestionInfo questionInfo : list) {
                LogUtils.d("Children realQuestion = " + questionInfo.toString());

            }
        }
        return list;
    }



   public static QuestionInfo getClickQuestionFromRect(List<QuestionInfo> list,float posX, float posY){
       for (QuestionInfo questionInfo : list) {
           RectF queLocation = questionInfo.getQueLocation();
           if(queLocation.left < posX && queLocation.right > posX && queLocation.top < posY && queLocation.bottom > posY){
               return questionInfo;
           }
       }
        return null;
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
