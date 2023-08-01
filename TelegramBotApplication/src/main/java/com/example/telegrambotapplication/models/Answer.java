package com.example.telegrambotapplication.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "answers")
public class Answer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String buttonType;
    private String nextQuestion;
    @OneToOne()
    @JoinColumn(name = "question_id",referencedColumnName = "id")
    private Question question;
}
