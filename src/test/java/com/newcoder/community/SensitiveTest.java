package com.newcoder.community;

import com.newcoder.community.util.SensitiveFilter;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Scanner;

/**
 * @author Yongjiu, X
 * @create 2022-07-15 21:58
 */
@SpringBootTest
public class SensitiveTest {

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Test
    public void testSensitiveFilter(){
        String xinLiHua = "爱生活";
        String replaceSensitiveWords = sensitiveFilter.replaceSensitiveWords(xinLiHua);
        System.out.println(replaceSensitiveWords);
    }
}
