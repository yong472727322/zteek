package com.zteek.service.impl;

import com.sun.corba.se.impl.encoding.CDROutputStream_1_0;
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
import java.util.*;

@Service
public class TaskServiceImpl implements TaskService {

    private static Logger log = LoggerFactory.getLogger(TaskService.class);

    @Autowired
    private TaskMapper taskMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public synchronized AmazonTaskRun getTask(String imei, String proxyIp) {
        //任务个数
        if(Constant.tasks.isEmpty()){
            //没有任务
            log.warn("没有任务。。。");
            return null;
        }
        int size = Constant.tasks.size();
        log.info("手机[{}]获取任务，当前内存任务个数[{}]",imei,size);
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
            log.warn("获取任务，结果[null]");
            return null;
        }

        //验证  一个IP一台手机最大跑的任务个数

//        if(!Constant.ip_phone_max.isEmpty()){
//            List<Map<String, Map<Long, Date>>> map2 = Constant.ip_phone_max.get(proxyIp);
//            //使用IP的手机个数
//            int useIpPhones = map2.size();
//            log.error("检测手机[{}]，使用IP[{}]的手机个数[{}]",imei,proxyIp, useIpPhones);
//            if(Constant.phone_ip_task.isEmpty()){
//                Constant.phone_ip_task.put(imei,Constant.ipPhoneMaxTaskNum);
//            }else if(null == Constant.phone_ip_task.get(imei) || Constant.phone_ip_task.get(imei) > size){
//                Constant.phone_ip_task.put(imei,size);
//            }
//            if(useIpPhones > 0){
//                Map<String, Map<Long, Date>> mapp = new HashMap<>();
//                for (Map<String, Map<Long, Date>> map : map2){
//                    mapp = map;
//                    Map<Long, Date> map3 = map.get(imei);
//                    if(null != map3){
//                        if(map3.size() >= Constant.phone_ip_task.get(imei)){
//                            log.error("手机[{}]使用IP[{}]执行任务次数[{}]达到目标次数[{}]",imei,proxyIp,map3.size(),Constant.phone_ip_task.get(imei));
//                            return null;
//                        }
//                        Date date = map3.get(task.getId());
//                        if(null != date){
//                            log.error("任务[{}]已经被手机[{}]用IP[{}]执行过，返回失败，重新获取。",task.getId(),imei,proxyIp);
//                            return null;
//                        }else {
//                            log.error("任务[{}]没有被手机[{}]用IP[{}]执行过，返回任务。",task.getId(),imei,proxyIp);
//                            map3.put(task.getId(),new Date());
//                        }
//                    }else {
//                        log.error("手机[{}]第一次使用IP[{}]获取任务[{}]。",imei,proxyIp,task.getId());
//                        Map<Long, Date> map4 = new HashMap<>();
//                        map4.put(task.getId(),new Date());
//                        map.put(imei,map4);
//                    }
//                }
//                map2.add(mapp);
//            }else {
//                log.error("手机[{}]第一次使用IP[{}]获取任务",imei,proxyIp);
//
//                Map<Long,Date> taskMap = new HashMap<>();
//                taskMap.put(task.getId(),new Date());
//
//                Map<String,Map<Long,Date>> phoneMap = new HashMap<>();
//                phoneMap.put(imei,taskMap);
//
//                List<Map<String,Map<Long,Date>>> list = new ArrayList<>();
//                list.add(phoneMap);
//
//                Constant.ip_phone_max.put(proxyIp,list);
//            }
//        }else {
//            //第一个进来取任务的手机
//            log.error("手机[{}]使用IP[{}]第一个进来取任务",imei,proxyIp);
//
//            Map<Long,Date> taskMap = new HashMap<>();
//            taskMap.put(task.getId(),new Date());
//
//            Map<String,Map<Long,Date>> phoneMap = new HashMap<>();
//            phoneMap.put(imei,taskMap);
//
//            List<Map<String,Map<Long,Date>>> list = new ArrayList<>();
//            list.add(phoneMap);
//
//            Constant.ip_phone_max.put(proxyIp,list);
//        }
        log.error("手机[{}]使用IP[{}]获取到任务[{}]",imei,proxyIp,task.getId());


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

    @Override
    public int updateTaskStatusById(int status, Long id) {

        return taskMapper.updateTaskStatusById(status,id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AmazonTask getTaskForPC() {
        try{
            AmazonTask task = taskMapper.getTaskForPC();
            if(null == task){
                return null;
            }
            //更新任务
            int i = taskMapper.updatePCTaskById(task.getId());
            if(i > 0){
                return task;
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public int insertPCTask(AmazonTaskRun task) {
        return taskMapper.insertPCTask(task);
    }

    @Override
    public List<AmazonTaskRun> getPCTaskList(Map<String, Object> param) {
        return taskMapper.getPCTaskList(param);
    }
}
