package com.zteek.entity;

import java.util.Date;

/**
 * 手机日志
 * @author leo
 * @date 2018/10/19 18:32
 */
public class PhoneLog {

    private Long id;
    /**
     * 手机识别码
     */
    private String imei;
    /**
     * 日志信息
     */
    private String message;
    /**
     * 日志时间
     */
    private Date createdTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }
}
