package com.subcycle.service;

import com.subcycle.dto.SubscriptionRequest;
import com.subcycle.dto.SubscriptionResponse;
import com.subcycle.entity.Category;
import com.subcycle.entity.Subscription;
import com.subcycle.entity.User;
import com.subcycle.repository.CategoryRepository;
import com.subcycle.repository.SubscriptionRepository;
import com.subcycle.specification.SubscriptionSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SubscriptionService {

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    public List<SubscriptionResponse> getSubscriptions(User user) {
        return subscriptionRepository.findByUserOrderByNextPaymentDateAsc(user)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public Page<SubscriptionResponse> getSubscriptionsWithPagination(
            User user,
            String search,
            String status,
            Long categoryId,
            String billingCycle,
            int page,
            int size,
            String sortBy,
            String sortDirection
    ) {
        // 建立排序
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection)
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        Sort sort = Sort.by(direction, sortBy != null ? sortBy : "nextPaymentDate");

        // 建立分頁
        Pageable pageable = PageRequest.of(page, size, sort);

        // 建立查詢條件
        Specification<Subscription> spec = SubscriptionSpecification.filterSubscriptions(
                user, search, status, categoryId, billingCycle);

        // 執行分頁查詢
        Page<Subscription> subscriptionPage = subscriptionRepository.findAll(spec, pageable);

        // 轉換為 Response
        return subscriptionPage.map(this::toResponse);
    }

    public SubscriptionResponse createSubscription(User user, SubscriptionRequest request) {
        Subscription subscription = new Subscription();
        subscription.setUser(user);
        applyRequest(subscription, user, request);
        subscription = subscriptionRepository.save(subscription);
        return toResponse(subscription);
    }

    public SubscriptionResponse updateSubscription(User user, Long id, SubscriptionRequest request) {
        Subscription subscription = subscriptionRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "找不到訂閱"));

        applyRequest(subscription, user, request);
        subscription = subscriptionRepository.save(subscription);
        return toResponse(subscription);
    }

    public void deleteSubscription(User user, Long id) {
        Subscription subscription = subscriptionRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "找不到訂閱"));
        subscriptionRepository.delete(subscription);
    }

    private void applyRequest(Subscription subscription, User user, SubscriptionRequest request) {
        subscription.setName(request.getName());
        subscription.setPrice(Optional.ofNullable(request.getAmount()).orElse(BigDecimal.ZERO));
        subscription.setBillingCycle(Optional.ofNullable(request.getCycle()).orElse("monthly"));
        subscription.setNextPaymentDate(request.getNextPaymentDate());
        subscription.setStatus(Optional.ofNullable(request.getStatus()).orElse("active"));
        subscription.setDescription(request.getDescription());

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findByIdAndUser(request.getCategoryId(), user)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "類別不存在"));
            subscription.setCategory(category);
        } else {
            subscription.setCategory(null);
        }
    }

    private SubscriptionResponse toResponse(Subscription subscription) {
        return new SubscriptionResponse(
                subscription.getId(),
                subscription.getName(),
                subscription.getPrice(),
                subscription.getBillingCycle(),
                subscription.getNextPaymentDate(),
                subscription.getCategory() != null ? subscription.getCategory().getId() : null,
                subscription.getStatus(),
                subscription.getDescription()
        );
    }
}
