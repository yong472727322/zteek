package com.zteek.utils;

import com.zteek.entity.AmazonTask;
import com.zteek.entity.IpPool;
import com.zteek.service.IpPoolService;
import com.zteek.service.TaskService;
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
import java.net.NoRouteToHostException;
import java.net.SocketTimeoutException;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Component
public class IPUtil implements CommandLineRunner {

    @Autowired
    private IpPoolService ipPoolService;
    @Autowired
    private TaskService taskService;

    private static Logger log = LoggerFactory.getLogger(IPUtil.class);
    /**
     * 默认 重拨 计数器
     */
    public static final int DEFAULT_COUNT = 5;

    /**
     * 更换IP标识，true：VPS正在重新拨号
     */
//    public static boolean changIpFlag = false;

    private int changCount = DEFAULT_COUNT;


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
//  todo      换代理之前，先判断有没有未使用过的代理
        /**
         * 根据手机识别码  找出 使用的IP 及 VPS
         */

        //手机使用的VPS
        String useVps = null;
        //手机使用的IP
        String useIp = null;
        //遍历 所有 IP 的 使用记录 ，找到 当前手机
        for(Map.Entry<String,Map<String,Date>> map : Constant.use.entrySet()){
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
                    for(Map.Entry<String,String> vps : Constant.vps.entrySet()){
                        //vps1 当前 IP
                        String vpsCurrentIP = vps.getValue();
                        if(useIp.equalsIgnoreCase(vpsCurrentIP)){
                            useVps = vps.getKey();
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
        Integer targetNum = Constant.vps_change.get(useVps);

        //获取当前VPS的 实际数量
        Map<String, Date> useRecord = Constant.use.get(Constant.vps.get(useVps));
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
        } catch (NoRouteToHostException e) {
            log.warn("主机[{}]访问失败，可能VPS正在切换中。。。[{}]",vpsIp,e.getMessage());
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
            //todo 验证VPS 是否可用 使用最新的IP发送 GET请求 到 VPS 如果通，说明可用，否则，不可用，不加入
            Constant.vps_detail.put(ipPool.getVps(),ipPool);
            Constant.vps.put(ipPool.getVps(),ipPool.getIp());
            Constant.vps_change.put(ipPool.getVps(),changCount);
        }
        List<AmazonTask> tasks = taskService.getTasks(Constant.max_task_num);
        for(AmazonTask task : tasks){
            Constant.tasks.add(task);
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