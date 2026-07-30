package org.example.marketplace.dto.tag;

import jakarta.validation.constraints.NotBlank;

public record TagRequest(
        @NotBlank
        String name) {
}
