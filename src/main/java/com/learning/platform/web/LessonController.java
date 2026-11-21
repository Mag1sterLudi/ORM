package com.learning.platform.web;

import com.learning.platform.dto.AssignmentDtos.AssignmentRequest;
import com.learning.platform.dto.AssignmentDtos.AssignmentResponse;
import com.learning.platform.dto.CourseDtos.LessonRequest;
import com.learning.platform.dto.CourseDtos.LessonResponse;
import com.learning.platform.service.AssignmentService;
import com.learning.platform.service.CourseContentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Lessons", description = "Уроки и задания к ним")
@RestController
@RequestMapping("/api/lessons")
@RequiredArgsConstructor
public class LessonController {

    private final CourseContentService contentService;
    private final AssignmentService assignmentService;

    @GetMapping("/{id}")
    public LessonResponse get(@PathVariable Long id) {
        return contentService.getLesson(id);
    }

    @PutMapping("/{id}")
    public LessonResponse update(@PathVariable Long id, @Valid @RequestBody LessonRequest request) {
        return contentService.updateLesson(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        contentService.deleteLesson(id);
    }

    @PostMapping("/{id}/assignments")
    @ResponseStatus(HttpStatus.CREATED)
    public AssignmentResponse addAssignment(@PathVariable Long id, @Valid @RequestBody AssignmentRequest request) {
        return assignmentService.create(id, request);
    }

    @GetMapping("/{id}/assignments")
    public List<AssignmentResponse> assignments(@PathVariable Long id) {
        return assignmentService.listByLesson(id);
    }
}
