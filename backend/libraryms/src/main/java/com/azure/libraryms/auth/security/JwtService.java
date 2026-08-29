package com.azure.libraryms.auth.security;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtService {
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token-expiration}")
    private long accessExpiration;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshExpiration;

    public String generateAccessToken(UserDetails user) {
        return generateToken(user, "ACCESS", accessExpiration);
    }

    public String generateRefreshToken(UserDetails user) {
        return generateToken(user, "REFRESH", refreshExpiration);
    }

    public String generateResetToken(UserDetails user) {
        // TODO : Implement a separate expiration for reset tokens if needed
        return generateToken(user, "RESET", refreshExpiration); 
    }

    public String extractEmail(String token) {
        return parseClaims(token).getSubject();
    }

    public boolean isTokenValid(String token, UserDetails user, String expectedType) {
        try {
            Claims claims = parseClaims(token);
            return claims.getSubject().equals(user.getUsername())
                    && expectedType.equals(claims.get("type", String.class))
                    && !claims.getExpiration().before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String extractJti(String token) {
        return parseClaims(token).getId();
    }

    public Duration getRemainingValidity(String token) {
        long diffMs = parseClaims(token).getExpiration().getTime() - System.currentTimeMillis();
        return Duration.ofMillis(Math.max(diffMs, 0));
    }

    private String generateToken(UserDetails user, String type, long expiration) {
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(user.getUsername())
                .claim("type", type)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    public Claims parseClaims(String token) {
        return Jwts.parser().verifyWith(getSigningKey()).build()
                .parseSignedClaims(token).getPayload();
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}
