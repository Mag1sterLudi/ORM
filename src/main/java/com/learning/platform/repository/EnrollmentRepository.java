package com.learning.platform.repository;

import com.learning.platform.entity.Enrollment;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    boolean existsByStudentIdAndCourseId(Long studentId, Long courseId);

    Optional<Enrollment> findByStudentIdAndCourseId(Long studentId, Long courseId);

    /** Студенты курса (JOIN FETCH, чтобы не ловить LazyInitializationException при маппинге) */
    @Query("select e from Enrollment e join fetch e.student join fetch e.course where e.course.id = :courseId order by e.id")
    List<Enrollment> findByCourseId(@Param("courseId") Long courseId);

    /** Курсы студента */
    @Query("select e from Enrollment e join fetch e.course join fetch e.student where e.student.id = :studentId order by e.id")
    List<Enrollment> findByStudentId(@Param("studentId") Long studentId);
}
