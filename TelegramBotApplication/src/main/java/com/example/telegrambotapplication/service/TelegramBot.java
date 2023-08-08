package com.example.telegrambotapplication.service;

import com.example.telegrambotapplication.config.TelegramConfig;
import com.example.telegrambotapplication.models.Action;
import com.example.telegrambotapplication.models.Translate;
import com.example.telegrambotapplication.models.redis.RedisHelper;
import com.example.telegrambotapplication.service.redisService.RedisHelperService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramBot extends TelegramLongPollingBot {
    private final TelegramConfig telegramConfig;

    private final TranslateService translateService;

    private final RedisHelperService redisHelperService;

    private final QuestionService questionService;

    private final ActionService actionService;


    @Override
    public void onUpdateReceived(Update update) {
        SendMessage sendMessage = new SendMessage();


//        checkLang(update, chatId);
//
//        String key=actionService.findActionByNextQuestion(redisHelperService.getRedisHelperByChatId(update.getMessage().getChatId()).getNextQuestion()).getButtonName();
//        String lang = redisHelperService.getRedisHelperByChatId(update.getMessage().getChatId()).getLang();
//        translateService.getButtonByKeyAndLang(key,lang);


        if (!update.hasCallbackQuery() && update.getMessage().getText().equals("/start")) {
            Long chatId = update.getMessage().getChatId();
            sendMessage.setChatId(chatId);
            checkLang(update, chatId);
            doPeriod(update, sendMessage, chatId);
        } else if (update.hasCallbackQuery()) {
            if (update.getCallbackQuery().getData().equals("Azerbaycan")){
                redisHelperService.updateRedisLang(update,"Azerbaycan");
            }else if (update.getCallbackQuery().getData().equals("English")){
                redisHelperService.updateRedisLang(update,"English");
            }else if (update.getCallbackQuery().getData().equals("Pусский")){
                redisHelperService.updateRedisLang(update,"Pусский");
            }
            System.out.println(update.getCallbackQuery().getData());
            Long id = update.getCallbackQuery().getMessage().getChatId();
            sendMessage.setChatId(id);
            doPeriod(update, sendMessage, id);
        }
        setButton(update,sendMessage);


        try {
            execute(sendMessage);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public void checkLang(Update update, Long chatId) {

        if (update.getMessage().getText().equals("/start")) {

            redisHelperService.saveRedis(new RedisHelper(chatId, update.getMessage().getText(), "Azerbaycan"), update);
            System.out.println(redisHelperService.getByChatId(chatId));
        }
    }

    public void doPeriod(Update update, SendMessage sendMessage, Long chatId) {
//        if (update.hasCallbackQuery()) {
            RedisHelper redisHelper = redisHelperService.getByChatId(chatId);
            System.out.println(redisHelper);
            Translate translate = translateService.getQuestionByKeyAndLang(redisHelper.getNextQuestion(), redisHelper.getLang());
            sendMessage.setText(translate.getValue());
            String redisNextQuestion = redisHelperService.getRedisHelperByChatId(chatId).getNextQuestion();
            redisHelperService.updateRedisNextQuestion(update, redisNextQuestion);
//        }
        System.out.println(redisHelperService.getRedisHelperByChatId(chatId));
    }

    private ReplyKeyboard setButton(Update update, SendMessage sendMessage) {
        Long chatId=null;
        if (update.hasCallbackQuery()){
            chatId = update.getCallbackQuery().getMessage().getChatId();
        }else if (update.getMessage().hasText()){
            chatId=update.getMessage().getChatId();
        }

        sendMessage.setChatId(chatId);
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<InlineKeyboardButton> inlineKeyboardButtons = new ArrayList<>();
        RedisHelper redisHelperChatId = redisHelperService.getRedisHelperByChatId(chatId);
        String buttonName = actionService.findActionByNextQuestion(redisHelperChatId.getNextQuestion()).getButtonName();
        System.out.println(buttonName);
        String lang = redisHelperChatId.getLang();
        List<Translate> translates = translateService.getQuestionsByKeyAndLang(buttonName, lang);

        translates.stream().forEach(translate -> {
                    InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();

                    inlineKeyboardButton.setText(translate.getValue());
                    inlineKeyboardButton.setCallbackData(translate.getValue());

                    inlineKeyboardButtons.add(inlineKeyboardButton);
                }
        );
        List<List<InlineKeyboardButton>> lists = new ArrayList<>();
        lists.add(inlineKeyboardButtons);

        inlineKeyboardMarkup.setKeyboard(lists);

        sendMessage.setReplyMarkup(inlineKeyboardMarkup);

        return inlineKeyboardMarkup;
    }


    @Override
    public String getBotUsername() {
        return telegramConfig.getName();
    }

    @Override
    public String getBotToken() {
        return telegramConfig.getToken();
    }

    private void saveFileToFolder(String fileId, String fileName) throws Exception {
        GetFile getFile = new GetFile(fileId);
        File file = execute(getFile);
        String fileUrl = file.getFileUrl(getBotToken());
        URL url = new URL(fileUrl);
        InputStream inputStream = url.openStream();

        FileUtils.copyInputStreamToFile(inputStream, new java.io.File(fileName));
    }

    private void sendText(String chatId, String text) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(text);

        try {
            execute(sendMessage);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
