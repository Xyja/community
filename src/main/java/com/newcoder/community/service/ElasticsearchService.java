package com.newcoder.community.service;

import com.newcoder.community.entity.DiscussPost;
import org.springframework.data.domain.Page;

/**
 * @author Yongjiu, X
 * @create 2022-08-04 15:20
 */
public interface ElasticsearchService {

    //向es服务器提交新产生的帖子
    void saveDiscussPost(DiscussPost post);

    //在es服务器删除帖子
    void deleteDiscussPost(int id);

    //搜索方法
    Page<DiscussPost> searchDiscussPost(String keyword, int current,int limit);
}
