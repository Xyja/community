package com.newcoder.community.controller.intercepter;

import com.newcoder.community.entity.User;
import com.newcoder.community.service.MessageService;
import com.newcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Yongjiu, X
 * @create 2022-08-02 17:41
 */
@Component
public class MessageInterceptor implements HandlerInterceptor {

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private MessageService messageService;

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        User user = hostHolder.getUser();
        if (user != null && modelAndView != null){
            int letterUnreadCount = messageService.findUnreadLettersCount(user.getId(),null);
            int noticeUnreadCount = messageService.findUnreadNoticeCount(user.getId(),null);
            modelAndView.addObject("allUnreadCount",letterUnreadCount+noticeUnreadCount);
        }
    }
}