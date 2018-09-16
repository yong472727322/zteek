package com.zteek.service.impl;

import com.zteek.entity.IpPool;
import com.zteek.entity.IpRecord;
import com.zteek.mapper.IpPoolMapper;
import com.zteek.service.IpPoolService;
import com.zteek.utils.IPUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class IpPoolServiceImpl implements IpPoolService {

    @Autowired
    private IpPoolMapper ipPoolMapper;
    @Autowired
    private IPUtil ipUtil;

    @Override
    public IpPool getNewIP() {
        return ipPoolMapper.getNewIP();
    }

    @Override
    public IpPool getIPByIMEI(String imei) {
        return ipPoolMapper.getIPByIMEI(imei);
    }

    @Override
    public int insertUseRecord(Long pId, String ip, String imei) {
        IpRecord record = new IpRecord();
        record.setpId(pId);
        record.setIp(ip);
        record.setImei(imei);
        ipPoolMapper.insertRecord(record);

        //写入内存计数
        boolean b = ipUtil.changIp(imei);
        if(b){
            return 2;
        }
        return 1;
    }

    @Override
    public int insertIP(IpPool ipPool) {
        int i = ipPoolMapper.checkIP(ipPool.getIp(),ipPool.getVps());
        if(0 == i){
            //此IP近1小时没有出现过
            ipPoolMapper.insertIP(ipPool);
            return 1;
        }
        return 0;

    }

}
