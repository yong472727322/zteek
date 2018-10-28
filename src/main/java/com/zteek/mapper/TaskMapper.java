package com.zteek.mapper;

import com.zteek.entity.AmazonTask;
import com.zteek.entity.AmazonTaskRun;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface TaskMapper {

    public List<AmazonTask> getTasks(@Param("maxTaskNum") Integer maxTaskNum);

    public int updateTaskById(@Param("id") Long id);

    int insertTaskRecord(AmazonTaskRun atr);

    int completedTaskById(@Param("taskId") Long taskId);

    int updateTaskRecord(AmazonTaskRun atr);

    /**
     * 首页/ASIN详情页 折线图
     * @return
     */
    List<AmazonTask> lineChart(@Param("resultCode") Integer resultCode,@Param("taskId") Long taskId);

    Map<String,Integer> indexTable(@Param("ct") int ct);

    int insertTask(AmazonTask task);

    List<AmazonTask> getTaskList(Map<String, Object> param);

    /**
     * 查询任务成功率及成功数
     * @param taskId
     * @param rateType  1:今日，2：昨日，3：全部
     * @return
     */
    Map<String,BigDecimal> taskSuccessRate(@Param("taskId") Long taskId, @Param("rateType") int rateType);

    /**
     * 查询成功任务耗时
     * @param taskId
     * @return
     */
    Map<String,Integer> taskConsuming(@Param("taskId") Long taskId);

    /**
     * 根据ID更新任务状态
     * @param status
     * @param id
     * @return
     */
    int updateTaskStatusById(@Param("status") int status, @Param("id") Long id);
}
