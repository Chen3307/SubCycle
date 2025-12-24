package com.subcycle.repository;

import com.subcycle.entity.Category;
import com.subcycle.entity.Subscription;
import com.subcycle.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Optional;
import java.time.LocalDate;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long>, JpaSpecificationExecutor<Subscription> {
    @NonNull
    List<Subscription> findByUserOrderByNextPaymentDateAsc(User user);
    @NonNull
    List<Subscription> findByUser(User user);
    @NonNull
    List<Subscription> findByUserAndCategoryIn(User user, List<Category> categories);
    @NonNull
    List<Subscription> findByUserAndNextPaymentDateBefore(User user, LocalDate date);
    @NonNull
    Optional<Subscription> findByIdAndUser(Long id, User user);
    @NonNull
    Page<Subscription> findByUser(User user, Pageable pageable);
    @NonNull
    List<Subscription> findByUserId(Long userId);
    @NonNull
    List<Subscription> findByNextPaymentDateBefore(LocalDate date);
    @NonNull
    List<Subscription> findByStatus(String status);
    void deleteByUser(User user);
}
