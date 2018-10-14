package com.zteek.service;

import com.zteek.entity.IpPool;

public interface IpPoolService {


    /**
     * 获取指定VPS最新的IP
     * @return
     */
    IpPool getNewIpByVps(String vps);
    /**
     * 根据手机IMEI获取IP
     * @return
     */
    IpPool getIPByIMEI(String imei);

    /**
     * 插入一条VPS使用记录
     * @param pId
     * @param ip
     * @param imei
     * @return
     */
    int insertUseRecord(Long pId, String ip, String imei);

    /**
     * 保存VPS
     * @param ipPool
     */
    int insertIP(IpPool ipPool);
}
