package com.zteek.service;

import com.zteek.entity.AmazonTask;
import com.zteek.entity.AmazonTaskRun;

import java.math.BigDecimal;
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
    List<AmazonTask> indexChart(Integer resultCode);

    /**
     * 首页 表格
     * @param i
     * @return
     */
    Map<String, Integer> indexTable(int i);

    /**
     * 按照级别倒序、创建时间升序，获取指定数量任务
     * @param maxTaskNum
     * @return
     */
    List<AmazonTask> getTasks(Integer maxTaskNum);

    /**
     * 根据ASIN查找任务
     * @param asin
     * @return
     */
    AmazonTask findTaskByAsin(String asin);

    /**
     * ASIN详情页 折线图
     * @param i null:全部，1：失败，2：成功
     * @param id    任务ID
     * @return
     */
    List<AmazonTask> asinChart(Integer i, Long id);

    /**
     * 查询任务成功率及成功数
     * @param taskId
     * @param rateType  1:今日，2：昨日，3：全部
     * @return
     */
    Map<String,BigDecimal> taskSuccessRate(Long taskId, int rateType);

    /**
     * 查询成功任务耗时
     * @param taskId
     * @return
     */
    Map<String, Integer> taskConsuming(Long taskId);
}
