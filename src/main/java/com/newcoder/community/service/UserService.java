package com.newcoder.community.service;

import com.newcoder.community.dao.UserMapper;
import com.newcoder.community.entity.LoginTicket;
import com.newcoder.community.entity.User;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

/**
 * @author Yongjiu, X
 * @create 2022-07-07 21:22
 */
public interface UserService {

    //根据用户id查询用户
    User findUserById(int userId);

    //注册的方法
    Map<String, Object> register(User user);

    //激活方法
    int activation(int userId, String activationCode);

    //登录方法
    Map<String, Object> login(String username, String password, int expiredSeconds);

    //退出方法
    void logout(String ticket);

    //查询LoginTicket
    LoginTicket findLoginTicket(String ticket);

    //更新头像的路径
    int updateHeader(int userId, String headerUrl);

    //通过用户名查询用户
    User findUserByUsername(String Username);


}
