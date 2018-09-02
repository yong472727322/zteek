package com.zteek.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Component
public class IPUtil  {

    /**
     * 默认 重拨 计数器
     */
    private static final int DEFAULT_COUNT = 5;

    private int changCount = DEFAULT_COUNT;
    private Set<String> changIpSet = new HashSet<>();

    public void setChangCount(int cc){
        if(cc > 0){
            this.changCount = cc;
        }else {
            this.changCount = DEFAULT_COUNT;
        }

    }
    public int getChangCount(){
        return changCount;
    }

    public boolean changIp(String imei, String ip){
        changIpSet.add(imei);
        if(changIpSet.size() >= changCount){
            //发送更换IP请求
            changVpsIp(ip);
            //清空计数
            changIpSet.clear();
            return true;
        }
        return false;
    }

    private void changVpsIp(String ip) {
        CloseableHttpClient httpCilent = HttpClients.createDefault();//Creates CloseableHttpClient instance with default configuration.
        HttpGet httpGet = new HttpGet("http://"+ip+":1819/amazon/changVpsIp");
        try {
            httpCilent.execute(httpGet);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                httpCilent.close();//释放资源
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static String host = "";

    public String getHost2(){
        return host;
    }

    public String getHost(){
        Runtime runtime = Runtime.getRuntime();
        String charsetName = "UTF-8";
//        charsetName = "GBK";
        String result = null;
        try {
            System.out.println("=================get proxy ip：");
            Process process = runtime.exec("  /root/get-ip.sh ");
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
            System.out.println("网络异常："+e.getMessage());
            e.printStackTrace();
        }
        return result;
    }


    public static String getIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if(StringUtils.isNotEmpty(ip) && !"unKnown".equalsIgnoreCase(ip)){
            //多次反向代理后会有多个ip值，第一个ip才是真实ip
            int index = ip.indexOf(",");
            if(index != -1){
                return ip.substring(0,index);
            }else{
                return ip;
            }
        }
        ip = request.getHeader("X-Real-IP");
        if(StringUtils.isNotEmpty(ip) && !"unKnown".equalsIgnoreCase(ip)){
            return ip;
        }
        return request.getRemoteAddr();
    }


//    @Scheduled(cron = "0/30 * * * * ?")
    public void testVPN() {
        getNetworkState("www.GOOGLE.com");
    }

    @Scheduled(cron = "0 0 * * * ?")
    public void getNewHost() {
        System.out.println(new Date()+ " 开始切换IP");
        host = getHost();
        System.out.println(new Date()+ " 获取IP成功");
    }


    private void getNetworkState(String ip) {
        Runtime runtime = Runtime.getRuntime();
        String charsetName = "UTF-8";
//        charsetName = "GBK";
        try {
            System.out.println("=================test ip："+ip);
            Process process = runtime.exec("ping " +ip + "  -c 5 ");
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
            String result  = new String(sb.toString().getBytes(charsetName));
            System.out.println("ping result:"+result);
            if (!StringUtils.isBlank(result)) {
                if (result.indexOf("TTL") > 0 || result.indexOf("ttl") > 0) {
                    System.out.println("success,time:" + new Date());
                } else {
                    System.out.println("fail,time:" + new Date());

                }
            }
        } catch (Exception e) {
            System.out.println("网络异常："+e.getMessage());
            e.printStackTrace();
        }
    }

//    @Override
//    public void run(String... args) throws Exception {
//        System.out.println("初始化获取代理IP");
//        host = getHost();
//        System.out.println("获取代理IP成功，IP为：["+host+"]");
//    }

    public void setHost(String host2) {
        host = host2;
    }
}
