package com.learning.platform.repository;

import com.learning.platform.entity.Submission;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {

    boolean existsByAssignmentIdAndStudentId(Long assignmentId, Long studentId);

    @Query("select s from Submission s join fetch s.student join fetch s.assignment "
            + "where s.assignment.id = :assignmentId order by s.submittedAt")
    List<Submission> findByAssignmentId(@Param("assignmentId") Long assignmentId);

    @Query("select s from Submission s join fetch s.student join fetch s.assignment "
            + "where s.student.id = :studentId order by s.submittedAt")
    List<Submission> findByStudentId(@Param("studentId") Long studentId);
}
