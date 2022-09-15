package com.newcoder.community.service.impl;

import com.newcoder.community.dao.MessageMapper;
import com.newcoder.community.entity.Message;
import com.newcoder.community.service.MessageService;
import com.newcoder.community.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import javax.swing.text.html.HTML;
import java.util.List;

/**
 * @author Yongjiu, X
 * @create 2022-07-21 20:40
 */
@Service
public class MessageServiceImpl implements MessageService {

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Autowired
    private MessageMapper messageMapper;

    @Override
    public List<Message> findConversations(int userId, int offset, int limit) {
        return messageMapper.selectConversations(userId,offset,limit);
    }

    @Override
    public int findConversationsCount(int userId) {
        return messageMapper.selectConversationsCount(userId);
    }

    @Override
    public List<Message> findLetters(String conversationId, int offset, int limit) {
        return messageMapper.selectLetters(conversationId,offset,limit);
    }

    @Override
    public int findLettersCount(String conversationId) {
        return messageMapper.selectLettersCount(conversationId);
    }

    @Override
    public int findUnreadLettersCount(int userId, String conversationId) {
        return messageMapper.selectLetterUnreadCount(userId,conversationId);
    }

    @Override
    public int addMessage(Message message) {
        message.setContent(HtmlUtils.htmlEscape(message.getContent()));
        message.setContent(sensitiveFilter.replaceSensitiveWords(message.getContent()));
        return messageMapper.insertMessage(message);

    }

    @Override
    public int readMessage(List<Integer> ids){
        return messageMapper.updateStatus(ids,1);
    }

    @Override
    public Message findLatestNotice(int userId, String topic) {

        return messageMapper.selectLatestNotice(userId,topic);
    }

    @Override
    public int findNoticeCount(int userId, String topic) {
        return messageMapper.selectNoticeCount(userId,topic);
    }

    @Override
    public int findUnreadNoticeCount(int userId, String topic) {
        return messageMapper.selectNoticeUnreadCount(userId,topic);
    }

    @Override
    public List<Message> findNotices(int userId, String topic, int offset, int limit) {
        return messageMapper.selectNotices(userId, topic, offset, limit);
    }
}
