package com.learning.platform.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.learning.platform.dto.UserDtos.CreateUserRequest;
import com.learning.platform.dto.UserDtos.UserResponse;
import com.learning.platform.entity.Role;
import com.learning.platform.entity.User;
import com.learning.platform.exception.ConflictException;
import com.learning.platform.exception.NotFoundException;
import com.learning.platform.repository.CourseRepository;
import com.learning.platform.repository.UserRepository;
import com.learning.platform.service.UserService;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Юнит-тесты сервиса пользователей с мок-репозиториями (Mockito). */
@ExtendWith(MockitoExtension.class)
class ServiceUnitTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private CourseRepository courseRepository;
    @InjectMocks
    private UserService userService;

    @Test
    void createNormalizesEmailAndCreatesProfile() {
        when(userRepository.existsByEmailIgnoreCase("Ann@Mail.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserResponse response = userService.create(new CreateUserRequest("Ann", "Ann@Mail.com", Role.STUDENT, "bio", null, null));

        assertThat(response.email()).isEqualTo("ann@mail.com");
        assertThat(response.profile().bio()).isEqualTo("bio");
    }

    @Test
    void createRejectsTakenEmail() {
        when(userRepository.existsByEmailIgnoreCase("taken@mail.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.create(new CreateUserRequest("A", "taken@mail.com", Role.STUDENT, null, null, null)))
                .isInstanceOf(ConflictException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    void deleteTeacherWithCoursesIsForbidden() {
        User teacher = new User("T", "t@mail.com", Role.TEACHER);
        when(userRepository.findById(1L)).thenReturn(Optional.of(teacher));
        when(courseRepository.existsByTeacherId(1L)).thenReturn(true);

        assertThatThrownBy(() -> userService.delete(1L)).isInstanceOf(ConflictException.class);
        verify(userRepository, never()).delete(any());
    }

    @Test
    void getUnknownUserThrowsNotFound() {
        when(userRepository.findWithProfileById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.get(42L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("User with id 42 not found");
    }
}
