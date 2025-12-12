package com.subcycle.controller;

import com.subcycle.dto.PaymentHistoryRequest;
import com.subcycle.dto.PaymentHistoryResponse;
import com.subcycle.entity.User;
import com.subcycle.service.PaymentHistoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@Tag(name = "付款歷史", description = "付款記錄相關 API")
@SecurityRequirement(name = "bearer-jwt")
@RestController
@RequestMapping("/api/payments")
public class PaymentHistoryController {

    @Autowired
    private PaymentHistoryService paymentHistoryService;

    @GetMapping
    public ResponseEntity<List<PaymentHistoryResponse>> getAllPayments(
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(paymentHistoryService.getAllPayments(user));
    }

    @GetMapping("/range")
    public ResponseEntity<List<PaymentHistoryResponse>> getPaymentsByDateRange(
            @AuthenticationPrincipal User user,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return ResponseEntity.ok(paymentHistoryService.getPaymentsByDateRange(user, startDate, endDate));
    }

    @GetMapping("/subscription/{subscriptionId}")
    public ResponseEntity<List<PaymentHistoryResponse>> getPaymentsBySubscription(
            @AuthenticationPrincipal User user,
            @PathVariable Long subscriptionId
    ) {
        return ResponseEntity.ok(paymentHistoryService.getPaymentsBySubscription(user, subscriptionId));
    }

    @PostMapping
    public ResponseEntity<PaymentHistoryResponse> createPayment(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody PaymentHistoryRequest request
    ) {
        return ResponseEntity.ok(paymentHistoryService.createPayment(user, request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PaymentHistoryResponse> updatePayment(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @Valid @RequestBody PaymentHistoryRequest request
    ) {
        return ResponseEntity.ok(paymentHistoryService.updatePayment(user, id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePayment(
            @AuthenticationPrincipal User user,
            @PathVariable Long id
    ) {
        paymentHistoryService.deletePayment(user, id);
        return ResponseEntity.noContent().build();
    }
}
