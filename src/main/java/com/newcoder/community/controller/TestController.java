package com.newcoder.community.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

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
}
