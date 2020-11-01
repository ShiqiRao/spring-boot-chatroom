package com.example.chat.repository;

import com.example.chat.entity.TChatMessage;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ChatMessageRepository extends MongoRepository<TChatMessage, String> {


}
