package com.learning.platform.service;

import com.learning.platform.dto.EnrollmentResponse;
import com.learning.platform.entity.Course;
import com.learning.platform.entity.Enrollment;
import com.learning.platform.entity.EnrollmentStatus;
import com.learning.platform.entity.User;
import com.learning.platform.exception.BusinessRuleException;
import com.learning.platform.exception.ConflictException;
import com.learning.platform.exception.NotFoundException;
import com.learning.platform.repository.EnrollmentRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Запись студентов на курсы */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseService courseService;
    private final UserService userService;

    @Transactional
    public EnrollmentResponse enroll(Long courseId, Long studentId) {
        Course course = courseService.getEntity(courseId);
        User student = userService.requireStudent(studentId);
        if (enrollmentRepository.existsByStudentIdAndCourseId(studentId, courseId)) {
            throw new ConflictException("Student " + studentId + " is already enrolled in course " + courseId);
        }
        Enrollment enrollment = new Enrollment(student, course);
        course.getEnrollments().add(enrollment);
        enrollmentRepository.save(enrollment);
        log.info("Student id={} enrolled in course id={}", studentId, courseId);
        return EnrollmentResponse.from(enrollment);
    }

    @Transactional
    public void unenroll(Long courseId, Long studentId) {
        Enrollment enrollment = getEnrollment(courseId, studentId);
        enrollment.getCourse().getEnrollments().remove(enrollment);
        enrollmentRepository.delete(enrollment);
        log.info("Student id={} unenrolled from course id={}", studentId, courseId);
    }

    @Transactional
    public EnrollmentResponse complete(Long courseId, Long studentId) {
        Enrollment enrollment = getEnrollment(courseId, studentId);
        enrollment.setStatus(EnrollmentStatus.COMPLETED);
        return EnrollmentResponse.from(enrollment);
    }

    public List<EnrollmentResponse> studentsOfCourse(Long courseId) {
        courseService.getEntity(courseId);
        return enrollmentRepository.findByCourseId(courseId).stream().map(EnrollmentResponse::from).toList();
    }

    public List<EnrollmentResponse> coursesOfStudent(Long studentId) {
        userService.getEntity(studentId);
        return enrollmentRepository.findByStudentId(studentId).stream().map(EnrollmentResponse::from).toList();
    }

    /** Проверка, используемая заданиями, тестами и отзывами. */
    void requireEnrolled(Long studentId, Long courseId) {
        if (!enrollmentRepository.existsByStudentIdAndCourseId(studentId, courseId)) {
            throw new BusinessRuleException("Student " + studentId + " is not enrolled in course " + courseId);
        }
    }

    private Enrollment getEnrollment(Long courseId, Long studentId) {
        return enrollmentRepository.findByStudentIdAndCourseId(studentId, courseId)
                .orElseThrow(() -> new NotFoundException(
                        "Enrollment of student " + studentId + " in course " + courseId + " not found"));
    }
}
