package com.newcoder.community.util;

import com.newcoder.community.entity.User;
import org.springframework.stereotype.Component;

/**持有用户的信息，用于代替session对象
 * @author Yongjiu, X
 * @create 2022-07-14 21:41
 */
@Component
public class HostHolder {

    //ThreadLocal 是以线程为单位 存取值的
    private ThreadLocal<User> users = new ThreadLocal<>();

    public void setUser(User user){
        users.set(user);
    }

    public User getUser(){
        return users.get();
    }

    public void clear(){
        users.remove();
    }
}
