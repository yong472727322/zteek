package com.zteek.controller;

import com.zteek.entity.IpPool;
import com.zteek.utils.IPUtil;
import com.zteek.utils.MD5;
import com.zteek.utils.ProxyMessage;
import com.zteek.utils.ReturnResult;
import com.zteek.service.IpPoolService;
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
        try{
            String ip = IPUtil.getIp(request);
            logger.info("客户端[{}]请求代理地址",ip);
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

            String s = MD5.MD5(String.valueOf(timestamp));
            if(s.toUpperCase().equals(token.toUpperCase())){

                //根据手机IMEI获取IP
                IpPool ipPool = ipPoolService.getIPByIMEI(imei);

                rr.setCode("0000");
                rr.setMessage("success");
                ProxyMessage pm = new ProxyMessage();
                pm.setAddr(ipPool.getIp());
                pm.setPort(ipPool.getPort());
                pm.setAccount(ipPool.getAccount());
                pm.setPassword(ipPool.getPassword());
                rr.setObject(pm);

                //插入一条使用记录
                ipPoolService.insertUseRecord(ipPool.getId(),ip,imei);
            }else {
                rr.setCode("9999");
                rr.setMessage("fail");
            }
        }catch (Exception e){
            e.printStackTrace();
        }
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
            logger.info("客户端[{}]发送换IP请求",ip);
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

            String s = MD5.MD5(String.valueOf(timestamp));
            if(s.toUpperCase().equals(token.toUpperCase())){
                rr.setCode("0000");
                rr.setMessage("success");
                boolean b = ipUtil.changIp(imei, ip);
                rr.setObject(b);
            }else {
                rr.setCode("9999");
                rr.setMessage("fail");
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return rr;
    }

    /**
     * 保存IP
     * @return
     */
    @RequestMapping("saveIP")
    public void saveIP(IpPool ipPool){
        ipPoolService.insertIP(ipPool);
    }


    /**
     * 修改更换计数器
     * @return
     */
    @RequestMapping("setChangCount")
    public Object setChangCount(int cc){
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


}
