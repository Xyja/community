package com.newcoder.community.controller.intercepter;

import com.newcoder.community.entity.LoginTicket;
import com.newcoder.community.entity.User;
import com.newcoder.community.service.UserService;
import com.newcoder.community.util.CookieUtil;
import com.newcoder.community.util.HostHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

/**
 * @author Yongjiu, X
 * @create 2022-07-14 21:20
 */
@Component
public class LoginTicketInterceptor implements HandlerInterceptor {

    //在请求开始，就根据ticket去查询用户信息 因为在请求过程中 随时随地可能用到用户信息

    private static final Logger logger = LoggerFactory.getLogger(LoginTicketInterceptor.class);

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //因为 HandlerInterceptor 接口定义好了方法 不方便用@cookieValue
        //而后面很多拦截器都需要用到request获取cookie里面的ticket 我们这里把获取cookie值的方法做一个封装
        //方便以后复用

        //从Cookie中获取凭证
        String ticket = CookieUtil.getValue(request,"ticket");
        if (ticket != null){
            //查询凭证
            LoginTicket loginTicket =  userService.findLoginTicket(ticket);
            //检查凭证是否有效 如果无效就让重新登录
            if (loginTicket != null && loginTicket.getStatus() == 0 && loginTicket.getExpired().after(new Date())){
                //根据凭证查询用户
                User user = userService.findUserById(loginTicket.getUserId());
                //让本次请求持有用户
                //每一次请求是服务器给一个线程处理 为了保证这些线程互不干扰 保证这个操作的隔离性
                //把这些访问user 放到一个线程里
                hostHolder.setUser(user);
            }
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        User user = hostHolder.getUser();
        if (user != null && modelAndView != null){
            modelAndView.addObject("loginUser",user);
        }
    }

    //在整个请求结束之后清理掉线程里面的数据
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        hostHolder.clear();
    }
}
