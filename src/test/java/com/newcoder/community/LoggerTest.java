package com.newcoder.community;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author Yongjiu, X
 * @create 2022-07-10 20:06
 */
@SpringBootTest
public class LoggerTest {

    //要记日志 我们需要记录日志的组件  logger
    //LoggerFactory.getLogger(LoggerTest.class) 传入的类就是这个logger 的名字
    //后面如果打印到这个类的信息 就会与其他logger区别
    private static final Logger logger = LoggerFactory.getLogger(LoggerTest.class);

    @Test
    public void testLogger(){
        System.out.println(logger.getName());//com.newcoder.community.LoggerTest

        logger.debug("debug");
        logger.info("info");
        logger.warn("warn");
        logger.error("error");
    }
}
