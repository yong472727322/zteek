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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.UUID;

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
        File file = new File(path);
        HttpHeaders headers = new HttpHeaders();
        String fileName=new String(apkName.getBytes("UTF-8"),"iso-8859-1");     //为了解决中文名称乱码问题
        headers.setContentDispositionFormData("attachment", fileName);
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        return new ResponseEntity<>(FileUtils.readFileToByteArray(file), headers, HttpStatus.CREATED);
    }


    @RequestMapping(value="/fileUpLoad")
    public String testUpload(HttpServletRequest request, @RequestParam(value="desc",required=false) String desc, @RequestParam("photo") CommonsMultipartFile file) throws Exception{
        ServletContext servletContext = request.getServletContext();//获取ServletContext的对象 代表当前WEB应用
        String realPath = servletContext.getRealPath("/uploads");//得到文件上传目的位置的真实路径
        System.out.println("realPath :"+realPath);
        File file1 = new File(realPath);
        if(!file1.exists()){
            file1.mkdir();   //如果该目录不存在，就创建此抽象路径名指定的目录。
        }
        String prefix = UUID.randomUUID().toString();
        prefix = prefix.replace("-","");
        String fileName = prefix+"_"+file.getOriginalFilename();//使用UUID加前缀命名文件，防止名字重复被覆盖

        InputStream in= file.getInputStream();;//声明输入输出流

        OutputStream out=new FileOutputStream(new File(realPath+"\\"+fileName));//指定输出流的位置;

        byte []buffer =new byte[1024];
        int len=0;
        while((len=in.read(buffer))!=-1){
            out.write(buffer, 0, len);
            out.flush();                //类似于文件复制，将文件存储到输入流，再通过输出流写入到上传位置
        }                               //这段代码也可以用IOUtils.copy(in, out)工具类的copy方法完成

        out.close();
        in.close();

        return "success";
    }

    /*
     *采用spring提供的上传文件的方法
     */
    @RequestMapping("springUpload")
    public String  springUpload(HttpServletRequest request) throws IllegalStateException, IOException
    {
        long  startTime=System.currentTimeMillis();
        //将当前上下文初始化给  CommonsMutipartResolver （多部分解析器）
        CommonsMultipartResolver multipartResolver=new CommonsMultipartResolver(
                request.getSession().getServletContext());
        //检查form中是否有enctype="multipart/form-data"
        if(multipartResolver.isMultipart(request))
        {
            //将request变成多部分request
            MultipartHttpServletRequest multiRequest=(MultipartHttpServletRequest)request;
            //获取multiRequest 中所有的文件名
            Iterator iter=multiRequest.getFileNames();

            while(iter.hasNext())
            {
                //一次遍历所有文件
                MultipartFile file=multiRequest.getFile(iter.next().toString());
                if(file!=null)
                {
                    String path= apkPath + file.getOriginalFilename();
                    //上传
                    file.transferTo(new File(path));
                }

            }

        }
        long  endTime=System.currentTimeMillis();
        System.out.println("方法三的运行时间："+String.valueOf(endTime-startTime)+"ms");
        return "/success";
    }





}
