package com.learning.platform.repository;

import com.learning.platform.entity.Quiz;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface QuizRepository extends JpaRepository<Quiz, Long> {

    boolean existsByModuleId(Long moduleId);

    /** Тест с вопросами, варианты ответов догружаются лениво внутри транзакции */
    @Query("select distinct q from Quiz q left join fetch q.questions join fetch q.module m join fetch m.course where q.id = :id")
    Optional<Quiz> findWithQuestionsById(@Param("id") Long id);
}
