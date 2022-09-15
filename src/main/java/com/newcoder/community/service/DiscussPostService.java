package com.newcoder.community.service;

import com.newcoder.community.entity.DiscussPost;

import java.util.List;

/**
 * @author Yongjiu, X
 * @create 2022-07-07 21:07
 */
public interface DiscussPostService {


    //查询帖子的方法
    List<DiscussPost> findDiscussPosts(int userId, int offset, int limit);


    //查询行数的方法
    int findDiscussPostRows(int userId);

    //发布帖子
    int addDiscussPost(DiscussPost discussPost);

    //查询帖子
    DiscussPost findDiscusPostById(int id);

    //修改帖子评论的数量
    int updateCommentCount(int id, int commentCount);
}
