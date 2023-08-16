package com.example.telegrambotapplication.service;

import com.example.telegrambotapplication.models.Action;
import com.example.telegrambotapplication.repo.ActionRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ActionService {

    private final ActionRepo actionRepo;
    public Action findActionByNextQuestion(String buttonKey){
        return actionRepo.findActionByNextQuestion(buttonKey);
    }
}
