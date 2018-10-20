package com.zteek.service;

import com.zteek.entity.PhoneLog;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 手机、手机日志
 * @author leo
 * @date 2018/10/20 09:46
 */
public interface PhoneService {
    /**
     * 保存手机日志信息
     * @param imei
     * @param message
     * @return
     */
    int recordLog(String imei, String message);

    /**
     * 根据 imei 获取 其最新的一条日志
     * @param imei
     * @return
     */
    PhoneLog getNewLogByImei(String imei);

    /**
     * 获取大于指定ID的日志
     * @param imei
     * @param id
     * @return
     */
    List<PhoneLog> getNewLogs(String imei,Long id);

    /**
     * 获取最近10分钟有日志的手机
     * @param recentlyMinute
     * @return
     */
    List<PhoneLog> getRecentlyLog(@Param("recentlyMinute") int recentlyMinute);
}
