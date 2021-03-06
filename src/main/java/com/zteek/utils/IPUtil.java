package com.zteek.utils;

import com.zteek.entity.AmazonTask;
import com.zteek.entity.IpPool;
import com.zteek.service.IpPoolService;
import com.zteek.service.TaskService;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpHostConnectException;
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

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private IpPoolService ipPoolService;
    @Autowired
    private TaskService taskService;

    private static Logger log = LoggerFactory.getLogger(IPUtil.class);


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
     *
     * @param flag  true:主动，false:被动（调用获取IP次数大于目标次数）
     * @param imei
     * @param useIp
     * @return
     */
    public boolean changIp(boolean flag, String imei, String useIp) {
        /**
         * 根据手机识别码  找出 使用的IP 及 VPS
         */

        if (null == useIp) {
            log.warn("没有传代理IP，直接返回失败。");
            return false;
        }

        //手机使用的VPS
        String useVps = null;
        if (Constant.vps.size() > 0) {
            for (Map.Entry<String, String> vps : Constant.vps.entrySet()) {
                String value = vps.getKey();
                if (useIp.equalsIgnoreCase(vps.getValue())) {
                    useVps = value;
                }
            }
        } else {
            log.warn("VPS还没有加载到内存，直接返回失败。");
            return false;
        }


        //获取当前VPS的 目标数量
        Integer targetNum = Constant.vps_change.get(useVps);

        //获取当前VPS的 实际数量
        Map<String, Date> useRecord = Constant.use.get(useIp);
        if (null == useRecord) {
//            log.warn("获取当前IP[{}]的使用记录，结果为[null]，可能是服务器重启了或VPS刚好切换了新IP。",useIp);
            return false;
        }
        //清除 使用时间，标识为 切换IP
        useRecord.put(imei, null);
        log.info("清除手机[{}]使用IP[{}]的时间，标识为 切换IP", imei, useIp);
        int currentNum = 0;
        for (Map.Entry<String, Date> map : useRecord.entrySet()) {
            Date value = map.getValue();
            if (null == value) {
                currentNum++;
            }
        }

        log.info("手机[{}][{}]请求换VPS[{}]的IP[{}]，目标数量[{}]，实际数量[{}]", imei, flag == true ? "主动" : "被动", useVps, useIp, targetNum, currentNum);

        if (flag) {
            currentNum = currentNum + 1;
        }
        if (currentNum > targetNum) {
            if (null != useVps) {
                Boolean aBoolean = Constant.vps_state.get(useVps);
                if (aBoolean) {
                    log.warn("VPS[{}]正在切换IP中。。。", useVps);
                    return false;
                }
            }
            //判断该IP是否还有正在使用的手机，防止超过限制的手机还在使用就直接切换导致任务失败
            int count = 0;
            Map<String, Date> map = Constant.use.get(useIp);
            for (Map.Entry<String, Date> record : map.entrySet()) {
                String phone = record.getKey();
                Date startUseTime = record.getValue();
                if (null == startUseTime) {
                    count++;
                }
            }
            if (count < map.size()) {
                log.warn("IP[{}]还有手机正在使用中，不直接切换。", useIp);
                return false;
            }
            //发送更换IP请求
            changVpsIp(useIp);
            //清空计数
            useRecord.clear();
            //设置VPS不可用
            Constant.vps_state.put(useVps, true);
            log.info("设置VPS[{}]不可用", useVps);
            return true;
        }
        return false;
    }

    /**
     * 向VPS发送更换IP的命令
     *
     * @param vpsIp
     */
    public void changVpsIp(String vpsIp) {
        HttpClient httpCilent = HttpClients.createDefault();
        String url = "http://" + vpsIp + ":1819/amazon/changVpsIp";
        try {
            HttpGet httpGet = new HttpGet(url);
            //设置请求和传输超时时间
            RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(60000).setConnectTimeout(60000).build();
            httpGet.setConfig(requestConfig);
            log.info("发送GET请求到[{}]", url);
            httpCilent.execute(httpGet);
        } catch (NoRouteToHostException e) {
            log.warn("主机[{}]访问失败，可能VPS正在切换中。。。[{}]", vpsIp, e.getMessage());
        } catch (SocketTimeoutException e) {
            log.warn("Socket超时，说明向VPS发送更换IP的命令成功（VPS切换IP中）");
        } catch (ConnectTimeoutException e) {
            log.warn("Connect超时，说明向VPS发送更换IP的命令成功（VPS切换IP中）");
        } catch (HttpHostConnectException e) {
            log.warn("连接被拒绝，可能VPS在切换或者是旧VPS的无效IP。忽略。");
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
     *
     * @param args
     * @throws Exception

    @Override
    public void run(String... args) throws Exception {
        log.info("服务器启动中。。。");
        //获取所有VPS在数据库的最新IP，设置 VPS 最大使用次数
        List<IpPool> vpsNewIps = ipPoolService.getVpsNewIps();
        for (IpPool ipPool : vpsNewIps) {
            //todo 验证VPS 是否可用 使用最新的IP发送 GET请求 到 VPS 如果通，说明可用，否则，不可用，不加入
            Constant.vps_detail.put(ipPool.getVps(), ipPool);
            Constant.vps.put(ipPool.getVps(), ipPool.getIp());
            Constant.vps_change.put(ipPool.getVps(), Constant.changCount);
            Constant.vps_state.put(ipPool.getVps(), false);
        }
        String country = null;
        List<AmazonTask> taskList = taskService.selectCountry();
        if (taskList.size() > 1) {
            int random = (int) (Math.random() * taskList.size());
            country = taskList.get(random).getCountry();
        } else {
            country = taskList.get(0).getCountry();
        }
        //清空 内存中的任务
        TaskMapManager.getInstance().getTask(country).clear();
        //重新 从数据库中查询
        List<AmazonTask> tasks = taskService.getTasks(Constant.max_task_num, country);
        for (AmazonTask task : tasks) {
            TaskMapManager.getInstance().getTask(country).add(task);
        }
        execShell(" /root/init-server.sh 127.0.0.1");
        log.info("服务器启动完成");
    }
     */

    /**
     * 执行sh脚本
     *
     * @param exec
     * @return
     */
    public String execShell(String exec) {
        Runtime runtime = Runtime.getRuntime();
        String charsetName = "UTF-8";
        String result = null;
        try {
            log.info("exec shell is [{}]", exec);
            Process process = runtime.exec(exec);
            InputStream iStream = process.getInputStream();
            InputStreamReader iSReader = new InputStreamReader(iStream, charsetName);
            BufferedReader bReader = new BufferedReader(iSReader);
            String line = null;
            StringBuffer sb = new StringBuffer();
            while ((line = bReader.readLine()) != null) {
                sb.append(line);
            }
            iStream.close();
            iSReader.close();
            bReader.close();
            result = new String(sb.toString().getBytes(charsetName));
        } catch (Exception e) {
            log.error("exec shell [{}] fail, result is [{}]", exec, e);
        }
        return result;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("启动完成");
    }
}