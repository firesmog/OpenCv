package com.readboy.net.bean;

import java.util.Arrays;

public class Block {
    private Line[] line;
    private String type;

    public Line[] getLine() {
        return line;
    }

    public void setLine(Line[] line) {
        this.line = line;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "Block{" +
                "line=" + Arrays.toString(line) +
                ", type='" + type + '\'' +
                '}';
    }
}
