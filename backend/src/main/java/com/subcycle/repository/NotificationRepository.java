package com.subcycle.repository;

import com.subcycle.entity.Notification;
import com.subcycle.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserOrderByCreatedAtDesc(User user);

    List<Notification> findByUserAndIsReadOrderByCreatedAtDesc(User user, Boolean isRead);

    Long countByUserAndIsRead(User user, Boolean isRead);

    Optional<Notification> findByIdAndUser(Long id, User user);
}
