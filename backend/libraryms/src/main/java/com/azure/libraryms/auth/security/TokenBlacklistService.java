package com.azure.libraryms.auth.security;

import java.time.Duration;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private static final String PREFIX = "auth:blacklist:";

    private final StringRedisTemplate redisTemplate;
    private final JwtService jwtService;

    public void blacklist(String token) {
        String jti = jwtService.extractJti(token);
        Duration ttl = jwtService.getRemainingValidity(token);
        if (ttl.isNegative() || ttl.isZero()) {
            return;
        }
        redisTemplate.opsForValue().set(PREFIX + jti, "1", ttl);
    }

    public boolean isBlacklisted(String token) {
        String jti = jwtService.extractJti(token);
        return Boolean.TRUE.equals(redisTemplate.hasKey(PREFIX + jti));
    }
}