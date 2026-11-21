package com.learning.platform.service;

import com.learning.platform.dto.AssignmentDtos.AssignmentRequest;
import com.learning.platform.dto.AssignmentDtos.AssignmentResponse;
import com.learning.platform.dto.AssignmentDtos.GradeRequest;
import com.learning.platform.dto.AssignmentDtos.SubmissionResponse;
import com.learning.platform.dto.AssignmentDtos.SubmitRequest;
import com.learning.platform.entity.Assignment;
import com.learning.platform.entity.Lesson;
import com.learning.platform.entity.Submission;
import com.learning.platform.entity.User;
import com.learning.platform.exception.BusinessRuleException;
import com.learning.platform.exception.ConflictException;
import com.learning.platform.exception.NotFoundException;
import com.learning.platform.repository.AssignmentRepository;
import com.learning.platform.repository.EnrollmentRepository;
import com.learning.platform.repository.SubmissionRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Задания к урокам и решения студентов. Выдача, сдача, оценивание. */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final SubmissionRepository submissionRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final CourseContentService contentService;
    private final EnrollmentService enrollmentService;
    private final UserService userService;
    private final NotificationService notificationService;

    /** Создаёт задание и уведомляет записанных на курс студентов. */
    @Transactional
    public AssignmentResponse create(Long lessonId, AssignmentRequest request) {
        Lesson lesson = contentService.getLessonEntity(lessonId);
        Assignment assignment = new Assignment(request.title(), request.description(), request.dueDate(), request.maxScore());
        lesson.addAssignment(assignment);
        assignmentRepository.save(assignment);
        Long courseId = lesson.getModule().getCourse().getId();
        enrollmentRepository.findByCourseId(courseId).forEach(e ->
                notificationService.notify(e.getStudent(), "New assignment: " + assignment.getTitle()));
        log.info("Created assignment id={} for lesson id={}", assignment.getId(), lessonId);
        return AssignmentResponse.from(assignment);
    }

    public AssignmentResponse get(Long id) {
        return AssignmentResponse.from(getEntity(id));
    }

    public List<AssignmentResponse> listByLesson(Long lessonId) {
        contentService.getLessonEntity(lessonId);
        return assignmentRepository.findByLessonIdOrderByIdAsc(lessonId).stream().map(AssignmentResponse::from).toList();
    }

    @Transactional
    public AssignmentResponse update(Long id, AssignmentRequest request) {
        Assignment assignment = getEntity(id);
        assignment.setTitle(request.title());
        assignment.setDescription(request.description());
        assignment.setDueDate(request.dueDate());
        assignment.setMaxScore(request.maxScore());
        return AssignmentResponse.from(assignment);
    }

    @Transactional
    public void delete(Long id) {
        Assignment assignment = getEntity(id);
        assignment.getLesson().getAssignments().remove(assignment);
        log.info("Deleted assignment id={}", id);
    }

    /** Студент отправляет решение. Повторная отправка на то же задание запрещена */
    @Transactional
    public SubmissionResponse submit(Long assignmentId, SubmitRequest request) {
        Assignment assignment = assignmentRepository.findWithCourseById(assignmentId)
                .orElseThrow(() -> NotFoundException.of("Assignment", assignmentId));
        User student = userService.requireStudent(request.studentId());
        enrollmentService.requireEnrolled(student.getId(), assignment.getLesson().getModule().getCourse().getId());
        if (submissionRepository.existsByAssignmentIdAndStudentId(assignmentId, student.getId())) {
            throw new ConflictException("Student " + student.getId() + " has already submitted assignment " + assignmentId);
        }
        Submission submission = new Submission(assignment, student, request.content());
        assignment.getSubmissions().add(submission);
        submissionRepository.save(submission);
        log.info("Student id={} submitted assignment id={}", student.getId(), assignmentId);
        return SubmissionResponse.from(submission);
    }

    /** Преподаватель выставляет оценку (не выше максимального балла задания) */
    @Transactional
    public SubmissionResponse grade(Long submissionId, GradeRequest request) {
        Submission submission = getSubmissionEntity(submissionId);
        int maxScore = submission.getAssignment().getMaxScore();
        if (request.score() > maxScore) {
            throw new BusinessRuleException("Score " + request.score() + " exceeds max score " + maxScore);
        }
        submission.grade(request.score(), request.feedback());
        notificationService.notify(submission.getStudent(), "Your submission for '" + submission.getAssignment().getTitle()
                + "' was graded: " + request.score() + "/" + maxScore);
        log.info("Graded submission id={} score={}", submissionId, request.score());
        return SubmissionResponse.from(submission);
    }

    public SubmissionResponse getSubmission(Long id) {
        return SubmissionResponse.from(getSubmissionEntity(id));
    }

    public List<SubmissionResponse> submissionsByAssignment(Long assignmentId) {
        getEntity(assignmentId);
        return submissionRepository.findByAssignmentId(assignmentId).stream().map(SubmissionResponse::from).toList();
    }

    public List<SubmissionResponse> submissionsByStudent(Long studentId) {
        userService.getEntity(studentId);
        return submissionRepository.findByStudentId(studentId).stream().map(SubmissionResponse::from).toList();
    }

    private Assignment getEntity(Long id) {
        return assignmentRepository.findById(id).orElseThrow(() -> NotFoundException.of("Assignment", id));
    }

    private Submission getSubmissionEntity(Long id) {
        return submissionRepository.findById(id).orElseThrow(() -> NotFoundException.of("Submission", id));
    }
}
