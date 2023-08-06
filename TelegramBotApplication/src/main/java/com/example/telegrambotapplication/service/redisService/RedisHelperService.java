package com.example.telegrambotapplication.service.redisService;

import com.example.telegrambotapplication.models.redis.RedisHelper;
import com.example.telegrambotapplication.service.QuestionService;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

@RequiredArgsConstructor
@Service
public class RedisHelperService {

    private final String hashReference = "RedisHelper";

    private final QuestionService questionService;

    @Resource(name = "template")
    private HashOperations<String, Long, RedisHelper> hashOperations;

    public RedisHelper getRedisHelperByChatId(Long chatId) {
        return hashOperations.get(hashReference, chatId);
    }

    public RedisHelper saveRedis(RedisHelper redisHelper, Update update) {
//        redisHelper.setChatId(update.getMessage().getChatId());
//        redisHelper.setNextQuestion(update.getMessage().getText());
        hashOperations.put(hashReference, update.getMessage().getChatId(), redisHelper);
        System.out.println("save metodunda cap olundu "+redisHelper);
        return redisHelper;
    }

    public void updateRedisLang(Update update, String lang) {
        RedisHelper redisHelper = getRedisHelperByChatId(update.getMessage().getChatId());
        redisHelper.setLang(lang);
        hashOperations.put(hashReference, update.getMessage().getChatId(), redisHelper);
    }
    public void updateRedisNextQuestion(Update update, String key) {
        RedisHelper redisHelper = getRedisHelperByChatId(update.getMessage().getChatId());
        redisHelper.setNextQuestion(questionService.findQuestionByKey(key).getAction().getNextQuestion());
        hashOperations.put(hashReference, update.getMessage().getChatId(), redisHelper);
    }

    public RedisHelper getByChatId(Long id) {
        return hashOperations.get(hashReference, id);
    }


}

