package com.learning.platform.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.learning.platform.dto.CatalogDtos.CategoryRequest;
import com.learning.platform.dto.CatalogDtos.TagRequest;
import com.learning.platform.dto.CourseDtos.CourseRequest;
import com.learning.platform.dto.CourseDtos.CourseResponse;
import com.learning.platform.dto.CourseDtos.CourseStructureResponse;
import com.learning.platform.dto.CourseDtos.LessonRequest;
import com.learning.platform.dto.CourseDtos.ModuleRequest;
import com.learning.platform.entity.User;
import com.learning.platform.exception.BusinessRuleException;
import com.learning.platform.exception.NotFoundException;
import com.learning.platform.support.IntegrationTest;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/** Управление курсами и их содержимым через сервисный слой. */
class CourseServiceTest extends IntegrationTest {

    @Autowired
    private CourseService courseService;
    @Autowired
    private CourseContentService contentService;
    @Autowired
    private CatalogService catalogService;

    private CourseRequest request(User teacher, Long categoryId, Set<Long> tagIds, List<ModuleRequest> modules) {
        return new CourseRequest("Hibernate Basics", "desc", 4, null, categoryId, teacher.getId(), tagIds, modules);
    }

    @Test
    void createsCourseWithCategoryTeacherTagsAndStructure() {
        User teacher = teacher();
        Long category = catalogService.createCategory(new CategoryRequest("Databases")).id();
        Long tag = catalogService.createTag(new TagRequest("JPA")).id();
        List<ModuleRequest> modules = List.of(
                new ModuleRequest("Intro", null, 1, List.of(new LessonRequest("L1", "c", null), new LessonRequest("L2", "c", null))),
                new ModuleRequest("Relations", null, 2, List.of(new LessonRequest("L3", "c", null))));

        CourseResponse created = courseService.create(request(teacher, category, Set.of(tag), modules));
        flushAndClear();

        CourseResponse loaded = courseService.get(created.id());
        assertThat(loaded.teacher().id()).isEqualTo(teacher.getId());
        assertThat(loaded.category().name()).isEqualTo("Databases");
        assertThat(loaded.tags()).extracting("name").containsExactly("JPA");
        CourseStructureResponse structure = courseService.getStructure(created.id());
        assertThat(structure.modules()).extracting("title").containsExactly("Intro", "Relations");
        assertThat(structure.modules().get(0).lessons()).hasSize(2);
    }

    @Test
    void addsModulesAndLessonsLater() {
        CourseResponse course = courseService.create(request(teacher(), null, null, null));
        Long moduleId = contentService.addModule(course.id(), new ModuleRequest("Added", null, 1, null)).id();
        contentService.addLesson(moduleId, new LessonRequest("New lesson", "text", "https://video"));
        flushAndClear();

        assertThat(courseService.getStructure(course.id()).modules().get(0).lessons())
                .extracting("title").containsExactly("New lesson");
    }

    @Test
    void updatesCourseAndReplacesTeacher() {
        CourseResponse course = courseService.create(request(teacher(), null, null, null));
        User newTeacher = teacher();

        courseService.update(course.id(), new CourseRequest("Renamed", "new desc", 8, null, null, newTeacher.getId(), null, null));
        flushAndClear();

        CourseResponse loaded = courseService.get(course.id());
        assertThat(loaded.title()).isEqualTo("Renamed");
        assertThat(loaded.teacher().id()).isEqualTo(newTeacher.getId());
    }

    @Test
    void rejectsStudentAsTeacher() {
        assertThatThrownBy(() -> courseService.create(request(student(), null, null, null)))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void deleteRemovesCourse() {
        CourseResponse course = courseService.create(request(teacher(), null, null,
                List.of(new ModuleRequest("M", null, 1, List.of(new LessonRequest("L", null, null))))));
        flushAndClear();

        courseService.delete(course.id());
        flushAndClear();

        assertThatThrownBy(() -> courseService.get(course.id())).isInstanceOf(NotFoundException.class);
    }
}
