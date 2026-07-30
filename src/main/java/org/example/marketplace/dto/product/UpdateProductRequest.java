package org.example.marketplace.dto.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.util.List;

/**
 * Специально отдельная сущность, чтобы можно было указать  поля категории и тега
 */
public record UpdateProductRequest(

        @NotBlank
        String name,
        @NotBlank
        String description,

        @NotNull
        @Positive
        BigDecimal price,

        @NotNull
        @PositiveOrZero
        Integer stockQuantity,

        @NotNull
        Long categoryId,

        @NotNull
        List<Long> tags
) {
}
