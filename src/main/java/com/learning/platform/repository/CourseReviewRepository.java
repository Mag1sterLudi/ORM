package com.learning.platform.repository;

import com.learning.platform.entity.CourseReview;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CourseReviewRepository extends JpaRepository<CourseReview, Long> {

    boolean existsByCourseIdAndStudentId(Long courseId, Long studentId);

    @Query("select r from CourseReview r join fetch r.student join fetch r.course where r.course.id = :courseId order by r.createdAt")
    List<CourseReview> findByCourseId(@Param("courseId") Long courseId);

    @Query("select avg(r.rating) from CourseReview r where r.course.id = :courseId")
    Double averageRating(@Param("courseId") Long courseId);

    /** Средний рейтинг сразу для нескольких курсов одним запросом: строки вида [courseId, average]. */
    @Query("select r.course.id, avg(r.rating) from CourseReview r where r.course.id in :courseIds group by r.course.id")
    List<Object[]> averageRatings(@Param("courseIds") Collection<Long> courseIds);
}
