package com.learning.platform.service;

import com.learning.platform.entity.AnswerOption;
import com.learning.platform.entity.Question;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * Подсчёт правильных ответов. Вопрос засчитывается, если множество выбранных вариантов
 * в точности совпадает с множеством правильных (работает и для single, и для multiple choice).
 */
@Component
public class QuizGrader {

    public int countCorrect(Collection<Question> questions, Map<Long, Set<Long>> answers) {
        int correct = 0;
        for (Question question : questions) {
            Set<Long> selected = answers.getOrDefault(question.getId(), Set.of());
            Set<Long> expected = question.getOptions().stream()
                    .filter(AnswerOption::isCorrect)
                    .map(AnswerOption::getId)
                    .collect(Collectors.toSet());
            if (!expected.isEmpty() && expected.equals(selected)) {
                correct++;
            }
        }
        return correct;
    }
}
