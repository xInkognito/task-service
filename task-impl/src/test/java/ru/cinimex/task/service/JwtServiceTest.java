package ru.cinimex.task.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.OffsetDateTime;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class JwtServiceTest {

    private JwtService jwtService;
    private final String SECRET = "mySecretKeyForTestingPurposesOnly12345678";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "jwtSecret", SECRET);
    }

    // Вспомогательный метод для тестов
    private String createTestToken(String subject, List<String> roles, long expirationMillis) {
        byte[] keyBytes = SECRET.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        return Jwts.builder()
                .subject(subject)
                .claim("roles", roles)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMillis))
                .signWith(Keys.hmacShaKeyFor(keyBytes))
                .compact();
    }

    @Test
    @DisplayName("isTokenValid - успех для активного токена")
    void isTokenValid_Success() {
        String token = createTestToken("test_user", List.of("ROLE_USER"), 1000 * 60); // +1 мин
        assertTrue(jwtService.isTokenValid(token));
    }

    @Test
    @DisplayName("isTokenValid - ошибка для просроченного токена")
    void isTokenValid_Expired() {
        // Создаем токен, который УЖЕ истек (минус 1 минута)
        String token = createTestToken("test_user", List.of("ROLE_USER"), -60000);
        assertFalse(jwtService.isTokenValid(token));
    }

    @Test
    @DisplayName("extractUserName - корректное извлечение")
    void extractUserName_Success() {
        String token = createTestToken("ivan_ivanov", List.of("ROLE_USER"), 1000 * 60);
        assertEquals("ivan_ivanov", jwtService.extractUserName(token));
    }

    @Test
    @DisplayName("extractRole - корректное извлечение списка ролей")
    void extractRole_Success() {
        List<String> roles = List.of("ROLE_USER", "ROLE_ADMIN");
        String token = createTestToken("user", roles, 1000 * 60);

        List<String> extractedRoles = jwtService.extractRole(token);

        assertEquals(2, extractedRoles.size());
        assertTrue(extractedRoles.contains("ROLE_ADMIN"));
    }
}