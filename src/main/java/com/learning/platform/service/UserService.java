package com.learning.platform.service;

import com.learning.platform.dto.UserDtos.CreateUserRequest;
import com.learning.platform.dto.UserDtos.ProfileRequest;
import com.learning.platform.dto.UserDtos.UpdateUserRequest;
import com.learning.platform.dto.UserDtos.UserResponse;
import com.learning.platform.entity.Profile;
import com.learning.platform.entity.Role;
import com.learning.platform.entity.User;
import com.learning.platform.exception.BusinessRuleException;
import com.learning.platform.exception.ConflictException;
import com.learning.platform.exception.NotFoundException;
import com.learning.platform.repository.CourseRepository;
import com.learning.platform.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Управление пользователями и их профилями. */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;

    @Transactional
    public UserResponse create(CreateUserRequest request) {
        ensureEmailFree(request.email());
        User user = new User(request.name(), request.email().toLowerCase(), request.role());
        user.attachProfile(new Profile(request.bio(), request.avatarUrl(), request.phone()));
        userRepository.save(user);
        log.info("Created user id={} role={}", user.getId(), user.getRole());
        return UserResponse.withProfile(user);
    }

    public UserResponse get(Long id) {
        User user = userRepository.findWithProfileById(id).orElseThrow(() -> NotFoundException.of("User", id));
        return UserResponse.withProfile(user);
    }

    public List<UserResponse> list(Role role) {
        List<User> users = role == null ? userRepository.findAll(Sort.by("id")) : userRepository.findByRoleOrderByIdAsc(role);
        return users.stream().map(UserResponse::from).toList();
    }

    @Transactional
    public UserResponse update(Long id, UpdateUserRequest request) {
        User user = getEntity(id);
        if (!user.getEmail().equalsIgnoreCase(request.email())) {
            ensureEmailFree(request.email());
        }
        if (user.getRole() == Role.TEACHER && request.role() != Role.TEACHER && courseRepository.existsByTeacherId(id)) {
            throw new ConflictException("User " + id + " teaches courses; reassign them before changing the role");
        }
        user.setName(request.name());
        user.setEmail(request.email().toLowerCase());
        user.setRole(request.role());
        return UserResponse.withProfile(user);
    }

    @Transactional
    public UserResponse updateProfile(Long id, ProfileRequest request) {
        User user = userRepository.findWithProfileById(id).orElseThrow(() -> NotFoundException.of("User", id));
        Profile profile = user.getProfile();
        if (profile == null) {
            user.attachProfile(new Profile(request.bio(), request.avatarUrl(), request.phone()));
        } else {
            profile.setBio(request.bio());
            profile.setAvatarUrl(request.avatarUrl());
            profile.setPhone(request.phone());
        }
        return UserResponse.withProfile(user);
    }

    /** Удаляет пользователя вместе с его записями, решениями и результатами. Преподавателя с курсами удалить нельзя. */
    @Transactional
    public void delete(Long id) {
        User user = getEntity(id);
        if (courseRepository.existsByTeacherId(id)) {
            throw new ConflictException("User " + id + " teaches courses; reassign or delete them first");
        }
        userRepository.delete(user);
        log.info("Deleted user id={}", id);
    }

    User getEntity(Long id) {
        return userRepository.findById(id).orElseThrow(() -> NotFoundException.of("User", id));
    }

    User requireStudent(Long id) {
        return requireRole(getEntity(id), Role.STUDENT);
    }

    User requireTeacher(Long id) {
        return requireRole(getEntity(id), Role.TEACHER);
    }

    private User requireRole(User user, Role role) {
        if (user.getRole() != role) {
            throw new BusinessRuleException("User " + user.getId() + " must have role " + role);
        }
        return user;
    }

    private void ensureEmailFree(String email) {
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("User with email " + email + " already exists");
        }
    }
}
