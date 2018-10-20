package com.zteek.mapper;

import com.zteek.entity.PhoneLog;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * @author leo
 * @date 2018/10/20 09:57
 */
public interface PhoneMapper {
    /**
     * 获取最新的一条日志或大于指定ID的日志
     * @param imei
     * @param id
     * @return
     */
    List<PhoneLog> getNewLogByImei(@Param("imei") String imei,@Param("id") Long id);

    /**
     * 获取最近 recentlyMinute 分钟有日志的手机
     * @return
     */
    List<PhoneLog> getRecentlyLog(@Param("recentlyMinute") int recentlyMinute);

    /**
     * 保存日志
     * @param log
     * @return
     */
    int recordLog(PhoneLog log);
}
