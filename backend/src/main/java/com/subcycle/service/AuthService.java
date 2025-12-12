package com.subcycle.service;

import com.subcycle.dto.AuthResponse;
import com.subcycle.dto.LoginRequest;
import com.subcycle.dto.RegisterRequest;
import com.subcycle.entity.User;
import com.subcycle.entity.Category;
import com.subcycle.repository.UserRepository;
import com.subcycle.repository.CategoryRepository;
import com.subcycle.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.time.LocalDateTime;

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
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthenticationManager authenticationManager;

    /**
     * 用戶註冊
     */
    public AuthResponse register(RegisterRequest request) {
        // 檢查 email 是否已存在
        if (userRepository.existsByEmail(request.getEmail())) {
            return new AuthResponse(null, null, null, null, "此電子郵件已被註冊");
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

        // 生成 JWT
        String token = jwtUtil.generateToken(user);

        return new AuthResponse(token, user.getId(), user.getEmail(), user.getName());
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

            // 生成 JWT
            String token = jwtUtil.generateToken(user);

            return new AuthResponse(token, user.getId(), user.getEmail(), user.getName());

        } catch (AuthenticationException e) {
            return new AuthResponse(null, null, null, null, "電子郵件或密碼錯誤");
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
}
