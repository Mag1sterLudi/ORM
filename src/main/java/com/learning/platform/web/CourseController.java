package com.learning.platform.web;

import com.learning.platform.dto.CourseDtos.CourseRequest;
import com.learning.platform.dto.CourseDtos.CourseResponse;
import com.learning.platform.dto.CourseDtos.CourseStructureResponse;
import com.learning.platform.dto.CourseDtos.ModuleRequest;
import com.learning.platform.dto.CourseDtos.ModuleResponse;
import com.learning.platform.dto.EnrollmentResponse;
import com.learning.platform.dto.ReviewDtos.ReviewRequest;
import com.learning.platform.dto.ReviewDtos.ReviewResponse;
import com.learning.platform.service.CourseContentService;
import com.learning.platform.service.CourseService;
import com.learning.platform.service.EnrollmentService;
import com.learning.platform.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Courses", description = "Курсы, их модули, запись студентов и отзывы")
@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;
    private final CourseContentService contentService;
    private final EnrollmentService enrollmentService;
    private final ReviewService reviewService;

    @Operation(summary = "Создать курс (можно сразу с модулями и уроками)")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CourseResponse create(@Valid @RequestBody CourseRequest request) {
        return courseService.create(request);
    }

    @Operation(summary = "Поиск курсов по категории, преподавателю, тегу")
    @GetMapping
    public List<CourseResponse> search(@RequestParam(required = false) Long categoryId,
                                       @RequestParam(required = false) Long teacherId,
                                       @RequestParam(required = false) String tag) {
        return courseService.search(categoryId, teacherId, tag);
    }

    @GetMapping("/{id}")
    public CourseResponse get(@PathVariable Long id) {
        return courseService.get(id);
    }

    @Operation(summary = "Полная структура курса: модули и уроки")
    @GetMapping("/{id}/structure")
    public CourseStructureResponse structure(@PathVariable Long id) {
        return courseService.getStructure(id);
    }

    @PutMapping("/{id}")
    public CourseResponse update(@PathVariable Long id, @Valid @RequestBody CourseRequest request) {
        return courseService.update(id, request);
    }

    @Operation(summary = "Удалить курс со всем содержимым (каскадно)")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        courseService.delete(id);
    }

    @PostMapping("/{id}/modules")
    @ResponseStatus(HttpStatus.CREATED)
    public ModuleResponse addModule(@PathVariable Long id, @Valid @RequestBody ModuleRequest request) {
        return contentService.addModule(id, request);
    }

    @GetMapping("/{id}/modules")
    public List<ModuleResponse> modules(@PathVariable Long id) {
        return contentService.listModules(id);
    }

    @Operation(summary = "Записать студента на курс")
    @PostMapping("/{id}/enroll")
    @ResponseStatus(HttpStatus.CREATED)
    public EnrollmentResponse enroll(@PathVariable Long id, @RequestParam Long userId) {
        return enrollmentService.enroll(id, userId);
    }

    @Operation(summary = "Отписать студента от курса")
    @DeleteMapping("/{id}/enroll")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unenroll(@PathVariable Long id, @RequestParam Long userId) {
        enrollmentService.unenroll(id, userId);
    }

    @Operation(summary = "Отметить курс завершённым для студента")
    @PostMapping("/{id}/complete")
    public EnrollmentResponse complete(@PathVariable Long id, @RequestParam Long userId) {
        return enrollmentService.complete(id, userId);
    }

    @Operation(summary = "Студенты, записанные на курс")
    @GetMapping("/{id}/students")
    public List<EnrollmentResponse> students(@PathVariable Long id) {
        return enrollmentService.studentsOfCourse(id);
    }

    @PostMapping("/{id}/reviews")
    @ResponseStatus(HttpStatus.CREATED)
    public ReviewResponse addReview(@PathVariable Long id, @Valid @RequestBody ReviewRequest request) {
        return reviewService.add(id, request);
    }

    @GetMapping("/{id}/reviews")
    public List<ReviewResponse> reviews(@PathVariable Long id) {
        return reviewService.listByCourse(id);
    }
}
