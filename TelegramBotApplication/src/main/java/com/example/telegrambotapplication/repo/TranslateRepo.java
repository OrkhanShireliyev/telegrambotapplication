package com.example.telegrambotapplication.repo;

import com.example.telegrambotapplication.models.Translate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TranslateRepo extends JpaRepository<Translate,Long> {
    Translate getTranslateByKeyAndLang(String key,String lang);

    List<Translate> getTranslatesByKeyAndLang(String key,String lang);
}
