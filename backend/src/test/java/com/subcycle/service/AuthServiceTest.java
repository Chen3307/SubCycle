package com.subcycle.service;

import com.subcycle.dto.AuthResponse;
import com.subcycle.dto.LoginRequest;
import com.subcycle.dto.RegisterRequest;
import com.subcycle.entity.Category;
import com.subcycle.entity.User;
import com.subcycle.repository.CategoryRepository;
import com.subcycle.repository.UserRepository;
import com.subcycle.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setName("Test User");
        testUser.setRole("USER");
        testUser.setIsActive(true);

        registerRequest = new RegisterRequest();
        registerRequest.setEmail("newuser@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setName("New User");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");
    }

    @Test
    void testRegister_Success() {
        // Arrange
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(categoryRepository.findByUser(any(User.class))).thenReturn(Collections.emptyList());
        when(categoryRepository.saveAll(anyList())).thenReturn(Collections.emptyList());
        when(jwtUtil.generateToken(any(User.class))).thenReturn("jwt-token");
        when(refreshTokenService.createRefreshToken(any(User.class))).thenReturn("refresh-token");

        // Act
        AuthResponse response = authService.register(registerRequest);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getToken());
        assertEquals("jwt-token", response.getToken());
        assertEquals("refresh-token", response.getRefreshToken());
        assertNotNull(response.getUserId());
        assertEquals(testUser.getEmail(), response.getEmail());
        assertEquals(testUser.getName(), response.getName());

        verify(userRepository).existsByEmail(registerRequest.getEmail());
        verify(userRepository).save(any(User.class));
        verify(jwtUtil).generateToken(any(User.class));
        verify(refreshTokenService).createRefreshToken(any(User.class));
    }

    @Test
    void testRegister_EmailAlreadyExists() {
        // Arrange
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(true);

        // Act
        AuthResponse response = authService.register(registerRequest);

        // Assert
        assertNull(response.getToken());
        assertEquals("此電子郵件已被註冊", response.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testLogin_Success() {
        // Arrange
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtUtil.generateToken(testUser)).thenReturn("jwt-token");
        when(refreshTokenService.createRefreshToken(testUser)).thenReturn("refresh-token");

        // Act
        AuthResponse response = authService.login(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals("refresh-token", response.getRefreshToken());
        assertEquals(testUser.getId(), response.getUserId());
        assertEquals(testUser.getEmail(), response.getEmail());
        assertEquals(testUser.getName(), response.getName());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).save(testUser);
        verify(jwtUtil).generateToken(testUser);
        verify(refreshTokenService).createRefreshToken(testUser);
    }

    @Test
    void testLogin_InvalidCredentials() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new AuthenticationException("Bad credentials") {});

        // Act
        AuthResponse response = authService.login(loginRequest);

        // Assert
        assertNull(response.getToken());
        assertEquals("電子郵件或密碼錯誤", response.getMessage());
        verify(jwtUtil, never()).generateToken(any(User.class));
        verify(refreshTokenService, never()).createRefreshToken(any(User.class));
    }

    @Test
    void testRefreshAccessToken_Success() {
        // Arrange
        String refreshTokenString = "valid-refresh-token";
        com.subcycle.entity.RefreshToken refreshToken = new com.subcycle.entity.RefreshToken();
        refreshToken.setUser(testUser);
        refreshToken.setToken(refreshTokenString);
        refreshToken.setExpiresAt(LocalDateTime.now().plusDays(7));

        when(refreshTokenService.verifyRefreshToken(refreshTokenString)).thenReturn(refreshToken);
        when(jwtUtil.generateToken(testUser)).thenReturn("new-jwt-token");

        // Act
        AuthResponse response = authService.refreshAccessToken(refreshTokenString);

        // Assert
        assertNotNull(response);
        assertEquals("new-jwt-token", response.getToken());
        assertEquals(refreshTokenString, response.getRefreshToken());
        assertEquals(testUser.getId(), response.getUserId());

        verify(refreshTokenService).verifyRefreshToken(refreshTokenString);
        verify(jwtUtil).generateToken(testUser);
    }

    @Test
    void testLogout_Success() {
        // Arrange
        doNothing().when(refreshTokenService).revokeRefreshToken(testUser);

        // Act
        var result = authService.logout(testUser);

        // Assert
        assertTrue((Boolean) result.get("success"));
        assertEquals("登出成功", result.get("message"));
        verify(refreshTokenService).revokeRefreshToken(testUser);
    }
}
