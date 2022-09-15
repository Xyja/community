package com.newcoder.community.service.impl;

import com.newcoder.community.dao.DiscussPostMapper;
import com.newcoder.community.entity.DiscussPost;
import com.newcoder.community.service.DiscussPostService;
import com.newcoder.community.service.UserService;
import com.newcoder.community.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

/**
 * @author Yongjiu, X
 * @create 2022-07-07 20:43
 */
@Service //让容器访问到这个类 并标记为service类
public class DiscussPostServiceImpl implements DiscussPostService {

    //service需要dao层做数据访问
    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    //开发小tips：即便是service方法再简单 也需要让dao层去做 不能让controller层直接调用dao层
    //后续开发更方便  即把dao层查到的数据做一些逻辑处理

    //查询帖子的方法

    /**
     * 查询出来的结果 要求显示用户名 但我们得到的数据是用户id  也就说我们可以根据用户id
     * 去数据库拼接表查询并返回  另一种方法就是 查询结果出来再单独查询一下用户名
     * 查完之后再返回到页面  这里采用后者  因为使用redis缓存数据的时候会比较方便 性能也会比较高
     * 代码看以来也会比较直观
     * 显然我们在这里 查user 不合适  可以写在 UserService里面 在controller里面进行调用
     * @param userId
     * @param offset
     * @param limit
     * @return
     */
    @Override
    public List<DiscussPost> findDiscussPosts(int userId, int offset, int limit){
        List<DiscussPost> discussPosts = discussPostMapper.selectDiscussPosts(userId, offset, limit);
        return discussPosts; //注意如果查询出来不显示用户名是因为这里还没有处理

    }

    //查询行数的方法
    @Override
    public int findDiscussPostRows(int userId) {
        return discussPostMapper.selectDiscussPostRows(userId);

    }

    /**
     * 发布新的DiscussPost
     * @param discussPost
     * @return
     */
    @Override
    public int addDiscussPost(DiscussPost discussPost) {
        if (discussPost == null){
            throw new IllegalArgumentException("参数不能为空！");
        }
        //转义HTML标记  就是比如我发表标题为  <script>title</script>
        //然后标题跟我写的一模一样 不会转义成一些符号
        discussPost.setTitle(HtmlUtils.htmlEscape(discussPost.getTitle()));
        discussPost.setContent(HtmlUtils.htmlEscape(discussPost.getContent()));

        //过滤敏感词  注入 SensitiveFilter
        discussPost.setTitle(sensitiveFilter.replaceSensitiveWords(discussPost.getTitle()));
        discussPost.setContent(sensitiveFilter.replaceSensitiveWords(discussPost.getContent()));

        return discussPostMapper.insertDiscussPost(discussPost);
    }

    /**
     * 查询帖子
     * @param id
     * @return
     */
    @Override
    public DiscussPost findDiscusPostById(int id) {
        return discussPostMapper.selectDiscussPostById(id);
    }

    /**
     * 修改帖子评论的数量
     * @param id
     * @param commentCount
     * @return
     */
    @Override
    public int updateCommentCount(int id, int commentCount) {
        return discussPostMapper.updateCommentCount(id,commentCount);
    }


}
