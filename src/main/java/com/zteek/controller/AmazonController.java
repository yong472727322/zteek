package com.zteek.controller;

import com.zteek.entity.IpPool;
import com.zteek.utils.IPUtil;
import com.zteek.utils.MD5;
import com.zteek.utils.ProxyMessage;
import com.zteek.utils.ReturnResult;
import com.zteek.service.IpPoolService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("amazon")
public class AmazonController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private IPUtil ipUtil;
    @Autowired
    private IpPoolService ipPoolService;

    /**
     * 获取代理
     * @param token
     * @param timestamp
     * @param request
     * @return
     */
    @RequestMapping("proxy")
    public ReturnResult proxy(String token, Long timestamp, String imei,HttpServletRequest request){
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
            if(IPUtil.changIpFlag){
                rr.setCode("9999");
                rr.setMessage("fail");
                rr.setObject("正在切换IP。。。");
                return rr;
            }

            String s = MD5.MD5(String.valueOf(timestamp));
            if(s.toUpperCase().equals(token.toUpperCase())){

                //根据手机IMEI获取IP
                IpPool ipPool = ipPoolService.getIPByIMEI(imei);

                ProxyMessage pm = new ProxyMessage();
                pm.setAddr(ipPool.getIp());
                pm.setPort(ipPool.getPort());
                pm.setAccount(ipPool.getAccount());
                pm.setPassword(ipPool.getPassword());
                rr.setObject(pm);
                proxyIp = pm.getAddr();

                //插入一条使用记录
                int i = ipPoolService.insertUseRecord(ipPool.getId(), ip, imei);
                if(2 == i){
                    rr.setCode("9999");
                    rr.setMessage("fail");
                    rr.setObject("IP使用次数达到，切换。");
                }
            }else {
                rr.setCode("9999");
                rr.setMessage("fail");
                rr.setObject("token验证失败不能为空");
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        logger.info("手机[{}]获取到的代理IP是[{}]",imei,proxyIp);
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
    public ReturnResult changeIP(String token, Long timestamp, String imei,HttpServletRequest request){
        ReturnResult rr = new ReturnResult();
        try{
            String ip = IPUtil.getIp(request);
            logger.info("手机[{}],ip[{}]发送换IP请求",imei,ip);
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
            if(IPUtil.changIpFlag){
                rr.setCode("9999");
                rr.setMessage("fail");
                rr.setObject("正在切换IP。。。");
                return rr;
            }
            String s = MD5.MD5(String.valueOf(timestamp));
            if(s.toUpperCase().equals(token.toUpperCase())){
                boolean b = ipUtil.changIp2(imei);
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
     * 保存IP
     * @return
     */
    @RequestMapping("saveIP")
    public void saveIP(IpPool ipPool){
        int i = ipPoolService.insertIP(ipPool);
        logger.info("VPS[{}]发送保存IP[{}]请求,保存结果[{}]",ipPool.getVps(),ipPool.getIp(),i);
        if(1 == i){
            //VPS更换IP成功，修改状态
            IPUtil.changIpFlag = false;
            IPUtil.currentIp = ipPool.getIp();
        }else {
            //再次请求VPS更换IP
            ipUtil.changVpsIp(ipPool.getIp());
        }
    }

    /**
     * 记录VPS当前IP
     * @return
     */
    @RequestMapping("recordIP")
    public void recordIP(String ip,String vps){
        logger.info("记录vps[{}]发送过来的ip[{}]",vps,ip);
        IPUtil.currentIp = ip;
        //查询出数据库中最新的IP
        IpPool ipPool = ipPoolService.getNewIpByVps(vps);
        String dbIP = ipPool.getIp();
        logger.info("数据库中最新的IP[{}]",dbIP);
        //比较是否相同
        if (!dbIP.equals(ip)) {
            //不相同，插入数据库
            ipPool.setIp(ip);
            ipPool.setVps(vps);
            int i = ipPoolService.insertIP(ipPool);
            logger.warn("数据库IP[{}]和VPS[{}]发送过来的IP[{}]不一致，更新数据库。结果[{}]",dbIP,vps,ip,i);
            if(0 == i){
                //此IP重复，发送切换IP请求到VPS
                logger.warn("此IP[{}]重复，发送切换IP请求到VPS[{}]",ip,vps);
                ipUtil.changVpsIp(ip);
            }else if(1 == i){
                //VPS更换IP成功，修改状态
                IPUtil.changIpFlag = false;
            }
        }
        //打印空行，隔开日志
        System.out.println();
    }

    /**
     * 获取VPS当前IP
     * @return
     */
    @RequestMapping("getIP")
    public Object getIP(){
        if(StringUtils.isEmpty(IPUtil.currentIp)){
            //如果IPUtil中记录的IP是空，则取数据库中最新的
            IpPool newIP = ipPoolService.getNewIP();
            String currentIp = newIP.getIp();
            logger.info("如果IPUtil中记录的IP[{}]是空，则取数据库中最新的IP[{}]，同时把IP写到IPUtil中",IPUtil.currentIp,currentIp);
            IPUtil.currentIp = currentIp;
        }
        return IPUtil.currentIp;
    }

    /**
     * 修改更换计数器
     * @return
     */
    @RequestMapping("setChangCount")
    public Object setChangCount(int cc,String vps){
        //如果 没有传 VPS 说明 修改 全部
        if(StringUtils.isEmpty(vps)){

        }else {

        }
        ipUtil.setChangCount(cc);
       return cc;
    }

    /**
     * 获取 计数器
     * @return
     */
    @RequestMapping("getChangCount")
    public Object getChangCount(){
        return ipUtil.getChangCount();
    }

    /**
     * 获取 计数器
     * @return
     */
    @RequestMapping("getCurrentCount")
    public Object getCurrentCount(){
        return ipUtil.getCurrentCount();
    }


}
