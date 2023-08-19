package com.example.telegrambotapplication.service;

import com.example.telegrambotapplication.config.TelegramConfig;
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

    private final QuestionService questionService;
    private final TranslateService translateService;
    private final ActionService actionService;
    private final RedisHelperService redisHelperService;

    @Override
    public void onUpdateReceived(Update update) {
        SendMessage sendMessage = new SendMessage();

        checkLang(update, sendMessage);

        try {
            execute(sendMessage);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void checkLang(Update update, SendMessage sendMessage) {

        if (!update.hasCallbackQuery() && update.getMessage().getText().equals("/start")) {
            Long chatId = update.getMessage().getChatId();
            sendMessage.setChatId(chatId);
            startPoint(update, chatId);
            doPeriod(update, sendMessage, chatId);
            setButton(update, sendMessage);
//            translateService.answerCheck(update);

            System.out.println(update.getMessage().getText());
        } else if (update.hasCallbackQuery()) {
            hasCallBackMessage(update, sendMessage);
        } else if (update.hasMessage() && !update.getMessage().getText().equals("/start") &&
                redisHelperService.getByChatId(update.getMessage().getChatId()).getNextQuestion() != null) {
            hasMessage(update, sendMessage);
        } else if (update.hasMessage() && redisHelperService.getByChatId(update.getMessage().getChatId()).getNextQuestion() == null) {
            endPoint(update, sendMessage);
        }
    }

    public void startPoint(Update update, Long chatId) {
        if (update.getMessage().getText().equals("/start")) {
            List<String> answers = new ArrayList<>();
            redisHelperService.saveRedis(new RedisHelper(chatId, update.getMessage().getText(), "Azerbaycan", "button", answers), update);
            System.out.println(redisHelperService.getByChatId(chatId));
        }
    }

    public void endPoint(Update update, SendMessage sendMessage) {
        Long chatId1 = update.getMessage().getChatId();
        sendMessage.setChatId(chatId1);
        redisHelperService.getByChatId(chatId1).getAnswers();

        if (redisHelperService.getByChatId(chatId1).getLang().equals("Azerbaycan")) {
            sendMessage.setText("Suallariniz bitdi.Yeni bir sorguya baslamaq isteyirsinizse /start yazin");
        } else if (redisHelperService.getByChatId(chatId1).getLang().equals("English")) {
            sendMessage.setText("Your questions are finished.If you want to start a new question, type /start");
        } else if (redisHelperService.getByChatId(chatId1).getLang().equals("Pусский")) {
            sendMessage.setText("Ваши вопросы закончены. Если вы хотите начать новый вопрос, введите /start");
        }
    }

    public void hasMessage(Update update, SendMessage sendMessage) {
        Long chatId1 = update.getMessage().getChatId();
        sendMessage.setChatId(chatId1);
        doPeriod(update, sendMessage, chatId1);
        String answer = update.getMessage().getText();
        redisHelperService.updateAnswers(update, answer);
        redisHelperService.getByChatId(chatId1).getAnswers().stream().forEach(System.out::println);
        System.out.println(redisHelperService.getRedisHelperByChatId(chatId1).getNextQuestion());

    }

    public void hasCallBackMessage(Update update, SendMessage sendMessage) {
        if (update.getCallbackQuery().getData().equals("Azerbaycan")) {
            redisHelperService.updateRedisLang(update, "Azerbaycan");
        } else if (update.getCallbackQuery().getData().equals("English")) {
            redisHelperService.updateRedisLang(update, "English");
        } else if (update.getCallbackQuery().getData().equals("Pусский")) {
            redisHelperService.updateRedisLang(update, "Pусский");
        }
        String answer = update.getCallbackQuery().getData();
        System.out.println(answer);
        Long chatId1 = update.getCallbackQuery().getMessage().getChatId();
        sendMessage.setChatId(chatId1);
        doPeriod(update, sendMessage, chatId1);
        setButton(update, sendMessage);
        System.out.println(update.getCallbackQuery().getMessage().getText());
        redisHelperService.updateAnswers(update, answer);
        redisHelperService.getByChatId(chatId1).getAnswers().stream().forEach(System.out::println);
    }

    public void doPeriod(Update update, SendMessage sendMessage, Long chatId) {
        RedisHelper redisHelper = redisHelperService.getByChatId(chatId);
        System.out.println(redisHelper);
        Translate translate = translateService.getQuestionByKeyAndLang(redisHelper.getNextQuestion(), redisHelper.getLang());
        sendMessage.setText(translate.getValue());
        String redisNextQuestion = redisHelperService.getRedisHelperByChatId(chatId).getNextQuestion();
        redisHelperService.updateRedisNextQuestion(update, redisNextQuestion);
        System.out.println(redisHelperService.getRedisHelperByChatId(chatId));
    }

    private ReplyKeyboard setButton(Update update, SendMessage sendMessage) {
        Long chatId = null;
        if (update.hasCallbackQuery()) {
            chatId = update.getCallbackQuery().getMessage().getChatId();
        } else if (update.getMessage().hasText()) {
            chatId = update.getMessage().getChatId();
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
