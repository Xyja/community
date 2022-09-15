package com.newcoder.community;

import com.newcoder.community.dao.DiscussPostMapper;
import com.newcoder.community.dao.LoginTicketMapper;
import com.newcoder.community.dao.MessageMapper;
import com.newcoder.community.dao.UserMapper;
import com.newcoder.community.entity.DiscussPost;
import com.newcoder.community.entity.LoginTicket;
import com.newcoder.community.entity.Message;
import com.newcoder.community.entity.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.List;

/**
 * @author Yongjiu, X
 * @create 2022-07-06 21:13
 */

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MapperTest {

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private LoginTicketMapper loginTicketMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Test
    public void testSelect(){
        User user = userMapper.selectById(101);
        System.out.println(user);

        user = userMapper.selectByUserName("liubei");
        System.out.println(user);

        user = userMapper.selectByEmail("newcoder101@sina.com");
        System.out.println(user);
    }

    @Test
    public void testInsert(){
        User user = new User();
        user.setUsername("YanJing");
        user.setPassword("123456abc");
        user.setEmail("yanjing@qq.com");
        user.setSalt("bac");
        user.setHeaderUrl("http://www.nowcoder.com/101.png");
        user.setCreateTime(new Date());
        user.setType(0);
        user.setStatus(1);

        int rows = userMapper.insertUser(user);
        System.out.println(rows);
        System.out.println(user.getId());
    }

    @Test
    public void testUpdate(){
        int i = userMapper.updateHeaderUrl(150, "http://www.nowcoder.com/101.png");
        System.out.println(i);

        int i1 = userMapper.updatePassword(150, "123456");
        System.out.println(i1);


    }

    @Test
    public void testSelectPosts(){
        List<DiscussPost> list = discussPostMapper.selectDiscussPosts(0,0,10);
        for (DiscussPost post : list){
            System.out.println(post);
        }

        int rows = discussPostMapper.selectDiscussPostRows(0);
        System.out.println(rows);
    }

    @Test
    public void testInsertLoginTicket(){
        LoginTicket loginTicket = new LoginTicket(0,101,"abc",0,new Date(System.currentTimeMillis() + 1000 * 60 * 10));
        loginTicketMapper.insertLoginTicket(loginTicket);
    }

    @Test
    public void testSelectAndUpdateLoginTicket(){
        LoginTicket loginTicket = loginTicketMapper.selectByTicket("abc");
        System.out.println(loginTicket);
        loginTicketMapper.updateStatus("abc",1);
        LoginTicket loginTicket1 = loginTicketMapper.selectByTicket("abc");
        System.out.println(loginTicket1);
    }

    @Test
    public void TestSelectLetters(){
        List<Message> list = messageMapper.selectConversations(111,0,20);
        for (Message m : list){
            System.out.println(m);
        }

        int count = messageMapper.selectConversationsCount(111);
        System.out.println(count);

        List<Message> letters = messageMapper.selectLetters("111_112", 0, 10);
        for (Message m : letters){
            System.out.println(m);
        }

        int count1 = messageMapper.selectLettersCount("111_112");
        System.out.println(count1);

        int count2 = messageMapper.selectLetterUnreadCount(131, "111_131");
        System.out.println(count2);
    }


}
