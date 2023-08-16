package com.example.telegrambotapplication.service;

import com.example.telegrambotapplication.models.Message;
import com.example.telegrambotapplication.models.Translate;
import com.example.telegrambotapplication.repo.MessageRepo;
import com.example.telegrambotapplication.service.redisService.RedisHelperService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class MessageService {
    private final MessageRepo messageRepo;
    private final RedisHelperService redisHelperService;
    private final ActionService actionService;
//    private final TranslateService translateService;


    private Pattern DATE_PATTERN = Pattern.compile(
            "^((2000|2400|2800|(19|2[0-9])(0[48]|[2468][048]|[13579][26]))-02-29)$"//February 29
                    + "|^(((19|2[0-9])[0-9]{2})-02-(0[1-9]|1[0-9]|2[0-8]))$"// Days of February
                    + "|^(((19|2[0-9])[0-9]{2})-(0[13578]|10|12)-(0[1-9]|[12][0-9]|3[01]))$"//1900 – 2999
                    + "|^(((19|2[0-9])[0-9]{2})-(0[469]|11)-(0[1-9]|[12][0-9]|30))$");// 30-Day Months

    public Message getMessageByKeyandLang(String key, String lang) {
        return messageRepo.getMessageByKeyAndLang(key, lang);
    }


    public String messageStartDate(Update update) {
        Date startDate = null;

        try {

            Date userDate = new Date().from(LocalDate.now().atStartOfDay()
                    .atZone(ZoneId.systemDefault())
                    .toInstant());
            if (DATE_PATTERN.matcher(update.getMessage().getText()).matches()) {
                startDate = new SimpleDateFormat("yyyy-MM-dd").parse(update.getMessage().getText());
            } else {
                return getMessageByKeyandLang("format",
                        redisHelperService.getByChatId(update.getMessage().getChatId()).getLang()).getValue();
            }
            if (0 < userDate.compareTo(startDate)) {
                return getMessageByKeyandLang("date",
                        redisHelperService.getByChatId(update.getMessage().getChatId()).getLang()).getValue();
            }
        } catch (Exception ex) {
            return getMessageByKeyandLang("format",
                    redisHelperService.getByChatId(update.getMessage().getChatId()).getLang()).getValue();
        }
        redisHelperService.addAnswer(redisHelperService.getByChatId(update.getMessage().getChatId()), update, update.getMessage().getText());
        return null;
    }

    public String messageEndDate(Update update) {

        Date endDate = null;
        try {
            int count = redisHelperService.getByChatId(update.getMessage().getChatId()).getAnswers().size();
            String date = redisHelperService.getByChatId(update.getMessage().getChatId()).getAnswers().get(count - 1);
            Date userDate = new SimpleDateFormat("yyyy-MM-dd").parse(date);
            if (DATE_PATTERN.matcher(update.getMessage().getText()).matches()) {
                endDate = new SimpleDateFormat("yyyy-MM-dd").parse(update.getMessage().getText());
            } else {
                System.out.println(update.getMessage().getText());
                return getMessageByKeyandLang("format", redisHelperService.getByChatId(update.getMessage().getChatId()).getLang()).getValue();
            }
            if (0 < userDate.compareTo(endDate)) {
                return getMessageByKeyandLang("date", redisHelperService.getByChatId(update.getMessage().getChatId()).getLang()).getValue();
            }
        } catch (Exception ex) {
            System.out.println(update.getMessage().getText());
            return getMessageByKeyandLang("format", redisHelperService.getByChatId(update.getMessage().getChatId()).getLang()).getValue();
        }
        redisHelperService.addAnswer(redisHelperService.getByChatId(update.getMessage().getChatId()), update, update.getMessage().getText());

        System.out.println(redisHelperService.getByChatId(update.getMessage().getChatId()).getAnswers());
        return null;
    }
}
