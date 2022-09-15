package com.newcoder.community.dao;

import com.newcoder.community.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author Yongjiu, X
 * @create 2022-07-18 20:18
 */
@Mapper
public interface CommentMapper {

    //查询有多少条评论
    List<Comment> selectCommentByEntity(int entityType, int entityId, int offset, int limit);

    //查询总共多少条
    int selectCountByEntity(int entityType, int entityId);

    //增加评论
    int insertComment(Comment comment);

    //通过id查询评论
    Comment selectCommentById(int id);
}
