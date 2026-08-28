package com.azure.libraryms.common.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "auth")
public record AuthProperties(
    Integer loginMaxAttempts,
    Integer loginAttemptsTimeout,
    Integer loginLockoutMinutes
) {
}
