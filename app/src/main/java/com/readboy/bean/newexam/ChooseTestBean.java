package com.readboy.bean.newexam;

public class ChooseTestBean {
    private int testIconId;
    private String testName;
    private boolean isSelected;

    public ChooseTestBean() {
    }

    public ChooseTestBean(String testName) {
        this.testName = testName;
    }

    public ChooseTestBean(int testIconId, String testName, boolean isSelected) {
        this.testIconId = testIconId;
        this.testName = testName;
        this.isSelected = isSelected;
    }

    public int getTestIconId() {
        return testIconId;
    }

    public void setTestIconId(int testIconId) {
        this.testIconId = testIconId;
    }

    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    @Override
    public String toString() {
        return "ChooseTextBean{" +
                "textIconId=" + testIconId +
                ", textName='" + testName + '\'' +
                ", isSelected=" + isSelected +
                '}';
    }
}
