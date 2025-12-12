package com.subcycle.controller;

import com.subcycle.dto.CategoryRequest;
import com.subcycle.dto.CategoryResponse;
import com.subcycle.entity.User;
import com.subcycle.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@Tag(name = "類別管理", description = "訂閱類別相關 API")
@SecurityRequirement(name = "bearer-jwt")
@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getCategories(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(categoryService.getCategories(user));
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CategoryRequest request
    ) {
        return ResponseEntity.ok(categoryService.createCategory(user, request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> updateCategory(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest request
    ) {
        return ResponseEntity.ok(categoryService.updateCategory(user, id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(
            @AuthenticationPrincipal User user,
            @PathVariable Long id
    ) {
        categoryService.deleteCategory(user, id);
        return ResponseEntity.noContent().build();
    }
}
