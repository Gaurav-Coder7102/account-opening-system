package com.bank.auth.controller;

import com.bank.auth.dto.LoginRequest;
import com.bank.auth.dto.RegisterRequest;
import com.bank.auth.dto.TokenResponse;
import com.bank.auth.dto.UserResponse;
import com.bank.auth.service.AuthService;
import com.bank.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse userResponse = authService.register(request);
        ApiResponse<UserResponse> response = ApiResponse.<UserResponse>builder()
                .success(true)
                .message("User registered successfully")
                .data(userResponse)
                .correlationId(UUID.randomUUID().toString())
                .build();
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(@Valid @RequestBody LoginRequest request) {
        TokenResponse tokenResponse = authService.login(request);
        ApiResponse<TokenResponse> response = ApiResponse.<TokenResponse>builder()
                .success(true)
                .message("Login successful")
                .data(tokenResponse)
                .correlationId(UUID.randomUUID().toString())
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> refresh(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        TokenResponse tokenResponse = authService.refresh(refreshToken);
        ApiResponse<TokenResponse> response = ApiResponse.<TokenResponse>builder()
                .success(true)
                .message("Token refreshed successfully")
                .data(tokenResponse)
                .correlationId(UUID.randomUUID().toString())
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@AuthenticationPrincipal String username) {
        authService.logout(username);
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(true)
                .message("Logout successful")
                .correlationId(UUID.randomUUID().toString())
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<String>> me(@AuthenticationPrincipal String username) {
        ApiResponse<String> response = ApiResponse.<String>builder()
                .success(true)
                .message("User info retrieved")
                .data(username)
                .correlationId(UUID.randomUUID().toString())
                .build();
        return ResponseEntity.ok(response);
    }
}
