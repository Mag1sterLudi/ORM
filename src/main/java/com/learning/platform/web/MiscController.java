package com.learning.platform.web;

import com.learning.platform.dto.NotificationResponse;
import com.learning.platform.service.NotificationService;
import com.learning.platform.service.ReviewService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Reviews & Notifications")
@RestController
@RequiredArgsConstructor
public class MiscController {

    private final ReviewService reviewService;
    private final NotificationService notificationService;

    @DeleteMapping("/api/reviews/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteReview(@PathVariable Long id) {
        reviewService.delete(id);
    }

    @PutMapping("/api/notifications/{id}/read")
    public NotificationResponse markRead(@PathVariable Long id) {
        return notificationService.markRead(id);
    }
}
