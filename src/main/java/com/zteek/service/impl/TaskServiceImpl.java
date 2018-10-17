package com.zteek.service.impl;

import com.zteek.entity.AmazonTask;
import com.zteek.entity.AmazonTaskRun;
import com.zteek.mapper.TaskMapper;
import com.zteek.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class TaskServiceImpl implements TaskService {

    @Autowired
    private TaskMapper taskMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AmazonTaskRun getTask(String imei) {
        //先取一个未完成的任务
        AmazonTask at = taskMapper.getTask();
        if(null != at.getId()){
            //更新任务数
            taskMapper.updateTaskById(at.getId());
            //写记录表
            AmazonTaskRun atr = new AmazonTaskRun();
            atr.setTaskId(at.getId());
            atr.setImei(imei);
            taskMapper.insertTaskRecord(atr);
            //返回任务
            atr.setArgs(at.getArgs());
            return atr;
        }
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int endTask(AmazonTaskRun atr) {
        //完成一次任务
        taskMapper.completedTaskById(atr.getTaskId());
        //更新 任务 记录
        taskMapper.updateTaskRecord(atr);
        return 1;
    }

    @Override
    public int insertTask(AmazonTask task) {
        return taskMapper.insertTask(task);
    }

    @Override
    public List<AmazonTask> getTaskList(Map<String, Object> param) {
        return taskMapper.getTaskList(param);
    }

    @Override
    public List<AmazonTask> indexChart(Integer resultCode) {
        return taskMapper.indexChart(resultCode);
    }

    @Override
    public Map<String, Integer> indexTable(int i) {
        return taskMapper.indexTable(i);
    }
}
