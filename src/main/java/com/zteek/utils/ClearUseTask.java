package com.zteek.utils;

import com.zteek.entity.IpPool;
import com.zteek.mapper.TaskMapper;
import com.zteek.mapper.VmCountMapper;
import com.zteek.service.IpPoolService;
import com.zteek.service.PhoneService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;

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
    @Autowired
    private TaskMapper taskMapper;
    @Autowired
    private VmCountMapper vmCountMapper;

    //30分钟
    private int thirtyMin = 30 * 60 * 1000;

    /**
     * 每30分钟 清除一次 超时任务
     */
    @Scheduled(cron = "0 0/30 * * * ?")
    public void clearUse(){
        //当前时间
        Date now = new Date();
        log.error("定时任务，开始检测是否有手机长时间(连续30分钟)使用代理，防止手机问题导致IP不切换。");
        if(!Constant.use.isEmpty()){
            try{
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
            }catch (ConcurrentModificationException e){
                try {
                    int i = new Random().nextInt(5000);
                    log.warn("有正在操作Constant.use，随机暂停[{}]毫秒，再次调用。",i);
                    Thread.sleep(i);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
                clearUse();
            }catch (Exception e){
                log.error("定时任务，检测是否有手机长时间(连续30分钟)使用代理，防止手机问题导致IP不切换。出错，错误：[{}]",e);
            }

        }
        log.error("定时任务，结束检测是否有手机长时间(连续30分钟)使用代理，防止手机问题导致IP不切换。");
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
        log.error("定时任务，开始每20分钟 检测一次VPS的IP。");
        //当前时间
        Date now = new Date();
        for(Map.Entry<String,Boolean> vpsState : Constant.vps_state.entrySet()){
            String vps = vpsState.getKey();
            Date date = Constant.vps_new_ip.get(vps);
            //找到 正在 更换IP 中的VPS
            if(vpsState.getValue()){
                if(null != date){
                    if((now.getTime()-date.getTime()) > thirtyMin){
                        String vpsIp = Constant.vps.get(vps);
                        log.warn("此VPS[{}]的IP[{}]已经超过30分钟没有更换或更换IP时间超过30分钟，重新调用换IP",vps,vpsIp);
                        ipUtil.changVpsIp(vpsIp);
                    }
                }else {
                    String vpsIp = Constant.vps.get(vps);
                    log.warn("此VPS[{}]的没有IP的最近时间，直接重新调用换IP",vps);
                    ipUtil.changVpsIp(vpsIp);
                }
            }else {
                if(null == date){
                    log.warn("此VPS[{}]没有处在更换IP状态，但是没有IP最近时间，重新调换IP",vps);
                }
            }
        }
        log.error("定时任务，结束每20分钟 检测一次VPS的IP。");
    }

    /**
     * 每20分钟 检测一次VPS，使用次数达到目标次数，并且都标记为null。
     */
    @Scheduled(cron = "30 7/20 * * * ?")
    public void changeVpsIp2(){
        log.error("定时任务，开始每20分钟 检测一次VPS，使用次数达到目标次数，并且都标记为null。。");
        if(!Constant.use.isEmpty()) {
            try {
                for (Map.Entry<String, Map<String, Date>> use : Constant.use.entrySet()) {
                    String ip = use.getKey();
                    Map<String, Date> phones = use.getValue();
                    int chg = 0;
                    int size = 0;
                    if (!phones.isEmpty()) {
                        size = phones.size();
                        for (Map.Entry<String, Date> phone : phones.entrySet()) {
                            Date value = phone.getValue();
                            if (null == value) {
                                chg++;
                            }
                        }
                    }
                    Integer targeNum = null;
                    //得到此IP对应的VPS的目标使用次数
                    for (Map.Entry<String, String> vps : Constant.vps.entrySet()) {
                        String vpsIp = vps.getValue();
                        if (vpsIp.equalsIgnoreCase(ip)) {
                            targeNum = Constant.vps_change.get(vps.getKey());
                            break;
                        }
                    }
                    if (chg >= targeNum && chg >= size) {
                        log.warn("此IP[{}]已经达到目标次数[{}]，并且标记为切换的数量为[{}]，切换IP", ip, targeNum, chg);
                        ipUtil.changVpsIp(ip);
                    }
                }
            }catch (ConcurrentModificationException e){
                try {
                    int i = new Random().nextInt(5000);
                    log.warn("有正在操作Constant.use，随机暂停[{}]毫秒，再次调用。",i);
                    Thread.sleep(i);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
                changeVpsIp2();
            }catch (Exception e){
                log.error("定时任务，每20分钟 检测一次VPS，使用次数达到目标次数，并且都标记为null。出错，错误：[{}]",e);
            }
        }
        log.error("定时任务，结束每20分钟 检测一次VPS，使用次数达到目标次数，并且都标记为null。。");
    }


    @Scheduled(cron = "0 0 0 * * ?")
    public void clearNull() {
        //每天晚上零点清空执行次数
        log.error("====================================定时任务，每天晚上零点清空已执行次数=================================");
        vmCountMapper.updateBeforeCount();
        vmCountMapper.updateCountToNull();
    }
}
