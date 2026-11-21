package com.learning.platform.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.learning.platform.dto.CourseDtos.CourseStructureResponse;
import com.learning.platform.entity.Course;
import com.learning.platform.entity.CourseModule;
import com.learning.platform.entity.Lesson;
import com.learning.platform.entity.Role;
import com.learning.platform.entity.User;
import com.learning.platform.service.CourseService;
import org.hibernate.LazyInitializationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Демонстрация проблемы ленивой загрузки. Тест намеренно НЕ транзакционный, т.е.
 * каждый вызов репозитория открывает и закрывает собственную сессию.
 */
@SpringBootTest
@ActiveProfiles("test")
class LazyLoadingTest {

    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CourseService courseService;
    @Autowired
    private TransactionTemplate tx;

    private Long courseId;

    @BeforeEach
    void setUp() {
        courseId = tx.execute(status -> {
            User teacher = userRepository.save(new User("Lazy Teacher", "lazy.teacher@test.local", Role.TEACHER));
            Course course = new Course("Lazy course", null, teacher, null);
            CourseModule module = new CourseModule("Module", null, 1);
            module.addLesson(new Lesson("Lesson", "text", null));
            course.addModule(module);
            return courseRepository.save(course).getId();
        });
    }

    @AfterEach
    void tearDown() {
        tx.executeWithoutResult(status -> {
            courseRepository.deleteById(courseId);
            userRepository.findByEmailIgnoreCase("lazy.teacher@test.local").ifPresent(userRepository::delete);
        });
    }

    @Test
    void accessingLazyCollectionOutsideSessionThrows() {
        Course detached = courseRepository.findById(courseId).orElseThrow();

        assertThatThrownBy(() -> detached.getModules().size())
                .isInstanceOf(LazyInitializationException.class);
    }

    @Test
    void accessingLazyManyToOneOutsideSessionThrows() {
        Course detached = courseRepository.findById(courseId).orElseThrow();

        assertThatThrownBy(() -> detached.getTeacher().getName())
                .isInstanceOf(LazyInitializationException.class);
    }

    @Test
    void joinFetchLoadsCollectionBeforeSessionCloses() {
        Course course = courseRepository.findWithModulesById(courseId).orElseThrow();

        assertThat(course.getModules()).hasSize(1);
    }

    @Test
    void initializingInsideTransactionAvoidsException() {
        Course course = tx.execute(status -> {
            Course c = courseRepository.findById(courseId).orElseThrow();
            c.getModules().size();
            return c;
        });

        assertThat(course.getModules()).hasSize(1);
    }

    @Test
    void serviceReturnsFullyLoadedDto() {
        CourseStructureResponse structure = courseService.getStructure(courseId);

        assertThat(structure.modules()).hasSize(1);
        assertThat(structure.modules().get(0).lessons()).extracting("title").containsExactly("Lesson");
    }
}
