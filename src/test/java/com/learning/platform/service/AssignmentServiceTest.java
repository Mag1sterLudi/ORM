package com.learning.platform.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.learning.platform.dto.AssignmentDtos.AssignmentRequest;
import com.learning.platform.dto.AssignmentDtos.GradeRequest;
import com.learning.platform.dto.AssignmentDtos.SubmissionResponse;
import com.learning.platform.dto.AssignmentDtos.SubmitRequest;
import com.learning.platform.entity.Course;
import com.learning.platform.entity.SubmissionStatus;
import com.learning.platform.entity.User;
import com.learning.platform.exception.BusinessRuleException;
import com.learning.platform.exception.ConflictException;
import com.learning.platform.support.IntegrationTest;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/** Полный цикл заданий - создание, сдача, запрет повторной сдачи, оценивание, выборки. */
class AssignmentServiceTest extends IntegrationTest {

    @Autowired
    private AssignmentService assignmentService;
    @Autowired
    private NotificationService notificationService;

    private User student;
    private Long assignmentId;

    @BeforeEach
    void setUp() {
        student = student();
        Course course = courseWithStructure(teacher());
        enroll(student, course);
        Long lessonId = course.getModules().get(0).getLessons().get(0).getId();
        assignmentId = assignmentService.create(lessonId,
                new AssignmentRequest("Mapping task", "Map entities", LocalDate.now().plusDays(3), 10)).id();
    }

    @Test
    void createNotifiesEnrolledStudents() {
        assertThat(notificationService.list(student.getId())).extracting("message").anyMatch(m -> m.toString().contains("Mapping task"));
    }

    @Test
    void studentSubmitsAndTeacherGrades() {
        SubmissionResponse submission = assignmentService.submit(assignmentId, new SubmitRequest(student.getId(), "my solution"));
        assignmentService.grade(submission.id(), new GradeRequest(9, "Nice"));
        flushAndClear();

        SubmissionResponse graded = assignmentService.getSubmission(submission.id());
        assertThat(graded.score()).isEqualTo(9);
        assertThat(graded.status()).isEqualTo(SubmissionStatus.GRADED);
        assertThat(assignmentService.submissionsByAssignment(assignmentId)).hasSize(1);
        assertThat(assignmentService.submissionsByStudent(student.getId())).extracting("feedback").containsExactly("Nice");
    }

    @Test
    void preventsSecondSubmission() {
        assignmentService.submit(assignmentId, new SubmitRequest(student.getId(), "first"));

        assertThatThrownBy(() -> assignmentService.submit(assignmentId, new SubmitRequest(student.getId(), "second")))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void rejectsSubmissionFromNotEnrolledStudent() {
        User outsider = student();

        assertThatThrownBy(() -> assignmentService.submit(assignmentId, new SubmitRequest(outsider.getId(), "x")))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("not enrolled");
    }

    @Test
    void rejectsScoreAboveMax() {
        SubmissionResponse submission = assignmentService.submit(assignmentId, new SubmitRequest(student.getId(), "sol"));

        assertThatThrownBy(() -> assignmentService.grade(submission.id(), new GradeRequest(11, null)))
                .isInstanceOf(BusinessRuleException.class);
    }
}
