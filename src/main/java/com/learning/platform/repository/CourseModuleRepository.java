package com.learning.platform.repository;

import com.learning.platform.entity.CourseModule;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseModuleRepository extends JpaRepository<CourseModule, Long> {

    List<CourseModule> findByCourseIdOrderByOrderIndexAsc(Long courseId);
}
