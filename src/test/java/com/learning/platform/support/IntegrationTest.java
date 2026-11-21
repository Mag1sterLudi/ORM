package com.learning.platform.support;

import com.learning.platform.entity.Assignment;
import com.learning.platform.entity.Course;
import com.learning.platform.entity.CourseModule;
import com.learning.platform.entity.Enrollment;
import com.learning.platform.entity.Lesson;
import com.learning.platform.entity.Profile;
import com.learning.platform.entity.Role;
import com.learning.platform.entity.User;
import com.learning.platform.repository.CourseRepository;
import com.learning.platform.repository.EnrollmentRepository;
import com.learning.platform.repository.UserRepository;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * Базовый класс интеграционных тестов - поднимает полный Spring-контекст на H2 (профиль test),
 * каждый тест выполняется в транзакции, которая откатывается в конце.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public abstract class IntegrationTest {

    private static final AtomicInteger SEQ = new AtomicInteger();

    @Autowired
    protected EntityManager em;
    @Autowired
    protected UserRepository userRepository;
    @Autowired
    protected CourseRepository courseRepository;
    @Autowired
    protected EnrollmentRepository enrollmentRepository;

    protected User user(Role role) {
        int n = SEQ.incrementAndGet();
        User user = new User(role.name() + " " + n, role.name().toLowerCase() + n + "@test.local", role);
        user.attachProfile(new Profile("bio " + n, null, null));
        return userRepository.save(user);
    }

    protected User teacher() {
        return user(Role.TEACHER);
    }

    protected User student() {
        return user(Role.STUDENT);
    }

    /** Курс. 2 модуля по 2 урока, в первом уроке - одно задание. Всё сохраняется каскадно. */
    protected Course courseWithStructure(User teacher) {
        Course course = new Course("Hibernate " + SEQ.incrementAndGet(), "ORM course", teacher, null);
        for (int m = 1; m <= 2; m++) {
            CourseModule module = new CourseModule("Module " + m, null, m);
            for (int l = 1; l <= 2; l++) {
                module.addLesson(new Lesson("Lesson " + m + "." + l, "content", null));
            }
            course.addModule(module);
        }
        course.getModules().get(0).getLessons().get(0)
                .addAssignment(new Assignment("Homework", "Do it", LocalDate.now().plusDays(7), 100));
        return courseRepository.save(course);
    }

    protected Enrollment enroll(User student, Course course) {
        Enrollment enrollment = new Enrollment(student, course);
        course.getEnrollments().add(enrollment);
        return enrollmentRepository.save(enrollment);
    }

    /** Сбрасывает изменения в БД и очищает persistence context, чтобы следующие чтения шли из базы. */
    protected void flushAndClear() {
        em.flush();
        em.clear();
    }
}
