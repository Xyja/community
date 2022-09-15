package com.newcoder.community.controller;

import com.newcoder.community.entity.DiscussPost;
import com.newcoder.community.entity.User;
import com.newcoder.community.entity.PageInfo;
import com.newcoder.community.service.DiscussPostService;
import com.newcoder.community.service.LikeService;
import com.newcoder.community.service.UserService;
import com.newcoder.community.util.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Yongjiu, X
 * @create 2022-07-07 21:38
 */
@Controller
public class HomeController implements CommunityConstant {

    //controller 肯定要进行查询嘛，肯定需要service层
    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;


    @RequestMapping(path = "/index",method = RequestMethod.GET)
    public String getIndexPage(Model model, PageInfo pageInfo){

        //在处理数据之前就把分页相关的功能处理好 并封装到 pageInfo里面
        //1.服务器计算出总条数 返回给页面
        pageInfo.setRows(discussPostService.findDiscussPostRows(0));
        pageInfo.setPath("/index"); //页面就可以复用这个路径了

        //这里可以 调用  model.attribute("pageInfo",pageInfo),但其实这一步可以省略
        //在SpringMVC框架里，方法参数都是由DisPatcherServlet初始化（实例化）并处理 model 和 view
        //会自动的把pageInfo装到model里面，所以在Thymeleaf中可以之间访问pageInfo中的数据

        //PageInfo.getOffset(), PageInfo.getLimit() 这两个值如果 页面不传过来 就是服务器设置的默认值
        List<DiscussPost> list = discussPostService.findDiscussPosts(0, pageInfo.getOffset(), pageInfo.getLimit());
        //上面查询出来的只是一些用户ID而已 我们需要把这些id变成用户名
        //我们需要把discussPost 和 user 组合起来 用一个List<Map<String,Object>>
        List<Map<String,Object>> discussPosts = new ArrayList<>();
        if(list.size() != 0){
            for (DiscussPost post : list) {
                Map<String, Object> map = new HashMap<>();
                map.put("post",post);
                int userId = post.getUserId();
                User user = userService.findUserById(userId);
                map.put("user",user);

                long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_DISCUSSPOST,post.getId());
                map.put("likeCount",likeCount);

                discussPosts.add(map);


            }
        }
        model.addAttribute("discussPosts",discussPosts);
        return "/index";
    }

    @RequestMapping(path = "/error",method = RequestMethod.GET)
    public String getErrorPage(){
        return "/error/500";
    }


}
