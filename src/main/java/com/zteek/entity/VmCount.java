package com.zteek.entity;

import java.util.Date;
/**
 * 各虚拟机执行情况
 * */
public class VmCount {

    private Long id;
    private String userName;//虚拟机用户名
    private String asin;//该虚拟机在跑asin
    private String keyword;//该虚拟机在跑asin对应关键字
    private Integer runCount;//该虚拟机已跑次数
    private Integer beforeCount;//十分钟前的执行次数
    private Date createTime;
    private Date updateTime;
    private Integer vmStatus;//虚拟机的运行状态，使用中，空闲中，有异常
    private String message;//异常信息
    private String country;
    private String taskName;
    private Integer restart;
    private Integer updateWar;

    public Integer getRestart() {
        return restart;
    }

    public void setRestart(Integer restart) {
        this.restart = restart;
    }

    public Integer getUpdateWar() {
        return updateWar;
    }

    public void setUpdateWar(Integer updateWar) {
        this.updateWar = updateWar;
    }
    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }
    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getAsin() {
        return asin;
    }

    public void setAsin(String asin) {
        this.asin = asin;
    }

    public Integer getRunCount() {
        return runCount;
    }

    public void setRunCount(Integer runCount) {
        this.runCount = runCount;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
    public Integer getBeforeCount() {
        return beforeCount;
    }

    public void setBeforeCount(Integer beforeCount) {
        this.beforeCount = beforeCount;
    }
    public Integer getVmStatus() {
        return vmStatus;
    }

    public void setVmStatus(Integer vmStatus) {
        this.vmStatus = vmStatus;
    }
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }
}
