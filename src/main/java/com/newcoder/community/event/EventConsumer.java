package com.newcoder.community.event;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.newcoder.community.entity.Event;
import com.newcoder.community.entity.Message;
import com.newcoder.community.service.MessageService;
import com.newcoder.community.util.CommunityConstant;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Yongjiu, X
 * @create 2022-08-01 19:55
 */
@Component
public class EventConsumer implements CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);

    @Autowired
    private MessageService messageService;

    @KafkaListener(topics = {TOPIC_COMMENT,TOPIC_FOLLOW,TOPIC_LIKE})
    public void handleCommentLikeFollowMessage(ConsumerRecord record){
        if (record == null || record.value() == null){
            logger.error("消息内容为空！");
            return;
        }
        Event event = JSONObject.parseObject(record.value().toString(),Event.class);
        if (event == null){
            logger.error("消息格式错误！");
            return;
        }


        //发送站内通知
        Message message = new Message();
        message.setFromId(SYSTEM_USER_ID);
        message.setToId(event.getEntityUserId());
        message.setConversationId(event.getTopic());
        message.setCreateTime(new Date());

        //message的content字段是存储的某个事件  如 某某某关注了某某某
        //所以我们需要这些数据

        Map<String, Object> content = new HashMap<>();
        content.put("userId",event.getUserId());
        content.put("entityType",event.getEntityType());
        content.put("entityId",event.getEntityId());

        //然后event里面一些杂七杂八的数据也去处理存入content字段
        if (!event.getData().isEmpty()){
            for (Map.Entry<String,Object> entry : event.getData().entrySet()){
                content.put(entry.getKey(),entry.getValue());
            }
        }
        //存好content字段的数据
        message.setContent(JSONObject.toJSONString(content));
        messageService.addMessage(message);

        //我们主要是调用生产者，什么时候触发事件，什么时候调用，消费者是一直监听着topic的
        //不用管
    }

    /**
     * 消费发帖事件
     */
    @KafkaListener(topics = {TOPIC_PUBLISH})
    public void handlePublishMessage(){

    }
}
