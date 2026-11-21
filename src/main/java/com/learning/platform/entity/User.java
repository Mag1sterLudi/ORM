package com.learning.platform.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Участник системы - студент, преподаватель или администратор. */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    /** 1-1 - профиль хранит редко используемые поля */
    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Profile profile;

    /** 1-M - курсы, которые ведёт преподаватель (без каскада - удалять преподавателя с курсами запрещено) */
    @OneToMany(mappedBy = "teacher", fetch = FetchType.LAZY)
    private List<Course> coursesTaught = new ArrayList<>();

    /** M-M с Course через сущность-связку Enrollment */
    @OneToMany(mappedBy = "student", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private List<Enrollment> enrollments = new ArrayList<>();

    @OneToMany(mappedBy = "student", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private List<Submission> submissions = new ArrayList<>();

    @OneToMany(mappedBy = "student", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private List<QuizSubmission> quizSubmissions = new ArrayList<>();

    @OneToMany(mappedBy = "student", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private List<CourseReview> reviews = new ArrayList<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private List<Notification> notifications = new ArrayList<>();

    public User(String name, String email, Role role) {
        this.name = name;
        this.email = email;
        this.role = role;
    }

    /** Устанавливает профиль, поддерживая обе стороны связи */
    public void attachProfile(Profile newProfile) {
        newProfile.setUser(this);
        this.profile = newProfile;
    }
}
