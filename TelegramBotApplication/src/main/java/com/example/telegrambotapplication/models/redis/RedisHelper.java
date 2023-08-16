package com.example.telegrambotapplication.models.redis;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class RedisHelper implements Serializable {
    @Id
    private Long chatId;
    private String nextQuestion;
    private String lang;
    private String buttonType;
    private List<String> answers;
}
