package com.learning.platform.service;

import com.learning.platform.dto.NotificationResponse;
import com.learning.platform.entity.Notification;
import com.learning.platform.entity.User;
import com.learning.platform.exception.NotFoundException;
import com.learning.platform.repository.NotificationRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Внутренние уведомления пользователей. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional
    public void notify(User user, String message) {
        notificationRepository.save(new Notification(user, message));
    }

    public List<NotificationResponse> list(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(NotificationResponse::from)
                .toList();
    }

    @Transactional
    public NotificationResponse markRead(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> NotFoundException.of("Notification", id));
        notification.setRead(true);
        return NotificationResponse.from(notification);
    }
}
