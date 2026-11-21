package com.learning.platform.web;

import com.learning.platform.dto.QuizDtos.QuizResponse;
import com.learning.platform.dto.QuizDtos.QuizResultResponse;
import com.learning.platform.dto.QuizDtos.QuizUpdateRequest;
import com.learning.platform.dto.QuizDtos.TakeQuizRequest;
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

@Tag(name = "Quizzes", description = "Прохождение тестов и результаты")
@RestController
@RequestMapping("/api/quizzes")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    @Operation(summary = "Тест с вопросами (без правильных ответов)")
    @GetMapping("/{id}")
    public QuizResponse get(@PathVariable Long id) {
        return quizService.get(id);
    }

    @PutMapping("/{id}")
    public QuizResponse update(@PathVariable Long id, @Valid @RequestBody QuizUpdateRequest request) {
        return quizService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        quizService.delete(id);
    }

    @Operation(summary = "Пройти тест: answers = {questionId: [optionId, ...]}")
    @PostMapping("/{id}/take")
    @ResponseStatus(HttpStatus.CREATED)
    public QuizResultResponse take(@PathVariable Long id, @Valid @RequestBody TakeQuizRequest request) {
        return quizService.take(id, request);
    }

    @Operation(summary = "Результаты теста (для преподавателя)")
    @GetMapping("/{id}/results")
    public List<QuizResultResponse> results(@PathVariable Long id) {
        return quizService.resultsByQuiz(id);
    }
}
