package com.zteek.controller;

import com.zteek.entity.Account;
import com.zteek.entity.AmazonTaskRun;
import com.zteek.service.AccountService;
import com.zteek.service.TaskService;
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
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

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

    /**
     * 取账号
     * @return object为空表示没有账号
     */
    @RequestMapping("getAccount")
    public ReturnResult getAccount(String imei){
        ReturnResult rr = new ReturnResult();
        if(StringUtils.isEmpty(imei)){
            rr.setCode("9999");
            rr.setMessage("fail");
            rr.setObject("imei不能为空");
            return rr;
        }
        logger.info("手机[{}]获取账号",imei);
        try{
            Account account = accountService.getAccount(imei);
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
    public ReturnResult getTask(String imei){
        ReturnResult rr = new ReturnResult();
        if(StringUtils.isEmpty(imei)){
            rr.setCode("9999");
            rr.setMessage("fail");
            rr.setObject("imei不能为空");
            return rr;
        }
        logger.info("手机[{}]获取新任务",imei);
        try{
            AmazonTaskRun atr = taskService.getTask(imei);
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
        File file = null;
        try {
            file = new File(path);
        }catch (Exception e){
            logger.warn("文件[{}]不存在",apkName);
            return null;
        }
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
        } catch (IOException e) {
            logger.warn("文件[{}]下载失败。原因：[{}]",apkName,e);
        }
        return null;
    }


}
