package com.newcoder.community.controller;

import com.newcoder.community.annotation.LoginRequired;
import com.newcoder.community.entity.User;
import com.newcoder.community.service.FollowService;
import com.newcoder.community.service.LikeService;
import com.newcoder.community.service.UserService;
import com.newcoder.community.util.CommunityConstant;
import com.newcoder.community.util.CommunityUtil;
import com.newcoder.community.util.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Yongjiu, X
 * @create 2022-07-15 15:16
 */
@Controller
@RequestMapping("/user")
public class UserController implements CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);


    @Value("${community.path.upload}")
    private String uploadPath;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    //hostHolder取出当前user
    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private FollowService followService;


    /**
     * 用户个人设置页面 请求
     */
    @LoginRequired
    @RequestMapping(path = "/setting",method = RequestMethod.GET)
    public String getSettingPage(){
        return "/site/setting";
    }

    /**
     * 上传文件
     * 1. 文件存放路径  项目域名  上下文路径
     */
    @LoginRequired
    @RequestMapping(path = "/upload",method = RequestMethod.POST)
    public String uploadHeader(MultipartFile headerImg, Model model){
        if (headerImg == null){ //如果图片为空 就返回当前页面并给出一个提示
            model.addAttribute("error","图片不能为空！");
            return "/site/setting";
        }

        //因为上传文件 用户可以多次传同一个文件 我们不能让它们彼此覆盖 需要 用随机字符串
        //生成随机的文件名 但是后缀不变

        //得到文件名的后缀
        String filename = headerImg.getOriginalFilename();
        String suffix = filename.substring(filename.lastIndexOf("."));
        //这里再判断一下图片后缀是不是合理
        if (StringUtils.isBlank(suffix)){
            model.addAttribute("error","图片格式不正确！");
            return "/site/setting";
        }

        //生成随机的文件名
        filename = CommunityUtil.generateUUID() + suffix;

        //指定文件存放的路径
        File dest = new File(uploadPath + "/" + filename);

        //把headerImg的数据写入到这个file
        try {
            //存储文件
            headerImg.transferTo(dest);
        } catch (IOException e) {
            //遇到异常，肯定的记录日志
            logger.error("文件上传失败！" + e.getMessage());
            //抛出异常，打断正常程序 controller的异常将来统一处理
            throw new RuntimeException("文件上传失败，服务器发生异常！", e);
        }

        //到这里，用户头像路径已经存储了  接下来 更新用户头像路径
        //提供的是web路径 并不是磁盘路径
        // http://localhost:9999/community/user/header/xxx.png
        User user = hostHolder.getUser();
        String headerUrl = domain + contextPath + "/user/header" + filename;
        userService.updateHeader(user.getId(),headerUrl);

        //到此为止 更新头像成功  重定向到首页
        return "redirect:/index";

    }

    /**
     * 获取头像
     * 1.返回值比较特殊 不是字符串也不是页面 是二进制流
     * 2.直接手动调response返回
     */
    @RequestMapping(path = "/header/{filename}",method = RequestMethod.GET)
    public void getHeader(@PathVariable String filename, HttpServletResponse response){

        //服务器存放的路径
        filename = uploadPath + "/" + filename;
        //解析文件后缀
        String suffix = filename.substring(filename.lastIndexOf("."));
        //响应图片
        response.setContentType("image/" + suffix);
        try (//java1.7的语法  再小括号里面的数据 编译的时候 会自动再finally里面关闭
                OutputStream os = response.getOutputStream();
                //把文件读到输入流里面
                FileInputStream fis = new FileInputStream(filename);
                ){

            //设置缓冲区 高效输出
            byte[] buffer = new byte[124];
            int b = 0;
            while ((b = fis.read(buffer)) != -1){
                os.write(buffer,0,b);
            }
            //输出流SpringMVC会自动关闭 因为response是它管理的  但是 输入流需要我们手动关闭
        } catch (IOException e) {
            //把异常的情况 记录日志
            logger.error("获取头像失败！" + e.getMessage());
        }
    }

    /**
     * 个人主页
     * @return
     */
    @RequestMapping(path = "/profile/{userId}",method = RequestMethod.GET)
    public String getProfilePage(@PathVariable("userId") int userId, Model model){
        User user = userService.findUserById(userId);
        if (user == null){
            throw new RuntimeException("该用户不存在！");

        }
        model.addAttribute("user",user);
        //查询用户获得的点赞数量
        int likeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount",likeCount);

        //关注数量
        long followeeCount = followService.findFolloweeCount(userId,ENTITY_TYPE_USER);
        model.addAttribute("followeeCount",followeeCount);


        //粉丝数量
        long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER,userId);
        model.addAttribute("followerCount",followerCount);
        //当前用户对当前实体是否已存在关注关系
        boolean hasFollowed = false;
        if (hostHolder.getUser() != null){
            hasFollowed = followService.hasFollowed(hostHolder.getUser().getId(),ENTITY_TYPE_USER,userId);
        }
        model.addAttribute("hasFollowed",hasFollowed);

        return "/site/profile";
    }
}
