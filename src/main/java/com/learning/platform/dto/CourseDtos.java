package com.learning.platform.dto;

import com.learning.platform.dto.CatalogDtos.CategoryResponse;
import com.learning.platform.dto.CatalogDtos.TagResponse;
import com.learning.platform.dto.UserDtos.UserResponse;
import com.learning.platform.entity.Course;
import com.learning.platform.entity.CourseModule;
import com.learning.platform.entity.Lesson;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/** DTO для курсов, модулей и уроков */
public final class CourseDtos {

    private CourseDtos() {
    }

    /** Создание/обновление курса. Модули (с уроками) можно передать сразу при создании */
    public record CourseRequest(
            @NotBlank @Size(max = 200) String title,
            @Size(max = 4000) String description,
            @Min(1) Integer durationWeeks,
            LocalDate startDate,
            Long categoryId,
            @NotNull Long teacherId,
            Set<Long> tagIds,
            List<@Valid ModuleRequest> modules) {
    }

    public record ModuleRequest(
            @NotBlank @Size(max = 200) String title,
            @Size(max = 2000) String description,
            @NotNull @Min(1) Integer orderIndex,
            List<@Valid LessonRequest> lessons) {
    }

    public record LessonRequest(
            @NotBlank @Size(max = 200) String title,
            String content,
            @Size(max = 500) String videoUrl) {
    }

    public record LessonResponse(Long id, Long moduleId, String title, String content, String videoUrl) {
        public static LessonResponse from(Lesson l) {
            return new LessonResponse(l.getId(), l.getModule().getId(), l.getTitle(), l.getContent(), l.getVideoUrl());
        }
    }

    public record ModuleResponse(Long id, Long courseId, String title, String description, int orderIndex) {
        public static ModuleResponse from(CourseModule m) {
            return new ModuleResponse(m.getId(), m.getCourse().getId(), m.getTitle(), m.getDescription(), m.getOrderIndex());
        }
    }

    /** Модуль вместе с уроками и ссылкой на тест (для структуры курса) */
    public record ModuleStructure(Long id, String title, String description, int orderIndex,
                                  Long quizId, List<LessonResponse> lessons) {
        public static ModuleStructure from(CourseModule m) {
            return new ModuleStructure(m.getId(), m.getTitle(), m.getDescription(), m.getOrderIndex(),
                    m.getQuiz() == null ? null : m.getQuiz().getId(),
                    m.getLessons().stream().map(LessonResponse::from).toList());
        }
    }

    public record CourseResponse(Long id, String title, String description, Integer durationWeeks, LocalDate startDate,
                                 CategoryResponse category, UserResponse teacher, List<TagResponse> tags,
                                 Double averageRating) {
        /** Вызывать внутри транзакции - teacher/category/tags - ленивые связи */
        public static CourseResponse from(Course c, Double averageRating) {
            return new CourseResponse(c.getId(), c.getTitle(), c.getDescription(), c.getDurationWeeks(), c.getStartDate(),
                    CategoryResponse.from(c.getCategory()), UserResponse.from(c.getTeacher()),
                    c.getTags().stream().map(TagResponse::from).sorted(Comparator.comparing(TagResponse::name)).toList(),
                    averageRating);
        }
    }

    /** Полная структура курса - курс -> модули -> уроки */
    public record CourseStructureResponse(Long id, String title, List<ModuleStructure> modules) {
        public static CourseStructureResponse from(Course c) {
            return new CourseStructureResponse(c.getId(), c.getTitle(),
                    c.getModules().stream().map(ModuleStructure::from).toList());
        }
    }
}
