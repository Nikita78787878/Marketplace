package org.example.marketplace.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record CreateProductRequest(
        @NotBlank
        String name,

        @NotBlank
        String description,

        @Positive
        @NotNull
        BigDecimal price,

        @PositiveOrZero
        @NotNull
        Integer stockQuantity,

        @NotNull
        Long categoryId) {
}
