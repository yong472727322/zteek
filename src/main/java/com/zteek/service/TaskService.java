package com.zteek.service;

import com.zteek.entity.AmazonTask;
import com.zteek.entity.AmazonTaskRun;

public interface TaskService {
    /**
     * 获取任务
     * @param imei
     * @return
     */
    AmazonTaskRun getTask(String imei);

    int endTask(AmazonTaskRun atr);
}
