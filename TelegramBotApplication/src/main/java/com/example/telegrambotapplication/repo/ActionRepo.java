package com.example.telegrambotapplication.repo;

import com.example.telegrambotapplication.models.Action;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ActionRepo extends JpaRepository<Action,Long> {

    Action findActionByNextQuestion(String nextQuestion);

}
