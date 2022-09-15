package com.newcoder.community.dao;

import com.newcoder.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author Yongjiu, X
 * @create 2022-07-07 20:08
 */
@Mapper //有了注解才能被容器扫描这个接口 实现自动装配
public interface DiscussPostMapper {

    //注意细节：这里的userId是为了一行的功能查询自己发布的帖子 如果 userId为空 返回0 没啥细节搞错了
    //如果userID有值  则是查看个人发布的帖子  类似于个人空间
    //动态sql  在加上分页的参数 起始行号 和每页条数
    List<DiscussPost> selectDiscussPosts(int userId, int offset, int limit);

    //查询总帖子数量 为了分页 做准备的方法  动态sql
    //注意：如果说需要动态的拼接一个条件，并且这个方法只有这一个条件 这个时候这个参数必须起别名
    int selectDiscussPostRows(@Param("userId") int userId);



    //===================以上是最开始的首页访问方法2022/7/7========

    //发布帖子
    int insertDiscussPost(DiscussPost discussPost);

    //查询帖子的详情
    DiscussPost selectDiscussPostById(int id);

    //增加帖子的评论数量
    int updateCommentCount(int id, int commentCount);


}
