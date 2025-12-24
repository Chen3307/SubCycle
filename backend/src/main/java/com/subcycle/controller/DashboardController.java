package com.subcycle.controller;

import com.subcycle.dto.DashboardStatistics;
import com.subcycle.entity.User;
import com.subcycle.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 儀表板統計 API
 */
@Tag(name = "儀表板", description = "儀表板統計數據 API")
@SecurityRequirement(name = "bearer-jwt")
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @Operation(summary = "獲取儀表板統計數據", description = "獲取包含環比、本月進度、Top 5等完整統計數據")
    @GetMapping("/statistics")
    public ResponseEntity<DashboardStatistics> getStatistics(@AuthenticationPrincipal User user) {
        DashboardStatistics statistics = dashboardService.getDashboardStatistics(user);
        return ResponseEntity.ok(statistics);
    }
}
