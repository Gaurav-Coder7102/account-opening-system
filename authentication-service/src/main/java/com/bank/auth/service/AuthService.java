package com.bank.auth.service;

import com.bank.auth.dto.LoginRequest;
import com.bank.auth.dto.RegisterRequest;
import com.bank.auth.dto.TokenResponse;
import com.bank.auth.dto.UserResponse;
import com.bank.auth.entity.RefreshToken;
import com.bank.auth.entity.User;
import com.bank.auth.exception.UserAlreadyExistsException;
import com.bank.auth.repository.RefreshTokenRepository;
import com.bank.auth.repository.UserRepository;
import com.bank.common.exception.BusinessException;
import com.bank.common.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    @Value("${jwt.expiration:900000}")
    private long jwtExpirationInMs;

    @Value("${jwt.refresh-expiration:604800000}")
    private long refreshExpirationInMs;

    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException("Username is already taken!");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email is already in use!");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole().toUpperCase())
                .enabled(true)
                .build();

        user = userRepository.save(user);

        return UserResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .role(user.getRole())
                .build();
    }

    @Transactional
    public TokenResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Invalid username or password");
        }

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BusinessException("User not found", "USER_NOT_FOUND"));

        String accessToken = jwtUtil.generateToken(user.getId().toString(), user.getUsername(), user.getRole());
        String refreshToken = createRefreshToken(user.getId());

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtExpirationInMs / 1000)
                .build();
    }

    @Transactional
    public String createRefreshToken(UUID userId) {
        RefreshToken refreshToken = RefreshToken.builder()
                .userId(userId)
                .token(UUID.randomUUID().toString())
                .expiresAt(LocalDateTime.now().plusSeconds(refreshExpirationInMs / 1000))
                .build();
        
        // Optionally revoke existing tokens
        refreshTokenRepository.deleteByUserId(userId);
        
        refreshTokenRepository.save(refreshToken);
        return refreshToken.getToken();
    }

    @Transactional
    public TokenResponse refresh(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new BusinessException("Refresh token is not in database!", "TOKEN_NOT_FOUND"));

        if (refreshToken.isRevoked() || refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new BusinessException("Refresh token was expired or revoked. Please make a new signin request", "TOKEN_EXPIRED");
        }

        User user = userRepository.findById(refreshToken.getUserId())
                .orElseThrow(() -> new BusinessException("User not found", "USER_NOT_FOUND"));

        String accessToken = jwtUtil.generateToken(user.getId().toString(), user.getUsername(), user.getRole());
        
        // Rotate refresh token
        String newRefreshToken = createRefreshToken(user.getId());

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtExpirationInMs / 1000)
                .build();
    }
    
    @Transactional
    public void logout(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("User not found", "USER_NOT_FOUND"));
        refreshTokenRepository.deleteByUserId(user.getId());
    }
}
