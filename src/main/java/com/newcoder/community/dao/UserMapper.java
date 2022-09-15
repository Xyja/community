package com.newcoder.community.dao;

import com.newcoder.community.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author Yongjiu, X
 * @create 2022-07-06 20:33
 */
@Mapper
public interface UserMapper {

    //根据id查询用户
    User selectById(int id);

    //根据用户名查用户
    User selectByUserName(String Username);

    //根据邮箱查用户
    User selectByEmail(String email);

    //增加用户  返回 增加的行数
    int insertUser(User user);

    //修改用户的状态信息  返回修改了几条
    int updateStatus(int id, int status);

    //更新头像  返回int
    int updateHeaderUrl(int id, String headerUrl);

    //更改密码  返回int
    int updatePassword(int id, String password);

    //根据id删除用户
    int deleteById(int id);


}
