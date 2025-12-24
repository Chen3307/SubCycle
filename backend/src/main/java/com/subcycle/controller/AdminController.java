package com.subcycle.controller;

import com.subcycle.dto.CreateUserRequest;
import com.subcycle.dto.ResetUserPasswordRequest;
import com.subcycle.dto.UpdateUserRequest;
import com.subcycle.dto.UpdateUserStatusRequest;
import com.subcycle.dto.UserResponse;
import com.subcycle.entity.User;
import com.subcycle.repository.CategoryRepository;
import com.subcycle.repository.EmailVerificationTokenRepository;
import com.subcycle.repository.RefreshTokenRepository;
import com.subcycle.repository.SubscriptionRepository;
import com.subcycle.repository.UserRepository;
import com.subcycle.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 管理者控制器
 */
@Tag(name = "管理者", description = "管理者專用 API")
@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = {
        "http://localhost:5173",
        "http://localhost:5174",
        "http://localhost:3000"
})
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;


    @Autowired
    private EmailVerificationTokenRepository emailVerificationTokenRepository;

    @Autowired
    private CategoryService categoryService;

    /**
     * 建立新用戶
     */
    @Operation(summary = "建立用戶", description = "管理者新增使用者帳戶")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/users")
    public ResponseEntity<Map<String, Object>> createUser(
            @Valid @RequestBody CreateUserRequest request
    ) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "此 Email 已被使用");
        }

        if (!"ADMIN".equals(request.getRole()) && !"USER".equals(request.getRole())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "角色必須為 USER 或 ADMIN");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setName(request.getName());
        user.setRole(request.getRole());
        user.setIsActive(request.getIsActive() == null ? true : request.getIsActive());
        user.setEmailVerified(false);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        userRepository.save(user);
        categoryService.ensureDefaultCategories(user);

        UserResponse response = new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getCurrency(),
                user.getNotificationDays(),
                user.getRole(),
                user.getIsActive(),
                user.getEmailVerified(),
                user.getLastLoginAt(),
                user.getCreatedAt()
        );

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "用戶已建立",
                "user", response
        ));
    }

    /**
     * 獲取所有用戶列表（分頁）
     */
    @Operation(summary = "獲取用戶列表", description = "管理者查看所有用戶（分頁、搜尋）")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users")
    public ResponseEntity<Map<String, Object>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<User> userPage;

        if (search != null && !search.trim().isEmpty()) {
            userPage = userRepository.findByEmailContainingOrNameContaining(search, search, pageable);
        } else {
            userPage = userRepository.findAll(pageable);
        }

        Page<UserResponse> responsePage = userPage.map(user -> new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getCurrency(),
                user.getNotificationDays(),
                user.getRole(),
                user.getIsActive(),
                user.getEmailVerified(),
                user.getLastLoginAt(),
                user.getCreatedAt()
        ));

        Map<String, Object> response = new HashMap<>();
        response.put("users", responsePage.getContent());
        response.put("currentPage", responsePage.getNumber());
        response.put("totalPages", responsePage.getTotalPages());
        response.put("totalItems", responsePage.getTotalElements());

        return ResponseEntity.ok(response);
    }

    /**
     * 獲取單一用戶詳情
     */
    /**
     * 更新用戶資訊
     */
    @Operation(summary = "更新用戶資訊", description = "管理者編輯用戶的基本資訊和角色")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/users/{id}")
    public ResponseEntity<Map<String, Object>> updateUser(
            @PathVariable @NonNull Long id,
            @Valid @RequestBody UpdateUserRequest request,
            @AuthenticationPrincipal User admin
    ) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "用戶不存在"));

        // 防止管理者修改自己的角色
        if (user.getId().equals(admin.getId()) && !request.getRole().equals(user.getRole())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "無法修改自己的角色");
        }

        // 檢查 Email 是否已被其他用戶使用
        if (!user.getEmail().equals(request.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "此 Email 已被使用");
            }
            user.setEmail(request.getEmail());
        }

        user.setName(request.getName());
        user.setRole(request.getRole());
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "用戶資訊已更新"
        ));
    }

    /**
     * 停用/啟用用戶
     */
    @Operation(summary = "停用/啟用用戶", description = "管理者啟用或停用用戶帳號")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/users/{id}/status")
    public ResponseEntity<Map<String, Object>> updateUserStatus(
            @PathVariable @NonNull Long id,
            @Valid @RequestBody UpdateUserStatusRequest request,
            @AuthenticationPrincipal User admin
    ) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "用戶不存在"));

        // 防止管理者停用自己
        if (user.getId().equals(admin.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "無法停用自己的帳號");
        }

        user.setIsActive(request.getIsActive());
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);

        String action = request.getIsActive() ? "啟用" : "停用";
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "用戶已" + action
        ));
    }

    /**
     * 管理者重置用戶密碼
     */
    @Operation(summary = "重置用戶密碼", description = "管理者為用戶重置密碼")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/users/{id}/reset-password")
    public ResponseEntity<Map<String, Object>> resetUserPassword(
            @PathVariable @NonNull Long id,
            @Valid @RequestBody ResetUserPasswordRequest request,
            @AuthenticationPrincipal User admin
    ) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "用戶不存在"));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "密碼已重置成功"
        ));
    }

    /**
     * 刪除用戶（永久刪除）
     */
    @Operation(summary = "刪除用戶", description = "管理者永久刪除用戶")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/users/{id}")
    @Transactional
    public ResponseEntity<Map<String, Object>> deleteUser(
            @PathVariable @NonNull Long id,
            @AuthenticationPrincipal User admin
    ) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "用戶不存在"));

        // 防止管理者刪除自己
        if (user.getId().equals(admin.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "無法刪除自己的帳號");
        }

        refreshTokenRepository.deleteByUser(user);
        emailVerificationTokenRepository.deleteByUser(user);
        subscriptionRepository.deleteByUser(user);
        categoryRepository.deleteByUser(user);
        userRepository.delete(user);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "用戶已刪除"
        ));
    }

}
