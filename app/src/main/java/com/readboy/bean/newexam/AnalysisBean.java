package com.readboy.bean.newexam;


public class AnalysisBean {
    private int paperQuestion;
    private boolean select;

    public AnalysisBean() {
    }

    public AnalysisBean(int paperQuestion, boolean select) {
        this.paperQuestion = paperQuestion;
        this.select = select;
    }

    public int getPaperQuestion() {
        return paperQuestion;
    }

    public void setPaperQuestion(int paperQuestion) {
        this.paperQuestion = paperQuestion;
    }

    public boolean isSelect() {
        return select;
    }

    public void setSelect(boolean select) {
        this.select = select;
    }
}
