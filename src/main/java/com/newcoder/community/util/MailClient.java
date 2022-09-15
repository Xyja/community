package com.newcoder.community.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMailMessage;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

/**这个组件提供的是发邮件的功能，他要把发邮件的这个事儿委托给新浪去做，相当于一个客户端
 * @Component 是让这个bean让spring去管理  component表示的是一个通用的bean 在哪个层面都可以用
 * @author Yongjiu, X
 * @create 2022-07-11 20:29
 */
@Component
public class MailClient {

    //打印日志
    private static final Logger logger = LoggerFactory.getLogger(MailClient.class);

    //JavaMailSender 发送邮件
    /**
     * 发邮件 需要 发件人 收件人  和内容
     * 直接把 username: xiaoyongjiu@sina.com 注入进来 当作发件人
     *
     */
    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;



    public void sendMail(String to, String subject, String content) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message);
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);
            mailSender.send(helper.getMimeMessage());
        } catch (MessagingException e) {
            logger.error("发送邮件失败:" + e.getMessage());
        }
    }



}
