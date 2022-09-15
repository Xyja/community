package com.newcoder.community.service.impl;

import com.newcoder.community.dao.LoginTicketMapper;
import com.newcoder.community.dao.UserMapper;
import com.newcoder.community.entity.LoginTicket;
import com.newcoder.community.entity.User;
import com.newcoder.community.service.UserService;
import com.newcoder.community.util.CommunityConstant;
import com.newcoder.community.util.CommunityUtil;
import com.newcoder.community.util.MailClient;
import com.newcoder.community.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * @author Yongjiu, X
 * @create 2022-07-07 21:23
 */
@Service
public class UserServiceImpl implements UserService, CommunityConstant {

//    @Autowired
//    private LoginTicketMapper loginTicketMapper;
    //使用RedisTemplate
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    //域名
    @Value("${community.path.domain}")
    private String domain;

    //上下文路径
    @Value("server.servlet.context-path")
    //注意这里可能回报错  因为我用yaml配置的 如果报错就配置到properties里面 没报错
    private String contextPath;


    @Override
    public User findUserById(int userId) {
//        return userMapper.selectById(userId);
        //使用Redis缓存user信息之后 我们不用直接每次都从数据库查数据
        //可以先去查Redis，如果有直接返回，没有再去数据库，然后存入Redi
        //如果变更user信息，直接把Redis的缓存删除，下次要查再从数据库查出来存到Redis里面
        User user = getCache(userId);
        if (user == null){
            user = initCache(userId);
        }
        return user;
    }

    /**
     * 注册的方法 可能会报一系列的错 用Map封装
     * @return
     */
    public Map<String, Object> register(User user){

        Map<String,Object> map = new HashMap<>();
        //首先对空置处理 判断
        if(user == null){
            throw new IllegalArgumentException("参数不能为空！");
        }
        //注意账号空，是一个业务上的漏洞，并不是一个错误
        if (StringUtils.isBlank(user.getUsername())){
            map.put("usernameMessage","账号不能为空");
            return map;
        }
        if (StringUtils.isBlank(user.getPassword())){
            map.put("passwordMessage","密码不能为空");
            return map;
        }
        if (StringUtils.isBlank(user.getEmail())){
            map.put("emailMessage","邮箱不能为空");
            return map;
        }

        //如果以上必要数据都不为空 现在就拿着这些数据去数据库查 如果为新 即可进行下一步
        //如果已经注册过了，返回提升信息

        //验证账号
        User u = userMapper.selectByUserName(user.getUsername());
        if (u != null){
            map.put("usernameMessage","该账号已存在！");
            return map;
        }

        //验证邮箱
        u  = userMapper.selectByEmail(user.getEmail());
        if(u != null){
            map.put("emailMessage","该邮箱已被注册！");
            return map;
        }

        //注册用户 之前应该把user的信息填写完整
        //生成5为的随机字符串
        user.setSalt(CommunityUtil.generateUUID().substring(0,5));
        //用MD5加密后的字符串覆盖密码
        user.setPassword(CommunityUtil.getMD5(user.getPassword() + user.getSalt()));
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode(CommunityUtil.generateUUID());
        //给用户随机生成一个头像 牛客网有随机的1001个头像 images.nowcoder.com/head/(0-1001)t.png
        user.setHeaderUrl(String.format("images.nowcoder.com/head/%dt.png",new Random().nextInt(1000)));
        user.setCreateTime(new Date());

        //ok填写完信息  可以调用userMapper 将user作为一条数据写入到数据库里了
        userMapper.insertUser(user);

        //注册完成之后给用户发送一个HTML的激活邮件
        //前端需要两个参数  url  和 email
        Context context = new Context();
        context.setVariable("email",user.getEmail());
        //http://localhost:9999/community/activation/(userId)/(激活码)
        //这里注意 user 开始传进来的时候 是没有userID的  是userMapper.insertUser(user)
        //调用完成 会自动给user一个id
        String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url",url);
        String content = templateEngine.process("/mail/activation",context);
        mailClient.sendMail(user.getEmail(),"激活账号",content);

        //到此为止  逻辑层处理完毕 即用户提交注册表单 我可以处理数据层和逻辑层 接下来
        //让控制器拦截请求 LoggerController 里面新增 注册方法的拦截
        return map;
    }

    /**
     * 激活的方法
     */
    public int activation(int userId, String activationCode){
        User user = userMapper.selectById(userId);
        if (user.getStatus() == 1){ //表示已经激活
            return ACTIVATION_REPEAT;
        }else if (user.getActivationCode().equals(activationCode)){
            //注意这是错误的  我们需要改的是数据库的 status
//            user.setStatus(1);
            userMapper.updateStatus(userId,1);
            //这个时候，user的信息发生变化，直接把Redis里面的缓存清除
            clearCache(userId);
            //在激活之前还得做一件事 把用户的激活状态改为1
            return ACTIVATION_SUCCESS;
        }else {
            return ACTIVATION_FAILURE;
        }

        //到此为止 逻辑层处理完了 就是返回给客户端的信息处理完了 下面整controller
    }

    /**
     * 登录的方法 登录也可能会遇到一系列的错误 返回的数据仍然用一个Map封装
     * 1.页面传过来的用户密码是明文 数据库里面是加密之后的 所以加密后再与数据库密码对比
     * 2.再传入一个希望多少时间以后过期的int 传入一个秒数
     *
     */
    public Map<String, Object> login(String username, String password, int expiredSeconds){
        Map<String, Object> map = new HashMap<>();

        //空值处理
        if(StringUtils.isBlank(username)){
            map.put("usernameMessage","账号不能为空！");
            return map;
        }
        if(StringUtils.isBlank(password)){
            map.put("passwordMessage","密码不能为空！");
            return map;
        }

        //如果不为空 接下来进行合法性验证

        //验证账号
        User user = userMapper.selectByUserName(username);
        if (user == null){
            map.put("usernameMessage","该账号不存在！");
            return map;
        }
        //如果有账号  判断是否激活
        if (user.getStatus() == 0){
            map.put("usernameMessage","该账号未激活！");
            return map;
        }

        //验证密码
        password = CommunityUtil.getMD5(password + user.getSalt());
        if(!user.getPassword().equals(password)){
            map.put("passwordMessage","密码错误！");
            return map;
        }

        //如果程序执行到这里 说明账号密码都验证通过

        //登录成功 生成登录凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setStatus(0);  //有效的状态
        //有效时间为： 当前时间往后推移 expiredSeconds * 1000 s
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds * 1000));

        //设置好后 传入到数据库
        //loginTicketMapper.insertLoginTicket(loginTicket);

        //重构 存到Redis里面
        String redisKey = RedisKeyUtil.getTicketKey(loginTicket.getTicket());
        redisTemplate.opsForValue().set(redisKey,loginTicket);


        //这个login_ticket 表 就有点类似于 session 的作用 可以让会话进行连续的操作

        //返回给页面
        map.put("ticket",loginTicket.getTicket());

        //到此为止，登录的业务逻辑层写完 接下来可以写controller层 根据页面表单提交过来的
        //username，password，验证码 用逻辑层 处理请求
        return map;
    }

    /**
     * 退出
     * 1.把凭证传给我 我根据凭证 修改状态
     * 2.status=1  无效  0  有效
     */
    public void logout(String ticket){
        //loginTicketMapper.updateStatus(ticket,1);
        String redisKey = RedisKeyUtil.getTicketKey(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(redisKey);
        loginTicket.setStatus(1);
        redisTemplate.opsForValue().set(redisKey,loginTicket);

    }

    /**
     * 查询LoginTicket
     * @param ticket
     * @return
     */
    @Override
    public LoginTicket findLoginTicket(String ticket) {

        //return loginTicketMapper.selectByTicket(ticket);
        String redisKey = RedisKeyUtil.getTicketKey(ticket);
        return (LoginTicket) redisTemplate.opsForValue().get(redisKey);
    }

    //更改头像
    @Override
    public int updateHeader(int userId, String headerUrl) {

//        return userMapper.updateHeaderUrl(userId, headerUrl);
        int rows = userMapper.updateHeaderUrl(userId, headerUrl);
        //更新之后清理掉Redis里面的缓存 为什么更新之后清理，这里两个数据库不能用事务管理
        //如果更改失败 就不用清除缓存
        clearCache(userId);
        return rows;
    }

    @Override
    public User findUserByUsername(String Username) {
        return userMapper.selectByUserName(Username);
    }

    //1.当查询时，优先从缓存中取值
    private User getCache(int userId){
        String redisKey = RedisKeyUtil.getUserKey(userId);
        return (User) redisTemplate.opsForValue().get(redisKey);
    }
    //2.取不到时，初始化缓存数据
    private User initCache(int userId){
        User user = userMapper.selectById(userId);
        String redisKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.opsForValue().set(redisKey,user,3600, TimeUnit.SECONDS);
        return user;
    }
    //3.数据变更时，清除缓存数据
    private void clearCache(int userId){
        String redisKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.delete(redisKey);
    }
}
