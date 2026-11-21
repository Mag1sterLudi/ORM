package com.learning.platform.unit;

import static org.assertj.core.api.Assertions.assertThat;

import com.learning.platform.entity.Course;
import com.learning.platform.entity.CourseModule;
import com.learning.platform.entity.Quiz;
import com.learning.platform.entity.QuizSubmission;
import com.learning.platform.entity.Role;
import com.learning.platform.entity.Submission;
import com.learning.platform.entity.SubmissionStatus;
import com.learning.platform.entity.Tag;
import com.learning.platform.entity.User;
import org.junit.jupiter.api.Test;

/** Юнит-тесты доменной логики сущностей и helper-методов */
class EntityLogicTest {

    @Test
    void quizSubmissionCalculatesPercentAndPassed() {
        Quiz quiz = new Quiz("Quiz", null, 70);
        User student = new User("S", "s@x", Role.STUDENT);

        QuizSubmission passed = new QuizSubmission(quiz, student, 3, 4);
        QuizSubmission failed = new QuizSubmission(quiz, student, 2, 3);

        assertThat(passed.getScore()).isEqualTo(75);
        assertThat(passed.isPassed()).isTrue();
        assertThat(failed.getScore()).isEqualTo(67);
        assertThat(failed.isPassed()).isFalse();
    }

    @Test
    void quizWithoutQuestionsGivesZeroScore() {
        QuizSubmission result = new QuizSubmission(new Quiz("Empty", null, 0), new User("S", "s@x", Role.STUDENT), 0, 0);

        assertThat(result.getScore()).isZero();
    }

    @Test
    void gradingSubmissionChangesStatus() {
        Submission submission = new Submission(null, null, "text");
        assertThat(submission.getStatus()).isEqualTo(SubmissionStatus.SUBMITTED);

        submission.grade(80, "ok");

        assertThat(submission.getStatus()).isEqualTo(SubmissionStatus.GRADED);
        assertThat(submission.getScore()).isEqualTo(80);
    }

    @Test
    void helperMethodsMaintainBothSidesOfRelations() {
        Course course = new Course("C", null, null, null);
        CourseModule module = new CourseModule("M", null, 1);
        Tag tag = new Tag("Java");

        course.addModule(module);
        course.addTag(tag);

        assertThat(module.getCourse()).isSameAs(course);
        assertThat(tag.getCourses()).containsExactly(course);

        course.removeModule(module);
        course.removeTag(tag);

        assertThat(module.getCourse()).isNull();
        assertThat(course.getModules()).isEmpty();
        assertThat(tag.getCourses()).isEmpty();
    }
}
