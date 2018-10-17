package com.zteek.utils;

import com.zteek.entity.IpPool;
import com.zteek.service.IpPoolService;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;
import java.util.*;

@Component
public class IPUtil implements CommandLineRunner {

    @Autowired
    private IpPoolService ipPoolService;

    private static Logger log = LoggerFactory.getLogger(IPUtil.class);
    /**
     * 默认 重拨 计数器
     */
    private static final int DEFAULT_COUNT = 5;

    /**
     * 更换IP标识，true：VPS正在重新拨号
     */
//    public static boolean changIpFlag = false;

    private int changCount = DEFAULT_COUNT;

    /**
     * 存放 VPS 及其 对应的 IP
     *  格式： <VPS1,IP1>,<VPS2,IP3>
     */
    public static Map<String,String> vps = new HashMap<>();
    /**
     * 存放 VPS 详细 信息
     *  格式：<VPS1,ipPool>
     */
    public static Map<String,IpPool> vps_detail = new HashMap<>();

    /**
     * 存放 IP 使用记录
     *    格式：  <IP1，<手机1，时间1>>
     */
    public static Map<String,Map<String,Date>> use = new HashMap<>();

    /**
     * 存放 VPS 更换 计数器
     *    格式：   <VPS1,5>    <VPS2,3>
     */
    public static Map<String,Integer> vps_change = new HashMap<>();

    /**
     * 存放 VPS 状态    true:更换IP中，false:正常使用
     *    格式：   <VPS1,更换IP中>    <VPS2,false>
     */
    public static Map<String,Boolean> vps_state = new HashMap<>();

    public static String getIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (StringUtils.isNotEmpty(ip) && !"unKnown".equalsIgnoreCase(ip)) {
            //多次反向代理后会有多个ip值，第一个ip才是真实ip
            int index = ip.indexOf(",");
            if (index != -1) {
                return ip.substring(0, index);
            } else {
                return ip;
            }
        }
        ip = request.getHeader("X-Real-IP");
        if (StringUtils.isNotEmpty(ip) && !"unKnown".equalsIgnoreCase(ip)) {
            return ip;
        }
        return request.getRemoteAddr();
    }

    /**
     * 主动更换IP
     * @param flag  true:主动，false:被动（调用获取IP次数大于目标次数）
     * @param imei
     * @return
     */
    public boolean changIp(boolean flag,String imei) {
        /**
         * 根据手机识别码  找出 使用的IP 及 VPS
         */

        //手机使用的VPS
        String useVps = null;
        //手机使用的IP
        String useIp = null;
        //遍历 所有 IP 的 使用记录 ，找到 当前手机
        for(Map.Entry<String,Map<String,Date>> map :use.entrySet()){
            //使用该IP的手机集合
            Map<String, Date> value = map.getValue();
            //遍历集合
            for(Map.Entry<String,Date> val : value.entrySet()){
                //记录的手机
                String phoneImei = val.getKey();
                //比对，是否是当前手机
                if(phoneImei.equalsIgnoreCase(imei)){
                    //手机当前IP
                    useIp = map.getKey();
                    //是当前手机，遍历 VPS ，根据IP找到对应的VPS
                    for(Map.Entry<String,String> vps1 : vps.entrySet()){
                        //vps1 当前 IP
                        String vpsCurrentIP = vps1.getValue();
                        if(useIp.equalsIgnoreCase(vpsCurrentIP)){
                            useVps = vps1.getKey();
                            break;
                        }
                    }
                }
            }
        }

        //如果没有使用记录，直接返回失败
        if(null == useIp && null == useVps){
            return false;
        }

        //获取当前VPS的 目标数量
        Integer targetNum = vps_change.get(useVps);

        //获取当前VPS的 实际数量
        Map<String, Date> useRecord = use.get(vps.get(useVps));
        int currentNum = useRecord.size();

        log.info("手机[{}][{}]请求换VPS[{}]的IP，目标数量[{}]，实际数量[{}]",imei,flag==true?"主动":"被动",useVps,targetNum,currentNum);

        if(flag){
            currentNum = currentNum + 1;
        }
        if (currentNum > targetNum) {
            //发送更换IP请求
            changVpsIp(useIp);
            //清空计数
            useRecord.clear();
            return true;
        }
        return false;
    }

    /**
     * 向VPS发送更换IP的命令
     * @param vpsIp
     */
    public void changVpsIp(String vpsIp) {
        HttpClient httpCilent = HttpClients.createDefault();
        String url = "http://" + vpsIp + ":1819/amazon/changVpsIp";
        try {
            HttpGet httpGet = new HttpGet(url);
            //设置请求和传输超时时间
            RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(2000).setConnectTimeout(2000).build();
            httpGet.setConfig(requestConfig);
            log.info("发送GET请求到[{}]", url);
            httpCilent.execute(httpGet);
        } catch (SocketTimeoutException e) {
            log.warn("超时，说明向VPS[{}]发送更换IP的命令成功（VPS切换IP中）",vpsIp);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                ((CloseableHttpClient) httpCilent).close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static String host = "";

    public String getHost() {
        return host;
    }




    /**
     * 一启动就初始化一些东西
     * @param args
     * @throws Exception
     */
    @Override
    public void run(String... args) throws Exception {
        log.info("服务器启动中。。。");
        //获取所有VPS在数据库的最新IP，设置 VPS 最大使用次数
        List<IpPool> vpsNewIps = ipPoolService.getVpsNewIps();
        for(IpPool ipPool : vpsNewIps){
            vps_detail.put(ipPool.getVps(),ipPool);
            vps.put(ipPool.getVps(),ipPool.getIp());
            vps_change.put(ipPool.getVps(),changCount);
        }
        execShell(" /root/init-server.sh 127.0.0.1");
        log.info("服务器启动完成");
    }

    /**
     * 执行sh脚本
     * @param exec
     * @return
     */
    public String execShell(String exec){
        Runtime runtime = Runtime.getRuntime();
        String charsetName = "UTF-8";
        String result = null;
        try {
            log.info("exec shell is [{}]",exec);
            Process process = runtime.exec(exec);
            InputStream iStream = process.getInputStream();
            InputStreamReader iSReader = new InputStreamReader(iStream,charsetName);
            BufferedReader bReader = new BufferedReader(iSReader);
            String line = null;
            StringBuffer sb = new StringBuffer();
            while ((line = bReader.readLine()) != null) {
                sb.append(line);
            }
            iStream.close();
            iSReader.close();
            bReader.close();
            result  = new String(sb.toString().getBytes(charsetName));
        } catch (Exception e) {
            log.error("exec shell [{}] fail, result is [{}]",exec,e);
        }
        return result;
    }

}