package com.learning.platform.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Тест по модулю (1-1 с CourseModule) */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "quizzes")
public class Quiz extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "module_id", nullable = false, unique = true)
    private CourseModule module;

    @Column(nullable = false, length = 200)
    private String title;

    /** Ограничение по времени в минутах (необязательно) */
    @Column(name = "time_limit")
    private Integer timeLimit;

    /** Минимальный процент правильных ответов для зачёта */
    @Column(name = "pass_score", nullable = false)
    private int passScore = 60;

    @OneToMany(mappedBy = "quiz", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Question> questions = new ArrayList<>();

    @OneToMany(mappedBy = "quiz", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuizSubmission> submissions = new ArrayList<>();

    public Quiz(String title, Integer timeLimit, int passScore) {
        this.title = title;
        this.timeLimit = timeLimit;
        this.passScore = passScore;
    }

    public void addQuestion(Question question) {
        question.setQuiz(this);
        questions.add(question);
    }
}
