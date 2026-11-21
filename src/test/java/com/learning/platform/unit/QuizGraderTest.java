package com.learning.platform.unit;

import static org.assertj.core.api.Assertions.assertThat;

import com.learning.platform.entity.AnswerOption;
import com.learning.platform.entity.Question;
import com.learning.platform.entity.QuestionType;
import com.learning.platform.service.QuizGrader;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

/** Юнит-тесты логики подсчёта правильных ответов (без Spring-контекста). */
class QuizGraderTest {

    private final QuizGrader grader = new QuizGrader();

    private static Question question(long id, QuestionType type, boolean... correctFlags) {
        Question question = new Question("Q" + id, type);
        ReflectionTestUtils.setField(question, "id", id);
        for (int i = 0; i < correctFlags.length; i++) {
            AnswerOption option = new AnswerOption("O" + i, correctFlags[i]);
            ReflectionTestUtils.setField(option, "id", id * 10 + i);
            question.addOption(option);
        }
        return question;
    }

    @Test
    void countsSingleChoiceAnswer() {
        Question q = question(1, QuestionType.SINGLE_CHOICE, false, true);

        assertThat(grader.countCorrect(List.of(q), Map.of(1L, Set.of(11L)))).isEqualTo(1);
        assertThat(grader.countCorrect(List.of(q), Map.of(1L, Set.of(10L)))).isZero();
    }

    @Test
    void multipleChoiceRequiresExactSetOfCorrectOptions() {
        Question q = question(2, QuestionType.MULTIPLE_CHOICE, true, true, false);

        assertThat(grader.countCorrect(List.of(q), Map.of(2L, Set.of(20L, 21L)))).isEqualTo(1);
        assertThat(grader.countCorrect(List.of(q), Map.of(2L, Set.of(20L)))).isZero();
        assertThat(grader.countCorrect(List.of(q), Map.of(2L, Set.of(20L, 21L, 22L)))).isZero();
    }

    @Test
    void unansweredQuestionIsIncorrect() {
        Question q1 = question(1, QuestionType.SINGLE_CHOICE, true, false);
        Question q2 = question(2, QuestionType.SINGLE_CHOICE, true, false);

        assertThat(grader.countCorrect(List.of(q1, q2), Map.of(1L, Set.of(10L)))).isEqualTo(1);
    }
}
