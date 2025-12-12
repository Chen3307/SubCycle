package com.subcycle.repository;

import com.subcycle.entity.Subscription;
import com.subcycle.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    List<Subscription> findByUserOrderByNextPaymentDateAsc(User user);
    Optional<Subscription> findByIdAndUser(Long id, User user);
}
