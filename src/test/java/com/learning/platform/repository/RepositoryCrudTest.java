package com.learning.platform.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.learning.platform.entity.Assignment;
import com.learning.platform.entity.Category;
import com.learning.platform.entity.Course;
import com.learning.platform.entity.CourseModule;
import com.learning.platform.entity.Enrollment;
import com.learning.platform.entity.Lesson;
import com.learning.platform.entity.Submission;
import com.learning.platform.entity.Tag;
import com.learning.platform.entity.User;
import com.learning.platform.support.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

/** CRUD-операции через Spring Data репозитории, каскады и ограничения БД. */
class RepositoryCrudTest extends IntegrationTest {

    @Autowired
    private CourseModuleRepository moduleRepository;
    @Autowired
    private LessonRepository lessonRepository;
    @Autowired
    private AssignmentRepository assignmentRepository;
    @Autowired
    private SubmissionRepository submissionRepository;
    @Autowired
    private TagRepository tagRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private ProfileRepository profileRepository;

    @Test
    void createCourseCascadesModulesLessonsAndAssignments() {
        Course course = courseWithStructure(teacher());
        flushAndClear();

        assertThat(courseRepository.findById(course.getId())).isPresent();
        assertThat(moduleRepository.findByCourseIdOrderByOrderIndexAsc(course.getId())).hasSize(2);
        Long firstModuleId = moduleRepository.findByCourseIdOrderByOrderIndexAsc(course.getId()).get(0).getId();
        assertThat(lessonRepository.findByModuleIdOrderByIdAsc(firstModuleId)).hasSize(2);
        assertThat(assignmentRepository.count()).isEqualTo(1);
    }

    @Test
    void readEnrollmentReferencesCorrectStudentAndCourse() {
        User student = student();
        Course course = courseWithStructure(teacher());
        Enrollment enrollment = enroll(student, course);
        flushAndClear();

        Enrollment loaded = enrollmentRepository.findById(enrollment.getId()).orElseThrow();
        assertThat(loaded.getStudent().getId()).isEqualTo(student.getId());
        assertThat(loaded.getCourse().getId()).isEqualTo(course.getId());
        assertThat(enrollmentRepository.findByCourseId(course.getId())).hasSize(1);
        assertThat(enrollmentRepository.findByStudentId(student.getId())).hasSize(1);
    }

    @Test
    void updateCourseAndGradeSubmissionArePersisted() {
        User student = student();
        Course course = courseWithStructure(teacher());
        Assignment assignment = assignmentRepository.findAll().get(0);
        Submission submission = submissionRepository.save(new Submission(assignment, student, "answer"));
        flushAndClear();

        Course toUpdate = courseRepository.findById(course.getId()).orElseThrow();
        toUpdate.setDescription("Updated description");
        submissionRepository.findById(submission.getId()).orElseThrow().grade(85, "good");
        flushAndClear();

        assertThat(courseRepository.findById(course.getId()).orElseThrow().getDescription()).isEqualTo("Updated description");
        Submission graded = submissionRepository.findById(submission.getId()).orElseThrow();
        assertThat(graded.getScore()).isEqualTo(85);
        assertThat(graded.getFeedback()).isEqualTo("good");
    }

    @Test
    void deleteCourseCascadesContentButKeepsTagsAndUsers() {
        User student = student();
        User teacher = teacher();
        Tag tag = tagRepository.save(new Tag("Java"));
        Course course = courseWithStructure(teacher);
        course.addTag(tag);
        enroll(student, course);
        submissionRepository.save(new Submission(assignmentRepository.findAll().get(0), student, "answer"));
        flushAndClear();

        courseRepository.delete(courseRepository.findById(course.getId()).orElseThrow());
        flushAndClear();

        assertThat(courseRepository.count()).isZero();
        assertThat(moduleRepository.count()).isZero();
        assertThat(lessonRepository.count()).isZero();
        assertThat(assignmentRepository.count()).isZero();
        assertThat(submissionRepository.count()).isZero();
        assertThat(enrollmentRepository.count()).isZero();
        assertThat(tagRepository.findById(tag.getId())).isPresent();
        assertThat(userRepository.findById(student.getId())).isPresent();
    }

    @Test
    void deleteUserCascadesProfileAndEnrollments() {
        User student = student();
        enroll(student, courseWithStructure(teacher()));
        flushAndClear();

        userRepository.delete(userRepository.findById(student.getId()).orElseThrow());
        flushAndClear();

        assertThat(profileRepository.findByUserId(student.getId())).isEmpty();
        assertThat(enrollmentRepository.findByStudentId(student.getId())).isEmpty();
    }

    @Test
    void orphanRemovalDeletesModuleRemovedFromCourse() {
        Course course = courseWithStructure(teacher());
        flushAndClear();

        Course loaded = courseRepository.findById(course.getId()).orElseThrow();
        CourseModule first = loaded.getModules().get(0);
        loaded.removeModule(first);
        flushAndClear();

        assertThat(moduleRepository.findByCourseIdOrderByOrderIndexAsc(course.getId())).hasSize(1);
        assertThat(lessonRepository.count()).isEqualTo(2);
    }

    @Test
    void duplicateEnrollmentViolatesUniqueConstraint() {
        User student = student();
        Course course = courseWithStructure(teacher());
        enroll(student, course);
        flushAndClear();

        Course reloaded = courseRepository.findById(course.getId()).orElseThrow();
        User reloadedStudent = userRepository.findById(student.getId()).orElseThrow();
        assertThatThrownBy(() -> enrollmentRepository.saveAndFlush(new Enrollment(reloadedStudent, reloaded)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void duplicateUserEmailViolatesUniqueConstraint() {
        User existing = student();
        flushAndClear();

        assertThatThrownBy(() -> userRepository.saveAndFlush(
                new User("Copy", existing.getEmail(), existing.getRole())))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void manyToManyTagsAndManyToOneCategoryAreMapped() {
        Category category = categoryRepository.save(new Category("Programming"));
        Tag java = tagRepository.save(new Tag("Java"));
        Tag orm = tagRepository.save(new Tag("ORM"));
        Course course = courseWithStructure(teacher());
        course.setCategory(category);
        course.addTag(java);
        course.addTag(orm);
        flushAndClear();

        Course loaded = courseRepository.findDetailedById(course.getId()).orElseThrow();
        assertThat(loaded.getTags()).extracting(Tag::getName).containsExactlyInAnyOrder("Java", "ORM");
        assertThat(loaded.getCategory().getName()).isEqualTo("Programming");
        assertThat(courseRepository.search(category.getId(), null, "java")).hasSize(1);
        assertThat(courseRepository.search(null, null, "python")).isEmpty();
        assertThat(courseRepository.search(null, null, null)).hasSize(1);
    }

    @Test
    void oneToOneProfileIsLinkedToUser() {
        User user = student();
        flushAndClear();

        User loaded = userRepository.findWithProfileById(user.getId()).orElseThrow();
        assertThat(loaded.getProfile()).isNotNull();
        assertThat(loaded.getProfile().getUser().getId()).isEqualTo(user.getId());
    }

    @Test
    void lessonHasAssignmentsCollection() {
        Course course = courseWithStructure(teacher());
        flushAndClear();

        Lesson lesson = lessonRepository.findAll().stream()
                .filter(l -> l.getTitle().equals("Lesson 1.1")).findFirst().orElseThrow();
        assertThat(lesson.getAssignments()).hasSize(1);
        assertThat(lesson.getModule().getCourse().getId()).isEqualTo(course.getId());
    }
}
