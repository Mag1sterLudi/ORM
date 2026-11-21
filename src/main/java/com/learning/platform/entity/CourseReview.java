package com.learning.platform.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Отзыв студента о курсе (один отзыв на курс от одного студента) */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "course_reviews",
        uniqueConstraints = @UniqueConstraint(name = "uk_review_course_student",
                columnNames = {"course_id", "student_id"}))
public class CourseReview extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @Column(nullable = false)
    private int rating;

    @Column(length = 2000)
    private String comment;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public CourseReview(Course course, User student, int rating, String comment) {
        this.course = course;
        this.student = student;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = LocalDateTime.now();
    }
}
