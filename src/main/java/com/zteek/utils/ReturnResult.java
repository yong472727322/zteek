package com.zteek.utils;

public class ReturnResult {

    private String code;
    private String message;
    private Object object;

    public ReturnResult() {
        this.code = Constant.CODE_SUCCESS;
        this.message = Constant.SUCCESS;
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
