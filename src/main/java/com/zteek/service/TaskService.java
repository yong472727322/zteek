package com.zteek.service;

import com.zteek.entity.AmazonTask;
import com.zteek.entity.AmazonTaskRun;

import java.util.List;
import java.util.Map;

public interface TaskService {
    /**
     * 获取任务
     * @param imei
     * @return
     */
    AmazonTaskRun getTask(String imei);

    int endTask(AmazonTaskRun atr);

    /**
     * 添加任务
     * @param task
     * @return
     */
    int insertTask(AmazonTask task);

    List<AmazonTask> getTaskList(Map<String,Object> param);

    /**
     * 首页 折线图
     * @return
     */
    List<AmazonTask> indexChart();

    /**
     * 首页 表格
     * @param i
     * @return
     */
    Map<String, Integer> indexTable(int i);
}