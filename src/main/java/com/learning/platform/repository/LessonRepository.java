package com.learning.platform.repository;

import com.learning.platform.entity.Lesson;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LessonRepository extends JpaRepository<Lesson, Long> {

    List<Lesson> findByModuleIdOrderByIdAsc(Long moduleId);
}
