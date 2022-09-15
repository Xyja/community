package com.newcoder.community.controller;

import com.google.code.kaptcha.Producer;
import com.newcoder.community.entity.User;
import com.newcoder.community.service.UserService;
import com.newcoder.community.util.CommunityConstant;
import com.newcoder.community.util.CommunityUtil;
import com.newcoder.community.util.RedisKeyUtil;
import com.sun.deploy.net.HttpResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author Yongjiu, X
 * @create 2022-07-12 18:17
 */
@Controller
public class LoginController implements CommunityConstant {

    //方便区别日志信息是哪个类打印出来的
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private Producer kaptchaProducer;

    @Autowired
    private UserService userService;

    //上下文路径
    @Value("${server.servlet.context-path}")
    //注意这里可能回报错  因为我用yaml配置的 如果报错就配置到properties里面 没报错
    private String contextPath;

    //处理注册页面的请求
    @RequestMapping(path = "/register",method = RequestMethod.GET)
    public String getRegisterPage(){

        return "/site/register";
    }

    //处理登录页面的请求
    @RequestMapping(path = "/login",method = RequestMethod.GET)
    public String getLoginPage(){
        return "/site/login";
    }

    /**
     * 处理注册表单 验证并调用userService层 注册用户
     * 可以传入用户名、邮箱、密码 三个参数 ，也可以在 直接让SpringMVC自动帮我们封装
     * 只要字段名匹配
     * 注意这里是post请求  请求页面的话 是 get 请求 并不一样
     * @param model
     * @param user
     * @return
     */
    @RequestMapping(path = "/register",method = RequestMethod.POST)
    public String register(Model model, User user){
        Map<String, Object> map = userService.register(user);
        //得到map 看怎么去给浏览器做响应
        if (map == null || map.isEmpty()){
            //如果map为空 我们需要给浏览器一个提升 注册成功 然后跳转到首页 激活再跳转到登陆页面
            //也可以重新写一个简单的页面提升注册成功 几秒后自动跳转到首页
            model.addAttribute("msg","注册成功，我们已经向您的邮箱发送了一封激活邮件，请尽快激活");
            model.addAttribute("target","/index");
            return "/site/operate-result";
        }else { //上面处理的是注册成功的情况
            //下面处理的是注册失败的情况
            model.addAttribute("usernameMessage",map.get("usernameMessage"));
            model.addAttribute("passwordMessage",map.get("passwordMessage"));
            model.addAttribute("emailMessage",map.get("emailMessage"));
            return "/site/register";
        }
    }

    /**
     * http://localhost:9999/community/activation/(userId)/(激活码)
     * 处理激活请求 请求路径是我们发送的邮件超链接的路径 统一的
     * @param model
     * @param userId
     * @param activationCode
     * @return
     */
    @RequestMapping(path = "/activation/{userId}/{activationCode}",method = RequestMethod.GET)
    public String activation(Model model,
                             @PathVariable("userId") int userId,
                             @PathVariable("activationCode") String activationCode){
        int result = userService.activation(userId, activationCode);
        if (result == ACTIVATION_SUCCESS){ //激活成功就跳转到登录页面
            model.addAttribute("msg","激活成功，您的账号已经可以正常使用了");
            model.addAttribute("target","/login");
        }else if (result == ACTIVATION_REPEAT){
            model.addAttribute("msg","该账号已经激活，请勿重复激活！");
            model.addAttribute("target","/index");
        }else { //激活失败就跳转到首页
            model.addAttribute("msg","激活失败，激活码无效！");
            model.addAttribute("target","/index");
        }
        return "/site/operate-result";
    }


    /**
     * 获取验证码的图片  需要用response手动输出 不是返回String类型的 字符串或者 页面
     * 还得用到session 保存验证码 服务器记录 再次登录的时候 验证对不对  敏感数据 存到Session
     * @param response
     * @param //session
     */
    @RequestMapping(value = "/kaptcha",method = RequestMethod.GET)
    public void getKaptcha(HttpServletResponse response/*, HttpSession session*/){
        //生成验证码
        String text = kaptchaProducer.createText();
        BufferedImage image = kaptchaProducer.createImage(text);

        //将验证码存入session  方便后面使用
        //session.setAttribute("kaptcha",text);

        //重构
        //验证码的归属
        String kaptchaOwner = CommunityUtil.generateUUID();
        Cookie cookie = new Cookie("kaptchaOwner",kaptchaOwner);
        cookie.setMaxAge(60);
        cookie.setPath(contextPath);

        response.addCookie(cookie);

        // 将验证码存到Redis
        String redisKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
        redisTemplate.opsForValue().set(redisKey,text,60, TimeUnit.SECONDS);



        //将图片直接输出给浏览器  声明返回的是png格式的图片
        response.setContentType("image/png");
        try {
            ServletOutputStream os = response.getOutputStream();
            ImageIO.write(image,"png",os);
        } catch (IOException e) {
            //这里默认是没有问题的  服务器生成验证码图片 如果有问题 就打印日志 服务器出问题了
            //那么就创建一个Logger  到时候打印的时候知道是哪个类出问题
            logger.error("响应验证码失败",e.getMessage());
        }
    }

    /**
     * 处理登录表单请求 验证表单里面的username，password，验证码是否正确
     * 并做相应处理  注意这里路径也是 /login 但是  是post请求 前面是get请求
     * 1.第四个参数 是页面勾选记住我  服务器做的处理就是失效的时间推迟
     * 2.我需要model返回数据 也需要从session里面获取验证码 之前我们是把验证码放到session里面了
     * 3.如果说登录成功了，我们要把ticket放到客户端的cookie里面
     */
    @RequestMapping(path = "/login",method = RequestMethod.POST)
    public String login(String username, String password, String code, boolean rememberme,
                        Model model/*, HttpSession session*/,HttpServletResponse response,
                        @CookieValue("kaptchaOwner") String kaptchaOwner){

        //首先判断验证码对不对 验证码不对就不用判断账号密码  验证码判断是直接在表现层判断
        //String kaptcha = (String) session.getAttribute("kaptcha");
        String kaptcha = null;

        //判断 kaptchaOwner key 是否已经失效
        if (StringUtils.isNotBlank(kaptchaOwner)){
            String redisKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
            kaptcha = (String) redisTemplate.opsForValue().get(redisKey);
        }

        if (StringUtils.isBlank(kaptcha) || StringUtils.isBlank(code) ||
        !kaptcha.equalsIgnoreCase(code)){
            model.addAttribute("codeMessage","验证码不正确！");
            return "/site/login";
        }

        //检查账号、密码 用户如果勾选了记住我 那么 记录此条数据的时间就会长一些
        //如果没有勾选 记录的时间就短一些 那么我还是设计两个常量时间 CommunityConstant
        int expiredSeconds = rememberme ? REMEMBER_EXPIRED_SECONDS : DEFAULT_EXPIRED_SECONDS;
        Map<String, Object> map = userService.login(username, password, expiredSeconds);
        if (map.containsKey("ticket")){  //成功了，才会往map里面放一个ticket
            //成功之后，重定向到首页
            Cookie cookie = new Cookie("ticket",map.get("ticket").toString());
            //给cookie设置允许范围 那么肯定是整个软件都可以 但是最好不要写死 context路径 用一个
            //参数 跟前面 userServiceImpl 里面的处理方法 类似  注入 @value
            cookie.setPath(contextPath);
            //设置cookie有效时间
            cookie.setMaxAge(expiredSeconds);
            //在服务器响应时，就会把这个cookie发送给客户端页面
            response.addCookie(cookie);
            return "redirect:/index";
        }else {
            model.addAttribute("usernameMessage",map.get("usernameMessage"));
            model.addAttribute("passwordMessage",map.get("passwordMessage"));
            return "/site/login";
        }

        //到此为止登录表单请求的服务器端代码写好了 接下来处理 html
    }

    /**
     * 退出
     */
    @RequestMapping(path = "/logout",method = RequestMethod.GET)
    public String logout(@CookieValue("ticket") String ticket){
        userService.logout(ticket);
        return "redirect:/login"; //重定向的时候默认是get请求
    }
    //接下来配置html
}
