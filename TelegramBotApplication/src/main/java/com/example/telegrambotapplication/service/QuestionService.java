package com.example.telegrambotapplication.service;

import com.example.telegrambotapplication.models.Question;
import com.example.telegrambotapplication.repo.QuestionRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class QuestionService {
    private final QuestionRepo questionRepo;

    public Question findQuestionByKey(String key) {
        Question question = questionRepo.findQuestionByKey(key).get();
        return question;
    }
}
