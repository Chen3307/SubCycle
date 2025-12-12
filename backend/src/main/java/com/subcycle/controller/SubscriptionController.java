package com.subcycle.controller;

import com.subcycle.dto.SubscriptionRequest;
import com.subcycle.dto.SubscriptionResponse;
import com.subcycle.entity.User;
import com.subcycle.service.SubscriptionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@Tag(name = "訂閱管理", description = "訂閱相關 API")
@SecurityRequirement(name = "bearer-jwt")
@RestController
@RequestMapping("/api/subscriptions")
public class SubscriptionController {

    @Autowired
    private SubscriptionService subscriptionService;

    @GetMapping
    public ResponseEntity<List<SubscriptionResponse>> list(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(subscriptionService.getSubscriptions(user));
    }

    @Operation(summary = "搜尋和篩選訂閱（分頁）",
               description = "支援關鍵字搜尋、狀態篩選、類別篩選、計費週期篩選，並提供分頁和排序功能")
    @GetMapping("/search")
    public ResponseEntity<Page<SubscriptionResponse>> search(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String billingCycle,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "nextPaymentDate") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection
    ) {
        return ResponseEntity.ok(subscriptionService.getSubscriptionsWithPagination(
                user, search, status, categoryId, billingCycle, page, size, sortBy, sortDirection));
    }

    @PostMapping
    public ResponseEntity<SubscriptionResponse> create(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody SubscriptionRequest request
    ) {
        return ResponseEntity.ok(subscriptionService.createSubscription(user, request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SubscriptionResponse> update(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @Valid @RequestBody SubscriptionRequest request
    ) {
        return ResponseEntity.ok(subscriptionService.updateSubscription(user, id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal User user,
            @PathVariable Long id
    ) {
        subscriptionService.deleteSubscription(user, id);
        return ResponseEntity.noContent().build();
    }
}
