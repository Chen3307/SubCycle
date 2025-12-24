package com.subcycle.controller;

import com.subcycle.dto.SubscriptionTemplateResponse;
import com.subcycle.service.SubscriptionTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 訂閱模板控制器
 * 提供預設訂閱項目供用戶選擇
 */
@RestController
@RequestMapping("/api/subscription-templates")
public class SubscriptionTemplateController {

    @Autowired
    private SubscriptionTemplateService templateService;

    /**
     * 獲取所有訂閱模板
     */
    @GetMapping
    public ResponseEntity<List<SubscriptionTemplateResponse>> getAllTemplates() {
        return ResponseEntity.ok(templateService.getAllTemplates());
    }

    /**
     * 根據類別獲取訂閱模板
     */
    @GetMapping("/category/{categoryName}")
    public ResponseEntity<List<SubscriptionTemplateResponse>> getTemplatesByCategory(
            @PathVariable String categoryName) {
        return ResponseEntity.ok(templateService.getTemplatesByCategory(categoryName));
    }

    /**
     * 搜尋訂閱模板
     */
    @GetMapping("/search")
    public ResponseEntity<List<SubscriptionTemplateResponse>> searchTemplates(
            @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(templateService.searchTemplates(keyword));
    }
}
