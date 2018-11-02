package com.zteek.utils;

import com.zteek.entity.IpPool;
import com.zteek.service.IpPoolService;
import com.zteek.service.PhoneService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 定时 清除 代理 使用，防止一台手机长时间不返回 结果，强制切换IP
 * @author leo
 * @date 2018/10/25 09:36
 */
@Component
public class ClearUseTask {
    private static Logger log = LoggerFactory.getLogger(ClearUseTask.class);

    @Autowired
    private IPUtil ipUtil;
    @Autowired
    private IpPoolService ipPoolService;
    @Autowired
    private PhoneService phoneService;

    /**
     * 每30分钟 清除一次 超时任务
     */
    @Scheduled(cron = "0 0/30 * * * ?")
    public void clearUse(){
        //当前时间
        Date now = new Date();
        //30分钟
        int thirtyMin = 30 * 60 * 1000;
        log.info("定时任务，开始检测是否有手机长时间(连续30分钟)使用代理，防止手机问题导致IP不切换。");
        if(!Constant.use.isEmpty()){
            for(Map.Entry<String,Map<String,Date>>  use : Constant.use.entrySet()){
                String vpsIp = use.getKey();
                //IP使用记录
                Map<String, Date> useRecord = use.getValue();
                if(!useRecord.isEmpty()){

                    //找到当前IP的VPS，并找到对应的限制使用次数
                    String vps = null;
                    for(Map.Entry<String,String> map : Constant.vps.entrySet()){
                        String ip = map.getValue();
                        if(ip.equalsIgnoreCase(vpsIp)){
                            vps = map.getKey();
                            break;
                        }
                    }

                    //判断 当前VPS 的状态  如果  是正在切换，就不管了
                    if(Constant.vps_state.get(vps)){
                        log.info("当前VPS[{}]正在切换中，跳过。",vps);
                        continue;
                    }

                    //实际数量
                    int currentNum = 0;
                    for(Map.Entry<String,Date> phone : useRecord.entrySet()){
                        String imei = phone.getKey();
                        //开始 使用时间
                        Date startUseDate = phone.getValue();
                        if(null != startUseDate){
                            if((now.getTime()-startUseDate.getTime()) > thirtyMin){
                                log.warn("手机[{}]使用IP[{}]时间超过30分钟，判定为任务失败，强制把使用时间设置为null(标识为切换IP)",imei,vpsIp);
                                phone.setValue(null);
                                currentNum ++;
                            }
                        }else {
                            currentNum ++;
                        }
                    }
                    log.info("验证IP[{}]是否所有的使用时间都设置为null(标识为切换IP)",vpsIp);
                    Integer targetNum = Constant.vps_change.get(vps);
                    if(null != targetNum){
                        if(currentNum >= targetNum && currentNum >= Constant.use.get(vpsIp).size()){
                            log.info("VPS[{}],IP[{}]，实际数量[{}]达到目标数量[{}]并且所有手机使用时间都标识为null(标识为切换IP)，切换。",vps,vpsIp,currentNum,targetNum);
                            ipUtil.changVpsIp(vpsIp);
                        }
                    }
                }
            }
        }
        log.info("定时任务，结束检测是否有手机长时间(连续30分钟)使用代理，防止手机问题导致IP不切换。");
    }

    /**
     * 每天0点清除无效VPS
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void clearVps(){
        log.info("定时任务，开始每天0点清除无效VPS。");
        List<IpPool> vpsNewIps = ipPoolService.getVpsNewIps();
        Constant.vps.clear();
        for(IpPool ipPool : vpsNewIps){
            Constant.vps.put(ipPool.getVps(),ipPool.getIp());
        }
        log.info("定时任务，结束每天0点清除无效VPS。");
    }

    /**
     * 每天1点清除3天前的日志
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void clearPhoneLog(){
        log.info("定时任务，开始每天1点清除3天前的日志。");
        phoneService.deleteLog();
        log.info("定时任务，结束每天1点清除3天前的日志。");
    }


    /**
     * 每20分钟 检测一次VPS的IP，防止一直显示在换，实际上已经换了的，只是状态没有更新。
     */
    @Scheduled(cron = "0 5/20 * * * ?")
    public void changeVpsIp(){
        log.info("定时任务，开始每20分钟 检测一次VPS的IP。");
        //当前时间
        Date now = new Date();
        //30分钟
        int thirtyMin = 30 * 60 * 1000;
        for(Map.Entry<String,Boolean> vpsState : Constant.vps_state.entrySet()){
            //找到 正在 更换IP 中的VPS
            if(vpsState.getValue()){
                String vps = vpsState.getKey();
                Date date = Constant.vps_new_ip.get(vps);
                if(null != date){
                    if((now.getTime()-date.getTime()) > thirtyMin){
                        String vpsIp = Constant.vps.get(vps);
                        log.warn("此VPS[{}]的IP[{}]已经超过30分钟没有更换或更换IP时间超过30分钟，重新调用换IP",vps,vpsIp);
                        ipUtil.changVpsIp(vpsIp);
                    }
                }
            }
        }
        log.info("定时任务，结束每20分钟 检测一次VPS的IP。");
    }

}
