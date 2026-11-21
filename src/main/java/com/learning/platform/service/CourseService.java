package com.learning.platform.service;

import com.learning.platform.dto.CourseDtos.CourseRequest;
import com.learning.platform.dto.CourseDtos.CourseResponse;
import com.learning.platform.dto.CourseDtos.CourseStructureResponse;
import com.learning.platform.entity.Course;
import com.learning.platform.exception.NotFoundException;
import com.learning.platform.repository.CourseRepository;
import com.learning.platform.repository.CourseReviewRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Управление курсами - создание, поиск, обновление, удаление, получение структуры. */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseService {

    private final CourseRepository courseRepository;
    private final CourseReviewRepository reviewRepository;
    private final UserService userService;
    private final CatalogService catalogService;
    private final ContentFactory contentFactory;

    /** Создаёт курс. Переданные модули и уроки сохраняются каскадно вместе с курсом. */
    @Transactional
    public CourseResponse create(CourseRequest request) {
        Course course = new Course();
        applyFields(course, request);
        if (request.modules() != null) {
            request.modules().forEach(m -> course.addModule(contentFactory.module(m)));
        }
        courseRepository.save(course);
        log.info("Created course id={} '{}' with {} modules", course.getId(), course.getTitle(), course.getModules().size());
        return CourseResponse.from(course, null);
    }

    public CourseResponse get(Long id) {
        Course course = courseRepository.findDetailedById(id).orElseThrow(() -> NotFoundException.of("Course", id));
        return CourseResponse.from(course, reviewRepository.averageRating(id));
    }

    /**
     * Полная структура курса. Модули и их тесты загружаются через JOIN FETCH, а уроки догружаются лениво
     * при маппинге в DTO. Это не вызывает LazyInitializationException, потому что метод выполняется внутри транзакции.
     */
    public CourseStructureResponse getStructure(Long id) {
        Course course = courseRepository.findWithModulesById(id).orElseThrow(() -> NotFoundException.of("Course", id));
        return CourseStructureResponse.from(course);
    }

    public List<CourseResponse> search(Long categoryId, Long teacherId, String tag) {
        String tagFilter = tag == null ? null : tag.toLowerCase();
        List<Course> courses = courseRepository.search(categoryId, teacherId, tagFilter);
        Map<Long, Double> ratings = averageRatings(courses);
        return courses.stream()
                .map(c -> CourseResponse.from(c, ratings.get(c.getId())))
                .toList();
    }

    @Transactional
    public CourseResponse update(Long id, CourseRequest request) {
        Course course = getEntity(id);
        applyFields(course, request);
        log.info("Updated course id={}", id);
        return CourseResponse.from(course, reviewRepository.averageRating(id));
    }

    /** Каскадно удаляет модули, уроки, задания, решения, тесты, записи и отзывы курса. */
    @Transactional
    public void delete(Long id) {
        courseRepository.delete(getEntity(id));
        log.info("Deleted course id={}", id);
    }

    Course getEntity(Long id) {
        return courseRepository.findById(id).orElseThrow(() -> NotFoundException.of("Course", id));
    }

    private Map<Long, Double> averageRatings(List<Course> courses) {
        Map<Long, Double> ratings = new HashMap<>();
        if (!courses.isEmpty()) {
            List<Long> ids = courses.stream().map(Course::getId).toList();
            reviewRepository.averageRatings(ids).forEach(row -> ratings.put((Long) row[0], (Double) row[1]));
        }
        return ratings;
    }

    private void applyFields(Course course, CourseRequest request) {
        course.setTitle(request.title());
        course.setDescription(request.description());
        course.setDurationWeeks(request.durationWeeks());
        course.setStartDate(request.startDate());
        course.setTeacher(userService.requireTeacher(request.teacherId()));
        course.setCategory(request.categoryId() == null ? null : catalogService.getCategoryEntity(request.categoryId()));
        Set<Long> tagIds = request.tagIds() == null ? Set.of() : request.tagIds();
        List.copyOf(course.getTags()).forEach(course::removeTag);
        tagIds.forEach(tagId -> course.addTag(catalogService.getTagEntity(tagId)));
    }
}
