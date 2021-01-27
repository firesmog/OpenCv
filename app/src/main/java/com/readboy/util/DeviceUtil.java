package com.readboy.util;

import android.content.Context;
import android.graphics.RectF;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

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

import cn.dream.exerciseanalysis.entity.PaperQuestion;


public class DeviceUtil {
    /**
     * width : 660
     * height : 930
     * children : [{"type":10023,"width":593,"height":160,"leftTopX":33,"leftTopY":0,"rightBottomX":626,"rightBottomY":160,"children":[{"type":"title","content":"一、单词听写","width":594,"height":60,"leftTopX":0,"leftTopY":0,"rightBottomX":594,"rightBottomY":60,"answer":[]},{"type":"question","content":"1.___________________________\n2.___________________________\n3.___________________________\n4.___________________________","width":594,"height":80,"leftTopX":0,"leftTopY":60,"rightBottomX":594,"rightBottomY":140,"answer":[{"content":"___________________________","width":240,"height":20,"leftTopX":28,"leftTopY":20,"rightBottomX":268,"rightBottomY":40},{"content":"___________________________","width":240,"height":20,"leftTopX":306,"leftTopY":20,"rightBottomX":546,"rightBottomY":40},{"content":"___________________________","width":240,"height":20,"leftTopX":28,"leftTopY":60,"rightBottomX":268,"rightBottomY":80},{"content":"___________________________","width":240,"height":20,"leftTopX":306,"leftTopY":60,"rightBottomX":546,"rightBottomY":80}]}]},{"type":10006,"width":593,"height":282,"leftTopX":33,"leftTopY":160,"rightBottomX":626,"rightBottomY":442,"children":[{"type":"title","content":"二、客观填空题","width":594,"height":60,"leftTopX":0,"leftTopY":0,"rightBottomX":594,"rightBottomY":60,"answer":[]},{"type":"question","content":"1. 已知矩形ABCD中，AB=6cm，AD=8cm，以A为圆心作⊙A，使B，C，D三点中，至少有一个点在圆内，且至少有一个点在圆外，则⊙A的半径r（cm）的取值范围是 ______ ．（不用写单位）","width":594,"height":126,"leftTopX":0,"leftTopY":60,"rightBottomX":594,"rightBottomY":186,"answer":[{"content":["6<r<10"],"width":53,"height":30,"leftTopX":107,"leftTopY":78,"rightBottomX":160,"rightBottomY":108}]},{"type":"question","content":"2. P为⊙O内一点，OP=3cm，⊙O半径为5cm，则经过P点的最短弦长为 ______ ．cm；最长弦长为 ______ ．cm．","width":594,"height":97,"leftTopX":0,"leftTopY":186,"rightBottomX":594,"rightBottomY":283,"answer":[{"content":["8","10"],"width":53,"height":30,"leftTopX":0,"leftTopY":48,"rightBottomX":53,"rightBottomY":78},{"content":"______","width":53,"height":30,"leftTopX":240,"leftTopY":48,"rightBottomX":293,"rightBottomY":78}]}]},{"type":10001,"width":593,"height":488,"leftTopX":33,"leftTopY":442,"rightBottomX":626,"rightBottomY":930,"children":[{"type":"title","content":"三、选择题","width":594,"height":60,"leftTopX":0,"leftTopY":0,"rightBottomX":594,"rightBottomY":60,"answer":[]},{"type":"question","content":"1.植树时，为了使同一行树坑在一条直线上，只需定出两个树坑的位置，其中的数学道理是 (  ) 。\n\nA. 两点确定一条直线\n\nB. 两点确定一条曲线\n\nC. 两点确定一条线段","width":594,"height":210,"leftTopX":0,"leftTopY":60,"rightBottomX":594,"rightBottomY":270,"answer":[{"content":["0"],"width":53,"height":30,"leftTopX":142,"leftTopY":48,"rightBottomX":195,"rightBottomY":78}]},{"type":"question","content":"2.选项中得数最大的算式是 (  ) 。\n\nA. 56.3÷3.2\n\nB. 56.3×3.2\n\nC. 56.3÷0.98\n\nD. 56.3×0.98","width":594,"height":218,"leftTopX":0,"leftTopY":270,"rightBottomX":594,"rightBottomY":488,"answer":[{"content":["1"],"width":53,"height":30,"leftTopX":231,"leftTopY":18,"rightBottomX":284,"rightBottomY":48}]}]}]
     */

    private int width;
    private int height;
    private List<ChildrenBeanX> children;

    public static ExamBean getExamData(Context context){
        ExamBean data = null;
        try {
            String question = context.getResources().getString(R.string.string_ai_correct);
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

    public static ExamBean getExamData(String fileName,Context context){
        ExamBean data = null;
        try {
            String question = GsonUtil.getJson(fileName,context);
            data = GsonUtil.gsonToBean(question, ExamBean.class);
        }catch (Exception e){
            LogUtils.e("error == " + e.getMessage());
        }
        return data;
    }

    public static String getQuestionAccordType(ExamBean examBean,int type){
        String result = "";
        List<Children> children = examBean.getChildren();
        for (Children child : children) {
            LogUtils.d(" getQuestionAccordType = " + child.toString() + " type == " + type);
            if(type == child.getType()){
                result = child.getChildren().get(0).getContent();
            }
        }
        LogUtils.d(" getQuestionAccordType = " + result);
        return result;
    }

    public static List<PaperQuestion> getAnalysisData(String fileName, Context context){
        List<PaperQuestion> data = new ArrayList<>();
        try {
            String question = GsonUtil.getJson(fileName,context);
            LogUtils.d("question question == " + question);
            data = GsonUtil.parserJsonToArrayBeans(question, PaperQuestion.class);
            for (PaperQuestion datum : data) {
                LogUtils.d(" getAnalysisData  getAnalysisData = " +  datum.toString());
            }
        }catch (Exception e){
            LogUtils.e("getAnalysisData error == " + e.getMessage());
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

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public List<ChildrenBeanX> getChildren() {
        return children;
    }

    public void setChildren(List<ChildrenBeanX> children) {
        this.children = children;
    }

    public static class ChildrenBeanX {
        /**
         * type : 10023
         * width : 593
         * height : 160
         * leftTopX : 33
         * leftTopY : 0
         * rightBottomX : 626
         * rightBottomY : 160
         * children : [{"type":"title","content":"一、单词听写","width":594,"height":60,"leftTopX":0,"leftTopY":0,"rightBottomX":594,"rightBottomY":60,"answer":[]},{"type":"question","content":"1.___________________________\n2.___________________________\n3.___________________________\n4.___________________________","width":594,"height":80,"leftTopX":0,"leftTopY":60,"rightBottomX":594,"rightBottomY":140,"answer":[{"content":"___________________________","width":240,"height":20,"leftTopX":28,"leftTopY":20,"rightBottomX":268,"rightBottomY":40},{"content":"___________________________","width":240,"height":20,"leftTopX":306,"leftTopY":20,"rightBottomX":546,"rightBottomY":40},{"content":"___________________________","width":240,"height":20,"leftTopX":28,"leftTopY":60,"rightBottomX":268,"rightBottomY":80},{"content":"___________________________","width":240,"height":20,"leftTopX":306,"leftTopY":60,"rightBottomX":546,"rightBottomY":80}]}]
         */

        private int type;
        private int width;
        private int height;
        private int leftTopX;
        private int leftTopY;
        private int rightBottomX;
        private int rightBottomY;
        private List<ChildrenBean> children;

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }

        public int getLeftTopX() {
            return leftTopX;
        }

        public void setLeftTopX(int leftTopX) {
            this.leftTopX = leftTopX;
        }

        public int getLeftTopY() {
            return leftTopY;
        }

        public void setLeftTopY(int leftTopY) {
            this.leftTopY = leftTopY;
        }

        public int getRightBottomX() {
            return rightBottomX;
        }

        public void setRightBottomX(int rightBottomX) {
            this.rightBottomX = rightBottomX;
        }

        public int getRightBottomY() {
            return rightBottomY;
        }

        public void setRightBottomY(int rightBottomY) {
            this.rightBottomY = rightBottomY;
        }

        public List<ChildrenBean> getChildren() {
            return children;
        }

        public void setChildren(List<ChildrenBean> children) {
            this.children = children;
        }

        public static class ChildrenBean {
            /**
             * type : title
             * content : 一、单词听写
             * width : 594
             * height : 60
             * leftTopX : 0
             * leftTopY : 0
             * rightBottomX : 594
             * rightBottomY : 60
             * answer : []
             */

            private String type;
            private String content;
            private int width;
            private int height;
            private int leftTopX;
            private int leftTopY;
            private int rightBottomX;
            private int rightBottomY;
            private List<?> answer;

            public String getType() {
                return type;
            }

            public void setType(String type) {
                this.type = type;
            }

            public String getContent() {
                return content;
            }

            public void setContent(String content) {
                this.content = content;
            }

            public int getWidth() {
                return width;
            }

            public void setWidth(int width) {
                this.width = width;
            }

            public int getHeight() {
                return height;
            }

            public void setHeight(int height) {
                this.height = height;
            }

            public int getLeftTopX() {
                return leftTopX;
            }

            public void setLeftTopX(int leftTopX) {
                this.leftTopX = leftTopX;
            }

            public int getLeftTopY() {
                return leftTopY;
            }

            public void setLeftTopY(int leftTopY) {
                this.leftTopY = leftTopY;
            }

            public int getRightBottomX() {
                return rightBottomX;
            }

            public void setRightBottomX(int rightBottomX) {
                this.rightBottomX = rightBottomX;
            }

            public int getRightBottomY() {
                return rightBottomY;
            }

            public void setRightBottomY(int rightBottomY) {
                this.rightBottomY = rightBottomY;
            }

            public List<?> getAnswer() {
                return answer;
            }

            public void setAnswer(List<?> answer) {
                this.answer = answer;
            }
        }
    }

    public static Point transformPointFromOld(com.readboy.bean.old.Point point){
        Point point1 = new Point();
        double[] points = {point.getX(),point.getY()};
        point1.set(points);
        return point1;
    }


}
