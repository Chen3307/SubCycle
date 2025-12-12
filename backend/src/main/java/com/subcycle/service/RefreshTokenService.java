package com.subcycle.service;

import com.subcycle.entity.RefreshToken;
import com.subcycle.entity.User;
import com.subcycle.repository.RefreshTokenRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class RefreshTokenService {

    // Refresh token 有效期：7 天
    private static final int REFRESH_TOKEN_VALIDITY_DAYS = 7;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    /**
     * 為用戶創建新的 refresh token
     */
    @Transactional
    public String createRefreshToken(User user) {
        // 刪除該用戶舊的 refresh token
        refreshTokenRepository.deleteByUser(user);

        // 創建新的 refresh token
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiresAt(LocalDateTime.now().plusDays(REFRESH_TOKEN_VALIDITY_DAYS));
        refreshToken.setRevoked(false);

        refreshTokenRepository.save(refreshToken);

        return refreshToken.getToken();
    }

    /**
     * 驗證並獲取 refresh token
     */
    public RefreshToken verifyRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "無效的 Refresh Token"));

        if (refreshToken.isRevoked()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh Token 已被撤銷");
        }

        if (refreshToken.isExpired()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh Token 已過期");
        }

        return refreshToken;
    }

    /**
     * 撤銷用戶的 refresh token
     */
    @Transactional
    public void revokeRefreshToken(User user) {
        refreshTokenRepository.deleteByUser(user);
    }

    /**
     * 清理過期的 refresh tokens
     */
    @Transactional
    public void cleanupExpiredTokens() {
        refreshTokenRepository.deleteExpiredTokens(LocalDateTime.now());
    }
}
