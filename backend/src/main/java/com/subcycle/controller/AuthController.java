package com.subcycle.controller;

import com.subcycle.dto.AuthResponse;
import com.subcycle.dto.LoginRequest;
import com.subcycle.dto.RefreshTokenRequest;
import com.subcycle.dto.RegisterRequest;
import com.subcycle.entity.User;
import com.subcycle.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

/**
 * 認證控制器
 */
@Tag(name = "認證", description = "使用者認證相關 API")
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = {
        "http://localhost:5173",
        "http://localhost:5174",
        "http://localhost:3000"
})
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * 用戶註冊
     */
    @Operation(summary = "使用者註冊", description = "建立新的使用者帳戶")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "註冊成功"),
        @ApiResponse(responseCode = "400", description = "請求資料驗證失敗或 Email 已被註冊")
    })
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);

        if (response.getToken() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        return ResponseEntity.ok(response);
    }

    /**
     * 用戶登入
     */
    @Operation(summary = "使用者登入", description = "使用 Email 和密碼登入，返回 JWT Token")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);

        if (response.getToken() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        return ResponseEntity.ok(response);
    }

    /**
     * 使用 Refresh Token 獲取新的 Access Token
     */
    @Operation(summary = "刷新 Access Token", description = "使用 Refresh Token 獲取新的 Access Token")
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse response = authService.refreshAccessToken(request.getRefreshToken());
        return ResponseEntity.ok(response);
    }

    /**
     * 登出
     */
    @Operation(summary = "登出", description = "撤銷使用者的 Refresh Token")
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(authService.logout(user));
    }

    /**
     * 驗證 Email
     */
    @Operation(summary = "驗證 Email", description = "使用驗證 token 驗證使用者的電子郵件地址")
    @GetMapping("/verify-email/{token}")
    public ResponseEntity<Map<String, Object>> verifyEmail(@PathVariable String token) {
        return ResponseEntity.ok(authService.verifyEmail(token));
    }

    /**
     * 重新發送驗證郵件
     */
    @Operation(summary = "重新發送驗證郵件", description = "為指定的 Email 重新發送驗證郵件")
    @PostMapping("/resend-verification")
    public ResponseEntity<Map<String, Object>> resendVerificationEmail(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        return ResponseEntity.ok(authService.resendVerificationEmail(email));
    }
}
