package com.learning.platform.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.learning.platform.support.IntegrationTest;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

/** Проверяет, что Hibernate создал схему - все таблицы, включая join-таблицу course_tag, и внешние ключи. */
class SchemaCreationTest extends IntegrationTest {

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void allTablesAreCreated() {
        List<String> tables = jdbc.queryForList(
                "select lower(table_name) from information_schema.tables where table_schema = 'public'", String.class);

        assertThat(tables).contains("users", "profiles", "categories", "tags", "course_tag", "courses", "modules",
                "lessons", "assignments", "submissions", "enrollments", "quizzes", "questions", "answer_options",
                "quiz_submissions", "course_reviews", "notifications");
    }

    @Test
    void foreignKeysAreCreated() {
        Integer fkCount = jdbc.queryForObject(
                "select count(*) from information_schema.table_constraints "
                        + "where constraint_type = 'FOREIGN KEY' and table_schema = 'public'", Integer.class);

        // profiles, courses(2), course_tag(2), modules, lessons, assignments, submissions(2), enrollments(2),
        // quizzes, questions, answer_options, quiz_submissions(2), course_reviews(2), notifications
        assertThat(fkCount).isGreaterThanOrEqualTo(20);
    }
}
