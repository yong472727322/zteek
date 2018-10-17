package com.zteek.admin;

import com.alibaba.fastjson.JSONObject;
import com.zteek.entity.AmazonTask;
import com.zteek.entity.DatatablesView;
import com.zteek.entity.User;
import com.zteek.exception.BusinessException;
import com.zteek.service.TaskService;
import com.zteek.service.UserService;
import com.zteek.utils.Constant;
import com.zteek.utils.ReturnResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("admin")
public class AdminController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private UserService userService;
    @Autowired
    private TaskService taskService;
    @Value("${apk.path}")
    private String apkPath;

    /**
     * 转到 登陆页面
     * @return
     */
    @GetMapping("toLogin")
    public String login(HttpServletRequest request){
        //登陆过，不再重新登陆
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute(Constant.user);
        if(null != user){
            logger.info("用户[{}]，已经是登陆状态，重定向到首页",user.getId());
            return "redirect:index";
        }
        return "login";
    }

    /**
     * 登出
     * @return
     */
    @GetMapping("logout")
    public String logout(HttpServletRequest request){
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute(Constant.user);
        session.removeAttribute(Constant.user);
        logger.info("用户[{}]登出，重定向到登陆页面",user.getId());
        return "redirect:toLogin";
    }

    /**
     * 转到 首页
     * @return
     */
    @GetMapping("index")
    public String index(HttpServletRequest request,ModelMap map) {

        List<AmazonTask> list = taskService.indexChart(null);
        map.addAttribute("chartData",list);
        list = taskService.indexChart(1);
        map.addAttribute("failChartData",list);
        list = taskService.indexChart(2);
        map.addAttribute("successChartData",list);

        for(int i = 0 ; i < 10 ; i ++){
            Map<String,Integer> map1 = taskService.indexTable(i);
            for(Map.Entry<String,Integer> map2 : map1.entrySet()){
                map.addAttribute(map2.getKey()+"",map2.getValue()+"");
            }
        }

        return "index";
    }



    /**
     * 登陆
     * @param username
     * @param password
     * @return
     */
    @ResponseBody
    @PostMapping("login")
    public ReturnResult login(HttpServletRequest request,String username, String password){
        logger.info("用户[{}]登陆系统。",username);
        ReturnResult rr = new ReturnResult();
        try{
            User user = userService.login(username,password);
            HttpSession session = request.getSession();
            session.setAttribute(Constant.user,user);
            rr.setObject(user);
        }catch (BusinessException be){
            logger.warn("用户[{}]登陆系统失败，原因[{}]",username,be.getMessage());
            rr.setCode(Constant.CODE_FAIL);
            rr.setMessage(Constant.FAIL);
            rr.setObject(be.getMessage());
        }catch (Exception e){
            logger.error("用户[{}]登陆系统出错，原因[{}]",username,e);
            rr.setCode(Constant.CODE_FAIL);
            rr.setMessage(Constant.FAIL);
            rr.setObject("系统错误。");
        }
        return rr;
    }


    /**
     * 转到 上传APK页面
     * @return
     */
    @GetMapping("file")
    public String file() {
        return "file";
    }

    @PostMapping("/upload")
    public String singleFileUpload(@RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            return "redirect:/admin/index";
        }

        try {
            String filename = file.getOriginalFilename();
            logger.info("上传文件[{}],大小[{}]MB",filename,file.getSize()/1000/1000);
            // Get the file and save it somewhere
            byte[] bytes = file.getBytes();
            Path path = Paths.get(apkPath + filename);
            Files.write(path, bytes);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return "redirect:/admin/index";
    }


    @GetMapping("taskList")
    public String taskList(){
        return "taskList";
    }


    @ResponseBody
    @RequestMapping("taskData")
    public Object taskData(){
        List<AmazonTask> taskList = taskService.getTaskList(null);
        for(AmazonTask task : taskList){
            String args = task.getArgs();
            JSONObject json = (JSONObject) JSONObject.parse(args);
            task.setKeyword(json.get("keyword")+"");
            task.setKeyword1(json.get("keyword1")+"");
            task.setKeyword2(json.get("keyword2")+"");
            task.setProductName(json.get("productName")+"");
        }
        DatatablesView<AmazonTask> dataView = new DatatablesView<>();
        dataView.setRecordsTotal(taskList.size());
        dataView.setData(taskList);
        return dataView;
    }
    @GetMapping("toTaskAdd")
    public String toTaskAdd(){
        return "taskAdd";
    }

    /**
     * 添加任务
     * @param task
     * @return
     */
    @ResponseBody
    @PostMapping("taskAdd")
    public Integer taskAdd(HttpServletRequest request,AmazonTask task){
        int i = 0;
        try{
            logger.info("添加任务，任务参数[{}]",task);
            Map<String,Object> param = new HashMap<>(7);
            param.put("keyword",task.getKeyword());
            param.put("keyword1",task.getKeyword1());
            param.put("keyword2",task.getKeyword2());
            param.put("productName",task.getProductName());

            String s = JSONObject.toJSONString(param);
            task.setArgs(s);
            task.setSite("US");
            HttpSession session = request.getSession();
            User user = (User) session.getAttribute(Constant.user);
            task.setCreatedBy(user.getName());
            i = taskService.insertTask(task);
            logger.info("用户[{}]添加任务[{}]，结果：[{}]",user.getName(),task.getAsin(),i);
        }catch (Exception e){
            logger.error("添加任务出错，原因:[{}]",e);
        }
        return i;
    }
}
