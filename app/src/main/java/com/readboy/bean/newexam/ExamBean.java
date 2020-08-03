package com.readboy.bean.newexam;

import java.util.List;

public class ExamBean {
    private List<Children> children;
    private int width;
    private int height;

    public List<Children> getChildren() {
        return children;
    }

    public void setChildren(List<Children> children) {
        this.children = children;
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

    @Override
    public String toString() {
        return "ExamBean{" +
                "children=" + children +
                ", width=" + width +
                ", height=" + height +
                '}';
    }
}
