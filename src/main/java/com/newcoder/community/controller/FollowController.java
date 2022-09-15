package com.newcoder.community.controller;

import com.newcoder.community.entity.Event;
import com.newcoder.community.entity.PageInfo;
import com.newcoder.community.entity.User;
import com.newcoder.community.event.EventProducer;
import com.newcoder.community.service.FollowService;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Yongjiu, X
 * @create 2022-07-31 20:34
 */
@Controller
public class FollowController implements CommunityConstant {

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private UserService userService;

    @Autowired
    private FollowService followService;

    @Autowired
    private HostHolder hostHolder;

    /**
     * 关注某个实体
     * @return
     */
    @RequestMapping(path = "/follow",method = RequestMethod.POST)
    @ResponseBody
    public String follow(int entityType, int entityId){
        User user = hostHolder.getUser();

        followService.follow(user.getId(),entityType,entityId);

        //触发关注事件
        Event event = new Event()
                .setTopic(TOPIC_FOLLOW)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(entityType)
                .setEntityId(entityId)
                .setEntityUserId(entityId);
        eventProducer.fireEvent(event);

        return CommunityUtil.getJSONString(0,"已关注！");
    }

    /**
     * 取消关注某个实体
     * @return
     */
    @RequestMapping(path = "/unfollow",method = RequestMethod.POST)
    @ResponseBody
    public String unFollow(int entityType, int entityId){
        User user = hostHolder.getUser();

        followService.unFollow(user.getId(),entityType,entityId);

        return CommunityUtil.getJSONString(0,"已取消关注！");
    }

    /**
     * 查看用户的关注列表
     * @param userId
     * @param pageInfo
     * @param model
     * @return
     */
    @RequestMapping(path = "/followees/{userId}",method = RequestMethod.GET)
    public String getFollowees(@PathVariable("userId") int userId, PageInfo pageInfo,
                               Model model){
        User user = userService.findUserById(userId);
        if (user == null){
            throw new RuntimeException("该用户不存在！");
        }
        model.addAttribute("user",user);

        pageInfo.setLimit(5);
        pageInfo.setPath("/followees/" + userId);
        pageInfo.setRows((int) followService.findFolloweeCount(userId,ENTITY_TYPE_USER));

        List<Map<String, Object>> userList = followService.findFollowees(userId,pageInfo.getOffset(),pageInfo.getLimit());

        //判断当前用户与目标用户是否存在互相关注的关系
        if (userList != null){
            for (Map<String,Object> map : userList){
                User u = (User) map.get("user");
                map.put("hasFollowed",hasFollowed(u.getId()));
            }
        }
        model.addAttribute("users",userList);
        return "/site/followee";

    }

    /**
     * 查询用户的粉丝列表
     * @param userId
     * @param pageInfo
     * @param model
     * @return
     */
    @RequestMapping(path = "/followers/{userId}",method = RequestMethod.GET)
    public String getFollowers(@PathVariable("userId") int userId, PageInfo pageInfo,
                               Model model){
        User user = userService.findUserById(userId);
        if (user == null){
            throw new RuntimeException("该用户不存在！");
        }
        model.addAttribute("user",user);

        pageInfo.setLimit(5);
        pageInfo.setPath("/followers/" + userId);
        pageInfo.setRows((int) followService.findFollowerCount(ENTITY_TYPE_USER,userId));

        List<Map<String, Object>> userList = followService.findFollowers(userId,pageInfo.getOffset(),pageInfo.getLimit());

        //判断当前用户与目标用户是否存在互相关注的关系
        if (userList != null){
            for (Map<String,Object> map : userList){
                User u = (User) map.get("user");
                map.put("hasFollowed",hasFollowed(u.getId()));
            }
        }
        model.addAttribute("users",userList);

        return "/site/follower";

    }

    /**
     * 判断当前用户是否与目标用户互相关注
     * @param userId
     * @return
     */
    private boolean hasFollowed(int userId){
        if (hostHolder.getUser() == null){
            return false;
        }
        return followService.hasFollowed(hostHolder.getUser().getId(),ENTITY_TYPE_USER,userId);
    }
}
