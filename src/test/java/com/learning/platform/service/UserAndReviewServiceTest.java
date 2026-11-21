package com.learning.platform.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.learning.platform.dto.ReviewDtos.ReviewRequest;
import com.learning.platform.dto.UserDtos.CreateUserRequest;
import com.learning.platform.dto.UserDtos.ProfileRequest;
import com.learning.platform.dto.UserDtos.UpdateUserRequest;
import com.learning.platform.dto.UserDtos.UserResponse;
import com.learning.platform.entity.Course;
import com.learning.platform.entity.Role;
import com.learning.platform.entity.User;
import com.learning.platform.exception.BusinessRuleException;
import com.learning.platform.exception.ConflictException;
import com.learning.platform.support.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/** Пользователи, профили (1-1) и отзывы о курсах. */
class UserAndReviewServiceTest extends IntegrationTest {

    @Autowired
    private UserService userService;
    @Autowired
    private ReviewService reviewService;
    @Autowired
    private CourseService courseService;

    @Test
    void createsUserWithProfileAndUpdatesProfile() {
        UserResponse created = userService.create(new CreateUserRequest("Anna", "Anna@Mail.com", Role.STUDENT, "bio", null, null));
        userService.updateProfile(created.id(), new ProfileRequest("new bio", "https://avatar", "+7"));
        flushAndClear();

        UserResponse loaded = userService.get(created.id());
        assertThat(loaded.email()).isEqualTo("anna@mail.com");
        assertThat(loaded.profile().bio()).isEqualTo("new bio");
    }

    @Test
    void rejectsDuplicateEmailIgnoringCase() {
        userService.create(new CreateUserRequest("A", "dup@mail.com", Role.STUDENT, null, null, null));

        assertThatThrownBy(() -> userService.create(new CreateUserRequest("B", "DUP@mail.com", Role.STUDENT, null, null, null)))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void cannotDeleteTeacherWithCourses() {
        User teacher = teacher();
        courseWithStructure(teacher);

        assertThatThrownBy(() -> userService.delete(teacher.getId())).isInstanceOf(ConflictException.class);
    }

    @Test
    void cannotChangeRoleOfTeacherWithCourses() {
        User teacher = teacher();
        courseWithStructure(teacher);

        assertThatThrownBy(() -> userService.update(teacher.getId(),
                new UpdateUserRequest(teacher.getName(), teacher.getEmail(), Role.STUDENT)))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void enrolledStudentLeavesSingleReviewAndAverageIsCalculated() {
        User first = student();
        User second = student();
        Course course = courseWithStructure(teacher());
        enroll(first, course);
        enroll(second, course);

        reviewService.add(course.getId(), new ReviewRequest(first.getId(), 5, "Great"));
        reviewService.add(course.getId(), new ReviewRequest(second.getId(), 4, "Good"));
        flushAndClear();

        assertThat(reviewService.listByCourse(course.getId())).hasSize(2);
        assertThat(courseService.get(course.getId()).averageRating()).isEqualTo(4.5);
        assertThat(courseService.search(null, null, null))
                .filteredOn(c -> c.id().equals(course.getId()))
                .extracting(c -> c.averageRating())
                .containsExactly(4.5);
        assertThatThrownBy(() -> reviewService.add(course.getId(), new ReviewRequest(first.getId(), 3, "again")))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void notEnrolledStudentCannotReview() {
        Course course = courseWithStructure(teacher());

        assertThatThrownBy(() -> reviewService.add(course.getId(), new ReviewRequest(student().getId(), 5, null)))
                .isInstanceOf(BusinessRuleException.class);
    }
}
