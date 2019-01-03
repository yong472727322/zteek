package com.zteek.entity;

import java.util.Date;

public class AmazonTaskRun {

    private Long id;
    private Long taskId;
    private String args;
    private String imei;
    private String resultMessage;
    private Integer resultCode;
    private Date createdTime;
    private Date endTime;
    private String consuming;
    private Integer taskNums;


    //PC端任务参数
    private String asin;
    private String productName;
    private Integer runNum;
    private Integer count;
    private String url;
    private Integer level;
    private Date updateTime;
    private Integer doPage;
    private String country;
    private String taskName;

    public Integer getSginCount() {
        return sginCount;
    }

    public void setSginCount(Integer sginCount) {
        this.sginCount = sginCount;
    }

    private Integer sginCount;


    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public Integer getDoPage() {
        return doPage;
    }
    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void setDoPage(Integer doPage) {
        this.doPage = doPage;
    }
    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
    public String getAsin() {
        return asin;
    }

    public void setAsin(String asin) {
        this.asin = asin;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Integer getRunNum() {
        return runNum;
    }

    public void setRunNum(Integer runNum) {
        this.runNum = runNum;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public String getArgs() {
        return args;
    }

    public void setArgs(String args) {
        this.args = args;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public String getResultMessage() {
        return resultMessage;
    }

    public void setResultMessage(String resultMessage) {
        this.resultMessage = resultMessage;
    }

    public Integer getResultCode() {
        return resultCode;
    }

    public void setResultCode(Integer resultCode) {
        this.resultCode = resultCode;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public String getConsuming() {
        return consuming;
    }

    public void setConsuming(String consuming) {
        this.consuming = consuming;
    }
    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }
    public Integer getTaskNums() {
        return taskNums;
    }

    public void setTaskNums(Integer taskNums) {
        this.taskNums = taskNums;
    }

}
