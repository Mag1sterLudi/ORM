package com.learning.platform.repository;

import com.learning.platform.entity.Role;
import com.learning.platform.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmailIgnoreCase(String email);

    Optional<User> findByEmailIgnoreCase(String email);

    List<User> findByRoleOrderByIdAsc(Role role);

    /** Загружает пользователя вместе с ленивым профилем одним запросом. */
    @Query("select u from User u left join fetch u.profile where u.id = :id")
    Optional<User> findWithProfileById(Long id);
}
