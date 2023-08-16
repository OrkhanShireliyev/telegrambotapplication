package com.example.telegrambotapplication.repo;

import com.example.telegrambotapplication.models.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepo extends JpaRepository<Message,Long> {
    Message getMessageByKeyAndLang(String key,String lang);
}
