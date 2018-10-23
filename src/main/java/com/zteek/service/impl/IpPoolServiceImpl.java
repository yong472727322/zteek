package com.zteek.service.impl;

import com.zteek.entity.IpPool;
import com.zteek.entity.IpRecord;
import com.zteek.mapper.IpPoolMapper;
import com.zteek.service.IpPoolService;
import com.zteek.utils.Constant;
import com.zteek.utils.IPUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class IpPoolServiceImpl implements IpPoolService {

    private static Logger log = LoggerFactory.getLogger(IpPoolService.class);

    @Autowired
    private IpPoolMapper ipPoolMapper;
    @Autowired
    private IPUtil ipUtil;

    @Override
    public List<IpPool> getVpsNewIps() {
        return ipPoolMapper.getVpsNewIps();
    }

    @Override
    public IpPool getNewIpByVps(String vps) {
        return ipPoolMapper.getNewIpByVps(vps);
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
        return ipPoolMapper.insertRecord(record);
    }

    @Override
    public int insertIP(IpPool ipPool) {
        int i = ipPoolMapper.checkIP(ipPool.getIp(),ipPool.getVps());
        if(0 == i){
            //此IP近1小时没有出现过
            ipPoolMapper.insertIP(ipPool);
            return 1;
        }else {
            log.warn("VPS[{}]近1小时使用过此IP[{}]，返回结果0",ipPool.getVps(),ipPool.getIp());
        }
        return 0;

    }

    @Override
    public IpPool getNewIpByImei(String imei) {

        //遍历 VPS ，找到未使用过的IP
        String notUseIp = null;
        for(Map.Entry<String,String> vps : Constant.vps.entrySet()){
            //判断VPS状态
            if(Constant.vps_state.get(vps.getKey())){
                log.info("VPS[{}]正在更换IP中，不可用。",vps.getKey());
                continue;
            }
            //想用的IP，判断是否存在使用记录
            String ip = vps.getValue();
            boolean flag = true;
            Map<String, Map<String, Date>> use = Constant.use;
            if(use.size() > 0){
                Map<String, Date> stringDateMap = use.get(ip);
                if(null != stringDateMap && stringDateMap.size() > 0){
                    for(Map.Entry<String,Date> use2 : stringDateMap.entrySet()){
                        String key = use2.getKey();
                        if(imei.equalsIgnoreCase(key)){
                            flag = false;
                            break;
                        }
                    }
                    if(flag){
                        notUseIp = ip;
                        break;
                    }
                }else {
                    log.info("IP[{}]没有使用记录，可用。",ip);
                    notUseIp = ip;
                    break;
                }
            }else {
                log.info("use的大小[0]，说明没有使用记录，可用。");
                notUseIp = ip;
                break;
            }
        }

        log.info("遍历 IP使用记录  找手机[{}]未使用过的IP，结果：[{}]",imei,notUseIp);

        //如果没有找到，说明内存中所有的IP都已经使用过了，发送更换IP请求
        if(null == notUseIp){
            log.warn("没有找到，说明内存中所有的IP，手机[{}]都已经使用过了，发送更换IP请求",imei);
            ipUtil.changIp(false,imei, null);
            return null;
        }

        //遍历所有的VPS，找到 该IP对应的VPS
        String notUseVps = null;
        for(Map.Entry<String,String> map : Constant.vps.entrySet()){
            if(notUseIp.equalsIgnoreCase(map.getValue())){
                notUseVps = map.getKey();
                break;
            }
        }
        log.info("遍历所有的VPS，找到 该IP[{}]对应的VPS，结果：[{}]",notUseIp,notUseVps);
        if(null != notUseVps){
            IpPool ipPool = Constant.vps_detail.get(notUseVps);
            //插入一条使用记录
            int i = this.insertUseRecord(ipPool.getId(), ipPool.getIp(), imei);
            if(i > 0){
                log.info("插入一条使用记录，结果：[{}]",i);
                Map<String,Date> map = new HashMap<>(3);
                map.put(imei,new Date());
                Constant.use.put(notUseIp,map);
                return ipPool;
            }
            return null;
        }
        return null;
    }

}
