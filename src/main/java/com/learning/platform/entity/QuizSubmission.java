package com.learning.platform.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Результат прохождения теста студентом. Допускаются повторные попытки */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "quiz_submissions")
public class QuizSubmission extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    /** Процент правильных ответов (0-100). */
    @Column(nullable = false)
    private int score;

    @Column(name = "correct_answers", nullable = false)
    private int correctAnswers;

    @Column(name = "total_questions", nullable = false)
    private int totalQuestions;

    @Column(nullable = false)
    private boolean passed;

    @Column(name = "taken_at", nullable = false)
    private LocalDateTime takenAt;

    public QuizSubmission(Quiz quiz, User student, int correctAnswers, int totalQuestions) {
        this.quiz = quiz;
        this.student = student;
        this.correctAnswers = correctAnswers;
        this.totalQuestions = totalQuestions;
        this.score = totalQuestions == 0 ? 0 : Math.round(correctAnswers * 100f / totalQuestions);
        this.passed = score >= quiz.getPassScore();
        this.takenAt = LocalDateTime.now();
    }
}
