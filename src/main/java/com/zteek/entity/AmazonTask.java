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

    private String asin;
    private String keyword;
    private String keyword1;
    private String keyword2;
    private String productName;
    private Date calendarDate;
    private Integer total;

    public Date getCalendarDate() {
        return calendarDate;
    }

    public void setCalendarDate(Date calendarDate) {
        this.calendarDate = calendarDate;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public String getAsin() {
        return asin;
    }

    public void setAsin(String asin) {
        this.asin = asin;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getKeyword1() {
        return keyword1;
    }

    public void setKeyword1(String keyword1) {
        this.keyword1 = keyword1;
    }

    public String getKeyword2() {
        return keyword2;
    }

    public void setKeyword2(String keyword2) {
        this.keyword2 = keyword2;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
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

    @Override
    public String toString() {
        return "AmazonTask{" +
                "id=" + id +
                ", args='" + args + '\'' +
                ", level=" + level +
                ", site='" + site + '\'' +
                ", runNum=" + runNum +
                ", runingNum=" + runingNum +
                ", runCompleted=" + runCompleted +
                ", remaining=" + remaining +
                ", createdBy='" + createdBy + '\'' +
                ", createdDate=" + createdDate +
                ", asin='" + asin + '\'' +
                ", keyword='" + keyword + '\'' +
                ", keyword1='" + keyword1 + '\'' +
                ", keyword2='" + keyword2 + '\'' +
                ", productName='" + productName + '\'' +
                ", calendarDate=" + calendarDate +
                ", total=" + total +
                '}';
    }
}
