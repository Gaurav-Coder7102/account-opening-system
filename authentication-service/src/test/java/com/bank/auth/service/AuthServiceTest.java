package com.bank.auth.service;

import com.bank.auth.dto.LoginRequest;
import com.bank.auth.dto.RegisterRequest;
import com.bank.auth.dto.TokenResponse;
import com.bank.auth.dto.UserResponse;
import com.bank.auth.entity.User;
import com.bank.auth.repository.RefreshTokenRepository;
import com.bank.auth.repository.UserRepository;
import com.bank.common.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "jwtExpirationInMs", 900000L);
        ReflectionTestUtils.setField(authService, "refreshExpirationInMs", 604800000L);
    }

    @Test
    void testRegister() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("gaurav.kadam");
        request.setEmail("gaurav@example.com");
        request.setPassword("password123");
        request.setRole("CUSTOMER");

        when(userRepository.existsByUsername(request.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("hashed-pwd");

        User savedUser = User.builder()
                .id(UUID.randomUUID())
                .username("gaurav.kadam")
                .email("gaurav@example.com")
                .role("CUSTOMER")
                .build();
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("gaurav.kadam", response.getUsername());
        assertEquals("CUSTOMER", response.getRole());
    }

    @Test
    void testLogin() {
        LoginRequest request = new LoginRequest();
        request.setUsername("gaurav.kadam");
        request.setPassword("password123");

        User user = User.builder()
                .id(UUID.randomUUID())
                .username("gaurav.kadam")
                .role("CUSTOMER")
                .build();

        when(userRepository.findByUsername(request.getUsername())).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken(any(), any(), any())).thenReturn("mock-jwt-token");
        when(refreshTokenRepository.save(any())).thenReturn(null);

        TokenResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("mock-jwt-token", response.getAccessToken());
        assertNotNull(response.getRefreshToken());
    }
}
