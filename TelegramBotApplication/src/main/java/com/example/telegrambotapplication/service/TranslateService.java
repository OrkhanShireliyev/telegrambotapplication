package com.example.telegrambotapplication.service;

import com.example.telegrambotapplication.models.Translate;
import com.example.telegrambotapplication.repo.TranslateRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TranslateService {
    private final TranslateRepo translateRepo;

    public void saveTranslate(Translate translate){
        translateRepo.save(translate);
    }

    public Translate getTranslateByKeyAndLang(String key,String lang){
       return translateRepo.getTranslateByKeyAndLang(key,lang);
    }

    public List<Translate> getTranslatesByKeyAndLang(String key,String lang){
        return translateRepo.getTranslatesByKeyAndLang(key,lang);
    }
}
