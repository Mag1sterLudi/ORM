package com.learning.platform.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Тег курса (M-M с Course через таблицу course_tag). */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "tags")
public class Tag extends BaseEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @ManyToMany(mappedBy = "tags", fetch = FetchType.LAZY)
    private Set<Course> courses = new HashSet<>();

    public Tag(String name) {
        this.name = name;
    }
}
