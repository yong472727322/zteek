package com.zteek.utils;

import com.zteek.entity.AmazonTask;
import com.zteek.entity.IpPool;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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
     * 存放 同时执行的任务
     *
     */
    public static Map<Integer,AmazonTask> tasks = new HashMap<>(16);

}
