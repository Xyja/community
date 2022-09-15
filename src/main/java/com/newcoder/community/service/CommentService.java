package com.newcoder.community.service;

import com.newcoder.community.entity.Comment;

import java.util.List;

/**
 * @author Yongjiu, X
 * @create 2022-07-18 20:29
 */
public interface CommentService {

    //查询某页数据
    List<Comment> findCommentByEntity(int entityType, int entityId, int offset, int limit);

    //查询总共记录条数
    int findCommentCount(int entityType, int entityId);

    //增加评论
    int addComment(Comment comment);

    //通过评论id查询评论
    Comment findCommentById(int id);
}
