package com.zteek.utils;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.annotation.Scheduled;
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

    private static Logger log = LoggerFactory.getLogger(IPUtil.class);
    /**
     * 默认 重拨 计数器
     */
    private static final int DEFAULT_COUNT = 5;

    /**
     * 更换IP标识，true：VPS正在重新拨号
     */
    public static boolean changIpFlag = false;

    private int changCount = DEFAULT_COUNT;
    private Set<String> changIpSet = new HashSet<>();
    private Set<String> changIpSet2 = new HashSet<>();

    /**
     * 存放 VPS 及其 对应的 IP
     *  格式： <VPS1,IP1>,<VPS2,IP3>
     */
    private Map<String,String> vps = new HashMap<>();

    /**
     * 存放 IP 使用记录
     *    格式：  <IP1，<手机1，时间1>>
     */
    private Map<String,Map<String,String>> use = new HashMap<>();

    /**
     * 存放 VPS 更换 计数器
     *    格式：   <VPS1,5>    <VPS2,3>
     */
    private Map<String,String> vps_change = new HashMap<>();

    /**
     * 当前IP
     */
    public static String currentIp = "";

    public void setChangCount(int cc) {
        if (cc > 0) {
            this.changCount = cc;
        } else {
            this.changCount = DEFAULT_COUNT;
        }

    }

    public int getChangCount() {
        return changCount;
    }

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

    public boolean changIp(String imei) {
        changIpSet.add(imei);
        if (changIpSet.size() > changCount) {
            log.info("调用获取IP次数[{}]大于目标次数[{}]。换IP",changIpSet.size(),changCount);
            changIpFlag = true;
            //发送更换IP请求
            changVpsIp(currentIp);
            //清空计数
            changIpSet.clear();
            changIpSet2.clear();
            return true;
        }
        return false;
    }

    public boolean changIp2(String imei) {
        changIpSet2.add(imei);
        log.info("手机[{}]主动请求换IP，目标数量[{}]，实际数量[{}]",imei,changCount,changIpSet2.size());
        if (changIpSet2.size() >= changCount) {
            changIpFlag = true;
            //发送更换IP请求
            changVpsIp(currentIp);
            //清空计数
            changIpSet.clear();
            changIpSet2.clear();
            return true;
        }
        return false;
    }

    public void changVpsIp(String ip) {
        HttpClient httpCilent = HttpClients.createDefault();        //Creates CloseableHttpClient instance with default configuration.
        String url = "http://" + ip + ":1819/amazon/changVpsIp";
        try {
            HttpGet httpGet = new HttpGet(url);
            RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(2000).setConnectTimeout(2000).build();//设置请求和传输超时时间
            httpGet.setConfig(requestConfig);
            log.info("发送GET请求到[{}]", url);
            httpCilent.execute(httpGet);
        } catch (SocketTimeoutException e) {
            log.warn("chang ip success");
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


    public int getCurrentCount() {
        return changIpSet.size();
    }



    /**
     * 一启动就初始化一些东西
     * @param args
     * @throws Exception
     */
    @Override
    public void run(String... args) throws Exception {
        log.info("服务器启动中。。。");
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