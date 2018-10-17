package com.zteek.service;

import com.zteek.entity.IpPool;

import java.util.List;

public interface IpPoolService {

    /**
     * 获取所有VPS最新的IP
     * @return
     */
    List<IpPool> getVpsNewIps();
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

    /**
     * 获取新IP，取内存中的IP
     * @param imei
     * @return
     */
    IpPool getNewIpByImei(String imei);
}
