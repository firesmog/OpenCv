package com.readboy.bean.newexam;

import android.graphics.Bitmap;

import org.opencv.core.Point;

import java.util.List;

public class RectangleInfo {
    private Bitmap bitmap;
    private List<Point> points;

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public List<Point> getPoints() {
        return points;
    }

    public void setPoints(List<Point> points) {
        this.points = points;
    }
}
