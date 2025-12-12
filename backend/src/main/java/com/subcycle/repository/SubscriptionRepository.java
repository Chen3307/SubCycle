package com.subcycle.repository;

import com.subcycle.entity.Subscription;
import com.subcycle.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long>, JpaSpecificationExecutor<Subscription> {
    List<Subscription> findByUserOrderByNextPaymentDateAsc(User user);
    Optional<Subscription> findByIdAndUser(Long id, User user);
    Page<Subscription> findByUser(User user, Pageable pageable);
}
