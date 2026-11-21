package com.learning.platform.repository;

import com.learning.platform.entity.Course;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CourseRepository extends JpaRepository<Course, Long> {

    /** Карточка курса - преподаватель, категория и теги подгружаются через EntityGraph. */
    @EntityGraph(attributePaths = {"teacher", "category", "tags"})
    Optional<Course> findDetailedById(Long id);

    /**
     * Курс вместе с модулями и их тестами (JOIN FETCH) - решение проблемы ленивой загрузки коллекции.
     * Тест подтягивается здесь же, иначе Hibernate загрузил бы его отдельным запросом для каждого модуля.
     */
    @Query("select distinct c from Course c left join fetch c.modules m left join fetch m.quiz where c.id = :id")
    Optional<Course> findWithModulesById(@Param("id") Long id);

    /** Список курсов с фильтрами - null означает "без фильтра". Тег передаётся в нижнем регистре. */
    @EntityGraph(attributePaths = {"teacher", "category"})
    @Query("""
            select distinct c from Course c
            left join c.tags t
            where (:categoryId is null or c.category.id = :categoryId)
              and (:teacherId is null or c.teacher.id = :teacherId)
              and (:tag is null or lower(t.name) = :tag)
            order by c.id
            """)
    List<Course> search(@Param("categoryId") Long categoryId,
                        @Param("teacherId") Long teacherId,
                        @Param("tag") String tag);

    boolean existsByTeacherId(Long teacherId);
}
