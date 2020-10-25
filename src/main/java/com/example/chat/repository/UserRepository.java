package com.example.chat.repository;

import com.example.chat.entity.TUser;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<TUser, String> {
}
