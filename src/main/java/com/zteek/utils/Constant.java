package com.zteek.utils;

import com.zteek.entity.AmazonTask;
import com.zteek.entity.IpPool;

import java.util.*;

/**
 * 常量
 */
public class Constant {

    public static String user = "user";
    public final static String FAIL = "fail";
    public final static String SUCCESS = "success";
    public final static String CODE_SUCCESS = "0000";
    public final static String CODE_FAIL = "9999";

    /**
     * 存放 VPS 及其 对应的 IP
     *  格式： <VPS1,IP1>,<VPS2,IP3>
     */
    public static Map<String,String> vps = new HashMap<>();
    /**
     * 存放 VPS 详细 信息
     *  格式：<VPS1,ipPool>
     */
    public static Map<String,IpPool> vps_detail = new HashMap<>();

    /**
     * 存放 IP 使用记录
     *    格式：  <IP1，<手机1，时间1>>
     */
    public static Map<String,Map<String,Date>> use = new HashMap<>();

    /**
     * 存放 VPS 更换 计数器
     *    格式：   <VPS1,5>    <VPS2,3>
     */
    public static Map<String,Integer> vps_change = new HashMap<>();

    /**
     * 存放 VPS 状态    true:更换IP中，false:正常使用
     *    格式：   <VPS1,更换IP中>    <VPS2,false>
     */
    public static Map<String,Boolean> vps_state = new HashMap<>();



    /**
     * 存放 同时执行US的任务
     *  格式：<任务详情>
     */
    public static List<AmazonTask> usTasks = new ArrayList<>(16);

    /**
     * 存放 同时执行的JP任务
     *  格式：<任务详情>
     */
    public static List<AmazonTask> jpTasks = new ArrayList<>(16);

    /**
     * 存放 同时执行的UK任务
     *  格式：<任务详情>
     */
    public static List<AmazonTask> ukTasks = new ArrayList<>(16);

    /**
     * 存放 同时执行的FR任务
     *  格式：<任务详情>
     */
    public static List<AmazonTask> frTasks = new ArrayList<>(16);

    /**
     * 存放 同时执行的IT任务
     *  格式：<任务详情>
     */
    public static List<AmazonTask> itTasks = new ArrayList<>(16);

    /**
     * 存放 同时执行的ES任务
     *  格式：<任务详情>
     */
    public static List<AmazonTask> esTasks = new ArrayList<>(16);

    /**
     * 存放 同时执行的CA任务
     *  格式：<任务详情>
     */
    public static List<AmazonTask> caTasks = new ArrayList<>(16);

    /**
     * 存放 同时执行的DE任务
     *  格式：<任务详情>
     */
    public static List<AmazonTask> deTasks = new ArrayList<>(16);

    /**
     * 存放 各个国家的取账号次数 country[0]=US,country[1]=JP
     *  格式：<任务详情>
     */
    public static Long[] country = new Long[2];

    /**
     * 同时最大执行任务数
     */
    public static Integer max_task_num = 3;

    /**
     * 任务计数器
     */
    public static Integer task_counter = 0;
    /**
     * 最大计数
     */
    public static final Integer MAX_TASK_COUNTER = 100;

    /**
     * 默认 重拨 计数器
     */
    public static final int DEFAULT_COUNT = 2;
    /**
     *  重拨 计数器
     */
    public static int changCount = DEFAULT_COUNT;

    /**
     * vps 最新IP的时间
     */
    public static Map<String,Date> vps_new_ip = new HashMap<>();


    /**
     * 默认一个IP一台手机最大跑的任务个数
     */
    public static final int DEFAULT_IP_PHONE_MAX_TASK_NUM = 3;

    /**
     * 一个IP一台手机最大跑的任务个数
     */
    public static int ipPhoneMaxTaskNum = DEFAULT_IP_PHONE_MAX_TASK_NUM;

    /**
     * 手机，单个IP，执行任务个数
     */
    public static Map<String,Integer> phone_ip_task = new HashMap<>();

    /**
     * 记录手机跑的任务数
     */
    public static Map<String,List<Map<String,Map<Long,Date>>>> ip_phone_max = new HashMap<>();

}
