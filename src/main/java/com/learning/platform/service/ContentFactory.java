package com.learning.platform.service;

import com.learning.platform.dto.CourseDtos.LessonRequest;
import com.learning.platform.dto.CourseDtos.ModuleRequest;
import com.learning.platform.entity.CourseModule;
import com.learning.platform.entity.Lesson;
import org.springframework.stereotype.Component;

/** Создаёт сущности модулей и уроков из запросов (переиспользуется в CourseService и CourseContentService). */
@Component
public class ContentFactory {

    public CourseModule module(ModuleRequest request) {
        CourseModule module = new CourseModule(request.title(), request.description(), request.orderIndex());
        if (request.lessons() != null) {
            request.lessons().forEach(l -> module.addLesson(lesson(l)));
        }
        return module;
    }

    public Lesson lesson(LessonRequest request) {
        return new Lesson(request.title(), request.content(), request.videoUrl());
    }
}
