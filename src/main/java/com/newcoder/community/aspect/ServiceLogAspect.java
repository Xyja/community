package com.newcoder.community.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.mvc.condition.RequestConditionHolder;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;

/**我希望这个Aspect  可以在用户调用service方法之前 输出到日志记录info文件
 * 某某用户ip地址  在某某时间  调用了某某方法
 * @author Yongjiu, X
 * @create 2022-07-24 16:08
 */
@Component
@Aspect
public class ServiceLogAspect {

    private static final Logger logger = LoggerFactory.getLogger(ServiceLogAspect.class);


    @Pointcut("execution(* com.newcoder.community.service.*.*(..))")
    public void pointcut(){

    }

    /**
     * 我需要前置通知
     */
    @Before("pointcut()")
    public void before(JoinPoint joinPoint){
        //某某用户  在某某时间  调用了某某方法
        //那么怎么获取用户的Ip  通过Request  但是不能简单的声明request对象
        //通过RequestContextHolder的静态方法获取，再强转为ServletRequestAttributes
        //方法多一点
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null){
            return;
        }
        HttpServletRequest request = attributes.getRequest();
        String ip = request.getRemoteHost();
        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String target = joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName();
        logger.info(String.format("用户[%s],在[%s],访问了[%s].",ip,now,target));
    }
}
