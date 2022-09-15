package com.newcoder.community;

import com.newcoder.community.service.impl.TestTransactionServiceImpl;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author Yongjiu, X
 * @create 2022-07-17 21:26
 */
@SpringBootTest
public class TransactionTest {

    @Autowired
    private TestTransactionServiceImpl testTransactionService;

    @Test
    public void testSave1(){
        Object obj = testTransactionService.save1();
        System.out.println(obj);
    }

    @Test
    public void testSave2(){
        Object obj = testTransactionService.save2();
        System.out.println(obj);
    }
}
