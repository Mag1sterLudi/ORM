package com.learning.platform.dto;

import com.learning.platform.entity.Notification;
import java.time.LocalDateTime;

/** Уведомление пользователя */
public record NotificationResponse(Long id, String message, boolean read, LocalDateTime createdAt) {

    public static NotificationResponse from(Notification n) {
        return new NotificationResponse(n.getId(), n.getMessage(), n.isRead(), n.getCreatedAt());
    }
}
