package ru.cinimex.task.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@RequiredArgsConstructor
@Component
public class JwtService {
    @Value("${jwt.secret}")
    private String jwtSecret;

    public String generateToken(Authentication authenticate) {
        if (authenticate.getPrincipal() instanceof User user) {
            Map<String, Object> claims = new HashMap<>();
            claims.put("roles", user.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList());

            return Jwts.builder()
                    .claims(claims)
                    .subject(user.getUsername())
                    .issuedAt(new Date(System.currentTimeMillis()))
                    .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 3))
                    .signWith(getSignKey()).compact();
        }
        throw new IllegalArgumentException("Incorrect type of authentication principal");
    }

    public String generateTechToken(OffsetDateTime expiredDate) {
        final Map<String, Object> claims = new HashMap<>();
        claims.put("roles", List.of("ROLE_TECH"));
        return Jwts.builder()
                .claims(claims)
                .subject("tech_user")
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(Date.from(expiredDate.toInstant()))
                .signWith(getSignKey()).compact();
    }

    public String extractUserName(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public List<String> extractRole(String token) {
        return extractClaim(token,
                claims -> claims.get("roles", List.class));
    }

    private SecretKey getSignKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimResolver) {
        final Claims claims = extractAllClaims(token);
        return claimResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
