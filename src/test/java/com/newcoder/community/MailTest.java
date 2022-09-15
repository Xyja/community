package com.newcoder.community;

import com.newcoder.community.util.MailClient;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

/**
 * @author Yongjiu, X
 * @create 2022-07-11 20:45
 */
@SpringBootTest
public class MailTest {

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Test
    public void testMail(){
        mailClient.sendMail("nowcoder@sina.com","TestMail","hello!");
    }

    @Test
    public void testHtmlMail(){
        //给模板传一个参数
        Context context = new Context();
        context.setVariable("username","Jerry");
        //用模板生成动态网页
        String content = templateEngine.process("/mail/demo",context);
        System.out.println(content);

        mailClient.sendMail("nowcoder@sina.com","Html",content);
    }
}
