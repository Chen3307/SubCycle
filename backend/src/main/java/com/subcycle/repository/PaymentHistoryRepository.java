package com.subcycle.repository;

import com.subcycle.entity.PaymentHistory;
import com.subcycle.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PaymentHistoryRepository extends JpaRepository<PaymentHistory, Long> {
    List<PaymentHistory> findByUserOrderByPaymentDateDesc(User user);

    List<PaymentHistory> findByUserAndPaymentDateBetweenOrderByPaymentDateDesc(
            User user, LocalDate startDate, LocalDate endDate);

    List<PaymentHistory> findByUser_IdAndSubscription_IdOrderByPaymentDateDesc(
            Long userId, Long subscriptionId);

    Optional<PaymentHistory> findByIdAndUser(Long id, User user);
}
