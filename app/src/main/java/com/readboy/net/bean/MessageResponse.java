package com.readboy.net.bean;


public class MessageResponse {

    private String response_code;
    private String response_msg;

    public String getResponse_code() {
        return response_code;
    }

    public void setResponse_code(String response_code) {
        this.response_code = response_code;
    }

    public String getResponse_msg() {
        return response_msg;
    }

    public void setResponse_msg(String response_msg) {
        this.response_msg = response_msg;
    }

    @Override
    public String toString() {
        return "MessageResponse{" +
                "response_code='" + response_code + '\'' +
                ", response_msg='" + response_msg + '\'' +
                '}';
    }
}
