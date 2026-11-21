package com.learning.platform.service;

import com.learning.platform.dto.ReviewDtos.ReviewRequest;
import com.learning.platform.dto.ReviewDtos.ReviewResponse;
import com.learning.platform.entity.Course;
import com.learning.platform.entity.CourseReview;
import com.learning.platform.entity.User;
import com.learning.platform.exception.ConflictException;
import com.learning.platform.exception.NotFoundException;
import com.learning.platform.repository.CourseReviewRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Отзывы студентов о курсах. Оставить отзыв может только записанный студент, не более одного раза */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final CourseReviewRepository reviewRepository;
    private final CourseService courseService;
    private final UserService userService;
    private final EnrollmentService enrollmentService;

    @Transactional
    public ReviewResponse add(Long courseId, ReviewRequest request) {
        Course course = courseService.getEntity(courseId);
        User student = userService.requireStudent(request.studentId());
        enrollmentService.requireEnrolled(student.getId(), courseId);
        if (reviewRepository.existsByCourseIdAndStudentId(courseId, student.getId())) {
            throw new ConflictException("Student " + student.getId() + " has already reviewed course " + courseId);
        }
        CourseReview review = new CourseReview(course, student, request.rating(), request.comment());
        course.getReviews().add(review);
        return ReviewResponse.from(reviewRepository.save(review));
    }

    public List<ReviewResponse> listByCourse(Long courseId) {
        courseService.getEntity(courseId);
        return reviewRepository.findByCourseId(courseId).stream().map(ReviewResponse::from).toList();
    }

    @Transactional
    public void delete(Long id) {
        CourseReview review = reviewRepository.findById(id).orElseThrow(() -> NotFoundException.of("Review", id));
        review.getCourse().getReviews().remove(review);
        reviewRepository.delete(review);
    }
}
