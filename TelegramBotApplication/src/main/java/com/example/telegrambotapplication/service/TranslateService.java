package com.example.telegrambotapplication.service;

import com.example.telegrambotapplication.models.Translate;
import com.example.telegrambotapplication.repo.TranslateRepo;
import com.example.telegrambotapplication.service.redisService.RedisHelperService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

import javax.swing.*;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TranslateService {

    private final TranslateRepo translateRepo;

    private final RedisHelperService redisHelperService;

    private final ActionService actionService;


    public void saveTranslate(Translate translate) {
        translateRepo.save(translate);
    }

    public Translate getQuestionByKeyAndLang(String key, String lang) {
        return translateRepo.getTranslateByKeyAndLang(key, lang);
    }

    public List<Translate> getQuestionsByKeyAndLang(String key, String lang) {
        return translateRepo.getTranslatesByKeyAndLang(key, lang);
    }

    public List<Translate> getButtonByKeyAndLang(String key, String language) {
        return translateRepo.getTranslatesByKeyAndLang(key, language);
    }

    public List<Translate> getButton(Update update) {
        String lang = redisHelperService.getRedisHelperByChatId(update.getMessage().getChatId()).getLang();
        String buttonName = null;
        if (actionService.findActionByNextQuestion(redisHelperService.getRedisHelperByChatId(
                update.getMessage().getChatId()).getNextQuestion()) != null &&
                actionService.findActionByNextQuestion(
                        redisHelperService.getRedisHelperByChatId(update.getMessage().getChatId()).getNextQuestion()).getActionType().equals("button")) {
            buttonName = actionService.findActionByNextQuestion(redisHelperService.getRedisHelperByChatId(update.getMessage().getChatId())
                    .getNextQuestion()).getButtonName();
            return getButtonByKeyAndLang(buttonName,lang);
        }
        return null;
    }
}
