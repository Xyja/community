package com.newcoder.community.util;

/**
 * 工具简单就不用Spring容器来管理  直接提供静态方法供我们访问即可
 * @author Yongjiu, X
 * @create 2022-07-31 15:34
 */
public class RedisKeyUtil {

    private static final String SPLIT = ":";
    private static final String PREFIX_ENTITY_LIKE = "like:entity"; //前缀
    private static final String PREFIX_USER_LIKE = "like:user"; //前缀
    private static final String PREFIX_FOLLWEE = "follwee";
    private static final String PREFIX_FOLLWER = "follwer";
    private static final String PREFIX_KAPTCHA = "kaptcha";
    private static final String PREFIX_TICKET = "ticket";
    private static final String PREFIX_USER = "user";

    //再写一个方法 把这些常量拼接在动态的参数上 即实现点赞的功能的 key  的拼接

    /**
     * 某个实体的赞  的  key  方便往Redis里面存数据
     * like:entity:entityType:entityId -> set(userId) 当我们需要统计赞的数量 和查看点赞的人
     * 用set 的方法就很方便  scard  smembers
     */
    public static String getEntityLikeKey(int entityType, int entityId){

        return PREFIX_ENTITY_LIKE + SPLIT + entityType + SPLIT + entityId;

    }

    /**
     * 对用户的赞
     * like:user:userId  -> int
     */
    public static String getUserLikeKey(int userId){
        return PREFIX_USER_LIKE + SPLIT + userId;
    }

    /**
     * 某个用户关注的实体
     *  followee:userId:entityType -> zset(entityId,now)
     */
    public static String getFolloweeKey(int userId, int entityType){
        return PREFIX_FOLLWEE + SPLIT + userId + SPLIT + entityType;
    }

    /**
     * 某个实体拥有的粉丝
     * follower:entityType:entityId  -> zset(userId,now)
     */
    public static String getFollowerKey(int entityType, int entityId){
        return PREFIX_FOLLWER + SPLIT + entityType + SPLIT + entityId;
    }

    /**
     * 验证码的 key
     * 随机生成一个字符串  发给浏览器的cookie保存 短期有效
     */
    public static String getKaptchaKey(String owner){
        return PREFIX_KAPTCHA + SPLIT + owner;
    }

    /**
     * 登录凭证 ticketKey
     */
    public static String getTicketKey(String ticket){
        return PREFIX_TICKET + SPLIT + ticket;
    }

    /**
     * 用户 userKey
     */
    public static String getUserKey(int userId){
        return PREFIX_USER + SPLIT + userId;
    }
}
