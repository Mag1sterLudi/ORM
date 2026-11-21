package com.learning.platform.dto;

import com.learning.platform.entity.Profile;
import com.learning.platform.entity.Role;
import com.learning.platform.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** DTO для пользователей и профилей */
public final class UserDtos {

    private UserDtos() {
    }

    public record CreateUserRequest(
            @NotBlank @Size(max = 100) String name,
            @NotBlank @Email @Size(max = 150) String email,
            @NotNull Role role,
            @Size(max = 2000) String bio,
            @Size(max = 500) String avatarUrl,
            @Size(max = 30) String phone) {
    }

    public record UpdateUserRequest(
            @NotBlank @Size(max = 100) String name,
            @NotBlank @Email @Size(max = 150) String email,
            @NotNull Role role) {
    }

    public record ProfileRequest(
            @Size(max = 2000) String bio,
            @Size(max = 500) String avatarUrl,
            @Size(max = 30) String phone) {
    }

    public record ProfileResponse(Long id, String bio, String avatarUrl, String phone) {
        public static ProfileResponse from(Profile p) {
            return p == null ? null : new ProfileResponse(p.getId(), p.getBio(), p.getAvatarUrl(), p.getPhone());
        }
    }

    public record UserResponse(Long id, String name, String email, Role role, ProfileResponse profile) {
        /** Краткое представление без профиля */
        public static UserResponse from(User u) {
            return new UserResponse(u.getId(), u.getName(), u.getEmail(), u.getRole(), null);
        }

        /** Полное представление, вызывать внутри транзакции (профиль ленивый) */
        public static UserResponse withProfile(User u) {
            return new UserResponse(u.getId(), u.getName(), u.getEmail(), u.getRole(), ProfileResponse.from(u.getProfile()));
        }
    }
}
