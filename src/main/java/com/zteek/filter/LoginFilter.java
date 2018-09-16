package com.zteek.filter;

import com.zteek.entity.User;
import com.zteek.utils.Constant;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * 登陆过滤器
 */
@Component
@ServletComponentScan
@WebFilter(urlPatterns = "/admin/*",filterName = "loginFilter")
public class LoginFilter implements Filter {

    //不用过虑的地址
    private static Set<String> GreenUrlSet = new HashSet<String>();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        //登陆页面
        GreenUrlSet.add("/admin/toLogin");
        GreenUrlSet.add("/admin/login");
    }

    @Override
    public void doFilter(ServletRequest arg0, ServletResponse arg1, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest)arg0;
        HttpServletResponse response = (HttpServletResponse)arg1;
        String requestURI = request.getRequestURI();
        if(needLogin(requestURI)) {
            //判断是否已经登陆了
            HttpSession session = request.getSession();
            User user = (User) session.getAttribute(Constant.user);
            if(null == user){
                // 需要登录则跳转到登录Controller
                response.sendRedirect("toLogin");
                return;
            }
        }
        chain.doFilter(arg0, arg1);
    }

    @Override
    public void destroy() {

    }



    /**
     * 判断是否需要登录
     * @param uri
     * @return
     */
    private boolean needLogin(String uri) {
        //放行不需要验证的地址
        if (GreenUrlSet.contains(uri)) {
            return false;
        }
        return true;
    }
}
