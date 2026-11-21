package com.learning.platform.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.learning.platform.entity.Course;
import com.learning.platform.entity.EnrollmentStatus;
import com.learning.platform.entity.User;
import com.learning.platform.exception.BusinessRuleException;
import com.learning.platform.exception.ConflictException;
import com.learning.platform.exception.NotFoundException;
import com.learning.platform.support.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/** Запись на курс - уникальность, отписка, списки студентов и курсов. */
class EnrollmentServiceTest extends IntegrationTest {

    @Autowired
    private EnrollmentService enrollmentService;

    @Test
    void enrollsStudentAndListsBothSides() {
        User student = student();
        Course course = courseWithStructure(teacher());

        enrollmentService.enroll(course.getId(), student.getId());
        flushAndClear();

        assertThat(enrollmentService.studentsOfCourse(course.getId())).extracting("studentId").containsExactly(student.getId());
        assertThat(enrollmentService.coursesOfStudent(student.getId())).extracting("courseId").containsExactly(course.getId());
    }

    @Test
    void rejectsDuplicateEnrollment() {
        User student = student();
        Course course = courseWithStructure(teacher());
        enrollmentService.enroll(course.getId(), student.getId());

        assertThatThrownBy(() -> enrollmentService.enroll(course.getId(), student.getId()))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void rejectsTeacherEnrollmentAndUnknownStudent() {
        Course course = courseWithStructure(teacher());

        assertThatThrownBy(() -> enrollmentService.enroll(course.getId(), teacher().getId()))
                .isInstanceOf(BusinessRuleException.class);
        assertThatThrownBy(() -> enrollmentService.enroll(course.getId(), 999_999L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void unenrollRemovesRecordAndCompleteChangesStatus() {
        User first = student();
        User second = student();
        Course course = courseWithStructure(teacher());
        enrollmentService.enroll(course.getId(), first.getId());
        enrollmentService.enroll(course.getId(), second.getId());

        enrollmentService.unenroll(course.getId(), first.getId());
        assertThat(enrollmentService.complete(course.getId(), second.getId()).status()).isEqualTo(EnrollmentStatus.COMPLETED);
        flushAndClear();

        assertThat(enrollmentService.studentsOfCourse(course.getId())).extracting("studentId").containsExactly(second.getId());
        assertThatThrownBy(() -> enrollmentService.unenroll(course.getId(), first.getId()))
                .isInstanceOf(NotFoundException.class);
    }
}
