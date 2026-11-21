package com.learning.platform.repository;

import com.learning.platform.entity.QuizSubmission;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface QuizSubmissionRepository extends JpaRepository<QuizSubmission, Long> {

    @Query("select qs from QuizSubmission qs join fetch qs.quiz join fetch qs.student "
            + "where qs.student.id = :studentId order by qs.takenAt")
    List<QuizSubmission> findByStudentId(@Param("studentId") Long studentId);

    @Query("select qs from QuizSubmission qs join fetch qs.quiz join fetch qs.student "
            + "where qs.quiz.id = :quizId order by qs.takenAt")
    List<QuizSubmission> findByQuizId(@Param("quizId") Long quizId);
}
