package com.subcycle.controller;

import com.subcycle.dto.SubscriptionRequest;
import com.subcycle.dto.SubscriptionResponse;
import com.subcycle.entity.User;
import com.subcycle.service.SubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subscriptions")
public class SubscriptionController {

    @Autowired
    private SubscriptionService subscriptionService;

    @GetMapping
    public ResponseEntity<List<SubscriptionResponse>> list(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(subscriptionService.getSubscriptions(user));
    }

    @PostMapping
    public ResponseEntity<SubscriptionResponse> create(
            @AuthenticationPrincipal User user,
            @RequestBody SubscriptionRequest request
    ) {
        return ResponseEntity.ok(subscriptionService.createSubscription(user, request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SubscriptionResponse> update(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @RequestBody SubscriptionRequest request
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
