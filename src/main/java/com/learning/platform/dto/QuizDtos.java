package com.learning.platform.dto;

import com.learning.platform.entity.AnswerOption;
import com.learning.platform.entity.Question;
import com.learning.platform.entity.QuestionType;
import com.learning.platform.entity.Quiz;
import com.learning.platform.entity.QuizSubmission;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** DTO для тестов, вопросов, вариантов ответов и результатов */
public final class QuizDtos {

    private QuizDtos() {
    }

    public record QuizRequest(
            @NotBlank @Size(max = 200) String title,
            @Min(1) Integer timeLimit,
            @Min(0) @Max(100) Integer passScore,
            @NotEmpty List<@Valid QuestionRequest> questions) {
    }

    /** Изменение названия, лимита времени и проходного балла теста, вопросы при этом не меняются */
    public record QuizUpdateRequest(
            @NotBlank @Size(max = 200) String title,
            @Min(1) Integer timeLimit,
            @NotNull @Min(0) @Max(100) Integer passScore) {
    }

    public record QuestionRequest(
            @NotBlank @Size(max = 1000) String text,
            @NotNull QuestionType type,
            @NotEmpty @Size(min = 2) List<@Valid OptionRequest> options) {
    }

    public record OptionRequest(@NotBlank @Size(max = 500) String text, boolean correct) {
    }

    /** Вариант ответа для студента - без признака правильности. */
    public record OptionResponse(Long id, String text) {
        public static OptionResponse from(AnswerOption o) {
            return new OptionResponse(o.getId(), o.getText());
        }
    }

    public record QuestionResponse(Long id, String text, QuestionType type, List<OptionResponse> options) {
        public static QuestionResponse from(Question q) {
            return new QuestionResponse(q.getId(), q.getText(), q.getType(),
                    q.getOptions().stream().map(OptionResponse::from).toList());
        }
    }

    public record QuizResponse(Long id, Long moduleId, String title, Integer timeLimit, int passScore,
                               List<QuestionResponse> questions) {
        public static QuizResponse from(Quiz q) {
            return new QuizResponse(q.getId(), q.getModule().getId(), q.getTitle(), q.getTimeLimit(), q.getPassScore(),
                    q.getQuestions().stream().map(QuestionResponse::from).toList());
        }
    }

    /** Ответы студента: id вопроса -> множество id выбранных вариантов. */
    public record TakeQuizRequest(@NotNull Long studentId, @NotNull Map<Long, Set<Long>> answers) {
    }

    public record QuizResultResponse(Long id, Long quizId, String quizTitle, Long studentId, String studentName,
                                     int score, int correctAnswers, int totalQuestions, boolean passed,
                                     LocalDateTime takenAt) {
        public static QuizResultResponse from(QuizSubmission s) {
            return new QuizResultResponse(s.getId(), s.getQuiz().getId(), s.getQuiz().getTitle(),
                    s.getStudent().getId(), s.getStudent().getName(), s.getScore(), s.getCorrectAnswers(),
                    s.getTotalQuestions(), s.isPassed(), s.getTakenAt());
        }
    }
}
