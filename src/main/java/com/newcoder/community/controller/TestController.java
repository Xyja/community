package com.newcoder.community.controller;

import com.newcoder.community.util.CommunityUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @author Yongjiu, X
 * @create 2022-06-26 17:19
 */
@Controller
public class TestController {

    @RequestMapping("/hello")
    @ResponseBody
    public String testHello(){
        return "hello,Spring Boot";
    }


    //cookie示例
    @RequestMapping(value = "/cookie/set",method = RequestMethod.GET)
    @ResponseBody
    public String setCookie(HttpServletResponse response){

        //创建Cookie
        Cookie cookie = new Cookie("code", CommunityUtil.generateUUID());
        //设置cookie生效的范围
        cookie.setPath("/community/cookie");
        //cookie默认关掉浏览器就消失  服务器发给浏览器  但是可以设置生存时间，长期有效
        //设置cookie的生存时间
        cookie.setMaxAge(60*10);
        response.addCookie(cookie);
        return "hello cookie";
    }

    @RequestMapping("/cookie/get")
    @ResponseBody
    public String getCookie(@CookieValue("code") String code){
        //服务器怎么得到cookie  request.getCookie() 是一个List集合  还需要遍历才能拿到
        //一般用注解 @CookieValue("code") String code
        System.out.println("code = " + code);
        return "get cookie";
    }


    //session示例
    @RequestMapping(value = "/session/set",method = RequestMethod.GET)
    @ResponseBody
    public String setSession(HttpSession session){ //springMVC会自动创建session并注入
        session.setAttribute("id",1);
        session.setAttribute("name","test");
        return "set session";
    }

    @RequestMapping(value = "/session/get",method = RequestMethod.GET)
    @ResponseBody
    public String getSession(HttpSession session){
        System.out.println(session.getAttribute("id"));
        System.out.println(session.getAttribute("name"));
        return "get session";
    }

    /**
     * ajax示例
     */
    @RequestMapping(path = "/ajax",method = RequestMethod.POST)
    @ResponseBody
    public String testAjax(String name, int age){
        System.out.println(name);
        System.out.println(age);
        return CommunityUtil.getJSONString(0,"操作成功！");
    }
}
