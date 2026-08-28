package com.azure.libraryms.auth.dto.response;

public record LoginResponse(String accessToken, String refreshToken) {
    
}
