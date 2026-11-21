package com.learning.platform.web;

import com.learning.platform.dto.AssignmentDtos.AssignmentRequest;
import com.learning.platform.dto.AssignmentDtos.AssignmentResponse;
import com.learning.platform.dto.AssignmentDtos.GradeRequest;
import com.learning.platform.dto.AssignmentDtos.SubmissionResponse;
import com.learning.platform.dto.AssignmentDtos.SubmitRequest;
import com.learning.platform.service.AssignmentService;
import io.swagger.v3.oas.annotations.Operation;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Assignments", description = "Задания, сдача и оценивание решений")
@RestController
@RequiredArgsConstructor
public class AssignmentController {

    private final AssignmentService assignmentService;

    @GetMapping("/api/assignments/{id}")
    public AssignmentResponse get(@PathVariable Long id) {
        return assignmentService.get(id);
    }

    @PutMapping("/api/assignments/{id}")
    public AssignmentResponse update(@PathVariable Long id, @Valid @RequestBody AssignmentRequest request) {
        return assignmentService.update(id, request);
    }

    @DeleteMapping("/api/assignments/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        assignmentService.delete(id);
    }

    @Operation(summary = "Отправить решение (одно на студента)")
    @PostMapping("/api/assignments/{id}/submit")
    @ResponseStatus(HttpStatus.CREATED)
    public SubmissionResponse submit(@PathVariable Long id, @Valid @RequestBody SubmitRequest request) {
        return assignmentService.submit(id, request);
    }

    @Operation(summary = "Все решения по заданию (для преподавателя)")
    @GetMapping("/api/assignments/{id}/submissions")
    public List<SubmissionResponse> submissions(@PathVariable Long id) {
        return assignmentService.submissionsByAssignment(id);
    }

    @GetMapping("/api/submissions/{id}")
    public SubmissionResponse getSubmission(@PathVariable Long id) {
        return assignmentService.getSubmission(id);
    }

    @Operation(summary = "Выставить оценку и комментарий")
    @PutMapping("/api/submissions/{id}/grade")
    public SubmissionResponse grade(@PathVariable Long id, @Valid @RequestBody GradeRequest request) {
        return assignmentService.grade(id, request);
    }
}
