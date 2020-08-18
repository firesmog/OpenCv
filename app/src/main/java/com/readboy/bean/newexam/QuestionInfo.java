package com.readboy.bean.newexam;

import android.graphics.RectF;

public class QuestionInfo {
    /**
     * * 题目四个角点位置信息
     */
    private RectF queLocation;

     /**
      * * 题目类型
     */
    private int type;
    /**
     * * 题目序号（按照蒙版显示区域进行排序来查看答案解析）
     */
    private int queNum;
    /**
     * * 当前大题对应的第几小题
     */
    private int smallQue;

    public int getSmallQue() {
        return smallQue;
    }

    public void setSmallQue(int smallQue) {
        this.smallQue = smallQue;
    }

    public RectF getQueLocation() {
        return queLocation;
    }

    public void setQueLocation(RectF queLocation) {
        this.queLocation = queLocation;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getQueNum() {
        return queNum;
    }

    public void setQueNum(int queNum) {
        this.queNum = queNum;
    }

    @Override
    public String toString() {
        return "QuestionInfo{" +
                "queLocation=" + queLocation +
                ", type=" + type +
                ", queNum=" + queNum +
                ", smallQue=" + smallQue +
                '}';
    }
}
