package com.learning.platform.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Учебный курс. Удаление курса каскадно удаляет его структуру (модули, уроки, задания, тесты),
 * записи студентов и отзывы, теги не удаляются - удаляются только строки из course_tag.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "courses")
public class Course extends BaseEntity {

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 4000)
    private String description;

    @Column(name = "duration_weeks")
    private Integer durationWeeks;

    @Column(name = "start_date")
    private LocalDate startDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "teacher_id", nullable = false)
    private User teacher;

    @OneToMany(mappedBy = "course", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    private List<CourseModule> modules = new ArrayList<>();

    @OneToMany(mappedBy = "course", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Enrollment> enrollments = new ArrayList<>();

    @OneToMany(mappedBy = "course", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CourseReview> reviews = new ArrayList<>();

    /** M-M без промежуточной сущности. Каскада REMOVE нет намеренно. */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "course_tag",
            joinColumns = @JoinColumn(name = "course_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id"))
    private Set<Tag> tags = new HashSet<>();

    public Course(String title, String description, User teacher, Category category) {
        this.title = title;
        this.description = description;
        this.teacher = teacher;
        this.category = category;
    }

    public void addModule(CourseModule module) {
        module.setCourse(this);
        modules.add(module);
    }

    public void removeModule(CourseModule module) {
        modules.remove(module);
        module.setCourse(null);
    }

    public void addTag(Tag tag) {
        tags.add(tag);
        tag.getCourses().add(this);
    }

    public void removeTag(Tag tag) {
        tags.remove(tag);
        tag.getCourses().remove(this);
    }
}
