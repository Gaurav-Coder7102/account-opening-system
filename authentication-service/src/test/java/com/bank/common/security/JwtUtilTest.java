package com.bank.common.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970");
        ReflectionTestUtils.setField(jwtUtil, "jwtExpirationInMs", 900000L); // 15 mins
        jwtUtil.init();
    }

    @Test
    void testGenerateToken() {
        String token = jwtUtil.generateToken("user-123", "testuser", "CUSTOMER");
        assertNotNull(token);
        
        String username = jwtUtil.extractUsername(token);
        assertEquals("testuser", username);
        
        String userId = jwtUtil.extractUserId(token);
        assertEquals("user-123", userId);
        
        String role = jwtUtil.extractRole(token);
        assertEquals("CUSTOMER", role);
        
        assertTrue(jwtUtil.validateToken(token));
    }
}
