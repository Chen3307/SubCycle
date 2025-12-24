package com.subcycle.service;

import com.subcycle.dto.ChangePasswordRequest;
import com.subcycle.dto.UpdateProfileRequest;
import com.subcycle.dto.UserResponse;
import com.subcycle.entity.User;
import com.subcycle.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@SuppressWarnings("null")
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public UserResponse getUserProfile(User user) {
        return toResponse(user);
    }

    public UserResponse updateProfile(User user, UpdateProfileRequest request) {
        if (request.getName() != null) {
            user.setName(request.getName());
        }
        if (request.getCurrency() != null) {
            user.setCurrency(request.getCurrency());
        }
        if (request.getNotificationDays() != null) {
            if (request.getNotificationDays() < 0 || request.getNotificationDays() > 30) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "通知天數必須在 0-30 之間");
            }
            user.setNotificationDays(request.getNotificationDays());
        }

        user = userRepository.save(user);
        return toResponse(user);
    }

    public void changePassword(User user, ChangePasswordRequest request) {
        // 驗證目前密碼
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "目前密碼錯誤");
        }

        // 檢查新密碼是否與舊密碼相同
        if (request.getCurrentPassword().equals(request.getNewPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "新密碼不能與目前密碼相同");
        }

        // 更新密碼
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
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
    }
}
