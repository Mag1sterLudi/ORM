package com.learning.platform.repository;

import com.learning.platform.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<Tag, Long> {

    boolean existsByNameIgnoreCase(String name);
}
