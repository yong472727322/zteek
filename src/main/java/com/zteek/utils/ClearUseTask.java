package com.zteek.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;

import java.util.Date;
import java.util.Map;

/**
 * 定时 清除 代理 使用，防止一台手机长时间不返回 结果，强制切换IP
 * @author leo
 * @date 2018/10/25 09:36
 */
@Controller
public class ClearUseTask {
    private static Logger log = LoggerFactory.getLogger(ClearUseTask.class);

    @Autowired
    private IPUtil ipUtil;

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
                boolean flag = false;
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
                                flag = true;
                                phone.setValue(null);
                                currentNum ++;
                            }
                        }else {
                            currentNum ++;
                        }
                    }
                    if(flag){
                        log.info("验证IP[{}]是否所有的使用时间都设置为null(标识为切换IP)",vpsIp);
                        Integer targetNum = Constant.vps_change.get(vps);
                        if(null != targetNum){
                            if(currentNum >= targetNum){
                                log.info("VPS[{}],IP[{}]，实际数量[{}]达到目标数量[{}]，切换。",vps,vpsIp,currentNum,targetNum);
                                ipUtil.changVpsIp(vpsIp);
                            }
                        }
                    }
                }
            }
        }
        log.info("定时任务，结束检测是否有手机长时间(连续30分钟)使用代理，防止手机问题导致IP不切换。");
    }

}
