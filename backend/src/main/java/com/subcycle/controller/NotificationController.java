package com.subcycle.controller;

import com.subcycle.dto.NotificationRequest;
import com.subcycle.dto.NotificationResponse;
import com.subcycle.entity.User;
import com.subcycle.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getAllNotifications(
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(notificationService.getAllNotifications(user));
    }

    @GetMapping("/unread")
    public ResponseEntity<List<NotificationResponse>> getUnreadNotifications(
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(notificationService.getUnreadNotifications(user));
    }

    @GetMapping("/unread/count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(Map.of("count", notificationService.getUnreadCount(user)));
    }

    @PostMapping
    public ResponseEntity<NotificationResponse> createNotification(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody NotificationRequest request
    ) {
        return ResponseEntity.ok(notificationService.createNotification(user, request));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<NotificationResponse> markAsRead(
            @AuthenticationPrincipal User user,
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(notificationService.markAsRead(user, id));
    }

    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(
            @AuthenticationPrincipal User user
    ) {
        notificationService.markAllAsRead(user);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(
            @AuthenticationPrincipal User user,
            @PathVariable Long id
    ) {
        notificationService.deleteNotification(user, id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/read")
    public ResponseEntity<Void> deleteAllRead(
            @AuthenticationPrincipal User user
    ) {
        notificationService.deleteAllRead(user);
        return ResponseEntity.noContent().build();
    }
}
