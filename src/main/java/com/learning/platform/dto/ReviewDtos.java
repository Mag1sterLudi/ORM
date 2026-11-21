package com.learning.platform.dto;

import com.learning.platform.entity.CourseReview;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/** DTO для отзывов о курсах */
public final class ReviewDtos {

    private ReviewDtos() {
    }

    public record ReviewRequest(
            @NotNull Long studentId,
            @NotNull @Min(1) @Max(5) Integer rating,
            @Size(max = 2000) String comment) {
    }

    public record ReviewResponse(Long id, Long courseId, Long studentId, String studentName, int rating,
                                 String comment, LocalDateTime createdAt) {
        public static ReviewResponse from(CourseReview r) {
            return new ReviewResponse(r.getId(), r.getCourse().getId(), r.getStudent().getId(), r.getStudent().getName(),
                    r.getRating(), r.getComment(), r.getCreatedAt());
        }
    }
}
