package com.learning.platform.dto;

import com.learning.platform.entity.Enrollment;
import com.learning.platform.entity.EnrollmentStatus;
import java.time.LocalDate;

/** Запись студента на курс */
public record EnrollmentResponse(Long id, Long studentId, String studentName, Long courseId, String courseTitle,
                                 LocalDate enrollDate, EnrollmentStatus status) {

    public static EnrollmentResponse from(Enrollment e) {
        return new EnrollmentResponse(e.getId(), e.getStudent().getId(), e.getStudent().getName(),
                e.getCourse().getId(), e.getCourse().getTitle(), e.getEnrollDate(), e.getStatus());
    }
}
