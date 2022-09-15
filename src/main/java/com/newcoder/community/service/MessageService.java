package com.newcoder.community.service;

import com.newcoder.community.entity.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Yongjiu, X
 * @create 2022-07-21 20:38
 */
@Service
public interface MessageService {

    //查询会话
    List<Message> findConversations(int userId, int offset, int limit);

    //查询会话总数量
    int findConversationsCount(int userId);

    List<Message> findLetters(String conversationId, int offset, int limit);

    int findLettersCount(String conversationId);

    int findUnreadLettersCount(int userId, String conversationId);

    //添加消息
    int addMessage(Message message);

    //写一个方法解决显示已读和删除消息
    int readMessage(List<Integer> ids);

    //查询最新的通知
    Message findLatestNotice(int userId, String topic);

    //查询某个主题所包含的通知数量
    int findNoticeCount(int userId, String topic);

    //查询未读通知数量
    int findUnreadNoticeCount(int userId, String topic);

    List<Message> findNotices(int userId, String topic, int offset, int limit);
}
