package com.readboy.bean.newexam;


import cn.dream.exerciseanalysis.entity.PaperQuestion;

public class AnalysisBean {
    private PaperQuestion paperQuestion;
    private boolean select;

    public AnalysisBean() {
    }

    public AnalysisBean(PaperQuestion paperQuestion, boolean select) {
        this.paperQuestion = paperQuestion;
        this.select = select;
    }



    public PaperQuestion getPaperQuestion() {
        return paperQuestion;
    }

    public void setPaperQuestion(PaperQuestion paperQuestion) {
        this.paperQuestion = paperQuestion;
    }

    public boolean isSelect() {
        return select;
    }

    public void setSelect(boolean select) {
        this.select = select;
    }



    @Override
    public String toString() {
        return "AnalysisBean{" +
                "paperQuestion=" + paperQuestion +
                ", select=" + select +
                '}';
    }
}
