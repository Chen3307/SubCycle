package com.subcycle.service;

import com.subcycle.dto.AuthResponse;
import com.subcycle.dto.LoginRequest;
import com.subcycle.dto.RegisterRequest;
import com.subcycle.dto.ForgotPasswordRequest;
import com.subcycle.dto.ResetPasswordRequest;
import com.subcycle.entity.User;
import com.subcycle.entity.Category;
import com.subcycle.entity.PasswordResetToken;
import com.subcycle.repository.UserRepository;
import com.subcycle.repository.CategoryRepository;
import com.subcycle.repository.PasswordResetTokenRepository;
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
import java.util.Arrays;
import java.util.List;
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
    private CategoryRepository categoryRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private RefreshTokenService refreshTokenService;

    /**
     * 用戶註冊
     */
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

        user = userRepository.save(user);

        // 建立預設類別
        createDefaultCategories(user);

        // 生成 JWT 和 Refresh Token
        String token = jwtUtil.generateToken(user);
        String refreshToken = refreshTokenService.createRefreshToken(user);

        return new AuthResponse(token, refreshToken, user.getId(), user.getEmail(), user.getName());
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

            // 更新最後登入時間
            user.setLastLoginAt(LocalDateTime.now());
            userRepository.save(user);

            // 生成 JWT 和 Refresh Token
            String token = jwtUtil.generateToken(user);
            String refreshToken = refreshTokenService.createRefreshToken(user);

            return new AuthResponse(token, refreshToken, user.getId(), user.getEmail(), user.getName());

        } catch (AuthenticationException e) {
            AuthResponse errorResponse = new AuthResponse();
            errorResponse.setMessage("電子郵件或密碼錯誤");
            return errorResponse;
        }
    }

    /**
     * 為新用戶建立預設類別
     */
    private void createDefaultCategories(User user) {
        if (!categoryRepository.findByUser(user).isEmpty()) {
            return; // 已有類別則跳過
        }

        List<Category> defaults = Arrays.asList(
            buildCategory(user, "串流影音", "play-circle", "#EF4444", 1),
            buildCategory(user, "音樂", "music", "#F59E0B", 2),
            buildCategory(user, "雲端儲存", "cloud", "#10B981", 3),
            buildCategory(user, "生產力工具", "briefcase", "#3B82F6", 4),
            buildCategory(user, "遊戲", "gamepad", "#8B5CF6", 5),
            buildCategory(user, "健康", "heart", "#F56C6C", 6)
        );

        categoryRepository.saveAll(defaults);
    }

    private Category buildCategory(User user, String name, String icon, String color, int sortOrder) {
        Category category = new Category();
        category.setUser(user);
        category.setName(name);
        category.setIcon(icon);
        category.setColor(color);
        category.setSortOrder(sortOrder);
        return category;
    }

    /**
     * 忘記密碼 - 生成重設 token
     */
    @Transactional
    public Map<String, Object> forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail()).orElse(null);

        // 為了安全性，不論使用者是否存在，都返回成功訊息
        // 這樣可以避免透過此 API 探測哪些 email 已註冊
        if (user == null) {
            return Map.of("success", true, "message", "如果該電子郵件存在，我們已發送重置連結");
        }

        // 刪除該用戶之前的重設 token（如果有）
        passwordResetTokenRepository.deleteByUser(user);

        // 生成新的重設 token
        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setUser(user);
        resetToken.setToken(token);
        resetToken.setExpiryDate(LocalDateTime.now().plusHours(1)); // 1 小時有效期

        passwordResetTokenRepository.save(resetToken);

        // TODO: 在實際環境中，這裡應該發送 Email
        // 暫時在開發環境中返回 token（正式環境請移除）
        return Map.of(
                "success", true,
                "message", "如果該電子郵件存在，我們已發送重置連結",
                "token", token  // 開發用，正式環境請移除此行
        );
    }

    /**
     * 驗證重設 token 是否有效
     */
    public Map<String, Object> verifyResetToken(String token) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "無效的重置連結"));

        if (resetToken.isExpired()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "重置連結已過期");
        }

        return Map.of(
                "success", true,
                "email", resetToken.getUser().getEmail()
        );
    }

    /**
     * 重設密碼
     */
    @Transactional
    public Map<String, Object> resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "無效的重置連結"));

        if (resetToken.isExpired()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "重置連結已過期");
        }

        // 更新密碼
        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // 刪除已使用的 token
        passwordResetTokenRepository.delete(resetToken);

        return Map.of("success", true, "message", "密碼重置成功");
    }

    /**
     * 使用 Refresh Token 獲取新的 Access Token
     */
    public AuthResponse refreshAccessToken(String refreshTokenString) {
        // 驗證 refresh token
        com.subcycle.entity.RefreshToken refreshToken = refreshTokenService.verifyRefreshToken(refreshTokenString);

        User user = refreshToken.getUser();

        // 生成新的 Access Token（保留原有的 Refresh Token）
        String newAccessToken = jwtUtil.generateToken(user);

        return new AuthResponse(newAccessToken, refreshTokenString, user.getId(), user.getEmail(), user.getName());
    }

    /**
     * 登出 - 撤銷 Refresh Token
     */
    public Map<String, Object> logout(User user) {
        refreshTokenService.revokeRefreshToken(user);
        return Map.of("success", true, "message", "登出成功");
    }
}
