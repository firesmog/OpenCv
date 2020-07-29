package com.readboy.net.bean;

public class Location {
    private Point right_bottom;
    private Point top_left;

    public Point getRight_bottom() {
        return right_bottom;
    }

    public void setRight_bottom(Point right_bottom) {
        this.right_bottom = right_bottom;
    }

    public Point getTop_left() {
        return top_left;
    }

    public void setTop_left(Point top_left) {
        this.top_left = top_left;
    }

    @Override
    public String toString() {
        return "Location{" +
                "right_bottom=" + right_bottom +
                ", top_left=" + top_left +
                '}';
    }
}
