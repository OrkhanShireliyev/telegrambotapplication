package com.example.telegrambotapplication.service;

import com.example.telegrambotapplication.config.TelegramConfig;
import com.example.telegrambotapplication.models.Translate;
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

    @Override
    public void onUpdateReceived(Update update) {
        Translate translate = null;
        if (update.hasMessage()) {
        translate = translateService.getTranslateByKeyAndLang(update.getMessage().getText(), "Azerbaycan");
        SendMessage sendMessage = new SendMessage();
        Long chatId = update.getMessage().getChatId();
        sendMessage.setChatId(chatId);
        sendMessage.setText(translate.getValue());
        setButton(update, sendMessage);
        try {
            execute(sendMessage);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        }

        if (update.hasCallbackQuery()) {
            String text = update.getCallbackQuery().getData();
            System.out.println(text);

        }
    }

    private void setButton(Update update, SendMessage sendMessage) {
//        SendMessage sendMessage =new SendMessage();
        Long chatId = update.getMessage().getChatId();
        sendMessage.setChatId(chatId);
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<InlineKeyboardButton> inlineKeyboardButtons = new ArrayList<>();

        List<Translate> translates = translateService.getTranslatesByKeyAndLang("lang", "Azerbaycan");
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
