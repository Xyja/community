package com.newcoder.community.controller;

import com.alibaba.fastjson.JSONObject;
import com.newcoder.community.entity.Message;
import com.newcoder.community.entity.PageInfo;
import com.newcoder.community.entity.User;
import com.newcoder.community.service.MessageService;
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
import org.springframework.web.util.HtmlUtils;

import java.util.*;

/**
 * @author Yongjiu, X
 * @create 2022-07-21 20:47
 */
@Controller
public class MessageController implements CommunityConstant {

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private MessageService messageService;

    @Autowired
    private UserService userService;

    /**
     * 私信列表页面
     * @param model
     * @param pageInfo
     * @return
     */
    @RequestMapping(path = "/letter/list",method = RequestMethod.GET)
    public String getLettersList(Model model, PageInfo pageInfo){
        //设置分页信息
        User user = hostHolder.getUser();
        pageInfo.setLimit(5);
        pageInfo.setPath("/letters/list");
        pageInfo.setRows(messageService.findConversationsCount(user.getId()));
        //会话列表
        List<Message> conversationList = messageService.findConversations(user.getId(),pageInfo.getOffset(),pageInfo.getLimit());
        List<Map<String, Object>> conversations = new ArrayList<>();
        if (conversationList != null){
            for (Message message : conversationList){
                Map<String, Object> map = new HashMap<>();
                map.put("conversation",message);
                map.put("letterCount",messageService.findLettersCount(message.getConversationId()));
                map.put("unreadCount",messageService.findUnreadLettersCount(user.getId(),message.getConversationId()));
                int targetId = user.getId() == message.getFromId() ? message.getToId() : message.getFromId();
                map.put("target", userService.findUserById(targetId));

                conversations.add(map);
            }
        }

        model.addAttribute("conversations",conversations);

        //查询未读消息数量
        int letterUnreadCount = messageService.findUnreadLettersCount(user.getId(),null);
        model.addAttribute("letterUnreadCount",letterUnreadCount);

        //查询未读通知的数量
        int noticeUnreadCount = messageService.findUnreadNoticeCount(user.getId(),null);
        model.addAttribute("noticeUnreadCount",noticeUnreadCount);
        return "/site/letter";

    }

    /**
     * 私信详情页面
     * @param conversationId
     * @param pageInfo
     * @param model
     * @return
     */
    @RequestMapping(path = "/letter/detail/{conversationId}",method = RequestMethod.GET)
    public String getLetterDetail(@PathVariable("conversationId") String conversationId,
                                  PageInfo pageInfo, Model model){
        //分页信息
        pageInfo.setLimit(5);
        pageInfo.setPath("/letter/detail" + conversationId);
        pageInfo.setRows(messageService.findLettersCount(conversationId));

        //私信列表
        List<Message> letterList = messageService.findLetters(conversationId,pageInfo.getOffset(),pageInfo.getLimit());
        List<Map<String, Object>> letters = new ArrayList<>();
        if (letterList != null){
            for (Message m : letterList){
                Map<String, Object> map = new HashMap<>();
                map.put("letter",m);
                map.put("fromUser",userService.findUserById(m.getFromId()));
                letters.add(map);

            }
        }
        model.addAttribute("letters",letters);

        //私信目标
        model.addAttribute("target",getLetterTarget(conversationId));

        //提取出未读的消息 设置为已读
        List<Integer> ids = getLetterIds(letterList);
        if (!ids.isEmpty()){
            messageService.readMessage(ids);
        }

        return "/site/letter-detail";


    }

    /**
     * 发送私信
     * @param toName
     * @param content
     * @return
     */
    @RequestMapping(path = "/letter/send",method = RequestMethod.POST)
    @ResponseBody
    public String sendLetter(String toName, String content){
        User target = userService.findUserByUsername(toName);
        if (target == null){
            return CommunityUtil.getJSONString(1,"目标用户不存在！");
        }

        Message message = new Message();
        message.setFromId(hostHolder.getUser().getId());
        message.setToId(target.getId());
        if (message.getFromId() < message.getToId()){
            message.setConversationId(message.getFromId() + "_" + message.getToId());
        }else {
            message.setConversationId(message.getToId() + "_" + message.getFromId());
        }
        message.setContent(content);
        message.setCreateTime(new Date());

        //Message封装完毕  可以插入数据库
        messageService.addMessage(message);
        System.out.println("heollllll.00000");
        return CommunityUtil.getJSONString(0);
    }


    /**
     * 查询通知列表   评论 点赞 关注
     * @param model
     * @return
     */
    @RequestMapping(path = "/notice/list",method = RequestMethod.GET)
    public String getNoticeList(Model model){
        //查谁的
        User user = hostHolder.getUser();
        //用什么封装数据
        Map<String, Object> messageVo = new HashMap<>();

        //查询评论通知
        Message message = messageService.findLatestNotice(user.getId(), TOPIC_COMMENT);
        if (message != null){
            messageVo.put("message",message);
            //把content内容取消转义
            String content = HtmlUtils.htmlUnescape(message.getContent());
            //把json字符串变成map
            Map<String, Object> data = JSONObject.parseObject(content,HashMap.class);
            //取出data里面的数据并封装到messageVo里面 后面好发给浏览器
            messageVo.put("user",userService.findUserById((Integer) data.get("userId")));
            messageVo.put("entityType",data.get("entityType"));
            messageVo.put("entityId",data.get("entityId"));
            messageVo.put("postId",data.get("postId"));

            int count = messageService.findNoticeCount(user.getId(),TOPIC_COMMENT);
            messageVo.put("count",count);

            int unread = messageService.findUnreadNoticeCount(user.getId(),TOPIC_COMMENT);
            messageVo.put("unread",unread);

        }
        model.addAttribute("commentNotice",messageVo);

        //查点赞类通知
        messageVo = new HashMap<>();
        message = messageService.findLatestNotice(user.getId(), TOPIC_LIKE);
        if (message != null){
            messageVo.put("message",message);
            //把content内容取消转义
            String content = HtmlUtils.htmlUnescape(message.getContent());
            //把json字符串变成map
            Map<String, Object> data = JSONObject.parseObject(content,HashMap.class);
            //取出data里面的数据并封装到messageVo里面 后面好发给浏览器
            messageVo.put("user",userService.findUserById((Integer) data.get("userId")));
            messageVo.put("entityType",data.get("entityType"));
            messageVo.put("entityId",data.get("entityId"));
            messageVo.put("postId",data.get("postId"));

            int count = messageService.findNoticeCount(user.getId(),TOPIC_LIKE);
            messageVo.put("count",count);

            int unread = messageService.findUnreadNoticeCount(user.getId(),TOPIC_LIKE);
            messageVo.put("unread",unread);
        }
        model.addAttribute("likeNotice",messageVo);

        //查关注类通知
        messageVo = new HashMap<>();
        message = messageService.findLatestNotice(user.getId(), TOPIC_FOLLOW);
        if (message != null){
            messageVo.put("message",message);
            //把content内容取消转义
            String content = HtmlUtils.htmlUnescape(message.getContent());
            //把json字符串变成map
            Map<String, Object> data = JSONObject.parseObject(content,HashMap.class);
            //取出data里面的数据并封装到messageVo里面 后面好发给浏览器
            messageVo.put("user",userService.findUserById((Integer) data.get("userId")));
            messageVo.put("entityType",data.get("entityType"));
            messageVo.put("entityId",data.get("entityId"));


            int count = messageService.findNoticeCount(user.getId(),TOPIC_FOLLOW);
            messageVo.put("count",count);

            int unread = messageService.findUnreadNoticeCount(user.getId(),TOPIC_FOLLOW);
            messageVo.put("unread",unread);
        }
        model.addAttribute("followNotice",messageVo);

        //查询总的未读消息数量  分私信 和通知需要查两次
        //私信
        int letterUnreadCount = messageService.findUnreadLettersCount(user.getId(),null);
        model.addAttribute("letterUnreadCount",letterUnreadCount);
        //通知
        int noticeUnreadCount = messageService.findUnreadNoticeCount(user.getId(),null);
        model.addAttribute("noticeUnreadCount",noticeUnreadCount);

        return "/site/notice";
    }

    @RequestMapping(path = "/notice/detail/{topic}",method = RequestMethod.GET)
    public String getNoticeDetails(@PathVariable("topic") String topic, PageInfo pageInfo,
                                   Model model){
        User user = hostHolder.getUser();

        pageInfo.setLimit(5);
        pageInfo.setPath("/notice/detail" + topic);
        pageInfo.setRows(messageService.findNoticeCount(user.getId(),topic));

        List<Message> noticeList = messageService.findNotices(user.getId(), topic, pageInfo.getOffset(), pageInfo.getLimit());
        List<Map<String, Object>> noticeVoList = new ArrayList<>();
        if (noticeList != null){
            for (Message notice : noticeList){
                Map<String, Object> map = new HashMap<>();
                //通知
                map.put("notice",notice);
                //内容
                String content = HtmlUtils.htmlUnescape(notice.getContent());
                Map<String, Object> data = JSONObject.parseObject(content,HashMap.class);
                map.put("user",userService.findUserById((Integer) data.get("userId")));
                map.put("entityType",data.get("entityType"));
                map.put("entityId",data.get("entityId"));
                map.put("postId",data.get("postId"));
                //通知作者
                map.put("fromUser",userService.findUserById(notice.getFromId()));
                noticeVoList.add(map);
            }
        }
        model.addAttribute("notices",noticeVoList);

        //设置已读
        List<Integer> ids = getLetterIds(noticeList);
        if (!ids.isEmpty()){
            messageService.readMessage(ids);
        }

        return "/site/notice-detail";

    }


    private List<Integer> getLetterIds(List<Message> letterList){
        List<Integer> ids = new ArrayList<>();
        if (letterList != null){
            for (Message message : letterList){
                if (hostHolder.getUser().getId() == message.getToId() && message.getStatus()==0){
                    ids.add(message.getId());
                }
            }
        }
        return ids;
    }


    private User getLetterTarget(String conversationId){
        String[] ids = conversationId.split("_");
        int id0 = Integer.parseInt(ids[0]);
        int id1 = Integer.parseInt(ids[1]);

        if (hostHolder.getUser().getId() == id0){
            return userService.findUserById(id1);
        }else {
            return userService.findUserById(id0);
        }

    }
}
