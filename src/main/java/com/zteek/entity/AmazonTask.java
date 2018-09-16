package com.zteek.entity;

import java.util.Date;

public class AmazonTask {
    private Long id;
    private String args;
    private Integer level;
    private String site;
    private Integer runNum;
    private Integer runingNum;
    private Integer runCompleted;
    private Integer remaining;
    private String createdBy;
    private Date createdDate;

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

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public Integer getRunNum() {
        return runNum;
    }

    public void setRunNum(Integer runNum) {
        this.runNum = runNum;
    }

    public Integer getRuningNum() {
        return runingNum;
    }

    public void setRuningNum(Integer runingNum) {
        this.runingNum = runingNum;
    }

    public Integer getRunCompleted() {
        return runCompleted;
    }

    public void setRunCompleted(Integer runCompleted) {
        this.runCompleted = runCompleted;
    }

    public Integer getRemaining() {
        return remaining;
    }

    public void setRemaining(Integer remaining) {
        this.remaining = remaining;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }
}
