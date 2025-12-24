package com.subcycle.controller;

import com.subcycle.dto.SubscriptionResponse;
import com.subcycle.entity.Subscription;
import com.subcycle.entity.User;
import com.subcycle.repository.SubscriptionRepository;
import com.subcycle.security.CustomUserDetailsService;
import com.subcycle.service.ExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/export")
@Tag(name = "Export", description = "數據導出 API")
@SecurityRequirement(name = "Bearer Authentication")
public class ExportController {

    @Autowired
    private ExportService exportService;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    /**
     * 導出訂閱列表為 Excel
     */
    @GetMapping("/subscriptions/excel")
    @Operation(summary = "導出訂閱列表為 Excel")
    public ResponseEntity<byte[]> exportSubscriptionsToExcel(Authentication authentication) throws IOException {
        User user = userDetailsService.getUserFromAuthentication(authentication);
        List<Subscription> subscriptions = subscriptionRepository.findByUserId(user.getId());

        List<SubscriptionResponse> responses = subscriptions.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        String displayName = user.getName() != null && !user.getName().isBlank()
                ? user.getName()
                : user.getUsername();
        byte[] excelBytes = exportService.exportSubscriptionsToExcel(responses, displayName);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment",
                "subscriptions_" + LocalDate.now().format(DateTimeFormatter.ISO_DATE) + ".xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .body(excelBytes);
    }

    /**
     * 轉換 Subscription 到 SubscriptionResponse
     */
    private SubscriptionResponse convertToResponse(Subscription subscription) {
        SubscriptionResponse response = new SubscriptionResponse();
        response.setId(subscription.getId());
        response.setName(subscription.getName());
        response.setPrice(subscription.getPrice());
        response.setBillingCycle(subscription.getBillingCycle());
        response.setNextPaymentDate(subscription.getNextPaymentDate());
        response.setStartDate(subscription.getStartDate());
        response.setEndDate(subscription.getEndDate());
        response.setStatus(subscription.getStatus());

        if (subscription.getCategory() != null) {
            com.subcycle.dto.CategoryResponse categoryResponse = new com.subcycle.dto.CategoryResponse();
            categoryResponse.setId(subscription.getCategory().getId());
            categoryResponse.setName(subscription.getCategory().getName());
            categoryResponse.setColor(subscription.getCategory().getColor());
            categoryResponse.setIcon(subscription.getCategory().getIcon());
            categoryResponse.setSortOrder(subscription.getCategory().getSortOrder());
            response.setCategory(categoryResponse);
        }

        return response;
    }
}

