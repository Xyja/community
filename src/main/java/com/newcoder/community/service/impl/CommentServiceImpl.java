package com.newcoder.community.service.impl;

import com.newcoder.community.dao.CommentMapper;
import com.newcoder.community.entity.Comment;
import com.newcoder.community.service.CommentService;
import com.newcoder.community.service.DiscussPostService;
import com.newcoder.community.util.CommunityConstant;
import com.newcoder.community.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

/**
 * @author Yongjiu, X
 * @create 2022-07-18 20:30
 */
@Service
public class CommentServiceImpl implements CommentService, CommunityConstant {

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Autowired
    private CommentMapper commentMapper;

    @Override
    public List<Comment> findCommentByEntity(int entityType, int entityId, int offset, int limit) {
        return commentMapper.selectCommentByEntity(entityType,entityId,offset,limit);
    }

    @Override
    public int findCommentCount(int entityType, int entityId) {
        return commentMapper.selectCountByEntity(entityType,entityId);
    }

    /**
     * 增加评论 逻辑层包含两次访问数据库 并修改数据 所以我们希望这是一个事务
     * @param comment
     * @return
     */
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    @Override
    public int addComment(Comment comment) {
        //首先处理空值
        if (comment == null){
            System.out.println("空了。。。焯");
            throw new IllegalArgumentException("评论不能为空！");
        }
        //然后对页面传过来的实体 进行一些过滤 包括敏感词和 转义字符
        comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));
        comment.setContent(sensitiveFilter.replaceSensitiveWords(comment.getContent()));
        int rows = commentMapper.insertComment(comment);

        //更新评论数量 可以评论帖子 可以回复评论  但是这是更新回复帖子的评论
        if (comment.getEntityType() == ENTITY_TYPE_DISCUSSPOST){
            int count = commentMapper.selectCountByEntity(comment.getEntityType(),comment.getEntityId());
            discussPostService.updateCommentCount(comment.getEntityId(),count);

        }


        return rows;
    }

    @Override
    public Comment findCommentById(int id) {
        return commentMapper.selectCommentById(id);
    }


}
