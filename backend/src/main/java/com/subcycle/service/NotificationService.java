package com.subcycle.service;

import com.subcycle.dto.NotificationRequest;
import com.subcycle.dto.NotificationResponse;
import com.subcycle.entity.Notification;
import com.subcycle.entity.Subscription;
import com.subcycle.entity.User;
import com.subcycle.repository.NotificationRepository;
import com.subcycle.repository.SubscriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    public List<NotificationResponse> getAllNotifications(User user) {
        return notificationRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<NotificationResponse> getUnreadNotifications(User user) {
        return notificationRepository.findByUserAndIsReadOrderByCreatedAtDesc(user, false).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public Long getUnreadCount(User user) {
        return notificationRepository.countByUserAndIsRead(user, false);
    }

    public NotificationResponse createNotification(User user, NotificationRequest request) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType(request.getType());
        notification.setTitle(request.getTitle());
        notification.setMessage(request.getMessage());

        // 如果有訂閱ID，驗證並設置
        if (request.getSubscriptionId() != null) {
            Subscription subscription = subscriptionRepository.findByIdAndUser(request.getSubscriptionId(), user)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "訂閱不存在或無權限"));
            notification.setSubscription(subscription);
        }

        notification = notificationRepository.save(notification);
        return toResponse(notification);
    }

    public NotificationResponse markAsRead(User user, Long id) {
        Notification notification = notificationRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "通知不存在或無權限"));

        notification.setIsRead(true);
        notification = notificationRepository.save(notification);
        return toResponse(notification);
    }

    public void markAllAsRead(User user) {
        List<Notification> notifications = notificationRepository.findByUserAndIsReadOrderByCreatedAtDesc(user, false);
        notifications.forEach(notification -> notification.setIsRead(true));
        notificationRepository.saveAll(notifications);
    }

    public void deleteNotification(User user, Long id) {
        Notification notification = notificationRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "通知不存在或無權限"));

        notificationRepository.delete(notification);
    }

    public void deleteAllRead(User user) {
        List<Notification> readNotifications = notificationRepository.findByUserAndIsReadOrderByCreatedAtDesc(user, true);
        notificationRepository.deleteAll(readNotifications);
    }

    private NotificationResponse toResponse(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getSubscription() != null ? notification.getSubscription().getId() : null,
                notification.getSubscription() != null ? notification.getSubscription().getName() : null,
                notification.getType(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getIsRead(),
                notification.getCreatedAt()
        );
    }
}
