package com.learning.platform.dto;

import com.learning.platform.entity.Assignment;
import com.learning.platform.entity.Submission;
import com.learning.platform.entity.SubmissionStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** DTO для заданий и решений */
public final class AssignmentDtos {

    private AssignmentDtos() {
    }

    public record AssignmentRequest(
            @NotBlank @Size(max = 200) String title,
            @Size(max = 4000) String description,
            LocalDate dueDate,
            @NotNull @Min(1) @Max(1000) Integer maxScore) {
    }

    public record AssignmentResponse(Long id, Long lessonId, String title, String description,
                                     LocalDate dueDate, int maxScore) {
        public static AssignmentResponse from(Assignment a) {
            return new AssignmentResponse(a.getId(), a.getLesson().getId(), a.getTitle(), a.getDescription(),
                    a.getDueDate(), a.getMaxScore());
        }
    }

    public record SubmitRequest(@NotNull Long studentId, @NotBlank String content) {
    }

    public record GradeRequest(@NotNull @Min(0) Integer score, @Size(max = 2000) String feedback) {
    }

    public record SubmissionResponse(Long id, Long assignmentId, String assignmentTitle, Long studentId, String studentName,
                                     LocalDateTime submittedAt, String content, Integer score, Integer maxScore,
                                     String feedback, SubmissionStatus status) {
        public static SubmissionResponse from(Submission s) {
            return new SubmissionResponse(s.getId(), s.getAssignment().getId(), s.getAssignment().getTitle(),
                    s.getStudent().getId(), s.getStudent().getName(), s.getSubmittedAt(), s.getContent(),
                    s.getScore(), s.getAssignment().getMaxScore(), s.getFeedback(), s.getStatus());
        }
    }
}
