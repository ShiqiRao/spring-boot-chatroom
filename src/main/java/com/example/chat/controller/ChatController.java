package com.example.chat.controller;

import com.example.chat.entity.TChatMessage;
import com.example.chat.repository.ChatMessageRepository;
import com.example.chat.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatController {

    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public")
    public TChatMessage sendMessage(@Payload TChatMessage chatMessage) {
        //在记录中留下类型为CHAT的信息
        if (chatMessage.getType() == TChatMessage.MessageType.CHAT) {
            chatMessageRepository.save(chatMessage.setCreateTime(LocalDateTime.now()));
        }
        return chatMessage;
    }

    @MessageMapping("/chat.addUser")
    @SendTo("/topic/public")
    public TChatMessage addUser(@Payload TChatMessage chatMessage,
                                SimpMessageHeaderAccessor headerAccessor) {
        headerAccessor.getSessionAttributes().put("username", chatMessage.getSender());
        return chatMessage;
    }

    @SubscribeMapping("/chat.lastTenMessage")
    public List<TChatMessage> addUser() {
        //读取10条历史记录
        List<TChatMessage> ret = chatMessageRepository.findTop10ByOrderByCreateTimeDesc();
        ret.sort(Comparator.comparing(TChatMessage::getCreateTime));
        return ret;
    }
}
