package com.zteek.mapper;

import com.zteek.entity.AmazonTask;
import com.zteek.entity.AmazonTaskRun;
import org.apache.ibatis.annotations.Param;

public interface TaskMapper {

    public AmazonTask getTask();
    public int updateTaskById(@Param("id") Long id);

    int insertTaskRecord(AmazonTaskRun atr);

    int completedTaskById(Long taskId);

    int updateTaskRecord(AmazonTaskRun atr);
}
