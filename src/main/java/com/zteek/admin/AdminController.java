package com.zteek.admin;

import com.zteek.entity.User;
import com.zteek.exception.BusinessException;
import com.zteek.service.UserService;
import com.zteek.utils.Constant;
import com.zteek.utils.ReturnResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("admin")
public class AdminController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private UserService userService;

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
    public String index(){
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

}
