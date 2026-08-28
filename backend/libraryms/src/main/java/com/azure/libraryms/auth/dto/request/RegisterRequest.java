package com.azure.libraryms.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @Email String email,
        @Size(min = 6, message = "Password must be at least 6 characters long") @NotBlank String password,
        @NotBlank String fullName) {

}
