package com.zteek.admin;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zteek.entity.*;
import com.zteek.exception.BusinessException;
import com.zteek.service.*;
import com.zteek.utils.Constant;
import com.zteek.utils.ReturnResult;
import com.zteek.utils.TaskMapManager;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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
    @Autowired
    private VmCountService vmCountService;
    @Autowired
    private AccountService accountService;

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
            //task.setSite("US");
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
        //根据id，查询出任务对应的国家
        AmazonTask amazonTask = taskService.selectCountryById(id);
        if(i > 0){
            //清空 内存中的任务
            TaskMapManager.getInstance().getTask(amazonTask.getCountry()).clear();
            //重新 从数据库中查询
            List<AmazonTask> tasks = taskService.getTasks(Constant.max_task_num,amazonTask.getCountry());
            for(AmazonTask task : tasks){
                TaskMapManager.getInstance().getTask(amazonTask.getCountry()).add(task);
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
        String country = null;
        List<AmazonTask> taskList = taskService.selectCountry();
        if (taskList.size()>1) {
            int random = (int) ( Math.random () * taskList.size());
            country = taskList.get(random).getCountry();
        }else {
            country = taskList.get(0).getCountry();
        }
        if(i > 0){
            //清空 内存中的任务
            TaskMapManager.getInstance().getTask(country).clear();
            //重新 从数据库中查询
            List<AmazonTask> tasks = taskService.getTasks(Constant.max_task_num,country);
            for(AmazonTask task : tasks){
                TaskMapManager.getInstance().getTask(country).add(task);
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





    @GetMapping("toPCTaskAdd")
    public String toPCTaskAdd(){
        return "pcTaskAdd";
    }
    /**
     * 添加PC端任务
     * @param task
     * @return
     */
    @ResponseBody
    @PostMapping("pcTaskAdd")
    public Integer pcTaskAdd(HttpServletRequest request, AmazonTaskRun task){
        int i = 0;
        try{
            logger.info("添加PC端任务，任务参数[{}]",task);
            if(StringUtils.isNotEmpty(task.getProductName())){
                task.setProductName(task.getProductName().trim());
            }
            i = taskService.insertPCTask(task);
            logger.info("添加PC端任务[{}]，结果：[{}]",task.getAsin(),i);
        }catch (Exception e){
            logger.error("添加PC端任务出错，原因:[{}]",e);
        }
        return i;
    }

    /**
     * 编辑PC端任务
     * @param task
     * @return
     */
    @ResponseBody
    @PostMapping("pcTaskUpdate")
    public void pcTaskUpdate(HttpServletRequest request, AmazonTaskRun task,Long id){
        try{
            logger.info("编辑PC端任务，任务参数[{}]",task);
            if(id !=null && id > 0) {
                taskService.updateTask(task, id);
            }
            logger.info("编辑PC端任务[{}]，结果：[{}]",task.getAsin());
        }catch (Exception e){
            logger.error("编辑PC端任务出错，原因:[{}]",e);
        }
    }

    @ResponseBody
    @PostMapping("taskDelete/{id}")
    public String taskDelete(@PathVariable("id") Long id){
        String result = null;
        try{
            if (id !=null && id > 0) {
                taskService.deleteTask(id);
            }
            result="删除任务成功";
            logger.info("删除任务成功");
        }catch (Exception e){
            logger.error("删除任务失败，原因:[{}]",e);
            result="删除任务失败";
        }
        return result;
    }

    @GetMapping("taskUpdate/{id}")
    public String updateTask(@PathVariable("id")Long id, ModelMap map){
        map.addAttribute("id",id);
        AmazonTaskRun task=taskService.findtaskForId(id);
        map.addAttribute("task",task);
        return "pcTaskUpdate";
    }

    @GetMapping("addCountryAndTask/{id}")
    public String addCountryAndTask(@PathVariable("id")Long id, ModelMap map){
        map.addAttribute("id",id);
        VmCount vmCount=vmCountService.findVmForId(id);
        map.addAttribute("vmCount",vmCount);
        return "vmCountUpdate";
    }

    /**
     * 指定虚拟机任务
     * @param vmCount
     * @return
     */
    @ResponseBody
    @PostMapping(value = "vmCountUpdate")
    public void vmCountUpdate(HttpServletRequest request, VmCount vmCount){
        try{
            logger.info("指定虚拟机任务，任务参数[{}]",vmCount);
            if (vmCount!=null) {
                vmCountService.vmCountUpdate(vmCount);
            }
            logger.info("指定虚拟机任务[{}]，结果：[{}]",vmCount);
        }catch (Exception e){
            logger.error("指定虚拟机任务出错，原因:[{}]",e);
        }
    }

    @ResponseBody
    @GetMapping("updateStatus/{userName}")
    public String updateStatus(@PathVariable("userName")String userName){
        String result = null;
        try{
            if (userName !=null) {
                vmCountService.updateStatus(userName);
            }
            result="标记异常成功";
            logger.info("标记异常成功");
        }catch (Exception e){
            logger.error("标记异常失败，原因:[{}]",e);
            result="标记异常失败";
        }
        return result;
    }

    @GetMapping("pcTaskList")
    public String pcTaskList(){
        return "pcTaskList";
    }
    @ResponseBody
    @RequestMapping("pcTaskData")
    public Object pcTaskData(int iDisplayStart,int iDisplayLength){
        PageHelper.startPage((iDisplayStart/iDisplayLength)+1,iDisplayLength);
        List<AmazonTaskRun> taskList = taskService.getPCTaskList(null);
        PageInfo<AmazonTaskRun> pageInfo = new PageInfo<>(taskList);
        DatatablesView<AmazonTaskRun> dataView = new DatatablesView<>();
        dataView.setRecordsTotal(Integer.parseInt(pageInfo.getTotal()+""));
        dataView.setData(taskList);
        return dataView;
    }

    @GetMapping("vmList")
    public String vmList(){
        return "vmList";
    }

    @ResponseBody
    @RequestMapping("vmData")
    public Object vmData(int iDisplayStart,int iDisplayLength){
        PageHelper.startPage((iDisplayStart/iDisplayLength)+1,iDisplayLength);
        List<VmCount> vmList = vmCountService.getVmList(null);
        PageInfo<VmCount> pageInfo = new PageInfo<>(vmList);
        DatatablesView<VmCount> dataView = new DatatablesView<>();
        dataView.setRecordsTotal(Integer.parseInt(pageInfo.getTotal()+""));
        dataView.setData(vmList);
        return dataView;
    }

    @GetMapping("toVmAdd")
    public String toVmAdd(){
        return "vmAdd";
    }
    /**
     * 添加虚拟机
     * @param vmCount
     * @return
     */
    @ResponseBody
    @PostMapping("vmAdd")
    public Integer vmAdd(HttpServletRequest request, VmCount vmCount){
        int i = 0;
        try{
            logger.info("添加PC端任务，任务参数[{}]",vmCount);
            if(StringUtils.isNotEmpty(vmCount.getUserName())){
                vmCount.setUserName(vmCount.getUserName().trim());
            }
            i = vmCountService.insertVm(vmCount);
            logger.info("添加PC端任务[{}]，结果：[{}]",vmCount.getAsin(),i);
        }catch (Exception e){
            logger.error("添加PC端任务出错，原因:[{}]",e);
        }
        return i;
    }



    @GetMapping("countList")
    public String countList(){
        return "countList";
    }
    @ResponseBody
    @RequestMapping("countData")
    public Object countData(int iDisplayStart,int iDisplayLength){
        PageHelper.startPage((iDisplayStart/iDisplayLength)+1,iDisplayLength);
        List<Account> countList = accountService.getCountList(null);
        PageInfo<Account> pageInfo = new PageInfo<>(countList);
        DatatablesView<Account> dataView = new DatatablesView<>();
        dataView.setRecordsTotal(Integer.parseInt(pageInfo.getTotal()+""));
        dataView.setData(countList);
        return dataView;
    }

    @ResponseBody
    @GetMapping("openUS")
    public String openUS(){
        String result = null;
        try{
            accountService.openUS();
            result="开启美国账号成功";
            logger.info("开启美国账号成功");
        }catch (Exception e){
            logger.error("开启美国账号失败，原因:[{}]",e);
            result="开启美国账号失败";
        }
        return result;
    }

    @ResponseBody
    @GetMapping("closeUS")
    public String closeUS(){
        String result = null;
        try{
            accountService.closeUS();
            result="关闭美国账号成功";
            logger.info("关闭美国账号成功");
        }catch (Exception e){
            logger.error("关闭美国账号失败，原因:[{}]",e);
            result="关闭美国账号失败";
        }
        return result;
    }

    @ResponseBody
    @GetMapping("openJP")
    public String openJP(){
        String result = null;
        try{
            accountService.openJP();
            result="开启日本账号成功";
            logger.info("开启日本账号成功");
        }catch (Exception e){
            logger.error("开启日本账号失败，原因:[{}]",e);
            result="开启日本账号失败";
        }
        return result;
    }

    @ResponseBody
    @GetMapping("closeJP")
    public String closeJP(){
        String result = null;
        try{
            accountService.closeJP();
            result="关闭日本账号成功";
            logger.info("关闭日本账号成功");
        }catch (Exception e){
            logger.error("关闭日本账号失败，原因:[{}]",e);
            result="关闭日本账号失败";
        }
        return result;
    }
    @ResponseBody
    @PostMapping("restart/{id}")
    public String restart(@PathVariable("id")Long id){
        String result = null;
        try{
            if (id !=null && id > 0) {
                vmCountService.restart(id);
            }
            result="重启成功，将于五分钟内生效";
            logger.info("重启虚拟机成功");
        }catch (Exception e){
            logger.error("重启失败，原因:[{}]",e);
            result="重启虚拟机失败!!!!!!!!";
        }
        return result;
    }

    @ResponseBody
    @PostMapping("updateWar/{id}")
    public String updateWar(@PathVariable("id")Long id){
        String result = null;
        try{
            if (id !=null && id > 0) {
                vmCountService.updateWar(id);
            }
            result="更新成功，将于五分钟内生效";
            logger.info("更新虚拟机成功");
        }catch (Exception e){
            logger.error("更新失败，原因:[{}]",e);
            result="更新虚拟机失败!!!!!!!";
        }
        return result;
    }

}
