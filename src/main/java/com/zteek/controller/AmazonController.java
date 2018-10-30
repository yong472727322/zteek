package com.zteek.controller;

import com.zteek.entity.AmazonTask;
import com.zteek.entity.IpPool;
import com.zteek.entity.PhoneLog;
import com.zteek.service.IpPoolService;
import com.zteek.service.PhoneService;
import com.zteek.service.TaskService;
import com.zteek.utils.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("amazon")
public class AmazonController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private IPUtil ipUtil;
    @Autowired
    private IpPoolService ipPoolService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private PhoneService phoneService;

    /**
     * 获取代理
     * @param token
     * @param timestamp
     * @param request
     * @return
     */
    @RequestMapping("proxy")
    public ReturnResult proxy(String token, Long timestamp, String imei,HttpServletRequest request){
        long startTime = System.currentTimeMillis();
        ReturnResult rr = new ReturnResult();
        String proxyIp = null;
        try{
            String ip = IPUtil.getIp(request);
            logger.info("手机[{}],ip[{}]请求代理地址",imei,ip);
            if(null == token || "".equals(token)){
                rr.setCode("9999");
                rr.setMessage("fail");
                rr.setObject("token不能为空");
                return rr;
            }
            if(null == timestamp || "".equals(timestamp)){
                rr.setCode("9999");
                rr.setMessage("fail");
                rr.setObject("timestamp不能为空");
                return rr;
            }
            if(null == imei || "".equals(imei)){
                rr.setCode("9999");
                rr.setMessage("fail");
                rr.setObject("IMEI不能为空");
                return rr;
            }
            //如果请求时间大于60分钟，返回失败
            long currentTimeMillis = System.currentTimeMillis();
            long l = currentTimeMillis - timestamp;
            if(l > (1000*60*60)){
                rr.setCode("9999");
                rr.setMessage("fail");
                rr.setObject("请求时间大于60分钟");
                return rr;
            }

            String s = MD5.MD5(String.valueOf(timestamp));
            if(s.toUpperCase().equals(token.toUpperCase())){
                IpPool ipPool = ipPoolService.getNewIpByImei(imei);
                if(null == ipPool){
                    rr.setCode("9999");
                    rr.setMessage("fail");
                    rr.setObject("IP使用次数达到，切换。");
                    return null;
                }else {
                    ProxyMessage pm = new ProxyMessage();
                    pm.setAddr(ipPool.getIp());
                    pm.setPort(ipPool.getPort());
                    pm.setAccount(ipPool.getAccount());
                    pm.setPassword(ipPool.getPassword());
                    rr.setObject(pm);
                    proxyIp = pm.getAddr();
                }
            }else {
                rr.setCode("9999");
                rr.setMessage("fail");
                rr.setObject("token验证失败不能为空");
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        long endTime = System.currentTimeMillis();
        logger.info("手机[{}]获取到的代理IP是[{}]，耗时[{}]毫秒",imei,proxyIp,(endTime-startTime));
        return rr;
    }

    /**
     * 发送换IP请求
     * @param token
     * @param timestamp
     * @param request
     * @return
     */
    @RequestMapping("changeIP")
    public ReturnResult changeIP(String token, Long timestamp, String imei,String proxy,HttpServletRequest request){
        ReturnResult rr = new ReturnResult();
        try{
            String ip = IPUtil.getIp(request);
            logger.info("手机[{}],ip[{}]请求IP[{}]切换网络",imei,ip,proxy);
            if(null == token || "".equals(token)){
                rr.setCode("9999");
                rr.setMessage("fail");
                return rr;
            }
            if(null == timestamp || "".equals(timestamp)){
                rr.setCode("9999");
                rr.setMessage("fail");
                return rr;
            }
            if(StringUtils.isEmpty(proxy)){
                rr.setCode("9999");
                rr.setMessage("代理IP必须");
                return rr;
            }
            if(null == imei || "".equals(imei)){
                rr.setCode("9999");
                rr.setMessage("fail");
                return rr;
            }
            //如果请求时间大于60分钟，返回失败
            long currentTimeMillis = System.currentTimeMillis();
            long l = currentTimeMillis - timestamp;
            if(l > (1000*60*60)){
                rr.setCode("9999");
                rr.setMessage("fail");
                return rr;
            }

            String s = MD5.MD5(String.valueOf(timestamp));
            if(s.toUpperCase().equals(token.toUpperCase())){
                boolean b = ipUtil.changIp(true,imei,proxy);
                rr.setObject(b);
            }else {
                rr.setCode("9999");
                rr.setMessage("fail");
            }
        }catch (Exception e){
            logger.error("发送换IP请求出错，原因：[{}]",e);
        }
        return rr;
    }

    /**
     * 记录VPS的状态
     */
    @RequestMapping("recordVpsState")
    public void recordVpsState(String vps,boolean state){
        Constant.vps_state.put(vps,state);
    }
    /**
     * 保存IP
     * @return
     */
    @RequestMapping("saveIP")
    public boolean saveIP(IpPool ipPool){
        int i = ipPoolService.insertIP(ipPool);
        String vps = ipPool.getVps();
        logger.info("VPS[{}]发送保存IP[{}]请求,保存结果[{}],ID[{}]", vps,ipPool.getIp(),i,ipPool.getId());
        if(1 == i){
            //VPS更换IP成功，修改状态
            changeIpSucess(vps, ipPool);
            return true;
        }else {
            //再次请求VPS更换IP
            ipUtil.changVpsIp(ipPool.getIp());
        }
        return false;
    }

    /**
     * 记录VPS当前IP
     * @return
     */
    @RequestMapping("recordIP")
    public boolean recordIP(String ip,String vps){
        logger.info("记录vps[{}]发送过来的ip[{}]",vps,ip);
        //查询出数据库中最新的IP
        IpPool ipPool = ipPoolService.getNewIpByVps(vps);
        if(null == ipPool){
            ipPool = new IpPool();
            ipPool.setVps(vps);
            ipPool.setIp(ip);
            ipPool.setAccount(vps);
            ipPool.setPort(4431);
            ipPool.setPassword("password");
            int i = ipPoolService.insertIP(ipPool);
            logger.info("VPS[{}]是新加入的，直接插入数据库，结果[{}]",vps,i);
            if( 1 == i){
                changeIpSucess(vps,ipPool);
                return true;
            }
            return false;
        }
        String dbIP = ipPool.getIp();
        logger.info("VPS[{}]在数据库中最新的IP是[{}]",vps,dbIP);
        //比较是否相同
        if (!dbIP.equals(ip)) {
            //不相同，插入数据库
            ipPool.setIp(ip);
            ipPool.setVps(vps);
            int i = ipPoolService.insertIP(ipPool);
            logger.warn("VPS[{}]数据库最新IP[{}]和发送过来的IP[{}]不一致，更新数据库。结果[{}]",vps,dbIP,ip,i);
            if(0 == i){
                //此IP重复，发送切换IP请求到VPS
                logger.warn("此IP[{}]重复，发送切换IP请求到VPS[{}]",ip,vps);
                ipUtil.changVpsIp(ip);
            }else if(1 == i){
                changeIpSucess(vps, ipPool);
                return true;
            }
        }
        return false;
    }

    /**
     * 记录手机日志
     * @return
     */
    @RequestMapping("recordLog")
    public void recordLog(PhoneLog log){
        if(!StringUtils.isEmpty(log.getImei()) && !StringUtils.isEmpty(log.getMessage())){
            if(!StringUtils.isEmpty(log.getLevel()) && "error".equalsIgnoreCase(log.getLevel())){
                //只有错误日志，才打印出来。
                logger.error("记录手机[{}]发送过来的错误日志[{}]",log.getImei(),log.getMessage());
            }
            phoneService.recordLog(log);
        }
    }

    /**
     * VPS修改IP成功之后所需的步骤
     * @param vps
     * @param ipPool
     */
    private void changeIpSucess(String vps, IpPool ipPool) {
        //VPS更换IP成功，修改状态
        Constant.vps_state.put(vps,false);
        //设置新IP的时间
        Constant.vps_new_ip.put(vps,new Date());

        //获取VPS前一个IP
        String preIp = Constant.vps.get(vps);

        //清空 前一个IP 防止内存泄漏
        Constant.use.remove(preIp);

        //将VPS的当前IP 写入内存
        Constant.vps.put(vps,ipPool.getIp());
        Constant.vps_detail.put(vps,ipPool);

        //如果是新加入的，服务器一开始没有加载到
        if(!Constant.vps_change.containsKey(vps)){
            Constant.vps_change.put(vps,Constant.changCount);
        }
    }

    /**
     * 获取VPS当前IP
     * @return
     */
    @RequestMapping("getIP")
    public Object getIP(){
        return Constant.vps;
    }

    /**
     * 修改更换计数器
     * @return
     */
    @RequestMapping("setChangCount")
    public Object setChangCount(int cc,String vps){
        //如果 没有传 VPS 说明 修改 全部
        if(StringUtils.isEmpty(vps)){
            for(Map.Entry<String,Integer> map : Constant.vps_change.entrySet()){
                logger.info("1修改VPS[{}]最大使用次数[{}]",map.getKey(),cc);
                map.setValue(cc);
            }
        }else {
            logger.info("2修改VPS[{}]最大使用次数[{}]",vps,cc);
            Constant.vps_change.put(vps,cc);
        }
       return Constant.vps_change;
    }

    /**
     * 获取 计数器
     * @return
     */
    @RequestMapping("getChangCount")
    public Object getChangCount(){
        return Constant.vps_change;
    }

    /**
     * 获取 当前使用情况
     * @return
     */
    @RequestMapping("getCurrentCount")
    public Object getCurrentCount(){
        return Constant.use;
    }

    /**
     * 修改同时执行的任务个数
     */
    @RequestMapping("changeMaxTaskNum")
    public Object changeMaxTaskNum(int max){
        //清空 内存中的任务
        Constant.tasks.clear();
        //重新 从数据库中查询
        List<AmazonTask> tasks = taskService.getTasks(max);
        for(AmazonTask task : tasks){
            Constant.tasks.add(task);
        }
        Constant.max_task_num = max;
        return tasks;
    }


    /**
     * 根据不同参数，获取对应的变量
     * @param str
     * @return
     */
    @RequestMapping("getCurrentCount/{str}")
    public Object getCommon(@PathVariable("str") String str){
        Object obj;
        switch (str){
            case "use" :
                obj = Constant.use;
                break;
            case "task_counter":
                obj = Constant.task_counter;
                break;
            case "max_task_num":
                obj = Constant.max_task_num;
                break;
            case "tasks":
                obj = Constant.tasks;
                break;
            case "vps_state":
                obj = Constant.vps_state;
                break;
            case "vps_change":
                obj = Constant.vps_change;
                break;
            case "vps_detail":
                obj = Constant.vps_detail;
                break;
            case "vps_new_ip":
                obj = Constant.vps_new_ip;
                break;
            default:
                obj = Constant.vps;
        }
        return obj;
    }



}
