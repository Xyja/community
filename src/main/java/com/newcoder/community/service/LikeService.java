package com.newcoder.community.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Yongjiu, X
 * @create 2022-07-31 15:46
 */
@Service
public interface LikeService {

    //点赞
    void like(int userId, int entityType, int entityId,int entityUserId);

    //查询实体点赞的数量
    long findEntityLikeCount(int entityType, int entityId);

    //查询某个用户对某个实体点赞的状态  boolean值只能有两种状态 如果用整数可以表现多种状态
    int findEntityLikeStatus(int userId, int entityType, int entityId);

    //查询某个用户获得赞的数量
    int findUserLikeCount(int userId);
}
