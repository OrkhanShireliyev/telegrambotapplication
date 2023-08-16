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

    private final MessageService messageService;
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

    public String answerCheck(Update update) {

        if (
                update.getMessage().getText() != null &&
                !update.getMessage().getText().equals("/start") &&
                !update.getMessage().getText().equals("/stop") &&
                redisHelperService.getByChatId(update.getMessage().getChatId()) != null &&
                actionService.findActionByNextQuestion(redisHelperService.getByChatId(update.getMessage().getChatId()).
                        getNextQuestion()) != null &&
                actionService.findActionByNextQuestion(redisHelperService.getByChatId(update.getMessage().getChatId()).
                        getNextQuestion()).getActionType().equals("button")
        ) {
            return buttonMessage(update);
        } else {
            if (update.getMessage().getText() != null &&
                    !update.getMessage().getText().equals("/start") &&
                    !update.getMessage().getText().equals("/stop") &&
                    redisHelperService.getByChatId(update.getMessage().getChatId()) != null &&
                    actionService.findActionByNextQuestion(redisHelperService.getByChatId(update.getMessage().getChatId()).getNextQuestion()) != null &&
                    actionService.findActionByNextQuestion(redisHelperService.getByChatId(update.getMessage().getChatId()).
                            getNextQuestion()).getActionType().equals("freeText")
            ) {
                if (redisHelperService.getByChatId(update.getMessage().getChatId()).getNextQuestion().equals("endTime")) {
                    System.out.println("endTime");
                    System.out.println(redisHelperService.getByChatId(update.getMessage().getChatId()).getAnswers());
                    return messageService.messageStartDate(update);
                }
                if (redisHelperService.getByChatId(update.getMessage().getChatId()).getNextQuestion().equals("withSb")) {
                    return messageService.messageEndDate(update);
                }

            } else if (update.getMessage().getText() != null
                    && !update.getMessage().getText().equals("/start")
                    && !update.getMessage().getText().equals("/stop")
                    && redisHelperService.getByChatId(update.getMessage().getChatId()) != null
                    && actionService.findActionByNextQuestion(redisHelperService.
                    getByChatId(update.getMessage().getChatId()).getNextQuestion()) != null
                    && actionService.findActionByNextQuestion(redisHelperService.getByChatId(update.getMessage().getChatId()).
                    getNextQuestion()).getActionType().equals("freeText")
                    && !redisHelperService.getByChatId(update.getMessage().getChatId()).getNextQuestion().equals("endTime")
                    && !redisHelperService.getByChatId(update.getMessage().getChatId()).getNextQuestion().equals("search")
                    && !redisHelperService.getByChatId(update.getMessage().getChatId()).getNextQuestion().equals("withSb")
            ) {
                Long chatId = update.getMessage().getChatId();
                System.out.println(update.getMessage().getText());
                redisHelperService.addAnswer(redisHelperService.getByChatId(chatId), update, update.getMessage().getText());
            }
            if (redisHelperService.getByChatId(update.getMessage().getChatId()).getNextQuestion().equals("search")) {
                if (money.equals(update.getMessage().getText())) {
                    System.out.println(update.getMessage().getText());
                    redisHelperService.addAnswer(redisHelperService.getByChatId(update.getMessage().getChatId()), update, update.getMessage().getText());
//                    messageService.endSave(update);
                } else {
                    Long id = update.getMessage().getChatId();
                    return messageService.getMessageByKeyandLang("money", redisHelperService.getByChatId(id).getLang()).getValue();
                }
            }
        }
        return null;
    }

    public String buttonMessage(Update update) {
        System.out.println("button messageye girdi");
        String buttonKey = actionService.findActionByNextQuestion(redisHelperService.getByChatId(update.getMessage().getChatId()).
                getNextQuestion()).getButtonName();
        String language = redisHelperService.getByChatId(update.getMessage().getChatId()).getLang();
        System.out.println("fora catdi");
        for (Translate translate : getButtonByKeyAndLang(buttonKey, language)
        ) {
            System.out.println("fora girdi");
            String value = translate.getValue();
            if (value.equals(update.getCallbackQuery().getMessage().getText())) {
                System.out.println("1ci ife girdi girdi");
                if (translate.getKey().equals("lang")) {
                    System.out.println("2ci ife girdi girdi");
                    redisHelperService.updateLanguage(redisHelperService.getByChatId(update.getMessage().getChatId()), update, update.getMessage().getText());
                }
                System.out.println("Orxan istediyi MELUMAT-----------"+update.getCallbackQuery().getMessage().getText());
                if (update.getCallbackQuery().getData().equals("Ozum teklif edirem") ||
                        update.getCallbackQuery().getMessage().getText().equals("I offer it myself") ||
                        update.getCallbackQuery().getMessage().getText().equals("Я предлагаю это сам")) {
                    System.out.println("offere dirdi");
                    redisHelperService.updateButtonType(redisHelperService.getByChatId(update.getMessage().getChatId()), update, "freeText");
                    if (redisHelperService.getByChatId(update.getMessage().getChatId()).getLang().equals("Azerbaycan")) {
                        return "\n" +
                                "Zəhmət olmasa, həmin yerin adını yaz  ✏️";
                    } else if (redisHelperService.getByChatId(update.getMessage().getChatId()).getLang().equals("English")) {
                        return "\n" +
                                "Please write the name of the place  ✏️";
                    } else if (redisHelperService.getByChatId(update.getMessage().getChatId()).getLang().equals("Русский")) {
                        return "\n" +
                                "Пожалуйста, напишите название места  ✏️";
                    }
                }
                redisHelperService.addAnswer(redisHelperService.getByChatId(update.getMessage().getChatId()), update, update.getMessage().getText());
                return null;
            }
        }
        if (update.getMessage().getText() != null && redisHelperService.getByChatId(update.getMessage().getChatId()).getLang().equals("Azerbaycan")
                && redisHelperService.getByChatId(update.getMessage().getChatId()).getButtonType().equals("button")) {
            return messageService.getMessageByKeyandLang("btn", redisHelperService.getByChatId(update.getMessage().getChatId()).getLang()).getValue();
        } else if (update.getMessage().getText() != null && redisHelperService.getByChatId(update.getMessage().getChatId()).getLang().equals("English") &&
                redisHelperService.getByChatId(update.getMessage().getChatId()).getButtonType().equals("button")) {
            return messageService.getMessageByKeyandLang("btn", redisHelperService.getByChatId(update.getMessage().getChatId()).getLang()).getValue();
        } else if (update.getMessage().getText() != null && redisHelperService.getByChatId(update.getMessage().getChatId()).getLang().equals("Русский")
                && redisHelperService.getByChatId(update.getMessage().getChatId()).getButtonType().equals("button")) {
            return messageService.getMessageByKeyandLang("btn", redisHelperService.getByChatId(update.getMessage().getChatId()).getLang()).getValue();
        }
        return null;
    }
}
