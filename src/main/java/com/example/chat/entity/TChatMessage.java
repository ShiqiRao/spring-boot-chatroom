package com.example.chat.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document
@Accessors(chain = true)
@Setter
@Getter
public class TChatMessage {
    @Id
    private String id;
    /**
     * 消息类型
     */
    private MessageType type;
    /**
     * 消息内容
     */
    private String content;
    /**
     * 发件人
     */
    private String sender;
    /**
     * 收件人
     */
    private String receiver;
    @JsonIgnore
    private LocalDateTime createTime;

    public enum MessageType {
        CHAT,
        JOIN,
        LEAVE
    }
}
