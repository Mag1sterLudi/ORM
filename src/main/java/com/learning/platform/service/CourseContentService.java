package com.learning.platform.service;

import com.learning.platform.dto.CourseDtos.LessonRequest;
import com.learning.platform.dto.CourseDtos.LessonResponse;
import com.learning.platform.dto.CourseDtos.ModuleRequest;
import com.learning.platform.dto.CourseDtos.ModuleResponse;
import com.learning.platform.entity.Course;
import com.learning.platform.entity.CourseModule;
import com.learning.platform.entity.Lesson;
import com.learning.platform.exception.NotFoundException;
import com.learning.platform.repository.CourseModuleRepository;
import com.learning.platform.repository.LessonRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Управление содержимым курса - модули и уроки. */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseContentService {

    private final CourseModuleRepository moduleRepository;
    private final LessonRepository lessonRepository;
    private final CourseService courseService;
    private final ContentFactory contentFactory;

    @Transactional
    public ModuleResponse addModule(Long courseId, ModuleRequest request) {
        Course course = courseService.getEntity(courseId);
        CourseModule module = contentFactory.module(request);
        course.addModule(module);
        moduleRepository.save(module);
        log.info("Added module id={} to course id={}", module.getId(), courseId);
        return ModuleResponse.from(module);
    }

    public List<ModuleResponse> listModules(Long courseId) {
        courseService.getEntity(courseId);
        return moduleRepository.findByCourseIdOrderByOrderIndexAsc(courseId).stream().map(ModuleResponse::from).toList();
    }

    public ModuleResponse getModule(Long id) {
        return ModuleResponse.from(getModuleEntity(id));
    }

    @Transactional
    public ModuleResponse updateModule(Long id, ModuleRequest request) {
        CourseModule module = getModuleEntity(id);
        module.setTitle(request.title());
        module.setDescription(request.description());
        module.setOrderIndex(request.orderIndex());
        return ModuleResponse.from(module);
    }

    /** Удаляет модуль вместе с уроками, заданиями и тестом. */
    @Transactional
    public void deleteModule(Long id) {
        CourseModule module = getModuleEntity(id);
        module.getCourse().removeModule(module);
        log.info("Deleted module id={}", id);
    }

    @Transactional
    public LessonResponse addLesson(Long moduleId, LessonRequest request) {
        CourseModule module = getModuleEntity(moduleId);
        Lesson lesson = contentFactory.lesson(request);
        module.addLesson(lesson);
        lessonRepository.save(lesson);
        return LessonResponse.from(lesson);
    }

    public List<LessonResponse> listLessons(Long moduleId) {
        getModuleEntity(moduleId);
        return lessonRepository.findByModuleIdOrderByIdAsc(moduleId).stream().map(LessonResponse::from).toList();
    }

    public LessonResponse getLesson(Long id) {
        return LessonResponse.from(getLessonEntity(id));
    }

    @Transactional
    public LessonResponse updateLesson(Long id, LessonRequest request) {
        Lesson lesson = getLessonEntity(id);
        lesson.setTitle(request.title());
        lesson.setContent(request.content());
        lesson.setVideoUrl(request.videoUrl());
        return LessonResponse.from(lesson);
    }

    @Transactional
    public void deleteLesson(Long id) {
        Lesson lesson = getLessonEntity(id);
        lesson.getModule().getLessons().remove(lesson);
        log.info("Deleted lesson id={}", id);
    }

    CourseModule getModuleEntity(Long id) {
        return moduleRepository.findById(id).orElseThrow(() -> NotFoundException.of("Module", id));
    }

    Lesson getLessonEntity(Long id) {
        return lessonRepository.findById(id).orElseThrow(() -> NotFoundException.of("Lesson", id));
    }
}
