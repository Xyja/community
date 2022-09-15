package com.newcoder.community.service.impl;

import com.newcoder.community.dao.DiscussPostMapper;
import com.newcoder.community.dao.UserMapper;
import com.newcoder.community.entity.DiscussPost;
import com.newcoder.community.entity.User;
import com.newcoder.community.util.CommunityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Date;

/**
 * @author Yongjiu, X
 * @create 2022-07-17 21:06
 */
@Service
public class TestTransactionServiceImpl {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private TransactionTemplate transactionTemplate;

    /**
     * 新增用户，并发第一个帖子
     * 1.传播机制 常用的三个  传播机制: 两个事务交叉  A方法有一个事务 B有一个  A调用B
     *  1.REQUIRED： 支持当前事务 调用者的事务（外部事物），如果外部事物不存在则创建新的事务
     *  2.REQUIRES_NEW：创建一个新的事务，并且暂停外部事物 A调B，B无视A的事务，创建一个新的事务按照自己的方法执行
     *  3.NESTED：如果当前存在外部事物，则嵌套在该事务中执行（B嵌套在A的事务里执行，但是有独立的提交和回滚），否则和REQUIRED一样
     * @return
     */
    @Transactional(isolation = Isolation.READ_COMMITTED,propagation = Propagation.REQUIRED)
    public Object save1(){
        //新增用户
        User user = new User();
        user.setUsername("Rose");
        user.setSalt(CommunityUtil.generateUUID().substring(0,5));
        user.setPassword(CommunityUtil.getMD5("123") + user.getSalt());
        user.setEmail("Rose@sina.com");
        user.setHeaderUrl("http://image.nowcoder.com/head/99t.png");
        user.setCreateTime(new Date());
        userMapper.insertUser(user);


        //新增帖子
        DiscussPost discussPost = new DiscussPost();
        discussPost.setUserId(user.getId());
        discussPost.setTitle("hello, nowcoder");
        discussPost.setContent("new text");
        discussPost.setCreateTime(new Date());

        discussPostMapper.insertDiscussPost(discussPost);

        int i = 10 / 0; //会报错  事务会回滚java.lang.ArithmeticException: / by zero

        return "ok";
    }

    /**
     * 注入TransactionTemplate  管理事务
     * @return
     */
    public Object save2(){
        transactionTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        return transactionTemplate.execute(new TransactionCallback<Object>() {
            @Override
            public Object doInTransaction(TransactionStatus status) {
                //新增用户
                User user = new User();
                user.setUsername("Luna");
                user.setSalt(CommunityUtil.generateUUID().substring(0,5));
                user.setPassword(CommunityUtil.getMD5("666") + user.getSalt());
                user.setEmail("Luna@sina.com");
                user.setHeaderUrl("http://image.nowcoder.com/head/99t.png");
                user.setCreateTime(new Date());
                userMapper.insertUser(user);


                //新增帖子
                DiscussPost discussPost = new DiscussPost();
                discussPost.setUserId(user.getId());
                discussPost.setTitle("hello, nowcoder");
                discussPost.setContent("new text");
                discussPost.setCreateTime(new Date());

                discussPostMapper.insertDiscussPost(discussPost);

                int i = 10 / 0; //会报错  事务会回滚java.lang.ArithmeticException: / by zero
                return "ok";
            }
        });
    }
}
