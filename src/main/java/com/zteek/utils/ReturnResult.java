package com.zteek.utils;

public class ReturnResult {

    private String code;
    private String message;
    private Object object;

    public ReturnResult() {
        this.code = "0000";
        this.message="success";
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }
}
