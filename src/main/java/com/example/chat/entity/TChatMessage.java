package com.example.chat.entity;

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
    private MessageType type;
    private String content;
    private String sender;
    private LocalDateTime createTime;

    public enum MessageType {
        CHAT,
        JOIN,
        LEAVE
    }
}
