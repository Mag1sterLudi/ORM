package com.learning.platform.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Модуль (раздел) курса. Назван CourseModule, чтобы не конфликтовать с java.lang.Module */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "modules")
public class CourseModule extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 2000)
    private String description;

    @Column(name = "order_index", nullable = false)
    private int orderIndex;

    @OneToMany(mappedBy = "module", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<Lesson> lessons = new ArrayList<>();

    /** 1-1 (опционально): итоговый тест модуля. */
    @OneToOne(mappedBy = "module", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Quiz quiz;

    public CourseModule(String title, String description, int orderIndex) {
        this.title = title;
        this.description = description;
        this.orderIndex = orderIndex;
    }

    public void addLesson(Lesson lesson) {
        lesson.setModule(this);
        lessons.add(lesson);
    }

    public void attachQuiz(Quiz newQuiz) {
        newQuiz.setModule(this);
        this.quiz = newQuiz;
    }
}
