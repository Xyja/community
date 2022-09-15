package com.newcoder.community.util;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.util.Map;
import java.util.UUID;

/**
 * 此类   提供两个注册的方法  不交给Spring管理  很简单的方法
 * @author Yongjiu, X
 * @create 2022-07-12 18:45
 */
public class CommunityUtil {

    //生成随机字符串
    public static String generateUUID(){
        //UUID生成的字符串 带 -   需要把  -  替换成 空字符
        return UUID.randomUUID().toString().replaceAll("-","");
    }

    //MD5加密  MD5 只能加密不能解密 即不能解密成明文
    //简单的 abc md5 ->  dafsdkgir 但其实别人有一个密码本 可以对照
    //所以一般在用户设置的密码后面 拼接一个随机字符串 这样加密的密文就是唯一的了
    public static String getMD5(String key){
        if(StringUtils.isBlank(key)){
            return null;
        }
        return DigestUtils.md5DigestAsHex(key.getBytes());
    }

    /**
     * 返回给浏览器一个JSON字符串  我们这个方法要做的是
     * 把传进来的参数 封装成一个 JSONString
     * @param code
     * @param msg
     * @param map
     * @return
     */
    public static String getJSONString(int code, String msg, Map<String, Object> map){
        JSONObject json = new JSONObject();
        json.put("code",code);
        json.put("msg",msg);
        if (map != null){
            for (String key : map.keySet()){
                json.put("key",map.get(key));
            }
        }
        return json.toJSONString();
    }

    /**
     * 有时候方法没有数据 和提示  只有 code 因此重载一下该方法，便于调用
     */
    public static String getJSONString(int code, String msg){
        return getJSONString(code,msg,null);
    }

    public static String getJSONString(int code){
        return getJSONString(code,null,null);
    }

}
