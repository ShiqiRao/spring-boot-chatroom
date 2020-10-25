package com.example.chat.repository;

import com.example.chat.entity.TChatMessage;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ChatMessageRepository extends MongoRepository<TChatMessage, String> {

    List<TChatMessage> findTop10ByOrderByCreateTimeDesc();

}
