package com.zteek.utils;

import com.zteek.entity.AmazonTask;

import java.util.HashMap;
import java.util.List;

public class TaskMapManager {

    public static TaskMapManager instance = null;

    private TaskMapManager() {

    }

    public static TaskMapManager getInstance() {
        if (instance == null) {
            synchronized (TaskMapManager.class) {
                if (instance == null) {
                    instance = new TaskMapManager();
                    initMap();
                }
            }
        }
        return instance;
    }


    public static HashMap<String, List<AmazonTask>> map = new HashMap<>();

    public static void initMap() {
        map.put("US", Constant.usTasks);
        map.put("JP", Constant.jpTasks);
        map.put("FR", Constant.frTasks);
        map.put("DE", Constant.deTasks);
        map.put("ES", Constant.esTasks);
        map.put("CA", Constant.caTasks);
        map.put("IT", Constant.itTasks);
        map.put("UK", Constant.ukTasks);

    }

    public List<AmazonTask> getTask(String state) {
        if (map == null) {
            initMap();
        }
        return map.get(state);

    }
}
