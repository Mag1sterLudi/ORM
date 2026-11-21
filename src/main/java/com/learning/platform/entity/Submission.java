package com.learning.platform.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Решение студента. Один студент - одно решение на задание (уникальный индекс) */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "submissions",
        uniqueConstraints = @UniqueConstraint(name = "uk_submission_assignment_student",
                columnNames = {"assignment_id", "student_id"}))
public class Submission extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assignment_id", nullable = false)
    private Assignment assignment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    private Integer score;

    @Column(length = 2000)
    private String feedback;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SubmissionStatus status = SubmissionStatus.SUBMITTED;

    public Submission(Assignment assignment, User student, String content) {
        this.assignment = assignment;
        this.student = student;
        this.content = content;
        this.submittedAt = LocalDateTime.now();
    }

    public void grade(int newScore, String newFeedback) {
        this.score = newScore;
        this.feedback = newFeedback;
        this.status = SubmissionStatus.GRADED;
    }
}
