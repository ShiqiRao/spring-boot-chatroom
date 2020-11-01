package com.example.chat.controller;

import com.example.chat.entity.TChatMessage;
import com.example.chat.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;


@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatController {

    private final ChatMessageRepository chatMessageRepository;
    private final MongoTemplate mongoTemplate;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(Principal principal, @Payload TChatMessage chatMessage) {
        //在记录中留下类型为CHAT的信息
        if (chatMessage.getType() == TChatMessage.MessageType.CHAT) {
            chatMessageRepository.save(chatMessage.setCreateTime(LocalDateTime.now()));
        }
        if (StringUtils.isEmpty(chatMessage.getReceiver())) {
            messagingTemplate.convertAndSend("/topic/public", chatMessage);
        } else {
            //回传给发信人
            messagingTemplate.convertAndSendToUser(chatMessage.getSender(), "/notification", chatMessage);
            //发送给收信人
            messagingTemplate.convertAndSendToUser(chatMessage.getReceiver(), "/notification", chatMessage);
        }

    }

    @MessageMapping("/chat.addUser")
    @SendTo("/topic/public")
    public TChatMessage addUser(@Payload TChatMessage chatMessage,
                                SimpMessageHeaderAccessor headerAccessor) {
        headerAccessor.getSessionAttributes().put("username", chatMessage.getSender());
        return chatMessage;
    }

    @SubscribeMapping("/chat.lastTenMessage")
    public List<TChatMessage> addUser(Principal principal) {
        //读取10条历史记录
        Query query = new Query();
        //查询群聊、自己发送或者发送给自己的历史记录
        Criteria criteria = new Criteria().orOperator(
                Criteria.where("receiver").is(null),
                Criteria.where("sender").is(principal.getName()),
                Criteria.where("receiver").is(principal.getName()));
        query.addCriteria(criteria)
                //按时间倒序
                .with(Sort.by(Sort.Direction.DESC, "createTime"))
                .limit(10);
        List<TChatMessage> ret = mongoTemplate.find(query, TChatMessage.class);
        ret.sort(Comparator.comparing(TChatMessage::getCreateTime));
        return ret;
    }
}
