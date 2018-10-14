package com.zteek.mapper;

import com.zteek.entity.AmazonTask;
import com.zteek.entity.AmazonTaskRun;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface TaskMapper {

    public AmazonTask getTask();
    public int updateTaskById(@Param("id") Long id);

    int insertTaskRecord(AmazonTaskRun atr);

    int completedTaskById(@Param("taskId") Long taskId);

    int updateTaskRecord(AmazonTaskRun atr);

    /**
     * 首页 折线图
     * @return
     */
    List<AmazonTask> indexChart();

    Map<String,Integer> indexTable(@Param("ct") int ct);

    int insertTask(AmazonTask task);

    List<AmazonTask> getTaskList(Map<String, Object> param);


}
