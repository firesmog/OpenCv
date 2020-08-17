package com.readboy.bean.newexam;

public class GuideBean {
    private int curItem;
    private boolean select;

    public GuideBean() {
    }

    public GuideBean(int curItem, boolean select) {
        this.curItem = curItem;
        this.select = select;
    }

    public int getCurItem() {
        return curItem;
    }

    public void setCurItem(int curItem) {
        this.curItem = curItem;
    }

    public boolean isSelect() {
        return select;
    }

    public void setSelect(boolean select) {
        this.select = select;
    }

    @Override
    public String toString() {
        return "GuideBean{" +
                "curItem=" + curItem +
                ", select=" + select +
                '}';
    }
}
