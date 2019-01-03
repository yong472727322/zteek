package com.zteek.controller;

import com.alibaba.fastjson.JSON;
import com.zteek.entity.*;
import com.zteek.service.AccountService;
import com.zteek.service.TaskService;
import com.zteek.service.VmCountService;
import com.zteek.utils.Constant;
import com.zteek.utils.IPUtil;
import com.zteek.utils.ReadingAllCookiesBySql;
import com.zteek.utils.ReturnResult;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 任务
 */
@RestController
@RequestMapping("task")
public class TaskController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private TaskService taskService;
    @Autowired
    private AccountService accountService;
    @Value("${apk.path}")
    private String apkPath;
    @Autowired
    private VmCountService vmCountService;

    /**
     * 取国家
     * @return 对应的国家，如"US","JP"
     */
    @RequestMapping("getCountry")
    public ReturnResult getCountry(String imei){
        ReturnResult rr = new ReturnResult();
        if(StringUtils.isEmpty(imei)){
            rr.setCode("9999");
            rr.setMessage("fail");
            rr.setObject("imei不能为空");
            return rr;
        }
        AmazonTask amazonTask = new AmazonTask();
        String country = null;
        try {
            List<AmazonTask> taskList = taskService.selectCountry();
            if (taskList.size() > 1) {
                logger.info("手机[{}]获取国家", imei);
                int random = (int) (Math.random() * taskList.size());
                country = taskList.get(random).getCountry();
                amazonTask.setCountry(country);
                rr.setObject(amazonTask);
            } else {
                country = taskList.get(0).getCountry();
                amazonTask.setCountry(country);
                rr.setObject(amazonTask);
            }
        }catch (Exception e){
            logger.info("手机[{}]获取国家出错，原因：[{}]",imei,e);
            rr.setCode("9999");
            rr.setMessage("fail");
        }
        return rr;
    }



    /**
     * 取账号
     * @return object为空表示没有账号
     */
    @RequestMapping("getAccount")
    public ReturnResult getAccount(String imei,String country){
        if (StringUtils.isEmpty(country)) {
            country = "US";
        }
        ReturnResult rr = new ReturnResult();
        if(StringUtils.isEmpty(imei)){
            rr.setCode("9999");
            rr.setMessage("fail");
            rr.setObject("imei不能为空");
            return rr;
        }
        try{
            Account account = accountService.getAccount(imei,country);
            rr.setObject(account);
        }catch (Exception e){
            logger.info("手机[{}]获取账号出错，原因：[{}]",imei,e);
            rr.setCode("9999");
            rr.setMessage("fail");
        }
        return rr;
    }

    /**
     * 反馈账号注册结果
     *      id,imei,registResult（注册结果，0-失败，1-成功）
     * @return
     */
    @RequestMapping("registResult")
    public ReturnResult registResult(Account account){
        ReturnResult rr = new ReturnResult();
        if(null == account.getId()){
            rr.setCode("9999");
            rr.setMessage("fail");
            rr.setObject("ID不能为空");
            return rr;
        }
        if(StringUtils.isEmpty(account.getImei())){
            rr.setCode("9999");
            rr.setMessage("fail");
            rr.setObject("imei不能为空");
            return rr;
        }
        if(null == account.getRegistResult()){
            rr.setCode("9999");
            rr.setMessage("fail");
            rr.setObject("registResult不能为空（注册结果，0-失败，1-成功）");
            return rr;
        }
        logger.info("手机[{}]反馈账号[{}]注册结果[{}]",account.getImei(),account.getId(),account.getRegistResult());
        try{
            accountService.registResult(account);
        }catch (Exception e){
            logger.info("手机[{}]反馈账号[{}]注册结果[{}]出错，原因：[{}]",account.getImei(),account.getId(),account.getRegistResult(),e);
            rr.setCode("9999");
            rr.setMessage("fail");
        }
        return rr;
    }

    /**
     * 取任务
     * @return object为空表示没有任务
     */
    @RequestMapping("getTask")
    public ReturnResult getTask(HttpServletRequest request,String imei,String country){
        String ip = IPUtil.getIp(request);
        ReturnResult rr = new ReturnResult();
        if(StringUtils.isEmpty(imei)){
            rr.setCode("9999");
            rr.setMessage("fail");
            rr.setObject("imei不能为空");
            return rr;
        }
        try{
            logger.info("手机[{}]使用代理IP[{}]获取[{}]新任务",imei,ip,country);
            AmazonTaskRun atr = taskService.getTask(imei,ip,country);
            rr.setObject(atr);
        }catch (Exception e){
            logger.info("手机[{}]获取新任务出错，原因：[{}]",imei,e);
            rr.setCode("9999");
            rr.setMessage("fail");
        }
        return rr;
    }

    /**
     * 结束任务
     * @param atr
     *          必须：   imei,id,taskId,resultMessage,resultCode
     * @return
     */
    @RequestMapping("endTask")
    public ReturnResult endTask(AmazonTaskRun atr){
        ReturnResult rr = new ReturnResult();
        if(null == atr.getId()){
            rr.setCode("9999");
            rr.setMessage("fail");
            rr.setObject("ID不能为空");
            return rr;
        }
        if(StringUtils.isEmpty(atr.getImei())){
            rr.setCode("9999");
            rr.setMessage("fail");
            rr.setObject("imei不能为空");
            return rr;
        }
        if(null == atr.getTaskId()){
            rr.setCode("9999");
            rr.setMessage("fail");
            rr.setObject("taskId不能为空");
            return rr;
        }
        if(StringUtils.isEmpty(atr.getResultMessage())){
            rr.setCode("9999");
            rr.setMessage("fail");
            rr.setObject("resultMessage不能为空");
            return rr;
        }
        if(null == atr.getTaskId()){
            rr.setCode("9999");
            rr.setMessage("fail");
            rr.setObject("resultCode不能为空");
            return rr;
        }
        logger.info("手机[{}]反馈任务[{}]的结果[{}]",atr.getImei(),atr.getId(),atr.getResultMessage());
        try{
            taskService.endTask(atr);
        }catch (Exception e){
            logger.info("手机[{}]反馈任务[{}]的结果[{}]出错，原因：[{}]",atr.getImei(),atr.getTaskId(),atr.getResultMessage(),e);
            rr.setCode("9999");
            rr.setMessage("fail");
        }
        return rr;
    }

    /**
     * 下载脚本
     * @param apkName
     * @return
     * @throws IOException
     */
    @RequestMapping("download")
    public ResponseEntity<byte[]> download(String apkName) throws IOException {
        if(StringUtils.isEmpty(apkName)){
            return null;
        }
        String path = apkPath + apkName;
        File file = new File(path);
        HttpHeaders headers = new HttpHeaders();
        String fileName= null;
        try {
            //为了解决中文名称乱码问题
            fileName = new String(apkName.getBytes("UTF-8"),"iso-8859-1");
        } catch (UnsupportedEncodingException e) {
            logger.warn("文件[{}]名称编码失。.",apkName);
        }
        headers.setContentDispositionFormData("attachment", fileName);
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        try {
            return new ResponseEntity<>(FileUtils.readFileToByteArray(file), headers, HttpStatus.CREATED);
        } catch (FileNotFoundException e) {
            logger.warn("文件[{}]不存在",apkName);
        } catch (IOException e) {
            logger.warn("文件[{}]下载失败。原因：[{}]",apkName,e);
        }
        return null;
    }



    /**
     * PC端随机获取任务
     * @return object为空表示没有任务
     */
    @RequestMapping("getTaskForPC")
    public ReturnResult getTaskForPC(String Country){
        ReturnResult rr = new ReturnResult();
        try{
            AmazonTask atr = taskService.getTaskForPC(Country);
            if(null != atr){
                rr.setObject(atr);
            }else {
                rr.setCode("9999");
                rr.setMessage("fail");
            }
        }catch (Exception e){
            rr.setCode("9999");
            rr.setMessage("fail");
        }
        return rr;
    }

    /**
     * PC端根据任务名称获取任务
     * @return object为空表示没有任务
     */
    @RequestMapping("getTaskForPCByName")
    public ReturnResult getTaskForPCByName(String nameAndCountry){
        ReturnResult rr = new ReturnResult();
        String[] nameCountry = nameAndCountry.split("@@@");
        String taskName = nameCountry[0];
        String Country = nameCountry[1];
        try{
            AmazonTask atr = taskService.getTaskForPCByName(taskName,Country);
            if(null != atr){
                rr.setObject(atr);
            }else {
                rr.setCode("9999");
                rr.setMessage("fail");
            }
        }catch (Exception e){
            rr.setCode("9999");
            rr.setMessage("fail");
        }
        return rr;
    }

    /**
     * PC端随机获取1000个Cookie
     * @return object为空表示没有Cookie
     */
    @RequestMapping("getCookies")
    public ReturnResult getCookies(){
        ReturnResult rr = new ReturnResult();
        try{
            List<Cookies> Cookies = taskService.getCookies();
            if(null != Cookies){
                rr.setObject(Cookies);
                //将已取出的cookie设置下一次使用时间为三天后
                //三天后的时间
                Date date = new Date();
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                calendar.add(Calendar.DAY_OF_MONTH, +3);
                date = calendar.getTime();
                for (com.zteek.entity.Cookies cookie : Cookies) {
                    taskService.updateCookieNextTime(cookie.getId(),date);
                }
            }else {
                rr.setCode("9999");
                rr.setMessage("fail");
            }
        }catch (Exception e){
            rr.setCode("9999");
            rr.setMessage("fail");
        }
        return rr;
    }

    /**
     * PC端获取指定虚拟机信息
     * @return object为空表示没有信息
     */
    @RequestMapping("getCountryAndTaskName")
    public ReturnResult getCountryAndTaskName(String userName){
        ReturnResult rr = new ReturnResult();
        try{
            VmCount vmCount = vmCountService.getCountryAndTaskName(userName);
            if(null != vmCount){
                rr.setObject(vmCount);
            }else {
                rr.setCode("9999");
                rr.setMessage("fail");
            }
        }catch (Exception e){
            rr.setCode("9999");
            rr.setMessage("fail");
        }
        return rr;
    }

    /**
     * PC端更改任务执行次数
     * @return object为空表示没有任务
     */
    @RequestMapping("updateCount")
    public ReturnResult updateCount(String countAndId){
        ReturnResult rr = new ReturnResult();
        try{
            String[] countId = countAndId.split("@@@");
            //执行次数
            //Integer count=Integer.valueOf(countId[1]);
            //任务id
            Long taskId = Long.valueOf(countId[0]);
            if(null != taskId){
                //查询出数据库中当前的已执行次数
                //AmazonTaskRun amazonTaskRun = taskService.findtaskForId(taskId);
                //已执行次数加上此次已执行次数
                //count=amazonTaskRun.getCount()+1;
                taskService.updateCount(taskId);
            }else {
                rr.setCode("9999");
                rr.setMessage("fail");
            }
        }catch (Exception e){
            rr.setCode("9999");
            rr.setMessage("fail");
        }
        return rr;
    }

    /**
     * PC端更改任务执行次数
     * @return object为空表示没有任务
     */
    @RequestMapping("updateSginCount")
    public ReturnResult updateSginCount(String sginCountAndId){
        ReturnResult rr = new ReturnResult();
        try{
            String[] countId = sginCountAndId.split("@@@");
            //任务id
            Long taskId = Long.valueOf(countId[0]);
            if(null != taskId){
                taskService.updateSginCount(taskId);
            }else {
                rr.setCode("9999");
                rr.setMessage("fail");
            }
        }catch (Exception e){
            rr.setCode("9999");
            rr.setMessage("fail");
        }
        return rr;
    }


    /**
     * PC端更改各虚拟机执行次数
     * @return object为空表示没有任务
     */
    @RequestMapping("updateVmCount")
    public ReturnResult updateVmCount(String vmCount){
        ReturnResult rr = new ReturnResult();
        try{
            String[] vmCounts = vmCount.split("@@@");
            //该虚拟机执行的asin
            String asin = vmCounts[0];
            //虚拟机名称
            String userName = vmCounts[1];
            if(null != userName){
                vmCountService.updateVmCount(asin,userName);
            }else {
                rr.setCode("9999");
                rr.setMessage("fail");
            }
        }catch (Exception e){
            rr.setCode("9999");
            rr.setMessage("fail");
        }
        return rr;
    }

    /**
     * PC端更改各虚拟机执行状态
     * @return object为空表示没有任务
     */
    @RequestMapping("updateVmStatus")
    public ReturnResult updateVmStatus(String vmCount){
        ReturnResult rr = new ReturnResult();
        try{
            String[] vmCounts = vmCount.split("@@@");
            //该虚拟机的状态
            Integer vmStatus = Integer.valueOf(vmCounts[1]);
            //虚拟机名称
            String userName = vmCounts[0];
            //异常信息
            String message = vmCounts[2];
            //虚拟机跑的关键字
            String keyword = vmCounts[3];
            if(null != userName){
                vmCountService.updateVmStatus(vmStatus,userName,message,keyword);
            }else {
                rr.setCode("9999");
                rr.setMessage("fail");
            }
        }catch (Exception e){
            rr.setCode("9999");
            rr.setMessage("fail");
        }
        return rr;
    }

    /**
     * PC端更改各虚拟机重启状态为已重启
     * @return object为空表示没有任务
     */
    @RequestMapping("updateRestartStatus")
    public ReturnResult updateRestartStatus(String userName){
        ReturnResult rr = new ReturnResult();
        try{
            if(null != userName){
                vmCountService.updateRestartStatus(userName);
            }else {
                rr.setCode("9999");
                rr.setMessage("fail");
            }
        }catch (Exception e){
            rr.setCode("9999");
            rr.setMessage("fail");
        }
        return rr;
    }

    /**
     * PC端更改各虚拟机重启状态为已重启
     * @return object为空表示没有任务
     */
    @RequestMapping("updateWarStatus")
    public ReturnResult updateWarStatus(String userName){
        ReturnResult rr = new ReturnResult();
        try{
            if(null != userName){
                vmCountService.updateWarStatus(userName);
            }else {
                rr.setCode("9999");
                rr.setMessage("fail");
            }
        }catch (Exception e){
            rr.setCode("9999");
            rr.setMessage("fail");
        }
        return rr;
    }


}
