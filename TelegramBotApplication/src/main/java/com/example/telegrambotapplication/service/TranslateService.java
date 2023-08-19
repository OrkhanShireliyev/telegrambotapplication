package com.example.telegrambotapplication.service;

import com.example.telegrambotapplication.models.Translate;
import com.example.telegrambotapplication.repo.TranslateRepo;
import com.example.telegrambotapplication.service.redisService.RedisHelperService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class TranslateService {

    private final QuestionService questionService;
    private final RedisHelperService redisHelperService;
    private final ActionService actionService;

    private final TranslateRepo translateRepo;

    public Pattern money = Pattern.compile("r'^\\d{4}$'");

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

    public List<Translate> getStartButton(Update update) {
        String language = redisHelperService.getByChatId(update.getMessage().getChatId()).getLang();
        String buttonKey = null;
        if (actionService.findActionByNextQuestion(redisHelperService.getByChatId(update.getMessage().getChatId()).
                getNextQuestion()) != null &&
                actionService.findActionByNextQuestion(redisHelperService.getByChatId(update.getMessage().getChatId()).
                        getNextQuestion()).getActionType().equals("button")) {
            buttonKey = actionService.findActionByNextQuestion(redisHelperService.getByChatId(update.getMessage().getChatId()).
                    getNextQuestion()).getButtonName();
            return getButtonByKeyAndLang(buttonKey, language);
        }
        return null;
    }
}
