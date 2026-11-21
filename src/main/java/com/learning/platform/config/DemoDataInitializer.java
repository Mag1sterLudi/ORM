package com.learning.platform.config;

import com.learning.platform.dto.AssignmentDtos.AssignmentRequest;
import com.learning.platform.dto.AssignmentDtos.GradeRequest;
import com.learning.platform.dto.AssignmentDtos.SubmitRequest;
import com.learning.platform.dto.CatalogDtos.CategoryRequest;
import com.learning.platform.dto.CatalogDtos.TagRequest;
import com.learning.platform.dto.CourseDtos.CourseRequest;
import com.learning.platform.dto.CourseDtos.CourseStructureResponse;
import com.learning.platform.dto.CourseDtos.LessonRequest;
import com.learning.platform.dto.CourseDtos.ModuleRequest;
import com.learning.platform.dto.QuizDtos.OptionRequest;
import com.learning.platform.dto.QuizDtos.QuestionRequest;
import com.learning.platform.dto.QuizDtos.QuizRequest;
import com.learning.platform.dto.QuizDtos.QuizResponse;
import com.learning.platform.dto.QuizDtos.TakeQuizRequest;
import com.learning.platform.dto.ReviewDtos.ReviewRequest;
import com.learning.platform.dto.UserDtos.CreateUserRequest;
import com.learning.platform.entity.QuestionType;
import com.learning.platform.entity.Role;
import com.learning.platform.repository.UserRepository;
import com.learning.platform.service.AssignmentService;
import com.learning.platform.service.CatalogService;
import com.learning.platform.service.CourseService;
import com.learning.platform.service.EnrollmentService;
import com.learning.platform.service.QuizService;
import com.learning.platform.service.ReviewService;
import com.learning.platform.service.UserService;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Заполняем пустую БД демонстрационными данными через сервисный слой
 * (включается свойством app.demo-data.enabled=true, по умолчанию - в профиле dev)
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.demo-data.enabled", havingValue = "true")
public class DemoDataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final UserService userService;
    private final CatalogService catalogService;
    private final CourseService courseService;
    private final EnrollmentService enrollmentService;
    private final AssignmentService assignmentService;
    private final QuizService quizService;
    private final ReviewService reviewService;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Database is not empty, demo data skipped");
            return;
        }
        userService.create(new CreateUserRequest("Администратор", "admin@platform.local", Role.ADMIN, null, null, null));
        Long teacher = userService.create(new CreateUserRequest("Иван Петров", "ivan.petrov@platform.local", Role.TEACHER,
                "Java-разработчик, 10 лет опыта с Hibernate", "https://i.pravatar.cc/150?u=ivan", "+7-900-000-00-01")).id();
        Long teacher2 = userService.create(new CreateUserRequest("Мария Смирнова", "maria.smirnova@platform.local",
                Role.TEACHER, "Преподаватель Spring", null, null)).id();
        Long anna = userService.create(new CreateUserRequest("Анна Иванова", "anna@platform.local", Role.STUDENT,
                "Начинающий Java-разработчик", null, null)).id();
        Long boris = userService.create(new CreateUserRequest("Борис Кузнецов", "boris@platform.local", Role.STUDENT,
                null, null, null)).id();
        userService.create(new CreateUserRequest("Вера Соколова", "vera@platform.local", Role.STUDENT, null, null, null));

        Long programming = catalogService.createCategory(new CategoryRequest("Программирование")).id();
        Long databases = catalogService.createCategory(new CategoryRequest("Базы данных")).id();
        Long java = catalogService.createTag(new TagRequest("Java")).id();
        Long hibernate = catalogService.createTag(new TagRequest("Hibernate")).id();
        Long spring = catalogService.createTag(new TagRequest("Spring")).id();
        Long beginner = catalogService.createTag(new TagRequest("Beginner")).id();

        Long hibernateCourse = courseService.create(new CourseRequest("Основы Hibernate",
                "ORM, JPA и Hibernate: сущности, связи, ленивая загрузка", 6, LocalDate.now().plusDays(7),
                databases, teacher, Set.of(java, hibernate), List.of(
                new ModuleRequest("Введение в ORM", "Зачем нужен ORM", 1, List.of(
                        new LessonRequest("Что такое ORM", "ORM связывает объекты и таблицы...", "https://video.local/orm-1"),
                        new LessonRequest("Первая сущность", "@Entity, @Id, @GeneratedValue...", null))),
                new ModuleRequest("Связи и загрузка", "OneToMany, ManyToMany, Lazy", 2, List.of(
                        new LessonRequest("Связи между сущностями", "@OneToMany, @ManyToOne...", null),
                        new LessonRequest("Lazy vs Eager", "LazyInitializationException и JOIN FETCH...", null)))))).id();
        courseService.create(new CourseRequest("Spring Boot для начинающих", "Быстрый старт со Spring Boot", 4,
                LocalDate.now().plusDays(14), programming, teacher2, Set.of(java, spring, beginner), List.of(
                new ModuleRequest("Первое приложение", null, 1, List.of(
                        new LessonRequest("Spring Initializr", "Создаём проект...", null))))));

        CourseStructureResponse structure = courseService.getStructure(hibernateCourse);
        Long firstLesson = structure.modules().get(0).lessons().get(0).id();
        Long secondLesson = structure.modules().get(0).lessons().get(1).id();
        Long firstModule = structure.modules().get(0).id();

        enrollmentService.enroll(hibernateCourse, anna);
        enrollmentService.enroll(hibernateCourse, boris);

        Long essay = assignmentService.create(firstLesson, new AssignmentRequest("Эссе: зачем нужен ORM",
                "Опишите плюсы и минусы ORM", LocalDate.now().plusDays(10), 100)).id();
        assignmentService.create(secondLesson, new AssignmentRequest("Сущность Student",
                "Создайте сущность Student с полями id, name, email", LocalDate.now().plusDays(14), 50));

        Long submission = assignmentService.submit(essay, new SubmitRequest(anna, "ORM избавляет от рутинного SQL...")).id();
        assignmentService.grade(submission, new GradeRequest(90, "Отличная работа!"));
        assignmentService.submit(essay, new SubmitRequest(boris, "ORM - это маппинг объектов на таблицы."));

        QuizResponse quiz = quizService.create(firstModule, new QuizRequest("Тест по модулю 1", 15, 60, List.of(
                new QuestionRequest("Что означает ORM?", QuestionType.SINGLE_CHOICE, List.of(
                        new OptionRequest("Object-Relational Mapping", true),
                        new OptionRequest("Online Resource Manager", false))),
                new QuestionRequest("Какие связи по умолчанию LAZY?", QuestionType.MULTIPLE_CHOICE, List.of(
                        new OptionRequest("@OneToMany", true),
                        new OptionRequest("@ManyToMany", true),
                        new OptionRequest("@ManyToOne", false))))));
        Long q1 = quiz.questions().get(0).id();
        Long q2 = quiz.questions().get(1).id();
        quizService.take(quiz.id(), new TakeQuizRequest(anna, Map.of(
                q1, Set.of(quiz.questions().get(0).options().get(0).id()),
                q2, Set.of(quiz.questions().get(1).options().get(0).id(), quiz.questions().get(1).options().get(1).id()))));
        quizService.take(quiz.id(), new TakeQuizRequest(boris, Map.of(
                q1, Set.of(quiz.questions().get(0).options().get(1).id()))));

        reviewService.add(hibernateCourse, new ReviewRequest(anna, 5, "Очень понятно объяснена ленивая загрузка"));
        log.info("Demo data created: 6 users, 2 courses, assignments, quiz, submissions, review");
    }
}
