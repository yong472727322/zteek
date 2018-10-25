package com.zteek.service.impl;

import com.zteek.entity.AmazonTask;
import com.zteek.entity.AmazonTaskRun;
import com.zteek.mapper.TaskMapper;
import com.zteek.service.TaskService;
import com.zteek.utils.Constant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TaskServiceImpl implements TaskService {

    private static Logger log = LoggerFactory.getLogger(TaskService.class);

    @Autowired
    private TaskMapper taskMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public synchronized AmazonTaskRun getTask(String imei) {

        //任务个数
        int size = Constant.tasks.size();
        log.info("手机[{}]获取任务，当前内存任务个数[{}]",imei,size);
        if(size < 1){
            //没有任务
            return null;
        }
        int cc = 0;
        //循环获取，防止获取到的任务已经执行完成，但是内存没有更新
        AmazonTask task = null;
        while(cc < size){
            int i = Constant.task_counter % size;
            //从内存中获取一个任务
            task = Constant.tasks.get(i);
            log.info("第[{}]次从内存中获取第[{}]个任务，获取到任务[{}]",cc,i,task.getId());
            //计数器+1
            Constant.task_counter ++;

            //判断任务是否已经完成了
            if(task.getRemaining() <= 0 || task.getRunNum() <= task.getRunCompleted()){
                //任务已经完成，从内存中删除
                log.info("任务[{}]已经完成，从内存中删除",task.getId());
                Constant.tasks.remove(task);
                task = null;
                cc ++;
            }else {
                cc = size + 1;
            }
        }

        if(null == task){
            return null;
        }

        //修改任务执行数量的情况
        task.setRemaining(task.getRemaining()-1);
        task.setRunCompleted(task.getRunCompleted()+1);

        //更新任务数
        taskMapper.updateTaskById(task.getId());
        //写记录表
        AmazonTaskRun atr = new AmazonTaskRun();
        atr.setTaskId(task.getId());
        atr.setImei(imei);
        taskMapper.insertTaskRecord(atr);
        //返回任务
        atr.setArgs(task.getArgs());
        //如果计数器大于100，重置，防止太大
        if(Constant.task_counter > Constant.MAX_TASK_COUNTER){
            Constant.task_counter = 0;
        }
        return atr;
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
        return taskMapper.lineChart(resultCode,null);
    }

    @Override
    public Map<String, Integer> indexTable(int i) {
        return taskMapper.indexTable(i);
    }

    @Override
    public List<AmazonTask> getTasks(Integer maxTaskNum) {
        if(null == maxTaskNum || maxTaskNum < 1){
            maxTaskNum = Constant.max_task_num;
        }
        return taskMapper.getTasks(maxTaskNum);
    }

    @Override
    public AmazonTask findTaskByAsin(String asin) {
        Map<String,Object> params = new HashMap<>(3);
        params.put("asin",asin);
        List<AmazonTask> taskList = taskMapper.getTaskList(params);
        if(null != taskList && taskList.size() > 0){
            return taskList.get(0);
        }
        return null;
    }

    @Override
    public List<AmazonTask> asinChart(Integer i, Long id) {
        return taskMapper.lineChart(i,id);
    }

    @Override
    public Map<String, BigDecimal> taskSuccessRate(Long taskId, int rateType) {
        return taskMapper.taskSuccessRate(taskId,rateType);
    }

    @Override
    public Map<String, Integer> taskConsuming(Long taskId) {
        return taskMapper.taskConsuming(taskId);
    }
}
