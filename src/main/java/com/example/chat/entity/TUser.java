package com.example.chat.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Accessors(chain = true)
@Setter
@Getter
public class TUser {

    @Id
    private String id;
    private String username;
    private String password;

}