package com.subcycle.service;

import com.subcycle.dto.PaymentHistoryRequest;
import com.subcycle.dto.PaymentHistoryResponse;
import com.subcycle.entity.PaymentHistory;
import com.subcycle.entity.Subscription;
import com.subcycle.entity.User;
import com.subcycle.repository.PaymentHistoryRepository;
import com.subcycle.repository.SubscriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PaymentHistoryService {

    @Autowired
    private PaymentHistoryRepository paymentHistoryRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    public List<PaymentHistoryResponse> getAllPayments(User user) {
        return paymentHistoryRepository.findByUserOrderByPaymentDateDesc(user).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<PaymentHistoryResponse> getPaymentsByDateRange(User user, LocalDate startDate, LocalDate endDate) {
        return paymentHistoryRepository.findByUserAndPaymentDateBetweenOrderByPaymentDateDesc(user, startDate, endDate)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<PaymentHistoryResponse> getPaymentsBySubscription(User user, Long subscriptionId) {
        // 驗證訂閱是否屬於該使用者
        subscriptionRepository.findByIdAndUser(subscriptionId, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "訂閱不存在或無權限"));

        return paymentHistoryRepository.findByUser_IdAndSubscription_IdOrderByPaymentDateDesc(user.getId(), subscriptionId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public PaymentHistoryResponse createPayment(User user, PaymentHistoryRequest request) {
        // 驗證訂閱是否存在且屬於該使用者
        Subscription subscription = subscriptionRepository.findByIdAndUser(request.getSubscriptionId(), user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "訂閱不存在或無權限"));

        PaymentHistory payment = new PaymentHistory();
        payment.setUser(user);
        payment.setSubscription(subscription);
        payment.setAmount(request.getAmount());
        payment.setPaymentDate(request.getPaymentDate());
        payment.setStatus(request.getStatus());
        payment.setNotes(request.getNotes());

        payment = paymentHistoryRepository.save(payment);
        return toResponse(payment);
    }

    public PaymentHistoryResponse updatePayment(User user, Long id, PaymentHistoryRequest request) {
        PaymentHistory payment = paymentHistoryRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "付款記錄不存在或無權限"));

        // 如果要更新訂閱ID，需驗證新訂閱
        if (request.getSubscriptionId() != null && !request.getSubscriptionId().equals(payment.getSubscription().getId())) {
            Subscription subscription = subscriptionRepository.findByIdAndUser(request.getSubscriptionId(), user)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "訂閱不存在或無權限"));
            payment.setSubscription(subscription);
        }

        payment.setAmount(request.getAmount());
        payment.setPaymentDate(request.getPaymentDate());
        payment.setStatus(request.getStatus());
        payment.setNotes(request.getNotes());

        payment = paymentHistoryRepository.save(payment);
        return toResponse(payment);
    }

    public void deletePayment(User user, Long id) {
        PaymentHistory payment = paymentHistoryRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "付款記錄不存在或無權限"));

        paymentHistoryRepository.delete(payment);
    }

    private PaymentHistoryResponse toResponse(PaymentHistory payment) {
        return new PaymentHistoryResponse(
                payment.getId(),
                payment.getSubscription().getId(),
                payment.getSubscription().getName(),
                payment.getAmount(),
                payment.getPaymentDate(),
                payment.getStatus(),
                payment.getNotes(),
                payment.getCreatedAt()
        );
    }
}
