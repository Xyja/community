package com.newcoder.community.service;

import com.newcoder.community.entity.PageInfo;

import java.util.List;
import java.util.Map;

/**
 * @author Yongjiu, X
 * @create 2022-07-31 20:27
 */
public interface FollowService {

    //关注某个实体
    void follow(int userId, int entityTye, int entityId);

    //取消关注某个实体
    void unFollow(int userId, int entityTye, int entityId);

    //查询某个用户关注的实体对象数量  数值
    long findFolloweeCount(int userId, int entityType);

    //查询实体的粉丝数量  数值
    long findFollowerCount(int entityType, int entityId);

    //查询当前用户是否已关注该实体
    boolean hasFollowed(int userId, int entityType, int entityId);


    //查询某个用户关注的用户 列表
    List<Map<String, Object>> findFollowees(int userId, int offset, int limit);

    //查询某个用户的粉丝 列表
    List<Map<String, Object>> findFollowers(int userId, int offset, int limit);

}
