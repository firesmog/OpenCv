package com.readboy.net.bean;

public class Word {
    private String content;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "Word{" +
                "content='" + content + '\'' +
                '}';
    }
}
