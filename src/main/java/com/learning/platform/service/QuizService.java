package com.learning.platform.service;

import com.learning.platform.dto.QuizDtos.QuestionRequest;
import com.learning.platform.dto.QuizDtos.QuizRequest;
import com.learning.platform.dto.QuizDtos.QuizResponse;
import com.learning.platform.dto.QuizDtos.QuizResultResponse;
import com.learning.platform.dto.QuizDtos.QuizUpdateRequest;
import com.learning.platform.dto.QuizDtos.TakeQuizRequest;
import com.learning.platform.entity.AnswerOption;
import com.learning.platform.entity.CourseModule;
import com.learning.platform.entity.Question;
import com.learning.platform.entity.QuestionType;
import com.learning.platform.entity.Quiz;
import com.learning.platform.entity.QuizSubmission;
import com.learning.platform.entity.User;
import com.learning.platform.exception.BusinessRuleException;
import com.learning.platform.exception.ConflictException;
import com.learning.platform.exception.NotFoundException;
import com.learning.platform.repository.QuizRepository;
import com.learning.platform.repository.QuizSubmissionRepository;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Тесты по модулям - создание, прохождение, результаты. */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuizService {

    private final QuizRepository quizRepository;
    private final QuizSubmissionRepository quizSubmissionRepository;
    private final CourseContentService contentService;
    private final EnrollmentService enrollmentService;
    private final UserService userService;
    private final QuizGrader grader;

    /** Создаёт тест модуля - вопросы и варианты сохраняются каскадно. */
    @Transactional
    public QuizResponse create(Long moduleId, QuizRequest request) {
        CourseModule module = contentService.getModuleEntity(moduleId);
        if (quizRepository.existsByModuleId(moduleId)) {
            throw new ConflictException("Module " + moduleId + " already has a quiz");
        }
        Quiz quiz = new Quiz(request.title(), request.timeLimit(), request.passScore() == null ? 60 : request.passScore());
        request.questions().forEach(q -> quiz.addQuestion(toQuestion(q)));
        module.attachQuiz(quiz);
        quizRepository.save(quiz);
        log.info("Created quiz id={} for module id={}", quiz.getId(), moduleId);
        return QuizResponse.from(quiz);
    }

    public QuizResponse get(Long id) {
        return QuizResponse.from(getWithQuestions(id));
    }

    @Transactional
    public QuizResponse update(Long id, QuizUpdateRequest request) {
        Quiz quiz = getWithQuestions(id);
        quiz.setTitle(request.title());
        quiz.setTimeLimit(request.timeLimit());
        quiz.setPassScore(request.passScore());
        return QuizResponse.from(quiz);
    }

    @Transactional
    public void delete(Long id) {
        Quiz quiz = quizRepository.findById(id).orElseThrow(() -> NotFoundException.of("Quiz", id));
        quiz.getModule().setQuiz(null);
        quizRepository.delete(quiz);
    }

    /** Проверяет ответы студента, считает процент правильных и сохраняет результат. */
    @Transactional
    public QuizResultResponse take(Long quizId, TakeQuizRequest request) {
        Quiz quiz = getWithQuestions(quizId);
        User student = userService.requireStudent(request.studentId());
        enrollmentService.requireEnrolled(student.getId(), quiz.getModule().getCourse().getId());
        validateAnswers(quiz, request);
        int correct = grader.countCorrect(quiz.getQuestions(), request.answers());
        QuizSubmission result = new QuizSubmission(quiz, student, correct, quiz.getQuestions().size());
        quiz.getSubmissions().add(result);
        quizSubmissionRepository.save(result);
        log.info("Student id={} took quiz id={}: {}%", student.getId(), quizId, result.getScore());
        return QuizResultResponse.from(result);
    }

    public List<QuizResultResponse> resultsByQuiz(Long quizId) {
        quizRepository.findById(quizId).orElseThrow(() -> NotFoundException.of("Quiz", quizId));
        return quizSubmissionRepository.findByQuizId(quizId).stream().map(QuizResultResponse::from).toList();
    }

    public List<QuizResultResponse> resultsByStudent(Long studentId) {
        userService.getEntity(studentId);
        return quizSubmissionRepository.findByStudentId(studentId).stream().map(QuizResultResponse::from).toList();
    }

    private Quiz getWithQuestions(Long id) {
        return quizRepository.findWithQuestionsById(id).orElseThrow(() -> NotFoundException.of("Quiz", id));
    }

    private Question toQuestion(QuestionRequest request) {
        long correctCount = request.options().stream().filter(o -> o.correct()).count();
        if (correctCount == 0) {
            throw new BusinessRuleException("Question '" + request.text() + "' must have at least one correct option");
        }
        if (request.type() == QuestionType.SINGLE_CHOICE && correctCount > 1) {
            throw new BusinessRuleException("Single-choice question '" + request.text() + "' must have exactly one correct option");
        }
        Question question = new Question(request.text(), request.type());
        request.options().forEach(o -> question.addOption(new AnswerOption(o.text(), o.correct())));
        return question;
    }

    /** Ответы должны ссылаться только на вопросы и варианты этого теста */
    private void validateAnswers(Quiz quiz, TakeQuizRequest request) {
        for (var entry : request.answers().entrySet()) {
            Question question = quiz.getQuestions().stream()
                    .filter(q -> q.getId().equals(entry.getKey()))
                    .findFirst()
                    .orElseThrow(() -> new BusinessRuleException(
                            "Question " + entry.getKey() + " does not belong to quiz " + quiz.getId()));
            Set<Long> optionIds = question.getOptions().stream().map(AnswerOption::getId).collect(Collectors.toSet());
            Set<Long> selected = entry.getValue() == null ? Set.of() : entry.getValue();
            if (!optionIds.containsAll(selected)) {
                throw new BusinessRuleException("Answer contains options not belonging to question " + question.getId());
            }
            if (question.getType() == QuestionType.SINGLE_CHOICE && selected.size() > 1) {
                throw new BusinessRuleException("Only one option can be selected for question " + question.getId());
            }
        }
    }
}
