package com.example.telegrambotapplication.service.redisService;

import com.example.telegrambotapplication.models.redis.RedisHelper;
import com.example.telegrambotapplication.service.QuestionService;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

@RequiredArgsConstructor
@Service
public class RedisHelperService {

    private final String hashReference = "RedisHelper";

    @Autowired
    QuestionService questionService;

    @Resource(name = "template")
    private HashOperations<String, Long, RedisHelper> hashOperations;

    public RedisHelper getRedisHelperByChatId(Long chatId) {
        return hashOperations.get(hashReference, chatId);
    }

    public RedisHelper saveRedis(RedisHelper redisHelper, Update update) {
        hashOperations.put(hashReference, update.getMessage().getChatId(), redisHelper);
        return redisHelper;
    }

    public RedisHelper updateAnswers(Update update, String answer) {
        RedisHelper redisHelper = null;
        if (update.hasCallbackQuery()) {
            redisHelper = getRedisHelperByChatId(update.getCallbackQuery().getMessage().getChatId());
            List<String>answers=redisHelper.getAnswers();
            answers.add(answer);
            redisHelper.setAnswers(answers);
            hashOperations.put(hashReference, update.getCallbackQuery().getMessage().getChatId(), redisHelper);
        } else if (update.hasMessage()){
            redisHelper = getRedisHelperByChatId(update.getMessage().getChatId());
            List<String>answers=redisHelper.getAnswers();
            answers.add(answer);
            redisHelper.setAnswers(answers);
            hashOperations.put(hashReference, update.getMessage().getChatId(), redisHelper);
        }
            return redisHelper;
    }

    public void updateRedisLang(Update update, String lang) {
        RedisHelper redisHelper = getRedisHelperByChatId(update.getCallbackQuery().getMessage().getChatId());
        redisHelper.setLang(lang);
        hashOperations.put(hashReference, update.getCallbackQuery().getMessage().getChatId(), redisHelper);
    }

    public void updateLanguage(RedisHelper redis, Update update, String lang) {
        RedisHelper redisHelper = getByChatId(redis.getChatId());
        redisHelper.setLang(lang);
        hashOperations.put(hashReference, update.getMessage().getChatId(), redisHelper);
    }

    public void updateButtonType(RedisHelper redis, Update update, String buttonType) {
        RedisHelper redisHelper = getByChatId(redis.getChatId());
        redisHelper.setButtonType(buttonType);
        hashOperations.put(hashReference, update.getMessage().getChatId(), redisHelper);
    }

    public void updateRedisNextQuestion(Update update, String key) {
        RedisHelper redisHelper = null;
        if (update.hasCallbackQuery()) {
            redisHelper = getRedisHelperByChatId(update.getCallbackQuery().getMessage().getChatId());
        } else if (update.getMessage().hasText()) {
            redisHelper = getRedisHelperByChatId(update.getMessage().getChatId());
        }
        redisHelper.setNextQuestion(questionService.findQuestionByKey(key).getAction().getNextQuestion());
        hashOperations.put(hashReference, redisHelper.getChatId(), redisHelper);
    }

    public void addAnswer(RedisHelper redis, Update update, String answer) {
        RedisHelper helperRedis = getByChatId(redis.getChatId());
        helperRedis.getAnswers().add(answer);
        helperRedis.getAnswers().remove("s");
        List<String> list = helperRedis.getAnswers();
        helperRedis.setAnswers(list);
        hashOperations.put(hashReference, update.getMessage().getChatId(), helperRedis);
    }

    public RedisHelper getByChatId(Long id) {
        return hashOperations.get(hashReference, id);
    }

    public void removeRedis(Long chatId) {
        hashOperations.delete(hashReference, chatId);
    }
}

