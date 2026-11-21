package com.learning.platform.web;

import com.learning.platform.dto.CourseDtos.LessonRequest;
import com.learning.platform.dto.CourseDtos.LessonResponse;
import com.learning.platform.dto.CourseDtos.ModuleRequest;
import com.learning.platform.dto.CourseDtos.ModuleResponse;
import com.learning.platform.dto.QuizDtos.QuizRequest;
import com.learning.platform.dto.QuizDtos.QuizResponse;
import com.learning.platform.service.CourseContentService;
import com.learning.platform.service.QuizService;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Modules", description = "Модули курса, их уроки и тесты")
@RestController
@RequestMapping("/api/modules")
@RequiredArgsConstructor
public class ModuleController {

    private final CourseContentService contentService;
    private final QuizService quizService;

    @GetMapping("/{id}")
    public ModuleResponse get(@PathVariable Long id) {
        return contentService.getModule(id);
    }

    @PutMapping("/{id}")
    public ModuleResponse update(@PathVariable Long id, @Valid @RequestBody ModuleRequest request) {
        return contentService.updateModule(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        contentService.deleteModule(id);
    }

    @PostMapping("/{id}/lessons")
    @ResponseStatus(HttpStatus.CREATED)
    public LessonResponse addLesson(@PathVariable Long id, @Valid @RequestBody LessonRequest request) {
        return contentService.addLesson(id, request);
    }

    @GetMapping("/{id}/lessons")
    public List<LessonResponse> lessons(@PathVariable Long id) {
        return contentService.listLessons(id);
    }

    @Operation(summary = "Создать тест модуля с вопросами и вариантами ответов")
    @PostMapping("/{id}/quiz")
    @ResponseStatus(HttpStatus.CREATED)
    public QuizResponse createQuiz(@PathVariable Long id, @Valid @RequestBody QuizRequest request) {
        return quizService.create(id, request);
    }
}
