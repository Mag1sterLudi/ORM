package com.learning.platform.web;

import com.learning.platform.dto.AssignmentDtos.SubmissionResponse;
import com.learning.platform.dto.EnrollmentResponse;
import com.learning.platform.dto.NotificationResponse;
import com.learning.platform.dto.QuizDtos.QuizResultResponse;
import com.learning.platform.dto.UserDtos.CreateUserRequest;
import com.learning.platform.dto.UserDtos.ProfileRequest;
import com.learning.platform.dto.UserDtos.UpdateUserRequest;
import com.learning.platform.dto.UserDtos.UserResponse;
import com.learning.platform.entity.Role;
import com.learning.platform.service.AssignmentService;
import com.learning.platform.service.EnrollmentService;
import com.learning.platform.service.NotificationService;
import com.learning.platform.service.QuizService;
import com.learning.platform.service.UserService;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Users", description = "Пользователи, профили и их учебная активность")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final EnrollmentService enrollmentService;
    private final AssignmentService assignmentService;
    private final QuizService quizService;
    private final NotificationService notificationService;

    @Operation(summary = "Создать пользователя (вместе с профилем)")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse create(@Valid @RequestBody CreateUserRequest request) {
        return userService.create(request);
    }

    @Operation(summary = "Список пользователей, опционально по роли")
    @GetMapping
    public List<UserResponse> list(@RequestParam(required = false) Role role) {
        return userService.list(role);
    }

    @GetMapping("/{id}")
    public UserResponse get(@PathVariable Long id) {
        return userService.get(id);
    }

    @PutMapping("/{id}")
    public UserResponse update(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest request) {
        return userService.update(id, request);
    }

    @Operation(summary = "Обновить профиль пользователя (связь 1-1)")
    @PutMapping("/{id}/profile")
    public UserResponse updateProfile(@PathVariable Long id, @Valid @RequestBody ProfileRequest request) {
        return userService.updateProfile(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        userService.delete(id);
    }

    @Operation(summary = "Курсы, на которые записан студент")
    @GetMapping("/{id}/courses")
    public List<EnrollmentResponse> courses(@PathVariable Long id) {
        return enrollmentService.coursesOfStudent(id);
    }

    @Operation(summary = "Решения заданий студента с оценками")
    @GetMapping("/{id}/submissions")
    public List<SubmissionResponse> submissions(@PathVariable Long id) {
        return assignmentService.submissionsByStudent(id);
    }

    @Operation(summary = "Результаты тестов студента")
    @GetMapping("/{id}/quiz-results")
    public List<QuizResultResponse> quizResults(@PathVariable Long id) {
        return quizService.resultsByStudent(id);
    }

    @GetMapping("/{id}/notifications")
    public List<NotificationResponse> notifications(@PathVariable Long id) {
        userService.get(id);
        return notificationService.list(id);
    }
}
