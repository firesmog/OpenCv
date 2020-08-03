package com.readboy.bean.newexam;

import java.util.List;

public class Children {
    private List<ChildrenQuestion> children;
    private int height;
    private int leftTopX;
    private int leftTopY;
    private int rightBottomX;
    private int rightBottomY;
    private int width;
    //单选题： 10001；  主观填空题： 10006； 听力填空题：10023
    private int type;

    public List<ChildrenQuestion> getChildren() {
        return children;
    }

    public void setChildren(List<ChildrenQuestion> children) {
        this.children = children;
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

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "Children{" +
                "children=" + children +
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
