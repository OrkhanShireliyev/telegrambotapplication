package com.example.telegrambotapplication.repo;

import com.example.telegrambotapplication.models.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface QuestionRepo extends JpaRepository<Question,Long> {
   Optional<Question> findQuestionByKey(String key);

}

