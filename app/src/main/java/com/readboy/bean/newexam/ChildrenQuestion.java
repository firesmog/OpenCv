package com.readboy.bean.newexam;

import java.util.List;

public class ChildrenQuestion {
    private List<Answer>answer;
    private String content;
    private int height;
    private int leftTopX;
    private int leftTopY;
    private int rightBottomX;
    private int rightBottomY;
    private int width;
    private String type;

    public List<Answer> getAnswer() {
        return answer;
    }

    public void setAnswer(List<Answer> answer) {
        this.answer = answer;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
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

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "ChildrenX{" +
                "answer=" + answer +
                ", content='" + content + '\'' +
                ", height=" + height +
                ", leftTopX=" + leftTopX +
                ", leftTopY=" + leftTopY +
                ", rightBottomX=" + rightBottomX +
                ", rightBottomY=" + rightBottomY +
                ", width=" + width +
                ", type=" + type +
                '}';
    }
}
