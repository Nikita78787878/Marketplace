package org.example.marketplace.dto.auth;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank
        String username,

        @NotBlank
        @Size(min = 6, max = 20)
        String password) {
}
