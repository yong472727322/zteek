package com.zteek.mapper;


import com.zteek.entity.IpPool;
import com.zteek.entity.IpRecord;

import java.util.List;

/**
 * ip池
 */
public interface IpPoolMapper {

    /**
     * 查询最新的IP信息
     * @return
     */
    IpPool getNewIP();

    /**
     * 根据IMEI获取IP
     * @param imei
     * @return
     */
    IpPool getIPByIMEI(String imei);

    /**
     * 插入使用记录
     * @param record
     * @return
     */
    int insertRecord(IpRecord record);

    void insertIP(IpPool ipPool);
}
