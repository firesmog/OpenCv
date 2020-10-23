package com.readboy.net.bean;

public class ExamResponse {
    private int F_responseNo;
    private String F_responseMsg;
    private String F_score;

    public int getF_responseNo() {
        return F_responseNo;
    }

    public void setF_responseNo(int f_responseNo) {
        F_responseNo = f_responseNo;
    }

    public String getF_responseMsg() {
        return F_responseMsg;
    }

    public void setF_responseMsg(String f_responseMsg) {
        F_responseMsg = f_responseMsg;
    }

    public String getF_score() {
        return F_score;
    }

    public void setF_score(String f_score) {
        F_score = f_score;
    }

    @Override
    public String toString() {
        return "ExamResponse{" +
                "F_responseNo=" + F_responseNo +
                ", F_responseMsg='" + F_responseMsg + '\'' +
                ", F_score='" + F_score + '\'' +
                '}';
    }
}
