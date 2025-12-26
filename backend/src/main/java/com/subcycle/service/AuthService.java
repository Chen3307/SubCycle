package com.subcycle.service;

import com.subcycle.dto.AuthResponse;
import com.subcycle.dto.LoginRequest;
import com.subcycle.dto.RegisterRequest;
import com.subcycle.entity.User;
import com.subcycle.repository.UserRepository;
import com.subcycle.security.JwtUtil;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * 認證服務
 */
@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private com.subcycle.repository.EmailVerificationTokenRepository emailVerificationTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private EmailService emailService;

    /**
     * 用戶註冊
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // 檢查 email 是否已存在
        if (userRepository.existsByEmail(request.getEmail())) {
            AuthResponse errorResponse = new AuthResponse();
            errorResponse.setMessage("此電子郵件已被註冊");
            return errorResponse;
        }

        // 創建新用戶
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setName(request.getName());
        user.setRole("USER");
        user.setIsActive(true);
        user.setEmailVerified(false); // 預設為未驗證

        user = userRepository.save(user);

        // 建立預設類別
        categoryService.ensureDefaultCategories(user);

        // 生成並發送 Email 驗證
        try {
            sendVerificationEmail(user);
        } catch (Exception e) {
            // Email 發送失敗不應影響註冊流程
            System.err.println("發送驗證郵件失敗: " + e.getMessage());
        }

        // 註冊成功，但需要先驗證 Email 才能登入
        AuthResponse response = new AuthResponse();
        response.setMessage("註冊成功！已發送驗證郵件到您的信箱，請先完成 Email 驗證後再登入");
        response.setUserId(user.getId());
        response.setEmail(user.getEmail());
        response.setName(user.getName());

        return response;
    }

    /**
     * 生成並發送驗證郵件
     */
    private void sendVerificationEmail(User user) throws Exception {
        // 刪除該用戶之前的驗證 token（如果有）
        emailVerificationTokenRepository.deleteByUser(user);

        // 生成新的驗證 token
        String token = UUID.randomUUID().toString();
        com.subcycle.entity.EmailVerificationToken verificationToken = new com.subcycle.entity.EmailVerificationToken();
        verificationToken.setUser(user);
        verificationToken.setToken(token);
        verificationToken.setExpiryDate(LocalDateTime.now().plusHours(24)); // 24 小時有效期

        emailVerificationTokenRepository.save(verificationToken);

        // 生成驗證連結（前端 URL）
        String verificationUrl = "http://localhost:5173/verify-email?token=" + token;

        // 發送郵件
        emailService.sendEmailVerification(user.getEmail(), user.getName(), verificationUrl);
    }

    /**
     * 用戶登入
     */
    public AuthResponse login(LoginRequest request) {
        try {
            // 認證用戶
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            // 獲取用戶
            User user = (User) authentication.getPrincipal();

            if (user.getEmailVerified() == null || !user.getEmailVerified()) {
                AuthResponse errorResponse = new AuthResponse();
                errorResponse.setMessage("請先完成 Email 驗證");
                return errorResponse;
            }

            // 更新最後登入時間
            user.setLastLoginAt(LocalDateTime.now());
            userRepository.save(user);

            // 生成 JWT 和 Refresh Token
            String token = jwtUtil.generateToken(user);
            String refreshToken = refreshTokenService.createRefreshToken(user);

            return new AuthResponse(token, refreshToken, user.getId(), user.getEmail(), user.getName(), user.getRole());

        } catch (AuthenticationException e) {
            AuthResponse errorResponse = new AuthResponse();
            errorResponse.setMessage("電子郵件或密碼錯誤");
            return errorResponse;
        }
    }

    /**
     * 使用 Refresh Token 獲取新的 Access Token
     */
    public AuthResponse refreshAccessToken(String refreshTokenString) {
        com.subcycle.entity.RefreshToken refreshToken = refreshTokenService.verifyRefreshToken(refreshTokenString);

        User user = refreshToken.getUser();

        String newAccessToken = jwtUtil.generateToken(user);

        return new AuthResponse(newAccessToken, refreshTokenString, user.getId(), user.getEmail(), user.getName(), user.getRole());
    }

    /**
     * 登出 - 撤銷 Refresh Token
     */
    public Map<String, Object> logout(User user) {
        refreshTokenService.revokeRefreshToken(user);
        return Map.of("success", true, "message", "登出成功");
    }

    /**
     * 驗證 Email
     */
    @Transactional
    public Map<String, Object> verifyEmail(String token) {
        com.subcycle.entity.EmailVerificationToken verificationToken = emailVerificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "無效的驗證連結"));

        if (verificationToken.isExpired()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "驗證連結已過期，請重新申請驗證郵件");
        }

        // 更新用戶的 emailVerified 狀態
        User user = verificationToken.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);

        // 刪除已使用的 token
        emailVerificationTokenRepository.delete(verificationToken);

        return Map.of(
                "success", true,
                "message", "Email 驗證成功！"
        );
    }

    /**
     * 重新發送驗證郵件
     */
    @Transactional
    public Map<String, Object> resendVerificationEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "找不到此用戶"));

        if (user.getEmailVerified() != null && user.getEmailVerified()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "此 Email 已經驗證過了");
        }

        try {
            sendVerificationEmail(user);
            return Map.of(
                    "success", true,
                    "message", "驗證郵件已重新發送，請檢查您的信箱"
            );
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "發送郵件失敗，請稍後再試");
        }
    }
}
