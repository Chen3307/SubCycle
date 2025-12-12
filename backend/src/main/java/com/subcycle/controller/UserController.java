package com.subcycle.controller;

import com.subcycle.dto.ChangePasswordRequest;
import com.subcycle.dto.UpdateProfileRequest;
import com.subcycle.dto.UserResponse;
import com.subcycle.entity.User;
import com.subcycle.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<UserResponse> getProfile(
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(userService.getUserProfile(user));
    }

    @PutMapping("/profile")
    public ResponseEntity<UserResponse> updateProfile(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        return ResponseEntity.ok(userService.updateProfile(user, request));
    }

    @PutMapping("/password")
    public ResponseEntity<Map<String, String>> changePassword(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        userService.changePassword(user, request);
        return ResponseEntity.ok(Map.of("message", "密碼修改成功"));
    }
}
