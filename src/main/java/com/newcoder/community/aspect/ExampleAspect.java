package com.newcoder.community.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

/**
 * @author Yongjiu, X
 * @create 2022-07-24 15:50
 */
//@Component  //交给Spring IOC容器管理
//@Aspect //表面这是一个方面组件
public class ExampleAspect {
    /**
     *1. 定义切点位置   * com.newcoder.community.service.*.*(..))
     *                返回值可以是任意值  service包下所有组件的所有方法所有参数
     *
     */
    @Pointcut("execution(* com.newcoder.community.service.*.*(..))")
    public void pointcut(){

    }

    /**
     * 2.定义通知  可以定义在开始做什么事情 运行时做什么事情 结束时做什么事情 抛异常怎么破
     * 开始 结束 返回值以后 抛出异常后 环绕
     */
    @Before("pointcut()")
    public void before(){
        System.out.println("before");
    }

    @After("pointcut()")
    public void after(){
        System.out.println("after");
    }

    @AfterReturning("pointcut()")
    public void afterReturning(){
        System.out.println("afterReturning");
    }

    @AfterThrowing("pointcut()")
    public void afterThrowing(){
        System.out.println("afterThrowing");
    }

    /**
     * 因为程序执行的时候会织入一个代理对象 这个逻辑会织入到代理对象里 用来代替原始对象
     *
     * @param joinPoint
     * @return
     * @throws Throwable
     */
    @Around("pointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        //调用方法之前的处理
        System.out.println("before obj");

        Object obj = joinPoint.proceed(); //调用要处理的目标组件的方法

        //调用方法之后的处理
        System.out.println("after obj");
        return obj;
    }

}
