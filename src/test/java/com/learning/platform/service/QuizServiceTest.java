package com.learning.platform.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.learning.platform.dto.QuizDtos.OptionRequest;
import com.learning.platform.dto.QuizDtos.QuestionRequest;
import com.learning.platform.dto.QuizDtos.QuizRequest;
import com.learning.platform.dto.QuizDtos.QuizResponse;
import com.learning.platform.dto.QuizDtos.QuizResultResponse;
import com.learning.platform.dto.QuizDtos.QuizUpdateRequest;
import com.learning.platform.dto.QuizDtos.TakeQuizRequest;
import com.learning.platform.entity.Course;
import com.learning.platform.entity.QuestionType;
import com.learning.platform.entity.User;
import com.learning.platform.exception.BusinessRuleException;
import com.learning.platform.exception.ConflictException;
import com.learning.platform.support.IntegrationTest;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/** Тесты. Создание с вопросами и вариантами, прохождение, подсчёт баллов, результаты. */
class QuizServiceTest extends IntegrationTest {

    @Autowired
    private QuizService quizService;

    private User student;
    private Long moduleId;

    @BeforeEach
    void setUp() {
        student = student();
        Course course = courseWithStructure(teacher());
        enroll(student, course);
        moduleId = course.getModules().get(0).getId();
    }

    private QuizRequest twoQuestionQuiz() {
        return new QuizRequest("Module quiz", 10, 50, List.of(
                new QuestionRequest("ORM means?", QuestionType.SINGLE_CHOICE, List.of(
                        new OptionRequest("Object-Relational Mapping", true), new OptionRequest("Other", false))),
                new QuestionRequest("Lazy by default?", QuestionType.MULTIPLE_CHOICE, List.of(
                        new OptionRequest("@OneToMany", true), new OptionRequest("@ManyToMany", true),
                        new OptionRequest("@ManyToOne", false)))));
    }

    @Test
    void createsQuizWithQuestionsAndOptions() {
        QuizResponse quiz = quizService.create(moduleId, twoQuestionQuiz());
        flushAndClear();

        QuizResponse loaded = quizService.get(quiz.id());
        assertThat(loaded.questions()).hasSize(2);
        assertThat(loaded.questions().get(1).options()).hasSize(3);
    }

    @Test
    void takeQuizCalculatesScoreAndSavesResult() {
        QuizResponse quiz = quizService.create(moduleId, twoQuestionQuiz());
        var q1 = quiz.questions().get(0);
        var q2 = quiz.questions().get(1);
        // первый вопрос - верно, во втором выбран только один из двух правильных - неверно
        Map<Long, Set<Long>> answers = Map.of(
                q1.id(), Set.of(q1.options().get(0).id()),
                q2.id(), Set.of(q2.options().get(0).id()));

        QuizResultResponse result = quizService.take(quiz.id(), new TakeQuizRequest(student.getId(), answers));
        flushAndClear();

        assertThat(result.correctAnswers()).isEqualTo(1);
        assertThat(result.score()).isEqualTo(50);
        assertThat(result.passed()).isTrue();
        assertThat(quizService.resultsByStudent(student.getId())).hasSize(1);
        assertThat(quizService.resultsByQuiz(quiz.id())).hasSize(1);
    }

    @Test
    void updateChangesTitleAndPassScore() {
        QuizResponse quiz = quizService.create(moduleId, twoQuestionQuiz());

        quizService.update(quiz.id(), new QuizUpdateRequest("Final quiz", 20, 80));
        flushAndClear();

        QuizResponse loaded = quizService.get(quiz.id());
        assertThat(loaded.title()).isEqualTo("Final quiz");
        assertThat(loaded.passScore()).isEqualTo(80);
        assertThat(loaded.questions()).hasSize(2);
    }

    @Test
    void rejectsSecondQuizForSameModule() {
        quizService.create(moduleId, twoQuestionQuiz());

        assertThatThrownBy(() -> quizService.create(moduleId, twoQuestionQuiz())).isInstanceOf(ConflictException.class);
    }

    @Test
    void rejectsSingleChoiceQuestionWithTwoCorrectOptions() {
        QuizRequest invalid = new QuizRequest("Bad", null, null, List.of(
                new QuestionRequest("Q", QuestionType.SINGLE_CHOICE, List.of(
                        new OptionRequest("A", true), new OptionRequest("B", true)))));

        assertThatThrownBy(() -> quizService.create(moduleId, invalid)).isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void rejectsAnswersForForeignQuestion() {
        QuizResponse quiz = quizService.create(moduleId, twoQuestionQuiz());

        assertThatThrownBy(() -> quizService.take(quiz.id(),
                new TakeQuizRequest(student.getId(), Map.of(999_999L, Set.of(1L)))))
                .isInstanceOf(BusinessRuleException.class);
    }
}
