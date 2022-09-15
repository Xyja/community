package com.newcoder.community.util;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

/**
 * @author Yongjiu, X
 * @create 2022-07-14 21:24
 */
public class CookieUtil {
    //这里是一个简单的静态方法 就不用Spring容器管理了
    public static String getValue(HttpServletRequest request, String name){
        //处理空值
        if (request == null || name == null){
            throw new IllegalArgumentException("参数为空！");
        }
        Cookie[] cookies = request.getCookies();
        if (cookies != null){
            for (Cookie cookie : cookies){
                if (cookie.getName().equals(name)){
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}


