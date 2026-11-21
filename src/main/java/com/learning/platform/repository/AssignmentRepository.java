package com.learning.platform.repository;

import com.learning.platform.entity.Assignment;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

    List<Assignment> findByLessonIdOrderByIdAsc(Long lessonId);

    /** Задание вместе с цепочкой урок -> модуль -> курс (для проверки записи студента на курс) */
    @Query("""
            select a from Assignment a
            join fetch a.lesson l
            join fetch l.module m
            join fetch m.course
            where a.id = :id
            """)
    Optional<Assignment> findWithCourseById(@Param("id") Long id);
}
