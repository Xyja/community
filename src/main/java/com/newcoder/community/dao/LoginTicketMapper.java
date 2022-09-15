package com.newcoder.community.dao;

import com.newcoder.community.entity.LoginTicket;
import org.apache.ibatis.annotations.*;

/**
 * @author Yongjiu, X
 * @create 2022-07-14 16:15
 */
@Mapper
@Deprecated
//不推荐使用 我们能用Redis代替
public interface LoginTicketMapper {

    //之前的方式是写一个配置文件 写sql语句 映射此接口中的方法  也可以直接在mapper中
    //用注解的方式实现
    //注解的方式就是小括号里写一个大括号的字符串集合 spring可以把他们拼接成一个sql语句
    //每句话之后加一个空格

    //插入一个凭据
    @Insert({
            "insert into login_ticket (user_id,ticket,status,expired) ",
            "values(#{userId},#{ticket},#{status},#{expired})"
    })
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertLoginTicket(LoginTicket loginTicket);


    //根据ticket查询凭据  每个userId 都有一个ticket 根据ticket看用户是谁
    @Select({
            "select id,user_id,ticket,status,expired ",
            "from login_ticket where ticket = #{ticket}"
    })
    LoginTicket selectByTicket(String ticket);

    //修改凭据的状态 用户退出时候   也可以删除 但是常用的是 更改用户状态
    @Update({
            "update login_ticket set status = #{status} ",
            "where ticket = #{ticket}"
    })
    int updateStatus(String ticket, int status);

    /**
     * 演示动态sql的写法  就是加一个 script标签
     * <script>
     *     "upadte login_ticket set status = #{status} ",
     *     "<if test=\"ticket!=null\"> ",  //转义字符
     *         "and 1=1"
     *     "</if>",
     *     "where status = #{status}"
     * </script>
     */

    //写完一个mapper最好测一下 不然后期找bug  头大 ++
}
