package com.newcoder.community.controller;

import com.newcoder.community.entity.*;
import com.newcoder.community.event.EventProducer;
import com.newcoder.community.service.CommentService;
import com.newcoder.community.service.DiscussPostService;
import com.newcoder.community.service.LikeService;
import com.newcoder.community.service.UserService;
import com.newcoder.community.util.CommunityConstant;
import com.newcoder.community.util.CommunityUtil;
import com.newcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

/**
 * @author Yongjiu, X
 * @create 2022-07-16 21:07
 */
@Controller
@RequestMapping("/discuss")
public class DiscussPostController implements CommunityConstant {

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private LikeService likeService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private UserService userService;

    //获取当前用户
    @Autowired
    private HostHolder hostHolder;

    /**
     * 添加帖子
     * @param title
     * @param content
     * @return
     */
    @RequestMapping(path = "/add",method = RequestMethod.POST)
    @ResponseBody
    public String addDiscussPost(String title, String content){

        //从hostHolder取用户 判断是否登录
        User user = hostHolder.getUser();
        if (user == null){
            return CommunityUtil.getJSONString(403,"您还没有登录！");
        }

        DiscussPost discussPost = new DiscussPost();
        discussPost.setUserId(user.getId());
        discussPost.setTitle(title);
        discussPost.setContent(content);
        discussPost.setCreateTime(new Date());

        //对象构造好之后，调用Service层方法 存进数据库
        discussPostService.addDiscussPost(discussPost);

        //触发发帖事件，把帖子存到elasticsearch服务器里
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(user.getId())
                .setEntityType(ENTITY_TYPE_DISCUSSPOST)
                .setEntityId(discussPost.getId());
        eventProducer.fireEvent(event);




        //报错的情况 将来我们统一处理 就不在这里一个一个处理
        return CommunityUtil.getJSONString(0,"发布成功！");
    }

    /**
     * restful 风格  把id作为参数添加到路径的后面
     * 查询帖子
     * @return
     */
    @RequestMapping(path = "/detail/{discussPostId}",method = RequestMethod.GET)
    public String getDiscussPost(@PathVariable("discussPostId") int discussPostId,
                                 Model model, PageInfo pageInfo){
        //查询帖子
        DiscussPost discussPost = discussPostService.findDiscusPostById(discussPostId);
        //为了在模板上引用数据方便 就起一个简短的名字
        model.addAttribute("post",discussPost);
        //页面上需要显示用户的名字而不是id 所以需要处理一下
        //还是两种办法 1. 可以在写mapper的时候写一个关联查询 2.也可以手动封装好再返回
        //即通过查询回来的discussPost 获取 userId 再通过userService 根据id查询用户
        //最后 user 和 discussPost 都返回给页面就可以了
        //前者效率更高，但是查询方法 耦合就比较高 有些冗余  第二种方式不冗余但是需要查询两次
        //效率可能更低 后面redis解决这个问题  把数据缓存到redis里 效率也是非常高 几乎没影响
        //关联查询，具体查看mybatis的手册
        User user = userService.findUserById(discussPost.getUserId());
        model.addAttribute("user",user);

        //点赞数量
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_DISCUSSPOST,discussPostId);
        model.addAttribute("likeCount",likeCount);

        //点赞状态
        int likeStatus = hostHolder.getUser() == null ? 0:
                likeService.findEntityLikeStatus(hostHolder.getUser().getId(),ENTITY_TYPE_DISCUSSPOST,discussPostId);
        model.addAttribute("likeStatus",likeStatus);

        //目前位置只是简单的处理帖子详情页面  具体的回复点赞数 后面开发完帖子回复再处理

        //下面是查评论的分页信息
        pageInfo.setLimit(5);
        pageInfo.setPath("/discuss/detail/" + discussPostId);
        pageInfo.setRows(discussPost.getCommentCount());

        //分页查询  这样写把实体类型写死了 将来改动的话耦合度太大 写入CommunityConstant里面
        //让此类实现CommunityConstant 接口

        //评论：帖子的评论
        //回复：给评论的评论

        //评论列表
        List<Comment> commentList = commentService.findCommentByEntity(ENTITY_TYPE_DISCUSSPOST,
                discussPost.getId(),pageInfo.getOffset(),pageInfo.getLimit());
        //因为查到的表里有userId 我们需要进一步处理 让页面显示用户名称而不是id
        //commentVoList ：view object 显示的对象
        //评论Vo列表
        List<Map<String, Object>> commentVoList = new ArrayList<>();
        if (commentList != null){
            for (Comment comment : commentList){
                //评论Vo
                Map<String, Object> commentVo = new HashMap<>();
                //评论
                commentVo.put("comment",comment);
                //作者
                commentVo.put("user",userService.findUserById(comment.getUserId()));


                //点赞数量
                likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT,comment.getId());
                commentVo.put("likeCount",likeCount);

                //点赞状态
                likeStatus = hostHolder.getUser() == null ? 0:
                        likeService.findEntityLikeStatus(hostHolder.getUser().getId(),ENTITY_TYPE_COMMENT,comment.getId());
               commentVo.put("likeStatus",likeStatus);


                //查询回复列表
                List<Comment> replyList = commentService.findCommentByEntity(
                        ENTITY_TYPE_COMMENT,comment.getId(),0,Integer.MAX_VALUE);

                //回复的Vo列表
                List<Map<String, Object>> replyVoList = new ArrayList<>();
                if (replyList != null){
                    for (Comment reply : replyList){
                        Map<String, Object> replyVo = new HashMap<>();
                        //往map中存一个回复
                        replyVo.put("reply",reply);
                        //作者
                        replyVo.put("user",userService.findUserById(reply.getUserId()));

                        //回复目标的查询 如果是回复某人 则target！= null 如果是普通回复帖子target=null
                        User target = reply.getTargetId() == 0 ? null : userService.findUserById(reply.getTargetId());
                        replyVo.put("target",target);
                        //点赞数量
                        likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT,reply.getId());
                        replyVo.put("likeCount",likeCount);

                        //点赞状态
                        likeStatus = hostHolder.getUser() == null ? 0:
                                likeService.findEntityLikeStatus(hostHolder.getUser().getId(),ENTITY_TYPE_COMMENT,reply.getId());
                        replyVo.put("likeStatus",likeStatus);

                        replyVoList.add(replyVo);
                    }
                }
                commentVo.put("replys",replyVoList);

                //回复数量也查好， 并封装到commentVo 返回给页面
                int replyCount = commentService.findCommentCount(ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("replyCount",replyCount);
                commentVoList.add(commentVo);

            }


        }
        model.addAttribute("comments",commentVoList);
        return "/site/discuss-detail";
    }


}
