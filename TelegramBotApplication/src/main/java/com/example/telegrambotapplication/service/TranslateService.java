package com.example.telegrambotapplication.service;

import com.example.telegrambotapplication.models.Translate;
import com.example.telegrambotapplication.repo.TranslateRepo;
import com.example.telegrambotapplication.service.redisService.RedisHelperService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TranslateService {

    private final TranslateRepo translateRepo;

    private final RedisHelperService redisHelperService;

    public void saveTranslate(Translate translate){
        translateRepo.save(translate);
    }

    public Translate getQuestionByKeyAndLang(String key,String lang){
        return translateRepo.getTranslateByKeyAndLang(key,lang);
    }

    public List<Translate> getQuestionsByKeyAndLang(String key,String lang){
        return translateRepo.getTranslatesByKeyAndLang(key,lang);
    }
}
