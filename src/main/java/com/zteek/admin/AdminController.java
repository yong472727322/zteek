package com.zteek.admin;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zteek.entity.AmazonTask;
import com.zteek.entity.DatatablesView;
import com.zteek.entity.PhoneLog;
import com.zteek.entity.User;
import com.zteek.exception.BusinessException;
import com.zteek.service.PhoneService;
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
import java.math.BigDecimal;
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
    @Autowired
    private PhoneService phoneService;

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
            logger.info("用户[{}]，已经是登陆状态，重定向到首页",user.getUsername());
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
        logger.info("用户[{}]登出，重定向到登陆页面",user.getUsername());
        return "redirect:toLogin";
    }

    /**
     * 转到 首页
     * @return
     */
    @GetMapping("index")
    public String index(ModelMap map) {

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

    /**
     * ASIN 详情页
     * @param asin
     * @param map
     * @return
     */
    @GetMapping("taskDetail/{asin}")
    public String taskDetail(@PathVariable("asin") String asin,ModelMap map){
        AmazonTask task = taskService.findTaskByAsin(asin);
        if(null == task){
            //没有找到返回列表页
            return "taskList";
        }
        List<AmazonTask> list = taskService.asinChart(null,task.getId());
        map.addAttribute("chartData",list);
        list = taskService.asinChart(1,task.getId());
        map.addAttribute("failChartData",list);
        list = taskService.asinChart(2,task.getId());
        map.addAttribute("successChartData",list);
        map.addAttribute("asin",asin);

        //统计 信息

        map.addAttribute("runNum",task.getRunNum());
        map.addAttribute("runCompleted",task.getRunCompleted());
        map.addAttribute("remaining",task.getRemaining());

        Map<String, BigDecimal> successRate = taskService.taskSuccessRate(task.getId(), 1);
        map.addAttribute("todaySuccessRate",successRate.get("success_rate"));
        map.addAttribute("todayCompleted",successRate.get("success"));
        successRate = taskService.taskSuccessRate(task.getId(), 2);
        map.addAttribute("lastdaySuccessRate",successRate.get("success_rate"));
        map.addAttribute("lastdayCompleted",successRate.get("success"));
        successRate = taskService.taskSuccessRate(task.getId(), 3);
        map.addAttribute("successRate",successRate.get("success_rate"));
        map.addAttribute("completed",successRate.get("success"));

        Map<String, Integer> consume = taskService.taskConsuming(task.getId());
        map.addAttribute("maxConsume",consume.get("maxConsume"));
        map.addAttribute("minConsume",consume.get("minConsume"));
        map.addAttribute("avgConsume",consume.get("avgConsume"));

        return "taskDetail";
    }


    @ResponseBody
    @RequestMapping("taskData")
    public Object taskData(int iDisplayStart,int iDisplayLength){
        PageHelper.startPage((iDisplayStart/iDisplayLength)+1,iDisplayLength);
        List<AmazonTask> taskList = taskService.getTaskList(null);
        PageInfo<AmazonTask> pageInfo = new PageInfo<>(taskList);

        for(AmazonTask task : taskList){
            String args = task.getArgs();
            JSONObject json = (JSONObject) JSONObject.parse(args);
            task.setKeyword(json.get("keyword")+"");
            task.setKeyword1(json.get("keyword1")+"");
            task.setKeyword2(json.get("keyword2")+"");
            task.setProductName(json.get("productName")+"");
        }
        DatatablesView<AmazonTask> dataView = new DatatablesView<>();
        dataView.setRecordsTotal(Integer.parseInt(pageInfo.getTotal()+""));
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

    /**
     * 开始 任务
     * @param id
     * @return
     */
    @GetMapping("taskStart/{id}")
    public String taskStart(@PathVariable("id") Long id){
        int i = taskService.updateTaskStatusById(1,id);
        if(i > 0){
            //清空 内存中的任务
            Constant.tasks.clear();
            //重新 从数据库中查询
            List<AmazonTask> tasks = taskService.getTasks(Constant.max_task_num);
            for(AmazonTask task : tasks){
                Constant.tasks.add(task);
            }
        }
        return "redirect:/admin/taskList";
    }

    /**
     * 停止 任务
     * @param id
     * @return
     */
    @GetMapping("taskStop/{id}")
    public String taskStop(@PathVariable("id") Long id){
        int i = taskService.updateTaskStatusById(0,id);
        if(i > 0){
            //清空 内存中的任务
            Constant.tasks.clear();
            //重新 从数据库中查询
            List<AmazonTask> tasks = taskService.getTasks(Constant.max_task_num);
            for(AmazonTask task : tasks){
                Constant.tasks.add(task);
            }
        }
        return "redirect:/admin/taskList";
    }


    @GetMapping("toLog")
    public String toLog(ModelMap map){
        //查询最近100分钟内有日志的手机
        List<PhoneLog> phones = phoneService.getRecentlyLog(100);
        map.addAttribute("phones",phones);
        return "log";
    }
    /**
     * 查询手机日志
     * @param imei
     * @return
     */
    @ResponseBody
    @GetMapping("log")
    public ReturnResult log(String imei,Long id){
        ReturnResult rr = new ReturnResult();
        try{
            if(null == id){
                PhoneLog log = phoneService.getNewLogByImei(imei);
                rr.setObject(log);
            }else {
                List<PhoneLog> logs = phoneService.getNewLogs(imei,id);
                rr.setObject(logs);
            }
        }catch (BusinessException be){
            rr.setCode(Constant.CODE_FAIL);
            rr.setMessage(Constant.FAIL);
            rr.setObject(be.getMessage());
        }catch (Exception e){
            rr.setCode(Constant.CODE_FAIL);
            rr.setMessage(Constant.FAIL);
            rr.setObject("系统错误。");
        }
        return rr;
    }

}
