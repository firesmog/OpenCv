package com.readboy.net.bean;


import com.readboy.log.LogUtils;

import java.util.Arrays;

public class Line  implements Comparable<Line>{
    private int confidence;
    private Word[] word;

    private Location location;

    public int getConfidence() {
        return confidence;
    }

    public void setConfidence(int confidence) {
        this.confidence = confidence;
    }

    public Word[] getWord() {
        return word;
    }

    public void setWord(Word[] word) {
        this.word = word;
    }



    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    @Override
    public String toString() {
        return "Line{" +
                "confidence=" + confidence +
                ", word=" + Arrays.toString(word) +
                ", location=" + location +
                '}';
    }

    @Override
    public int compareTo(Line line) {
        Line line1 = this;
        int gapY = line.getLocation().getTop_left().getY() - line1.getLocation().getTop_left().getY();
        int gapX = line.getLocation().getTop_left().getX() - line1.getLocation().getTop_left().getX();
        if( gapY > 60 ){
            LogUtils.d("line = " + line.toString() + ",line2222 = " + line1.toString());
            return -1;
        }else if(gapY > -60 && gapX > 0) {
            LogUtils.d("line = " + line.toString() + ",line33333 = " + line1.toString());
            return -1;
        }
        LogUtils.d("line = " + line.toString() + ",line1 = " + line1.toString());
        return 1;
    }
}
